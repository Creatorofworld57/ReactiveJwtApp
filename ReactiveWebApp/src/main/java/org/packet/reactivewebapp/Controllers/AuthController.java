
package org.packet.reactivewebapp.Controllers;

import lombok.RequiredArgsConstructor;
import org.packet.reactivewebapp.Entities.AuthRequest;
import org.packet.reactivewebapp.Entities.AuthResponse;
import org.packet.reactivewebapp.Entities.Token;
import org.packet.reactivewebapp.Entities.User;
import org.packet.reactivewebapp.Utils.JwtTokenUtils;
import org.packet.reactivewebapp.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {


    private final JwtTokenUtils jwtUtil;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        System.out.println(authRequest.getName());
        System.out.println(authRequest.getPassword());
        return userService.findByUsername(authRequest.getName())
                .<ResponseEntity<AuthResponse>>handle((user, sink) -> {
                    System.out.println(passwordEncoder.encode(authRequest.getPassword()));
                    if (passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
                        sink.next(ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(authRequest.getName()),jwtUtil.generateRefreshToken(authRequest.getName()))));
                    } else {
                        System.out.println("else");
                        sink.error(new BadCredentialsException("Invalid username or password"));
                    }
                }).switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")));
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<AuthResponse>> signup(@RequestBody User user) {
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setId(UUID.randomUUID());
        user.setRoles("ROLE_ADMIN");
        return userService.save(user)
                .map(savedUser -> ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(user.getName()),jwtUtil.generateRefreshToken(user.getName()))));
    }
    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<AuthResponse>> refreshAccessToken(@RequestBody Token refreshToken) {
        System.out.println(refreshToken.getToken());
      return Mono.justOrEmpty(jwtUtil.extractUserName(refreshToken.getToken()))
                .flatMap(username -> userService.findByUsername(username)
                        .flatMap(userDetails -> {
                            if (jwtUtil.validateToken(refreshToken.getToken(), userDetails)) {
                                // Генерируем новый access-токен
                                String newAccessToken = jwtUtil.generateToken(userDetails.getName());
                                // Можно вернуть и refresh-токен (если он обновляется)
                                return Mono.just(ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken.getToken())));
                            } else {
                                return Mono.error(new BadCredentialsException("Invalid refresh token"));
                            }
                        })
                ).switchIfEmpty(Mono.error(new BadCredentialsException("Invalid refresh token empty")));
    }


    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/protected")
    public Mono<ResponseEntity<String>> protectedEndpoint() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> ResponseEntity.ok("You have accessed a protected endpoint, " + context.getAuthentication().getName() + "!"));
    }
}