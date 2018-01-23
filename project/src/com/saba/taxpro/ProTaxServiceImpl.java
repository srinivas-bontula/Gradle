package com.saba.taxpro;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.saba.tax.common.TaxExemptInfoRequestDetail;
import com.saba.tax.common.TaxExemptInfoResponseDetail;
import com.saba.tax.common.TaxInfoRequestDetail;
import com.saba.tax.common.TaxInfoResponseDetail;
import com.saba.taxpro.rater.ProTaxRaterWrapper;
import com.saba.taxpro.rater.TaxRateProcessor;


public class ProTaxServiceImpl implements ProTaxService {

	@Autowired
	private ProTaxHelper taxHelper; 
	
	@Override
	public Response getTax(TaxInfoRequestDetail request) {
		try{
			TaxInfoResponseDetail taxDetails = taxHelper.getTaxDetails(request);
			return Response.ok(taxDetails).build();
		}catch(Exception e){
			//return Response.status(Response.Status.BAD_REQUEST).build();
			TaxInfoResponseDetail msg = new TaxInfoResponseDetail();
			msg.setResponseCode("300");
			msg.setResponseMessage(e.getMessage());
			return Response.ok(msg).build();
		}
	}

	@Override
	public Response getTaxConfig() {
		return Response.ok(taxHelper.getTaxConfig()).build();
	}

	@Override
	public Response setTaxConfig(ProTaxRaterWrapper wrapper) {
		TaxRateProcessor.refreshMap(wrapper);
		return Response.ok(taxHelper.getTaxConfig()).build();
	}

	@Override
	public Response getTaxExemptionState(TaxExemptInfoRequestDetail request) {
		try{
			TaxExemptInfoResponseDetail exemptDetails = taxHelper.getTaxExemptionState(request);
			return Response.ok(exemptDetails).build();
		}catch(Exception e){
			TaxExemptInfoResponseDetail msg = new TaxExemptInfoResponseDetail();
			msg.setMessage(e.getMessage());
			return Response.ok(msg).build();
		}
	}

	@Override
	public Response getSysConfig() {
		Map<String,Object> sysConf = new HashMap<String,Object>();
		sysConf.put("raterConfig", ProTaxSysConfig.raterConfig);
		return Response.ok(sysConf).build();
	}

	@Override
	public Response setSysConfig(String value, String config) {
		if("raterConfig".equals(config)){
			ProTaxSysConfig.setRaterConfig(value);
		}else{
			throw new IllegalArgumentException("Invalid config passed");
		}
		return getSysConfig();
	}

}

