package com.myproject.testiva.Controller;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.myproject.testiva.Model.QuestionBank;
import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.StudyMaterial;
import com.myproject.testiva.Model.TestInfo;
import com.myproject.testiva.Model.TestInfo.TestStatus;
import com.myproject.testiva.Model.TestResult;
import com.myproject.testiva.Repository.QuestionBankRepo;
import com.myproject.testiva.Repository.StudentInfoRepo;
import com.myproject.testiva.Repository.TestInfoRepo;
import com.myproject.testiva.Repository.TestQuestionRepo;
import com.myproject.testiva.Repository.TestResultRepo;
import com.myproject.testiva.Repository.studyMaterialRepository;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Student")
public class StudentController {

	@Autowired
	private HttpSession session;

	@Autowired
	private TestInfoRepo testInfoRepo;

	@Autowired
	private TestQuestionRepo testQuestionRepo;

	@Autowired
	private QuestionBankRepo questionBankRepo;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private TestResultRepo testResultRepo;

	@Autowired
	private StudentInfoRepo studentInfoRepo;
	
	@Autowired
	private studyMaterialRepository studyMaterialRepo;


	// Methods Started From Here || GetMapping and PostMapping
	@GetMapping("/Dashboard")
	public String ShowDashboard(Model model) {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}
		StudentInfo student = (StudentInfo) session.getAttribute("loggedInStudent");
		List<TestResult> testResults = testResultRepo.findAllByEmail(student.getEmail());
		model.addAttribute("student", student); // StudentInfo object
		model.addAttribute("testCount", testResults.size()); // Total test count
		model.addAttribute("testResults", testResults); // Provide all results

		Optional<TestResult> latestResultOpt = testResultRepo.findTopByEmailOrderBySubmittedAtDesc(student.getEmail());
		latestResultOpt.ifPresent(result -> model.addAttribute("latestResult", result));

		return "Student/Dashboard";
	}

	@GetMapping("/GiveTest")
	public String ShowGiveTest(Model model) {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}
		StudentInfo studentInfo = (StudentInfo) session.getAttribute("loggedInStudent");
		
		// Fetch all tests for student's course/branch/year
		List<TestInfo> allTests = testInfoRepo.findByCourseAndBranchAndYearOrderByIdDesc(
				studentInfo.getCourse(), studentInfo.getBranch(), studentInfo.getYear());
		
		// Filter: Hide expired tests and incomplete tests
		LocalDateTime now = LocalDateTime.now();
		List<TestInfo> availableTests = new java.util.ArrayList<>();
		for (TestInfo t : allTests) {
			// Skip tests with null startTime (data integrity guard)
			if (t.getStartTime() == null) {
				continue;
			}
			// Skip tests with PENDING IDs (questions not yet assigned)
			if (t.getTestId() != null && t.getTestId().startsWith("PENDING-")) {
				continue;
			}
			// Show test if it hasn't expired yet: now <= startTime + testDuration
			LocalDateTime testEndTime = t.getStartTime().plusMinutes(t.getTestDuration());
			if (now.isBefore(testEndTime) || now.isEqual(testEndTime)) {
				availableTests.add(t);
			}
			// Also show Test_Over tests for up to 1 hour after end (for visibility)
			else if (t.getStatus().equals(TestStatus.Test_Over) && now.isBefore(testEndTime.plusHours(1))) {
				availableTests.add(t);
			}
		}
		
		model.addAttribute("availableTests", availableTests);
		return "Student/GiveTest";
	}

	// Store Test ID in a Temprary Variable
	private String testId;

	@PostMapping("/ValidateTestId")
	public String ValidateTestId(@RequestParam("inputTestId") String inputTestId, RedirectAttributes attributes) {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}
		if (inputTestId != null) {
			inputTestId = inputTestId.trim();
		}
		TestInfo testInfo = testInfoRepo.findByTestIdIgnoreCaseOrTestNameIgnoreCase(inputTestId, inputTestId);
		if(testInfo == null) {
			// Fallback try with exact match just in case
			testInfo = testInfoRepo.findByTestId(inputTestId);
		}

		if (testInfo == null) {
			attributes.addFlashAttribute("msg", "Validation Failed: The Test ID '" + inputTestId + "' is incorrect.");
			return "redirect:/Student/GiveTest";
		}
		
		return "redirect:/Student/StartTest?id=" + testInfo.getId();
	}

	@GetMapping("/StartTest")
	public String StartTest(@RequestParam(value = "id", required = false) Long id, 
							@RequestParam(value = "testid", required = false) String testIdParam, 
							RedirectAttributes attributes, Model model) {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}
		
		StudentInfo studentInfo = (StudentInfo) session.getAttribute("loggedInStudent");

		// 1. Determine which identifier we received
		TestInfo testInfo = null;
		if (id != null) {
			testInfo = testInfoRepo.findById(id).orElse(null);
		} else if (testIdParam != null && !testIdParam.isEmpty()) {
			testInfo = testInfoRepo.findByTestIdIgnoreCase(testIdParam.trim());
		}

		if (testInfo == null) {
			attributes.addFlashAttribute("msg", "Test not found. It may have been deleted or the link is invalid.");
			return "redirect:/Student/GiveTest";
		}

		// Use the actual testId from DB for all downstream operations
		String testId = testInfo.getTestId();
		this.testId = testId;

		// 2. Check if student already attempted this test
		if (testResultRepo.existsByEmailAndTestId(studentInfo.getEmail(), testId)) {
			attributes.addFlashAttribute("msg", "You have already given the test, you can't enroll twice!!");
			return "redirect:/Student/GiveTest";
		}

		// 3. Validate course/branch/year match
		if (!studentInfo.getCourse().equals(testInfo.getCourse())
				|| !studentInfo.getBranch().equals(testInfo.getBranch())
				|| !studentInfo.getYear().equals(testInfo.getYear())) {
			attributes.addFlashAttribute("msg", "Invalid Test — this test is not for your course/branch/year.");
			return "redirect:/Student/GiveTest";
		}

		// 4. Time window validation: [startTime - 10 min] → [startTime + testDuration]
		if (testInfo.getStartTime() != null) {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime windowStart = testInfo.getStartTime().minusMinutes(10);
			LocalDateTime windowEnd = testInfo.getStartTime().plusMinutes(testInfo.getTestDuration());

			if (now.isBefore(windowStart)) {
				attributes.addFlashAttribute("msg", "Test is not available yet. It opens at " 
					+ windowStart.toString().replace("T", " ") + ".");
				return "redirect:/Student/GiveTest";
			}
			if (now.isAfter(windowEnd)) {
				attributes.addFlashAttribute("msg", "This test has ended. The window was until " 
					+ windowEnd.toString().replace("T", " ") + ".");
				return "redirect:/Student/GiveTest";
			}
		}

		// 5. Status check
		if (testInfo.getStatus().equals(TestStatus.Test_Over)) {
			attributes.addFlashAttribute("msg", "This test has already ended.");
			return "redirect:/Student/GiveTest";
		}

		// 6. Load questions — try mapping table first, then legacy QuestionBank.test FK
		try {
			List<QuestionBank> qbList = null;

			// Try test_questions mapping table (new flow)
			if (testId != null) {
				qbList = testQuestionRepo.findQuestionsByTestId(testId);
			}
			
			// Fallback: legacy QuestionBank.test FK (old flow where questions have test_id directly)
			if (qbList == null || qbList.isEmpty()) {
				qbList = questionBankRepo.findAllByTest(testInfo);
			}

			// Guard: no questions at all
			if (qbList == null || qbList.isEmpty()) {
				attributes.addFlashAttribute("msg", "This test has no questions assigned yet. Please contact your instructor.");
				return "redirect:/Student/GiveTest";
			}

			// Create Gson handling LocalDateTime to avoid Java 17 InaccessibleObjectException
			Gson gson = new com.google.gson.GsonBuilder()
				.registerTypeAdapter(java.time.LocalDateTime.class, 
					(com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) -> 
						new com.google.gson.JsonPrimitive(src.toString()))
				.create();
			String json = gson.toJson(qbList);

			model.addAttribute("json", json);
			model.addAttribute("tt", testInfo.getTestDuration());
			model.addAttribute("tq", qbList.size());
			model.addAttribute("testname", testInfo.getTestName());
			model.addAttribute("testId", testId != null ? testId : (id != null ? String.valueOf(id) : testIdParam));

			return "Student/StartTest";

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error loading test: " + e.getMessage());
			return "redirect:/Student/GiveTest";
		}
	}

	// Test Over Logic and Method
	@GetMapping("/TestOver")
	public String ShowTestOver(Model model) {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}
		StudentInfo student = (StudentInfo) session.getAttribute("loggedInStudent");
		// Get latest submitted result
		testResultRepo.findTopByEmailOrderBySubmittedAtDesc(student.getEmail())
				.ifPresent(r -> model.addAttribute("result", r));
		return "Student/TestOver";
	}

	@PostMapping("/TestOver")
	public String TestOver(@RequestParam("t") int totalMarks, @RequestParam("s") int totalScore,
			@RequestParam("testId") String testId, RedirectAttributes attributes) {
		try {
			StudentInfo studentInfo = (StudentInfo) session.getAttribute("loggedInStudent");
			TestInfo testInfo = testInfoRepo.findByTestId(testId);

			TestResult result = new TestResult();
			result.setName(studentInfo.getName());
			result.setContactno(studentInfo.getContactno());
			result.setEmail(studentInfo.getEmail());
			result.setCourse(studentInfo.getCourse());
			result.setBranch(studentInfo.getBranch());
			result.setYear(studentInfo.getYear());

			result.setTestId(testId);
			result.setTestName(testInfo.getTestName());
			result.setTotalMarks(totalMarks);
			result.setTotalScore(totalScore);
			result.setSubmittedAt(LocalDateTime.now());
			testResultRepo.save(result);
			return "redirect:/Student/TestOver";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : " + e.getMessage());
			return "redirect:/Student/GiveTest";
		}
	}

	@GetMapping("/SeeResult")
	public String ShowSeeResult(Model model) {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}

		StudentInfo studentInfo = (StudentInfo) session.getAttribute("loggedInStudent");

		List<TestResult> testResults = testResultRepo.findAllByEmail(studentInfo.getEmail());
		model.addAttribute("testResults", testResults);
		return "Student/SeeResult";
	}

	@GetMapping("/ChangePassword")
	public String ShowChangePassword() {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}

		return "Student/ChangePassword";
	}

	@PostMapping("/ChangePassword")
	public String ChangePassword(HttpServletRequest request, RedirectAttributes attributes) {
		try {
			String currentPassword = request.getParameter("currentPassword");
			String newPassword = request.getParameter("newPassword");
			String confirmPassword = request.getParameter("confirmPassword");

			StudentInfo studentInfo = (StudentInfo) session.getAttribute("loggedInStudent");

			if (!newPassword.equals(confirmPassword)) {
				attributes.addFlashAttribute("msg", "New Password and Confirm Password are not Same.");
				return "redirect:/Student/ChangePassword";
			}

			if (currentPassword.equals(studentInfo.getPassword())) {
				// change kara do
				studentInfo.setPassword(confirmPassword);
				studentInfoRepo.save(studentInfo);
				session.removeAttribute("loggedInStudent");
				attributes.addFlashAttribute("msg", "Password Changed Successfully");
				return "redirect:/StudentLogin";
			} else {
				attributes.addFlashAttribute("msg", "Invalid Current Password.");
				return "redirect:/Student/ChangePassword";
			}

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : " + e.getMessage());
			return "redirect:/Student/ChangePassword";
		}
	}

	@GetMapping("/VideoMaterials")
	public String ShowVideoMaterial(Model model) {

	    if (session.getAttribute("loggedInStudent") == null) {
	        return "redirect:/StudentLogin";
	    }

	    List<StudyMaterial> videoMaterials =
	            studyMaterialRepo.findByMaterialTypeAndIsVisible(
	                    StudyMaterial.MaterialType.Video,
	                    StudyMaterial.MediaVisibility.Visible
	            ); 

	    model.addAttribute("videoMaterials", videoMaterials);
	    return "Student/VideoMaterial";
	}


	@GetMapping("/PdfMaterials")
	public String ShowPdfMaterial(Model model) {

	    if (session.getAttribute("loggedInStudent") == null) {
	        return "redirect:/StudentLogin";
	    }

	    List<StudyMaterial> pdfMaterials =
	            studyMaterialRepo.findByMaterialTypeAndIsVisible(
	                    StudyMaterial.MaterialType.PDF,
	                    StudyMaterial.MediaVisibility.Visible
	            );

	    model.addAttribute("pdfMaterials", pdfMaterials);
	    return "Student/PdfMaterial";
	}


	@GetMapping("/UpdateProfilePic")
	public String ShowUpdateProfilePic(Model model) {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}
		StudentInfo student = (StudentInfo) session.getAttribute("loggedInStudent");
		model.addAttribute("student", student);
		return "Student/UpdateProfilePic";
	}

	@PostMapping("/UpdateProfilePic")
	public String UpdateProfilePic(@RequestParam("profilePic") MultipartFile profilePic, RedirectAttributes attributes)
	{
		try {
			StudentInfo studentInfo = (StudentInfo) session.getAttribute("loggedInStudent");
			String storageFileName = System.currentTimeMillis()+"_"+profilePic.getOriginalFilename();
			String uploadDir = "Public/ProfilePic/";
			
			Path uploadPath = Paths.get(uploadDir);
			
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			try(InputStream inputStream = profilePic.getInputStream())
			{
				Files.copy(inputStream, Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
			}
			
			studentInfo.setProfilepic(storageFileName);
			
			attributes.addFlashAttribute("msg", "Profile Pic Successfully Updated.");
			return "redirect:/Student/UpdateProfilePic";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Error : "+e.getMessage());
			return "redirect:/Student/UpdateProfilePic";
		}
	}

	@GetMapping("/Logout")
	public String Logout() {
		if (session.getAttribute("loggedInStudent") == null) {
			return "redirect:/StudentLogin";
		}
		session.removeAttribute("loggedInStudent");
		return "redirect:/StudentLogin";
	}

}
