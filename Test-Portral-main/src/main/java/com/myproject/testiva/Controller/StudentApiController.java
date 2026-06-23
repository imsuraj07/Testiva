package com.myproject.testiva.Controller;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myproject.testiva.Model.*;
import com.myproject.testiva.Model.TestInfo.TestStatus;
import com.myproject.testiva.Repository.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/student")
public class StudentApiController {

    @Autowired private TestInfoRepo testInfoRepo;
    @Autowired private TestQuestionRepo testQuestionRepo;
    @Autowired private QuestionBankRepo questionBankRepo;
    @Autowired private TestResultRepo testResultRepo;

    // ===================== AUTH GUARD =====================
    private StudentInfo getStudent(HttpSession session) {
        return (StudentInfo) session.getAttribute("loggedInStudent");
    }

    // ===================== DASHBOARD =====================
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpSession session) {
        StudentInfo student = getStudent(session);
        if (student == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<TestResult> results = testResultRepo.findAllByEmail(student.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("student", Map.of(
            "name", student.getName(),
            "email", student.getEmail(),
            "course", student.getCourse(),
            "branch", student.getBranch(),
            "year", student.getYear(),
            "profilepic", student.getProfilepic() != null ? student.getProfilepic() : ""
        ));
        data.put("testCount", results.size());

        // Latest result
        Optional<TestResult> latestOpt = testResultRepo.findTopByEmailOrderBySubmittedAtDesc(student.getEmail());
        if (latestOpt.isPresent()) {
            TestResult r = latestOpt.get();
            data.put("latestResult", Map.of(
                "testName", r.getTestName(),
                "totalScore", r.getTotalScore(),
                "totalMarks", r.getTotalMarks(),
                "submittedAt", r.getSubmittedAt() != null ? r.getSubmittedAt().toString() : ""
            ));
        }

        // All results
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (TestResult r : results) {
            Map<String, Object> m = new HashMap<>();
            m.put("testName", r.getTestName());
            m.put("testId", r.getTestId());
            m.put("totalScore", r.getTotalScore());
            m.put("totalMarks", r.getTotalMarks());
            m.put("submittedAt", r.getSubmittedAt() != null ? r.getSubmittedAt().toString() : "");
            resultList.add(m);
        }
        data.put("results", resultList);

        return ResponseEntity.ok(data);
    }

    // ===================== AVAILABLE TESTS =====================
    @GetMapping("/tests")
    public ResponseEntity<?> getAvailableTests(HttpSession session) {
        StudentInfo student = getStudent(session);
        if (student == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<TestInfo> allTests = testInfoRepo.findByCourseAndBranchAndYearOrderByIdDesc(
                student.getCourse(), student.getBranch(), student.getYear());

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> available = new ArrayList<>();

        for (TestInfo t : allTests) {
            if (t.getStartTime() == null) continue;
            if (t.getTestId() != null && t.getTestId().startsWith("PENDING-")) continue;

            LocalDateTime testEndTime = t.getStartTime().plusMinutes(t.getTestDuration());
            boolean show = now.isBefore(testEndTime) || now.isEqual(testEndTime);
            if (!show && t.getStatus().equals(TestStatus.Test_Over) && now.isBefore(testEndTime.plusHours(1))) {
                show = true;
            }

            if (show) {
                boolean alreadyAttempted = testResultRepo.existsByEmailAndTestId(student.getEmail(), t.getTestId());
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getId());
                m.put("testId", t.getTestId());
                m.put("testName", t.getTestName());
                m.put("course", t.getCourse());
                m.put("branch", t.getBranch());
                m.put("year", t.getYear());
                m.put("testDuration", t.getTestDuration());
                m.put("numberOfQuestions", t.getNumberOfQuestions());
                m.put("startTime", t.getStartTime().toString());
                m.put("status", t.getStatus().name());
                m.put("alreadyAttempted", alreadyAttempted);
                available.add(m);
            }
        }

        return ResponseEntity.ok(available);
    }

    // ===================== VALIDATE TEST ID =====================
    @PostMapping("/validate-test")
    public ResponseEntity<?> validateTestId(@RequestBody Map<String, String> body, HttpSession session) {
        StudentInfo student = getStudent(session);
        if (student == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        String inputTestId = body.get("testId");
        if (inputTestId != null) inputTestId = inputTestId.trim();

        TestInfo testInfo = testInfoRepo.findByTestIdIgnoreCaseOrTestNameIgnoreCase(inputTestId, inputTestId);
        if (testInfo == null) testInfo = testInfoRepo.findByTestId(inputTestId);
        if (testInfo == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Test ID '" + inputTestId + "' not found."));
        }

        // Already attempted?
        if (testResultRepo.existsByEmailAndTestId(student.getEmail(), testInfo.getTestId())) {
            return ResponseEntity.status(400).body(Map.of("error", "You have already taken this test."));
        }

        // Course/branch/year match?
        if (!student.getCourse().equals(testInfo.getCourse()) ||
            !student.getBranch().equals(testInfo.getBranch()) ||
            !student.getYear().equals(testInfo.getYear())) {
            return ResponseEntity.status(400).body(Map.of("error", "This test is not for your course/branch/year."));
        }

        // Time window check
        if (testInfo.getStartTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = testInfo.getStartTime().minusMinutes(10);
            LocalDateTime windowEnd = testInfo.getStartTime().plusMinutes(testInfo.getTestDuration());

            if (now.isBefore(windowStart)) {
                return ResponseEntity.status(400).body(Map.of("error",
                    "Test not available yet. Opens at " + windowStart.toString().replace("T", " ")));
            }
            if (now.isAfter(windowEnd)) {
                return ResponseEntity.status(400).body(Map.of("error", "Test window has ended."));
            }
        }

        if (testInfo.getStatus().equals(TestStatus.Test_Over)) {
            return ResponseEntity.status(400).body(Map.of("error", "This test has already ended."));
        }

        return ResponseEntity.ok(Map.of("valid", true, "testDbId", testInfo.getId(), "testId", testInfo.getTestId()));
    }

    // ===================== START TEST (GET QUESTIONS) =====================
    @GetMapping("/start-test/{id}")
    public ResponseEntity<?> startTest(@PathVariable long id, HttpSession session) {
        StudentInfo student = getStudent(session);
        if (student == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        TestInfo testInfo = testInfoRepo.findById(id).orElse(null);
        if (testInfo == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Test not found."));
        }

        String testId = testInfo.getTestId();

        // Already attempted?
        if (testResultRepo.existsByEmailAndTestId(student.getEmail(), testId)) {
            return ResponseEntity.status(400).body(Map.of("error", "Already attempted this test."));
        }

        // Load questions
        List<QuestionBank> qbList = testQuestionRepo.findQuestionsByTestId(testId);
        if (qbList == null || qbList.isEmpty()) {
            qbList = questionBankRepo.findAllByTest(testInfo);
        }
        if (qbList == null || qbList.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "No questions assigned to this test."));
        }

        List<Map<String, Object>> questions = new ArrayList<>();
        for (QuestionBank q : qbList) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", q.getId());
            m.put("question", q.getQuestion());
            m.put("a", q.getA());
            m.put("b", q.getB());
            m.put("c", q.getC());
            m.put("d", q.getD());
            // Do NOT send correct answer to client for security
            questions.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("questions", questions);
        data.put("testDuration", testInfo.getTestDuration());
        data.put("testName", testInfo.getTestName());
        data.put("testId", testId);
        data.put("totalQuestions", qbList.size());

        return ResponseEntity.ok(data);
    }

    // ===================== SUBMIT TEST =====================
    @PostMapping("/submit-test")
    public ResponseEntity<?> submitTest(@RequestBody Map<String, Object> body, HttpSession session) {
        StudentInfo student = getStudent(session);
        if (student == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            String testId = (String) body.get("testId");
            @SuppressWarnings("unchecked")
            Map<String, String> answers = (Map<String, String>) body.get("answers");

            TestInfo testInfo = testInfoRepo.findByTestId(testId);
            if (testInfo == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Test not found."));
            }

            // Already attempted?
            if (testResultRepo.existsByEmailAndTestId(student.getEmail(), testId)) {
                return ResponseEntity.status(400).body(Map.of("error", "Already submitted."));
            }

            // Load questions with correct answers
            List<QuestionBank> qbList = testQuestionRepo.findQuestionsByTestId(testId);
            if (qbList == null || qbList.isEmpty()) {
                qbList = questionBankRepo.findAllByTest(testInfo);
            }

            int totalMarks = qbList.size();
            int totalScore = 0;
            List<Map<String, Object>> review = new ArrayList<>();

            for (QuestionBank q : qbList) {
                String selected = answers.get(String.valueOf(q.getId()));
                boolean isCorrect = selected != null && selected.equalsIgnoreCase(q.getCorrect());
                if (isCorrect) totalScore++;

                Map<String, Object> qReview = new HashMap<>();
                qReview.put("question", q.getQuestion());
                qReview.put("a", q.getA());
                qReview.put("b", q.getB());
                qReview.put("c", q.getC());
                qReview.put("d", q.getD());
                qReview.put("correct", q.getCorrect());
                qReview.put("selected", selected != null ? selected : "");
                qReview.put("isCorrect", isCorrect);
                review.add(qReview);
            }

            // Save result
            TestResult result = new TestResult();
            result.setName(student.getName());
            result.setContactno(student.getContactno());
            result.setEmail(student.getEmail());
            result.setCourse(student.getCourse());
            result.setBranch(student.getBranch());
            result.setYear(student.getYear());
            result.setTestId(testId);
            result.setTestName(testInfo.getTestName());
            result.setTotalMarks(totalMarks);
            result.setTotalScore(totalScore);
            result.setSubmittedAt(LocalDateTime.now());
            testResultRepo.save(result);

            Map<String, Object> response = new HashMap<>();
            response.put("totalScore", totalScore);
            response.put("totalMarks", totalMarks);
            response.put("percentage", totalMarks > 0 ? Math.round(totalScore * 100.0 / totalMarks) : 0);
            response.put("testName", testInfo.getTestName());
            response.put("review", review);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ===================== RESULTS =====================
    @GetMapping("/results")
    public ResponseEntity<?> getResults(HttpSession session) {
        StudentInfo student = getStudent(session);
        if (student == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<Map<String, Object>> list = new ArrayList<>();
        for (TestResult r : testResultRepo.findAllByEmail(student.getEmail())) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("testId", r.getTestId());
            m.put("testName", r.getTestName());
            m.put("totalScore", r.getTotalScore());
            m.put("totalMarks", r.getTotalMarks());
            m.put("submittedAt", r.getSubmittedAt() != null ? r.getSubmittedAt().toString() : "");
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }
}
