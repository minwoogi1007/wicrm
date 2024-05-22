package com.wio.crm.controller;

import com.wio.crm.service.DashboardService;
import com.wio.crm.service.MenuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/main")
    public String mainPage(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.info("No authentication information available.");
            return "redirect:/sign-in";
        }


        String username = authentication.getName();
       // String userid = authentication.getName(); // 현재 인증된 사용자의 ID를 가져옵니다.
       // model.addAttribute("userMenus", menuService.getCompanyUserMenus(userid));
        // 서비스를 호출하여 사용자 등급에 따른 데이터를 가져옵니다.
        Map<String, Object> tcntEmpData = dashboardService.getTcntEmp();
        if(tcntEmpData!=null){
            model.addAllAttributes(tcntEmpData);
        }else{
            return "redirect:/sign-in";
        }


        boolean isEmployee = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));

        model.addAttribute("isEmployee", isEmployee);

        model.addAttribute("content", "contents"); // Initial content
        return "contents";
    }




}
