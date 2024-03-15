package com.st.trex.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "YPAPA_Project_details")
@Data
public class Project {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
	@Column(name = "PROJECT_NAME")
    private String name;
	
	@Column(name = "FREETEXT1")
    private String freeText1;
	
	@Column(name = "FREETEXT2")
    private String freeText2;
	
	@Column(name = "ACTIVITY_STATUS__CODE")
    private String status;
	
	@Column(name = "INSERTION_DATE")
    private String insertionDate;
	
	@Column(name = "INSERTING_USER")
    private String insertingUser;
	
	@Column(name = "LAST_UPDATING_DATE")
    private String lastUpdatingDate;
	
	@Column(name = "LAST_UPDATING_USER")
    private String lastUpdatingUser;
    
    @ManyToMany
    @JoinTable(
        name = "project_user",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;
    
    
    // getters and setters
}