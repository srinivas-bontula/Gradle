package com.saba.payment.cybersource;


import java.util.ArrayList;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.saba.exception.SabaException;
import com.saba.properties.PropertyKey;
import com.saba.properties.SabaProperties;
import com.saba.properties.SiteConfig;
import com.saba.tenant.MicroSiteURLUtil;
import com.saba.tenant.TenantContext;
import com.saba.util.StringUtil;
import com.saba.util.UserMicrositeProvider;

/**
 * Created with IntelliJ IDEA.
 * User: rakesh
 * Date: 21/08/14
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CyberSourceHelper {
	
	private final static String  kHmacSHA256="HmacSHA256";
	private final static String  kArabicLocale = "ar_SA";
	private final static String  kCyberSourceSupportedArabicLocale = "ar_XN";
	private final static String  kBulgarianLocale = "bg_BG";
	private final static String  kPortugeseContinentalLocale = "pt_PT";
	private final static String  kSuomiLocale = "fi_FI";
	private final static String  kUkranianLocale = "uk_UA";
	private final static String  kGreekLocale = "el_GR";
	private final static String  kHebrewLocale = "he_IL";
	private final static String  kCyberSourceSupportedEnglishLocale = "en_US";

	public static CyberSourceConfig getConfig() throws SabaException {
    	String micrositeId = UserMicrositeProvider.getApplicableMicrositeId();
    	return getCyberSourceConfig(micrositeId);
    }
    
    public static CyberSourceConfig getMicrositeConfig(String micrositeId) throws SabaException {
    	if(micrositeId == null) {
			micrositeId = UserMicrositeProvider.getApplicableMicrositeId();
		}
    	return getCyberSourceConfig(micrositeId);
    }
    
    private static CyberSourceConfig getCyberSourceConfig(String micrositeId)throws SabaException{
    	SabaProperties props =null;
    	props = SabaProperties.getMicroSiteProperties(SiteConfig.kCyberSource, TenantContext.getTenantContext().getSiteName(), micrositeId);
        CyberSourceConfig config = new CyberSourceConfig();
            boolean sandbox = Boolean.parseBoolean(props.getValue(PropertyKey.kCyberSource_sandbox));
            config.setSandbox(sandbox);
            config.setMerchantId(props.getValue(PropertyKey.kCyberSource_merchant_id));
            config.setMerchantKey(props.getValue(PropertyKey.kCyberSource_merchant_key));
            config.setPaymentAction(props.getValue(PropertyKey.kCyberSource_payment_action));
            config.setAccessKey(props.getValue(PropertyKey.kCyberSource_accessKey));
            config.setSecretKey(props.getValue(PropertyKey.kCyberSource_secretKey));
            config.setProfileId(props.getValue(PropertyKey.kCyberSource_profileId));
            
            config.setTest_secureacceptance_url(props.getValue(PropertyKey.kCybersource_test_secureacceptance_url));
            config.setTest_server_url(props.getValue(PropertyKey.kCybersource_test_server_url));
            config.setTest_host(props.getValue(PropertyKey.kCybersource_test_host));
            config.setProd_secureacceptance_url(props.getValue(PropertyKey.kCybersource_prod_secureacceptance_url));
            config.setProd_server_url(props.getValue(PropertyKey.kCybersource_prod_server_url));
            config.setProd_host(props.getValue(PropertyKey.kCybersource_prod_host));
            
        return config;
    }

	public static boolean isCyberSourceEngine() throws SabaException {
		boolean isCyberSourceEngine =false;
		String micrositeId = UserMicrositeProvider.getApplicableMicrositeId();
		SabaProperties props = SabaProperties.getMicroSiteProperties(SiteConfig.kCreditCardEngine, TenantContext.getTenantContext().getSiteName(), micrositeId);
		String CCEngineName = props.getValue(PropertyKey.kCreditCard_Engine_Name);
		if(CCEngineName!=null && CCEngineName.equalsIgnoreCase("cybersource")){
			isCyberSourceEngine=true;
		}
		return isCyberSourceEngine;
	}
    
    public static boolean isCyberSourceEngine(String overriddenMicrositeId) throws SabaException {
    	boolean isCyberSourceEngine =false;
		String micrositeId = null;
		if(StringUtil.isEmpty(overriddenMicrositeId)){
			micrositeId = UserMicrositeProvider.getApplicableMicrositeId();
		}else{
			micrositeId = overriddenMicrositeId;
		}
    	SabaProperties props = SabaProperties.getMicroSiteProperties(SiteConfig.kCreditCardEngine, TenantContext.getTenantContext().getSiteName(), micrositeId);
    	String CCEngineName = props.getValue(PropertyKey.kCreditCard_Engine_Name);
    	if(CCEngineName!=null && CCEngineName.equalsIgnoreCase("cybersource")){
    		isCyberSourceEngine=true;
    	}
    	return isCyberSourceEngine;
    }
    
    public static String getHmacSHA256(String data, String secretKey){
    	String publicDigest = null;
    	
    	try{
    		 if(data!=null && secretKey !=null){
		  	    //  BASE64Encoder encoder = new BASE64Encoder();
		  	      Mac sha256Mac = Mac.getInstance(kHmacSHA256);
		  	      SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), kHmacSHA256);
		  	      sha256Mac.init(secretKeySpec);
		  	      byte[] publicBytes = sha256Mac.doFinal(data.getBytes());
		  	      //publicDigest = encoder.encodeBuffer(publicBytes);
		  	      publicDigest = Base64.getEncoder().encodeToString(publicBytes);
		  	      publicDigest = publicDigest.replaceAll("\n", "");
		  	      publicDigest = publicDigest.replaceAll("\r", "");
    		 }
	    } catch (Exception e) {
  	      e.printStackTrace();
    	}
    	
	      return publicDigest;
    }
    
    public static String getStringValue(String data){
 	   String resData =null;
 	   int dataLength =data.length(); 
 	   if(data!=null && dataLength>2){
 		   resData = data.substring(1,dataLength-1);
 		   if(data!=null && !"".equals(data) && data.equals("null")){
 			   resData =null;
 		   }
 	   }
 	   
 	   return resData;
    }
    
   public static ArrayList<String> processAVSError(String invalidFields) throws SabaException{
    	String kCard_CVN="card_cvn";
    	String kCity="bill_to_address_city";
    	String kCountry="bill_to_address_country";
    	String kAddress1="bill_to_address_line1";
    	String kAddress2="bill_to_address_line2";
    	String kZip="bill_to_address_postal_code";
    	String kState="bill_to_address_state";
    	String kCardTypeError = "card_type";
    	String kCardNumberError = "card_number";
    	String kCardSecurityCode="Card Security Code";
    	String kBillingAddress1="Billing address Address1";
    	String kBillingAddress2="Billing address Address2";
    	String kkBillingCity="Billing address City";
    	String kBillingState="Billing address State";
    	String kBillingCountry="Billing address Country";
    	String kBillingZip="Billing address Zip code";
    	String kCardType = "Card Type";
    	String kCardNumber = "Card Number";
    	
    	ArrayList<String> invalidFieldsArr = new ArrayList<String>();
    	StringTokenizer tokenizer = new StringTokenizer(invalidFields,",");
		String invalidField=null;
		while (tokenizer.hasMoreTokens()) {
			invalidField = tokenizer.nextToken();
			if(invalidField!=null){
				if (invalidField.equals(kCard_CVN)) {
					invalidFieldsArr.add(kCardSecurityCode);
				}
				else if(invalidField.equals(kAddress1)){
					invalidFieldsArr.add(kBillingAddress1);
				}
				else if(invalidField.equals(kAddress2)){
					invalidFieldsArr.add(kBillingAddress2);
				}
				else if(invalidField.equals(kCity)){
					invalidFieldsArr.add(kkBillingCity);
				}
				else if(invalidField.equals(kState)){
					invalidFieldsArr.add(kBillingState);
				}
				else if(invalidField.equals(kCountry)){
					invalidFieldsArr.add(kBillingCountry);
				}
				else if(invalidField.equals(kZip)){
					invalidFieldsArr.add(kBillingZip);
				}
				else if(invalidField.equals(kCardTypeError)){
					invalidFieldsArr.add(kCardType);
				}
				else if(invalidField.equals(kCardNumberError)){
					invalidFieldsArr.add(kCardNumber);
				}
				else{
					invalidFieldsArr.add(invalidField);
				}
				
			}
		}
		return invalidFieldsArr;
    	
    }
   
   public static String getCyberSourceSupportedLocale(String locale){
	   
	   if(locale.equals(kArabicLocale)){
		   return kCyberSourceSupportedArabicLocale;
	   }
	   else if(locale.equals(kBulgarianLocale)){
		   return kCyberSourceSupportedEnglishLocale;
	   }
	   else if(locale.equals(kPortugeseContinentalLocale)){
		   return kCyberSourceSupportedEnglishLocale;
	   }
	   else if(locale.equals(kSuomiLocale)){
		   return kCyberSourceSupportedEnglishLocale;
	   }
	   else if(locale.equals(kUkranianLocale)){
		   return kCyberSourceSupportedEnglishLocale;
	   }
	   else if(locale.equals(kGreekLocale)){
		   return kCyberSourceSupportedEnglishLocale;
	   }
	   else if(locale.equals(kHebrewLocale)){
		   return kCyberSourceSupportedEnglishLocale;
	   }
	   
	return locale;	   
   }


}
