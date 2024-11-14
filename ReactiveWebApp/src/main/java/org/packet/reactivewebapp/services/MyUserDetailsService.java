package org.packet.reactivewebapp.services;


import lombok.AllArgsConstructor;

import org.packet.reactivewebapp.Repositories.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;





public class MyUserDetailsService {
/*
    private UserRepository repository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.findByName(username)
                .map(UserDetailsImpl::new); // Преобразуем объект User в UserDetailsImpl
    }

 */
}