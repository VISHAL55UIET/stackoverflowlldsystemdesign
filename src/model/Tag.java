package model;

import java.util.Objects;

/** A topic label attached to questions. Names are stored lower-case and are unique. */
public class Tag {

    private final String name;

    public Tag(String name) {
        this.name = name.trim().toLowerCase();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Tag t && t.name.equals(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
