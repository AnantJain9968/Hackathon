package com.st.trex.serviceimpl;


import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.st.trex.dto.UserDto;
import com.st.trex.entity.Role;
import com.st.trex.entity.User;
import com.st.trex.entity.UserRole;
import com.st.trex.exception.service.EmailIsAlreadyPresentException;
import com.st.trex.exception.service.ServiceException;
import com.st.trex.exception.service.UserHasAlreadyAccessException;
import com.st.trex.exception.service.UserNotFoundException;
import com.st.trex.exception.service.UsernameIsNotPresentException;
import com.st.trex.repository.IRoleRepository;
import com.st.trex.repository.IUserRepository;
import com.st.trex.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	IUserRepository userRepository;
	@Autowired
	IRoleRepository roleRepository;
	
	@Override
	public String register(User user,Role role) throws ServiceException {
		userEmailIsPresent(user.getEmail());
		usernameIsPresent(user.getUsername());
		user.getRoles().add(new UserRole(getRole(role.getRoleName()),user));
		System.out.println(user.toString());
		try {
		userRepository.save(user);}
		catch(DataIntegrityViolationException e) {
			System.err.println(e.getLocalizedMessage());
			
		}
		
		return "Registered Successfully";
	}
	public void usernameIsPresent(String username) throws ServiceException {
		if(userRepository.findByUsername(username).isPresent())
			throw new EmailIsAlreadyPresentException("Username is already registered");
	}

	
	public void userEmailIsPresent(String email) throws ServiceException {
		if(userRepository.findByEmail(email).isPresent())
			throw new EmailIsAlreadyPresentException("Email is already registered");
	}

	private Role getRole(String roleName) {
		if( roleRepository.findByRoleName(roleName).isPresent())
			return roleRepository.findByRoleName(roleName).get();
		else
			return roleRepository.save(new Role(roleName));
	}
	public User getUserByUsername(String username) throws ServiceException {
		return  userRepository.findByUsername(username).map(data->{
			 System.out.println("data"+data.toString());
			return data;
		}).orElseThrow(() -> new UsernameIsNotPresentException("Username is not present"));
	}

	@Override
	public String login(UserDto user) throws ServiceException {
		if(userRepository.findByUsernameAndPassword(user.getUsername(),user.getPassword()).isPresent()) {
			return "Successfully Login";
		}
		else 
		throw new UserNotFoundException("Check username or password "); 
	}
	@Override
	public String giveAccess(String username) throws ServiceException {
		System.out.println("username="+username);
		User user=getUserByUsername(username);
		System.out.println(user);
		Role role=getRole("admin");
		Iterator<UserRole>userrole=user.getRoles().iterator();
		while(userrole.hasNext()) {
			if (userrole.next().getRole().getRoleName().matches("admin"))
			throw new UserHasAlreadyAccessException("User already has admin access");
		}
		
		user.getRoles().add(new UserRole(role, user));
		System.out.println("User"+userRepository.save(user));
		return "Access given Successfully";
	}



	

}
