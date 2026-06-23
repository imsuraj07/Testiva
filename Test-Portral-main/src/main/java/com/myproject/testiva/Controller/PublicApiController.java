package com.myproject.testiva.Controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.myproject.testiva.Model.Enquiry;
import com.myproject.testiva.Model.Enquiry.EnquiryStatus;
import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.StudentInfo.UserRole;
import com.myproject.testiva.Model.StudentInfo.UserStatus;
import com.myproject.testiva.Repository.EnquiryRepo;
import com.myproject.testiva.Repository.StudentInfoRepo;

@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    @Autowired
    private StudentInfoRepo userInfoRepo;

    @Autowired
    private EnquiryRepo enquiryRepo;

    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("contactno") String contactno,
            @RequestParam("password") String password,
            @RequestParam("course") String course,
            @RequestParam("branch") String branch,
            @RequestParam("year") String year,
            @RequestParam(value = "profilePic", required = false) MultipartFile profilePic) {
        try {
            if (userInfoRepo.existsByEmail(email)) {
                return ResponseEntity.status(400).body(Map.of("error", "Email already exists."));
            }

            StudentInfo newStudent = new StudentInfo();
            newStudent.setName(name);
            newStudent.setEmail(email);
            newStudent.setContactno(contactno);
            newStudent.setPassword(password);
            newStudent.setCourse(course);
            newStudent.setBranch(branch);
            newStudent.setYear(year);

            // File Uploading Code for upload profilePic
            if (profilePic != null && !profilePic.isEmpty()) {
                String storageFileName = System.currentTimeMillis() + "_" + profilePic.getOriginalFilename();
                String uploadDir = "Public/ProfilePic/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream inputStream = profilePic.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                newStudent.setProfilepic(storageFileName);
            }

            newStudent.setStatus(UserStatus.PENDING);
            newStudent.setRole(UserRole.STUDENT);
            newStudent.setRegdate(LocalDateTime.now());

            userInfoRepo.save(newStudent);

            return ResponseEntity.ok(Map.of("message", "Registration Successful! Waiting for Admin approval."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/contact")
    public ResponseEntity<?> submitEnquiry(@RequestBody Enquiry enquiry) {
        try {
            enquiry.setStatus(EnquiryStatus.PENDING);
            enquiry.setSubmittedAt(LocalDateTime.now());
            enquiryRepo.save(enquiry);
            return ResponseEntity.ok(Map.of("message", "Enquiry Submitted Successfully! We Will Connect You Soon."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
