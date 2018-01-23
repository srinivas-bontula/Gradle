package com.saba.tax.common;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by SGondhale on 6/11/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    private String addressCode;
    private String line1;
    private String line2;
    private String line3;
    private String city;
    private String region;
    private String country;
    private String zip;

    public Address(){

    }

    public Address(String addressCode, String line1, String line2, String line3,String city, String region, String country, String zip) {
        this.addressCode = addressCode;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.city = city;
        this.region = region;
        this.country = country;
        this.zip = zip;
    }

    public String getAddressCode() {
        return addressCode;
    }

    public void setAddressCode(String addressCode) {
        this.addressCode = addressCode;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getLine3() {
        return line3;
    }

    public void setLine3(String line3) {
        this.line3 = line3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

}
