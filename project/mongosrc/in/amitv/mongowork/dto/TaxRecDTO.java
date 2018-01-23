package in.amitv.mongowork.dto;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="taxrec")
public class TaxRecDTO {
	
	TaxRequestRecDTO req;
	TaxResponseRecDTO resp;

	public TaxRecDTO() {
		req = new TaxRequestRecDTO();
		resp = new TaxResponseRecDTO();
	}
	
	public TaxRequestRecDTO getReq() {
		return req;
	}
	public void setReq(TaxRequestRecDTO req) {
		this.req = req;
	}
	public TaxResponseRecDTO getResp() {
		return resp;
	}
	public void setResp(TaxResponseRecDTO resp) {
		this.resp = resp;
	}

	@Override
	public String toString() {
		return "TaxRecDTO [req=" + req + ", resp=" + resp + "]";
	}

}
