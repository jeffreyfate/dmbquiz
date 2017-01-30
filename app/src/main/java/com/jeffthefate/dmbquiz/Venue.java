package com.jeffthefate.dmbquiz;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by jeff on 1/28/2017.
 */

public class Venue {

    public Venue() {}

    private String name;
    private String city;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Venue venue = (Venue) o;

        return new EqualsBuilder()
                .append(name, venue.name)
                .append(city, venue.city)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(city)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Venue{" +
                "name='" + name + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
