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
	
	/*@PostMapping(value = "/addsong")
	ResponseEntity<String>addSong(@RequestBody Song song) throws MainException
	{
		String s=songService.addSong(song);
		return new ResponseEntity<String>(s,HttpStatus.ACCEPTED);
		
	}*/
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
	
	@PostMapping("/getDataByType")
	ResponseEntity<List<DashboardData>> getDataByType(@RequestBody InputDashBoardDto inputDto) throws Exception
	{
		System.out.println(inputDto);
		List<DashboardData>song=dashBoardService.getDashBoardDataByType(inputDto);
		return new ResponseEntity<List<DashboardData>>(song,HttpStatus.ACCEPTED);
		
	}
	
}
