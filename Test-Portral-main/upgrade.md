# Upgrade Plan: Enabling the Full Student Test Workflow

This document outlines a detailed, step-by-step plan required to make the Student Test Workflow functionally operational. Currently, the groundwork exists, but the test-taking interface ([StartTest.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/StartTest.html)), status transition logic, background active time-limiting logic, and results dashboard logic are either incomplete or missing logic.

---

## Step 1: Automated Test Status Management (Scheduler)
Currently, a test is created with the status `Scheduled`. However, the system requires a mechanism to automatically toggle the `TestStatus` to `Active` when the `startTime` is reached, and eventually to `Test_Over` when `startTime + testDuration` is reached.

**What needs to be done:**
1.  **Enable Scheduling:** Add `@EnableScheduling` to the main [Application.java](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/java/com/myproject/testiva/TestivaApplication.java) class.
2.  **Create a Scheduled Task:** Create a `TestStatusScheduler.java` `@Component` with a method annotated with `@Scheduled(fixedRate = 60000)` (runs every minute).
3.  **Update Logic:** 
    *   Query all tests where `status` is NOT `Test_Over`.
    *   If `LocalDateTime.now()` is >= `startTime` AND <= `startTime + (testDuration in hours)`, set status to `Active`.
    *   If `LocalDateTime.now()` is > `startTime + (testDuration in hours)`, set status to `Test_Over`.

---

## Step 2: Modifying [GiveTest.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/GiveTest.html) (Student Interface)
The user explicitly requested that the `Test ID` be hidden from the table and the test interface, as it is a security loop (currently visible to students).

**What needs to be done:**
1.  **Remove Test ID Column:** In [c:\Users\gsura\Desktop\Test portal\testiva\testiva\src\main\resources\templates\Student\GiveTest.html](file:///Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/Student/GiveTest.html) remove `<th>Test ID</th>` and `<td th:text="${t.testId}"></td>`.
2.  **Hide the Modals:** Remove the entire `<!-- Modal -->` block that prompts the student to manually "Enter Test ID".
3.  **Direct Action Link:** Change the "Select Test" button to bypass the modal and send the student directly to the controller:
    ```html
    <!-- New Anchor Link directly passing the test ID hidden in the URL/Controller -->
    <a th:href="@{/Student/StartTest(testid=${t.testId})}" class="btn btn-sm btn-primary">Start Test</a>
    ```

---

## Step 3: Implement [StartTest.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/StartTest.html) (The Missing Core Page)
When a student clicks "Start Test" and the [StudentController](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/java/com/myproject/testiva/Controller/StudentController.java#38-325) verifies the test is `Active`, it returns `"Student/StartTest"`. **This page currently does not exist.**

**What needs to be done:**
1.  **Create the View:** Create [c:\Users\gsura\Desktop\Test portal\testiva\testiva\src\main\resources\templates\Student\StartTest.html](file:///Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/Student/StartTest.html).
2.  **Frontend Layout Features required:**
    *   Hidden inputs capturing `json` (list of questions) passed from the model.
    *   A JavaScript controller to iterate through the questions array one at a time (Pagination vs Single View).
    *   Radio buttons representing `optionA`, `optionB`, `optionC`, and `optionD`.
    *   An array tracking what the student has answered locally.
3.  **Implement the Timer:**
    *   Read `testDuration` and `startTime`. Calculate remaining time explicitly on page load using JavaScript `setInterval`.
    *   If the timer reaches `00:00:00`, trigger an automatic Javascript form `submit()`.

---

## Step 4: Scoring and Submission Engine
When the student finishes the test (or time runs out), their answers need to be evaluated and sent to the existing [TestOver](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/java/com/myproject/testiva/Controller/StudentController.java#158-185) controller.

**What needs to be done:**
1.  **Javascript Validation:** In [StartTest.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/StartTest.html), create a Javascript function that traverses the student's chosen answers, compares them to the true `json` correct answer property, and tallies `totalMarks` and `totalScore`.
2.  **AJAX or Hidden Form POST:** Submit the `totalMarks`, `totalScore`, and `testId` to the `POST /Student/TestOver` endpoint securely.
3.  **Implement [TestOver.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/TestOver.html):** Ensure `Student/TestOver.html` exists to give them a "Thank you for completing the test, your score has been recorded." validation screen.

---

## Step 5: Dashboard Implementation
The student dashboard (`/Student/Dashboard`) is currently fetching `TestResult` objects but doesn't have a fully defined Thymeleaf View structure to present them beautifully.

**What needs to be done:**
1.  **Iterate Results:** In [c:\Users\gsura\Desktop\Test portal\testiva\testiva\src\main\resources\templates\Student\Dashboard.html](file:///Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/Student/Dashboard.html), add a `<table th:each>` loop over the `latestResult` or the collection of `testResults`.
2.  **Present Metrics:** Map variables like `Test Name`, `Total Marks`, `Scored Marks`, and `Status (Pass/Fail criteria natively)`.

---

## Summary Checklist
- [ ] Add `@EnableScheduling` and auto-status-updater logic.
- [ ] Edit [GiveTest.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/GiveTest.html) to hide the explicit `testId` and remove the Modal loop.
- [ ] Construct the crucial [StartTest.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/StartTest.html) page template with JS Timer, Question Parsing, and JS Autograder.
- [ ] Ensure [TestOver.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/student/TestOver.html) exists and handles the callback gracefully.
- [ ] Ensure [Dashboard.html](file:///c:/Users/gsura/Desktop/Test%20portal/testiva/testiva/src/main/resources/templates/Admin/Dashboard.html) loops the active scores correctly.
