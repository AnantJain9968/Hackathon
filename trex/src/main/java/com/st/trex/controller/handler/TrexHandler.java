package com.st.trex.controller.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.st.trex.dto.ErrorHandler;
import com.st.trex.exception.MainException;

@RestControllerAdvice
public class TrexHandler {

	@ExceptionHandler(MainException.class)
	public ResponseEntity<ErrorHandler> error(Exception e)
	{
		return new ResponseEntity<ErrorHandler>(new ErrorHandler(e.getMessage(),HttpStatus.BAD_REQUEST.value()),HttpStatus.BAD_REQUEST);
	}
}
