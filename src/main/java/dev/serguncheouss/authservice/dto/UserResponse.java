package dev.serguncheouss.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import dev.serguncheouss.authservice.model.User;

@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String accessToken;
    private String refreshToken;
    private boolean isActive;
    private String createDate;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getAccessToken(),
                user.getRefreshToken(),
                user.getIsActive(),
                user.getCreateDate().toString()
        );
    }
}
