package com.wio.crm.controller;

import com.wio.crm.service.MenuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private MenuService menuService;
    @GetMapping("/main")
    public String mainPage(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.info("No authentication information available.");
            return "sign-in";
        }
        String userid = authentication.getName(); // 현재 인증된 사용자의 ID를 가져옵니다.
        model.addAttribute("userMenus", menuService.getCompanyUserMenus(userid));

        return "main";
    }

    @GetMapping("/list")
    public String getContentFragment(Model model) {
        // 이 메소드는 AJAX 요청에 응답하여 필요한 데이터를 모델에 추가하고,
        // 해당 fragment의 뷰 이름을 반환합니다.
        System.out.println("여기 맞어?");
        return "list/list :: list";
    }
}
