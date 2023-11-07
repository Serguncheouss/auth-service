package dev.serguncheouss.authservice.service;

import dev.serguncheouss.authservice.model.Role;
import dev.serguncheouss.authservice.model.User;
import dev.serguncheouss.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private TokenService tokenService;

    public User create(String username, String password) {
        return create(username, password, false);
    }

    public User create(String username, String password, boolean isActive) {
        String encodedPassword = passwordEncoder().encode(password);

        final User user = new User(username, encodedPassword);
        roleService.getByName("ROLE_USER").ifPresent(r -> user.setRoles(Set.of(r)));
        final String[] tokens = tokenService.generateTokens(
                user.getUsername(),
                Map.of("roles", user.getRoles())
        );
        user.setAccessToken(tokens[0]);
        user.setRefreshToken(tokens[1]);
        user.setIsActive(isActive);

        return userRepository.save(user);
    }

    @Nullable
    public User getById(@NonNull UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Nullable
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public boolean isExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean update(@NonNull User user) {
        User savedUser;

        // Get user by ID it present otherwise get user by username
        if (user.getId() != null) {
            savedUser = userRepository.findById(user.getId()).orElse(null);
        } else if (user.getUsername() != null) {
            savedUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        } else {
            return false;
        }

        if (savedUser == null) {
            return false;
        }

        final String username = user.getUsername();
        if (username != null && !username.isBlank() && !username.equalsIgnoreCase(savedUser.getUsername())) {
            savedUser.setUsername(username);
        }

        final String password = user.getPassword();
        if (password != null && !password.isBlank()) {
            savedUser.setPassword(password);
        }

        final String accessToken = user.getAccessToken();
        if (accessToken != null && !accessToken.isBlank()) {
            savedUser.setAccessToken(accessToken);
        }

        final String refreshToken = user.getRefreshToken();
        if (refreshToken != null && !refreshToken.isBlank()) {
            savedUser.setRefreshToken(refreshToken);
        }

        final Boolean isActive = user.getIsActive();
        if (isActive != null) {
            savedUser.setIsActive(isActive);
        }

        final Set<Role> roles = user.getRoles();
        if (roles != null && roles.size() > 0) {
            savedUser.setRoles(roles);
        }

        userRepository.save(savedUser);

        return true;
    }

    public boolean delete(String username) {
        if (isExists(username)) {
            userRepository.deleteByUsername(username);

            return true;
        }

        return false;
    }

    public boolean delete(UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);

            return true;
        }

        return false;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
