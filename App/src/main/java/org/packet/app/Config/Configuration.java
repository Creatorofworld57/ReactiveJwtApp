package org.packet.app.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@org.springframework.context.annotation.Configuration

public class Configuration {

    @Bean
    static public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "api/login", "/api/audio/**", "/api/audioName/**","api/authorization","/api/checking","/api/uploadTrailer","login/oauth2/authorization/github","/login/oauth2/git","/login/oauth2/code/github","/api/audioCount","/api/user/withGithub/{id}","/api/searchOfTrack/{name}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user").permitAll()// Разрешить доступ без аутентификации
                        .requestMatchers("/newUser").anonymous() // Доступно только анонимным пользователям
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("https://localhost:3000/profile").authenticated()

                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // Путь к странице логина
                        .loginProcessingUrl("/perform_login") // URL для обработки логина
                        .defaultSuccessUrl("/Welcome")
                        // URL после успешного логина
                        .failureUrl("/login")
                        // URL после неудачного логина
                        .passwordParameter("password") // Параметр пароля
                        .usernameParameter("name") // Параметр имени пользователя
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout") // URL для выхода
                        .logoutSuccessUrl("https://localhost:3000/login") // URL после успешного выхода
                        .permitAll()
                );

        return http.build();
    }
}
