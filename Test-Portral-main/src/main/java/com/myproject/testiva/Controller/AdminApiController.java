package com.myproject.testiva.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.myproject.testiva.Model.*;
import com.myproject.testiva.Model.StudentInfo.UserRole;
import com.myproject.testiva.Model.StudentInfo.UserStatus;
import com.myproject.testiva.Model.TestInfo.TestStatus;
import com.myproject.testiva.Repository.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired private StudentInfoRepo userRepo;
    @Autowired private QuestionBankRepo qbRepo;
    @Autowired private TestInfoRepo testInfoRepo;
    @Autowired private TestQuestionRepo testQuestionRepo;
    @Autowired private TestResultRepo testResultRepo;
    @Autowired private EnquiryRepo enquiryRepo;

    // ===================== AUTH GUARD =====================
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("loggedInAdmin") != null;
    }

    // ===================== DASHBOARD =====================
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", userRepo.count() - 1);
        data.put("totalTests", testInfoRepo.count());
        data.put("totalResults", testResultRepo.count());
        data.put("totalEnquiries", enquiryRepo.count());
        data.put("totalQuestions", qbRepo.count());

        List<Map<String, Object>> recentTests = new ArrayList<>();
        for (TestInfo t : testInfoRepo.findAll()) {
            if (t.getTestId() != null && !t.getTestId().startsWith("PENDING-")) {
                Map<String, Object> tm = new HashMap<>();
                tm.put("id", t.getId());
                tm.put("testId", t.getTestId());
                tm.put("testName", t.getTestName());
                tm.put("course", t.getCourse());
                tm.put("branch", t.getBranch());
                tm.put("year", t.getYear());
                tm.put("status", t.getStatus().name());
                tm.put("startTime", t.getStartTime());
                tm.put("testDuration", t.getTestDuration());
                tm.put("numberOfQuestions", t.getNumberOfQuestions());
                recentTests.add(tm);
            }
        }
        data.put("recentTests", recentTests);

        List<Map<String, Object>> recentEnquiries = new ArrayList<>();
        for (Enquiry e : enquiryRepo.findTop5ByOrderBySubmittedAt()) {
            Map<String, Object> em = new HashMap<>();
            em.put("id", e.getId());
            em.put("name", e.getName());
            em.put("email", e.getEmail());
            em.put("status", e.getStatus().name());
            em.put("submittedAt", e.getSubmittedAt());
            recentEnquiries.add(em);
        }
        data.put("recentEnquiries", recentEnquiries);

        return ResponseEntity.ok(data);
    }

    // ===================== STUDENTS =====================
    @GetMapping("/students")
    public ResponseEntity<?> getStudents(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<Map<String, Object>> list = new ArrayList<>();
        for (StudentInfo s : userRepo.findAllByRole(UserRole.STUDENT)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("email", s.getEmail());
            m.put("contactno", s.getContactno());
            m.put("course", s.getCourse());
            m.put("branch", s.getBranch());
            m.put("year", s.getYear());
            m.put("status", s.getStatus().name());
            m.put("regdate", s.getRegdate());
            m.put("profilepic", s.getProfilepic());
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }

    @PutMapping("/students/{id}/status")
    public ResponseEntity<?> toggleStudentStatus(@PathVariable long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            StudentInfo student = userRepo.findById(id).orElse(null);
            if (student == null) return ResponseEntity.status(404).body(Map.of("error", "Student not found"));

            if (student.getStatus().equals(UserStatus.PENDING)) {
                student.setStatus(UserStatus.VERIFIED);
            } else if (student.getStatus().equals(UserStatus.VERIFIED)) {
                student.setStatus(UserStatus.DISABLED);
            } else if (student.getStatus().equals(UserStatus.DISABLED)) {
                student.setStatus(UserStatus.VERIFIED);
            }
            userRepo.save(student);
            return ResponseEntity.ok(Map.of("message", "Status updated", "newStatus", student.getStatus().name()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ===================== TESTS =====================
    @GetMapping("/tests")
    public ResponseEntity<?> getTests(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<Map<String, Object>> list = new ArrayList<>();
        List<TestInfo> tests = testInfoRepo.findAll();
        Collections.reverse(tests);
        for (TestInfo t : tests) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("testId", t.getTestId());
            m.put("testName", t.getTestName());
            m.put("course", t.getCourse());
            m.put("branch", t.getBranch());
            m.put("year", t.getYear());
            m.put("testDuration", t.getTestDuration());
            m.put("numberOfQuestions", t.getNumberOfQuestions());
            m.put("startTime", t.getStartTime());
            m.put("status", t.getStatus().name());
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/tests")
    public ResponseEntity<?> scheduleTest(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            TestInfo testInfo = new TestInfo();
            String pendingTestId = "PENDING-" + UUID.randomUUID().toString().substring(0, 8);
            testInfo.setTestId(pendingTestId);
            testInfo.setTestName((String) body.get("testName"));
            testInfo.setCourse((String) body.get("course"));
            testInfo.setBranch((String) body.get("branch"));
            testInfo.setYear((String) body.get("year"));
            testInfo.setTestDuration(Integer.parseInt(body.get("testDuration").toString()));
            testInfo.setNumberOfQuestions(Integer.parseInt(body.get("numberOfQuestions").toString()));
            testInfo.setStartTime(java.time.LocalDateTime.parse((String) body.get("startTime")));
            testInfo.setStatus(TestStatus.Scheduled);
            testInfoRepo.save(testInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test scheduled. Select questions next.");
            response.put("pendingTestId", pendingTestId);
            response.put("requiredQuestions", testInfo.getNumberOfQuestions());
            response.put("id", testInfo.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/tests/{id}")
    public ResponseEntity<?> updateTest(@PathVariable long id, @RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            TestInfo existing = testInfoRepo.findById(id).orElse(null);
            if (existing == null) return ResponseEntity.status(404).body(Map.of("error", "Test not found"));

            existing.setTestName((String) body.get("testName"));
            existing.setCourse((String) body.get("course"));
            existing.setBranch((String) body.get("branch"));
            existing.setYear((String) body.get("year"));
            existing.setTestDuration(Integer.parseInt(body.get("testDuration").toString()));
            existing.setNumberOfQuestions(Integer.parseInt(body.get("numberOfQuestions").toString()));
            if (body.get("startTime") != null) {
                existing.setStartTime(java.time.LocalDateTime.parse((String) body.get("startTime")));
            }
            testInfoRepo.save(existing);
            return ResponseEntity.ok(Map.of("message", "Test updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/tests/{id}")
    public ResponseEntity<?> deleteTest(@PathVariable long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            TestInfo testInfo = testInfoRepo.findById(id).orElse(null);
            if (testInfo != null) {
                List<TestQuestionMapping> mappings = testQuestionRepo.findAll();
                for (TestQuestionMapping mapping : mappings) {
                    if (mapping.getTest().getId() == id) {
                        testQuestionRepo.delete(mapping);
                    }
                }
                testInfoRepo.deleteById(id);
            }
            return ResponseEntity.ok(Map.of("message", "Test deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ===================== QUESTIONS =====================
    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<Map<String, Object>> list = new ArrayList<>();
        for (QuestionBank q : qbRepo.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", q.getId());
            m.put("question", q.getQuestion());
            m.put("a", q.getA());
            m.put("b", q.getB());
            m.put("c", q.getC());
            m.put("d", q.getD());
            m.put("correct", q.getCorrect());
            m.put("course", q.getCourse());
            m.put("branch", q.getBranch());
            m.put("year", q.getYear());
            if (q.getTest() != null) {
                m.put("testId", q.getTest().getTestId());
                m.put("testName", q.getTest().getTestName());
            }
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/questions")
    public ResponseEntity<?> addQuestion(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            QuestionBank qb = new QuestionBank();
            qb.setQuestion((String) body.get("question"));
            qb.setA((String) body.get("a"));
            qb.setB((String) body.get("b"));
            qb.setC((String) body.get("c"));
            qb.setD((String) body.get("d"));
            qb.setCorrect((String) body.get("correct"));
            qb.setCourse((String) body.get("course"));
            qb.setBranch((String) body.get("branch"));
            qb.setYear((String) body.get("year"));

            if (body.get("testId") != null) {
                TestInfo test = testInfoRepo.findByTestId((String) body.get("testId"));
                if (test != null) qb.setTest(test);
            }

            qbRepo.save(qb);
            return ResponseEntity.ok(Map.of("message", "Question added successfully", "id", qb.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/questions/upload")
    public ResponseEntity<?> uploadQuestions(
            @RequestParam("questionFile") MultipartFile csvFile,
            @RequestParam("course") String course,
            @RequestParam("branch") String branch,
            @RequestParam("year") String year,
            @RequestParam(value = "testId", required = false) String testId,
            HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            if (csvFile.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));

            TestInfo test = null;
            if (testId != null && !testId.isEmpty()) {
                test = testInfoRepo.findByTestId(testId);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
                String line;
                reader.readLine(); // skip header
                List<QuestionBank> qbList = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    String[] qbData = line.split(",");
                    if (qbData.length == 6) {
                        QuestionBank qb = new QuestionBank();
                        qb.setQuestion(qbData[0]);
                        qb.setA(qbData[1]);
                        qb.setB(qbData[2]);
                        qb.setC(qbData[3]);
                        qb.setD(qbData[4]);
                        qb.setCorrect(qbData[5]);
                        qb.setCourse(course);
                        qb.setBranch(branch);
                        qb.setYear(year);
                        if (test != null) qb.setTest(test);
                        qbList.add(qb);
                    }
                }
                qbRepo.saveAll(qbList);
                return ResponseEntity.ok(Map.of("message", "Uploaded " + qbList.size() + " questions"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            qbRepo.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Question deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/questions/assign")
    public ResponseEntity<?> assignQuestions(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        try {
            String testId = (String) body.get("testId");
            @SuppressWarnings("unchecked")
            List<Integer> questionIds = (List<Integer>) body.get("questionIds");

            TestInfo test = testInfoRepo.findByTestId(testId);
            if (test == null) return ResponseEntity.status(404).body(Map.of("error", "Test not found"));

            if (questionIds.size() != test.getNumberOfQuestions()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Expected " + test.getNumberOfQuestions() + " questions, got " + questionIds.size()));
            }

            // Generate real Test ID if pending
            if (test.getTestId().startsWith("PENDING-")) {
                long maxId = testInfoRepo.findTopByOrderByIdDesc().map(TestInfo::getId).orElse(0L);
                String generatedTestId = "TTP" + String.format("%03d", maxId + 1);
                test.setTestId(generatedTestId);
                testInfoRepo.save(test);
            }

            List<QuestionBank> selectedQuestions = qbRepo.findAllById(
                questionIds.stream().map(Long::valueOf).toList());
            List<TestQuestionMapping> mappings = new ArrayList<>();
            for (QuestionBank qb : selectedQuestions) {
                TestQuestionMapping mapping = new TestQuestionMapping();
                mapping.setTest(test);
                mapping.setQuestion(qb);
                mappings.add(mapping);
            }
            testQuestionRepo.saveAll(mappings);

            return ResponseEntity.ok(Map.of("message", "Questions assigned", "testId", test.getTestId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ===================== RESULTS =====================
    @GetMapping("/results")
    public ResponseEntity<?> getResults(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<Map<String, Object>> list = new ArrayList<>();
        for (TestResult r : testResultRepo.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("email", r.getEmail());
            m.put("contactno", r.getContactno());
            m.put("course", r.getCourse());
            m.put("branch", r.getBranch());
            m.put("year", r.getYear());
            m.put("testId", r.getTestId());
            m.put("testName", r.getTestName());
            m.put("totalScore", r.getTotalScore());
            m.put("totalMarks", r.getTotalMarks());
            m.put("submittedAt", r.getSubmittedAt());
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }

    // ===================== ENQUIRIES =====================
    @GetMapping("/enquiries")
    public ResponseEntity<?> getEnquiries(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<Map<String, Object>> list = new ArrayList<>();
        for (Enquiry e : enquiryRepo.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("name", e.getName());
            m.put("email", e.getEmail());
            m.put("contactno", e.getContactno());
            m.put("gender", e.getGender());
            m.put("subject", e.getSubject());
            m.put("enquiryText", e.getEnquiryText());
            m.put("status", e.getStatus().name());
            m.put("submittedAt", e.getSubmittedAt());
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }
}
