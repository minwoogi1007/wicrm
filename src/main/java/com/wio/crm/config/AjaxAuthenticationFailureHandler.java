package com.wio.crm.config;

import com.wio.crm.exception.UserNotConfirmedException;
import com.wio.crm.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AjaxAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(AjaxAuthenticationFailureHandler.class);

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String clientIp = getClientIp(request);
        String userId = request.getParameter("userId");
        String attemptKey = (userId != null ? userId : "") + ":" + clientIp;

        loginAttemptService.loginFailed(attemptKey);
        int remaining = loginAttemptService.getRemainingAttempts(attemptKey);

        logger.warn("로그인 실패 - userId: {}, IP: {}, 남은 시도: {}회", userId, clientIp, remaining);

        String errorMessage;
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (loginAttemptService.isBlocked(attemptKey)) {
            response.setStatus(429);
            errorMessage = "{\"error\": \"로그인 시도 횟수 초과. 15분 후 다시 시도해주세요.\"}";
        } else if (exception.getCause() instanceof UserNotConfirmedException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"승인 대기 중입니다.\"}";
        } else if (exception instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"비밀번호 확인 바랍니다. (남은 시도: " + remaining + "회)\"}";
        } else if (exception instanceof UsernameNotFoundException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"아이디 또는 비밀번호를 확인해주세요.\"}";
        } else if (exception instanceof DisabledException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"계정이 비활성화되었습니다.\"}";
        } else if (exception instanceof AccountExpiredException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"계정이 만료되었습니다.\"}";
        } else if (exception instanceof CredentialsExpiredException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"인증 정보가 만료되었습니다.\"}";
        } else if (exception instanceof LockedException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"계정이 잠겨 있습니다.\"}";
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errorMessage = "{\"error\": \"인증 실패.\"}";
        }

        response.getWriter().write(errorMessage);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}