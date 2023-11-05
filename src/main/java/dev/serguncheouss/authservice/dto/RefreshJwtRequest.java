package dev.serguncheouss.authservice.dto;

import lombok.Getter;

@Getter
public class RefreshJwtRequest {
    private String refreshToken;
}
