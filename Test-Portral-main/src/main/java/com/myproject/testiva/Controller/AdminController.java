package com.myproject.testiva.Controller;

import java.io.BufferedReader;
//import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.testiva.Model.Enquiry;
import com.myproject.testiva.Model.Enquiry.EnquiryStatus;
import com.myproject.testiva.Model.QuestionBank;
import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.StudentInfo.UserRole;
import com.myproject.testiva.Model.StudentInfo.UserStatus;
import com.myproject.testiva.Model.StudyMaterial;
import com.myproject.testiva.Model.TestInfo;
import com.myproject.testiva.Model.TestInfo.TestStatus;
import com.myproject.testiva.Model.TestQuestionMapping;
import com.myproject.testiva.Model.TestResult;
import com.myproject.testiva.Repository.EnquiryRepo;
import com.myproject.testiva.Repository.QuestionBankRepo;
import com.myproject.testiva.Repository.StudentInfoRepo;
import com.myproject.testiva.Repository.TestInfoRepo;
import com.myproject.testiva.Repository.TestQuestionRepo;
import com.myproject.testiva.Repository.TestResultRepo;
import com.myproject.testiva.Repository.studyMaterialRepository;
import com.myproject.testiva.ServiceAPI.SendAutoEmail;
import com.myproject.testiva.ServiceAPI.SendWhatsAppMessageAPI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Admin")
public class AdminController {

	@Autowired
	private HttpSession session;

	@Autowired
	private StudentInfoRepo userRepo;

	@Autowired
	private QuestionBankRepo qbRepo;

	@Autowired
	private TestInfoRepo testInfoRepo;

	@Autowired
	private TestQuestionRepo testQuestionRepo;

	@Autowired
	private TestResultRepo testResultRepo;

	@Autowired
	private EnquiryRepo enquiryRepo;

	@Autowired
	private studyMaterialRepository studyMaterialRepo;

	@Autowired
	private SendWhatsAppMessageAPI whatsAppMessageAPI;

	@Autowired
	private SendAutoEmail autoEmail;

	@GetMapping("/Dashboard")
	public String ShowDashboard(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}

		long totalStudents = userRepo.count() - 1;
		long totalTests = testInfoRepo.count();
		long totalResults = testResultRepo.count();
		long totalEnquiries = enquiryRepo.count();
		long totalQuestion = qbRepo.count();
		// TestInfo testInfo = testInfoRepo.findTopByOrderByTestIdDesc();

		// List<TestResult> recentActivities =
		// testResultRepo.findTop5ByOrderByTotalScoreDesc();
		List<TestResult> listOfRecentTestToppers = new ArrayList<>();

		// List<TestResult> listOfRecentTestToppers =
		// testResultRepo.findTop5ByTestIdOrderByTotalScoreDesc(testInfo.getTestId());
		List<Enquiry> enquiries = enquiryRepo.findTop5ByOrderBySubmittedAt();

		model.addAttribute("totalStudents", totalStudents);
		model.addAttribute("totalTests", totalTests);
		model.addAttribute("totalResults", totalResults);
		model.addAttribute("totalEnquiries", totalEnquiries);
		model.addAttribute("recentActivities", listOfRecentTestToppers);
		model.addAttribute("enquiries", enquiries);
		model.addAttribute("totalQuestion", totalQuestion);
		return "Admin/Dashboard";
	}

	@GetMapping("/ManageStudents")
	public String ShowManageStudents(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}

		List<StudentInfo> studentList = userRepo.findAllByRole(UserRole.STUDENT);
		model.addAttribute("studentList", studentList);
		return "Admin/ManageStudents";
	}

	@GetMapping("/UpdateStatus")
	public String UpdateStudentStatus(@RequestParam("id") long stdid) {
		try {
			StudentInfo studentInfo = userRepo.findById(stdid).get();
			///////////////////////////////////////////////////////////////
			if (studentInfo.getStatus().equals(UserStatus.PENDING)) {

				studentInfo.setStatus(UserStatus.VERIFIED);
				userRepo.save(studentInfo);
				autoEmail.sendVerificationStatusMail(studentInfo);
			} else if (studentInfo.getStatus().equals(UserStatus.VERIFIED)) {
				studentInfo.setStatus(UserStatus.DISABLED);
				userRepo.save(studentInfo);
			} else if (studentInfo.getStatus().equals(UserStatus.DISABLED)) {
				studentInfo.setStatus(UserStatus.VERIFIED);
				userRepo.save(studentInfo);
			}
			System.err.println("Status Updated");
			return "redirect:/Admin/ManageStudents";
		} catch (Exception e) {
			System.err.println("Error : " + e.getMessage());
			return "redirect:/Admin/ManageStudents";
		}
	}

	@GetMapping("/EditStudent")
	public String ShowEditStudent(@RequestParam("id") long id, Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		StudentInfo studentInfo = userRepo.findById(id).get();
		model.addAttribute("studentInfo", studentInfo);
		return "Admin/EditStudent";
	}

	@PostMapping("/EditStudent")
	public String EditStudent(@ModelAttribute("studentInfo") StudentInfo studentInfo, RedirectAttributes attributes) {
		try {

			return "redirect:/Admin/EditStudent";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : " + e.getMessage());
			return "redirect:/Admin/EditStudent";

		}
	}

	@GetMapping("/AddQuestion")
	public String ShowAddQuestion(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		QuestionBank questionBank = new QuestionBank();
		model.addAttribute("questionBank", questionBank);
		model.addAttribute("testList", testInfoRepo.findAll());
		return "Admin/AddQuestion";
	}

	@PostMapping("/UploadQuestion")
	public String UploadCSVQuestion(
			@RequestParam("questionFile") MultipartFile csvFile,
			@RequestParam("testId") Long testId,
			@ModelAttribute("questionBank") QuestionBank questionBank,
			RedirectAttributes attributes) {

		try {
			if (csvFile.isEmpty()) {
				attributes.addFlashAttribute("msg", "Empty File ❌");
				return "redirect:/Admin/AddQuestion";
			}

			TestInfo test = testInfoRepo.findById(testId).orElse(null);
			if (test == null) {
				attributes.addFlashAttribute("msg", "Invalid Test ❌");
				return "redirect:/Admin/AddQuestion";
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {

				String line;
				reader.readLine(); // skip header
				List<QuestionBank> qbList = new ArrayList<>();

				while ((line = reader.readLine()) != null) {
					String qbData[] = line.split(",");

					if (qbData.length == 6) {

						QuestionBank qb = new QuestionBank();

						qb.setQuestion(qbData[0]);
						qb.setA(qbData[1]);
						qb.setB(qbData[2]);
						qb.setC(qbData[3]);
						qb.setD(qbData[4]);
						qb.setCorrect(qbData[5]);

						qb.setCourse(questionBank.getCourse());
						qb.setBranch(questionBank.getBranch());
						qb.setYear(questionBank.getYear());

						// 🔥🔥🔥 MOST IMPORTANT LINE
						qb.setTest(test);

						qbList.add(qb);
					}
				}

				qbRepo.saveAll(qbList);
				attributes.addFlashAttribute("msg", "Questions uploaded successfully ✅");
				return "redirect:/Admin/AddQuestion";
			}

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : " + e.getMessage());
			return "redirect:/Admin/AddQuestion";
		}
	}

	@GetMapping("/ManageQuestion")
	public String ManageQuestionBank(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}

		List<QuestionBank> questionList = qbRepo.findAll();
		model.addAttribute("questionList", questionList);
		model.addAttribute("testList", testInfoRepo.findAll());
		return "Admin/ManageQuestionBank";
	}

	@GetMapping("/DeleteQuestion")
	public String DeleteQuestion(@RequestParam("id") long id, RedirectAttributes attributes) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		try {
			qbRepo.deleteById(id);
			attributes.addFlashAttribute("msg", "Question deleted successfully.");
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error: " + e.getMessage());
		}
		return "redirect:/Admin/ManageQuestion";
	}

	@GetMapping("/add-question/{testId}")
	public String ShowManualAddQuestion(@PathVariable("testId") String testId, Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		TestInfo test = testInfoRepo.findByTestId(testId);
		if (test == null) {
			return "redirect:/Admin/ScheduleTest";
		}
		long currentCount = qbRepo.countByTest(test);
		QuestionBank questionBank = new QuestionBank();
		questionBank.setTest(test);
		questionBank.setCourse(test.getCourse());
		questionBank.setBranch(test.getBranch());
		questionBank.setYear(test.getYear());
		model.addAttribute("questionBank", questionBank);
		model.addAttribute("test", test);
		model.addAttribute("currentCount", currentCount);
		return "Admin/ManualAddQuestion";
	}

	@PostMapping("/save-question/{testId}")
	public String SaveManualQuestion(@PathVariable("testId") String testId,
			@ModelAttribute("questionBank") QuestionBank questionBank,
			@RequestParam(value = "addAnother", required = false) String addAnother,
			RedirectAttributes attributes) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		TestInfo test = testInfoRepo.findByTestId(testId);
		if (test == null) {
			return "redirect:/Admin/ScheduleTest";
		}
		long currentCount = qbRepo.countByTest(test);
		if (currentCount >= test.getNumberOfQuestions()) {
			attributes.addFlashAttribute("msg", "Cannot add question. Test already has maximum number of questions.");
			return "redirect:/Admin/ScheduleTest";
		}
		questionBank.setTest(test);
		qbRepo.save(questionBank);
		attributes.addFlashAttribute("msg", "Question Added Successfully!");
		// 'Save & Add Another' keeps the form open; plain 'Save' goes back to schedule
		// list
		if (addAnother != null) {
			return "redirect:/Admin/add-question/" + testId;
		}
		return "redirect:/Admin/ScheduleTest";
	}

	@PostMapping("/assign-questions-to-test")
	public String AssignQuestionsToTest(@RequestParam("testId") String testId,
			@RequestParam(value = "selectedQuestionIds", required = false) List<Long> selectedQuestionIds,
			RedirectAttributes attributes) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		if (selectedQuestionIds == null || selectedQuestionIds.isEmpty()) {
			attributes.addFlashAttribute("msg", "No questions selected.");
			return "redirect:/Admin/ManageQuestion";
		}
		TestInfo test = testInfoRepo.findByTestId(testId);
		if (test == null) {
			attributes.addFlashAttribute("msg", "Invalid test ID.");
			return "redirect:/Admin/ManageQuestion";
		}

		// Validation: Verify selected count matches required count exactly
		if (selectedQuestionIds.size() != test.getNumberOfQuestions()) {
			if (selectedQuestionIds.size() > test.getNumberOfQuestions()) {
				attributes.addFlashAttribute("msg", "You selected more questions than required.");
			} else {
				attributes.addFlashAttribute("msg", "You selected fewer questions. Please select more questions.");
			}
			return "redirect:/Admin/ManageQuestion";
		}

		// Generate actual Test ID if this is a pending test (Step 4)
		if (test.getTestId().startsWith("PENDING-")) {
			long maxId = testInfoRepo.findTopByOrderByIdDesc().map(TestInfo::getId).orElse(0L);
			String generatedTestId = "TTP" + String.format("%03d", maxId + 1);
			test.setTestId(generatedTestId);
			testInfoRepo.save(test);
		}

		// Save test_questions mapping instead of modifying QuestionBank test field
		List<QuestionBank> selectedQuestions = qbRepo.findAllById(selectedQuestionIds);
		List<TestQuestionMapping> mappings = new ArrayList<>();
		for (QuestionBank qb : selectedQuestions) {
			TestQuestionMapping mapping = new TestQuestionMapping();
			mapping.setTest(test);
			mapping.setQuestion(qb);
			mappings.add(mapping);
		}
		testQuestionRepo.saveAll(mappings);

		// Required popup text matches exactly step 4 definition
		attributes.addFlashAttribute("successTestId", test.getTestId());

		return "redirect:/Admin/ManageQuestion";
	}

	@GetMapping("/ScheduleTest")
	public String ShowScheduleTest(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		TestInfo testInfo = new TestInfo();
		model.addAttribute("testInfo", testInfo);
		List<TestInfo> testList = testInfoRepo.findAll().reversed();
		model.addAttribute("testList", testList);

		return "Admin/ScheduleTest";
	}

	@PostMapping("/ScheduleTest")
	public String ScheduleTest(@ModelAttribute("testInfo") TestInfo testInfo, RedirectAttributes attributes) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		try {
			// Initially create with PENDING ID
			String pendingTestId = "PENDING-" + UUID.randomUUID().toString().substring(0, 8);
			testInfo.setTestId(pendingTestId);
			testInfo.setStatus(TestStatus.Scheduled);
			testInfoRepo.save(testInfo);

			// Pass attributes to manage questions implicitly for the new popup workflow
			attributes.addFlashAttribute("pendingTestId", pendingTestId);
			attributes.addFlashAttribute("requiredQuestionsCount", testInfo.getNumberOfQuestions());

			// Send reminders once fully scheduled might be better, but keeping existing
			// behavior
			List<StudentInfo> studentList = userRepo.findAllByRoleAndCourseAndBranchAndYear(
					UserRole.STUDENT, testInfo.getCourse(), testInfo.getBranch(), testInfo.getYear());
			for (StudentInfo student : studentList) {
				// whatsAppMessageAPI.sendTestReminderMessage(student, testInfo); // Disabled
				// due to expired API subscription
			}

			// Redirect directly to manage questions as instructed in Step 1
			return "redirect:/Admin/ManageQuestion";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : " + e.getMessage());
			return "redirect:/Admin/ScheduleTest";
		}
	}

	@GetMapping("/DeleteTest")
	public String DeleteTest(@RequestParam("id") long id, RedirectAttributes attributes) {
		try {
			TestInfo testInfo = testInfoRepo.findById(id).orElse(null);
			if (testInfo != null) {
				// Delete all associated TestQuestionMappings manually first
				// to avoid foreign-key constraint violations on deletion!
				List<TestQuestionMapping> mappings = testQuestionRepo.findAll();
				for (TestQuestionMapping mapping : mappings) {
					if (mapping.getTest().getId() == id) {
						testQuestionRepo.delete(mapping);
					}
				}
				testInfoRepo.deleteById(id);
			}
			return "redirect:/Admin/ScheduleTest";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error deleting test: " + e.getMessage());
			return "redirect:/Admin/ScheduleTest";
		}

	}

	// ===== TASK 2: EDIT TEST =====

	@GetMapping("/EditTest")
	public String ShowEditTest(@RequestParam("id") long id, Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		TestInfo testInfo = testInfoRepo.findById(id).orElse(null);
		if (testInfo == null) {
			return "redirect:/Admin/ScheduleTest";
		}
		model.addAttribute("testInfo", testInfo);
		return "Admin/EditTest";
	}

	@PostMapping("/EditTest")
	public String UpdateTest(@ModelAttribute("testInfo") TestInfo testInfo, RedirectAttributes attributes) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		try {
			TestInfo existing = testInfoRepo.findById(testInfo.getId()).orElse(null);
			if (existing == null) {
				attributes.addFlashAttribute("msg", "Test not found.");
				return "redirect:/Admin/ScheduleTest";
			}
			// Preserve auto-generated testId — don't let user change it
			testInfo.setTestId(existing.getTestId());
			testInfoRepo.save(testInfo);
			attributes.addFlashAttribute("msg", "✅ Test Updated Successfully!");
			return "redirect:/Admin/ScheduleTest";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error: " + e.getMessage());
			return "redirect:/Admin/ScheduleTest";
		}
	}

	@GetMapping("/ManageResult")
	public String ShowManageResult(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		model.addAttribute("testResults", testResultRepo.findAll());
		return "Admin/ManageResult";
	}

	@GetMapping("/Enquiry")
	public String ShowEnquiry(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		model.addAttribute("enquiryList", enquiryRepo.findAll());
		return "Admin/ManageEnquiry";
	}

	@PostMapping("/SendEnquiryReply")
	public String sendEnquiryReply(@RequestParam("id") long id, @RequestParam("subject") String subject,
			@RequestParam("message") String message, RedirectAttributes attributes) {
		try {
			Enquiry enquiry = enquiryRepo.findById(id).get();
			enquiry.setStatus(EnquiryStatus.RESPONSE_SENT);
			enquiryRepo.save(enquiry);
			autoEmail.sendEnquryResponseMail(enquiry, subject, message);
			attributes.addFlashAttribute("msg", "Response Sent Successfully!!!");
			return "redirect:/Admin/Enquiry";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error :" + e.getMessage());
			return "redirect:/Admin/Enquiry";
		}
	}

	@GetMapping("/DeleteEnquiry")
	public String DeleteEnquiry(@RequestParam("id") long id) {
		try {
			enquiryRepo.deleteById(id);
			return "redirect:/Admin/Enquiry";
		} catch (Exception e) {
			System.err.println("Error : " + e.getMessage());
			return "redirect:/Admin/Enquiry";
		}
	}

	@GetMapping("/ChangePassword")
	public String ShowChangePassword() {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		return "Admin/ChangePassword";
	}

	@PostMapping("/ChangePassword")
	public String ChangePassword(HttpServletRequest request, RedirectAttributes attributes) {
		try {
			StudentInfo adminInfo = (StudentInfo) session.getAttribute("loggedInAdmin");
			String oldPassword = request.getParameter("currentPassword");
			String newPassword = request.getParameter("newPassword");
			String confirmPassword = request.getParameter("confirmPassword");

			if (!newPassword.equals(confirmPassword)) {
				attributes.addFlashAttribute("errorMessage", "New Password and Confirm Password are not same");
				return "redirect:/Admin/ChangePassword";
			}

			if (oldPassword.equals(adminInfo.getPassword())) {
				adminInfo.setPassword(confirmPassword);
				userRepo.save(adminInfo);
				session.removeAttribute("loggedInAdmin");
				return "redirect:/AdminLogin";
			} else {
				attributes.addFlashAttribute("errorMessage", "Invalid Old Password!!!");
				return "redirect:/Admin/ChangePassword";

			}

		} catch (Exception e) {
			return "redirect:/Admin/ChangePassword";
		}
	}

	@GetMapping("/Logout")
	public String Logout() {
		session.removeAttribute("loggedInAdmin");
		return "redirect:/AdminLogin";
	}

	// Extra Method for count the number of question are available or not in our
	// questionBank
	public boolean hasEnoughQuestions(String course, String branch, String year, int requiredCount) {
		long available = qbRepo.countByCourseAndBranchAndYear(course, branch, year);
		return available >= requiredCount;
	}

	@GetMapping("/UpdateProfilePic")
	public String ShowUpdateProfile() {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		return "Admin/UpdateProfile";
	}

	@GetMapping("/UploadStudyMaterial")
	public String showUploadStudyMaterial(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}
		model.addAttribute("studyMaterial", new StudyMaterial());
		return "Admin/UploadStudyMaterial";
	}

	@PostMapping("/UploadStudyMaterial")
	public String uploadStudyMaterial(
			@ModelAttribute("studyMaterial") StudyMaterial studyMaterial,
			@RequestParam(value = "materialFile", required = false) MultipartFile materialFile,
			RedirectAttributes attributes) {

		try {

			if (session.getAttribute("loggedInAdmin") == null) {
				return "redirect:/AdminLogin";
			}

			// ================= PDF MATERIAL =================
			if (studyMaterial.getMaterialType() == StudyMaterial.MaterialType.PDF) {

				if (materialFile == null || materialFile.isEmpty()) {
					attributes.addFlashAttribute("msg", "❌ Please upload PDF file");
					return "redirect:/Admin/UploadStudyMaterial";
				}

				// SAME STYLE AS AddProduct
				String storageFileName = UUID.randomUUID() + "_" + materialFile.getOriginalFilename();

				String uploadDir = "public/study-material/";
				Path uploadPath = Paths.get(uploadDir);

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				try (InputStream inputStream = materialFile.getInputStream()) {
					Files.copy(
							inputStream,
							Paths.get(uploadDir + storageFileName),
							StandardCopyOption.REPLACE_EXISTING);
				}

				studyMaterial.setFileUrl(uploadDir + storageFileName);
			}

			// ================= VIDEO MATERIAL =================
			if (studyMaterial.getMaterialType() == StudyMaterial.MaterialType.Video) {

				if (studyMaterial.getFileUrl() == null || studyMaterial.getFileUrl().isBlank()) {
					attributes.addFlashAttribute("msg", "❌ Please enter video URL");
					return "redirect:/Admin/UploadStudyMaterial";
				}
			}

			// ================= COMMON FIELDS =================
			studyMaterial.setUploadedAt(LocalDateTime.now());
			studyMaterial.setViewCount(0);
			studyMaterial.setIsVisible(StudyMaterial.MediaVisibility.Visible);

			studyMaterialRepo.save(studyMaterial);

			attributes.addFlashAttribute("msg", "✅ Study material uploaded successfully");
			return "redirect:/Admin/UploadStudyMaterial";

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "❌ Error : " + e.getMessage());
			return "redirect:/Admin/UploadStudyMaterial";
		}
	}

	/* ================= MANAGE PDF ================= */

	@GetMapping("/ManagePDFMaterial")
	public String managePDFMaterial(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}

		List<StudyMaterial> pdfMaterials = studyMaterialRepo.findByMaterialType(
				StudyMaterial.MaterialType.PDF);

		model.addAttribute("pdfMaterials", pdfMaterials);
		return "Admin/ManagePdfMaterial"; // EXACT file name
	}

	/* ================= MANAGE VIDEO ================= */

	@GetMapping("/ManageVideoMaterial")
	public String manageVideoMaterial(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/AdminLogin";
		}

		List<StudyMaterial> videoMaterials = studyMaterialRepo.findByMaterialType(
				StudyMaterial.MaterialType.Video);

		model.addAttribute("videoMaterials", videoMaterials);
		return "Admin/ManageVideoMaterial"; // EXACT file name
	}

}
