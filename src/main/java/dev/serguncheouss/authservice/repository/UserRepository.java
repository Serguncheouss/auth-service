package dev.serguncheouss.authservice.repository;

import dev.serguncheouss.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(@NonNull String name);

    boolean existsByUsername(@NonNull String username);

    void deleteByUsername(@NonNull String username);
}
