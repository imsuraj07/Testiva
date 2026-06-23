package com.myproject.testiva.Model;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class TestInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, unique = true)
	private String testId; 
	
	@Column(nullable = false)
	private String testName;
	
	@Column(nullable = false)
	private String course;
	
	@Column(nullable = false)
	private String branch;
	
	@Column(nullable = false)
	private String year;
	
	@Column(nullable = false)
	private int testDuration;
	
	@Column(name="number_of_questions", nullable = false)
	private int numberOfQuestions;
	
	@Column(nullable = false)
	private LocalDateTime startTime;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TestStatus status;
	
	public enum TestStatus{
		Scheduled,
		Reminder_Sent,
		Active,
		Test_Over
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public int getTestDuration() {
		return testDuration;
	}

	public void setTestDuration(int testDuration) {
		this.testDuration = testDuration;
	}

	public int getNumberOfQuestions() {
		return numberOfQuestions;
	}

	public void setNumberOfQuestions(int numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public TestStatus getStatus() {
		return status;
	}

	public void setStatus(TestStatus status) {
		this.status = status;
	}
	
	public LocalDateTime endtime()
	{
		return startTime.plusMinutes(testDuration);
	}
	
}

