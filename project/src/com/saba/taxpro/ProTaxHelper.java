package com.saba.taxpro;

import com.saba.tax.common.TaxExemptInfoRequestDetail;
import com.saba.tax.common.TaxExemptInfoResponseDetail;
import com.saba.tax.common.TaxInfoRequestDetail;
import com.saba.tax.common.TaxInfoResponseDetail;
import com.saba.taxpro.business.TaxFinalizeHandler;
import com.saba.taxpro.business.TaxLineItemHandler;
import com.saba.taxpro.business.TaxRequestHandler;
import com.saba.taxpro.business.TaxValidationHandler;
import com.saba.taxpro.rater.ProTaxLogic;
import com.saba.taxpro.rater.ProTaxRater;
import com.saba.taxpro.rater.ProTaxRaterWrapper;
import com.saba.taxpro.rater.TaxRateProcessor;

public class ProTaxHelper {

	public TaxInfoResponseDetail getTaxDetails(TaxInfoRequestDetail request) throws Exception {
		TaxInfoResponseDetail taxDetail = new TaxInfoResponseDetail();
		
		TaxRequestHandler validationHandler = new TaxValidationHandler();
		TaxRequestHandler lineItemHandler = new TaxLineItemHandler();
		TaxRequestHandler finalizeHandler = new TaxFinalizeHandler();
		
		validationHandler.setNextHandler(lineItemHandler);
		lineItemHandler.setNextHandler(finalizeHandler);
		
		validationHandler.handleRequest(request, taxDetail);
		
		return taxDetail;
	}

	public ProTaxRaterWrapper getTaxConfig(){
		ProTaxRaterWrapper wrapper = new ProTaxRaterWrapper();
		for(ProTaxRater rater : TaxRateProcessor.raterList){
			wrapper.addRater(rater.getName(), rater);
		}
		return wrapper;
	}

	public TaxExemptInfoResponseDetail getTaxExemptionState(TaxExemptInfoRequestDetail request) throws Exception {
		ProTaxLogic logic = new ProTaxLogic();
		TaxExemptInfoResponseDetail exemptDetails = new TaxExemptInfoResponseDetail();
		boolean exemptStatus = logic.getExemptionStatusForAddress(request.getShippingAddress());
		exemptDetails.setTaxExempt(exemptStatus);
		exemptDetails.setMessage("SUCCESS");
		return exemptDetails;
	}	
}
