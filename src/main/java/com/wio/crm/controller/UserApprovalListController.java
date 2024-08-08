package com.wio.crm.controller;

import com.wio.crm.model.UserApproval;
import com.wio.crm.service.UserApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class UserApprovalListController {
    @Autowired
    private UserApprovalService userApprovalService;

    @GetMapping("/user-approval-list")
    public String getUserApprovalList(Model model) {
        List<UserApproval> userApprovals = userApprovalService.getPendingApprovals();
        model.addAttribute("userApprovals", userApprovals);
        return "user/user-approval-list";
    }

    @GetMapping("/user-approval/detail/{id}")
    public String getUserApprovalDetail(@PathVariable String  id, Model model) {
        System.out.println("id = " + id);
        UserApproval userApproval = userApprovalService.getUserApprovalById(id);
        model.addAttribute("userApproval", userApproval);
        return "user/user-approval-detail :: userApprovalDetails";
    }


}
