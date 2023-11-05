package dev.serguncheouss.authservice.service;

import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dev.serguncheouss.authservice.model.User;

import java.util.Map;

@Service
public class CustomersService {
    @Autowired
    UserService userService;
    @Autowired
    TokenService tokenService;

    public String[] getAccessToken(String refreshToken) throws AuthException {
        final User user = getUserWithChecks(refreshToken);
        final String newAccessToken = tokenService.generateAccessToken(
                user.getUsername(),
                Map.of("roles", user.getRoles())
        );

        final User updates = new User();
        updates.setId(user.getId());
        updates.setAccessToken(newAccessToken);
        updates.setRefreshToken(refreshToken);

        userService.update(updates);

        return new String[] {newAccessToken, refreshToken};
    }

    public String[] getNewRefreshToken(String oldRefreshToken) throws AuthException {
        final User user = getUserWithChecks(oldRefreshToken);
        final String[] newTokens = tokenService.generateTokens(
                user.getUsername(),
                Map.of("roles", user.getRoles())
        );

        final User updates = new User();
        updates.setId(user.getId());
        updates.setAccessToken(newTokens[0]);
        updates.setRefreshToken(newTokens[1]);

        userService.update(updates);

        return newTokens;
    }

    private User getUserWithChecks(String refreshToken) throws AuthException {
        if (!tokenService.isRefreshTokenValid(refreshToken)) {
            throw new AuthException("Token is not valid.");
        }

        final String username = tokenService.getUsernameFromRefreshToken(refreshToken);
        final User user = userService.getByUsername(username);

        if (user == null) {
            throw new AuthException("User is not found.");
        } else if (!user.getIsActive()) {
            throw new AuthException("User is not active.");
        }

        return user;
    }
}
