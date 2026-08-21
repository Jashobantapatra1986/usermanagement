package com.dppm.usmt.service;

import com.dppm.usmt.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Flux<User> findAll();

    Mono<User> findById(Long id);

    Mono<User> create(User user);

    Mono<User> update(Long id, User user);

    Mono<Void> deleteById(Long id);
}
