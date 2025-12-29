package com.combiphar.core.service;

import java.util.Objects;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.UserRepository;

/**
 * Service responsible for authentication and user registration. Follows SOLID
 * principles by focusing solely on auth business logic.
 */
public class AuthService {

    private final UserRepository userRepository;

    /**
     * Constructor injection for UserRepository.
     *
     * @param userRepository the repository to access user data
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    /**
     * Authenticates a user based on email and password. Defensive: handles null
     * inputs gracefully.
     *
     * @param email the user's email
     * @param password the user's plain text password
     * @return an Optional containing the User if authenticated, otherwise empty
     */
    public Optional<User> login(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(email)
                .filter(user -> BCrypt.checkpw(password, user.getPassword()));
    }

    /**
     * Registers a new customer in the system. Defensive: validates all inputs
     * before processing.
     *
     * @param name the customer's full name
     * @param email the customer's email
     * @param password the customer's plain text password
     * @throws IllegalArgumentException if validation fails or email is already
     * taken
     */
    public void registerCustomer(String name, String email, String password) {
        validateRegistrationInput(name, email, password);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email sudah terdaftar. Silakan gunakan email lain.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(null, name, email, hashedPassword, Role.CUSTOMER);
        newUser.setStatus("ACTIVE");

        userRepository.save(newUser);
    }

    /**
     * Validates registration inputs. Defensive programming: fail fast with
     * clear messages.
     */
    private void validateRegistrationInput(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama lengkap wajib diisi.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password minimal harus 6 karakter.");
        }
    }
}
