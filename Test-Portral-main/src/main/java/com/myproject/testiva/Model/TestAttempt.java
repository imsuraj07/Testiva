package com.myproject.testiva.Model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
@Entity
public class TestAttempt{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long attemptId;
	
	@ManyToOne
	private StudentInfo student;
	
	@ManyToOne
	private TestInfo test;
	
	private int score;
	private LocalDateTime submittedAt;
	public Long getAttemptId() {
		return attemptId;
	}
	public void setAttemptId(Long attemptId) {
		this.attemptId = attemptId;
	}
	public StudentInfo getStudent() {
		return student;
	}
	public void setStudent(StudentInfo student) {
		this.student = student;
	}
	public TestInfo getTest() {
		return test;
	}
	public void setTest(TestInfo test) {
		this.test = test;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}
	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}
	
	
}