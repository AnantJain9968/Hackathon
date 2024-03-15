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
					"					order by A";
		}
		else {
			sql =" select TO_CHAR(Insertion_Date, 'DD') A, status B, count(*) C \r\n" + 
					"from YPAPA_Regression_Data_RTL\r\n" + 
					"WHERE CATEGORY LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND OWNER LIKE DECODE(?,'ALL','%',?)\r\n" + 
					"AND Insertion_Date between TO_DATE(?, 'DD-MON-YYYY') and TO_DATE(?, 'DD-MON-YYYY')\r\n" + 
				    "group by TO_CHAR(Insertion_Date, 'DD'), status \r\n" + 
					"					order by A";
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



	/*public boolean updateBackLogData(List<BacklogData> backlogDataList, List<String> subItemIdList) throws Exception {

		String updateSql = " UPDATE SO_SCHEDULED\r\n" + "SET COMMITTED_QTY            =?,\r\n"
				+ "  PLANT__CODE                =?,\r\n" + "  FINISHED_GOOD__CODE        =?,\r\n"
				+ "  FIRST_COMMITTED_DATE       =TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') ,\r\n"
				+ "  LAST_COMMITTED_DATE        =TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS'),\r\n"
				+ "  LAST_COMMITTED_DATE_FLAG   =?,\r\n"
				+ "  SHIP_DATE                  =TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS'),\r\n"
				+ "  LAST_UPDATING_USER         =?,\r\n" + "  TBA_REASON__CODE           =?,\r\n"
				+ "  TBA_DATE                   =TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS'),\r\n"
				+ "  TBA_FLAG                   =?,\r\n"
				+ "  TBA_START_DATE             = DECODE(NVL(trim(?),'X'),'X',?,sysdate),\r\n"
				+ "  STATUS_DATE                =sysdate,\r\n" + "  LAST_UPDATING_DATE         =sysdate,\r\n"
				+ "  FULLY_INVD_FLAG            =?,\r\n" + "  AVAILABLE_QTY              =?,\r\n"
				+ "  PLANT_SO_STATUS__CODE      =? , SO_STATUS__CODE =? ,SCHEDULING_TYPE__CODE =? ,\r\n" + "  SHIP_MODE__CODE            =?,\r\n"
				+ "  STORE__CODE = (select NVL(MAX(STORE_CODE), '2001') from store_info where plant__code = ? and flag_scm = 'D' and ACTIVITY_STATUS__CODE = '30')\r\n"
				+ "WHERE ID                     =?\r\n" + "AND (COMMITTED_QTY          !=?\r\n"
				+ "OR PLANT__CODE              !=?\r\n" + "OR FINISHED_GOOD__CODE      !=RPAD(?,16)\r\n"
				+ "OR FIRST_COMMITTED_DATE     !=TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')\r\n"
				+ "OR LAST_COMMITTED_DATE      !=TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')\r\n"
				+ "OR LAST_COMMITTED_DATE_FLAG !=RPAD(?,1)\r\n"
				+ "OR SHIP_DATE                !=TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')\r\n"
				+ "OR TBA_REASON__CODE         !=RPAD(?,6)\r\n"
				+ "OR TBA_DATE                 !=TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')\r\n"
				+ "OR TBA_FLAG                 !=RPAD(?,1)\r\n"
				+ "OR TBA_START_DATE           != DECODE(NVL(trim(?),'X'),'X',?,sysdate)\r\n"
				+ "OR FULLY_INVD_FLAG          !=RPAD(?,1)\r\n" + "OR AVAILABLE_QTY            !=?\r\n"
				+ "OR PLANT_SO_STATUS__CODE    !=RPAD(?,2)\r\n   OR SO_STATUS__CODE    !=RPAD(?,2)  OR SCHEDULING_TYPE__CODE    !=RPAD(?,2)"  + "OR (SHIP_MODE__CODE          !=RPAD(?,2) or SHIP_MODE__CODE is null) )"
		// + ")"
		;
		String deleteConsumedQtySql ="delete from CONSUMED_QTY where SO_SCHEDULED__ID =?";
		String setSkipReasonSql = "UPDATE S4_ORDER_DATA\r\n" + "SET PROCESS_STATUS_CODE=?,\r\n"
				+ "  SKIP_REASON          =\r\n" + "  (SELECT descr FROM S4_PLD_STATUS WHERE CODE=?\r\n" + "  )\r\n"
				+ "WHERE ID = ?";

		String deleteSql = "UPDATE SO_SCHEDULED set SO_STATUS__CODE=40,LAST_UPDATING_USER=?,LAST_UPDATING_DATE=sysdate,PLANT_SO_STATUS__CODE=40 where ID=?";

		String insertIntoTxnOldSystem = "INSERT\r\n" + "INTO TXN_OLD_SYSTEM\r\n" + "  (\r\n" + "    ID,\r\n"
				+ "    INSERTION_DATE,\r\n" + "    FLAG_WHERE,\r\n" + "    FLAG_ACTION,\r\n" + "    FLAG_LEVEL,\r\n"
				+ "    PLANT__CODE,\r\n" + "    FLAG_PICK,\r\n" + "    FLAG_READY,\r\n" + "    CALL_NAME\r\n"
				+ "  )\r\n" + "  VALUES\r\n" + "  (\r\n" + "    ?,\r\n" + "    sysdate,\r\n" + "    ?,\r\n"
				+ "    ?,\r\n" + "    ?,\r\n" + "    ?,\r\n" + "    ?,\r\n" + "    ?,\r\n" + "    'S4'\r\n" + "  )";

		String insertProcedureCall = "DECLARE\r\n" + "  maxItemNr         VARCHAR(4);\r\n"
				+ "  parentSoSchdId    VARCHAR(40);\r\n" + "  so_code           CHAR(10);\r\n"
				+ "  so_id             VARCHAR(40);\r\n" + "  fgCode            CHAR(16);\r\n"
				+ "  commQty           NUMBER(11);\r\n" + "  lcdFlag           CHAR(1);\r\n"
				+ "  lcDate            VARCHAR(40);\r\n" + "  fcDate            VARCHAR(40);\r\n"
				+ "  lastUpdatingUser  VARCHAR(40);\r\n" + "  tbaFlag           CHAR(1 BYTE);\r\n"
				+ "  tbaReason         CHAR(6 BYTE);\r\n" + "  tbaDate           VARCHAR(40);\r\n"
				+ "  tbaStartDate      VARCHAR(40);\r\n" + "  shipDate          VARCHAR(40);\r\n"
				+ "  availableQty      NUMBER(11);\r\n" + "  motherItemNr      VARCHAR(4);\r\n"
				+ "  pickedQty         NUMBER(11);\r\n" + "  progInvdQty       NUMBER(11);\r\n"
				+ "  fullyInvdFlag     CHAR(1);\r\n" + "  plantSoStatusCode CHAR(2);\r\n"
				+ "  plantProgInvdQty  NUMBER(11);\r\n" + "  PlantPickedQty    NUMBER(11);\r\n"
				+ "  QtyEngagedInPick  NUMBER(11);\r\n" + "  qtyEngaedOutPick  NUMBER(11);\r\n"
				+ "  soStatusCode      CHAR(2);\r\n" + "  plantCode         CHAR(4);\r\n"
				+ "  shipModeCode      CHAR(2);\r\n schedulingType    CHAR(2);" + "BEGIN\r\n" + "  so_code           :=?;\r\n"
				+ "  parentSoSchdId    :=?;\r\n" + "  fgCode            :=?;\r\n" + "  commQty           :=?;\r\n"
				+ "  lcDate            :=?;\r\n" + "  fcDate            :=?;\r\n" + "  lastUpdatingUser  :=?;\r\n"
				+ "  lcdFlag           :=?;\r\n" + "  tbaFlag           :=?;\r\n" + "  tbaReason         :=?;\r\n"
				+ "  tbaDate           :=?;\r\n" + "  tbaStartDate      :=?;\r\n" + "  shipDate          :=?;\r\n"
				+ "  availableQty      :=?;\r\n" + "  motherItemNr      :=?;\r\n" + "  pickedQty         :=?;\r\n"
				+ "  progInvdQty       :=?;\r\n" + "  fullyInvdFlag     :=?;\r\n" + "  plantSoStatusCode :=?;\r\n"
				+ "  plantProgInvdQty  :=?;\r\n" + "  PlantPickedQty    :=?;\r\n" + "  QtyEngagedInPick  :=?;\r\n"
				+ "  qtyEngaedOutPick  :=?;\r\n" + "  soStatusCode      :=?;\r\n" + "  plantCode         :=?;\r\n"
				+ "  shipModeCode      :=?;\r\n  schedulingType    :=?;" + "  SELECT LPAD(TO_NUMBER(MAX(ITEM_NR)+1), 4, '0')\r\n"
				+ "  INTO maxItemNr\r\n" + "  FROM SO_SCHEDULED\r\n" + "  WHERE SO__CODE=so_code;\r\n" + "  INSERT\r\n"
				+ "  INTO SO_SCHEDULED\r\n" + "    (\r\n" + "      ID,\r\n" + "      SO__CODE,\r\n"
				+ "      PENDING_FLAG,\r\n" + "      UPDATING_ORG_CODE,\r\n" + "      SO_SUB_ITEM__ID,\r\n"
				+ "      FINISHED_GOOD__CODE,\r\n" + "      LAST_COMMITTED_DATE_FLAG,\r\n"
				+ "      CAUSE_ORIGIN_CANC__CODE,\r\n" + "      HOLD_CONDITION__CODE,\r\n"
				+ "      SCHEDULING_TYPE__CODE,\r\n" + "      SO_STATUS__CODE,\r\n" + "      TBA_REASON__CODE,\r\n"
				+ "      ORDER_CHANGE_TYPE_CODE,\r\n" + "      INVOICE_ROUTING__CODE,\r\n" + "      PLANT__CODE,\r\n"
				+ "      STORE__CODE,\r\n" + "      FIRST_COMMITTED_DATE,\r\n" + "      LAST_COMMITTED_DATE,\r\n"
				+ "      COMMITTED_QTY,\r\n" + "      AVAILABLE_QTY,\r\n" + "      EXPIRATION_DATE,\r\n"
				+ "      SHIP_DATE,\r\n" + "      STATUS_DATE,\r\n" + "      TBA_DATE,\r\n" + "      TBA_FLAG,\r\n"
				+ "      PICKED_QTY,\r\n" + "      PROGRESSIVE_INVD_QTY,\r\n" + "      RESERVED_QTY,\r\n"
				+ "      NOTE,\r\n" + "      ITEM_NR,\r\n" + "      OLD_TRANSACTION_NR,\r\n" + "      MOTHER_ITEM,\r\n"
				+ "      FULLY_INVD_FLAG,\r\n" + "      INVOICE_DATE,\r\n" + "      BLOCKED_QTY,\r\n"
				+ "      CONFIRM_RD_FLAG,\r\n" + "      CONFIRM_RD_DATE,\r\n" + "      TRANSHIPMENT_POINT1,\r\n"
				+ "      TRANSHIPMENT_POINT2,\r\n" + "      INSERTION_DATE,\r\n" + "      LAST_UPDATING_DATE,\r\n"
				+ "      LAST_UPDATING_USER,\r\n" + "      TRANSACTION_NR,\r\n" + "      RECORD_NR,\r\n"
				+ "      PARTIAL_SHIPT__CODE,\r\n" + "      KANBAN_FLAG,\r\n" + "      CARRIER__CODE,\r\n"
				+ "      EARLY_WARNING_DATE,\r\n" + "      EWD_TYPE__CODE,\r\n" + "      EWD_FLAG,\r\n"
				+ "      LETTER_OF_CREDIT_FLAG,\r\n" + "      TBA_START_DATE,\r\n" + "      EW_SOURCE__CODE,\r\n"
				+ "      BLOCKED_DATE,\r\n" + "      PLANT_SO_STATUS__CODE,\r\n" + "      PLANT_PROG_INVD_QTY,\r\n"
				+ "      PLANT_PICKED_QTY,\r\n" + "      QTY_ENGAGED_IN_PICK,\r\n" + "      QTY_ENGAGED_OUT_PICK,\r\n"
				+ "      PLANT_INVOICE_DATE,\r\n" + "      CONF_STATUS__CODE,\r\n" + "      RECV_PLANT__CODE,\r\n"
				+ "      RECV_STORE__CODE,\r\n" + "      LAST_LOGISTIC_UPDATING_DATE,\r\n"
				+ "      LAST_UPDATING_DATE_PLANT,\r\n" + "      FLAG_BE_CAP,\r\n" + "      EDI_LAST_EDD,\r\n"
				+ "      OA_LAST_EDD,\r\n" + "      WH_RESERVED_QTY,\r\n" + "      DD_FLAG,\r\n"
				+ "      DD_LAST_UPDATING_USER,\r\n" + "      DD_LAST_UPDATING_DATE,\r\n" + "      BATCH_KEY,\r\n"
				+ "      BATCH_VALUE,\r\n" + "      SHIP_MODE__CODE,\r\n" + "      SO_BLOCKED_REASON__CODE,\r\n"
				+ "      EXTERNAL_ITEM_ID,\r\n" + "      GTS_ORDBLK_STATUS__CODE,\r\n"
				+ "      EXCP_FULL_LOT_PICKING,\r\n" + "      EXCP_DELIVERY_TOLERANCE_LEVEL,\r\n"
				+ "      EXCP_NOT_OLDER_WEEK,\r\n" + "      MANUAL_PICK_PRIORITY,\r\n" + "      JIT_WINDOW,\r\n"
				+ "      EXT_TRANSIT_TIME\r\n" + "    )\r\n" + "  SELECT SO_SUB_ITEM__ID\r\n" + "    || '.'\r\n"
				+ "    || TO_NUMBER(maxItemNr),\r\n" + "    SO__CODE,\r\n" + "    PENDING_FLAG,\r\n"
				+ "    UPDATING_ORG_CODE,\r\n" + "    SO_SUB_ITEM__ID,\r\n" + "    fgCode,\r\n" + "    lcdFlag,\r\n"
				+ "    CAUSE_ORIGIN_CANC__CODE,\r\n" + "    HOLD_CONDITION__CODE,\r\n"
				+ "    schedulingType,\r\n" + "    soStatusCode,\r\n" + "    tbaReason,\r\n"
				+ "    ORDER_CHANGE_TYPE_CODE,\r\n" + "    INVOICE_ROUTING__CODE,\r\n" + "    plantCode,\r\n"
				+ "    (select NVL(MAX(STORE_CODE), '2001') from store_info where plant__code = plantCode and flag_scm = 'D' and ACTIVITY_STATUS__CODE = '30'),\r\n"
				+ "    TO_DATE ( fcDate, 'YYYY-MM-DD HH24:MI:SS' ) ,\r\n"
				+ "    TO_DATE ( lcDate, 'YYYY-MM-DD HH24:MI:SS' ) ,\r\n" + "    commQty,\r\n" + "    availableQty,\r\n"
				+ "    EXPIRATION_DATE,\r\n" + "    TO_DATE ( shipDate, 'YYYY-MM-DD HH24:MI:SS' ) ,\r\n"
				+ "    sysdate,\r\n" + "    TO_DATE ( tbaDate, 'YYYY-MM-DD HH24:MI:SS' ) ,\r\n" + "    tbaFlag,\r\n"
				+ "    pickedQty,\r\n" + "    progInvdQty,\r\n" + "    RESERVED_QTY,\r\n" + "    NOTE,\r\n"
				+ "    maxitemNr,\r\n" + "    OLD_TRANSACTION_NR,\r\n" + "    motherItemNr,\r\n"
				+ "    fullyInvdFlag,\r\n" + "    INVOICE_DATE,\r\n" + "    BLOCKED_QTY,\r\n"
				+ "    CONFIRM_RD_FLAG,\r\n" + "    CONFIRM_RD_DATE,\r\n" + "    TRANSHIPMENT_POINT1,\r\n"
				+ "    TRANSHIPMENT_POINT2,\r\n" + "    SYSDATE,\r\n" + "    SYSDATE,\r\n" + "    lastUpdatingUser,\r\n"
				+ "    TRANSACTION_NR,\r\n" + "    RECORD_NR,\r\n" + "    PARTIAL_SHIPT__CODE,\r\n"
				+ "    KANBAN_FLAG,\r\n" + "    CARRIER__CODE,\r\n"
				+ "    TO_DATE ( lcDate, 'YYYY-MM-DD HH24:MI:SS' ) ,\r\n" + "    EWD_TYPE__CODE,\r\n"
				+ "    EWD_FLAG,\r\n" + "    LETTER_OF_CREDIT_FLAG,\r\n"
				+ "    DECODE(NVL(trim(tbaStartDate),'X'),'X',tbaStartDate,sysdate),\r\n" + "    EW_SOURCE__CODE,\r\n"
				+ "    BLOCKED_DATE,\r\n" + "    plantSoStatusCode,\r\n" + "    plantProgInvdQty,\r\n"
				+ "    PlantPickedQty,\r\n" + "    QtyEngagedInPick,\r\n" + "    qtyEngaedOutPick,\r\n"
				+ "    PLANT_INVOICE_DATE,\r\n" + "    CONF_STATUS__CODE,\r\n" + "    RECV_PLANT__CODE,\r\n"
				+ "    RECV_STORE__CODE,\r\n" + "    LAST_LOGISTIC_UPDATING_DATE,\r\n"
				+ "    LAST_UPDATING_DATE_PLANT,\r\n" + "    FLAG_BE_CAP,\r\n" + "    EDI_LAST_EDD,\r\n"
				+ "    OA_LAST_EDD,\r\n" + "    WH_RESERVED_QTY,\r\n" + "    DD_FLAG,\r\n"
				+ "    DD_LAST_UPDATING_USER,\r\n" + "    DD_LAST_UPDATING_DATE,\r\n" + "    BATCH_KEY,\r\n"
				+ "    BATCH_VALUE,\r\n" + "    shipModeCode,\r\n" + "    SO_BLOCKED_REASON__CODE,\r\n"
				+ "    EXTERNAL_ITEM_ID,\r\n" + "    GTS_ORDBLK_STATUS__CODE,\r\n" + "    EXCP_FULL_LOT_PICKING,\r\n"
				+ "    EXCP_DELIVERY_TOLERANCE_LEVEL,\r\n" + "    EXCP_NOT_OLDER_WEEK,\r\n"
				+ "    MANUAL_PICK_PRIORITY,\r\n" + "    JIT_WINDOW,\r\n" + "    EXT_TRANSIT_TIME\r\n"
				+ "  FROM SO_SCHEDULED\r\n" + "  WHERE ID=parentSoSchdId;\r\n" + "  INSERT\r\n"
				+ "  INTO TXN_OLD_SYSTEM\r\n" + "    (\r\n" + "      ID,\r\n" + "      INSERTION_DATE,\r\n"
				+ "      FLAG_WHERE,\r\n" + "      FLAG_ACTION,\r\n" + "      FLAG_LEVEL,\r\n"
				+ "      PLANT__CODE,\r\n" + "      FLAG_PICK,\r\n" + "      FLAG_READY,\r\n" + "      CALL_NAME\r\n"
				+ "    )\r\n" + "    VALUES\r\n" + "    (\r\n" + "      (SELECT SO_SUB_ITEM__ID\r\n"
				+ "          || '.'\r\n" + "          || TO_NUMBER(maxItemNr)\r\n" + "        FROM SO_SCHEDULED\r\n"
				+ "        WHERE ID=parentSoSchdId\r\n" + "      )\r\n" + "      ,\r\n" + "      sysdate,\r\n"
				+ "      'C',\r\n" + "      'I',\r\n" + "      'I',\r\n" + "      ' ',\r\n" + "      ' ',\r\n"
				+ "      'NO',\r\n" + "      'S4'\r\n" + "    );\r\n" + "END;";

		String historyProcedureCall = "{call ro_ss_hist_insert(?,?)}";
		String EdiRsponseProcedureCall = "{call RO_INSERT_EDI_RSP (?,?,?,?,TO_DATE(?,'YYYY-MM-DD HH24:MI:SS'),?,?,?)}";
		Connection connection = null;
		List<BacklogSchedule> updateBacklogList = new ArrayList<>();
		PreparedStatement updateStatement = null;
		PreparedStatement deleteConsumedQtyStatement=null;
		PreparedStatement insertTxnOldStatement = null;
		PreparedStatement updateInsertTxnOldStatement = null;
		PreparedStatement deleteStatement = null;
		PreparedStatement e2AlignedSkipReasonStatement = null;
		PreparedStatement shipmode14SkipReasonStatement = null;
		PreparedStatement setTbaReasonSkipReasonStatement = null;
		CallableStatement callableStmt = null;
		CallableStatement updateCallableStmt = null;
		CallableStatement insertCallableStmt = null;
		CallableStatement ediResponseCallableStmt = null;
		try {
			connection = hikariDataSource.getConnection();
			connection.setAutoCommit(false);
			updateStatement = connection.prepareStatement(updateSql);
			insertTxnOldStatement = connection.prepareStatement(insertIntoTxnOldSystem);
			updateInsertTxnOldStatement = connection.prepareStatement(insertIntoTxnOldSystem);
			// logger.info("UpdateSql:"+updateSql);
			deleteStatement = connection.prepareStatement(deleteSql);
			deleteConsumedQtyStatement=connection.prepareStatement(deleteConsumedQtySql);
			e2AlignedSkipReasonStatement = connection.prepareStatement(setSkipReasonSql);
			shipmode14SkipReasonStatement = connection.prepareStatement(setSkipReasonSql);
			setTbaReasonSkipReasonStatement = connection.prepareStatement(setSkipReasonSql);
			// logger.info("deleteSql:"+deleteSql);
			ediResponseCallableStmt = connection.prepareCall(EdiRsponseProcedureCall);
			callableStmt = connection.prepareCall(historyProcedureCall);
			updateCallableStmt = connection.prepareCall(historyProcedureCall);
			insertCallableStmt = connection.prepareCall(insertProcedureCall);
			for (BacklogData backlogData : backlogDataList) {
				List<BacklogSchedule> backlogScheduleList = backlogData.getBacklogScheduleObj();
				for (BacklogSchedule obj : backlogScheduleList) {

					if (obj.getStatusFlagCmp().equals(OrderSchedulingConstant.NOACTION)) {
						// logger.info("OPERATION:INSERT");
						skipE2AlignedWithS4(obj, e2AlignedSkipReasonStatement);

					} else {

						if (obj.getShipModeCode() != null && obj.getShipModeCode().equals("14")
								&& obj.getStatusFlagCmp().equals("INSERT")) {
							setShipModeCodeInSkipReason(obj, shipmode14SkipReasonStatement);
						}
						if (obj.getTbaFlag() != null && obj.getTbaFlag().equals(OrderSchedulingConstant.Y)) {
							setTbaReasonSkipReason(obj, setTbaReasonSkipReasonStatement);
						}

						if (obj.getStatusFlagCmp().equals("UPDATE")) {
							// logger.info("Operation:Update");
							updateBacklogList.add(obj);
							updateStatement.setString(1, obj.getCommittedQty());
							updateStatement.setString(2, obj.getPlantCode());
							updateStatement.setString(3, obj.getFinishedGoodCode());
							updateStatement.setString(4, obj.getFirstCommittedDate());
							updateStatement.setString(5, obj.getLastCommittedDate());
							updateStatement.setString(6, obj.getLastCommittedDateFlag());
							updateStatement.setString(7, obj.getShipDate());
							updateStatement.setString(8, OrderSchedulingConstant.LAST_UPDATING_USER);
							updateStatement.setString(9, obj.getTbaReasonCode());
							updateStatement.setString(10, obj.getTbaDate());
							updateStatement.setString(11, obj.getTbaFlag());
							updateStatement.setString(12, obj.getTbaStartDate());
							updateStatement.setString(13, obj.getTbaStartDate());
							updateStatement.setString(14, obj.getFullyInvdFlag());
							updateStatement.setString(15, obj.getAvailableQuantity());
							updateStatement.setString(16, obj.getPlantSoStatusCode());
							updateStatement.setString(17, obj.getSoStatusCode());
							updateStatement.setString(18, obj.getSchedulingTypeCode());
							updateStatement.setString(19, obj.getShipModeCode());
							updateStatement.setString(20, obj.getPlantCode());
							updateStatement.setString(21, obj.getSoScheduledId());
							updateStatement.setString(22, obj.getCommittedQty());
							updateStatement.setString(23, obj.getPlantCode());
							updateStatement.setString(24, obj.getFinishedGoodCode());
							updateStatement.setString(25, obj.getFirstCommittedDate());
							updateStatement.setString(26, obj.getLastCommittedDate());
							updateStatement.setString(27, obj.getLastCommittedDateFlag());
							updateStatement.setString(28, obj.getShipDate());
							updateStatement.setString(29, obj.getTbaReasonCode());
							updateStatement.setString(30, obj.getTbaDate());
							updateStatement.setString(31, obj.getTbaFlag());
							updateStatement.setString(32, obj.getTbaStartDate());
							updateStatement.setString(33, obj.getTbaStartDate());
							updateStatement.setString(34, obj.getFullyInvdFlag());
							updateStatement.setString(35, obj.getAvailableQuantity());
							updateStatement.setString(36, obj.getPlantSoStatusCode());
							updateStatement.setString(37, obj.getSoStatusCode());
							updateStatement.setString(38, obj.getSchedulingTypeCode());
							updateStatement.setString(39, obj.getShipModeCode());

							updateStatement.addBatch();
							updateHistoryProcedure(obj, updateCallableStmt);
							if(obj.getConsumedQtyTable().equalsIgnoreCase("true"))
							deleteConsumedQty(obj,deleteConsumedQtyStatement);

						} else if (obj.getStatusFlagCmp().equals("BEGIN")) {
							// logger.info("OPERATION:DELETE");

							callableStmt.setString(1, obj.getSoScheduledId());
							callableStmt.setDate(2, new java.sql.Date(new java.util.Date().getTime()));

							callableStmt.addBatch();

							deleteStatement.setString(1, OrderSchedulingConstant.LAST_UPDATING_USER);
							deleteStatement.setString(2, obj.getSoScheduledId());
							deleteStatement.addBatch();

							insertTxnOldStatement.setString(1, obj.getSoScheduledId());
							insertTxnOldStatement.setString(2, "C");
							insertTxnOldStatement.setString(3, "D");
							insertTxnOldStatement.setString(4, "I");
							insertTxnOldStatement.setString(5, " ");
							insertTxnOldStatement.setString(6, " ");
							insertTxnOldStatement.setString(7, "NO");

							insertTxnOldStatement.addBatch();
							
							if(obj.getConsumedQtyTable().equalsIgnoreCase("true"))
								deleteConsumedQty(obj,deleteConsumedQtyStatement);


						} else if (obj.getStatusFlagCmp().equals("INSERT")) {
							// logger.info("OPERATION:INSERT");

							insertCallableStmt.setString(1, backlogData.getSoSubItemId().substring(0, 10));
							insertCallableStmt.setString(2, obj.getParentSoScheduleId());
							insertCallableStmt.setString(3, obj.getFinishedGoodCode());
							insertCallableStmt.setString(4, obj.getCommittedQty());
							insertCallableStmt.setString(5, obj.getLastCommittedDate());
							insertCallableStmt.setString(6, obj.getFirstCommittedDate());
							insertCallableStmt.setString(7, OrderSchedulingConstant.LAST_UPDATING_USER);
							insertCallableStmt.setString(8, obj.getLastCommittedDateFlag());
							insertCallableStmt.setString(9, obj.getTbaFlag());
							insertCallableStmt.setString(10, obj.getTbaReasonCode());
							insertCallableStmt.setString(11, obj.getTbaDate());
							insertCallableStmt.setString(12, obj.getTbaStartDate());
							insertCallableStmt.setString(13, obj.getShipDate());
							insertCallableStmt.setString(14, obj.getAvailableQuantity());
							insertCallableStmt.setString(15, obj.getMotherItem());
							insertCallableStmt.setString(16, obj.getPickedQty());
							insertCallableStmt.setString(17, obj.getProgressiveInvdQty());
							insertCallableStmt.setString(18, obj.getFullyInvdFlag());
							insertCallableStmt.setString(19, obj.getPlantSoStatusCode());
							insertCallableStmt.setString(20, obj.getPlantProgInvdQty());
							insertCallableStmt.setString(21, obj.getPlantPickedQty());
							insertCallableStmt.setString(22, obj.getQtyEngagedInPick());
							insertCallableStmt.setString(23, obj.getQtyEngagedOutPick());
							insertCallableStmt.setString(24, obj.getSoStatusCode());
							insertCallableStmt.setString(25, obj.getPlantCode());
							insertCallableStmt.setString(26, obj.getShipModeCode());
							insertCallableStmt.setString(27, obj.getSchedulingTypeCode());
							insertCallableStmt.addBatch();

							settingValuesForEdiResponse(obj, ediResponseCallableStmt);
						}

					}

				}
			}
			int insertCount[] = insertCallableStmt.executeBatch();
			int historyUpdate[] = updateCallableStmt.executeBatch();
			int histCount[] = callableStmt.executeBatch();
			int updateCount[] = updateStatement.executeBatch();
			int deleteCount[] = deleteStatement.executeBatch();

			int deleteUpdateTxnCount[] = insertTxnOldStatement.executeBatch();

			

			int tbaReasonSkip[] = setTbaReasonSkipReasonStatement.executeBatch();
			int deleteConsumedQtyRowCount[]=deleteConsumedQtyStatement.executeBatch();

			for (int i = 0; i < updateCount.length; i++) {
				logger.debug("updateBacklog " + updateBacklogList.get(i) + " value " + updateCount[i]);
				BacklogSchedule obj = updateBacklogList.get(i);
				if (updateCount[i] > 0) {

					if (obj.getShipModeCode() != null && obj.getShipModeCode().equals("14")) {
						setShipModeCodeInSkipReason(obj, shipmode14SkipReasonStatement);
					}
					// updateHistoryProcedure(obj, updateCallableStmt);
					updateInsertTxnOld(obj, updateInsertTxnOldStatement);

					if ((!obj.getPlantSoStatusCode().equals("90")) && (!obj.getFullyInvdFlag().equals("Y")))
						settingValuesForEdiResponse(obj, ediResponseCallableStmt);
				} else {
					skipE2AlignedWithS4(obj, e2AlignedSkipReasonStatement);
				}
			}
			int insertEdiRepsonse[] = ediResponseCallableStmt.executeBatch();
			// int historyUpdate[] = updateCallableStmt.executeBatch();
			int UpdateInTxt_OLD[] = updateInsertTxnOldStatement.executeBatch();
			int e2AlignedWithS4[] = e2AlignedSkipReasonStatement.executeBatch();
			int shipMode14Count[] = shipmode14SkipReasonStatement.executeBatch();
			
			

			connection.commit();
			connection.setAutoCommit(true);

			logger.info("updateBackLogDataForSubItemIdList : " + subItemIdList + " is executed updated rows count :  "
					+ updateCount.length + " inserted rows count :" + insertCount.length
					+ " historized SS rows count for delete: " + (histCount.length + historyUpdate.length)
					+ " deleted rows count : " + deleteCount.length + " delete update Txn_old count : "
					+ deleteUpdateTxnCount.length + " updatation in Old_txn : " + UpdateInTxt_OLD.length
					+ " history SS due to update : " + historyUpdate.length + " Inserted rows count in EDI response: "
					+ insertEdiRepsonse.length + " e2AlignedWithS4Count: " + e2AlignedWithS4.length
					+ " ShipMode 14 count: " + shipMode14Count.length + " tbaReasonSkip count " + tbaReasonSkip.length +" deleteConsumedQtyRow Count:"+deleteConsumedQtyRowCount.length);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in Method:updateBackLogData for SubItemIdList : " + subItemIdList, e);
			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(updateStatement);
			DbUtils.closeQuietly(deleteStatement);
			DbUtils.closeQuietly(insertCallableStmt);
			DbUtils.closeQuietly(insertTxnOldStatement);
			DbUtils.closeQuietly(callableStmt);
			DbUtils.closeQuietly(updateCallableStmt);
			DbUtils.closeQuietly(updateInsertTxnOldStatement);

			DbUtils.closeQuietly(e2AlignedSkipReasonStatement);
			DbUtils.closeQuietly(shipmode14SkipReasonStatement);
			DbUtils.closeQuietly(setTbaReasonSkipReasonStatement);

			DbUtils.closeQuietly(ediResponseCallableStmt);

		}

		return true;
	}

	private void deleteConsumedQty(BacklogSchedule obj, PreparedStatement deleteConsumedQtyStatement) throws Exception {
		try {

			deleteConsumedQtyStatement.setString(1, obj.getSoScheduledId());
			
			deleteConsumedQtyStatement.addBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in deleteConsumedQty" + e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in deleteConsumedQty" + e);
			throw e;
		}
		
	}

	private void setTbaReasonSkipReason(BacklogSchedule obj, PreparedStatement shipmode14SkipReasonStatement)
			throws Exception {
		try {

			shipmode14SkipReasonStatement.setString(1, OrderSchedulingConstant.SUCCESS);
			shipmode14SkipReasonStatement.setString(2, OrderSchedulingConstant.QTY_IN_TBA);
			shipmode14SkipReasonStatement.setString(3, obj.getS4OrderDataId());
			shipmode14SkipReasonStatement.addBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in setTbaReasonSkipReason" + e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in setTbaReasonSkipReason" + e);
			throw e;
		}

	}

	private void setShipModeCodeInSkipReason(BacklogSchedule obj, PreparedStatement shipmode14SkipReasonStatement)
			throws Exception {
		try {

			shipmode14SkipReasonStatement.setString(1, OrderSchedulingConstant.SUCCESS);
			shipmode14SkipReasonStatement.setString(2, OrderSchedulingConstant.SHIPMODE_14);
			shipmode14SkipReasonStatement.setString(3, obj.getS4OrderDataId());
			shipmode14SkipReasonStatement.addBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in setShipModeCodeInSkipReason" + e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in setShipModeCodeInSkipReason" + e);
			throw e;
		}

	}

	private void skipE2AlignedWithS4(BacklogSchedule obj, PreparedStatement setSkipReasonUpdateStatement)
			throws Exception {

		try {

			setSkipReasonUpdateStatement.setString(1, OrderSchedulingConstant.SKIP);
			setSkipReasonUpdateStatement.setString(2, OrderSchedulingConstant.E2_ALIGNED_WITH_S4);
			setSkipReasonUpdateStatement.setString(3, obj.getS4OrderDataId());
			setSkipReasonUpdateStatement.addBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in skipE2AlignedWithS4" + e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in skipE2AlignedWithS4" + e);
			throw e;
		}

	}

	private void updateInsertTxnOld(BacklogSchedule obj, PreparedStatement updateInsertTxnOldStatement)
			throws Exception {
		try {

			updateInsertTxnOldStatement.setString(1, obj.getSoScheduledId());
			updateInsertTxnOldStatement.setString(2, "C");
			updateInsertTxnOldStatement.setString(3, "V");
			updateInsertTxnOldStatement.setString(4, "I");
			updateInsertTxnOldStatement.setString(5, " ");
			updateInsertTxnOldStatement.setString(6, " ");
			updateInsertTxnOldStatement.setString(7, "NO");

			updateInsertTxnOldStatement.addBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in updateInsertTxnOld" + e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in updateInsertTxnOld" + e);
			throw e;
		}

	}

	private void updateHistoryProcedure(BacklogSchedule obj, CallableStatement updateCallableStmt) throws Exception {
		// TODO Auto-generated method stub

		try {

			updateCallableStmt.setString(1, obj.getSoScheduledId());
			updateCallableStmt.setDate(2, new java.sql.Date(new java.util.Date().getTime()));

			updateCallableStmt.addBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in updateHistoryProcedure" + e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in updateHistoryProcedure" + e);
			throw e;
		}

	}

	private void settingValuesForEdiResponse(BacklogSchedule obj, CallableStatement ediResponseCallableStmt)
			throws SQLException {
		// TODO Auto-generated method stub

		try {

			ediResponseCallableStmt.setString(1, obj.getSoSubItemId().split("\\.")[0]);
			ediResponseCallableStmt.setString(2, obj.getItemNr());
			ediResponseCallableStmt.setString(3, "S4");
			ediResponseCallableStmt.setString(4, obj.getChangeType());
			ediResponseCallableStmt.setString(5, obj.getLastCommittedDate());
			ediResponseCallableStmt.setString(6, obj.getMsgType());
			ediResponseCallableStmt.setString(7, " ");
			ediResponseCallableStmt.setString(8, OrderSchedulingConstant.LAST_UPDATING_USER);

			ediResponseCallableStmt.addBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in settingValuesForEdiResponse" + e);
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in settingValuesForEdiResponse" + e);
			throw e;
		}

	}

	
	public BacklogData getBacklogData(String subItemIdList) throws Exception {

		Connection connection = null;
		BacklogData backLogDataRet = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {

			String sql = "SELECT SO.CUSTOMER_SHIP_TO__ID , \r\n"
					+ "					  SO.SALES_ORDER_TYPE__CODE , \r\n"
					+ "					  SO.SO_SUB_TYPE__CODE , \r\n"
					+ "					  SI.COMMERCIAL_PRODUCT__CODE , \r\n"
					+ "					  NVL(SI.CUSTOMER_PART_NR__CODE,NVL(SI.CUSTOMER_PART_NR_DESCR,NULL)) AS CUSTOMER_PART_NR__CODE , \r\n"
					+ "					  SSI.ID , \r\n" + "					  SSI.SO_PENDING_CAUSE__CODE , \r\n"
					+ "					  TO_CHAR(SSI.REQUIRED_DATE , 'YYYY-MM-DD HH24:MI:SS') AS REQUIRED_DATE , \r\n"
					+ "					  SSI.REQUIRED_QTY , \r\n"
					+ "					  SSI.ITEM_SPLITTING_FLAG , \r\n"
					+ "					  SSI.SO_STATUS__CODE , \r\n"
					+ "					  PPF.planning_family__code , \r\n"
					+ "					  PMF.PRODUCT_MATURITY__CODE ,\r\n" + "            BB.QTY\r\n"
					+ "					FROM SO, \r\n" + "					  so_item SI, \r\n"
					+ "					  so_sub_item SSI, \r\n" + "					  plm_material pmf,\r\n"
					+ "            base_bulk bb,\r\n" + "					  PRODUCT_LINE PL, \r\n"
					+ "					  PRODUCT_PNL_FAMILY PPF \r\n"
					+ "					WHERE si.so__code               = so.code \r\n"
					+ "					AND ssi.so_item__id             = si.id \r\n"
					+ "					AND si.commercial_product__Code = pmf.code \r\n"
					+ "					AND pmf.type                    = 'CP' \r\n"
					+ "					AND SSI.ID                      =? \r\n"
					+ "					AND PPF.code                    = PL.product_pnl_family__code \r\n"
					+ "					AND PL.code                     = pmf.product_line__code \r\n"
					+ "          AND pmf.code                    = SI.COMMERCIAL_PRODUCT__CODE\r\n"
					+ "          AND pmf.type                    = 'CP'\r\n"
					+ "          and pmf.minimum_sellable_qty__code = bb.code\r\n"
					+ "          AND BB.activity_status__code    =30";

			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, subItemIdList);
			rs = statement.executeQuery();
			backLogDataRet = new BacklogData();
			while (rs.next()) {

				if (rs.getString("CUSTOMER_SHIP_TO__ID") != null)
					backLogDataRet.setCustomerShipToId(rs.getString("CUSTOMER_SHIP_TO__ID").toString());

				if (rs.getString("SALES_ORDER_TYPE__CODE") != null)
					backLogDataRet.setSalesOrderTypeCode(rs.getString("SALES_ORDER_TYPE__CODE").toString());

				if (rs.getString("SO_SUB_TYPE__CODE") != null)
					backLogDataRet.setSoSubTypeCode(rs.getString("SO_SUB_TYPE__CODE").toString());

				if (rs.getString("COMMERCIAL_PRODUCT__CODE") != null)
					backLogDataRet.setCommercialProductCode(rs.getString("COMMERCIAL_PRODUCT__CODE").toString());

				if (rs.getString("CUSTOMER_PART_NR__CODE") != null)
					backLogDataRet.setCustomerPartNrCode(rs.getString("CUSTOMER_PART_NR__CODE").toString());

				if (rs.getString("PLANNING_FAMILY__CODE") != null)
					backLogDataRet.setPlanningFamilyCode(rs.getString("PLANNING_FAMILY__CODE").toString());

				if (rs.getString("ID") != null)
					backLogDataRet.setSoSubItemId(rs.getString("ID").toString());

				if (rs.getString("SO_PENDING_CAUSE__CODE") != null)
					backLogDataRet.setSoPendingCauseCode(rs.getString("SO_PENDING_CAUSE__CODE").toString());

				if (rs.getString("REQUIRED_DATE") != null)
					backLogDataRet.setRequiredDate(rs.getString("REQUIRED_DATE").toString());// REQUIRED_DATE

				if (rs.getString("REQUIRED_QTY") != null)
					backLogDataRet.setRequiredQuantity(rs.getString("REQUIRED_QTY").toString());

				if (rs.getString("ITEM_SPLITTING_FLAG") != null)
					backLogDataRet.setItemSplittingFlag(rs.getString("ITEM_SPLITTING_FLAG").toString());

				if (rs.getString("SO_STATUS__CODE") != null)
					backLogDataRet.setSoStatusCode(rs.getString("SO_STATUS__CODE").toString());

				if (rs.getString("QTY") != null)
					backLogDataRet.setMinSellableQty(rs.getString("QTY").toString());
				logger.info("BackLog Data " + backLogDataRet);
				return backLogDataRet;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in Method:getBacklogData:" + e);
			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return backLogDataRet;

	}
	
	public void sample() {
		
		String sql ="select * from \r\n" + 
				"(\r\n" + 
				"  SELECT DISTINCT type.DESCR ALLOCATION_TYPE_DESCR,\r\n" + 
				"   FCG.ID,\r\n" + 
				"  FCG.SO_ALLOCATION_EXERCISE__ID,\r\n" + 
				"  FCG.SO_CPA_ALLOCATION__ID,\r\n" + 
				"  DECODE(FCG.SO_CPA_ALLOCATION__ID,NULL,'',c.DESCR ) CPA_DESCR,\r\n" + 
				"  DECODE(FCG.SO_CPA_ALLOCATION__ID,NULL,'',CPA.REGION__CODE ) REGION__CODE,\r\n" + 
				"  FCG.COMMERCIAL_PRODUCT__CODE,\r\n" + 
				"  FCG.DIRECT_CUSTOMER_GROUP__CODE,\r\n" + 
				"  FCG.FINAL_CUSTOMER_GROUP__CODE,\r\n" + 
				"  NVL((SELECT DESCR FROM customer_group cg WHERE cg.CODE = FCG.FINAL_CUSTOMER_GROUP__CODE AND (cg.DESCR LIKE ''\r\n" + 
				"  OR '' IS NULL) ) ,FCG.FINAL_CUSTOMER_GROUP__CODE) FINAL_CUSTOMER_GROUP_NAME,\r\n" + 
				"  FCG.FINAL_CUSTOMER_SHIP_TO,\r\n" + 
				"  FCG.QUARTER_YEAR,\r\n" + 
				"  FCG.GOOD_X_DIE,\r\n" + 
				"  TO_CHAR(FCG.START_DATE, 'DD-MON-YYYY HH24:MI:SS') START_DATE ,\r\n" + 
				"  TO_CHAR(FCG.END_DATE, 'DD-MON-YYYY HH24:MI:SS') END_DATE ,\r\n" + 
				"  FCG.ALLOCATION_STATUS__CODE,\r\n" + 
				"  FCG.ALLOCATED_QUANTITY,\r\n" + 
				"  NVL((SELECT bb.qty FROM BASE_BULK BB,PLM_MATERIAL PM WHERE BB.CODE = PM.BULK__CODE AND PM.CODE = FCG.COMMERCIAL_PRODUCT__CODE ) ,0) Bulk_QUANTITY,\r\n" + 
				"  TO_CHAR(FCG.INSERTION_DATE, 'DD-MON-YYYY HH24:MI:SS') INSERTION_DATE,\r\n" + 
				"  FCG.INSERTING_USER,\r\n" + 
				"  TO_CHAR(FCG.LAST_UPDATING_DATE, 'DD-MON-YYYY HH24:MI:SS') LAST_UPDATING_DATE,\r\n" + 
				"  FCG.LAST_UPDATING_USER,\r\n" + 
				"  FCG.TIME_HORIZON,\r\n" + 
				"  NVL(US.LDAP_UID,FCG.INSERTING_USER) INSERTION_USER,\r\n" + 
				"  NVL(US1.LDAP_UID,FCG.LAST_UPDATING_USER) LAST_UPDATION_USER,\r\n" + 
				"  NVL( DECODE(FCG.SO_CPA_ALLOCATION__ID,NULL,'',c.DESCR ),FCG.FINAL_CUSTOMER_GROUP__CODE) CUSTOMER_GROUP_CODE_DESCR,\r\n" + 
				"  NVL((SELECT SHORT_NAME FROM CUSTOMER_SHIP_TO cst WHERE cst.ID = FCG.FINAL_CUSTOMER_SHIP_TO) ,FCG.FINAL_CUSTOMER_SHIP_TO) FINAL_CUSTOMER_SHIP_TO_NAME,\r\n" + 
				"  FCG.CONTRACT_REFERENCE\r\n" + 
				"FROM SO_CP_X_FCG_ALLOCATION FCG ,\r\n" + 
				"  SW_USER US,\r\n" + 
				"  SW_USER US1,\r\n" + 
				"  SO_ALLOCATION_EXCERCISE ex ,\r\n" + 
				"  SO_CPA_ALLOCATION CPA ,\r\n" + 
				"  CAPACITY_PLANNING_AGG c ,\r\n" + 
				"  SO_ALLOCATION_TYPE type\r\n" + 
				"WHERE ex.ALLOCATION_STATUS__CODE   =30\r\n" + 
				"AND c.ACTIVITY_STATUS__CODE        = '30'\r\n" + 
				"AND FCG.INSERTING_USER             =US.ID\r\n" + 
				"AND FCG.LAST_UPDATING_USER         =US1.ID\r\n" + 
				"AND ex.Id                          =FCG.SO_ALLOCATION_EXERCISE__ID\r\n" + 
				"AND (cpa.ID                        =FCG.SO_CPA_ALLOCATION__ID\r\n" + 
				"OR FCG.SO_CPA_ALLOCATION__ID      IS NULL)\r\n" + 
				"AND ( c.CODE                       =CPA.CPA__CODE\r\n" + 
				"OR CPA.CPA__CODE                  IS NULL)\r\n" + 
				"AND type.CODE                      =ex.SO_ALLOCATION_TYPE__CODE\r\n" + 
				"AND ex.Id                          ='6'\r\n" + 
				"AND (c.DESCR LIKE ''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (CPA.REGION__CODE               =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (FCG.COMMERCIAL_PRODUCT__CODE   =RPAD('', 16)\r\n" + 
				"OR RPAD('', 16)                     IS NULL)\r\n" + 
				"AND (FCG.FINAL_CUSTOMER_GROUP__CODE =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (FCG.TIME_HORIZON               =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (TRUNC(FCG.START_DATE)          =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (FCG.ALLOCATION_STATUS__CODE    =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (TRUNC(FCG.END_DATE)            =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (TRUNC(FCG.INSERTION_DATE)      =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"AND (FCG.FINAL_CUSTOMER_SHIP_TO =''\r\n" + 
				"OR ''                               IS NULL)\r\n" + 
				"ORDER BY FCG.ID DESC\r\n" + 
				")\r\n" + 
				"where ROWNUM BETWEEN 1 AND 1000";
		 List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		// Print the result
	        for (Map<String, Object> row : rows) {
	            System.out.println(row.get("ID").toString());
	        }
	}

	public List<BacklogSchedule> getBacklogScheduleForSubItemIdList(List<String> subItemIdList) throws Exception {
		logger.debug("Inside getBacklogSchedule Method for SubItemId : " + subItemIdList);
		List<BacklogSchedule> backlogListObj = null;
		try {
			String sql = "SELECT FINISHED_GOOD__CODE , \r\n" + "					  ID , \r\n"
					+ "					  TO_CHAR(FIRST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS FIRST_COMMITTED_DATE , \r\n"
					+ "					  TO_CHAR(LAST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS LCD , \r\n"
					+ "					  LAST_COMMITTED_DATE_FLAG , \r\n" + "					  COMMITTED_QTY , \r\n"
					+ "					  PICKED_QTY , \r\n" + "					  PROGRESSIVE_INVD_QTY , \r\n"
					+ "					  SCHEDULING_TYPE__CODE , \r\n" + "					  SO_STATUS__CODE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n" + "					  FULLY_INVD_FLAG , \r\n"
					+ "					  SHIP_MODE__CODE , \r\n" + "					  PLANT__CODE , \r\n"
					+ "					  STORE__CODE , \r\n" + "					  TBA_REASON__CODE , \r\n"
					+ "					  PLANT__CODE , \r\n" + "					  STORE__CODE , \r\n"
					+ "					  TO_CHAR(SHIP_DATE,'YYYY-MM-DD HH24:MI:SS') AS SHIP_DATE , \r\n"
					+ "					  TO_CHAR(TBA_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_DATE , \r\n"
					+ "					  TBA_FLAG , \r\n"
					+ "					  TO_CHAR(EARLY_WARNING_DATE,'YYYY-MM-DD HH24:MI:SS') AS EARLY_WARNING_DATE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n"
					+ "					  PLANT_PROG_INVD_QTY , \r\n" + "					  PLANT_PICKED_QTY , \r\n"
					+ "					  QTY_ENGAGED_IN_PICK , \r\n"
					+ "					  QTY_ENGAGED_OUT_PICK, \r\n" + "					  ITEM_NR,\r\n"
					+ "					  TO_CHAR(TBA_START_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_START_DATE,\r\n"
					+ "            SO_SUB_ITEM__ID\r\n" + "		,AVAILABLE_QTY	,'false' as CONSUMED_QTY_TABLE			FROM SO_SCHEDULED S \r\n"
					+ "					WHERE S.SO_SUB_ITEM__ID      IN (%s)  \r\n"
					+ "					AND S.PLANT_SO_STATUS__CODE NOT IN (40,41,90) \r\n"
					+ "					AND S.SCHEDULING_TYPE__CODE NOT IN \r\n"
					+ "					  (SELECT s4.FILTER_VALUE \r\n" + "					  FROM s4_filter s4 \r\n"
					+ "					  WHERE s4.FILTER_NAME IN ('AATP_STOCK_TYPE','AATP_STOCK_TRANSIT_TYPE') \r\n"
					+ "					  ) \r\n" + "					UNION \r\n"
					+ "					SELECT S.FINISHED_GOOD__CODE , \r\n" + "					  ID , \r\n"
					+ "					  TO_CHAR(S.FIRST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS FIRST_COMMITTED_DATE , \r\n"
					+ "					  TO_CHAR(LAST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS LCD , \r\n"
					+ "					  LAST_COMMITTED_DATE_FLAG , \r\n" + "					  COMMITTED_QTY , \r\n"
					+ "					  PICKED_QTY , \r\n" + "					  PROGRESSIVE_INVD_QTY , \r\n"
					+ "					  SCHEDULING_TYPE__CODE , \r\n" + "					  SO_STATUS__CODE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n" + "					  FULLY_INVD_FLAG , \r\n"
					+ "					  SHIP_MODE__CODE , \r\n" + "					  S.PLANT__CODE , \r\n"
					+ "					  S.STORE__CODE , \r\n" + "					  TBA_REASON__CODE , \r\n"
					+ "					  S.PLANT__CODE , \r\n" + "					  S.STORE__CODE , \r\n"
					+ "					  TO_CHAR(SHIP_DATE,'YYYY-MM-DD HH24:MI:SS') AS SHIP_DATE , \r\n"
					+ "					  TO_CHAR(TBA_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_DATE , \r\n"
					+ "					  TBA_FLAG , \r\n"
					+ "					  TO_CHAR(EARLY_WARNING_DATE,'YYYY-MM-DD HH24:MI:SS') AS EARLY_WARNING_DATE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n"
					+ "					  PLANT_PROG_INVD_QTY , \r\n" + "					  PLANT_PICKED_QTY , \r\n"
					+ "					  QTY_ENGAGED_IN_PICK , \r\n"
					+ "					  QTY_ENGAGED_OUT_PICK, \r\n" + "					  ITEM_NR,\r\n"
					+ "					  TO_CHAR(TBA_START_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_START_DATE,\r\n"
					+ "            SO_SUB_ITEM__ID\r\n" + "			,AVAILABLE_QTY	,'true' as CONSUMED_QTY_TABLE		FROM SO_SCHEDULED S, \r\n"
					+ "					  consumed_qty cq \r\n"
					+ "					WHERE s.id                   = cq.SO_SCHEDULED__ID (+) \r\n"
					+ "					and  S.SO_SUB_ITEM__ID         IN (%s)  \r\n"
					+ "					AND S.PLANT_SO_STATUS__CODE NOT IN (40,41,90) \r\n"
					+ "					AND s.scheduling_type__code IN \r\n"
					+ "					  (SELECT s4.FILTER_VALUE \r\n" + "					  FROM s4_filter s4 \r\n"
					+ "					  WHERE s4.FILTER_NAME ='AATP_STOCK_TRANSIT_TYPE'\r\n"
					+ "					  ) \r\n"
					+ "					AND cq.IN_TRANSIT_FLAG (+) ='N' "
					+" 				    AND EXTRACT(YEAR FROM   DECODE(s.LAST_COMMITTED_DATE_FLAG,'Y',s.LAST_COMMITTED_DATE,s.FIRST_COMMITTED_DATE)) = '2059'"			
					+ "UNION \r\n" + 
					"										SELECT S.FINISHED_GOOD__CODE ,  					  ID , \r\n" + 
					"										  TO_CHAR(S.FIRST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS FIRST_COMMITTED_DATE , \r\n" + 
					"										  TO_CHAR(LAST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS LCD , \r\n" + 
					"										  LAST_COMMITTED_DATE_FLAG ,  					  COMMITTED_QTY , \r\n" + 
					"										  PICKED_QTY ,  					  PROGRESSIVE_INVD_QTY , \r\n" + 
					"										  SCHEDULING_TYPE__CODE ,  					  SO_STATUS__CODE , \r\n" + 
					"										  PLANT_SO_STATUS__CODE ,  					  FULLY_INVD_FLAG , \r\n" + 
					"										  SHIP_MODE__CODE ,  					  S.PLANT__CODE , \r\n" + 
					"										  S.STORE__CODE ,  					  TBA_REASON__CODE , \r\n" + 
					"										  S.PLANT__CODE ,  					  S.STORE__CODE , \r\n" + 
					"										  TO_CHAR(SHIP_DATE,'YYYY-MM-DD HH24:MI:SS') AS SHIP_DATE , \r\n" + 
					"										  TO_CHAR(TBA_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_DATE , \r\n" + 
					"										  TBA_FLAG , \r\n" + 
					"										  TO_CHAR(EARLY_WARNING_DATE,'YYYY-MM-DD HH24:MI:SS') AS EARLY_WARNING_DATE , \r\n" + 
					"										  PLANT_SO_STATUS__CODE , \r\n" + 
					"										  PLANT_PROG_INVD_QTY ,  					  PLANT_PICKED_QTY , \r\n" + 
					"										  QTY_ENGAGED_IN_PICK , \r\n" + 
					"										  QTY_ENGAGED_OUT_PICK,  					  ITEM_NR,\r\n" + 
					"										  TO_CHAR(TBA_START_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_START_DATE,\r\n" + 
					"					            SO_SUB_ITEM__ID 			,AVAILABLE_QTY	,'false' as CONSUMED_QTY_TABLE		FROM SO_SCHEDULED S\r\n" + 
					"										WHERE   S.SO_SUB_ITEM__ID         IN (%s)  \r\n" + 
					"										AND S.PLANT_SO_STATUS__CODE NOT IN (40,41,90) \r\n" + 
					"										AND s.scheduling_type__code IN \r\n" + 
					"										  (SELECT s4.FILTER_VALUE  					  FROM s4_filter s4 \r\n" + 
					"										  WHERE s4.FILTER_NAME ='AATP_STOCK_TYPE'\r\n" + 
					"										  ) \r\n" + 
					"										and   EXTRACT(YEAR FROM   DECODE(s.LAST_COMMITTED_DATE_FLAG,'Y',s.LAST_COMMITTED_DATE,S.FIRST_COMMITTED_DATE)) = '2059'"
					+ "order by FIRST_COMMITTED_DATE";
			String inSql = String.join(",", Collections.nCopies(subItemIdList.size(), "?"));
			List<String> list = new ArrayList<>(subItemIdList);
			list.addAll(subItemIdList);
			list.addAll(subItemIdList);
			List<Map<String, Object>> schedBackLogInfo = jdbcTemplate.queryForList(String.format(sql, inSql, inSql,inSql),
					list.toArray());
			// System.out.println("000000000000000000000000000000000000000000000000000000");
			backlogListObj = schedBackLogInfo.stream().map(rs -> {
				BacklogSchedule schedBackLogRet = new BacklogSchedule();
				// System.out.println("test::" + schedBackLogRet);
				if (rs.get("FINISHED_GOOD__CODE") != null) {
					schedBackLogRet.setFinishedGoodCode(rs.get("FINISHED_GOOD__CODE").toString());
				}
				if (rs.get("ID") != null) {
					schedBackLogRet.setSoScheduledId(rs.get("ID").toString());
				}
				if (rs.get("FIRST_COMMITTED_DATE") != null) {
					schedBackLogRet.setFirstCommittedDate(rs.get("FIRST_COMMITTED_DATE").toString());
				}
				if (rs.get("LCD") != null) {
					schedBackLogRet.setLastCommittedDate(rs.get("LCD").toString());// LCD
				}
				if (rs.get("LAST_COMMITTED_DATE_FLAG") != null) {
					schedBackLogRet.setLastCommittedDateFlag(rs.get("LAST_COMMITTED_DATE_FLAG").toString());
				}
				if (rs.get("COMMITTED_QTY") != null) {
					schedBackLogRet.setCommittedQty(rs.get("COMMITTED_QTY").toString());
				}
				if (rs.get("PICKED_QTY") != null) {
					schedBackLogRet.setPickedQty(rs.get("PICKED_QTY").toString());
				}
				if (rs.get("PROGRESSIVE_INVD_QTY") != null) {
					schedBackLogRet.setProgressiveInvdQty(rs.get("PROGRESSIVE_INVD_QTY").toString());
				}
				if (rs.get("SCHEDULING_TYPE__CODE") != null) {
					schedBackLogRet.setSchedulingTypeCode(rs.get("SCHEDULING_TYPE__CODE").toString());
				}
				if (rs.get("SO_STATUS__CODE") != null) {
					schedBackLogRet.setSoStatusCode(rs.get("SO_STATUS__CODE").toString());
				}
				if (rs.get("PLANT_SO_STATUS__CODE") != null) {
					schedBackLogRet.setPlantSoStatusCode(rs.get("PLANT_SO_STATUS__CODE").toString());
				}
				if (rs.get("FULLY_INVD_FLAG") != null) {
					schedBackLogRet.setFullyInvdFlag(rs.get("FULLY_INVD_FLAG").toString());
				}
				if (rs.get("SHIP_MODE__CODE") != null) {
					schedBackLogRet.setShipModeCode(rs.get("SHIP_MODE__CODE").toString().trim());
				}
				if (rs.get("PLANT__CODE") != null) {
					schedBackLogRet.setPlantCode(rs.get("PLANT__CODE").toString());
				}
				if (rs.get("STORE__CODE") != null) {
					schedBackLogRet.setStoreCode(rs.get("STORE__CODE").toString());
				}
				if (rs.get("TBA_REASON__CODE") != null) {
					schedBackLogRet.setTbaReasonCode(rs.get("TBA_REASON__CODE").toString());
				}
				if (rs.get("SHIP_DATE") != null) {
					schedBackLogRet.setShipDate(rs.get("SHIP_DATE").toString());
				}
				if (rs.get("TBA_DATE") != null) {
					schedBackLogRet.setTbaDate(rs.get("TBA_DATE").toString());
				}
				if (rs.get("TBA_FLAG") != null) {
					schedBackLogRet.setTbaFlag(rs.get("TBA_FLAG").toString());
				}
				if (rs.get("EARLY_WARNING_DATE") != null) {
					schedBackLogRet.setEarlyWarningDate(rs.get("EARLY_WARNING_DATE").toString());
				}
				if (rs.get("PLANT_PROG_INVD_QTY") != null) {
					schedBackLogRet.setPlantProgInvdQty(rs.get("PLANT_PROG_INVD_QTY").toString());
				}
				if (rs.get("PLANT_PICKED_QTY") != null) {
					schedBackLogRet.setPlantPickedQty(rs.get("PLANT_PICKED_QTY").toString());
				}
				if (rs.get("QTY_ENGAGED_IN_PICK") != null) {
					schedBackLogRet.setQtyEngagedInPick(rs.get("QTY_ENGAGED_IN_PICK").toString());
				}
				if (rs.get("QTY_ENGAGED_OUT_PICK") != null) {
					schedBackLogRet.setQtyEngagedOutPick(rs.get("QTY_ENGAGED_OUT_PICK").toString());
				}
				if (rs.get("ITEM_NR") != null) {
					schedBackLogRet.setItemNr(rs.get("ITEM_NR").toString());
				}
				if (rs.get("TBA_START_DATE") != null) {
					schedBackLogRet.setTbaStartDate(rs.get("TBA_START_DATE").toString());
				}
				if (rs.get("SO_SUB_ITEM__ID") != null) {
					schedBackLogRet.setSoSubItemId(rs.get("SO_SUB_ITEM__ID").toString());
				}
				if (rs.get("AVAILABLE_QTY") != null)
					schedBackLogRet.setAvailableQuantity(rs.get("AVAILABLE_QTY").toString());
				if (rs.get("CONSUMED_QTY_TABLE") != null)
					schedBackLogRet.setConsumedQtyTable(rs.get("CONSUMED_QTY_TABLE").toString());

				return schedBackLogRet;
			}).collect(Collectors.toList());

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getBacklogSchedule Method for SubItemId : " + subItemIdList + e.getMessage(), e);
		}
		return backlogListObj;
	}

	public List<BacklogSchedule> getBacklogScheduleForSubItemIdList(String subItemId) throws Exception {
		logger.info("Inside getBacklogSchedule Method for SubItemId : " + subItemId);
		List<BacklogSchedule> backlogListObj = new ArrayList<>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT FINISHED_GOOD__CODE , \r\n" + "					  ID , \r\n"
					+ "					  TO_CHAR(FIRST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS FIRST_COMMITTED_DATE , \r\n"
					+ "					  TO_CHAR(LAST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS LCD , \r\n"
					+ "					  LAST_COMMITTED_DATE_FLAG , \r\n" + "					  COMMITTED_QTY , \r\n"
					+ "					  PICKED_QTY , \r\n" + "					  PROGRESSIVE_INVD_QTY , \r\n"
					+ "					  SCHEDULING_TYPE__CODE , \r\n" + "					  SO_STATUS__CODE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n" + "					  FULLY_INVD_FLAG , \r\n"
					+ "					  nvl(SHIP_MODE__CODE,'') SHIP_MODE__CODE, \r\n"
					+ "					  PLANT__CODE , \r\n" + "					  STORE__CODE , \r\n"
					+ "					  TBA_REASON__CODE , \r\n" + "					  PLANT__CODE , \r\n"
					+ "					  STORE__CODE , \r\n"
					+ "					  TO_CHAR(SHIP_DATE,'YYYY-MM-DD HH24:MI:SS') AS SHIP_DATE , \r\n"
					+ "					  NVL(TO_CHAR(TBA_DATE,'YYYY-MM-DD HH24:MI:SS'),'') AS TBA_DATE , \r\n"
					+ "					  TBA_FLAG , \r\n"
					+ "					  TO_CHAR(EARLY_WARNING_DATE,'YYYY-MM-DD HH24:MI:SS') AS EARLY_WARNING_DATE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n"
					+ "					  PLANT_PROG_INVD_QTY , \r\n" + "					  PLANT_PICKED_QTY , \r\n"
					+ "					  QTY_ENGAGED_IN_PICK , \r\n"
					+ "					  QTY_ENGAGED_OUT_PICK, \r\n" + "					  ITEM_NR,\r\n"
					+ "					  NVL(TO_CHAR(TBA_START_DATE,'YYYY-MM-DD HH24:MI:SS'),'') AS TBA_START_DATE,\r\n"
					+ "            SO_SUB_ITEM__ID\r\n" + "		,AVAILABLE_QTY	,'false' as CONSUMED_QTY_TABLE			FROM SO_SCHEDULED S \r\n"
					+ "					WHERE S.SO_SUB_ITEM__ID      =?  \r\n"
					+ "					AND S.PLANT_SO_STATUS__CODE NOT IN (40,41) \r\n"
					+ "					AND S.SCHEDULING_TYPE__CODE NOT IN \r\n"
					+ "					  (SELECT s4.FILTER_VALUE \r\n" + "					  FROM s4_filter s4 \r\n"
					+ "					  WHERE s4.FILTER_NAME IN ('AATP_STOCK_TYPE','AATP_STOCK_TRANSIT_TYPE') \r\n"
					+ "					  ) \r\n" + "					UNION \r\n"
					+ "					SELECT S.FINISHED_GOOD__CODE , \r\n" + "					  ID , \r\n"
					+ "					  TO_CHAR(S.FIRST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS FIRST_COMMITTED_DATE , \r\n"
					+ "					  TO_CHAR(LAST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS LCD , \r\n"
					+ "					  LAST_COMMITTED_DATE_FLAG , \r\n" + "					  COMMITTED_QTY , \r\n"
					+ "					  PICKED_QTY , \r\n" + "					  PROGRESSIVE_INVD_QTY , \r\n"
					+ "					  SCHEDULING_TYPE__CODE , \r\n" + "					  SO_STATUS__CODE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n" + "					  FULLY_INVD_FLAG , \r\n"
					+ "					  SHIP_MODE__CODE , \r\n" + "					  S.PLANT__CODE , \r\n"
					+ "					  S.STORE__CODE , \r\n" + "					  TBA_REASON__CODE , \r\n"
					+ "					  S.PLANT__CODE , \r\n" + "					  S.STORE__CODE , \r\n"
					+ "					  TO_CHAR(SHIP_DATE,'YYYY-MM-DD HH24:MI:SS') AS SHIP_DATE , \r\n"
					+ "					  TO_CHAR(TBA_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_DATE , \r\n"
					+ "					  TBA_FLAG , \r\n"
					+ "					  TO_CHAR(EARLY_WARNING_DATE,'YYYY-MM-DD HH24:MI:SS') AS EARLY_WARNING_DATE , \r\n"
					+ "					  PLANT_SO_STATUS__CODE , \r\n"
					+ "					  PLANT_PROG_INVD_QTY , \r\n" + "					  PLANT_PICKED_QTY , \r\n"
					+ "					  QTY_ENGAGED_IN_PICK , \r\n"
					+ "					  QTY_ENGAGED_OUT_PICK, \r\n" + "					  ITEM_NR,\r\n"
					+ "					  TO_CHAR(TBA_START_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_START_DATE,\r\n"
					+ "            SO_SUB_ITEM__ID\r\n" + "			,AVAILABLE_QTY	,'true' as CONSUMED_QTY_TABLE		FROM SO_SCHEDULED S, \r\n"
					+ "					  consumed_qty cq \r\n"
					+ "					WHERE s.id                   = cq.SO_SCHEDULED__ID (+) \r\n"
					+ "					and  S.SO_SUB_ITEM__ID         IN (?)  \r\n"
					+ "					AND S.PLANT_SO_STATUS__CODE NOT IN (40,41,90) \r\n"
					+ "					AND s.scheduling_type__code IN \r\n"
					+ "					  (SELECT s4.FILTER_VALUE \r\n" + "					  FROM s4_filter s4 \r\n"
					+ "					  WHERE s4.FILTER_NAME ='AATP_STOCK_TRANSIT_TYPE'\r\n"
					+ "					  ) \r\n"
					+ "					AND cq.IN_TRANSIT_FLAG (+) ='N' "
					+"					 AND EXTRACT(YEAR FROM   DECODE(s.LAST_COMMITTED_DATE_FLAG,'Y',s.LAST_COMMITTED_DATE,s.FIRST_COMMITTED_DATE)) = '2059'"	
					+ "UNION \r\n" + 
					"										SELECT S.FINISHED_GOOD__CODE ,  					  ID , \r\n" + 
					"										  TO_CHAR(S.FIRST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS FIRST_COMMITTED_DATE , \r\n" + 
					"										  TO_CHAR(LAST_COMMITTED_DATE,'YYYY-MM-DD HH24:MI:SS') AS LCD , \r\n" + 
					"										  LAST_COMMITTED_DATE_FLAG ,  					  COMMITTED_QTY , \r\n" + 
					"										  PICKED_QTY ,  					  PROGRESSIVE_INVD_QTY , \r\n" + 
					"										  SCHEDULING_TYPE__CODE ,  					  SO_STATUS__CODE , \r\n" + 
					"										  PLANT_SO_STATUS__CODE ,  					  FULLY_INVD_FLAG , \r\n" + 
					"										  SHIP_MODE__CODE ,  					  S.PLANT__CODE , \r\n" + 
					"										  S.STORE__CODE ,  					  TBA_REASON__CODE , \r\n" + 
					"										  S.PLANT__CODE ,  					  S.STORE__CODE , \r\n" + 
					"										  TO_CHAR(SHIP_DATE,'YYYY-MM-DD HH24:MI:SS') AS SHIP_DATE , \r\n" + 
					"										  TO_CHAR(TBA_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_DATE , \r\n" + 
					"										  TBA_FLAG , \r\n" + 
					"										  TO_CHAR(EARLY_WARNING_DATE,'YYYY-MM-DD HH24:MI:SS') AS EARLY_WARNING_DATE , \r\n" + 
					"										  PLANT_SO_STATUS__CODE , \r\n" + 
					"										  PLANT_PROG_INVD_QTY ,  					  PLANT_PICKED_QTY , \r\n" + 
					"										  QTY_ENGAGED_IN_PICK , \r\n" + 
					"										  QTY_ENGAGED_OUT_PICK,  					  ITEM_NR,\r\n" + 
					"										  TO_CHAR(TBA_START_DATE,'YYYY-MM-DD HH24:MI:SS') AS TBA_START_DATE,\r\n" + 
					"					            SO_SUB_ITEM__ID 			,AVAILABLE_QTY	,'false' as CONSUMED_QTY_TABLE		FROM SO_SCHEDULED S\r\n" + 
					"										WHERE   S.SO_SUB_ITEM__ID         IN (?)  \r\n" + 
					"										AND S.PLANT_SO_STATUS__CODE NOT IN (40,41,90) \r\n" + 
					"										AND s.scheduling_type__code IN \r\n" + 
					"										  (SELECT s4.FILTER_VALUE  					  FROM s4_filter s4 \r\n" + 
					"										  WHERE s4.FILTER_NAME ='AATP_STOCK_TYPE'\r\n" + 
					"										  ) \r\n" + 
					"										and   EXTRACT(YEAR FROM   DECODE(s.LAST_COMMITTED_DATE_FLAG,'Y',s.LAST_COMMITTED_DATE,S.FIRST_COMMITTED_DATE)) = '2059'"
					+ "order by FIRST_COMMITTED_DATE";

			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, subItemId);
			statement.setString(2, subItemId);
			statement.setString(3, subItemId);
			rs = statement.executeQuery();
			while (rs.next()) {
				BacklogSchedule schedBackLogRet = new BacklogSchedule();
				// System.out.println("test::" + schedBackLogRet);

				if (rs.getString("FINISHED_GOOD__CODE") != null)
					schedBackLogRet.setFinishedGoodCode(rs.getString("FINISHED_GOOD__CODE").toString());
				if (rs.getString("ID") != null)
					schedBackLogRet.setSoScheduledId(rs.getString("ID").toString());
				if (rs.getString("FIRST_COMMITTED_DATE") != null)
					schedBackLogRet.setFirstCommittedDate(rs.getString("FIRST_COMMITTED_DATE").toString());
				if (rs.getString("LCD") != null)
					schedBackLogRet.setLastCommittedDate(rs.getString("LCD").toString());// LCD
				if (rs.getString("LAST_COMMITTED_DATE_FLAG") != null)
					schedBackLogRet.setLastCommittedDateFlag(rs.getString("LAST_COMMITTED_DATE_FLAG").toString());
				if (rs.getString("COMMITTED_QTY") != null)
					schedBackLogRet.setCommittedQty(rs.getString("COMMITTED_QTY").toString());
				if (rs.getString("PICKED_QTY") != null)
					schedBackLogRet.setPickedQty(rs.getString("PICKED_QTY").toString());
				if (rs.getString("PROGRESSIVE_INVD_QTY") != null)
					schedBackLogRet.setProgressiveInvdQty(rs.getString("PROGRESSIVE_INVD_QTY").toString());
				if (rs.getString("SCHEDULING_TYPE__CODE") != null)
					schedBackLogRet.setSchedulingTypeCode(rs.getString("SCHEDULING_TYPE__CODE").toString());
				if (rs.getString("SO_STATUS__CODE") != null)
					schedBackLogRet.setSoStatusCode(rs.getString("SO_STATUS__CODE").toString());
				if (rs.getString("PLANT_SO_STATUS__CODE") != null)
					schedBackLogRet.setPlantSoStatusCode(rs.getString("PLANT_SO_STATUS__CODE").toString());
				if (rs.getString("FULLY_INVD_FLAG") != null)
					schedBackLogRet.setFullyInvdFlag(rs.getString("FULLY_INVD_FLAG").toString());
				if (rs.getString("SHIP_MODE__CODE") != null)
					schedBackLogRet.setShipModeCode(rs.getString("SHIP_MODE__CODE").toString());
				if (rs.getString("PLANT__CODE") != null)
					schedBackLogRet.setPlantCode(rs.getString("PLANT__CODE").toString());
				if (rs.getString("STORE__CODE") != null)
					schedBackLogRet.setStoreCode(rs.getString("STORE__CODE").toString());
				if (rs.getString("TBA_REASON__CODE") != null)
					schedBackLogRet.setTbaReasonCode(rs.getString("TBA_REASON__CODE").toString());
				if (rs.getString("SHIP_DATE") != null)
					schedBackLogRet.setShipDate(rs.getString("SHIP_DATE").toString());
				if (rs.getString("TBA_DATE") != null)
					schedBackLogRet.setTbaDate(rs.getString("TBA_DATE").toString());
				if (rs.getString("TBA_FLAG") != null)
					schedBackLogRet.setTbaFlag(rs.getString("TBA_FLAG").toString());
				if (rs.getString("EARLY_WARNING_DATE") != null)
					schedBackLogRet.setEarlyWarningDate(rs.getString("EARLY_WARNING_DATE").toString());
				if (rs.getString("PLANT_PROG_INVD_QTY") != null)
					schedBackLogRet.setPlantProgInvdQty(rs.getString("PLANT_PROG_INVD_QTY").toString());
				if (rs.getString("PLANT_PICKED_QTY") != null)
					schedBackLogRet.setPlantPickedQty(rs.getString("PLANT_PICKED_QTY").toString());
				if (rs.getString("QTY_ENGAGED_IN_PICK") != null)
					schedBackLogRet.setQtyEngagedInPick(rs.getString("QTY_ENGAGED_IN_PICK").toString());
				if (rs.getString("QTY_ENGAGED_OUT_PICK") != null)
					schedBackLogRet.setQtyEngagedOutPick(rs.getString("QTY_ENGAGED_OUT_PICK").toString());
				if (rs.getString("ITEM_NR") != null)
					schedBackLogRet.setItemNr(rs.getString("ITEM_NR").toString());
				if (rs.getString("TBA_START_DATE") != null)
					schedBackLogRet.setTbaStartDate(rs.getString("TBA_START_DATE").toString());
				if (rs.getString("SO_SUB_ITEM__ID") != null)
					schedBackLogRet.setSoSubItemId(rs.getString("SO_SUB_ITEM__ID").toString());
				if (rs.getString("AVAILABLE_QTY") != null)
					schedBackLogRet.setAvailableQuantity(rs.getString("AVAILABLE_QTY").toString());
				if (rs.getString("CONSUMED_QTY_TABLE") != null)
					schedBackLogRet.setConsumedQtyTable(rs.getString("CONSUMED_QTY_TABLE").toString());
				backlogListObj.add(schedBackLogRet);
			}
			logger.info("BackLog Schedule " + backlogListObj);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getBacklogSchedule Method for SubItemId : " + subItemId + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return backlogListObj;
	}

	public int getMaxItemNrFromSO_Sched(String soCode) throws SQLException, Exception {

		int res = 0;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "SELECT MAX(ITEM_NR) max_item_nr \r\n" + "FROM SO_SCHEDULED\r\n" + "WHERE SO__CODE = ?";
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, soCode);
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				res = rs.getInt("max_item_nr");
			}
			if (rs != null)
				rs.close();
			if (statement != null)
				statement.close();
			if (connection != null)
				connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Exception in getMaxItemNrFromSO_Sched Method:" + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getMaxItemNrFromSO_Sched Method:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return res;

	}

	public void updateLockForSubItemId(String subItemId, String soSubitemLockCode, String statusCode) {
		String sql = "UPDATE SO_SUB_ITEM SET SO_PENDING_CAUSE__CODE=? WHERE ID=? AND SO_STATUS__CODE!=?";
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, soSubitemLockCode);
			statement.setString(2, subItemId);
			statement.setString(3, statusCode);
			statement.executeUpdate();
			logger.info("For SO_SUB_ITEM table SO_PENDING_CAUSE__CODE  updated as " + soSubitemLockCode
					+ " for SubItemId : " + subItemId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("For SO_SUB_ITEM table Error in updating SO_PENDING_CAUSE__CODE as " + soSubitemLockCode
					+ " for SubItemId : " + subItemId, e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("For SO_SUB_ITEM table Error in updating SO_PENDING_CAUSE__CODE as " + soSubitemLockCode
					+ " for SubItemId : " + subItemId, e);
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
		}
	}

	public void updateLockForSubItemIdList(List<String> subItemIdList, String soSubitemLockCode, String statusCode) {

		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "UPDATE SO_SUB_ITEM SET SO_PENDING_CAUSE__CODE=? WHERE SO_STATUS__CODE!=? AND ID IN (%s)";
		String placeholders = subItemIdList.stream().map(item -> "?").collect(Collectors.joining(", "));
		sql = String.format(sql, placeholders);
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, soSubitemLockCode);
			statement.setString(2, statusCode);
			for (int i = 0; i < subItemIdList.size(); i++) {
				statement.setString(3 + i, subItemIdList.get(i));
			}
			statement.executeUpdate();
			logger.debug("For SO_SUB_ITEM table SO_PENDING_CAUSE__CODE  updated as " + soSubitemLockCode
					+ " for SubItemIdList : " + subItemIdList);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("For SO_SUB_ITEM table Error in updating SO_PENDING_CAUSE__CODE as " + soSubitemLockCode
					+ " for SubItemId : " + subItemIdList, e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("For SO_SUB_ITEM table Error in updating SO_PENDING_CAUSE__CODE as " + soSubitemLockCode
					+ " for SubItemId : " + subItemIdList, e);
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
		}
	}
	public String getMaturityOfFinishedGood(String finishedGoodCode) {
		String res = "0";
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		String sql = "select PRODUCT_MATURITY__CODE from PLM_MATERIAL where TYPE='FG' and CODE=RPAD(?,16)";
		try {
			connection = hikariDataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, finishedGoodCode);
			rs = statement.executeQuery();
			while (rs.next()) {
				if(rs.getString("PRODUCT_MATURITY__CODE")!=null)
				return rs.getString("PRODUCT_MATURITY__CODE").trim();
			}
			if (rs != null)
				rs.close();
			if (statement != null)
				statement.close();
			if (connection != null)
				connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Exception in getMaxItemNrFromSO_Sched Method:" + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getMaxItemNrFromSO_Sched Method:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(connection);
			DbUtils.closeQuietly(statement);
			DbUtils.closeQuietly(rs);
		}
		return res;
	}
*/
}
