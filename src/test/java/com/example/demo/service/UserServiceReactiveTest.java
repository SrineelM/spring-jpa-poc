package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Demonstrates using StepVerifier with a blocking service by wrapping in a Mono */
@DataJpaTest
@Import({UserServiceReactiveTest.Config.class})
class UserServiceReactiveTest {

    @TestConfiguration
    static class Config {
        @Bean
        UserService userService() {
            UserRepository repo = Mockito.mock(UserRepository.class);
            Mockito.when(repo.count()).thenReturn(3L);
            return new UserService(repo);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    UserService userService;

    @Test
    void totalCount_withStepVerifier() {
        Mono<Long> countMono = Mono.fromCallable(() -> userService.getTotalUserCount());
        StepVerifier.create(countMono)
                .expectNext(3L)
                .verifyComplete();
    }
}
