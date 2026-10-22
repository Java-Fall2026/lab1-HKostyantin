package ua.library.model;

import ua.library.util.LibraryUtils;

public record Author(String authorCode, String name, int birthYear) {
    public Author {
        authorCode = LibraryUtils.text(authorCode, "Author code");
        name = LibraryUtils.text(name, "Author name");
        birthYear = LibraryUtils.authorBirthYear(birthYear);
    }
}
