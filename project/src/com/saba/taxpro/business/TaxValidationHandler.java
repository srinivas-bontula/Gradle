package com.saba.taxpro.business;

import com.saba.tax.common.TaxInfoRequestDetail;
import com.saba.tax.common.TaxInfoResponseDetail;

public class TaxValidationHandler extends TaxRequestHandler {

	@Override
	public void handleRequest(TaxInfoRequestDetail request, TaxInfoResponseDetail response) throws Exception {
		if(nextHandler != null){
			nextHandler.handleRequest(request, response);
		}
	}

}
