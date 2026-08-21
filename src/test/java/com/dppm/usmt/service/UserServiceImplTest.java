package com.dppm.usmt.service;

import com.dppm.usmt.entity.User;
import com.dppm.usmt.exception.ResourceNotFoundException;
import com.dppm.usmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void findAllReturnsUsersFromRepository() {
        when(userRepository.findAll()).thenReturn(Flux.just(user(1L), user(2L)));

        StepVerifier.create(userService.findAll())
                .expectNextCount(2)
                .verifyComplete();

        verify(userRepository).findAll();
    }

    @Test
    void findByIdReturnsUserWhenPresent() {
        User expectedUser = user(1L);
        when(userRepository.findById(1L)).thenReturn(Mono.just(expectedUser));

        StepVerifier.create(userService.findById(1L))
                .expectNext(expectedUser)
                .verifyComplete();
    }

    @Test
    void findByIdReturnsNotFoundErrorWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.findById(99L))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResourceNotFoundException.class);
                    assertThat(error).hasMessage("User not found with id: 99");
                })
                .verify();
    }

    @Test
    void createSetsAuditDatesClearsIdAndSavesUser() {
        User user = user(10L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userService.create(user))
                .assertNext(savedUser -> {
                    assertThat(savedUser.getId()).isNull();
                    assertThat(savedUser.getCreatedDate()).isNotNull();
                    assertThat(savedUser.getUpdatedDate()).isEqualTo(savedUser.getCreatedDate());
                })
                .verifyComplete();

        verify(userRepository).save(user);
    }

    @Test
    void updatePreservesIdAndCreatedDateThenUpdatesTimestamp() {
        LocalDateTime createdDate = LocalDateTime.of(2026, 1, 1, 10, 0);
        User existingUser = user(1L);
        existingUser.setCreatedDate(createdDate);
        User requestedUser = user(null);
        requestedUser.setFirstName("Updated");
        when(userRepository.findById(1L)).thenReturn(Mono.just(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userService.update(1L, requestedUser))
                .assertNext(updatedUser -> {
                    assertThat(updatedUser.getId()).isEqualTo(1L);
                    assertThat(updatedUser.getCreatedDate()).isEqualTo(createdDate);
                    assertThat(updatedUser.getUpdatedDate()).isNotNull();
                    assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
                })
                .verifyComplete();
    }

    @Test
    void updateReturnsNotFoundErrorWhenUserDoesNotExist() {
        User requestedUser = user(null);
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.update(99L, requestedUser))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteByIdDeletesExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(user(1L)));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteById(1L)).verifyComplete();

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteByIdReturnsNotFoundErrorWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteById(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(userRepository, never()).deleteById(99L);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@example.com");
        return user;
    }
}
