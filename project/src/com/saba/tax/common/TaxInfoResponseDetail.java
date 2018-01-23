package com.saba.tax.common;

import java.util.List;

/**
 * Created by SGondhale on 6/11/2015.
 */
public class TaxInfoResponseDetail {

    private String orderNo;
    private String taxTransactionDocID;
    private String responseCode;
    private String responseMessage;
    private double totalAmount;
    private double totalTax;
    private List<TaxLineDetail> taxLines;

    public TaxInfoResponseDetail(){

    }

    public TaxInfoResponseDetail(String orderNo, String taxTransactionDocID, String responseCode,String responseMessage ,double totalAmount, double totalTax, List<TaxLineDetail> taxLines) {
        this.orderNo = orderNo;
        this.taxTransactionDocID = taxTransactionDocID;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.totalAmount = totalAmount;
        this.totalTax = totalTax;
        this.taxLines = taxLines;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTaxTransactionDocID() {
        return taxTransactionDocID;
    }

    public void setTaxTransactionDocID(String taxTransactionDocID) {
        this.taxTransactionDocID = taxTransactionDocID;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(double totalTax) {
        this.totalTax = totalTax;
    }

    public List<TaxLineDetail> getTaxLines() {
        return taxLines;
    }

    public void setTaxLines(List<TaxLineDetail> taxLines) {
        this.taxLines = taxLines;
    }

}
