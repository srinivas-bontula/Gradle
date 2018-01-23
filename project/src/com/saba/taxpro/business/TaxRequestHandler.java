package com.saba.taxpro.business;

import com.saba.tax.common.TaxInfoRequestDetail;
import com.saba.tax.common.TaxInfoResponseDetail;

public abstract class TaxRequestHandler {
	
	protected TaxRequestHandler nextHandler;
	
	public abstract void handleRequest(TaxInfoRequestDetail request, TaxInfoResponseDetail response) throws Exception;
	
	public void setNextHandler(TaxRequestHandler handler){
		this.nextHandler = handler;
	}
}
