package com.saba.payment;

public interface CyberSourceConstants
{
	// Use these constants to map Attribute names to UI labels
	public static final String kBillState = "bill_state";
	public static final String kBillZip = "bill_zip";
	public static final String kCustomerEmail = "customer_email";
	public static final String kBillCountry = "bill_country";
	public static final String kComma=",";
	public static final String kEqualTo="=";
	public static final String kFalse ="false";
	public static final String kTrue ="true";
	public static final String kStringFormat ="%.2f";
	public static final String kCyberSourceSuccessStatus = "100";
	public static final String kCyberSourceStatusMessage ="ACCEPT";
	public static final String kCyberSource ="CyberSource";
	public static final String kCyberSourceAuthOnly ="authorization";
	public static final String kCyberSourceCapture ="sale";
	public static final String kSignedFields="access_key,transaction_type,reference_number,profile_id,transaction_uuid,currency,override_custom_receipt_page,amount,signed_date_time,signed_field_names,unsigned_field_names";//,ignore_avs,ignore_cvn";
	public static final String kUnsignedFields="card_type,card_number,card_expiry_date,card_cvn,payment_method,locale,bill_to_forename,bill_to_surname,bill_to_email,bill_to_address_line1,bill_to_address_line2,bill_to_address_city,bill_to_address_state,bill_to_address_country,bill_to_address_postal_code";
	public static final String kDateFormat ="yyyy-MM-dd'T'HH:mm:ss";
	public static final String kCyberSourceTestSecureAcceptanceURL ="https://testsecureacceptance.cybersource.com/silent/pay";
	public static final String kCyberSourceProdSecureAcceptanceURL ="https://secureacceptance.cybersource.com/silent/pay";
	public static final String kPayment_Method="card";
	public static final String kTimeZone="UTC";
	public static final String kRequest_access_key="access_key";
	public static final String kRequest_transaction_type= "transaction_type";
	public static final String kRequest_reference_number="reference_number";
	public static final String kRequest_payment_method="payment_method";
	public static final String kRequest_amount="amount";
	public static final String kRequest_profile_id="profile_id";
	public static final String kRequest_transaction_uuid="transaction_uuid";
	public static final String kRequest_override_custom_receipt_page="override_custom_receipt_page";
	public static final String kRequest_currency="currency";
	public static final String kRequest_signed_date_time="signed_date_time";
	public static final String kRequest_signed_field_names="signed_field_names";
	public static final String kRequest_unsigned_field_names="unsigned_field_names";
	public static final String kRequest_locale="locale";
	public static final String kRequest_signature="signature";
	//public static final String kRequest_ignore_avs="ignore_avs";
	//public static final String kRequest_ignore_cvn="ignore_cvn";
	public static final String kResponse_reason_code="reason_code";
	public static final String kResponse_decision="decision";
	public static final String kResponse_transaction_id="transaction_id";
	public static final String kResponse_invalid_fields="invalid_fields";
	public static final String kTransaction_Reference_No = "auth_trans_ref_no";
	public static final String kBill_Transaction_Reference_No = "bill_trans_ref_no";

}
