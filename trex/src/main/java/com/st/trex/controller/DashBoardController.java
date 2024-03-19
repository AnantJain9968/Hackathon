package com.st.trex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.st.trex.dto.DashboardData;
import com.st.trex.dto.InputDashBoardDto;
import com.st.trex.exception.MainException;
import com.st.trex.service.IDashboardService;


@RestController
@CrossOrigin("*")
public class DashBoardController {

	@Autowired
	IDashboardService dashBoardService;
	

	@GetMapping("/dashboardData")
	ResponseEntity<List<DashboardData>>displaySong() throws MainException
	{
		List<DashboardData>song=dashBoardService.getDashBoardData();
		return new ResponseEntity<List<DashboardData>>(song,HttpStatus.ACCEPTED);
		
	}
	

	@GetMapping("/getCategory")
	ResponseEntity<List<String>>getCategory() throws MainException
	{
		List<String>song=dashBoardService.getCategories();
		return new ResponseEntity<List<String>>(song,HttpStatus.ACCEPTED);
		
	}
	
	@GetMapping("/getCoverageNames")
	ResponseEntity<List<String>>getCoverageNames() throws MainException
	{
		List<String>song=dashBoardService.getCoverageNames();
		return new ResponseEntity<List<String>>(song,HttpStatus.ACCEPTED);
		
	}
	
	@GetMapping("/getOwner")
	ResponseEntity<List<String>>getOwner() throws MainException
	{
		List<String>song=dashBoardService.getOwners();
		return new ResponseEntity<List<String>>(song,HttpStatus.ACCEPTED);
		
	}
	
	@PostMapping("/getDataByType")
	ResponseEntity<List<DashboardData>> getDataByType(@RequestBody InputDashBoardDto inputDto) throws Exception
	{
		System.out.println(inputDto);
		List<DashboardData>song=dashBoardService.getDashBoardDataByType(inputDto);
		return new ResponseEntity<List<DashboardData>>(song,HttpStatus.ACCEPTED);
		
	}
	
	@PostMapping("/getCategoryData")
	ResponseEntity<List<DashboardData>> getCategoryData(@RequestBody InputDashBoardDto inputDto) throws Exception
	{
		System.out.println(inputDto);
		List<DashboardData>song=dashBoardService.getCategoryData(inputDto);
		return new ResponseEntity<List<DashboardData>>(song,HttpStatus.ACCEPTED);
		
	}
	
	@PostMapping("/getDataByLineType")
	ResponseEntity<List<DashboardData>> getDataByLineType(@RequestBody InputDashBoardDto inputDto) throws Exception
	{
		System.out.println(inputDto);
		List<DashboardData>song=dashBoardService.getDashBoardDataByLineType(inputDto);
		return new ResponseEntity<List<DashboardData>>(song,HttpStatus.ACCEPTED);
		
	}
	
	@PostMapping("/getDataByCategory")
	ResponseEntity<List<DashboardData>> getDataByCategory(@RequestBody InputDashBoardDto inputDto) throws Exception
	{
		System.out.println(inputDto);
		List<DashboardData>song=dashBoardService.getDashBoardDataByCategory(inputDto);
		return new ResponseEntity<List<DashboardData>>(song,HttpStatus.ACCEPTED);
		
	}
	
	@PostMapping("/getDataByOwner")
	ResponseEntity<List<DashboardData>> getDataByOwner(@RequestBody InputDashBoardDto inputDto) throws Exception
	{
		System.out.println(inputDto);
		List<DashboardData>song=dashBoardService.getDashBoardDataByOwner(inputDto);
		return new ResponseEntity<List<DashboardData>>(song,HttpStatus.ACCEPTED);
		
	}
	
}
