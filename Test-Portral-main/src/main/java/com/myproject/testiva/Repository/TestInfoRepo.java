package com.myproject.testiva.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.testiva.Model.TestInfo;
import com.myproject.testiva.Model.TestInfo.TestStatus;

public interface TestInfoRepo extends JpaRepository<TestInfo, Long>{

	Optional<TestInfo> findTopByOrderByIdDesc();

	TestInfo findByTestId(String testId);
	TestInfo findByTestIdIgnoreCase(String testId);
	TestInfo findByTestIdIgnoreCaseOrTestNameIgnoreCase(String testId, String testName);

	TestInfo findTopByOrderByTestIdDesc();

	List<TestInfo> findByCourseAndBranchAndYearOrderByIdDesc(String course, String branch, String year);

	// Task 8: Only Active tests started within the last 3 days
	List<TestInfo> findByCourseAndBranchAndYearAndStatusAndStartTimeAfterOrderByIdDesc(
			String course, String branch, String year, TestStatus status, LocalDateTime cutoff);

}


