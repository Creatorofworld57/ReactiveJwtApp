package org.packet.reactivewebapp.Config;

import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final JWTAuthenticationManager jwtAuthenticationManager; // Используем финальный модификатор для thread-safety

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Отключаем CSRF
                .authenticationManager(jwtAuthenticationManager) // Указываем кастомный authenticationManager
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/refresh-token",
                                "/login", "/signup", "/api/audio/**", "/api/audioName/**",
                                "/api/authorization", "/api/user",
                                "/api/user/{name}", "/login/oauth2/git", "/login/oauth2/code/github",
                                "/api/audioCount", "/api/user/withGithub/{id}", "/api/searchOfTrack/{name}")
                        .permitAll()
                        .pathMatchers("/protected", "/api/**","/api/audio").authenticated()
                )
                .cors(corsSpec -> corsSpec.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.addAllowedOrigin("https://localhost:3000"); // Лучше использовать переменные окружения для CORS
                    corsConfig.addAllowedHeader("*");
                    corsConfig.setAllowedMethods(List.of(
                            HttpMethod.GET.name(),
                            HttpMethod.POST.name(),
                            HttpMethod.PUT.name(),
                            HttpMethod.PATCH.name(),
                            HttpMethod.DELETE.name(),
                            HttpMethod.OPTIONS.name()
                    ));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .securityContextRepository(securityContextRepository())
                .build();
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new ServerSecurityContextRepository() {

            @Override
            public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
                return Mono.empty(); // Не сохраняем контекст явно
            }

            @Override
            public Mono<SecurityContext> load(ServerWebExchange exchange) {
                return jwtAuthenticationManager.authenticationConverter()
                        .convert(exchange)
                        .map(SecurityContextImpl::new); // Передаём объект Authentication в SecurityContext
            }
        };
    }

    @Bean
    public GridFSBucket gridFSBucket(ReactiveMongoDatabaseFactory mongoDbFactory) {
        return GridFSBuckets.create(Objects.requireNonNull(mongoDbFactory.getMongoDatabase().block())); // блокировка для получения базы данных
    }
}
