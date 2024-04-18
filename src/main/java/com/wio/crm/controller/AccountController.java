package com.wio.crm.controller;

import com.wio.crm.model.Mileage;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Transaction;
import com.wio.crm.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;
    @GetMapping("/account")
    public String account(Model model) {
        Map<String, Object> accountInfo = accountService.getAccount();

        Tcnt01Emp account = (Tcnt01Emp) accountInfo.get("accountInfo");
        if (account == null) {
            throw new RuntimeException("Account information is missing!");
        }

        model.addAttribute("accountInfo", account);
        return "account/account";
    }
    @GetMapping("/accountM")
    public String accountM() {
        return "account/accountUpdate";
    }
}
