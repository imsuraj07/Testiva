package com.myproject.testiva.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.testiva.Model.Enquiry;

public interface EnquiryRepo extends JpaRepository<Enquiry, Long> {

	List<Enquiry> findTop5ByOrderBySubmittedAt();

}
