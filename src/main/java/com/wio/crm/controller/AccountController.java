package com.wio.crm.controller;

import com.wio.crm.dto.PasswordChangeDto;
import com.wio.crm.model.Account;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Map;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    // GET 요청: 계정 정보 페이지
    @GetMapping("/account")
    public String account(Model model, @RequestParam(value = "success", required = false) boolean success) {
        Map<String, Object> accountInfo = accountService.getAccount();
        Tcnt01Emp account = (Tcnt01Emp) accountInfo.get("accountInfo");

        // 계정 정보가 없는 경우, 사용자 정의 예외를 발생시킵니다.
        if (account == null) {
            throw new RuntimeException("Account information is missing!");
        }

        // 성공적으로 업데이트가 되었을 경우, 모델에 성공 플래그를 추가합니다.
        model.addAttribute("success", success);
        model.addAttribute("accountInfo", account);

        return "account/account";  // Thymeleaf 템플릿 반환
    }

    // GET 요청: 계정 정보 수정 페이지
    @GetMapping("/account/update")
    public String accountM(Model model) {
        Map<String, Object> accountInfo = accountService.getAccount();
        Tcnt01Emp account = (Tcnt01Emp) accountInfo.get("accountInfo");

        if (account == null) {
            throw new RuntimeException("Account information is missing!");
        }

        model.addAttribute("accountInfo", account);
        return "account/accountUpdate";  // 계정 정보 수정 페이지 반환
    }

    // POST 요청으로 계정 정보 업데이트 (AJAX)
    @PostMapping("/api/account/update")
    public ResponseEntity<String> updateAccount(@RequestBody Account account) {
        try {
            boolean updated = accountService.updateAccount(account);

            System.out.println("account========"+account);
            if (updated) {
                return ResponseEntity.ok("Account updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update account");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/api/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String currentPassword, @RequestParam String newPassword) {
        if (!accountService.checkCurrentPassword(currentPassword)) {
            // 현재 비밀번호 검증 실패
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("error", "현재 비밀번호가 정확하지 않습니다."));
        }

        // 비밀번호 변경 로직
        try {
            // 비밀번호 변경 시도
            accountService.changeUserPassword(newPassword);
            return ResponseEntity.ok(Collections.singletonMap("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            // 비밀번호 변경 중 예외 발생
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "비밀번호 변경에 실패했습니다."));
        }
    }
}
