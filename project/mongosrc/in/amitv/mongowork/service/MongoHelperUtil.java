package in.amitv.mongowork.service;

import in.amitv.mongowork.dto.TaxRecDTO;
import in.amitv.spring.AppContextAware;

public class MongoHelperUtil {

	public static void addTaxRecToMongo(TaxRecDTO dto){
		TaxRecService service = (TaxRecService) AppContextAware.getApplicationContext().getBean("taxRecService");
		if(isLoggingRequired(dto)){
			service.addTaxRec(dto);
		}
	}

	private static boolean isLoggingRequired(TaxRecDTO dto) {
		if(dto.getReq().getAddress().endsWith("gettaxconfig")){
			return false;
		}
		return true;
	}
	
}
