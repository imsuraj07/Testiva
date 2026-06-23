package com.myproject.testiva.Repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.testiva.Model.StudentInfo;

public interface StudentInfoRepo extends JpaRepository<StudentInfo, Long>{

	boolean existsByEmail(String email);

	StudentInfo findByEmail(String email);

	List<StudentInfo> findAllByRole(com.myproject.testiva.Model.StudentInfo.UserRole student);

	List<StudentInfo> findAllByRoleAndCourseAndBranchAndYear(com.myproject.testiva.Model.StudentInfo.UserRole student, String course, String branch,
			String year);

}
