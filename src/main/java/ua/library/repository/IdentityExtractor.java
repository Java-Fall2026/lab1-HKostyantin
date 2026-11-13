package ua.library.repository;


@FunctionalInterface
public interface IdentityExtractor<T> {
    String identityOf(T item);
}
