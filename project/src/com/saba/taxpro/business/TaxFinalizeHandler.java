package com.saba.taxpro.business;

import java.util.Random;

import com.saba.tax.common.TaxInfoRequestDetail;
import com.saba.tax.common.TaxInfoResponseDetail;
import com.saba.tax.common.TaxLineDetail;

public class TaxFinalizeHandler extends TaxRequestHandler {

	@Override
	public void handleRequest(TaxInfoRequestDetail request,
			TaxInfoResponseDetail response) throws Exception {

		response.setOrderNo(request.getOrderNo());
		response.setResponseCode("200");
		response.setResponseMessage("SUCCESS");
		response.setTaxTransactionDocID(new Integer(new Random().nextInt()).toString());
		
		// Set total amount and total tax fields here 
		double totalAmount = 0.0;
		double totalTax = 0.0;
		
		for(TaxLineDetail lineDetail : response.getTaxLines()){
			totalAmount = totalAmount + lineDetail.getAmount();
			totalTax = totalTax + lineDetail.getTax();
		}
		
		response.setTotalAmount(totalAmount);
		response.setTotalTax(totalTax);
		
		if(nextHandler != null){
			nextHandler.handleRequest(request, response);
		}
	}

}
