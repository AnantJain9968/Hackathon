package com.st.trex.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userRoleId")
public class UserRole {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int userRoleId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.EAGER)
	private Role role;
	public UserRole() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public UserRole(Role role,User user) {
		super();
		this.role = role;
		this.user=user;
	}

	public UserRole(int userRoleId, User user, Role role) {
		super();
		this.userRoleId = userRoleId;
		this.user = user;
		this.role = role;
	}
	public int getUserRoleId() {
		return userRoleId;
	}
	public void setUserRoleId(int userRoleId) {
		this.userRoleId = userRoleId;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	@Override
	public String toString() {
		return "UserRole [userRoleId=" + userRoleId + ", user=" + user + ", role=" + role + "]";
	}
	
}
