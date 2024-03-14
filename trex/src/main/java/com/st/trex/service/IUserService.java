package com.st.trex.service;

import java.sql.SQLIntegrityConstraintViolationException;

import com.st.trex.dto.UserDto;
import com.st.trex.entity.Role;
import com.st.trex.entity.User;
import com.st.trex.exception.service.ServiceException;

public interface IUserService {

	String register(User user, Role role) throws ServiceException;

	String login(UserDto user) throws ServiceException;

	String giveAccess(String username) throws ServiceException;

	

	

}
