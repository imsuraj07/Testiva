package com.myproject.testiva.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.testiva.Model.TestResult;

public interface TestResultRepo extends JpaRepository<TestResult, Long>{

	List<TestResult> findAllByEmail(String email);

	Optional<TestResult> findTopByEmailOrderBySubmittedAtDesc(String email);
	
	// Find Top 5 by Total Score (Descending Order)
    List<TestResult> findTop5ByOrderByTotalScoreDesc();

	List<TestResult> findTop5ByTestIdOrderByTotalScoreDesc(String testId);

	boolean existsByEmailAndTestId(String email, String testId);
}
