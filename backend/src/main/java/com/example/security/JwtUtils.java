package com.example.security;

import com.example.config.JwtProperties;
import com.example.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class JwtUtils {
    private final JwtProperties jwtProperties;

    private Key key;

    @PostConstruct
    public void init()
    {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.length() < 32)
        {
            throw new IllegalArgumentException("JWT Secretが短すぎます（最低32バイト）");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user)
    {
        return Jwts.builder().setSubject(user.getPrimaryEmail().getEmail()) // ← 修正
                .claim("id", user.getId().toString()).claim("role", user.getRole().name()).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public String getEmailFromToken(String token)
    {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject(); // email を
                                                                                                             // subject
                                                                                                             // に設定しているため
    }

    public boolean validateJwtToken(String authToken)
    {
        try
        {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e)
        {
            return false;
        }
    }
}
