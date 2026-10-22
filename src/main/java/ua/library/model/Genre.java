package ua.library.model;

public enum Genre {
    FICTION(1), SCIENCE(2), HISTORY(3), BIOGRAPHY(4);

    private final int shelfNumber;

    Genre(int shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    public int getShelfNumber() {
        return shelfNumber;
    }
}
