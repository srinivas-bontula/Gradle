package com.saba.tax.common;

/**
 * Created by SGondhale on 9/2/2015.
 */
public class TaxExemptInfoResponseDetail {

    private boolean taxExempt;
    private String message;

    public TaxExemptInfoResponseDetail(){

    }

    public TaxExemptInfoResponseDetail(boolean taxExempt, String message) {
        this.taxExempt = taxExempt;
        this.message = message;
    }

    public boolean isTaxExempt() {
        return taxExempt;
    }

    public void setTaxExempt(boolean taxExempt) {
        this.taxExempt = taxExempt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
