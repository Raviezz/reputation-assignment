package com.reputation.trailing.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetAssetValue implements Serializable{
	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("date")
	private String recordedDate;
	
	@JsonProperty("nav")
	private String navValue;

	@Override
	public String toString() {
		return "NetAssetValue [recordedDate=" + recordedDate + ", navValue=" + navValue + "]";
	}

	public String getRecordedDate() {
		return recordedDate;
	}

	public void setRecordedDate(String recordedDate) {
		this.recordedDate = recordedDate;
	}

	public String getNavValue() {
		return navValue;
	}

	public void setNavValue(String navValue) {
		this.navValue = navValue;
	}

}
