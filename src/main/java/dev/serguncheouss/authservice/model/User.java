package dev.serguncheouss.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "users", schema = "v1", indexes = {
        @Index(name = "ix_username", columnList = "username")
})
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @Getter
    @Setter
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Getter
    @Setter
    @Column(unique = true)
    private String username;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String accessToken;
    @Getter
    @Setter
    private String refreshToken;
    @Getter
    @Setter
    private Boolean isActive;
    @Getter
    @CreatedDate
    private Instant createDate;
    @Getter
    @LastModifiedDate
    private Instant updateDate;
    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}