package com.myproject.testiva.Repository;

import com.myproject.testiva.Model.TestQuestionMapping;
import com.myproject.testiva.Model.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestQuestionRepo extends JpaRepository<TestQuestionMapping, Long> {
    @Query("SELECT mapping.question FROM TestQuestionMapping mapping WHERE mapping.test.testId = :testId")
    List<QuestionBank> findQuestionsByTestId(@Param("testId") String testId);

    void deleteByTest(com.myproject.testiva.Model.TestInfo test);
}
