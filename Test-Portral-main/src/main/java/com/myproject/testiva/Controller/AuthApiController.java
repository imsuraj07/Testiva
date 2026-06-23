package com.myproject.testiva.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.StudentInfo.UserRole;
import com.myproject.testiva.Model.StudentInfo.UserStatus;
import com.myproject.testiva.Repository.StudentInfoRepo;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private StudentInfoRepo userRepo;

    // ===================== ADMIN LOGIN =====================
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials, HttpSession session) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (!userRepo.existsByEmail(email)) {
                return ResponseEntity.status(401).body(Map.of("error", "User doesn't exist."));
            }

            StudentInfo admin = userRepo.findByEmail(email);

            if (admin.getPassword().equals(password) && admin.getRole().equals(UserRole.ADMIN)) {
                session.setAttribute("loggedInAdmin", admin);

                Map<String, Object> response = new HashMap<>();
                response.put("id", admin.getId());
                response.put("name", admin.getName());
                response.put("email", admin.getEmail());
                response.put("role", "ADMIN");
                response.put("profilepic", admin.getProfilepic());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials or not an admin."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ===================== STUDENT LOGIN =====================
    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(@RequestBody Map<String, String> credentials, HttpSession session) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (!userRepo.existsByEmail(email)) {
                return ResponseEntity.status(401).body(Map.of("error", "Student doesn't exist."));
            }

            StudentInfo student = userRepo.findByEmail(email);

            if (student.getEmail().equals(email) && student.getPassword().equals(password)
                    && student.getRole().equals(UserRole.STUDENT)) {

                if (student.getStatus().equals(UserStatus.VERIFIED)) {
                    session.setAttribute("loggedInStudent", student);

                    Map<String, Object> response = new HashMap<>();
                    response.put("id", student.getId());
                    response.put("name", student.getName());
                    response.put("email", student.getEmail());
                    response.put("contactno", student.getContactno());
                    response.put("course", student.getCourse());
                    response.put("branch", student.getBranch());
                    response.put("year", student.getYear());
                    response.put("role", "STUDENT");
                    response.put("profilepic", student.getProfilepic());
                    return ResponseEntity.ok(response);
                } else if (student.getStatus().equals(UserStatus.PENDING)) {
                    return ResponseEntity.status(403).body(Map.of("error", "Status Pending. Please wait for Admin approval."));
                } else {
                    return ResponseEntity.status(403).body(Map.of("error", "Login Disabled. Please contact administration."));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ===================== LOGOUT =====================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    // ===================== SESSION CHECK =====================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        StudentInfo admin = (StudentInfo) session.getAttribute("loggedInAdmin");
        if (admin != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", admin.getId());
            response.put("name", admin.getName());
            response.put("email", admin.getEmail());
            response.put("role", "ADMIN");
            response.put("profilepic", admin.getProfilepic());
            return ResponseEntity.ok(response);
        }

        StudentInfo student = (StudentInfo) session.getAttribute("loggedInStudent");
        if (student != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", student.getId());
            response.put("name", student.getName());
            response.put("email", student.getEmail());
            response.put("course", student.getCourse());
            response.put("branch", student.getBranch());
            response.put("year", student.getYear());
            response.put("role", "STUDENT");
            response.put("profilepic", student.getProfilepic());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated."));
    }
}
