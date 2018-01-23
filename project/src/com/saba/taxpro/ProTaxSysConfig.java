package com.saba.taxpro;

public class ProTaxSysConfig {

	public static String raterConfigTypeCache = "cache";
	public static String raterConfigTypeMongo = "mongo";
	public static String raterConfig = raterConfigTypeMongo;
	
	public static void setRaterConfig(String type){
		if(type.equalsIgnoreCase(raterConfigTypeCache) || type.equals(raterConfigTypeMongo)){
			raterConfig = type;
		}else{
			throw new IllegalArgumentException("Invalid rater type passed, valid once are : "+raterConfigTypeCache+" / "+raterConfigTypeMongo);
		}
	}
	
}
