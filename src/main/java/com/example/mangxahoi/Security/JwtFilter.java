package com.example.mangxahoi.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException{
        try{
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Cookie[] cookies = request.getCookies();
                if(cookies != null){
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("access_token")){
                            String token = cookie.getValue();

                            if (token != null && jwtUtil.validateTokenType(token, "access_token")){
                                String username = jwtUtil.extractUsername(token);
                                UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception ex){
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request,response);
    }
}
