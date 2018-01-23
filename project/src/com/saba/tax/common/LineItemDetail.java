package com.saba.tax.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by SGondhale on 6/11/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineItemDetail {

    private String lineNo;
    private String shipFrom;
    private String shipTo;
    private String billTo;
    private String itemCode;
    private int quantity = 1;
    private double amount;
    private String currency;
    private List<Map<String,Object>> customPrimary = new ArrayList<>();
    private List<Map<String,Object>> customSecondary = new ArrayList<>();;

    public LineItemDetail(){

    }
    public LineItemDetail(String lineNo, String shipFrom, String shipTo, String billTo, String itemCode, int quantity, double amount, String currency, List<Map<String, Object>> customPrimary, List<Map<String, Object>> customSecondary) {
        this.lineNo = lineNo;
        this.shipFrom = shipFrom;
        this.shipTo = shipTo;
        this.billTo = billTo;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.amount = amount;
        this.currency = currency;
        this.customPrimary = customPrimary;
        this.customSecondary = customSecondary;
    }

    public String getLineNo() {
        return lineNo;
    }

    public void setLineNo(String lineNo) {
        this.lineNo = lineNo;
    }

    public String getShipFrom() {
        return shipFrom;
    }

    public void setShipFrom(String shipFrom) {
        this.shipFrom = shipFrom;
    }

    public String getShipTo() {
        return shipTo;
    }

    public void setShipTo(String shipTo) {
        this.shipTo = shipTo;
    }

    public String getBillTo() {
        return billTo;
    }

    public void setBillTo(String billTo) {
        this.billTo = billTo;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Map<String, Object>> getCustomPrimary() {
        return customPrimary;
    }

    public void setCustomPrimary(List<Map<String, Object>> customPrimary) {
        this.customPrimary = customPrimary;
    }

    public void addCustomPrimary(Map<String,Object> customFields){
        if(customPrimary == null){
            customPrimary = new ArrayList<>();
        }
        customPrimary.add(customFields);
    }
    
    public List<Map<String, Object>> getCustomSecondary() {
        return customSecondary;
    }

    public void setCustomSecondary(List<Map<String, Object>> customSecondary) {
        this.customSecondary = customSecondary;
    }

    public void addCustomSecondary(Map<String,Object> customFields){
        if(customSecondary == null){
            customSecondary = new ArrayList<>();
        }
        customSecondary.add(customFields);
    }
    
}