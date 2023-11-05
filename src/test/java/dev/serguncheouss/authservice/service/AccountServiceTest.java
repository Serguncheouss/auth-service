package dev.serguncheouss.authservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static dev.serguncheouss.authservice.MockDataProvider.EMAIL;
import static dev.serguncheouss.authservice.MockDataProvider.PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountServiceTest {
    @Autowired
    UserService userService;

    @Test
    void testCreate() {
        var user = userService.create(EMAIL, PASSWORD);

        assertThat(user).isNotNull();
    }
}
