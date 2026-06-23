package com.myproject.testiva.ServiceAPI;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.myproject.testiva.Model.QuestionBank;
import com.myproject.testiva.Repository.QuestionBankRepo;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private QuestionBankRepo questionBankRepo;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Drop redundant column if it exists to fix whitelabel/SQL errors
        try {
            jdbcTemplate.execute("ALTER TABLE test_info DROP COLUMN number_of_question");
            System.err.println("✅ Redundant column 'number_of_question' dropped successfully.");
        } catch (Exception e) {
            // Already dropped or doesn't exist, ignore
        }

        // Only run if the table is empty
        if (questionBankRepo.count() == 0) {
            List<QuestionBank> questions = new ArrayList<>();
            
            for (int i = 1; i <= 20; i++) {
                QuestionBank qb = new QuestionBank();
                qb.setQuestion("Sample Question " + i + ": What is the result of " + i + " + " + i + "?");
                qb.setA(String.valueOf((i + i) - 1));
                // B is always correct for simplicity
                qb.setB(String.valueOf(i + i));
                qb.setC(String.valueOf((i + i) + 1));
                qb.setD(String.valueOf((i + i) + 2));
                qb.setCorrect("B");
                
                // Assigning some default metadata so tests can group them
                qb.setCourse("B.Tech");
                qb.setBranch("Computer Science");
                qb.setYear("Third Year");
                
                questions.add(qb);
            }
            
            questionBankRepo.saveAll(questions);
            System.err.println("✅ Included 20 sample questions in QuestionBank successfully!");
        }
    }
}
