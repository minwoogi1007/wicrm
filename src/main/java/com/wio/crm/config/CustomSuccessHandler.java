package com.wio.crm.config;

import com.wio.crm.service.LoginAttemptService;
import com.wio.crm.service.LoginHistoryService;
import com.wio.crm.service.MenuService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class CustomSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomSuccessHandler.class);

    @Autowired
    private MenuService menuService;

    @Autowired
    private LoginHistoryService loginHistoryService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String attemptKey = authentication.getName() + ":" + clientIp;
        loginAttemptService.loginSucceeded(attemptKey);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = authentication.getName();
        String userName = "";
        String authority = "";
        String custCode = "";
        
        if (userDetails.getTcntUserInfo() != null) {
            userName = userDetails.getTcntUserInfo().getEmp_name();
            authority = userDetails.getTcntUserInfo().getAuthority();
            custCode = userDetails.getTcntUserInfo().getCustCode();
        } else {
            userName = userDetails.getTempUserInfo().getEmp_Name();
            authority = userDetails.getTempUserInfo().getPosition();
            custCode = "INTERNAL";
        }

        logger.debug("로그인 성공 - userId: {}, authority: {}, custCode: {}", userId, authority, custCode);

        List<Map<String, Object>> menuList;
        try {
            menuList = menuService.getCompanyUserMenus(authentication.getName(), authority);
        } catch (Exception e) {
            logger.error("메뉴 조회 실패", e);
            throw new ServletException("Failed to fetch user menus", e);
        }

        HttpSession session = request.getSession();
        session.setAttribute("USER_MENUS", menuList);

        loginHistoryService.recordLoginHistory(authentication.getName());

        session.setAttribute("loginUserAuthority", authority);
        session.setAttribute("loginUserId", userId);
        session.setAttribute("loginUserName", userName);
        session.setAttribute("custCode", custCode);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
