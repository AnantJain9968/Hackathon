package com.st.trex.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.st.trex.entity.Role;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Integer> {

	Optional<Role> findByRoleName(String roleName);

}
