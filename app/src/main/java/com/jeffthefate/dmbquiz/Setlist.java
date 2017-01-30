package com.jeffthefate.dmbquiz;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * Created by jeff on 1/28/2017.
 */

public class Setlist {

    public Setlist() {}

    private String set;
    private Date setDate;
    private Venue venue;
    private Date updated;

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public Date getSetDate() {
        return setDate;
    }

    public void setSetDate(Date setDate) {
        this.setDate = setDate;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Setlist setlist = (Setlist) o;

        return new EqualsBuilder()
                .append(set, setlist.set)
                .append(setDate, setlist.setDate)
                .append(venue, setlist.venue)
                .append(updated, setlist.updated)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(set)
                .append(setDate)
                .append(venue)
                .append(updated)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Setlist{" +
                "set='" + set + '\'' +
                ", setDate=" + setDate +
                ", venue=" + venue +
                ", updated=" + updated +
                '}';
    }
}
