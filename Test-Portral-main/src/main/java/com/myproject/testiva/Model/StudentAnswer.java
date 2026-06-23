package com.myproject.testiva.Model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
@Entity
public class StudentAnswer{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private TestAttempt testAttempt;
	
	@ManyToOne
	private QuestionBank questions;
	
	private String selectedOption;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TestAttempt getTestAttempt() {
		return testAttempt;
	}

	public void setTestAttempt(TestAttempt testAttempt) {
		this.testAttempt = testAttempt;
	}

	public QuestionBank getQuestions() {
		return questions;
	}

	public void setQuestions(QuestionBank questions) {
		this.questions = questions;
	}

	public String getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(String selectedOption) {
		this.selectedOption = selectedOption;
	}
	
}
	