package com.carbontrade.repository;

import com.carbontrade.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists by email.
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists by username.
     */
    boolean existsByUsername(String username);

    /**
     * Find all users with a given role (if your User model supports roles).
     */
    List<User> findByRole(String role);

    /**
     * Optional: Retrieve active/inactive users (if you have a boolean status field).
     */
    List<User> findByActive(boolean active);

    /**
     * Optional: case-insensitive search by username (useful for autocomplete).
     */
    List<User> findByUsernameContainingIgnoreCase(String partial);

    /**
     * Optional: find user by both email and username 
     * (useful for login workflows or duplicate checks).
     */
    Optional<User> findByEmailAndUsername(String email, String username);
}
