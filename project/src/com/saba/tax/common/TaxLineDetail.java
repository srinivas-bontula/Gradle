package com.saba.tax.common;

import java.util.List;

/**
 * Created by SGondhale on 6/11/2015.
 */
public class TaxLineDetail {

    private String lineNo;
    private double amount;
    private double rate;
    private double tax;
    private List<TaxDetail> taxDetails;
    public TaxLineDetail(){

    }

    public TaxLineDetail(String lineNo, double amount, double rate, double tax, List<TaxDetail> taxDetails) {
        this.lineNo = lineNo;
        this.amount = amount;
        this.rate = rate;
        this.tax = tax;
        this.taxDetails = taxDetails;
    }

    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public List<TaxDetail> getTaxDetails() {
        return taxDetails;
    }

    public void setTaxDetails(List<TaxDetail> taxDetails) {
        this.taxDetails = taxDetails;
    }

}
