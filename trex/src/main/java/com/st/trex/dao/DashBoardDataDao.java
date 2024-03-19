package com.st.trex.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.st.trex.dto.DashboardData;
import com.st.trex.dto.InputDashBoardDto;
import com.st.trex.dto.SeriesData;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class DashBoardDataDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	HikariDataSource hikariDataSource;

	private static final Logger logger = LoggerFactory.getLogger(DashBoardDataDao.class);
	
	public List<DashboardData> getDashboardDataList(InputDashBoardDto inputDto) throws Exception {

		System.out.print("dto "+inputDto);
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
//		String sql = "select TO_CHAR(Insertion_Date, ?) A, status B, count(*) C\r\n" + 
//		"from YPAPA_Regression_Data_RTL\r\n" + 
//		"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
//		"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
//		"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
//		"group by TO_CHAR(Insertion_Date, 'DD'), status\r\n" + 
//		"order by A, B";
		String sql="";
		if(inputDto.getGranularity().equals("IW")) {
			sql =" select TO_CHAR(Insertion_Date, 'IW') A, status B, count(*) C \r\n" + 
					"from YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"group by TO_CHAR(Insertion_Date, 'IW'), status\r\n" + 
					"order by A";
		}
		else if (inputDto.getGranularity().equals("MON")) {
			sql =" select TO_CHAR(Insertion_Date, 'MON') A, status B, count(*) C \r\n" + 
					"from YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"group by TO_CHAR(Insertion_Date, 'MON'), status\r\n" +  
					"ORDER BY TO_NUMBER(TO_CHAR(TO_DATE(TO_CHAR(Insertion_Date, 'MON'), 'MON'), 'MM')),\r\n" + 
					"  status";
		}
		else {
			sql =" select Insertion_Date A, status B, count(*) C \r\n" + 
					"from YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
				    "group by Insertion_Date, status \r\n" + 
					"					order by A";
		}
 

		try {
			connection = hikariDataSource.getConnection();
			ps = connection.prepareStatement(sql);

			ps.setString(1, inputDto.getCategory()); // set the category parameter
			ps.setString(2, inputDto.getCategory()); // set the category parameter
			ps.setString(3, inputDto.getOwner()); // set the owner parameter
			ps.setString(4, inputDto.getOwner()); // set the owner parameter
			ps.setString(5, inputDto.getStartDate()); // set the start date parameter
			ps.setString(6, inputDto.getEndDate()); // set the end date parameter

			rs = ps.executeQuery();
			Map<String, DashboardData> dashboardMap = new LinkedHashMap<>();

			while (rs.next()) {
			    String dashboardName = rs.getString("A");
			    String seriesName = rs.getString("B");
			    int value = rs.getInt("C");

			    DashboardData dashboardData = dashboardMap.getOrDefault(dashboardName, new DashboardData());
			    dashboardData.setName(dashboardName);

			    SeriesData seriesData = new SeriesData();
			    seriesData.setName(seriesName);
			    seriesData.setValue(value);

			    List<SeriesData> seriesList = dashboardData.getSeries();
			    if (seriesList == null) {
			        seriesList = new ArrayList<>();
			    }
			    seriesList.add(seriesData);

			    dashboardData.setSeries(seriesList);
			    dashboardMap.put(dashboardName, dashboardData);
			}

			List<DashboardData> dashboardList = new ArrayList<>(dashboardMap.values());
			return dashboardList;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
		}
		return null;
	}
	
	public List<DashboardData> getDashboardDataListCoverage(InputDashBoardDto inputDto) throws Exception {

		System.out.print("dto "+inputDto);
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql="";
		if(inputDto.getGranularity().equals("IW")) {
			sql =" select TO_CHAR(Insertion_Date, 'IW') A, NAME B, SUM(SCORE) C \r\n" + 
					"from YPAPA_COVERAGE_DATA_RTL\r\n" + 
					"WHERE NAME LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"group by TO_CHAR(Insertion_Date, 'IW'), NAME\r\n" + 
					"order by A";
		}
		else if (inputDto.getGranularity().equals("MON")) {
			sql ="  SELECT TO_CHAR(Insertion_Date, 'MON') A,\r\n" + 
					"  NAME B,\r\n" + 
					"  SUM(SCORE) C\r\n" + 
					"FROM YPAPA_COVERAGE_DATA_RTL\r\n" + 
					"WHERE NAME LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"GROUP BY TO_CHAR(Insertion_Date, 'MON'),\r\n" + 
					"  NAME\r\n" + 
					"ORDER BY TO_NUMBER(TO_CHAR(TO_DATE(TO_CHAR(Insertion_Date, 'MON'), 'MON'), 'MM')),\r\n" + 
					"  NAME";
		}
		else {
			sql =" select TO_CHAR(Insertion_Date, 'DD') A, NAME B, SUM(SCORE) C \r\n" + 
					"from YPAPA_COVERAGE_DATA_RTL\r\n" + 
					"WHERE NAME LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
				    "group by TO_CHAR(Insertion_Date, 'DD'), NAME \r\n" + 
					"					order by A";
		}
 

		try {
			connection = hikariDataSource.getConnection();
			ps = connection.prepareStatement(sql);

			ps.setString(1, inputDto.getCategory()); // set the category parameter
			ps.setString(2, inputDto.getCategory()); // set the category parameter
			ps.setString(3, inputDto.getOwner()); // set the owner parameter
			ps.setString(4, inputDto.getOwner()); // set the owner parameter
			ps.setString(5, inputDto.getStartDate()); // set the start date parameter
			ps.setString(6, inputDto.getEndDate()); // set the end date parameter

			rs = ps.executeQuery();
			Map<String, DashboardData> dashboardMap = new LinkedHashMap<>();

			while (rs.next()) {
			    String dashboardName = rs.getString("A");
			    String seriesName = rs.getString("B");
			    int value = rs.getInt("C");

			    DashboardData dashboardData = dashboardMap.getOrDefault(dashboardName, new DashboardData());
			    dashboardData.setName(dashboardName);

			    SeriesData seriesData = new SeriesData();
			    seriesData.setName(seriesName);
			    seriesData.setValue(value);

			    List<SeriesData> seriesList = dashboardData.getSeries();
			    if (seriesList == null) {
			        seriesList = new ArrayList<>();
			    }
			    seriesList.add(seriesData);

			    dashboardData.setSeries(seriesList);
			    dashboardMap.put(dashboardName, dashboardData);
			}

			List<DashboardData> dashboardList = new ArrayList<>(dashboardMap.values());
			return dashboardList;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
		}
		return null;
	}
	
	public List<DashboardData> getDashboardDataListCoverageByLine(InputDashBoardDto inputDto) throws Exception {

		System.out.print("dto "+inputDto);
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql="";
		if(inputDto.getGranularity().equals("IW")) {
			sql =" select NAME A,TO_CHAR(Insertion_Date, 'IW') B, SUM(SCORE) C \r\n" + 
					"from YPAPA_COVERAGE_DATA_RTL\r\n" + 
					"WHERE NAME LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"group by TO_CHAR(Insertion_Date, 'IW'), NAME\r\n" + 
					"order by B";
		}
		else if (inputDto.getGranularity().equals("MON")) {
			sql ="  SELECT NAME A,TO_CHAR(Insertion_Date, 'MON') B,\r\n" +
					"  SUM(SCORE) C\r\n" + 
					"FROM YPAPA_COVERAGE_DATA_RTL\r\n" + 
					"WHERE NAME LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"GROUP BY TO_CHAR(Insertion_Date, 'MON'),\r\n" + 
					"  NAME\r\n" + 
					"ORDER BY TO_NUMBER(TO_CHAR(TO_DATE(TO_CHAR(Insertion_Date, 'MON'), 'MON'), 'MM')),\r\n" + 
					"  NAME";
		}
		else {
			sql =" select NAME A,TO_CHAR(Insertion_Date, 'DD') B , SUM(SCORE) C \r\n" + 
					"from YPAPA_COVERAGE_DATA_RTL\r\n" + 
					"WHERE NAME LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
				    "group by TO_CHAR(Insertion_Date, 'DD'), NAME \r\n" + 
					"					order by B";
		}
 

		try {
			connection = hikariDataSource.getConnection();
			ps = connection.prepareStatement(sql);

			ps.setString(1, inputDto.getCategory()); // set the category parameter
			ps.setString(2, inputDto.getCategory()); // set the category parameter
			ps.setString(3, inputDto.getOwner()); // set the owner parameter
			ps.setString(4, inputDto.getOwner()); // set the owner parameter
			ps.setString(5, inputDto.getStartDate()); // set the start date parameter
			ps.setString(6, inputDto.getEndDate()); // set the end date parameter

			rs = ps.executeQuery();
			Map<String, DashboardData> dashboardMap = new LinkedHashMap<>();

			while (rs.next()) {
			    String dashboardName = rs.getString("A");
			    String seriesName = rs.getString("B");
			    int value = rs.getInt("C");

			    DashboardData dashboardData = dashboardMap.getOrDefault(dashboardName, new DashboardData());
			    dashboardData.setName(dashboardName);

			    SeriesData seriesData = new SeriesData();
			    seriesData.setName(seriesName);
			    seriesData.setValue(value);

			    List<SeriesData> seriesList = dashboardData.getSeries();
			    if (seriesList == null) {
			        seriesList = new ArrayList<>();
			    }
			    seriesList.add(seriesData);

			    dashboardData.setSeries(seriesList);
			    dashboardMap.put(dashboardName, dashboardData);
			}

			List<DashboardData> dashboardList = new ArrayList<>(dashboardMap.values());
			return dashboardList;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
		}
		return null;
	}
	
	public List<DashboardData> getDashboardDataListByCategory(InputDashBoardDto inputDto) throws Exception {
		try {

			String sql = "SELECT category A, status B, COUNT(*) C \r\n" + 
					"FROM YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE Insertion_Date BETWEEN TO_DATE(?, 'DD-MM-YYYY') AND TO_DATE(?, 'DD-MM-YYYY')\r\n" + 
					"GROUP BY category, status\r\n" + 
					"order by category,status";

			List<Map<String, Object>> backlogInfo = jdbcTemplate.queryForList(sql,
					inputDto.getStartDate(),inputDto.getEndDate());
			Map<String, DashboardData> dashboardMap = new HashMap<>();

			for (Map<String, Object> row : backlogInfo) {
			    String dashboardName = (String) row.get("A");
			    String seriesName = (String) row.get("B");
			    int value = (int) row.get("C");

			    DashboardData dashboardData = dashboardMap.getOrDefault(dashboardName, new DashboardData());
			    dashboardData.setName(dashboardName);

			    SeriesData seriesData = new SeriesData();
			    seriesData.setName(seriesName);
			    seriesData.setValue(value);

			    List<SeriesData> seriesList = dashboardData.getSeries();
			    if (seriesList == null) {
			        seriesList = new ArrayList<>();
			    }
			    seriesList.add(seriesData);

			    dashboardData.setSeries(seriesList);
			    dashboardMap.put(dashboardName, dashboardData);
			}

			List<DashboardData> dashboardList = new ArrayList<>(dashboardMap.values());
			return dashboardList;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in Method:getBacklogData:", e);
			throw new Exception(e);
		}
	}
	
	public List<DashboardData> getDashboardDataListByOwner(InputDashBoardDto inputDto) throws Exception {
		try {

			String sql = "SELECT owner A, status B, COUNT(*) C \r\n" + 
					"FROM YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE Insertion_Date BETWEEN TO_DATE(?, 'DD-MM-YYYY') AND TO_DATE(?, 'DD-MM-YYYY')\r\n" + 
					"GROUP BY owner, status\r\n" + 
					"order by owner,status";

			List<Map<String, Object>> backlogInfo = jdbcTemplate.queryForList(sql,
					inputDto.getStartDate(),inputDto.getEndDate());
			Map<String, DashboardData> dashboardMap = new HashMap<>();

			for (Map<String, Object> row : backlogInfo) {
			    String dashboardName = (String) row.get("A");
			    String seriesName = (String) row.get("B");
			    int value = (int) row.get("C");

			    DashboardData dashboardData = dashboardMap.getOrDefault(dashboardName, new DashboardData());
			    dashboardData.setName(dashboardName);

			    SeriesData seriesData = new SeriesData();
			    seriesData.setName(seriesName);
			    seriesData.setValue(value);

			    List<SeriesData> seriesList = dashboardData.getSeries();
			    if (seriesList == null) {
			        seriesList = new ArrayList<>();
			    }
			    seriesList.add(seriesData);

			    dashboardData.setSeries(seriesList);
			    dashboardMap.put(dashboardName, dashboardData);
			}

			List<DashboardData> dashboardList = new ArrayList<>(dashboardMap.values());
			return dashboardList;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in Method:getBacklogData:", e);
			throw new Exception(e);
		}
	}
	
	public List<String> getCategories() {
		List<String> list = new ArrayList<>();
		list.add("ALL");

		String sql = "select DISTINCT CATEGORY from YPAPA_Regression_Data_RTL";

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("CATEGORY"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return list;
	}
	
	public List<String> getCoverageNames() {
		List<String> list = new ArrayList<>();
		list.add("ALL");

		String sql = "select DISTINCT NAME from YPAPA_COVERAGE_DATA_RTL";

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("NAME"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return list;
	}
	
	public List<String> getOwners() {
		List<String> list = new ArrayList<>();
		list.add("ALL");

		String sql = "select DISTINCT OWNER from YPAPA_Regression_Data_RTL";

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("OWNER"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return list;
	}
	
	public List<String> getCoverageOwners() {
		List<String> list = new ArrayList<>();
		list.add("ALL");

		String sql = "select DISTINCT OWNER from YPAPA_COVERAGE_DATA_RTL";

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			rs = statement.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("OWNER"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return list;
	}

	public List<DashboardData> getDashboardDataListByLine(InputDashBoardDto inputDto) {


		System.out.print("dto "+inputDto);
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql="";
		if(inputDto.getGranularity().equals("IW")) {
			sql =" select status A, TO_CHAR(Insertion_Date, 'IW') B, count(*) C \r\n" + 
					"from YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"group by TO_CHAR(Insertion_Date, 'IW'), status\r\n" + 
					"order by B";
		}
		else if (inputDto.getGranularity().equals("MON")) {
			sql =" select status A, TO_CHAR(Insertion_Date, 'MON') B, count(*) C \r\n" + 
					"from YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
					"group by TO_CHAR(Insertion_Date, 'MON'), status\r\n" +  
					"ORDER BY TO_NUMBER(TO_CHAR(TO_DATE(TO_CHAR(Insertion_Date, 'MON'), 'MON'), 'MM')),\r\n" + 
					"  status";
		}
		else {
			sql =" select status A, TO_CHAR(Insertion_Date, 'DD') B, count(*) C \r\n" + 
					"from YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
				    "group by TO_CHAR(Insertion_Date, 'DD'), status \r\n" + 
					"					order by B";
		}
 
//		String sql ="select TO_CHAR(sysdate, 'IW') from dual";
		try {
			connection = hikariDataSource.getConnection();
			ps = connection.prepareStatement(sql);
//			ps.setString(1, "IW"); 
//			ps.setString(1, inputDto.getGranularity()); // set the granularity parameter
			ps.setString(1, inputDto.getCategory()); // set the category parameter
			ps.setString(2, inputDto.getCategory()); // set the category parameter
			ps.setString(3, inputDto.getOwner()); // set the owner parameter
			ps.setString(4, inputDto.getOwner()); // set the owner parameter
			ps.setString(5, inputDto.getStartDate()); // set the start date parameter
			ps.setString(6, inputDto.getEndDate()); // set the end date parameter
//			ps.setString(8, inputDto.getGranularity());
			rs = ps.executeQuery();
			Map<String, DashboardData> dashboardMap = new LinkedHashMap<>();

			while (rs.next()) {
			    String dashboardName = rs.getString("A");
			    String seriesName = rs.getString("B");
			    int value = rs.getInt("C");

			    DashboardData dashboardData = dashboardMap.getOrDefault(dashboardName, new DashboardData());
			    dashboardData.setName(dashboardName);

			    SeriesData seriesData = new SeriesData();
			    seriesData.setName(seriesName);
			    seriesData.setValue(value);

			    List<SeriesData> seriesList = dashboardData.getSeries();
			    if (seriesList == null) {
			        seriesList = new ArrayList<>();
			    }
			    seriesList.add(seriesData);

			    dashboardData.setSeries(seriesList);
			    dashboardMap.put(dashboardName, dashboardData);
			}

			List<DashboardData> dashboardList = new ArrayList<>(dashboardMap.values());
			return dashboardList;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(ps);
			DbUtils.closeQuietly(rs);
		}
		return null;
	
		
	}



}