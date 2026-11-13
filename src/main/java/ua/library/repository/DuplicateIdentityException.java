package ua.library.repository;


public class DuplicateIdentityException extends IllegalStateException {
    public DuplicateIdentityException(String repositoryName, String identity) {
        super("Duplicate identity '" + identity + "' in repository '" + repositoryName + "'");
    }
}
