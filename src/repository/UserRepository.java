package repository;

import model.User;

import java.util.Optional;

public class UserRepository extends InMemoryRepository<User> {

    public UserRepository() {
        super(User::getId);
    }

    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
