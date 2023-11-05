package dev.serguncheouss.authservice.config;

import dev.serguncheouss.authservice.model.Role;
import dev.serguncheouss.authservice.model.User;
import dev.serguncheouss.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    UserService userService;

    @Bean
    public UserDetailsManager userDetailsManager() {
        return new UserDetailsManager() {
            @Override
            public void createUser(UserDetails user) {
                userService.create(user.getUsername(), user.getPassword());
            }

            @Override
            public void updateUser(UserDetails user) {
                final User updates = new User(user.getUsername(), user.getPassword());

                updates.setIsActive(
                        user.isEnabled() &&
                        user.isAccountNonExpired() &&
                        user.isAccountNonLocked() &&
                        user.isCredentialsNonExpired());

                userService.update(updates);
            }

            @Override
            public void deleteUser(String username) {
                userService.delete(username);
            }

            @Override
            public void changePassword(String oldPassword, String newPassword) {
                Authentication currentUser = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
                if (currentUser == null) {
                    throw new AccessDeniedException("Can't change password as no Authentication object found in context for current user.");
                } else {
                    String username = currentUser.getName();

                    final User updates = new User(username, newPassword);
                    userService.update(updates);
                }
            }

            @Override
            public boolean userExists(String username) {
                return userService.isExists(username);
            }

            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                final User savedUser = userService.getByUsername(username);

                if (savedUser == null) {
                    throw new UsernameNotFoundException(username);
                }

                return org.springframework.security.core.userdetails.User.withUsername(username)
                        .password(savedUser.getPassword())
                        .roles(savedUser.getRoles().stream()
                                .map(Role::getName)
                                .toArray(String[]::new)
                        )
                        .disabled(!savedUser.getIsActive())
                        .accountExpired(!savedUser.getIsActive())
                        .credentialsExpired(!savedUser.getIsActive())
                        .accountLocked(!savedUser.getIsActive())
                        .build();
            }
        };
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                                .requestMatchers("/api/docs", "/api/health/v1/status", "/api/clients/v1/*").permitAll()
                                .anyRequest().authenticated()
                )
                .build();
    }
}
