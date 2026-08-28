package com.dppm.usmt.controller;

import com.dppm.usmt.dto.UserResponse;
import com.dppm.usmt.dto.UserRequest;
import com.dppm.usmt.entity.User;
import com.dppm.usmt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void findAllMapsUsersToResponses() {
        when(userService.findAll()).thenReturn(Flux.just(user(1L), user(2L)));

        List<UserResponse> response = userController.findAll().collectList().block();

        assertThat(response).hasSize(2);
        assertThat(response.getFirst().id()).isEqualTo(1L);
        assertThat(response.getFirst().email()).isEqualTo("jane.doe@example.com");
        verify(userService).findAll();
    }

    @Test
    void findByIdReturnsMappedOkResponse() {
        when(userService.findById(1L)).thenReturn(Mono.just(user(1L)));

        ResponseEntity<UserResponse> response = userController.findById(1L).block();

        assert response != null;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().id()).isEqualTo(1L);
    }

    @Test
    void createReturnsCreatedMappedResponse() {
        UserRequest request = request();
        User savedUser = user(1L);
        when(userService.create(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(Mono.just(savedUser));

        ResponseEntity<UserResponse> response = userController.create(request).block();

        assert response != null;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assert response.getBody() != null;
        assertThat(response.getBody().id()).isEqualTo(1L);
        verify(userService).create(org.mockito.ArgumentMatchers.argThat(user ->
                user.getFirstName().equals("Jane")
                        && user.getLastName().equals("Doe")
                        && user.getEmail().equals("jane.doe@example.com")
        ));
    }

    @Test
    void updateReturnsMappedOkResponse() {
        UserRequest request = request();
        User updatedUser = user(1L);
        updatedUser.setFirstName("Updated");
        when(userService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(User.class)))
                .thenReturn(Mono.just(updatedUser));

        ResponseEntity<UserResponse> response = userController.update(1L, request).block();

        assert response != null;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().firstName()).isEqualTo("Updated");
        verify(userService).update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.argThat(user ->
                user.getFirstName().equals("Jane")
                        && user.getLastName().equals("Doe")
                        && user.getEmail().equals("jane.doe@example.com")
        ));
    }

    @Test
    void deleteReturnsNoContentResponse() {
        when(userService.deleteById(1L)).thenReturn(Mono.empty());

        ResponseEntity<String> response = userController.delete(1L).block();

        assert response != null;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).deleteById(1L);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@example.com");
        user.setCreatedDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        user.setUpdatedDate(LocalDateTime.of(2026, 1, 2, 10, 0));
        user.setCreatedBy(1L);
        user.setUpdatedBy(2L);
        return user;
    }

    private UserRequest request() {
        return new UserRequest("Jane", "Doe", "jane.doe@example.com");
    }
}
