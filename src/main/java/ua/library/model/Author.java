package ua.library.model;

import ua.common.BaseEntity;
import ua.library.util.LibraryUtils;

public final class Author extends BaseEntity {
    private final String authorCode;
    private final String name;
    private final int birthYear;

    public Author(String authorCode, String name, int birthYear) {
        super();
        this.authorCode = LibraryUtils.text(authorCode, "Код автора");
        this.name = LibraryUtils.text(name, "Ім’я автора");
        this.birthYear = LibraryUtils.authorBirthYear(birthYear);
    }

    public String getAuthorCode() {
        return authorCode;
    }

    public String getName() {
        return name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Author author = (Author) other;
        return authorCode.equals(author.authorCode);
    }

    @Override
    public int hashCode() {
        return authorCode.hashCode();
    }

    @Override
    public String toString() {
        return "Автор{код автора='" + authorCode + "', ім’я='" + name
                + "', рік народження=" + birthYear + ", створено=" + createdAt + "}";
    }
}
