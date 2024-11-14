package org.packet.reactivewebapp.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.packet.reactivewebapp.Entities.User;
import org.packet.reactivewebapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.time}")
    private Duration jwtLifeTime;

    @Value ("${jwt.time.refresh-token}")
    private Duration jwtLifeTimeForRefreshToken;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
    private SecretKey key;

    public String generateToken(String username){

        Map<String,Object> claims = new HashMap<>();
        List<String> roleList = List.of(String.valueOf(new SimpleGrantedAuthority("ROLE_ADMIN")));
                claims.put("roles",roleList);
        return builderForToken(claims,jwtLifeTime,username);

    }
    public String generateRefreshToken(String username){
        return builderForToken(new HashMap<>(),jwtLifeTimeForRefreshToken,username);
    }
    public String builderForToken( Map<String,Object> claims, Duration time,String username){
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .claims().issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+time.toMillis()))
                .and()
                .signWith(key)
                .compact();
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private <Y> Y extractClaim(String token, Function<Claims,Y> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    public String extractUserName(String token){
        return extractClaim(token,Claims::getSubject);
    }
     public  boolean validateToken(String token, User userDetails){
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getName())) && isTokenExpired(token);
    }
    public List<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = (List<String>) extractClaim(token, er->er.get("roles")); // Извлечение ролей из токена
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
