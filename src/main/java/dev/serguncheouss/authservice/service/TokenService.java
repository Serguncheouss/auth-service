package dev.serguncheouss.authservice.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.security.auth.message.AuthException;
import org.springframework.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class TokenService {
    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;

    public TokenService(
            @Value("${jwt.secret.access}") String jwtAccessSecret,
            @Value("${jwt.secret.refresh}") String jwtRefreshSecret
    ) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
    }

    public String getUsernameFromAccessToken(String accessToken) throws AuthException {
        if (isAccessTokenValid(accessToken)) {
            final Claims claims = getAccessClaims(accessToken);
            return claims.getSubject();
        } else {
            throw new AuthException("Token is not valid.");
        }
    }

    public String getUsernameFromRefreshToken(String refreshToken) throws AuthException {
        if (isRefreshTokenValid(refreshToken)) {
            final Claims claims = getRefreshClaims(refreshToken);
            return claims.getSubject();
        } else {
            throw new AuthException("Token is not valid.");
        }
    }

    /**
     * Generates both an access and a refresh tokens from subject and puts claims to the access token.
     * Returns a string array with 2 elements.
     *
     * @param subject some {@link String} value
     * @param claims {@link Map} of claims
     * @return a {@link String[]} array with 2 elements, the first is access token and the second is refresh token.
     */
    public String[] generateTokens(@NonNull String subject, @NonNull Map<String, Object> claims) {
        final String accessToken = generateAccessToken(subject, claims);
        final String newRefreshToken = generateRefreshToken(subject);

        return new String[] {accessToken, newRefreshToken};
    }

    public String generateAccessToken(@NonNull String subject, @NonNull Map<String, Object> claims) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant = now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant(); // TODO move to config or static var
        final Date accessExpiration = Date.from(accessExpirationInstant);

        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(accessExpiration)
                .signWith(jwtAccessSecret)
                .addClaims(claims)
                .compact();
    }

    public String generateRefreshToken(@NonNull String subject) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant = now.plusDays(90).atZone(ZoneId.systemDefault()).toInstant(); // TODO move to config or static var
        final Date refreshExpiration = Date.from(refreshExpirationInstant);
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(refreshExpiration)
                .signWith(jwtRefreshSecret)
                .compact();
    }

    public boolean isAccessTokenValid(@NonNull String accessToken) {
        return validateToken(accessToken, jwtAccessSecret);
    }

    public boolean isRefreshTokenValid(@NonNull String refreshToken) {
        return validateToken(refreshToken, jwtRefreshSecret);
    }

    private boolean validateToken(@NonNull String token, @NonNull Key secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Token expired", expEx);
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt", unsEx);
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt", mjEx);
        } catch (SignatureException sEx) {
            log.error("Invalid signature", sEx);
        } catch (Exception e) {
            log.error("invalid token", e);
        }
        return false;
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    private Claims getClaims(@NonNull String token, @NonNull Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
