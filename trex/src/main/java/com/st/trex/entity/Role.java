package com.st.trex.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "roleId")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int roleId;
	private String roleName;
	@OneToMany(cascade = CascadeType.PERSIST,fetch = FetchType.LAZY,mappedBy = "role")
	private Set<UserRole>users=new HashSet<UserRole>();
	
	public Role() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public Role(int roleId, String roleName) {
		super();
		this.roleId = roleId;
		this.roleName = roleName;
	}


	

	@Override
	public String toString() {
		return "Role [roleId=" + roleId + ", roleName=" + roleName + "]";
	}


	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Set<UserRole> getUsers() {
		return users;
	}

	public void setUsers(Set<UserRole> users) {
		this.users = users;
	}

	public Role(String roleName) {
		super();
		this.roleName = roleName;
	}
	
}
