package com.reputation.trailing;

public class TrailingException extends Exception {
	private String errorCode;
	private String errorMsg;

	public TrailingException(String errMsg) {
		super(errMsg);
	}

	public TrailingException(String errCode, String errMsg) {
		super("[" + errCode + "]" + errMsg);
		this.errorCode = errCode;
		this.errorMsg = errMsg;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	public String getErrorMsg() {
		return errorMsg;
	}

}
