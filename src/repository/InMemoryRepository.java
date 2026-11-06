package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Thread-safe map-backed repository reused by all concrete repositories. */
public class InMemoryRepository<T> implements Repository<T> {

    private final Map<String, T> store = new ConcurrentHashMap<>();
    private final Function<T, String> idExtractor;

    public InMemoryRepository(Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public T save(T entity) {
        store.put(idExtractor.apply(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
