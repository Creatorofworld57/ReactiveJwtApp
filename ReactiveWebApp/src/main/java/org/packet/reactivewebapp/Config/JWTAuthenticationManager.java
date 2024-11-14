package org.packet.reactivewebapp.Config;


import lombok.RequiredArgsConstructor;
import org.packet.reactivewebapp.Utils.JwtTokenUtils;
import org.packet.reactivewebapp.services.UserService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenUtils jwtUtil;
    private final UserService userService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        String token = authentication.getCredentials().toString();
        String username = jwtUtil.extractUserName(token);
        System.out.println(username + " " + token);
        return userService.findByUsername(username)
                .flatMap(userDetails -> {
                    if (jwtUtil.validateToken(token, userDetails)) {
                        System.out.println("first checkpoint");
                        // Создаём Authentication на основе валидного токена
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails, token, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                        return Mono.just(auth);
                    } else {
                        return Mono.error(new AuthenticationException("Invalid JWT token") {
                        });
                    }
                });
    }

    public ServerAuthenticationConverter authenticationConverter() {
        return exchange -> {

            String token = exchange.getRequest().getHeaders().getFirst("Authorization");

            System.out.println("Token: " + token);

            if (token != null && token.startsWith("Bearer ")) {

                token = token.substring(7);


                return Mono.just(new UsernamePasswordAuthenticationToken(jwtUtil.extractUserName(token), token, Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            }
            return Mono.empty();
        };
    }
}
