package com.saba.learning.services.order.activity;

import com.saba.currency.SabaCurrency;
import com.saba.exception.LearningMessage;
import com.saba.exception.SabaException;
import com.saba.finder.LearningFinder;
import com.saba.learning.order.LearningOrderServices;
import com.saba.learning.services.common.ServiceObjectReference;
import com.saba.learning.services.order.business.OrderServiceHelper;
import com.saba.locator.Delegates;
import com.saba.locator.ServiceLocator;
import com.saba.order.LearningOrderFullDetail;
import com.saba.order.Order;
import com.saba.order.OrderStatus;
import com.saba.payment.CreditCardPayment;
import com.saba.payment.CreditCardResponse;
import com.saba.payment.CyberSourceConstants;
import com.saba.payment.PaymentAmount;
import com.saba.payment.PaymentAuthorization;
import com.saba.payment.stripe.StripeConstants;
import com.saba.payment.cybersource.CyberSourceHelper;
import com.saba.payment.cybersource.CybersourceAdaptor;
import com.saba.payment.paypal.PayflowPacket;
import com.saba.payment.paypal.PaypalHelper;
import com.saba.payment.paypal.PaypalPacket;
import com.saba.payment.paypal.PaypalProcessor;
import com.saba.payment.paypal.PaypalProcessor.PaypalQueryKey;
import com.saba.payment.paypal.PaypalResponseContext;
import com.saba.payment.stripe.StripeAdaptor;
import com.saba.payment.stripe.StripeHelper;
import com.saba.paymentinfo.CyberSourceInfoDetail;
import com.saba.paymentinfo.PaymentInfo;
import com.saba.paymentinfo.PaymentInfoDetail;
import com.saba.paymentinfo.PaymentStatus;
import com.saba.paymentinfo.PaymentTransactionStatus;
import com.saba.paymentinfo.PaymentType;
import com.saba.paymentinfo.PaypalInfoDetail;
import com.saba.paymentinfo.StripeInfoDetail;
import com.saba.primitives.AddressDetail;
import com.saba.saba3.db.IDbTypes;
import com.saba.skuorder.SKUOrderManager;
import com.saba.util.DataBaseUtil;
import com.saba.util.Debug;
import com.saba.workflow.BaseActivity;
import com.saba.workflow.ProcessContext;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: nikhil
 * Date: 22/1/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentConfirmationActivity extends BaseActivity {
    public static final String PHASE_RETURN = "return";
    public static final String TRANSACTION_ID_PARAM = "TRANSACTION_ID";
    public static final String INVALID_FIELDS = "invalid_fields";
	public static final String REASON_CODE = "reason_code";
	public static final String ERROR_MESSAGE = "message";
    @Resource(name = "sabaOrderReturnUrlCache")
    protected Map urlCache;

    @Override
    public ProcessContext execute(ProcessContext context) throws Exception {
        if(context.getSeedData() instanceof String) {
            String orderId = (String) context.getSeedData();
            PaymentSeed seed = new PaymentSeed(orderId, null, null, true);
            PaymentType paymentType = getPendingPaymentTypeForOrder(seed.getFullOrder(),seed);
            seed.setPaymentMethod(paymentType);
            context.setSeedData(seed);
        }
        if(context.getSeedData() instanceof Map) {
            Map seedMap = (Map) context.getSeedData();
            Order order = (Order)seedMap.get(Order.class);
            PaymentType paymentType = (PaymentType)seedMap.get(PaymentType.class);
            String transactionId = (String)seedMap.get(TRANSACTION_ID_PARAM);
            PaymentSeed seed = new PaymentSeed(order.getId(), paymentType, transactionId, true);
            context.setSeedData(seed);
        }
        if(context.getSeedData() instanceof PaymentSeed) {
            PaymentSeed seed = (PaymentSeed) context.getSeedData();
            OrderStatus orderStatus = seed.getFullOrder().getStatus();
            PaymentType paymentType = getPendingPaymentTypeForOrder(seed.getFullOrder(),seed);
            // Process order only if order is in pending payment status            
            // SPC-33400 : Added condition for receiving payment for Async order flow 
            // Since order still in cache it will be in OpenConfirmed status
            // As soon as order is finalized and auth payment is added it will go in pending payment state
            // Later on when charge will be successful order will go in confirm state again
            // So rather than changing order status in cache additional condition is added over here
            if (OrderStatus.kOpenUnconfirmed.equals(orderStatus) || (!seed.isCartMode() && OrderStatus.kOpenConfirmed.equals(orderStatus))
            		|| (seed instanceof AsyncPaymentSeed && OrderStatus.kOpenConfirmed.equals(orderStatus))) {
            	//CyberSource flow
            	if(PaymentType.kCyberSource.equals(paymentType)){
            		cyberSourceReceivePayment(context);
            	}else if(PaymentType.kStripe.equals(paymentType)){
            		stripeProcessPayment(context);
            	}
            	else{
            		  // PAYPAL & PAYFLOW
            		 paypalAndPayflowReceivePayment(context);
            	}
               
            }
        }
        return context;
    }

    public void paypalAndPayflowReceivePayment(ProcessContext context) throws SabaException {
        PaymentSeed paymentSeed = (PaymentSeed) context.getSeedData();
        if(paymentSeed.getRequest() != null) {
            PaypalResponseContext.setContext(paymentSeed.getRequest());
        }
        PaymentType paymentType = paymentSeed.getPaymentMethod();
        PaymentInfoDetail detail = getPaymentByTypeAndStatus(paymentSeed.getFullOrder(), paymentSeed.getPaymentMethod(), PaymentStatus.kPending);
        String micrositeId =null;
    	if(detail !=null){
    		micrositeId=detail.getMicrossiteId();
    	}
        PaypalProcessor processor = null;
        if(PaymentType.kPayflowLink.equals(paymentType)) {
            processor = PaypalHelper.getPayflowLinkProcessor(micrositeId);
        }else if (PaymentType.kPaypalExpressCheckout.equals(paymentType)) {
            processor = PaypalHelper.getExpressCheckout(micrositeId);
        }else {
            // DO NOT PROCESS IF IT IS NOT PAYPAL PAYMENT
            return;
        }
        PaymentStatus paymentStatus = PaymentStatus.kPending;
        String transactionId = null;
        try {
            PaypalPacket resp = null;
            if(paymentSeed.isRecheckPaymentStatus()) {
                PaypalQueryKey queryKey = PaypalQueryKey.TransactionId;
                String referenceId = paymentSeed.getRetryReferenceId();
                if(referenceId == null) {
                    // TRY SEARCHING TRANSACTION BY PAYMENT TOKEN
                    // If retry reference id was not supplied, restore token from pending payment info of this type.
                   // detail = getPaymentByTypeAndStatus(paymentSeed.getFullOrder(), paymentSeed.getPaymentMethod(), PaymentStatus.kPending);
                    if(detail != null && detail instanceof PaypalInfoDetail) {
                        PaypalInfoDetail paypalInfoDetail = (PaypalInfoDetail) detail;
                        referenceId = paypalInfoDetail.getGatewayTxId();
                        queryKey = PaypalQueryKey.Token;
                    }
                }
                resp = processor.retryTransaction(referenceId, queryKey);

                // No transaction found by TransactionId or Token for Payflow
                if(!resp.isValidTransaction() && PaymentType.kPayflowLink.equals(paymentSeed.getPaymentMethod())) {
                    // TRY SEARCHING TRANSACTION BY ORDER NUMBER SET AS CUSTOMER REFERENCE
                    resp = processor.retryTransaction(paymentSeed.getOrderNumber(), PaypalQueryKey.CustomerReference);
                }

                // Copy original transaction id for Payflow packet. The inquiry transaction id is discarded.
                if(resp instanceof PayflowPacket) {
                    resp.setTransactionId(((PayflowPacket) resp).getOriginalTransactionId());
                }
            }else {
            	// SPC-33400 In case of async order receive only payment authorization
            	if(paymentSeed instanceof AsyncPaymentSeed){
                	resp = processor.receivePaymentAuth();
                }else{
                	resp = processor.receivePayment();
                }
            }

            // Validate packet contents and ensure the transaction is for this order only
            validatePacket(paymentSeed, resp);

            if(resp.isValidTransaction()) {
                // A valid transaction was found
                transactionId = resp.getTransactionId();
                if(resp.isApproved()) {
                    // The transaction has been approved by gateway. Confirm the payment
                    paymentStatus = PaymentStatus.kCompleted;
                }
                updatePaymentInfo(paymentSeed, paymentStatus, resp);
                if(!PaymentStatus.kCompleted.equals(paymentStatus)) {
                    // Gateway has declined the transaction, we can safely cancel the order
                    markPaymentFailure(paymentSeed);
                }
            }else {
                // A valid transaction was not found, do not cancel order.
                // It is responsibility of the caller of this workflow to cancel order
                if(!paymentSeed.isRecheckPaymentStatus()) {
                    markPaymentFailure(paymentSeed);
                }
            }

            context.setAttribute(PaymentStatus.class, paymentStatus);
            context.setResponse(paymentStatus);
            if(!paymentSeed.isRecheckPaymentStatus()) {
                String resultDescription = getResultDescription(resp.getResult(), resp.getResultDescription());
                URI redirectUrl = PaymentHandshakeUrlActivity.getReturnUrl(urlCache, paymentSeed.getOrderId(), paymentSeed.getRequest(), paymentStatus, transactionId, resultDescription);
                context.setAttribute(PaymentConfirmationActivity.class, redirectUrl);
            }
        } catch (PaypalProcessor.PaypalException e) {
        	Debug.printStackTrace(e);
            throw new SabaException(LearningMessage.kPaymentGatewayNotReachable);
        }
    }
    
    //Stripe payment processing
    
    public void stripeProcessPayment(ProcessContext context) throws SabaException {
    	PaymentSeed paymentSeed = (PaymentSeed) context.getSeedData();
    	PaymentStatus paymentStatus = PaymentStatus.kPending;
    	String orderId =paymentSeed.getOrderId();
    	LearningOrderFullDetail fullOrderDetail =paymentSeed.getFullOrder();
    	String stripeToken = paymentSeed.getRequest().getParameter(StripeConstants.kStripeToken);
    	String email = paymentSeed.getRequest().getParameter(StripeConstants.kEmail);
    	String orderNo = fullOrderDetail.getOrderNo();
    	double amount =getPaymentAmount(paymentSeed);
    	PaymentAmount paymentAmount =  new PaymentAmount(amount,fullOrderDetail.getCurrency(),null,ServiceLocator.getClientInstance());
    	CreditCardResponse stripeResponse = null;
    	PaymentInfoDetail detail = getPaymentByTypeAndStatus(fullOrderDetail, paymentSeed.getPaymentMethod(), PaymentStatus.kPending); 
    	String micrositeId =null;
    	if(detail !=null){
    		micrositeId=detail.getMicrossiteId();
    	}
    	StripeAdaptor engine = new StripeAdaptor(micrositeId);
    	try{
    		if(paymentSeed instanceof AsyncPaymentSeed || ! StripeConstants.kStripeCapture.equalsIgnoreCase(StripeHelper.getConfig(micrositeId).getStripePaymentAction())){
        		stripeResponse =  engine.authorize(stripeToken, orderNo, paymentAmount,email,false);
        	}else{
        		stripeResponse =  engine.authorizeAndCapture(stripeToken, orderNo,paymentAmount,email);
        	}
    	}catch(SabaException exception){
    		markPaymentFailure(paymentSeed);
    	}
    	
    	if(stripeResponse !=null){
    		
    		if(detail != null && detail instanceof StripeInfoDetail && StripeConstants.kStripeSuccessMesg.equalsIgnoreCase(stripeResponse.getResultStatusMessage())) {
    			paymentStatus= PaymentStatus.kCompleted;
    			StripeInfoDetail stripeInfoDetail = (StripeInfoDetail) detail;
    			stripeInfoDetail.setGatewayTxId(stripeResponse.getGatewayTxRefId());
    			stripeInfoDetail.setPaymentStatus(paymentStatus);
    			stripeInfoDetail.setPaymentTransactionStatus(PaymentTransactionStatus.kCompleted);
    			//Confirm saba order
    			// SPC-33400 In case of async order only authorization will be performed till now
    			// So payment status and transaction status will be kept as Pending only
    			if(paymentSeed instanceof AsyncPaymentSeed){
    				stripeInfoDetail.setPaymentStatus(PaymentStatus.kPending);
    				stripeInfoDetail.setPaymentTransactionStatus(PaymentTransactionStatus.kPending);
    				paymentSeed.getCachedManager().updateOrderPaymentDetail((PaymentInfo) ServiceLocator.getReference(detail.getId()), detail);
    				paymentSeed.updateCache();
    				updateGatewayPayment(paymentSeed.getOrderId(),detail);
    			}else{
    				updatePaymentItem(orderId, stripeInfoDetail);
    				finalizeSKUOrder(orderId);
    			}
    		}else{
    			markPaymentFailure(paymentSeed);
    		}
    	}
		context.setAttribute(PaymentStatus.class, paymentStatus);
    	context.setResponse(paymentStatus);
    	URI redirectUrl =null;
    	if(stripeResponse !=null)
    		 redirectUrl = PaymentHandshakeUrlActivity.getReturnUrl(urlCache, paymentSeed.getOrderId(), paymentSeed.getRequest(), paymentStatus, stripeResponse.getGatewayTxRefId(),stripeResponse.getResultStatusMessage());
    	else
    		redirectUrl = PaymentHandshakeUrlActivity.getReturnUrl(urlCache, paymentSeed.getOrderId(), paymentSeed.getRequest(), paymentStatus, null,null);
    	context.setAttribute(PaymentConfirmationActivity.class, redirectUrl);
    }

    //CyberSource Payment
    public void cyberSourceReceivePayment(ProcessContext context) throws SabaException {

    	String errorMessageText="";
    	//Added try-catch block to capture any saba exception and avoid no message body writer for SabaException issue
	    try{
	    	PaymentSeed paymentSeed = (PaymentSeed) context.getSeedData();
	    	String orderId =paymentSeed.getOrderId();
	    	LearningOrderFullDetail fullOrderDetail =paymentSeed.getFullOrder();
	    	PaymentStatus paymentStatus = PaymentStatus.kPending;
	    	String statusMessage =null;
	    	String gatewayTxId =null;
	     	String transRefNumber = null;
	    	Map<String, String[]> reqMap =null;
	    	StringBuffer messageString = new StringBuffer();
	    	PaymentInfoDetail detail = getPaymentByTypeAndStatus(fullOrderDetail, paymentSeed.getPaymentMethod(), PaymentStatus.kPending);
	    	String micrositeId =null;
	    	if(detail !=null){
	    		micrositeId=detail.getMicrossiteId();
	    	}
	    	String secretKey =CyberSourceHelper.getMicrositeConfig(micrositeId).getSecretKey();
	    	String message =null;
	    	if(paymentSeed.getRequest()!=null){
	    		reqMap = paymentSeed.getRequest().getParameterMap();
	    	}
	    	if(reqMap!=null && !reqMap.isEmpty()){
	    		//Parse cybersource response from request
	    		//validate signature
	    		//confirm saba order
	    		String respSignature =null;
	    		String signedFields =null;
	    		String successStatus=null;
	    		Set<String> respParam= reqMap.keySet();
	    		if(respParam.contains(CyberSourceConstants.kRequest_signature)){
	    			respSignature=Arrays.toString(reqMap.get(CyberSourceConstants.kRequest_signature)); //This will return value in [abc] format. Convert it to abc.
	    			respSignature=CyberSourceHelper.getStringValue(respSignature);
	    		}
	    		if(respParam.contains(CyberSourceConstants.kResponse_reason_code)) {
	    			successStatus=Arrays.toString(reqMap.get(CyberSourceConstants.kResponse_reason_code));
	    			successStatus=CyberSourceHelper.getStringValue(successStatus);
	    		}
	    		if(respParam.contains(CyberSourceConstants.kResponse_decision)) {
	    			statusMessage=Arrays.toString(reqMap.get(CyberSourceConstants.kResponse_decision));
	    			statusMessage=CyberSourceHelper.getStringValue(statusMessage);
	    		}
	    		if(respParam.contains(CyberSourceConstants.kResponse_transaction_id)){
	    			gatewayTxId=Arrays.toString(reqMap.get(CyberSourceConstants.kResponse_transaction_id));
	    			gatewayTxId=CyberSourceHelper.getStringValue(gatewayTxId);
	    		}
	    		if(respParam.contains(CyberSourceConstants.kBill_Transaction_Reference_No)) {
	    			transRefNumber = Arrays.toString(reqMap.get(CyberSourceConstants.kBill_Transaction_Reference_No));
	    			transRefNumber = CyberSourceHelper.getStringValue(transRefNumber);
	    		}
	    		if(transRefNumber ==null && respParam.contains(CyberSourceConstants.kTransaction_Reference_No)) {
	    			transRefNumber = Arrays.toString(reqMap.get(CyberSourceConstants.kTransaction_Reference_No));
	    			transRefNumber = CyberSourceHelper.getStringValue(transRefNumber);
	    		}
    			
	    		if(respParam.contains(CyberSourceConstants.kRequest_signed_field_names)){
	    			signedFields=Arrays.toString(reqMap.get(CyberSourceConstants.kRequest_signed_field_names));
	    			signedFields=CyberSourceHelper.getStringValue(signedFields);
	    			StringTokenizer tokenizer = new StringTokenizer(signedFields,CyberSourceConstants.kComma);
	    			String tokenKey=null;
	    			String tokenVal =null;
	    			while (tokenizer.hasMoreTokens()) {
	    				tokenKey = tokenizer.nextToken();
	    				tokenVal = Arrays.toString(reqMap.get(tokenKey));
	    				tokenVal = CyberSourceHelper.getStringValue(tokenVal);
	    				messageString.append(tokenKey+CyberSourceConstants.kEqualTo+tokenVal+CyberSourceConstants.kComma);
	    				tokenKey=null;
	    				tokenVal =null;
	    			}
	    			/*  Start : SPC-59040 Proper error message in case of invalid state code while payment with  cybersource*/		
					List<String> tokenList = new ArrayList<>(Arrays.asList(signedFields.split(CyberSourceConstants.kComma)));		
					if(tokenList.contains(REASON_CODE)) {		
						String reason_Code = Arrays.toString(reqMap.get(REASON_CODE));		
						reason_Code = CyberSourceHelper.getStringValue(reason_Code);		
						if (reason_Code.equals("102")) {		
							if (tokenList.contains(INVALID_FIELDS)) {		
								String tokenValue = Arrays.toString(reqMap.get(INVALID_FIELDS));		
								tokenValue = CyberSourceHelper.getStringValue(tokenValue);		
								List<String> invalidFields = new ArrayList<>(Arrays.asList(tokenValue.split(CyberSourceConstants.kComma)));
								String allInvalidFields = StringUtils.collectionToCommaDelimitedString(invalidFields);
								List<String> updatedInvalidFields=CyberSourceHelper.processAVSError(allInvalidFields);
								String finalInvalidFields = StringUtils.collectionToCommaDelimitedString(updatedInvalidFields);
								errorMessageText = new SabaException(LearningMessage.kInvalidFields, finalInvalidFields).getMessage();		
										
							} else {		
								String error_message = Arrays.toString(reqMap.get(ERROR_MESSAGE));		
								error_message = CyberSourceHelper.getStringValue(error_message);		
								errorMessageText = error_message;		
							}		
						}		
					}		
					/* End SPC-59040 */
	    			message= messageString.toString();
	    			if( message!=null && message.endsWith(CyberSourceConstants.kComma)){
	    				message= message.substring(0, message.length()-1);
	    			}
	    		}
	    		//Verify the signature of cybersource gateway response with saba generated signature, if fails then reverse authorize / refund
	    		//If signature verification returns true then Check the success status. if success status is 100 then mark order confirm
	    		String sabaRespSignature = CyberSourceHelper.getHmacSHA256(message, secretKey);
	    		boolean isValidSignature = (sabaRespSignature!=null && sabaRespSignature.equals(respSignature))?Boolean.TRUE:Boolean.FALSE;
	    		
	    		if(!isValidSignature){
	    			//Mark payment failure 
	    			markPaymentFailure(paymentSeed);
	    			//Void authrization / sale
	    				CybersourceAdaptor engine = new CybersourceAdaptor(micrositeId);
	        			String paymentAction=CyberSourceHelper.getMicrositeConfig(micrositeId).getPaymentAction();
	        			float amount =fullOrderDetail.getGrandTotalCharges().floatValue();
	        	        SabaCurrency currency = fullOrderDetail.getCurrency();
	        			PaymentAuthorization pA = new PaymentAuthorization(fullOrderDetail.getOrderNo(),gatewayTxId,null,null,null,null);
	            		CreditCardPayment creditCardPayment = new CreditCardPayment(null,pA);
	            		PaymentAmount pAmt = new PaymentAmount(amount,currency,null,ServiceLocator.getClientInstance());
	            		if(paymentAction!=null && paymentAction.equalsIgnoreCase(CyberSourceConstants.kCyberSourceCapture)){
	            				engine.refund(creditCardPayment, pAmt);
	            		}else{
	            				engine.reverseAuthorize(creditCardPayment, pAmt);
	            		}
	    			
	    		}else if(isValidSignature && CyberSourceConstants.kCyberSourceSuccessStatus.equals(successStatus) && CyberSourceConstants.kCyberSourceStatusMessage.equals(statusMessage)){
	    			
	    			//In case of two way transaction i.e. Auth and then capture make call for capture here.
	    			//Current flow marks payment status completed if auth or capture is Successful based on system settings
	    			//For Registrar flow where auth is done at initial state and capture at later state keep payment status in pending only
	    			
	    			 
	    			if(detail != null && detail instanceof CyberSourceInfoDetail ) {
	    				CyberSourceInfoDetail cyberSourceInfoDetail = (CyberSourceInfoDetail) detail;
						paymentStatus = PaymentStatus.kCompleted;
	    				cyberSourceInfoDetail.setGatewayTxId(gatewayTxId);
						cyberSourceInfoDetail.setCardTxResultCode(Integer.valueOf(successStatus));
	    				cyberSourceInfoDetail.setPaymentStatus(PaymentStatus.kCompleted);
	    				cyberSourceInfoDetail.setPaymentTransactionStatus(PaymentTransactionStatus.kCompleted);
	    				cyberSourceInfoDetail.setCcTxRefNo(transRefNumber);
	    				//Confirm saba order
	    				// SPC-33400 In case of async order only authorization will be performed till now
	    				// So payment status and transaction status will be kept as Pending only
	    				if(paymentSeed instanceof AsyncPaymentSeed){
	        				cyberSourceInfoDetail.setPaymentStatus(PaymentStatus.kPending);
	        				cyberSourceInfoDetail.setPaymentTransactionStatus(PaymentTransactionStatus.kPending);
	        				paymentSeed.getCachedManager().updateOrderPaymentDetail((PaymentInfo) ServiceLocator.getReference(detail.getId()), detail);
	        				paymentSeed.updateCache();
	        				updateGatewayPayment(paymentSeed.getOrderId(),detail);
	    				}else{
	    					updatePaymentItem(orderId, cyberSourceInfoDetail);
	    					finalizeSKUOrder(orderId);
	    				}
	    			}
	    		}else{
	    				//Mark payment failure 
		    			markPaymentFailure(paymentSeed);
	    		}
	    	}else{
	    		markPaymentFailure(paymentSeed);//In case of null response from cybersource gateway
	    	}
	
	    	context.setAttribute(PaymentStatus.class, paymentStatus);
	    	context.setResponse(paymentStatus);
	    	if(errorMessageText != null) {		    	
	    		HttpServletRequest request = paymentSeed.getRequest();		
	    		request.setAttribute(PaymentHandshakeUrlActivity.kCYBERSORUCE_ERROR_MESSAGE, errorMessageText);		
	    	}
	    	URI redirectUrl = PaymentHandshakeUrlActivity.getReturnUrl(urlCache, paymentSeed.getOrderId(), paymentSeed.getRequest(), paymentStatus, gatewayTxId,statusMessage);
	    	context.setAttribute(PaymentConfirmationActivity.class, redirectUrl);
	    }catch(SabaException e) {   
	    		PaymentSeed paymentSeed = (PaymentSeed) context.getSeedData();
	    		HttpServletRequest request = paymentSeed.getRequest();	
	    		String error = (errorMessageText !=null && !"".equals(errorMessageText))?errorMessageText :e.getMessage();
	    		request.setAttribute(PaymentHandshakeUrlActivity.kCYBERSORUCE_ERROR_MESSAGE, error);
	    		URI redirectUrl = PaymentHandshakeUrlActivity.getReturnUrl(urlCache, paymentSeed.getOrderId(), paymentSeed.getRequest(), null, null,null);
	    		context.setAttribute(PaymentConfirmationActivity.class, redirectUrl);
	    }
    }
    
    private String getResultDescription(String result, String resultDescription) {
        if(resultDescription == null) {
            resultDescription = result;
        }
        return resultDescription;
    }

    private void markPaymentFailure(PaymentSeed paymentSeed) throws SabaException {
    	// SPC-33400 : method signature change so as to make sure cancel order is called
    	// Only in case of sync order and not required for async order
    	if(!(paymentSeed instanceof AsyncPaymentSeed) && paymentSeed.isCartMode()){
    		new OrderServiceHelper(ServiceLocator.getClientInstance()).cancelOrder(new ServiceObjectReference(paymentSeed.getOrderId(), ""));
    	}else if(!paymentSeed.isCartMode()){
    	    LearningOrderServices mgr = (LearningOrderServices) ServiceLocator.getClientInstance().getManager(Delegates.kLearningOrderServices);
    	    PaymentInfoDetail detail = getPaymentByTypeAndStatus(paymentSeed.getFullOrder(), paymentSeed.getPaymentMethod(), PaymentStatus.kPending);
    	    mgr.removeOrderPayment((Order) ServiceLocator.getReference(paymentSeed.getOrderId()), (PaymentInfo) ServiceLocator.getReference(detail.getId()));
    	}
    }

    private boolean updatePaymentDetail(PaymentSeed seed, PaymentStatus status, PaypalPacket packet) throws SabaException {
        boolean updated = false;
        PaymentInfoDetail detail = getPaymentByTypeAndStatus(seed.getFullOrder(), seed.getPaymentMethod(), PaymentStatus.kPending);
        if(detail != null) {
            AddressDetail addressDetail = detail.getAddress();
            if(addressDetail != null) {
                if(packet.getAddress1() != null) {
                    addressDetail.setAddr1(packet.getAddress1());
                }
                if(packet.getCity() != null) {
                    addressDetail.setCity(packet.getCity());
                }
                if(packet.getState() != null) {
                    addressDetail.setState(packet.getState());
                }
                if(packet.getZip() != null) {
                    addressDetail.setZip(packet.getZip());
                }
                if(packet.getCountryCode() != null) {
                    addressDetail.setCountry(packet.getCountryCode());
                }
            }
            // SPC-33400 : Keep payment status as pending in case of async order
            if(!(seed instanceof AsyncPaymentSeed)){
            	detail.setPaymentStatus(status);
            }
            // SPC-33400 : Keep Transaction status as pending in case of async order
            if(PaymentStatus.kCompleted.equals(status) && !(seed instanceof AsyncPaymentSeed )) {
                detail.setPaymentTransactionStatus(PaymentTransactionStatus.kCompleted);
            }
            if(detail instanceof PaypalInfoDetail) {
                ((PaypalInfoDetail) detail).setGatewayTxId(packet.getTransactionId());
                ((PaypalInfoDetail) detail).setResultInfo(getResultDescription(packet.getResult(), packet.getResultDescription()));
            }
            // SPC-33400 In case of async order update payment details in cache only
            if(seed instanceof AsyncPaymentSeed){
            	seed.getCachedManager().updateOrderPaymentDetail((PaymentInfo) ServiceLocator.getReference(detail.getId()), detail);
            	seed.updateCache();
            	updateGatewayPayment(seed.getOrderId(),detail);
            }else{
            	updatePaymentItem(seed.getOrderId(), detail);
            }
            updated = true;
        }
        return updated;
    }

    private void validatePacket(PaymentSeed seed, PaypalPacket packet) throws SabaException {
        // Validate site name
        if(packet.getCustomField() != null &&
                !packet.getCustomField().equals(seed.getSabaSite())) {
            throw new SabaException(LearningMessage.kPaymentDetailInvalid);
        }
        // Validate invoice Id
        if(packet.getInvoiceId() != null &&
                !packet.getInvoiceId().equals(seed.getOrderNumber())) {
            throw new SabaException(LearningMessage.kPaymentDetailInvalid);
        }
        // Validate order amount
        BigDecimal orderAmount = seed.getFullOrder().getGrandTotalCharges();
    	// SPC-33683 : Added to handle split payment conditions
		Collection paymentDetails = seed.getFullOrder().getPaymentDetails();
    	float amountAlreadyPaid = 0.0f;
    	if(paymentDetails != null && !paymentDetails.isEmpty()){
    		Iterator paymentIterator = paymentDetails.iterator();
    		while(paymentIterator.hasNext()){
    			PaymentInfoDetail detail = (PaymentInfoDetail) paymentIterator.next();
    			if(PaymentStatus.kCompleted.equals(detail.getPaymentStatus()) || PaymentStatus.kRefunded.equals(detail.getPaymentStatus()) || 
    					(seed instanceof AsyncPaymentSeed && (PaymentStatus.kPending.equals(detail.getPaymentStatus())) && (PaymentType.kPurchaseOrder.equals(detail.getPaymentType())))){
    				amountAlreadyPaid = amountAlreadyPaid + detail.getMoneyAmt();
    			}
    		}
    	}
    	BigDecimal amountPaid = new BigDecimal(amountAlreadyPaid);
    	amountPaid = amountPaid.setScale(2, BigDecimal.ROUND_HALF_DOWN);
    	orderAmount = orderAmount.subtract(amountPaid);
        if(orderAmount != null && packet.getAmount() != null && orderAmount.compareTo(new BigDecimal(packet.getAmount())) != 0) {
            throw new SabaException(LearningMessage.kPaymentDetailInvalid);
        }
    }

    private void updatePaymentInfo(PaymentSeed seed, PaymentStatus status, PaypalPacket packet) throws SabaException {
        if(isDuplicatePayment(seed.getFullOrder(), packet.getTransactionId(), PaymentStatus.kCompleted)) {
            return;
        }
        // Update existing payment record for this transaction
        boolean updateSucceeded = updatePaymentDetail(seed, status, packet);
        // It could be an invalid payment if update did not succeed
        if(!updateSucceeded) {
            throw new SabaException(LearningMessage.kPaymentDetailInvalid);
        }
        if(PaymentStatus.kCompleted.equals(status)) {
            //Sets the DRAFT items to OPEN and notifies the SKU Contacts.
            finalizeSKUOrder(seed.getOrderId());
        }
    }

    private boolean isDuplicatePayment(LearningOrderFullDetail fullOrder, String gatewayTxId, PaymentStatus status) {
        Collection payments = fullOrder.getPaymentDetails();
        for(Object p: payments) {
            if(p instanceof PaypalInfoDetail) {
                PaypalInfoDetail detail = (PaypalInfoDetail)p;
                if(detail.getGatewayTxId() != null
                        && gatewayTxId != null
                        && gatewayTxId.equals(detail.getGatewayTxId())
                        && detail.getPaymentStatus() != null
                        && status != null
                        && status.equals(detail.getPaymentStatus())
                        ) {
                    return true;
                }
            }
        }
        return false;
    }

    protected PaymentType getPendingPaymentTypeForOrder(LearningOrderFullDetail fullOrder, PaymentSeed seed) {
        Collection payments = fullOrder.getPaymentDetails();
        for(Object p: payments) {
            PaymentInfoDetail detail = (PaymentInfoDetail)p;
            if(detail.getPaymentType() != null &&
                    detail.getPaymentStatus() != null &
                    PaymentStatus.kPending.equals(detail.getPaymentStatus())) {
					//SPC-33683 : Condition for split payment in async flow
            	if(!(PaymentType.kPurchaseOrder.equals(detail.getPaymentType()) && seed instanceof AsyncPaymentSeed)){
            		return detail.getPaymentType();
            	}
            }
        }
        return null;
    }

    protected PaymentInfoDetail getPaymentByTypeAndStatus(LearningOrderFullDetail fullOrder, PaymentType paymentType, PaymentStatus paymentStatus) {
        Collection payments = fullOrder.getPaymentDetails();
        for(Object p: payments) {
            PaymentInfoDetail detail = (PaymentInfoDetail)p;
            if(detail.getPaymentType() != null &&
               detail.getPaymentStatus() != null &&
                    paymentType.equals(detail.getPaymentType()) &&
                    paymentStatus.equals(detail.getPaymentStatus())) {
                return detail;
            }
        }
        return null;
    }

    protected void updatePaymentItem(String orderId, PaymentInfoDetail paymentInfoDetail) throws SabaException {
        LearningOrderServices mgr = (LearningOrderServices) ServiceLocator.getClientInstance().getManager(Delegates.kLearningOrderServices);
        Order order = (Order) ServiceLocator.getReference(orderId);
        PaymentInfo paymentInfo = (PaymentInfo) ServiceLocator.getReference(paymentInfoDetail.getId());
        mgr.updateOrderPaymentDetail(order, paymentInfo, paymentInfoDetail);
    }


    /**
     * Does the Job of finalizing the SKU Order and sends notifications to sku contacts in order to redeem
     * @param orderId
     * @throws SabaException
     */
    protected void finalizeSKUOrder(String orderId) throws SabaException {
        SKUOrderManager mgr = (SKUOrderManager)ServiceLocator.getClientInstance().getManager(Delegates.kSKUOrderManager);
        mgr.finalizeSKUOrder(orderId);
    }
    
    /** Method to update gateway payment once authorization confirmation is received
     *  This will be used by system to identify that auth is completed for these transaction
     * @param orderId
     * @param paymentInfoDetail
     * @throws SabaException
     */
    public void updateGatewayPayment(String orderId, PaymentInfoDetail paymentInfoDetail) throws SabaException {
    	Object[] binds = new Object[7];
    	int[] bindTypes = new int[7];

    	binds[0] = orderId;
    	binds[1] = ServiceLocator.getClientInstance().getSabaPrincipal().getUsername();
    	binds[2] = paymentInfoDetail.getPaymentStatus().getKey();
		if(paymentInfoDetail instanceof PaypalInfoDetail){
			binds[3] = ((PaypalInfoDetail) paymentInfoDetail).getGatewayTxId();
		}else if(paymentInfoDetail instanceof CyberSourceInfoDetail){
			binds[3] = ((CyberSourceInfoDetail) paymentInfoDetail).getGatewayTxId();
		}
    	binds[4] = paymentInfoDetail.getMoneyAmt();
    	binds[5] = paymentInfoDetail.getPaymentTransactionStatus().getKey();
    	binds[6] = "AUTH";

    	bindTypes[0] = IDbTypes.kObjectIdType;
    	bindTypes[1] = IDbTypes.kStringType;
    	bindTypes[2] = IDbTypes.kStringType;
    	bindTypes[3] = IDbTypes.kStringType;
    	bindTypes[4] = IDbTypes.kRealType;
    	bindTypes[5] = IDbTypes.kStringType;
    	bindTypes[6] = IDbTypes.kStringType;

    	DataBaseUtil.executeStoredProcedure(ServiceLocator.getClientInstance(),	LearningFinder.kUpdateGatewayPayment, binds, bindTypes);
    }
    
    protected float getPaymentAmount(PaymentSeed seed) throws SabaException {
    	BigDecimal orderAmount = seed.getFullOrder().getGrandTotalCharges();
    	// SPC-33683 : With Split payment purchase order can be already applied
    	// Send only remaining amount to payment gateway 
    	Collection paymentDetails = seed.getFullOrder().getPaymentDetails();
    	float amountAlreadyPaid = 0.0f;
    	if(paymentDetails != null && !paymentDetails.isEmpty()){
    		Iterator paymentIterator = paymentDetails.iterator();
    		while(paymentIterator.hasNext()){
    			PaymentInfoDetail detail = (PaymentInfoDetail) paymentIterator.next();
    			if(PaymentType.kPurchaseOrder.equals(detail.getPaymentType()) || PaymentType.kSubscriptionPayment.equals(detail.getPaymentType())){
    				amountAlreadyPaid = amountAlreadyPaid + detail.getMoneyAmt();
    			}
    		}
    	}
        BigDecimal amountPaid = new BigDecimal(amountAlreadyPaid);
        amountPaid = amountPaid.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        orderAmount = orderAmount.subtract(amountPaid);
    	return orderAmount.floatValue();
    }
    
}
