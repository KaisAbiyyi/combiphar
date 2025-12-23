package com.combiphar.core.service;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Service for authentication and registration logic.
 */
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user by email and password.
     */
    public Optional<User> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        return userRepository.findByEmail(email)
                .filter(user -> BCrypt.checkpw(password, user.getPassword()));
    }

    /**
     * Registers a new customer.
     */
    public void registerCustomer(String name, String email, String password) {
        validateRegistration(name, email, password);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(null, name, email, hashedPassword, Role.CUSTOMER);
        userRepository.save(user);
    }

    private void validateRegistration(String name, String email, String password) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters");
    }
}
