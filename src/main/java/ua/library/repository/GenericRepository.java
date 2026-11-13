package ua.library.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GenericRepository<T> {
    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<T> extractor;
    private final Map<String, T> items = new LinkedHashMap<>();

    public GenericRepository(String name, IdentityExtractor<T> extractor) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Repository name must not be blank");
        }
        if (extractor == null) {
            throw new IllegalArgumentException("Identity extractor must not be null");
        }
        this.name = name;
        this.extractor = extractor;
    }

    private String identityOf(T item) {
        if (item == null) {
            IllegalArgumentException error = new IllegalArgumentException("Item must not be null");
            log.error("Cannot add item to repository '{}': {}", name, error.getMessage());
            throw error;
        }
        final String identity;
        try {
            identity = extractor.identityOf(item);
        } catch (RuntimeException error) {
            log.error("Failed to extract identity in repository '{}'", name, error);
            throw error;
        }
        if (identity == null || identity.isBlank()) {
            IllegalArgumentException error = new IllegalArgumentException("Item identity must not be blank");
            log.error("Cannot add item to repository '{}': {}", name, error.getMessage());
            throw error;
        }
        return identity;
    }

    public void add(T item) {
        String identity = identityOf(item);
        if (items.containsKey(identity)) {
            log.warn("Duplicate identity '{}' rejected in repository '{}'", identity, name);
            throw new DuplicateIdentityException(name, identity);
        }
        items.put(identity, item);
        log.info("Added identity '{}' to repository '{}'", identity, name);
    }

    public boolean addIfAbsent(T item) {
        String identity = identityOf(item);
        if (items.containsKey(identity)) {
            log.warn("Duplicate identity '{}' ignored in repository '{}'", identity, name);
            return false;
        }
        items.put(identity, item);
        log.info("Added identity '{}' to repository '{}'", identity, name);
        return true;
    }

    public void addAll(Collection<? extends T> values) {
        if (values == null) {
            IllegalArgumentException error = new IllegalArgumentException("Items collection must not be null");
            log.error("Cannot add collection to repository '{}': {}", name, error.getMessage());
            throw error;
        }
        for (T value : values) {
            add(value);
        }
    }

    public boolean removeByIdentity(String identity) {
        T removed = items.remove(identity);
        if (removed == null) {
            log.warn("Identity '{}' not found for removal in repository '{}'", identity, name);
            return false;
        }
        log.info("Removed identity '{}' from repository '{}'", identity, name);
        return true;
    }

    public Optional<T> findByIdentity(String identity) {
        T found = items.get(identity);
        if (found == null) {
            log.warn("Identity '{}' not found in repository '{}'", identity, name);
        } else {
            log.info("Found identity '{}' in repository '{}'", identity, name);
        }
        return Optional.ofNullable(found);
    }

    public List<T> findAll() {
        return List.copyOf(new ArrayList<>(items.values()));
    }

    public int size() {
        return items.size();
    }
}
