package org.packet.reactivewebapp.Controllers;


import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import org.packet.reactivewebapp.Entities.User;
import org.packet.reactivewebapp.Repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.util.Date;

import java.util.UUID;


@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController{
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    @GetMapping("/api/user/{name}")
    public ResponseEntity<Mono<User>> getUser(@PathVariable String name){

        return ResponseEntity.ok(repository.findByName(name));
    }

    @PostMapping("/api/user")
    public Mono<ResponseEntity<String>> saveUser(@RequestParam("name") String name, @RequestParam("password") String password ) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setRoles("ADMIN");
        user.setCreated_At(new Date());
        user.setUpdated_At(new Date());


        return repository.save(user)
                .thenReturn(ResponseEntity.ok("Success"))
                .onErrorResume(error -> {
                    // Специфичная обработка ошибок
                    if (error instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.badRequest().body("Неправильные данные: " + error.getMessage()));
                    } else if (error instanceof MongoException) {
                        return Mono.just(ResponseEntity.status(500).body("Ошибка MongoDB: " + error.getMessage()));
                    } else {
                        return Mono.just(ResponseEntity.status(500).body("Произошла неизвестная ошибка: " + error.getMessage()));
                    }
                });
    }



}
