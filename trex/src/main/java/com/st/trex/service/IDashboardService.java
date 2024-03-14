package com.st.trex.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.st.trex.dto.DashboardData;

@Service
public interface IDashboardService {
	
	List<DashboardData> getDashBoardData();

}
