package com.st.trex.dto;

public class ErrorHandler {

	private String errorMessage;
	private int httpStatus;
	public ErrorHandler() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ErrorHandler(String errorMessage, int httpStatus) {
		super();
		this.errorMessage = errorMessage;
		this.httpStatus = httpStatus;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public int getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	
}
