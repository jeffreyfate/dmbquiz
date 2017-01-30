package com.jeffthefate.dmbquiz;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by jeff on 1/28/2017.
 */

public class Report {

    public Report() {}

    private Question question;

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;

        return new EqualsBuilder()
                .append(question, report.question)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(question)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Report{" +
                "question=" + question +
                '}';
    }
}
