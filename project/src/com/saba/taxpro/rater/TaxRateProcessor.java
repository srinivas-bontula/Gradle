package com.saba.taxpro.rater;

import in.amitv.mongowork.repository.TaxRaterRepository;
import in.amitv.spring.AppContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.saba.tax.common.Address;
import com.saba.tax.common.TaxDetail;
import com.saba.taxpro.ProTaxSysConfig;

public class TaxRateProcessor {

	private static TaxRaterRepository taxRaterRepository;
	public static List<ProTaxRater> raterList = new ArrayList<ProTaxRater>();
	public static ProTaxRater defaultRater;
	public static ProTaxRater errorRater;
	public static ProTaxRater runTimeErrorRater;
	
	static {
		if(ProTaxSysConfig.raterConfig.equals(ProTaxSysConfig.raterConfigTypeMongo)){
			List<ProTaxRater> list = getTaxRaterRepository().findAll();
			ProTaxRaterWrapper wrapper = new ProTaxRaterWrapper();
			for(ProTaxRater rater : list){
				wrapper.addRater(rater.getName(), rater);
			}
			refreshMap(wrapper);
		}else{
			ProTaxRater caStateRater = new ProTaxRater();
			caStateRater.setName("defaultConfig");
			caStateRater.setCurrency("USD");
			Address caStateAddress = new Address();
			caStateAddress.setRegion("CA");
			caStateRater.setAddress(caStateAddress);
			TaxDetail ca1 = new TaxDetail();
			ca1.setJurisName("CA");
			ca1.setJurisType("State");
			ca1.setTaxName("State tax");
			ca1.setTaxNameId("State tax id");
			ca1.setRate(0.02);
			TaxDetail ca2 = new TaxDetail();
			ca2.setJurisName("CA");
			ca2.setJurisType("County");
			ca2.setTaxName("County tax");
			ca2.setTaxNameId("County tax id");
			ca2.setRate(0.15);
			List<TaxDetail> taxList = new ArrayList<TaxDetail>();
			taxList.add(ca1);
			taxList.add(ca2);
			caStateRater.setTaxDetailList(taxList);
			raterList.add(caStateRater);
			defaultRater = caStateRater;
		}
	}
	
	public static ProTaxRater getRaterForAddress(Address shippingAddress){
		String state = shippingAddress.getRegion();
		for(ProTaxRater tempRater : raterList){
			if(tempRater.getAddress().getRegion().equals(state)){
				return tempRater;
			}
		}
		
		return defaultRater;
	}
	
	public static void refreshMap(ProTaxRaterWrapper wrapper){
		Map<String, ProTaxRater> raterMap = wrapper.getRaterMap();
		raterList.clear();
		for(String raterName : raterMap.keySet()){
			ProTaxRater proTaxRater = raterMap.get(raterName);
			proTaxRater.setName(raterName);
			raterList.add(proTaxRater);
			if("defaultConfig".equals(raterName)){
				defaultRater = proTaxRater;
			}else if("errorConfig".equals(raterName)){
				errorRater = proTaxRater;
			}else if("runtimeErrorConfig".equals(raterName)){
				runTimeErrorRater = proTaxRater;
			}
			if(ProTaxSysConfig.raterConfig.equals(ProTaxSysConfig.raterConfigTypeMongo)){
				getTaxRaterRepository().save(proTaxRater);
			}
		}

	}
	
	private static TaxRaterRepository getTaxRaterRepository(){
		if(taxRaterRepository == null){
			taxRaterRepository = (TaxRaterRepository) AppContextAware.getApplicationContext().getBean("taxRaterRepository");
		}
		return taxRaterRepository;
	}
	
}
