package com.wio.crm.controller;

import com.wio.crm.model.ConsultingInquiry;
import com.wio.crm.service.ConsultingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/consulting")
public class ConsultingController {

    private final Logger logger = LoggerFactory.getLogger(ConsultingController.class);

    @Autowired
    private ConsultingService consultingService;

    /**
     * 상담 문의 리스트 페이지
     */
    /**
     * 상담 문의 리스트 페이지
     */
    @GetMapping("/list")
    public String listConsulting(Model model,
                               @RequestParam(name = "customerName", required = false) String customerName,
                               @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                               @RequestParam(name = "orderNumber", required = false) String orderNumber,
                               @RequestParam(name = "inquiryType", required = false) String inquiryType,
                               @RequestParam(name = "status", required = false) String status,
                               @RequestParam(name = "sortField", required = false, defaultValue = "created_date") String sortField,
                               @RequestParam(name = "sortDirection", required = false, defaultValue = "desc") String sortDirection,
                               @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                               @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        
        logger.info("상담 문의 리스트 조회: customerName={}, phoneNumber={}, status={}, page={}, size={}", 
                customerName, phoneNumber, status, page, size);
        
        // 테스트 데이터 자동 생성
        if (consultingService.createDummyDataIfNeeded()) {
            logger.info("테스트 데이터가 자동으로 생성되었습니다.");
        }
        
        // 페이징 처리: 페이지는 1부터 시작하지만, offset은 0부터 시작
        int offset = (page - 1) * size;
        
        // 페이징 로그 추가
        logger.info("페이징 계산: page={}, size={}, offset={}", page, size, offset);
        
        Map<String, Object> pageInfo = consultingService.getConsultingInquiriesWithPaging(
                customerName, phoneNumber, orderNumber, inquiryType, status, 
                sortField, sortDirection, offset, size);
        
        // 조회 결과 확인
        List<?> inquiries = (List<?>) pageInfo.get("inquiries");
        long totalCount = (Long) pageInfo.get("totalCount");
        logger.info("조회 결과: 총 건수={}, 가져온 건수={}", totalCount, inquiries != null ? inquiries.size() : 0);
        
        // 상태별 건수 조회
        Map<String, Long> statusCounts = consultingService.getStatusCounts(customerName, phoneNumber, orderNumber, inquiryType);
        model.addAttribute("pendingCount", statusCounts.get("PENDING"));
        model.addAttribute("processingCount", statusCounts.get("PROCESSING"));
        model.addAttribute("completedCount", statusCounts.get("COMPLETED"));
        
        if (inquiries != null && !inquiries.isEmpty()) {
            logger.info("첫 번째 문의 데이터: {}", inquiries.get(0));
        }
        
        // 상담 유형 목록 조회
        List<Map<String, Object>> inquiryTypes = consultingService.getAllInquiryTypes();
        
        model.addAttribute("inquiries", inquiries);
        model.addAttribute("inquiryTypes", inquiryTypes);
        model.addAttribute("customerName", customerName);
        model.addAttribute("phoneNumber", phoneNumber);
        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("inquiryType", inquiryType);
        model.addAttribute("status", status);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        
        // 페이징 정보 추가
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalItems", totalCount);
        
        // 총 페이지 수 계산 - 제로 나누기 방지
        int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / size) : 0;
        model.addAttribute("totalPages", totalPages);
        
        logger.info("페이징 정보: 총 페이지 수={}, 현재 페이지={}", totalPages, page);
        
        return "consulting/list";
    }
    
    /**
     * 상담 문의 상세 페이지
     */
    @GetMapping("/detail")
    public String viewConsulting(@RequestParam(name = "id", required = true) Long inquiryId, Model model) {
        logger.info("상담 문의 상세 조회: inquiryId={}", inquiryId);
        
        ConsultingInquiry inquiry = consultingService.getConsultingInquiry(inquiryId);
        if (inquiry == null) {
            return "redirect:/error";
        }
        
        // 첨부 파일 목록 조회
        List<Map<String, Object>> attachments = consultingService.getAttachmentsByInquiryId(inquiryId);
        
        // 첨부파일 정보 디버깅 로그
        if (attachments != null && !attachments.isEmpty()) {
            logger.info("========== 첨부파일 정보 디버깅 ===========");
            logger.info("첨부파일 개수: {}", attachments.size());
            
            for (int i = 0; i < attachments.size(); i++) {
                Map<String, Object> attachment = attachments.get(i);
                logger.info("첨부파일 #{}", i+1);
                
                for (Map.Entry<String, Object> entry : attachment.entrySet()) {
                    logger.info("  {} : {}", entry.getKey(), entry.getValue());
                }
                
                // 전체 URL 구성
                if (attachment.containsKey("file_path")) {
                    String filePath = (String) attachment.get("file_path");
                    String serverUrl = "http://175.119.224.45:8080/uploads/";
                    String fullUrl = serverUrl + filePath;
                    
                    logger.info("  [이미지 경로 정보]");
                    logger.info("  - 기본 URL: {}", serverUrl);
                    logger.info("  - 파일 경로: {}", filePath);
                    logger.info("  - 전체 URL: {}", fullUrl);
                    
                    // 이미지 태그와 다운로드 링크 정보 로깅
                    logger.info("  [HTML 이미지 태그 예시]");
                    logger.info("  <img src=\"{}\" alt=\"첨부파일\">", fullUrl);
                    logger.info("  <a href=\"{}\" download>", fullUrl);
                    
                    // 모델에 전체 URL 정보 추가
                    attachment.put("full_url", fullUrl);
                }
                
                logger.info("------------------------------------");
            }
        } else {
            logger.info("첨부파일이 없습니다.");
        }
        
        // 코멘트 목록 조회
        List<Map<String, Object>> comments = consultingService.getCommentsByInquiryId(inquiryId);
        
        // 자주 쓰는 응답 템플릿 목록 조회
        List<Map<String, Object>> replyTemplates = consultingService.getActiveReplyTemplates();
        
        // 카테고리별로 그룹화된 템플릿 조회
        List<Map<String, Object>> replyTemplatesByCategory = consultingService.getActiveReplyTemplates();
        Map<String, List<Map<String, Object>>> groupedReplyTemplates = new HashMap<>();
        
        // 카테고리별로 수동 그룹화
        for (Map<String, Object> template : replyTemplatesByCategory) {
            String category = (String) template.getOrDefault("category", "기타");
            if (!groupedReplyTemplates.containsKey(category)) {
                groupedReplyTemplates.put(category, new ArrayList<>());
            }
            groupedReplyTemplates.get(category).add(template);
        }
        
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("attachments", attachments);
        model.addAttribute("comments", comments);
        model.addAttribute("replyTemplates", replyTemplates);
        model.addAttribute("groupedReplyTemplates", groupedReplyTemplates);
        
        return "consulting/detail";
    }
    
    /**
     * 상담 문의 상태 업데이트 (AJAX)
     */
    @PostMapping("/update-status")
    @ResponseBody
    public Map<String, Object> updateStatus(@RequestParam(name = "inquiryId") Long inquiryId, 
                                         @RequestParam(name = "status") String status,
                                         Authentication authentication) {
        logger.info("상담 문의 상태 업데이트: inquiryId={}, status={}", inquiryId, status);
        
        String username = authentication.getName();
        return consultingService.updateInquiryStatus(inquiryId, status, username);
    }
    
    /**
     * 다수의 상담 문의 상태 일괄 업데이트 (AJAX)
     */
    @PostMapping("/update-multiple-status")
    @ResponseBody
    public Map<String, Object> updateMultipleStatus(@RequestParam(name = "inquiryIds") List<Long> inquiryIds, 
                                                 @RequestParam(name = "status") String status,
                                                 Authentication authentication) {
        logger.info("다수 상담 문의 상태 일괄 업데이트: inquiryIds={}, status={}", inquiryIds, status);
        
        String username = authentication.getName();
        return consultingService.updateMultipleInquiryStatus(inquiryIds, status, username);
    }
    
    /**
     * 코멘트 등록 (AJAX)
     */
    @PostMapping("/add-comment")
    @ResponseBody
    public Map<String, Object> addComment(@RequestParam(name = "inquiryId") Long inquiryId,
                                      @RequestParam(name = "content") String content,
                                      @RequestParam(name = "isInternal", defaultValue = "false") Boolean isInternal,
                                      Authentication authentication) {
        logger.info("상담 문의 코멘트 등록: inquiryId={}, isInternal={}", inquiryId, isInternal);
        
        String commenter = authentication.getName();
        return consultingService.addComment(inquiryId, content, commenter, isInternal);
    }
    
    /**
     * 처리 내용 업데이트 (AJAX)
     */
    @PostMapping("/update-process")
    @ResponseBody
    public Map<String, Object> updateProcessContent(@RequestParam(name = "inquiryId") Long inquiryId,
                                               @RequestParam(name = "processContent") String processContent,
                                               Authentication authentication) {
        logger.info("상담 문의 처리 내용 업데이트: inquiryId={}", inquiryId);
        
        String username = authentication.getName();
        return consultingService.updateProcessContent(inquiryId, processContent, username);
    }
    
    /**
     * 내부 메모 업데이트 (AJAX)
     */
    @PostMapping("/update-memo")
    @ResponseBody
    public Map<String, Object> updateMemo(@RequestParam(name = "inquiryId") Long inquiryId,
                                      @RequestParam(name = "memo") String memo,
                                      Authentication authentication) {
        logger.info("상담 문의 내부 메모 업데이트: inquiryId={}", inquiryId);
        
        String username = authentication.getName();
        return consultingService.updateMemo(inquiryId, memo, username);
    }
    
    /**
     * 상담 문의 등록 페이지
     */
    @GetMapping("/add")
    public String addConsultingForm(Model model) {
        logger.info("상담 문의 등록 페이지 접근");
        
        // 문의 유형 목록 조회
        List<Map<String, Object>> inquiryTypes = consultingService.getAllInquiryTypes();
        model.addAttribute("inquiryTypes", inquiryTypes);
        
        // 빈 문의 객체 생성
        model.addAttribute("inquiry", new ConsultingInquiry());
        
        return "consulting/add";
    }
    
    /**
     * 상담 문의 등록 처리
     */
    @PostMapping("/add")
    public String addConsulting(@ModelAttribute ConsultingInquiry inquiry,
                             @RequestParam(name = "attachments", required = false) MultipartFile[] attachments,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        logger.info("상담 문의 등록: {}", inquiry.getCustomerName());
        
        String username = authentication.getName();
        Long inquiryId = consultingService.saveConsultingInquiry(inquiry, attachments, username);
        
        redirectAttributes.addFlashAttribute("message", "상담 문의가 성공적으로 등록되었습니다.");
        
        return "redirect:/consulting/detail?id=" + inquiryId;
    }
    
    /**
     * 이미지 경로 테스트 페이지
     */
    @GetMapping("/image-test")
    public String imageTest(@RequestParam(name = "id", required = false, defaultValue = "0") Long inquiryId, Model model) {
        logger.info("이미지 URL 테스트 페이지 접근: inquiryId={}", inquiryId);
        
        // ID가 0이면 가장 최근의 첨부파일이 있는 문의 찾기
        if (inquiryId == 0) {
            inquiryId = consultingService.getLatestInquiryIdWithAttachments();
            if (inquiryId == null) {
                logger.warn("첨부파일이 있는 문의를 찾을 수 없습니다.");
            } else {
                logger.info("첨부파일이 있는 문의 발견: inquiryId={}", inquiryId);
            }
        }
        
        // 이제 해당 ID로 상세 조회
        ConsultingInquiry inquiry = null;
        List<Map<String, Object>> attachments = new ArrayList<>();
        
        if (inquiryId > 0) {
            inquiry = consultingService.getConsultingInquiry(inquiryId);
            if (inquiry != null) {
                attachments = consultingService.getAttachmentsByInquiryId(inquiryId);
                
                // 첨부파일 URL 정보 추가
                for (Map<String, Object> attachment : attachments) {
                    if (attachment.containsKey("file_path")) {
                        String filePath = (String) attachment.get("file_path");
                        String serverUrl = "http://175.119.224.45:8080/uploads/";
                        attachment.put("full_url", serverUrl + filePath);
                    }
                }
            }
        }
        
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("attachments", attachments);
        model.addAttribute("serverUrl", "http://175.119.224.45:8080/uploads/");
        
        return "consulting/image_fix";
    }
}
