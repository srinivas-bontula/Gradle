package in.amitv.mongowork.dto;

public class TaxRequestRecDTO extends CommonTaxRecDTO {

	String address;
	String encoding;
	String method;
	String remoteInfo;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getRemoteInfo() {
		return remoteInfo;
	}
	public void setRemoteInfo(String remoteInfo) {
		this.remoteInfo = remoteInfo;
	}
	@Override
	public String toString() {
		return "TaxRequestRecDTO [address=" + address + ", encoding="
				+ encoding + ", method=" + method + ", remoteInfo="
				+ remoteInfo + ", contentType=" + contentType + ", headers="
				+ headers + ", payload=" + payload + ", ts=" + ts + "]";
	}
	
}
