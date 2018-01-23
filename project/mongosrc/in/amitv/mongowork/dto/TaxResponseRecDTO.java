package in.amitv.mongowork.dto;

public class TaxResponseRecDTO extends CommonTaxRecDTO {

	String responseCode;

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	@Override
	public String toString() {
		return "TaxResponseRecDTO [responseCode=" + responseCode
				+ ", contentType=" + contentType + ", headers=" + headers
				+ ", payload=" + payload + ", ts=" + ts + "]";
	}
	
}
