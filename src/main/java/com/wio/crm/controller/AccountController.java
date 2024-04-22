package com.wio.crm.controller;

import com.wio.crm.model.Account;
import com.wio.crm.model.Mileage;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Transaction;
import com.wio.crm.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;
    @GetMapping("/account")
    public String account(Model model,@RequestParam(value = "success", required = false, defaultValue="false") boolean success) {
        Map<String, Object> accountInfo = accountService.getAccount();

        Tcnt01Emp account = (Tcnt01Emp) accountInfo.get("accountInfo");
        if (account == null) {
            throw new RuntimeException("Account information is missing!");
        }

        System.out.println("success======"+success);

        if (success) {
            model.addAttribute("success", true);
        }
        model.addAttribute("accountInfo", account);
        return "account/account";
    }
    @GetMapping("/account/accountM")
    public String accountM(Model model) {
        Map<String, Object> accountInfo = accountService.getAccount();

        Tcnt01Emp account = (Tcnt01Emp) accountInfo.get("accountInfo");
        if (account == null) {
            throw new RuntimeException("Account information is missing!");
        }

        model.addAttribute("accountInfo", account);

        return "account/accountUpdate";
    }

    @PostMapping("/account/update")
    public String updateAccount(Account account, Model model) {
        boolean updateStatus = accountService.updateAccount(account);
        // Check if the update was successful and add the appropriate flash message
        String redirectUrl = "redirect:/account";
        redirectUrl += updateStatus ? "?success=true" : "?error=true";

        return redirectUrl;
    }
}
