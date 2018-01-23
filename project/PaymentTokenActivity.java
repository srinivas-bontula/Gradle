package com.saba.learning.services.order.activity;

import com.saba.currency.CurrencyDetail;
import com.saba.currency.CurrencyManager;
import com.saba.currency.SabaCurrency;
import com.saba.exception.LearningMessage;
import com.saba.exception.SabaException;
import com.saba.finder.LearningFinder;
import com.saba.i18n.I18NManager;
import com.saba.learning.order.LearningOrderServices;
import com.saba.learning.services.common.ServiceObjectReference;
import com.saba.learning.services.order.PaymentRequest;
import com.saba.learning.services.order.PaymentToken;
import com.saba.learning.services.order.business.OrderServiceHelper;
import com.saba.locator.Delegates;
import com.saba.locator.ServiceLocator;
import com.saba.order.Order;
import com.saba.party.Party;
import com.saba.party.PartyManager;
import com.saba.party.Person;
import com.saba.party.PersonDetail;
import com.saba.payment.CyberSourceConstants;
import com.saba.payment.cybersource.CyberSourceHelper;
import com.saba.payment.paypal.ExpressCheckoutPacket;
import com.saba.payment.paypal.ExpressCheckoutProcessor;
import com.saba.payment.paypal.PayflowLinkProcessor;
import com.saba.payment.paypal.PayflowPacket;
import com.saba.payment.paypal.PaypalConfig;
import com.saba.payment.paypal.PaypalHelper;
import com.saba.payment.paypal.PaypalHelper.PaypalType;
import com.saba.payment.paypal.PaypalPacket;
import com.saba.payment.paypal.PaypalProcessor;
import com.saba.payment.stripe.StripeHelper;
import com.saba.paymentinfo.CyberSourceInfoDetail;
import com.saba.paymentinfo.PaymentInfoDetail;
import com.saba.paymentinfo.PaymentStatus;
import com.saba.paymentinfo.PaymentType;
import com.saba.paymentinfo.PaypalInfoDetail;
import com.saba.paymentinfo.StripeInfoDetail;
import com.saba.primitives.AddressDetail;
import com.saba.primitives.ContactInfoDetail;
import com.saba.primitives.NameDetail;
import com.saba.saba3.db.IDbTypes;
import com.saba.servlet.RequestContext;
import com.saba.util.DataBaseUtil;
import com.saba.util.Debug;
import com.saba.util.StringUtil;
import com.saba.util.UserMicrositeProvider;
import com.saba.workflow.BaseActivity;
import com.saba.workflow.ProcessContext;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;


/**
 * Created with IntelliJ IDEA.
 * User: nikhil
 * Date: 21/1/13
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentTokenActivity extends BaseActivity {
    private static BigDecimal ZERO = new BigDecimal("0.0");
    private String micrositeId = null;
    @Resource(name = "sabaOrderReturnUrlCache")
    protected Map urlCache;

    @Override
    public ProcessContext execute(ProcessContext context) throws Exception {
        if(!(context.getSeedData() instanceof CheckoutSeed)) {
            return context;
        }

        CheckoutSeed seed = (CheckoutSeed) context.getSeedData();
        if(seed.getPaymentRequest() == null) {
            return context;
        }
        micrositeId = UserMicrositeProvider.getApplicableMicrositeId();
        PaymentRequest request = seed.getPaymentRequest();
        // Never give out null token
        PaymentToken token = new PaymentToken();
        if( (PaymentType.kPaypalExpressCheckout.equals(request.getPaymentMethod())
                || PaymentType.kPayflowLink.equals(request.getPaymentMethod())
                || PaymentType.kCyberSource.equals(request.getPaymentMethod())
                || PaymentType.kStripe.equals(request.getPaymentMethod()))
            && seed.getFullOrder().getGrandTotalCharges().compareTo(ZERO) == 0) {

            //If Paypal or Payflow Payment and order Price is Zero, then throw Error
            throw new SabaException(LearningMessage.kPaymentDetailInvalid);
        }
        if(!StringUtil.isEmpty(seed.getMicrosite())){
            micrositeId = seed.getMicrosite();
        }
        if(seed.getFullOrder().getGrandTotalCharges().compareTo(ZERO) == 0) {
            PaymentHandshakeUrlActivity.setReturnUrl(urlCache, request);
            URI returnUrl = PaymentHandshakeUrlActivity.getReturnUrl(urlCache, request.getOrderId(), RequestContext.getRequest(), PaymentStatus.kCompleted, "", "");
            if(returnUrl != null) {
                token.setSubmitUrl(returnUrl.toURL().toString());
            }

        }else if(PaymentType.kPaypalExpressCheckout.equals(request.getPaymentMethod())) {
            token = generatePaypalToken(context);
        }else if(PaymentType.kPayflowLink.equals(request.getPaymentMethod())) {
            token = generatePayflowToken(context);
        }
        else if(PaymentType.kCyberSource.equals(request.getPaymentMethod())) {
            token = generateCyberSourceToken(context);
        }else if(PaymentType.kStripe.equals(request.getPaymentMethod())){
        	 token = generateStripeToken(context);
        }
        context.setAttribute(PaymentToken.class, token);
        return context;
    }
    
    protected PaymentToken generateStripeToken(ProcessContext context) throws SabaException {
    	PaymentToken token = new PaymentToken();  
    	CheckoutSeed seed = (CheckoutSeed) context.getSeedData();
        PaymentRequest paymentRequest = seed.getPaymentRequest();
        PaymentHandshakeUrlActivity.setReturnUrl(urlCache, paymentRequest);
        NameDetail nameDetail = new NameDetail();
        nameDetail.setFname(paymentRequest.getFirstName());
        nameDetail.setLname(paymentRequest.getLastName());
        
        AddressDetail addressDetail = new AddressDetail();
        addressDetail.setAddr1(paymentRequest.getAddress1());
        addressDetail.setAddr2(paymentRequest.getAddress2());
        addressDetail.setAddr3(paymentRequest.getAddress3());
        addressDetail.setCity(paymentRequest.getCity());
        addressDetail.setState(paymentRequest.getState());
        addressDetail.setZip(paymentRequest.getZip());
        addressDetail.setCountry(paymentRequest.getCountry());
        
        ContactInfoDetail contactInfo = new ContactInfoDetail();
        contactInfo.setEmail(paymentRequest.getEmail());
        token.addHiddenField("reciept_email", paymentRequest.getEmail());
        Party billedParty = seed.getFullOrder().getBillto();
        String sTxId =  seed.getFullOrder().getOrderNo();
        
        float amount =getPaymentAmount(seed);
        
        PaymentInfoDetail detail = new StripeInfoDetail(
   			    null,
   			    amount,
                nameDetail,
                addressDetail,
                contactInfo,
                billedParty,
                null,
                null,
                null,
                PaymentType.kStripe,
                PaymentStatus.kPending,
                sTxId,
                null,
                null,
                seed.getFullOrder().getCurrency()
        );
        // SPC-33400 in case of async flow update payment in cache only
        if(seed instanceof AsyncCheckoutSeed){
            seed.getCachedManager().addOrderPaymentDetail(detail);
            seed.updateCache();
            addGatewayPayment(seed.getOrderId(), seed.getOrderNumber(),detail);
        }else{
            addPaymentItem(seed.getOrderId(), detail);
        }
        token.setTokenStatus(true);
        token.setPaymentType("Stripe");
        token.addHiddenField("returnUrl", (String)context.getAttribute("returnUrl"));
        if(StringUtil.isEmpty(micrositeId)){
            token.addHiddenField("stripePublicKey", StripeHelper.getConfig().getStripePublicKey());
        }else{
            token.addHiddenField("stripePublicKey", StripeHelper.getConfig(micrositeId).getStripePublicKey());
        }
    	return token; 
    }
    protected PaymentToken generateCyberSourceToken(ProcessContext context) throws SabaException {
    	/*
    	 * Save the address information associated to payment
    	 * Generate signature
    	 * Create saba payment detail with payment initiated state 
    	 * Return payment token with required information
    	 * */
    	PaymentToken token = new PaymentToken();   
    	CheckoutSeed seed = (CheckoutSeed) context.getSeedData();
        PaymentRequest paymentRequest = seed.getPaymentRequest();
        PaymentHandshakeUrlActivity.setReturnUrl(urlCache, paymentRequest);
        String csPaymentAction = CyberSourceHelper.getMicrositeConfig(micrositeId).getPaymentAction();
        // SPC-33400 In case of Async Order payment action can be AUTH only
        if(seed instanceof AsyncCheckoutSeed){
        	csPaymentAction = CyberSourceConstants.kCyberSourceAuthOnly;
        }
        NameDetail nameDetail = new NameDetail();
        nameDetail.setFname(paymentRequest.getFirstName());
        nameDetail.setLname(paymentRequest.getLastName());
        
        AddressDetail addressDetail = new AddressDetail();
        addressDetail.setAddr1(paymentRequest.getAddress1());
        addressDetail.setAddr2(paymentRequest.getAddress2());
        addressDetail.setAddr3(paymentRequest.getAddress3());
        addressDetail.setCity(paymentRequest.getCity());
        addressDetail.setState(paymentRequest.getState());
        addressDetail.setZip(paymentRequest.getZip());
        addressDetail.setCountry(paymentRequest.getCountry());
        
        ContactInfoDetail contactInfo = new ContactInfoDetail();
        contactInfo.setEmail(paymentRequest.getEmail());
        
        Party billedParty = seed.getFullOrder().getBillto();
        SabaCurrency currency = seed.getFullOrder().getCurrency();
        CurrencyManager curMgr = (CurrencyManager)  ServiceLocator.getClientInstance().getManager(Delegates.kCurrencyManager); 
        String currencyShortName = curMgr.getDetail(currency).getISOCode();
		float amount =getPaymentAmount(seed);
		String amountString=String.format(CyberSourceConstants.kStringFormat, amount);		
		String sTxId =  seed.getFullOrder().getOrderNo();
		String locale =getJavaLocaleForCybersource();
		
    	String returnUrl = (String)context.getAttribute("returnUrl");
		String accessKey =CyberSourceHelper.getMicrositeConfig(micrositeId).getAccessKey();
		String secretKey =CyberSourceHelper.getMicrositeConfig(micrositeId).getSecretKey();
		String profileId =CyberSourceHelper.getMicrositeConfig(micrositeId).getProfileId();
		String signedFields =CyberSourceConstants.kSignedFields;
		String unSignedFields=CyberSourceConstants.kUnsignedFields;
		/*String submitURL = CyberSourceHelper.getMicrositeConfig(getMicrositeId()).getSandbox()?CyberSourceConstants.kCyberSourceTestSecureAcceptanceURL:CyberSourceConstants.kCyberSourceProdSecureAcceptanceURL;*/
		String submitURL = CyberSourceHelper.getMicrositeConfig(micrositeId).getSandbox()?CyberSourceHelper.getMicrositeConfig(micrositeId).getTest_secureacceptance_url():CyberSourceHelper.getMicrositeConfig(micrositeId).getProd_secureacceptance_url();
		SimpleDateFormat dt = new SimpleDateFormat(CyberSourceConstants.kDateFormat);
		dt.setTimeZone(TimeZone.getTimeZone(CyberSourceConstants.kTimeZone));
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(CyberSourceConstants.kTimeZone));
		String signedDateTime =dt.format(c.getTime());
		
		StringBuffer messageString = new StringBuffer();
		messageString.append(CyberSourceConstants.kRequest_access_key+CyberSourceConstants.kEqualTo+accessKey);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_transaction_type+CyberSourceConstants.kEqualTo+csPaymentAction);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_reference_number+CyberSourceConstants.kEqualTo+sTxId);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_profile_id+CyberSourceConstants.kEqualTo+profileId);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_transaction_uuid+CyberSourceConstants.kEqualTo+seed.getOrderId());
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_currency+CyberSourceConstants.kEqualTo+currencyShortName);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_override_custom_receipt_page+CyberSourceConstants.kEqualTo+returnUrl);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_amount+CyberSourceConstants.kEqualTo+amountString);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_signed_date_time+CyberSourceConstants.kEqualTo+signedDateTime+"Z");
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_signed_field_names+CyberSourceConstants.kEqualTo+signedFields);
		messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_unsigned_field_names+CyberSourceConstants.kEqualTo+unSignedFields);
		//messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_ignore_avs+CyberSourceConstants.kEqualTo+CyberSourceConstants.kTrue);		
		//messageString.append(CyberSourceConstants.kComma+CyberSourceConstants.kRequest_ignore_cvn+CyberSourceConstants.kEqualTo+CyberSourceConstants.kTrue);
		String message =messageString.toString();
		String signature =CyberSourceHelper.getHmacSHA256(message,secretKey);		
		
		PaymentInfoDetail detail = new CyberSourceInfoDetail(
   			    null,
   			    amount,
                nameDetail,
                addressDetail,
                contactInfo,
                billedParty,
                null,
                null,
                null,
                PaymentType.kCyberSource,
                PaymentStatus.kPending,
                sTxId,
                null,
                null,
                seed.getFullOrder().getCurrency()
        );
        // SPC-33400 in case of async flow update payment in cache only
        if(seed instanceof AsyncCheckoutSeed){
            seed.getCachedManager().addOrderPaymentDetail(detail);
            seed.updateCache();
            addGatewayPayment(seed.getOrderId(), seed.getOrderNumber(),detail);
        }else{
            addPaymentItem(seed.getOrderId(), detail);
        }
        token.setTokenStatus(true);
        token.setPaymentType(CyberSourceConstants.kCyberSource);
        token.addHiddenField(CyberSourceConstants.kRequest_transaction_type, csPaymentAction);
        token.addHiddenField(CyberSourceConstants.kRequest_reference_number, sTxId);
        token.addHiddenField(CyberSourceConstants.kRequest_amount, amountString);
        token.addHiddenField(CyberSourceConstants.kRequest_payment_method, CyberSourceConstants.kPayment_Method);
        token.addHiddenField(CyberSourceConstants.kRequest_access_key, accessKey);
        token.addHiddenField(CyberSourceConstants.kRequest_signed_date_time, signedDateTime+"Z");
        token.addHiddenField(CyberSourceConstants.kRequest_profile_id,profileId);
        token.addHiddenField(CyberSourceConstants.kRequest_locale,locale.toLowerCase());
        token.addHiddenField(CyberSourceConstants.kRequest_currency,currencyShortName);
        token.addHiddenField(CyberSourceConstants.kRequest_transaction_uuid, seed.getOrderId());
        token.addHiddenField(CyberSourceConstants.kRequest_override_custom_receipt_page, returnUrl);
        token.addHiddenField(CyberSourceConstants.kRequest_signature, signature);
        token.addHiddenField(CyberSourceConstants.kRequest_signed_field_names, signedFields);
        token.addHiddenField(CyberSourceConstants.kRequest_unsigned_field_names, unSignedFields);
        // token.addHiddenField(CyberSourceConstants.kRequest_ignore_avs, CyberSourceConstants.kTrue);
        //token.addHiddenField(CyberSourceConstants.kRequest_ignore_cvn, CyberSourceConstants.kTrue);
        token.setSubmitUrl(submitURL);
        
    	return token; 
    }
    protected PaymentToken generatePaypalToken(ProcessContext context) throws SabaException {
        CheckoutSeed seed = (CheckoutSeed) context.getSeedData();
        PaymentRequest paymentRequest = seed.getPaymentRequest();

        PaymentHandshakeUrlActivity.setReturnUrl(urlCache, paymentRequest);

        ExpressCheckoutProcessor processor = PaypalHelper.getExpressCheckout(micrositeId);
        ExpressCheckoutPacket request = new ExpressCheckoutPacket();
        populatePacket(context, request);
        try {
            // SPC-33400 In case of async payment request only authorization
            ExpressCheckoutPacket resp = null;
            if(seed instanceof AsyncCheckoutSeed){
            	resp = processor.requestAuthPayment(request);
            }else{
            	resp = processor.requestPayment(request);
            }

            PaymentToken token = new PaymentToken();
            if(resp.isApproved()) {
                String forwardURL = processor.getForwardURL(resp.getToken());
                token.setTokenStatus(true);
                token.setSubmitUrl(forwardURL);
            }
            token.setErrors(GatewayErrorMessages.getLocalizedMessage(paymentRequest.getPaymentMethod(), resp.getResult()));
            if(token.getErrors() == null) {
                token.setErrors(resp.getResultDescription());
            }
            if(token.isTokenStatus()) {
                attachPendingPayment(resp.getToken(), seed);
            }else {
                try {
                	if(!(seed instanceof AsyncCheckoutSeed) && paymentRequest.isCartMode()){
                		new OrderServiceHelper(ServiceLocator.getClientInstance()).cancelOrder(new ServiceObjectReference(seed.getOrderId(), ""));
                	}
                }catch (Exception ex) {
                }
            }
            return token;
        } catch (PaypalProcessor.PaypalException e) {
        	// Additional debug required here to get exact error
        	Debug.printStackTrace(e);
            throw new SabaException(LearningMessage.kPaymentGatewayNotReachable);
        }
    }

    protected PaymentToken generatePayflowToken(ProcessContext context) throws SabaException {
        CheckoutSeed seed = (CheckoutSeed) context.getSeedData();
        PaymentRequest paymentRequest = seed.getPaymentRequest();
        PaymentHandshakeUrlActivity.setReturnUrl(urlCache, paymentRequest);

        PayflowLinkProcessor processor = PaypalHelper.getPayflowLinkProcessor(micrositeId);
        PayflowPacket packet = new PayflowPacket();
        String site = ServiceLocator.getClientInstance().getSabaPrincipal().getSiteName();
       
        populatePacket(context, packet);
        try {
        	// SPC-33400 In case of async payment request only authorization
        	PayflowPacket response = null;
        	if(seed instanceof AsyncCheckoutSeed){
        		response = processor.requestAuthPayment(packet);
        	}else{
        		response = processor.requestPayment(packet);
        	}
        	PaymentToken token = new PaymentToken();
        	if(response.isApproved()) {
                token.setTokenStatus(true);
                token.addHiddenField("MODE", processor.getMode());
                token.addHiddenField("SECURETOKEN", response.getSecureToken());
                token.addHiddenField("SECURETOKENID", response.getSecureTokenId());
                token.setSubmitMethod(PaymentToken.HTTP_METHOD_POST);
                PaypalConfig config = PaypalHelper.getConfig(site, PaypalType.PayflowLink, micrositeId);
                if(config.isSandbox()) {
                	//token.setSubmitUrl(response.getSandBoxSubmitUrl());
                	token.setSubmitUrl(config.getSandBoxSubmitUrl());
                }else{
                	//token.setSubmitUrl(response.getLiveSubmitUrl());
                	token.setSubmitUrl(config.getLiveSubmitUrl());
                }
                
            }
            token.setErrors(GatewayErrorMessages.getLocalizedMessage(paymentRequest.getPaymentMethod(), response.getResult()));
            if(token.getErrors() == null) {
                token.setErrors(response.getResultDescription());
            }
            if(token.isTokenStatus()) {
                attachPendingPayment(response.getSecureToken(), seed);
            }else {
                try {
                	if(!(seed instanceof AsyncCheckoutSeed) && paymentRequest.isCartMode()){
                		new OrderServiceHelper(ServiceLocator.getClientInstance()).cancelOrder(new ServiceObjectReference(seed.getOrderId(), ""));
                	}
                }catch (Exception ex) {
                }
            }
            return token;
        } catch (PaypalProcessor.PaypalException e) {
        	// Additional debug required here to get exact error
        	Debug.printStackTrace(e);
            throw new SabaException(LearningMessage.kPaymentGatewayNotReachable);
        }
    }

    protected String getCurrencyCode(CheckoutSeed seed) throws SabaException {
        seed.getOrderDetail();
        SabaCurrency currencyRef = seed.getFullOrder().getCurrency();
        CurrencyManager manager = (CurrencyManager) ServiceLocator.getClientInstance().getManager(Delegates.kCurrencyManager);
        CurrencyDetail currencyDetail = manager.getDetail(currencyRef);
        return currencyDetail.getISOCode();
    }

    protected float getPaymentAmount(CheckoutSeed seed) throws SabaException {
    	BigDecimal orderAmount = seed.getFullOrder().getGrandTotalCharges();
    	// SPC-33683 : With Split payment purchase order can be already applied
    	// Send only remaining amount to payment gateway 
    	Collection paymentDetails = seed.getFullOrder().getPaymentDetails();
    	float amountAlreadyPaid = 0.0f;
    	if(paymentDetails != null && !paymentDetails.isEmpty()){
    		Iterator paymentIterator = paymentDetails.iterator();
    		while(paymentIterator.hasNext()){
    			PaymentInfoDetail detail = (PaymentInfoDetail) paymentIterator.next();
    			// SPC-39804 : Adding support for add-on payments,
    			// Lets add condition to consider confirmed payments as money already paid 
				if (PaymentType.kPurchaseOrder.equals(detail.getPaymentType())
						|| PaymentType.kSubscriptionPayment.equals(detail.getPaymentType())
						|| ((PaymentStatus.kCompleted.equals(detail.getPaymentStatus()) || PaymentStatus.kRefunded.equals(detail.getPaymentStatus())) && !seed
								.getPaymentRequest().isCartMode())){
    				amountAlreadyPaid = amountAlreadyPaid + detail.getMoneyAmt();
    			}
    		}
    	}
        BigDecimal amountPaid = new BigDecimal(amountAlreadyPaid);
        amountPaid = amountPaid.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        orderAmount = orderAmount.subtract(amountPaid);
    	return orderAmount.floatValue();
    }

    protected void populatePacket(ProcessContext context, PaypalPacket packet) throws SabaException {
        CheckoutSeed seed = (CheckoutSeed) context.getSeedData();
        PaymentRequest request = seed.getPaymentRequest();
        packet.setAmount(getPaymentAmount(seed));
        packet.setCurrencyCode(getCurrencyCode(seed));
        packet.setInvoiceId(seed.getOrderNumber());
        packet.setCustomField(seed.getSabaSite());

        packet.setCancelUrl((String) context.getAttribute("cancelUrl"));
        packet.setErrorUrl((String) context.getAttribute("errorUrl"));
        packet.setReturnUrl((String) context.getAttribute("returnUrl"));
        packet.setSilentPostUrl((String) context.getAttribute("silentCopyUrl"));

        packet.setAddress1(request.getAddress1());
        packet.setCity(request.getCity());
        packet.setState(request.getState());
        packet.setZip(request.getZip());
        packet.setCountryCode(request.getCountry());

        String javaLocale = ServiceLocator.getClientInstance().getSabaPrincipal().getJavaLocale();
        if(javaLocale != null) {
            int idx = javaLocale.indexOf('_');
            if(idx != -1 && (idx + 1) < javaLocale.length()) {
                javaLocale = javaLocale.substring(idx + 1);
            }
            packet.setLocaleCode(javaLocale);
        }

    }


    private void attachPendingPayment(String tokenId, CheckoutSeed seed) throws SabaException {
        PaymentRequest request = seed.getPaymentRequest();
        getCurrencyCode(seed);

        NameDetail nameDetail = new NameDetail();
        nameDetail.setFname(request.getFirstName());
        nameDetail.setLname(request.getLastName());
        AddressDetail addressDetail = new AddressDetail();
        addressDetail.setAddr1(request.getAddress1());
        addressDetail.setAddr2(request.getAddress2());
        addressDetail.setAddr3(request.getAddress3());
        addressDetail.setCity(request.getCity());
        addressDetail.setState(request.getZip());
        addressDetail.setZip(request.getZip());
        addressDetail.setCountry(request.getCountry());
        ContactInfoDetail contactInfo = new ContactInfoDetail();
        contactInfo.setEmail(request.getEmail());
        Party billedParty = seed.getFullOrder().getBillto();
        PaymentType paymentType = request.getPaymentMethod();
        PaymentStatus paymentStatus = PaymentStatus.kPending;
        PaymentInfoDetail detail =null;
        if(paymentType.equals(PaymentType.kPayflowLink)
                || paymentType.equals(PaymentType.kPaypalExpressCheckout)
                || paymentType.equals(PaymentType.kPaypalProDirect)){
        	 detail = new PaypalInfoDetail(null,
                     getPaymentAmount(seed),
                     nameDetail,
                     addressDetail,
                     contactInfo,
                     billedParty,
                     null,
                     null,
                     null,
                     paymentType,
                     paymentStatus,
                     seed.getOrderId(),
                     tokenId,
                     seed.getFullOrder().getCurrency()
             );
        }
        // SPC-33400 in case of async flow update payment in cache only
        if(seed instanceof AsyncCheckoutSeed){
        	seed.getCachedManager().addOrderPaymentDetail(detail);
        	seed.updateCache();
        	addGatewayPayment(seed.getOrderId(),seed.getOrderNumber(),detail);
        }else{
        	addPaymentItem(seed.getOrderId(), detail);
        }
    }
    
       private void addPaymentItem(String orderId, PaymentInfoDetail paymentInfoDetail) throws SabaException {
       	paymentInfoDetail.setMicrossiteId(micrositeId);
        LearningOrderServices mgr = (LearningOrderServices) ServiceLocator.getClientInstance().getManager(Delegates.kLearningOrderServices);
        Order order = (Order) ServiceLocator.getReference(orderId);
        mgr.addOrderPaymentDetail(order, paymentInfoDetail);
    }
       
  
   /** New method to keep track of all authorization done at payment gateway for 
    *  Async order payments, This will add payment entry in table let_gateway_payment_info
    * @param orderId
    * @param paymentInfoDetail
    * @throws SabaException
    */
   public void addGatewayPayment(String orderId, String orderNo, PaymentInfoDetail paymentInfoDetail) throws SabaException {
	   Object[] binds = new Object[11];
	   int[] bindTypes = new int[11];

	   binds[0] = ServiceLocator.getClientInstance().getSabaPrincipal().getUsername();
	   binds[1] = orderId;
	   binds[2] = paymentInfoDetail.getPaymentType().getPaymentType();
	   binds[3] = paymentInfoDetail.getPaymentStatus().getKey();
	   if(paymentInfoDetail instanceof PaypalInfoDetail){
		   binds[4] = ((PaypalInfoDetail) paymentInfoDetail).getGatewayTxId();
	   }else if(paymentInfoDetail instanceof CyberSourceInfoDetail){
		   binds[4] = ((CyberSourceInfoDetail) paymentInfoDetail).getGatewayTxId();
	   }else if(paymentInfoDetail instanceof StripeInfoDetail){
		   binds[4] = ((StripeInfoDetail) paymentInfoDetail).getGatewayTxId();
	   }
	   binds[5] = paymentInfoDetail.getMoneyAmt();
	   binds[6] = paymentInfoDetail.getPaymentTransactionStatus().getKey();
	   binds[7] = paymentInfoDetail.getCurrency().getId();
	   binds[8] = "INIT";
	   binds[9] = micrositeId;
	   binds[10] = orderNo;

	   bindTypes[0] = IDbTypes.kStringType;
	   bindTypes[1] = IDbTypes.kObjectIdType;
	   bindTypes[2] = IDbTypes.kNumberType;
	   bindTypes[3] = IDbTypes.kStringType;
	   bindTypes[4] = IDbTypes.kStringType;
	   bindTypes[5] = IDbTypes.kRealType;
	   bindTypes[6] = IDbTypes.kStringType;
	   bindTypes[7] = IDbTypes.kObjectIdType;
	   bindTypes[8] = IDbTypes.kStringType;
	   bindTypes[9] = IDbTypes.kObjectIdType;
	   bindTypes[10] = IDbTypes.kStringType;

	   DataBaseUtil.executeStoredProcedure(ServiceLocator.getClientInstance(),
			   LearningFinder.kAddGatewayPayment, binds, bindTypes);
   }

   private String getJavaLocaleForCybersource() throws SabaException{
	   
	   String locale =ServiceLocator.getClientInstance().getSabaPrincipal().getJavaLocale();
		// Added this check for SPC-61247:JWT SSO not passing locale to CyberSource when making payment (Adobe)
		if(locale == null || "".equals(locale.trim())){
		   PartyManager partyManager = (PartyManager) ServiceLocator.getClientInstance().getManager(Delegates.kPartyManager);
           PersonDetail detail = partyManager.getDetail((Person)ServiceLocator.getReference(ServiceLocator.getClientInstance().getSabaPrincipal().getID()));
           I18NManager i18nMgr = (I18NManager) ServiceLocator.getClientInstance().getManager(Delegates.kI18NManager);
           locale = i18nMgr.getLocaleDetail(detail.getLocale()).getJavaLocale();
		}
		// added to verify and replace the locale if it is not supported by CyberSource.
		locale = CyberSourceHelper.getCyberSourceSupportedLocale(locale);
		//Cybersource expects locale name in en-us format 
		if(locale!=null && locale.contains("_")){
			locale = locale.replace('_', '-');
		}
		
		
		return locale;
   }
}
