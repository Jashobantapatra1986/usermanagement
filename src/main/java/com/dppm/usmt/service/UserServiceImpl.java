package com.dppm.usmt.service;

import com.dppm.usmt.entity.User;
import com.dppm.usmt.exception.ResourceNotFoundException;
import com.dppm.usmt.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> findById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found with id: " + id)));
    }

    @Override
    public Mono<User> create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setId(null);
        user.setCreatedDate(now);
        user.setUpdatedDate(now);
        return userRepository.save(user);
    }

    @Override
    public Mono<User> update(Long id, User user) {
        return findById(id)
                .flatMap(existing -> {
                    user.setId(existing.getId());
                    user.setCreatedDate(existing.getCreatedDate());
                    user.setUpdatedDate(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return findById(id).flatMap(user -> userRepository.deleteById(id));
    }
}
