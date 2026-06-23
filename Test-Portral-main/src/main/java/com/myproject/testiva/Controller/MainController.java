package com.myproject.testiva.Controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.testiva.Model.Enquiry;
import com.myproject.testiva.Model.Enquiry.EnquiryStatus;
import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.StudentInfo.UserRole;
import com.myproject.testiva.Model.StudentInfo.UserStatus;
import com.myproject.testiva.Repository.EnquiryRepo;
import com.myproject.testiva.Repository.StudentInfoRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	
	//All Interface References
	@Autowired
	private StudentInfoRepo userInfoRepo;
	
	@Autowired
	private EnquiryRepo enquiryRepo;
	
	
	@GetMapping("/")
	public String ShowIndex()
	{
		return "index";
	}
		
	@GetMapping("/AboutUs")
	public String ShowAboutUs()
	{
		return "aboutus";
	}
	
	@GetMapping("/Registration")
	public String ShowRegistration(Model model)
	{
		StudentInfo stdinfo = new StudentInfo();
		model.addAttribute("stdinfo", stdinfo);
		return "Registration";
	}
	
	@PostMapping("/Registration")
	public String Registration(@ModelAttribute("stdinfo") StudentInfo newStudent,@RequestParam("profilePic") MultipartFile profilePic, RedirectAttributes attributes)
	{
		try {
			
			//File Uploading Code for upload profilePic
			if (!profilePic.isEmpty() && profilePic!=null) {
				
				String storageFileName = System.currentTimeMillis()+"_"+profilePic.getOriginalFilename();
				String uploadDir = "Public/ProfilePic/";
				Path uploadPath = Paths.get(uploadDir);
				
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				
				try(InputStream inputStream = profilePic.getInputStream()){
					Files.copy(inputStream, Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
				}
				
				newStudent.setProfilepic(storageFileName);
			}
			//Upload Data Into Database
			newStudent.setStatus(UserStatus.PENDING);
			newStudent.setRole(com.myproject.testiva.Model.StudentInfo.UserRole.STUDENT);
			newStudent.setRegdate(LocalDateTime.now());
			
			userInfoRepo.save(newStudent);
			
			attributes.addFlashAttribute("msg", "Registration Successful ✅");
			return "redirect:/Registration";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : "+e.getMessage());
			return "redirect:/Registration";
		}
	}
	
	
	//Admin Login
	@GetMapping("/AdminLogin")
	public String ShowAdminLogin()
	{
		return "AdminLogin";
	}
	
	@PostMapping("/AdminLogin")
	public String AdminLogin(@RequestParam("email") String email, @RequestParam("password") String password, RedirectAttributes attributes, HttpSession session)
	{
		try {
			if (!userInfoRepo.existsByEmail(email)) {
				attributes.addFlashAttribute("msg", "User doesn't Exists.");
				return "redirect:/AdminLogin";
			}
			
			StudentInfo admin = userInfoRepo.findByEmail(email); 
			
			if (admin.getPassword().equals(password) && admin.getEmail().equals(email) && admin.getRole().equals(UserRole.ADMIN)) {
				
				//Generate Session
				session.setAttribute("loggedInAdmin", admin);
				return "redirect:/Admin/Dashboard";
			}
			else {
				attributes.addFlashAttribute("msg", "Invalid Userid or Password");
			}
			return "redirect:/AdminLogin";
			
		} catch (Exception e) {
			return "redirect:/AdminLogin";
		}
	}
	
	@GetMapping("/StudentLogin")
	public String ShowStudentLogin()
	{
		return "StudentLogin";
	}
	
	@PostMapping("/StudentLogin")
	public String StudentLogin(@RequestParam("email") String email, @RequestParam("password") String password, RedirectAttributes attributes, HttpSession session)
	{
		try {
			if (!userInfoRepo.existsByEmail(email)) {
				attributes.addFlashAttribute("msg", "Student doesn't Exists");
				return "redirect:/StudentLogin";
			}
			
			StudentInfo student = userInfoRepo.findByEmail(email);
			
			if (student.getEmail().equals(email) && student.getPassword().equals(password) && student.getRole().equals(UserRole.STUDENT)) {
				
				//check status
				if (student.getStatus().equals(UserStatus.VERIFIED)) {
					session.setAttribute("loggedInStudent", student);
					return "redirect:/Student/Dashboard";
				}
				else if(student.getStatus().equals(UserStatus.PENDING))
				{
					attributes.addFlashAttribute("msg", "Status Pending, Please wait for Admin approval.");
				}
				else if(student.getStatus().equals(UserStatus.DISABLED)){
					attributes.addFlashAttribute("msg", "Login Disabled, Please Contact Administration.");
				}
			}
			else {
				attributes.addFlashAttribute("msg", "Invalid User or Wrong Password.");
			}
			return "redirect:/StudentLogin";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : "+e.getMessage());
			return "redirect:/StudentLogin";
		}
	}
	
	@GetMapping("/ContactUs")
	public String ShowContactUs(Model model)
	{
		model.addAttribute("enquiry", new Enquiry());
		return "ContactUs";
	}
	
	@PostMapping("/ContactUs")
	public String SubmitEnquiry(@ModelAttribute("enquiry") Enquiry enquiry, RedirectAttributes attributes)
	{
		try {
			enquiry.setStatus(EnquiryStatus.PENDING);
			enquiry.setSubmittedAt(LocalDateTime.now());
			enquiryRepo.save(enquiry);
			attributes.addFlashAttribute("msg", "Enquiry Submitted Successfully! We Will Connect You Soon 🥲");
			return "redirect:/ContactUs";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : "+e.getMessage());
			return "redirect:/ContactUs";
		}
	}
}
