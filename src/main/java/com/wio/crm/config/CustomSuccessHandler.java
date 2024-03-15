package com.wio.crm.config;

import com.wio.crm.model.Menu;
import com.wio.crm.service.LoginHistoryService;
import com.wio.crm.service.MenuService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CustomSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private MenuService menuService;

    @Autowired
    private  LoginHistoryService loginHistoryService; // 로그인 이력을 관리하는 서비스




    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        // 사용자 권한에 따른 메뉴 리스트 조회 및 세션 저장 로직
        List<Menu> menuList = menuService.getCompanyUserMenus(authentication.getName());

        // 조회된 메뉴 리스트를 세션에 저장
        HttpSession session = request.getSession();
        session.setAttribute("USER_MENUS", menuList);

        // 로그인 이력 기록
        loginHistoryService.recordLoginHistory(authentication.getName());

        // 마지막에 한 번만 호출하여 리다이렉트 처리
        super.onAuthenticationSuccess(request, response, authentication);
    }
}