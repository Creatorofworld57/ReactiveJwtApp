package org.packet.reactivewebapp.services;



import lombok.RequiredArgsConstructor;
import org.packet.reactivewebapp.Entities.User;
import org.packet.reactivewebapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {


    private final  UserRepository userRepository;

    public Mono<User> findByUsername(String username) {
        return userRepository.findByName(username);
    }

    public Mono<User> save(User user) {
        user.setPassword(user.getPassword()); // Encrypt password before saving
        return userRepository.save(user);
    }
}