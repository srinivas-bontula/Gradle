package com.saba.taxpro.rater;

import java.util.ArrayList;
import java.util.List;

import com.saba.tax.common.Address;
import com.saba.tax.common.TaxDetail;
import com.saba.tax.common.TaxLineDetail;

public class ProTaxLogic {

	public void getLinetax(TaxLineDetail lineDetail, Address shippingAddress) throws Exception{
		ProTaxRater rater = getRater(shippingAddress);
		double amount = lineDetail.getAmount();
		double totalRate = 0.0;
		double totalTax = 0.0;
		List<TaxDetail> taxDetailList = rater.getTaxDetailList();
		List<TaxDetail> returnTaxList = new ArrayList<TaxDetail>();
		for(TaxDetail taxDetail : taxDetailList){
			double rate = taxDetail.getRate();
			TaxDetail cloneDetail = taxDetail.clone();
			double tax = amount*rate/100;
			totalRate = totalRate + rate;
			totalTax = totalTax + tax;
			cloneDetail.setTax(tax);
			returnTaxList.add(cloneDetail);
		}
		lineDetail.setRate(totalRate);
		lineDetail.setTax(totalTax);
		lineDetail.setTaxDetails(returnTaxList);
	}

	private ProTaxRater getRater(Address shippingAddress) throws Exception,	InternalError {
		ProTaxRater rater = TaxRateProcessor.getRaterForAddress(shippingAddress);
		if(rater.equals(TaxRateProcessor.errorRater)){
			throw new Exception("Error in tax calculation on server, Unable to determine tax for this state");
		}
		if(rater.equals(TaxRateProcessor.runTimeErrorRater)){
			throw new InternalError("Error in tax calculation, runtime exception");
		}
		return rater;
	}

	public boolean getExemptionStatusForAddress(Address shippingAddress) throws Exception {
		ProTaxRater rater = getRater(shippingAddress);
		return rater.isExempt();
	}

}
