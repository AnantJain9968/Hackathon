package com.st.trex.dto;

import lombok.Data;

@Data
public class InputDashBoardDto {
	private String category;
	private String owner;
	private String startDate;
	private String endDate;
	private String granularity;
	
    public String getEndDate() {
    	if(endDate==null) {
    		return "01-JAN-2900";
    	}
		return endDate;
    	
    }
    public String getStartDate() {
    	if(startDate==null) {
    		return "01-JAN-1900";
    	}
		return startDate;
    	
    }
	
}
