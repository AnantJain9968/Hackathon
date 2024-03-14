package com.st.trex.serviceimpl;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.st.trex.entity.User;
import com.st.trex.repository.IUserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	IUserRepository userRepository;
	
	@Override
	public User loadUserByUsername(String username) {
		
			return userRepository.findByUsername(username).map(obj->{
				return obj;
			}).orElseThrow(()-> new UsernameNotFoundException("Check username or password"));
		
		
	}

}
