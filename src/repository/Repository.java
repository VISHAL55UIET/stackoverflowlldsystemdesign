package repository;

import java.util.List;
import java.util.Optional;

/** Minimal persistence contract. Swap the in-memory implementation for JPA/JDBC without touching services. */
public interface Repository<T> {

    T save(T entity);

    Optional<T> findById(String id);

    List<T> findAll();

    void deleteById(String id);
}
