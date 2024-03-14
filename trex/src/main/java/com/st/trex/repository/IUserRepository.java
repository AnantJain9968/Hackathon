package com.st.trex.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import com.st.trex.entity.User;

public interface IUserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameAndPassword(String username, String password);

	Optional<User> findByUsername(String username);

}
