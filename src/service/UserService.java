package service;

import model.User;
import repository.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Email must be unique; check-and-save is atomic. */
    public synchronized User register(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already registered: " + email);
        }
        return userRepository.save(new User(name.trim(), email.trim()));
    }

    public User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
