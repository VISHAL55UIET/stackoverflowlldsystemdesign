package repository;

import model.Tag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Tags are unique by (lower-cased) name, so the name is the id. */
public class TagRepository extends InMemoryRepository<Tag> {

    private final Map<String, Tag> byName = new ConcurrentHashMap<>();

    public TagRepository() {
        super(Tag::getName);
    }

    /** Returns the existing tag or atomically creates and stores a new one. */
    public Tag getOrCreate(String name) {
        String key = name.trim().toLowerCase();
        return byName.computeIfAbsent(key, k -> save(new Tag(k)));
    }
}
