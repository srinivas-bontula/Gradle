package com.saba.taxpro.business;

import java.util.ArrayList;
import java.util.List;

import com.saba.tax.common.Address;
import com.saba.tax.common.LineItemDetail;
import com.saba.tax.common.TaxInfoRequestDetail;
import com.saba.tax.common.TaxInfoResponseDetail;
import com.saba.tax.common.TaxLineDetail;
import com.saba.taxpro.rater.ProTaxLogic;

public class TaxLineItemHandler extends TaxRequestHandler {

	@Override
	public void handleRequest(TaxInfoRequestDetail request,
			TaxInfoResponseDetail response) throws Exception {
		
		ProTaxLogic logic = new ProTaxLogic();
		if(request.getLines() != null){
			List<TaxLineDetail> taxLines = new ArrayList<TaxLineDetail>();
			for(LineItemDetail itemDetail : request.getLines()){
				TaxLineDetail lineDetail = new TaxLineDetail();
				lineDetail.setLineNo(itemDetail.getLineNo());
				//lineDetail.setAmount(itemDetail.getAmount()*itemDetail.getQuantity());
				lineDetail.setAmount(itemDetail.getAmount());
				Address shippingAddress = getShippingAddress(itemDetail.getShipTo(),request.getAddresses());
				logic.getLinetax(lineDetail, shippingAddress);
				taxLines.add(lineDetail);
			}
			response.setTaxLines(taxLines);
		}
		
		
		if(nextHandler != null){
			nextHandler.handleRequest(request, response);
		}
	}

	private Address getShippingAddress(String shipTo, List<Address> addresses) {
		for(Address addr : addresses){
			if(addr.getAddressCode().equals(shipTo)){
				return addr;
			}
		}
		return null;
	}

}
