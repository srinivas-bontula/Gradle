package com.saba.tax.common;

import java.text.SimpleDateFormat;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by SGondhale on 6/11/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaxInfoRequestDetail {

    private String orderNo;
    private String orderDate;
    private String custom0;
    private String custom1;
    private String custom2;
    private List<Address> addresses;
    private List<LineItemDetail> lines;

    public TaxInfoRequestDetail() {

    }

    public TaxInfoRequestDetail(String orderNo, String orderDate, String custom0, String custom1, String custom2, List<Address> addresses, List<LineItemDetail> lines) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.custom0 = custom0;
        this.custom1 = custom1;
        this.custom2 = custom2;
        this.addresses = addresses;
        this.lines = lines;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
       if(orderDate != null){
           return dateFormat.format(orderDate);
       }else{
           return "";
       }
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustom0() {
        return custom0;
    }

    public void setCustom0(String custom0) {
        this.custom0 = custom0;
    }

    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }
    
    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }
    
    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<LineItemDetail> getLines() {
        return lines;
    }

    public void setLines(List<LineItemDetail> lines) {
        this.lines = lines;
    }

}
