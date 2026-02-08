package com.portfolio.api.repository;

import com.portfolio.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for User entity operations.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderIdAndAuthProvider(String providerId, com.portfolio.api.model.AuthProvider authProvider);
}
