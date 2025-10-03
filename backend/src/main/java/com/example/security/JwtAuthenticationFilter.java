package com.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response); // ← トークン無ければ素通し
            return;
        }

        String token = authHeader.substring(7);
        try {
            // 1) まずトークンの署名/期限などを検証
            if (!jwtUtils.validateJwtToken(token)) {
                // 無効トークンは“無視して素通し”。ここでレスポンスは書かない
                log.debug("Invalid JWT: skip authentication");
                chain.doFilter(request, response);
                return;
            }

            // 2) 有効ならクレームから主体（email/username）を取り出す
            String username = jwtUtils.getEmailFromToken(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(username);

                // 3) 認証トークン作成（isAuthenticated=true のコンストラクタ）
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ex) {
            // 解析/検証で例外が出ても“素通し”する（401/403は後段の仕組みに任せる）
            log.debug("JWT parse/validate failed: {}", ex.getMessage());
        }

        chain.doFilter(request, response);
    }
}
