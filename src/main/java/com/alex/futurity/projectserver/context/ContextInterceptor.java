package com.alex.futurity.projectserver.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ContextInterceptor extends OncePerRequestFilter {
    private static final String HAS_TELEGRAM_HEADER_NAME = "telegram";
    private static final String USER_ID_HEADER_NAME = "user_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean hasTelegram = Boolean.parseBoolean(request.getHeader(HAS_TELEGRAM_HEADER_NAME));
        Long userId = Long.valueOf(request.getHeader(USER_ID_HEADER_NAME));
        User user = User.builder()
                .hasTelegram(hasTelegram)
                .userId(userId)
                .build();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        requestAttributes.setAttribute(UserContext.USER, user, RequestAttributes.SCOPE_REQUEST);

        RequestContextHolder.setRequestAttributes(requestAttributes);

        filterChain.doFilter(request, response);
    }
}
