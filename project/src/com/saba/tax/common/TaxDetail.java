package com.saba.tax.common;

/**
 * Created by SGondhale on 6/17/2015.
 */
public class TaxDetail {
    private double rate;
    private double tax;
    private String taxName;
    private String jurisType;
    private String jurisName;
    private String taxNameId;

    public TaxDetail(){

    }

    public TaxDetail(double rate, double tax, String taxName, String jurisType, String jurisName, String taxNameId) {
        this.rate = rate;
        this.tax = tax;
        this.taxName = taxName;
        this.jurisType = jurisType;
        this.jurisName = jurisName;
        this.taxNameId = taxNameId;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public String getJurisType() {
        return jurisType;
    }

    public void setJurisType(String jurisType) {
        this.jurisType = jurisType;
    }

    public String getJurisName() {
        return jurisName;
    }

    public void setJurisName(String jurisName) {
        this.jurisName = jurisName;
    }

    public String getTaxNameId() {
        return taxNameId;
    }

    public void setTaxNameId(String taxNameId) {
        this.taxNameId = taxNameId;
    }

	public TaxDetail clone(){
		return new TaxDetail(this.rate, 0.0d, this.taxName, this.jurisType, this.jurisName, this.taxNameId);
	}

    
    
}
