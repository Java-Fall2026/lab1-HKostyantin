package ua.library.model;

import ua.library.util.LibraryUtils;

public record Author(String authorCode, String name, int birthYear)
        implements Comparable<Author> {
    public Author {
        authorCode = LibraryUtils.text(authorCode, "Author code");
        name = LibraryUtils.text(name, "Author name");
        birthYear = LibraryUtils.authorBirthYear(birthYear);
    }
    /**
     * lab5
     */
    @Override
    public int compareTo(Author other) {
        int byName = name.compareTo(other.name);
        if (byName != 0) {
            return byName;
        }
        int byYear = Integer.compare(birthYear, other.birthYear);
        return byYear != 0 ? byYear : authorCode.compareTo(other.authorCode);
    }
}
