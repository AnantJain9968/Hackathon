package com.st.trex.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.st.trex.dto.DashboardData;
import com.st.trex.dto.InputDashBoardDto;

@Service
public interface IDashboardService {
	
	List<DashboardData> getDashBoardData();
	List<String> getCategories();
	List<DashboardData> getDashBoardDataByType(InputDashBoardDto inputDto) throws Exception;
	List<DashboardData> getDashBoardDataByLineType(InputDashBoardDto inputDto) throws Exception;
	List<String> getOwners();
	List<DashboardData> getDashBoardDataByCategory(InputDashBoardDto inputDto) throws Exception;
	List<DashboardData> getDashBoardDataByOwner(InputDashBoardDto inputDto) throws Exception;
	List<DashboardData> getCategoryData(InputDashBoardDto inputDto)throws Exception;
	List<String> getCoverageNames();


}
