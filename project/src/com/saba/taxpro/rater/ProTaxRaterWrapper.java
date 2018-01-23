package com.saba.taxpro.rater;

import java.util.HashMap;
import java.util.Map;

public class ProTaxRaterWrapper {

	Map<String,ProTaxRater> raterMap;
	
	public ProTaxRaterWrapper() {
		raterMap = new HashMap<String,ProTaxRater>();
	}

	public Map<String, ProTaxRater> getRaterMap() {
		return raterMap;
	}

	public void setRaterMap(Map<String, ProTaxRater> raterMap) {
		this.raterMap = raterMap;
	}
	
	public void addRater(String name, ProTaxRater rater){
		raterMap.put(name, rater);
	}
}
