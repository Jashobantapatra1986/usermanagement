package com.dppm.usmt.controller;

import com.dppm.usmt.dto.UserRequest;
import com.dppm.usmt.dto.UserResponse;
import com.dppm.usmt.entity.User;
import com.dppm.usmt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<UserResponse> findAll() {
        return userService.findAll().map(this::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> findById(@PathVariable Long id) {
        return userService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponse>> create(@RequestBody UserRequest request) {
        return userService.create(toEntity(request))
                .map(this::toResponse)
                .map(savedUser -> ResponseEntity.status(HttpStatus.CREATED).body(savedUser));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> update(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.update(id, toEntity(request))
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return userService.deleteById(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCreatedDate(),
                user.getUpdatedDate(),
                user.getCreatedBy(),
                user.getUpdatedBy()
        );
    }

    private User toEntity(UserRequest request) {
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        return user;
    }
}
