package com.wio.crm.controller;

import com.wio.crm.model.UserApproval;
import com.wio.crm.service.UserApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
public class UserApprovalListController {
    @Autowired
    private UserApprovalService userApprovalService;

    @GetMapping("/user-approval-list")
    public String getUserApprovalList(
            @RequestParam(required = false) String status,
            Model model) {
        Logger logger = LoggerFactory.getLogger(UserApprovalListController.class);

        logger.info("Received request for /user-approval-list with status: " + status);

        List<UserApproval> userApprovals;
        // status가 null이거나 빈 문자열이면 빈 리스트 반환
        if (status == null || status.isEmpty()) {
            logger.info("No status provided, fetching all approvals");
            userApprovals = new ArrayList<>();
        } else {
            logger.info("Fetching approvals with status: " + status);
            userApprovals = userApprovalService.getPendingApprovals(status);
        }

        model.addAttribute("userApprovals", userApprovals);

        return "user/user-approval-list";
    }

    @GetMapping("/user-approval/detail/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserApprovalDetail(@PathVariable("id") String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("userApproval", userApprovalService.getUserApprovalById(id));
        result.put("companies", userApprovalService.getCompanyList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/user-approval/approve")
    public ResponseEntity<String> approveUser(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String companyCode = payload.get("companyCode");

        // 요청 파라미터가 올바른지 확인
        if (userId == null || companyCode == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        // 승인 로직 수행
        userApprovalService.approveUser(userId, companyCode);
        return ResponseEntity.ok("User approved successfully");
    }
    @PostMapping("/user-approval/cancel-approval")
    public ResponseEntity<String> cancelApproval(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");

        // 서비스 레이어에서 승인 취소 처리
        userApprovalService.cancelApproval(userId);

        return ResponseEntity.ok("Approval cancelled successfully");
    }

}
