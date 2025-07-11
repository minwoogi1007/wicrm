package com.wio.crm.controller;

import com.wio.crm.mapper.TipdwMapper;

import com.wio.crm.model.UserInfo;
import com.wio.crm.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private TipdwMapper tipdwMapper;

    @Autowired
    private LoginService loginService;


    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "sign-in";
    }


    //@GetMapping("/logout")
    /*public String logout() {
        return "sign-in";
    }*/

    @GetMapping("/check-userid-availability")
    @ResponseBody
    public Map<String, Boolean> checkUserIdAvailability(@RequestParam String userId) {
        return Collections.singletonMap("isAvailable", tipdwMapper.countByUserId(userId) == 0);
    }

    @PostMapping("/apply-userid")
    public ResponseEntity<?> applyUserId(@RequestBody UserInfo userInfo) {
        loginService.applyUserId(userInfo);
        //System.out.println(userInfo);
        return ResponseEntity.ok().build();
    }

    private boolean hasUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
    }
}
