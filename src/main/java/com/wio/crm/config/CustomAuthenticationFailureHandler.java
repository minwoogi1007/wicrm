package com.wio.crm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wio.crm.exception.UserNotConfirmedException;
import com.wio.crm.service.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // UserNotConfirmedException 처리

        String errorMessage;
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (exception.getCause() instanceof UserNotConfirmedException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"승인 대기 중입니다.\"}";
        } else if (exception instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"비밀번호 확인 바랍니다.\"}";
        } else {
            // 다른 종류의 인증 실패
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"아이디 확인 바랍니다.\"}";
        }

        response.getWriter().write(errorMessage);
    }
}