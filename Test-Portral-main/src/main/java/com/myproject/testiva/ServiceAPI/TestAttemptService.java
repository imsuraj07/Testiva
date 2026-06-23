package com.myproject.testiva.ServiceAPI;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myproject.testiva.Model.QuestionBank;
import com.myproject.testiva.Model.StudentAnswer;
import com.myproject.testiva.Model.StudentInfo;
import com.myproject.testiva.Model.TestAttempt;
import com.myproject.testiva.Model.TestInfo;
import com.myproject.testiva.Repository.QuestionBankRepo;
import com.myproject.testiva.Repository.StudentAnswerRepo;
import com.myproject.testiva.Repository.TestAttemptRepo;

@Service
public class TestAttemptService {

    @Autowired
    private TestAttemptRepo testAttemptRepo;

    @Autowired
    private StudentAnswerRepo studentAnswerRepo;

    @Autowired
    private QuestionBankRepo questionRepo;

    public TestAttempt submitTest(
            StudentInfo student,
            TestInfo test,
            Map<Long, String> answers) {

        int score = 0;

        // 1️⃣ Create attempt
        TestAttempt attempt = new TestAttempt();
        attempt.setStudent(student);
        attempt.setTest(test);
        attempt.setSubmittedAt(LocalDateTime.now());

        attempt = testAttemptRepo.save(attempt);

        // 2️⃣ Fetch questions
        List<QuestionBank> questions = questionRepo.findById(test);

        // 3️⃣ Check answers
        for (QuestionBank q : questions) {
            String selected = answers.get(q.getId());

            if (selected != null && selected.equals(q.getCorrect().equals(selected))) {
                score++;
            }

            StudentAnswer sa = new StudentAnswer();
            sa.setTestAttempt(attempt);
            sa.setQuestions(q);
            sa.setSelectedOption(selected);

            studentAnswerRepo.save(sa);
        }

        // 4️⃣ Save score
        attempt.setScore(score);
        return testAttemptRepo.save(attempt);
    }
}