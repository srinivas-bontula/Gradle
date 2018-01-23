package com.saba.tax.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SGondhale on 9/2/2015.
 */
public class TaxExemptInfoRequestDetail {

    private String firstName;
    private String lastName;
    private String username;
    private String personNo;
    private Address shippingAddress;
    private List<Map<String,Object>> profileCustom = new ArrayList<>();
    private String custom0;
    private String custom1;
    private String custom2;

    public TaxExemptInfoRequestDetail(){

    }

    public TaxExemptInfoRequestDetail(String firstName, String lastName, String username, String personNo, Address shippingAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.personNo = personNo;
        this.shippingAddress = shippingAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPersonNo() {
        return personNo;
    }

    public void setPersonNo(String personNo) {
        this.personNo = personNo;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<Map<String, Object>> getProfileCustom() {
        return profileCustom;
    }

    public void setProfileCustom(List<Map<String, Object>> custom) {
        this.profileCustom = custom;
    }


    public void addProfileCustomValues(Map<String,Object> customFields){
        if(this.profileCustom == null){
            this.profileCustom = new ArrayList<>();
        }

        this.profileCustom.add(customFields);
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
}
