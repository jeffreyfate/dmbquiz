package com.jeffthefate.dmbquiz;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * Created by jeff on 1/28/2017.
 */

public class Question {

    public Question() {}

    private String answer;
    private String category;
    private String question;
    private Integer score;
    private Integer trivia;
    private String objectId;
    private Date created;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTrivia() {
        return trivia;
    }

    public void setTrivia(Integer trivia) {
        this.trivia = trivia;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Question question1 = (Question) o;

        return new EqualsBuilder()
                .append(answer, question1.answer)
                .append(category, question1.category)
                .append(question, question1.question)
                .append(score, question1.score)
                .append(trivia, question1.trivia)
                .append(objectId, question1.objectId)
                .append(created, question1.created)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(answer)
                .append(category)
                .append(question)
                .append(score)
                .append(trivia)
                .append(objectId)
                .append(created)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Question{" +
                "answer='" + answer + '\'' +
                ", category='" + category + '\'' +
                ", question='" + question + '\'' +
                ", score=" + score +
                ", trivia=" + trivia +
                ", objectId='" + objectId + '\'' +
                ", created=" + created +
                '}';
    }
}
