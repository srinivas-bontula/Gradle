package com.saba.taxpro.rater;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.saba.tax.common.Address;
import com.saba.tax.common.TaxDetail;

@Document(collection="taxratecfg")
public class ProTaxRater {
	
	@Id
	private String name;
	private String currency;
	private Address address;
	private List<TaxDetail> taxDetailList;
	private boolean exempt;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public List<TaxDetail> getTaxDetailList() {
		return taxDetailList;
	}
	public void setTaxDetailList(List<TaxDetail> taxDetailList) {
		this.taxDetailList = taxDetailList;
	}
	public boolean isExempt() {
		return exempt;
	}
	public void setExempt(boolean exempt) {
		this.exempt = exempt;
	}
	
}
