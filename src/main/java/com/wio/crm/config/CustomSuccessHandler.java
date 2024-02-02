package com.wio.crm.config;

import com.wio.crm.model.Menu;
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
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        // 사용자 권한에 따른 메뉴 리스트 조회 및 세션 저장 로직

        super.onAuthenticationSuccess(request, response, authentication);
        List<Menu> menuList = menuService.getCompanyUserMenus(authentication.getName());

        // 조회된 메뉴 리스트를 세션에 저장
        HttpSession session = request.getSession();
        session.setAttribute("USER_MENUS", menuList);
    }
}