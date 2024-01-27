package com.wio.crm.controller;

import com.wio.crm.mapper.TipdwMapper;
import com.wio.crm.service.MenuService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private TipdwMapper tipdwMapper;

    @Autowired
    private MenuService menuService;

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "sign-in";
    }

    @GetMapping("/check-userid-availability")
    @ResponseBody
    public Map<String, Boolean> checkUserIdAvailability(@RequestParam String userId) {
        return Collections.singletonMap("isAvailable", tipdwMapper.countByUserId(userId) == 0);
    }

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

    private boolean hasUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
    }
}
