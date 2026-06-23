package com.myproject.testiva.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_questions")
public class TestQuestionMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "test_id", referencedColumnName = "testId", nullable = false)
    private TestInfo test;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionBank question;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TestInfo getTest() {
        return test;
    }

    public void setTest(TestInfo test) {
        this.test = test;
    }

    public QuestionBank getQuestion() {
        return question;
    }

    public void setQuestion(QuestionBank question) {
        this.question = question;
    }
}
