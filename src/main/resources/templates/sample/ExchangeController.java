/**
 * 교환/반품 관리 컨트롤러
 * 사용 화면: exchange/list.html, exchange/form.html, exchange/view.html
 * 
 * 주요 기능:
 * - 교환/반품 목록 조회 (검색, 필터링, 페이징)
 * - 교환/반품 등록/수정/삭제
 * - 상태 관리 (접수대기 → 처리중 → 배송중 → 완료)
 * - 통계 정보 제공
 */
package com.wio.repairsystem.controller;

import com.wio.repairsystem.dto.ReturnItemDTO;
import com.wio.repairsystem.dto.ReturnItemSearchDTO;
import com.wio.repairsystem.dto.ReturnItemBulkDateUpdateDTO;
import com.wio.repairsystem.model.ReturnStatus;
import com.wio.repairsystem.model.ReturnType;
import com.wio.repairsystem.service.ReturnItemService;
import com.wio.repairsystem.service.AttachmentService;
import com.wio.repairsystem.service.ReturnItemStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {

    private final ReturnItemService returnItemService;
    private final AttachmentService attachmentService;
    private final ReturnItemStatsService returnItemStatsService;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadBaseDir;

    /**
     * 교환/반품 목록 페이지 (exchange/list.html)
     * 🎯 다중 필터 지원 추가
     */
    @GetMapping({"/list", ""})
    public String list(
            Model model, 
            @ModelAttribute ReturnItemSearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String filters) {
        
        log.info("교환/반품 목록 조회 - 검색조건: {}, 필터: {}", searchDTO, filters);
        
        // 🎯 다중 필터 처리
        Page<ReturnItemDTO> returnItems;
        try {
            // 페이징 정보를 searchDTO에 설정
            if (searchDTO == null) {
                searchDTO = new ReturnItemSearchDTO();
            }
            searchDTO.setPage(page);
            searchDTO.setSize(size);
            searchDTO.setSortBy(sortBy);
            searchDTO.setSortDir(sortDir);
            
            // 🎯 필터와 검색 조건 함께 처리
            boolean hasFilters = StringUtils.hasText(filters);
            boolean hasSearchCondition = searchDTO.hasSearchCondition();
            
            if (hasFilters && hasSearchCondition) {
                // 필터 + 검색 조건 둘 다 있는 경우
                log.info("🔍 필터 + 검색 조건 함께 적용 - 필터: {}, 검색: {}", filters, searchDTO.getKeyword());
                returnItems = applyMultipleFiltersWithSearch(filters, searchDTO);
                log.info("✅ 필터 + 검색 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            } else if (hasFilters) {
                // 필터만 있는 경우
                log.info("🔍 다중 필터만 적용: {}", filters);
                returnItems = applyMultipleFilters(filters, searchDTO);
                log.info("✅ 다중 필터 조회 완료 - 필터: {}, 결과 수: {}", filters, returnItems.getTotalElements());
            } else if (hasSearchCondition) {
                // 검색 조건만 있는 경우
                returnItems = returnItemService.search(searchDTO);
                log.info("🔍 검색 조건으로 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            } else {
                // 필터도 검색 조건도 없는 경우
                returnItems = returnItemService.findAll(page, size, sortBy, sortDir);
                log.info("📋 전체 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            }
        } catch (Exception e) {
            log.error("❌ 목록 조회 실패, 빈 페이지 반환: {}", e.getMessage(), e);
            returnItems = Page.empty(PageRequest.of(page, size));
        }
        
        // 🚀 현황요약 비활성화 - 성능 최적화를 위해 통계 조회 건너뜀
        log.info("📊 현황요약 비활성화됨 - 통계 조회 건너뜀으로 성능 향상");
        
        // 현황요약에 사용되는 통계 변수들을 빈 값으로 초기화
        Map<ReturnStatus, Long> statusCounts = new java.util.HashMap<>();
        Map<String, Long> siteCounts = new java.util.HashMap<>();
        Map<String, Long> typeCounts = new java.util.HashMap<>();
        Map<String, Long> reasonCounts = new java.util.HashMap<>();
        Map<String, Object> amountSummary = new java.util.HashMap<>();
        Map<String, Long> brandCounts = new java.util.HashMap<>();
        Long todayCount = 0L;
        
        // 상단 카드 통계는 유지 (필터링에 필요)
        Map<String, Long> cardStats = new java.util.HashMap<>();
        
        try {
            log.info("🚀 상단 카드 통계만 조회 시작 (현황요약 제외)");
            long startTime = System.currentTimeMillis();
            
            // 📊 상단 카드 통계만 조회 (필터링에 필요)
            Map<String, Object> allStats = returnItemStatsService.getAllStats();
            
            // 카드 통계 매핑 (필터링 기능에 필요)
            cardStats.put("collectionCompleted", ((Number) allStats.get("COLLECTION_COMPLETED")).longValue());
            cardStats.put("collectionPending", ((Number) allStats.get("COLLECTION_PENDING")).longValue());
            cardStats.put("logisticsConfirmed", ((Number) allStats.get("LOGISTICS_CONFIRMED")).longValue());
            cardStats.put("logisticsPending", ((Number) allStats.get("LOGISTICS_PENDING")).longValue());
            cardStats.put("exchangeShipped", ((Number) allStats.get("EXCHANGE_SHIPPED")).longValue());
            cardStats.put("exchangeNotShipped", ((Number) allStats.get("EXCHANGE_NOT_SHIPPED")).longValue());
            cardStats.put("returnRefunded", ((Number) allStats.get("RETURN_REFUNDED")).longValue());
            cardStats.put("returnNotRefunded", ((Number) allStats.get("RETURN_NOT_REFUNDED")).longValue());
            cardStats.put("paymentCompleted", ((Number) allStats.get("PAYMENT_COMPLETED")).longValue());
            cardStats.put("paymentPending", ((Number) allStats.get("PAYMENT_PENDING")).longValue());
            cardStats.put("completedCount", ((Number) allStats.get("COMPLETED_COUNT")).longValue());
            cardStats.put("incompletedCount", ((Number) allStats.get("INCOMPLETED_COUNT")).longValue());
            cardStats.put("overdueTenDaysCount", ((Number) allStats.get("OVERDUE_TEN_DAYS")).longValue());
            
            // 현황요약 통계는 조회하지 않음 (성능 향상)
            /*
            // 상태별 통계 매핑 - 현황요약 비활성화로 주석처리
            statusCounts.put(ReturnStatus.PENDING, ((Number) allStats.get("STATUS_PENDING")).longValue());
            statusCounts.put(ReturnStatus.PROCESSING, ((Number) allStats.get("STATUS_PROCESSING")).longValue());
            statusCounts.put(ReturnStatus.SHIPPING, ((Number) allStats.get("STATUS_SHIPPING")).longValue());
            statusCounts.put(ReturnStatus.COMPLETED, ((Number) allStats.get("STATUS_COMPLETED")).longValue());
            
            // 유형별 통계 매핑 - 현황요약 비활성화로 주석처리
            typeCounts.put("FULL_RETURN", ((Number) allStats.get("TYPE_FULL_RETURN")).longValue());
            typeCounts.put("PARTIAL_RETURN", ((Number) allStats.get("TYPE_PARTIAL_RETURN")).longValue());
            typeCounts.put("FULL_EXCHANGE", ((Number) allStats.get("TYPE_FULL_EXCHANGE")).longValue());
            typeCounts.put("PARTIAL_EXCHANGE", ((Number) allStats.get("TYPE_PARTIAL_EXCHANGE")).longValue());
            typeCounts.put("EXCHANGE", ((Number) allStats.get("TYPE_EXCHANGE")).longValue());
            
            // 금액 통계 매핑 - 현황요약 비활성화로 주석처리
            amountSummary.put("totalRefundAmount", allStats.get("TOTAL_REFUND_AMOUNT"));
            amountSummary.put("totalShippingFee", allStats.get("TOTAL_SHIPPING_FEE"));
            amountSummary.put("avgRefundAmount", allStats.get("AVG_REFUND_AMOUNT"));
            
            // 브랜드별 통계 매핑 - 현황요약 비활성화로 주석처리
            brandCounts.put("레노마", ((Number) allStats.get("BRAND_RENOMA")).longValue());
            brandCounts.put("코랄리크", ((Number) allStats.get("BRAND_CORALIK")).longValue());
            brandCounts.put("기타", ((Number) allStats.get("BRAND_OTHERS")).longValue());
            
            // 금일 등록 건수 - 현황요약 비활성화로 주석처리
            todayCount = ((Number) allStats.get("TODAY_COUNT")).longValue();
            
            // 📊 사이트별 통계 (DB 집계) - 현황요약 비활성화로 주석처리
            siteCounts = returnItemStatsService.getSiteStats();
            
            // 📊 사유별 통계 (DB 집계) - 현황요약 비활성화로 주석처리
            reasonCounts = returnItemStatsService.getReasonStats();
            */
            
            long endTime = System.currentTimeMillis();
            log.info("✅ 상단 카드 통계만 조회 완료 - 소요시간: {}ms (현황요약 제외로 더욱 빠름)", endTime - startTime);
            
        } catch (Exception e) {
            log.error("❌ 카드 통계 조회 실패, 기본값 사용: {}", e.getMessage());
            
            // 기본값으로 초기화
            cardStats.put("collectionCompleted", 0L);
            cardStats.put("collectionPending", 0L);
            cardStats.put("logisticsConfirmed", 0L);
            cardStats.put("logisticsPending", 0L);
            cardStats.put("exchangeShipped", 0L);
            cardStats.put("exchangeNotShipped", 0L);
            cardStats.put("returnRefunded", 0L);
            cardStats.put("returnNotRefunded", 0L);
            cardStats.put("paymentCompleted", 0L);
            cardStats.put("paymentPending", 0L);
            cardStats.put("completedCount", 0L);
            cardStats.put("incompletedCount", 0L);
            cardStats.put("overdueTenDaysCount", 0L);
        }
        
        // searchDTO가 null이면 빈 객체 생성
        if (searchDTO == null) {
            searchDTO = new ReturnItemSearchDTO();
        }
        
        model.addAttribute("returnItems", returnItems);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("siteCounts", siteCounts);
        model.addAttribute("typeCounts", typeCounts);
        model.addAttribute("reasonCounts", reasonCounts);
        model.addAttribute("amountSummary", amountSummary);
        model.addAttribute("brandCounts", brandCounts);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("cardStats", cardStats); // 🎯 상단 카드 통계 추가
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", returnItems.getTotalPages());
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("ASC") ? "DESC" : "ASC");
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());
        model.addAttribute("filters", filters); // 🎯 다중 필터 정보 추가
        
        return "exchange/list";
    }

    /**
     * 교환/반품 등록 폼 페이지 (exchange/form.html)
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        log.info("교환/반품 등록 폼 페이지 접근");
        
        ReturnItemDTO returnItem = new ReturnItemDTO();
        returnItem.setPaymentStatus("NOT_REQUIRED"); // 기본값 설정
        
        model.addAttribute("returnItem", returnItem);
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());
        model.addAttribute("isEdit", false);
        
        return "exchange/form";
    }

    /**
     * 교환/반품 수정 폼 페이지 (exchange/form.html)
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, 
                          @RequestParam(required = false) String returnTo, 
                          Model model) {
        log.info("교환/반품 수정 폼 페이지 접근 - ID: {}", id);
        
        try {
            ReturnItemDTO returnItem = returnItemService.getReturnItemById(id);
            
            // paymentStatus가 null인 경우 기본값 설정
            if (returnItem.getPaymentStatus() == null) {
                returnItem.setPaymentStatus("NOT_REQUIRED");
            }
            
            // 🔥 대표님 긴급: 모든 날짜 필드 디버깅 로그
            log.info("🔥🔥🔥 대표님 모든 날짜 필드 확인!");
            log.info("🔍 조회된 ReturnItem ID: {}", returnItem.getId());
            log.info("🔍 CS 접수일: {}", returnItem.getCsReceivedDate());
            log.info("🔍 주문일: {}", returnItem.getOrderDate());
            log.info("🔍 회수완료일: {}", returnItem.getCollectionCompletedDate());
            log.info("🔍 물류확인일: {}", returnItem.getLogisticsConfirmedDate());
            log.info("🔍 출고일자: {}", returnItem.getShippingDate());
            log.info("🔍 환불일자: {}", returnItem.getRefundDate());
            log.info("🔍 전체 데이터: {}", returnItem);
            
            model.addAttribute("returnItem", returnItem);
            model.addAttribute("returnStatuses", ReturnStatus.values());
            model.addAttribute("returnTypes", ReturnType.values());
            model.addAttribute("isEdit", true);
            
            // 🎯 목록 돌아가기 URL 추가
            if (returnTo != null && !returnTo.isEmpty()) {
                model.addAttribute("returnUrl", returnTo);
                log.info("🎯 목록 돌아가기 URL 설정: {}", returnTo);
            } else {
                model.addAttribute("returnUrl", "/exchange/list");
            }
            
            return "exchange/form";
        } catch (Exception e) {
            log.error("교환/반품 수정 폼 로드 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return "redirect:/exchange/list";
        }
    }

    /**
     * 교환/반품 상세 보기 페이지 (exchange/view.html)
     */
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, 
                       @RequestParam(required = false) String returnTo, 
                       Model model) {
        log.info("교환/반품 상세 보기 - ID: {}", id);
        
        try {
            ReturnItemDTO returnItem = returnItemService.getReturnItemById(id);
            model.addAttribute("returnItem", returnItem);
            model.addAttribute("returnStatuses", ReturnStatus.values());
            model.addAttribute("returnTypes", ReturnType.values());
            
            // 🎯 목록 돌아가기 URL 추가
            if (returnTo != null && !returnTo.isEmpty()) {
                model.addAttribute("returnUrl", returnTo);
                log.info("🎯 목록 돌아가기 URL 설정: {}", returnTo);
            } else {
                model.addAttribute("returnUrl", "/exchange/list");
            }
            
            return "exchange/view";
        } catch (Exception e) {
            log.error("교환/반품 상세 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return "redirect:/exchange/list";
        }
    }

    /**
     * 교환/반품 저장 (등록/수정)
     */
    @PostMapping("/save")
    public String save(@ModelAttribute ReturnItemDTO returnItemDTO, 
                       @RequestParam Map<String, String> allParams,
                       @RequestParam(value = "attachmentPhoto", required = false) MultipartFile attachmentPhoto,
                       @RequestParam(value = "attachmentImageData", required = false) String attachmentImageData,
                       @RequestParam(value = "returnTo", required = false) String returnTo,
                       RedirectAttributes redirectAttributes) {
        log.info("교환/반품 저장 - 데이터: {}", returnItemDTO);
        log.info("모든 파라미터: {}", allParams);
        log.info("첨부 파일: {}", attachmentPhoto != null ? attachmentPhoto.getOriginalFilename() : "없음");
        log.info("붙여넣기 이미지 데이터: {}", attachmentImageData != null ? "있음" : "없음");
        
        // 🔥 대표님 요청: CS 접수일 처리 로직 추가
        if (returnItemDTO.getCsReceivedDate() == null && returnItemDTO.getId() == null) {
            // 신규 등록이고 CS 접수일이 없으면 오늘 날짜로 설정
            returnItemDTO.setCsReceivedDate(java.time.LocalDate.now());
            log.info("🔥 CS 접수일 자동 설정 (신규): {}", returnItemDTO.getCsReceivedDate());
        }
        log.info("🔥 CS 접수일 최종 값: {}", returnItemDTO.getCsReceivedDate());

        // 필수 필드 수동 설정 (디버깅용)
        if (returnItemDTO.getReturnTypeCode() == null || returnItemDTO.getReturnTypeCode().isEmpty()) {
            String typeCode = allParams.get("returnTypeCode");
            log.info("returnTypeCode 수동 설정: {}", typeCode);
            returnItemDTO.setReturnTypeCode(typeCode);
        }
        
        if (returnItemDTO.getReturnStatusCode() == null || returnItemDTO.getReturnStatusCode().isEmpty()) {
            String statusCode = allParams.get("returnStatusCode");
            log.info("returnStatusCode 수동 설정: {}", statusCode);
            returnItemDTO.setReturnStatusCode(statusCode != null ? statusCode : "PENDING");
        }
        
        // 필수 필드 검증
        if (returnItemDTO.getReturnTypeCode() == null || returnItemDTO.getReturnTypeCode().isEmpty()) {
            log.error("RETURN_TYPE_CODE가 null입니다!");
            redirectAttributes.addFlashAttribute("error", "유형을 선택해주세요.");
            return "redirect:/exchange/create";
        }
        
        // 현재 로그인 사용자 정보 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        log.info("현재 로그인 사용자: {}", currentUsername);
        
        if (returnItemDTO.getId() == null) {
            // 신규 등록시에만 등록자 설정
            returnItemDTO.setCreatedBy(currentUsername);
        }
        // 수정자는 항상 현재 사용자로 설정
        returnItemDTO.setUpdatedBy(currentUsername);
        
        // 🔥 대표님 요청: 날짜 필드 변경 감지 및 수정자 기록 (수정 모드일 때만)
        if (returnItemDTO.getId() != null) {
            try {
                ReturnItemDTO existingItem = returnItemService.getReturnItemById(returnItemDTO.getId());
                LocalDateTime now = LocalDateTime.now();
                
                // 회수완료일 변경 감지
                if (!Objects.equals(existingItem.getCollectionCompletedDate(), returnItemDTO.getCollectionCompletedDate())) {
                    returnItemDTO.setCollectionUpdatedBy(currentUsername);
                    returnItemDTO.setCollectionUpdatedDate(now);
                    log.info("🔥 회수완료일 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 물류확인일 변경 감지
                if (!Objects.equals(existingItem.getLogisticsConfirmedDate(), returnItemDTO.getLogisticsConfirmedDate())) {
                    returnItemDTO.setLogisticsUpdatedBy(currentUsername);
                    returnItemDTO.setLogisticsUpdatedDate(now);
                    log.info("🔥 물류확인일 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 출고일자 변경 감지
                if (!Objects.equals(existingItem.getShippingDate(), returnItemDTO.getShippingDate())) {
                    returnItemDTO.setShippingUpdatedBy(currentUsername);
                    returnItemDTO.setShippingUpdatedDate(now);
                    log.info("🔥 출고일자 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 환불일자 변경 감지
                if (!Objects.equals(existingItem.getRefundDate(), returnItemDTO.getRefundDate())) {
                    returnItemDTO.setRefundUpdatedBy(currentUsername);
                    returnItemDTO.setRefundUpdatedDate(now);
                    log.info("🔥 환불일자 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 완료 상태 변경 시 PROCESSOR 컬럼에 처리자 기록
                if (!Objects.equals(existingItem.getIsCompleted(), returnItemDTO.getIsCompleted()) && 
                    returnItemDTO.getIsCompleted() != null && returnItemDTO.getIsCompleted() == 1) {
                    returnItemDTO.setProcessor(currentUsername);
                    log.info("🔥 완료 상태 변경 감지 - 처리자: {}", currentUsername);
                }
                
            } catch (Exception e) {
                log.error("기존 데이터 조회 실패: {}", e.getMessage());
            }
        }
        
        // 이미지 파일 처리
        try {
            String imageUrl = processImageUpload(attachmentPhoto, attachmentImageData);
            if (imageUrl != null) {
                returnItemDTO.setDefectPhotoUrl(imageUrl);
                log.info("이미지 저장 완료: {}", imageUrl);
            }
        } catch (Exception e) {
            log.error("이미지 저장 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
            return returnItemDTO.getId() != null ? "redirect:/exchange/edit/" + returnItemDTO.getId() : "redirect:/exchange/create";
        }
        
        log.info("최종 저장 데이터: {}", returnItemDTO);
        
        try {
            if (returnItemDTO.getId() != null) {
                // 수정
                returnItemService.updateReturnItem(returnItemDTO);
                redirectAttributes.addFlashAttribute("success", "교환/반품 정보가 수정되었습니다.");
                
                // 🎯 returnTo URL이 있으면 해당 URL로, 없으면 기본 목록으로
                if (returnTo != null && !returnTo.isEmpty()) {
                    log.info("🎯 저장 후 돌아갈 URL: {}", returnTo);
                    return "redirect:" + returnTo;
                } else {
                    return "redirect:/exchange/list?success=update";
                }
            } else {
                // 등록
                ReturnItemDTO savedItem = returnItemService.createReturnItem(returnItemDTO);
                redirectAttributes.addFlashAttribute("success", "교환/반품이 등록되었습니다.");
                return "redirect:/exchange/list?success=create";
            }
        } catch (Exception e) {
            log.error("교환/반품 저장 실패 - 오류: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다: " + e.getMessage());
            
            if (returnItemDTO.getId() != null) {
                String errorUrl = "/exchange/edit/" + returnItemDTO.getId() + "?error=true";
                if (returnTo != null && !returnTo.isEmpty()) {
                    errorUrl += "&returnTo=" + java.net.URLEncoder.encode(returnTo, java.nio.charset.StandardCharsets.UTF_8);
                }
                return "redirect:" + errorUrl;
            } else {
                return "redirect:/exchange/create?error=true";
            }
        }
    }

    /**
     * 교환/반품 삭제
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("교환/반품 삭제 - ID: {}", id);
        
        try {
            returnItemService.deleteReturnItem(id);
            redirectAttributes.addFlashAttribute("message", "교환/반품이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("교환/반품 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/exchange/list";
    }

    /**
     * 상태별 목록 조회
     */
    @GetMapping("/status/{status}")
    public String listByStatus(
            @PathVariable String status,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("상태별 교환/반품 목록 조회 - 상태: {}", status);
        
        try {
            ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
            Page<ReturnItemDTO> returnItems = returnItemService.findByStatus(returnStatus, page, size);
            Map<ReturnStatus, Long> statusCounts = returnItemService.getStatusCounts();
            
            model.addAttribute("returnItems", returnItems);
            model.addAttribute("statusCounts", statusCounts);
            model.addAttribute("currentStatus", returnStatus);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", returnItems.getTotalPages());
            model.addAttribute("totalItems", returnItems.getTotalElements());
            model.addAttribute("returnStatuses", ReturnStatus.values());
            model.addAttribute("returnTypes", ReturnType.values());
            
            return "exchange/list";
        } catch (Exception e) {
            log.error("상태별 목록 조회 실패 - 상태: {}, 오류: {}", status, e.getMessage());
            return "redirect:/exchange/list";
        }
    }

    // ========== AJAX API 엔드포인트 ==========

    /**
     * 상태 변경 (AJAX)
     */
    @PostMapping("/api/{id}/status")
    @ResponseBody
    public ResponseEntity<ReturnItemDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("교환/반품 상태 변경 - ID: {}, 상태: {}", id, status);
        
        try {
            ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
            ReturnItemDTO updatedItem = returnItemService.updateStatus(id, returnStatus);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            log.error("상태 변경 실패 - ID: {}, 상태: {}, 오류: {}", id, status, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 삭제 (AJAX)
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAjax(@PathVariable Long id) {
        log.info("교환/반품 삭제 (AJAX) - ID: {}", id);
        
        try {
            returnItemService.deleteReturnItem(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("삭제 실패 (AJAX) - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 상태별 통계 조회 (AJAX)
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<ReturnStatus, Long>> getStats() {
        try {
            Map<ReturnStatus, Long> statusCounts = returnItemService.getStatusCounts();
            return ResponseEntity.ok(statusCounts);
        } catch (Exception e) {
            log.error("통계 조회 실패 - 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 미처리 항목 조회 (AJAX)
     */
    @GetMapping("/api/unprocessed")
    @ResponseBody
    public ResponseEntity<List<ReturnItemDTO>> getUnprocessedItems() {
        try {
            List<ReturnItemDTO> unprocessedItems = returnItemService.findUnprocessed();
            return ResponseEntity.ok(unprocessedItems);
        } catch (Exception e) {
            log.error("미처리 항목 조회 실패 - 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 이미지 업로드 처리 (파일 업로드 또는 붙여넣기)
     */
    private String processImageUpload(MultipartFile attachmentPhoto, String attachmentImageData) throws IOException {
        // 1. 파일 업로드가 있는 경우
        if (attachmentPhoto != null && !attachmentPhoto.isEmpty()) {
            log.info("파일 업로드 처리: {}", attachmentPhoto.getOriginalFilename());
            return saveUploadedFile(attachmentPhoto);
        }
        
        // 2. 붙여넣기 이미지 데이터가 있는 경우
        if (attachmentImageData != null && !attachmentImageData.trim().isEmpty()) {
            log.info("붙여넣기 이미지 처리");
            return savePastedImage(attachmentImageData);
        }
        
        return null;
    }
    
    /**
     * 업로드된 파일 저장
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        // 업로드 디렉토리 준비
        Path uploadsPath = Paths.get(uploadBaseDir);
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        
        // images 서브디렉토리 생성
        Path imagesDir = uploadsPath.resolve("images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        
        // 파일명 생성 (중복 방지)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String uniqueFilename = UUID.randomUUID().toString() + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                            fileExtension;
        
        // 파일 저장
        Path filePath = imagesDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("파일 저장 완료: {}", filePath);
        
        // 웹에서 접근 가능한 상대 경로 반환
        return "images/" + uniqueFilename;
    }
    
    /**
     * 붙여넣기 이미지 저장
     */
    private String savePastedImage(String base64Data) throws IOException {
        // Base64 데이터에서 타입과 실제 데이터 분리
        String[] parts = base64Data.split(",");
        String imageData = parts.length > 1 ? parts[1] : parts[0];
        
        // 파일 확장자 결정
        String fileExtension = ".png"; // 기본값
        
        if (parts.length > 1 && parts[0].contains("image/")) {
            String mimeInfo = parts[0];
            if (mimeInfo.contains("image/jpeg")) {
                fileExtension = ".jpg";
            } else if (mimeInfo.contains("image/png")) {
                fileExtension = ".png";
            } else if (mimeInfo.contains("image/gif")) {
                fileExtension = ".gif";
            }
        }
        
        // 업로드 디렉토리 준비
        Path uploadsPath = Paths.get(uploadBaseDir);
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        
        // images 서브디렉토리 생성
        Path imagesDir = uploadsPath.resolve("images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        
        // 파일명 생성 (중복 방지)
        String uniqueFilename = "pasted_" + UUID.randomUUID().toString() + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                            fileExtension;
        
        // Base64 디코딩하여 파일 저장
        Path filePath = imagesDir.resolve(uniqueFilename);
        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        Files.write(filePath, imageBytes);
        
        log.info("붙여넣기 이미지 저장 완료: {}", filePath);
        
        // 웹에서 접근 가능한 상대 경로 반환
        return "images/" + uniqueFilename;
    }

    /**
     * 🎯 AJAX 필터링 - HTML 반환 (빠른 필터링)
     */
    @PostMapping("/api/filter-html")
    @ResponseBody
    public String filterByCardAjaxHtml(@RequestBody Map<String, Object> request, Model model) {
        try {
            String filterType = (String) request.get("filterType");
            Integer page = (Integer) request.getOrDefault("page", 0);
            Integer size = (Integer) request.getOrDefault("size", 20);
            
            log.info("🚀 AJAX 필터링 요청 - 필터: {}, 페이지: {}", filterType, page);
            
            // 검색 조건 설정
            ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
            searchDTO.setPage(page);
            searchDTO.setSize(size);
            searchDTO.setSortBy("id");
            searchDTO.setSortDir("DESC");
            
            // 기본 검색 조건이 있으면 추가
            if (request.containsKey("keyword")) {
                searchDTO.setKeyword((String) request.get("keyword"));
            }
            if (request.containsKey("startDate")) {
                searchDTO.setStartDate(LocalDate.parse((String) request.get("startDate")));
            }
            if (request.containsKey("endDate")) {
                searchDTO.setEndDate(LocalDate.parse((String) request.get("endDate")));
            }
            
            // 필터 적용
            Page<ReturnItemDTO> returnItems = applySingleFilter(filterType, searchDTO);
            
            // 모델에 데이터 추가
            model.addAttribute("returnItems", returnItems);
            model.addAttribute("currentPage", page);
            
            log.info("✅ AJAX 필터링 완료 - 결과: {}건", returnItems.getTotalElements());
            
            // 테이블 섹션만 반환
            return "exchange/table-section";
            
        } catch (Exception e) {
            log.error("❌ AJAX 필터링 실패: {}", e.getMessage(), e);
            model.addAttribute("returnItems", Page.empty(PageRequest.of(0, 20)));
            model.addAttribute("currentPage", 0);
            return "exchange/table-section";
        }
    }

    /**
     * 🎯 실시간 통계 조회 (검색 조건 적용)
     */
    @GetMapping("/api/realtime-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRealtimeStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("📊 실시간 통계 조회 - 검색: {}, 시작일: {}, 종료일: {}", keyword, startDate, endDate);
            
            // 검색 조건이 있으면 검색 통계, 없으면 전체 통계
            Map<String, Object> stats = new HashMap<>();
            
            if (StringUtils.hasText(keyword) || startDate != null || endDate != null) {
                // 검색 조건이 있는 경우
                ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
                searchDTO.setKeyword(keyword);
                searchDTO.setStartDate(startDate);
                searchDTO.setEndDate(endDate);
                
                // 검색 통계 조회
                Map<String, Object> searchStats = returnItemStatsService.getRealtimeStats(searchDTO);
                stats.putAll(searchStats);
            } else {
                // 전체 통계 조회
                Map<String, Object> allStats = returnItemStatsService.getAllStats();
                stats.putAll(allStats);
            }
            
            // 카드 통계 형태로 변환
            Map<String, Object> cardStats = new HashMap<>();
            cardStats.put("collectionCompletedCount", getLongValue(stats, "COLLECTION_COMPLETED"));
            cardStats.put("collectionPendingCount", getLongValue(stats, "COLLECTION_PENDING"));
            cardStats.put("logisticsConfirmedCount", getLongValue(stats, "LOGISTICS_CONFIRMED"));
            cardStats.put("logisticsPendingCount", getLongValue(stats, "LOGISTICS_PENDING"));
            cardStats.put("exchangeShippedCount", getLongValue(stats, "EXCHANGE_SHIPPED"));
            cardStats.put("exchangeNotShippedCount", getLongValue(stats, "EXCHANGE_NOT_SHIPPED"));
            cardStats.put("returnRefundedCount", getLongValue(stats, "RETURN_REFUNDED"));
            cardStats.put("returnNotRefundedCount", getLongValue(stats, "RETURN_NOT_REFUNDED"));
            cardStats.put("paymentCompletedCount", getLongValue(stats, "PAYMENT_COMPLETED"));
            cardStats.put("paymentPendingCount", getLongValue(stats, "PAYMENT_PENDING"));
            cardStats.put("completedCount", getLongValue(stats, "COMPLETED_COUNT"));
            cardStats.put("incompletedCount", getLongValue(stats, "INCOMPLETED_COUNT"));
            cardStats.put("overdueTenDaysCount", getLongValue(stats, "OVERDUE_TEN_DAYS"));
            cardStats.put("todayCount", getLongValue(stats, "TODAY_COUNT"));
            
            log.info("✅ 실시간 통계 조회 완료");
            
            return ResponseEntity.ok(cardStats);
            
        } catch (Exception e) {
            log.error("❌ 실시간 통계 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(createDefaultStats());
        }
    }

    /**
     * 기본 통계 생성
     */
    private Map<String, Object> createDefaultStats() {
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("collectionCompletedCount", 0L);
        defaultStats.put("collectionPendingCount", 0L);
        defaultStats.put("logisticsConfirmedCount", 0L);
        defaultStats.put("logisticsPendingCount", 0L);
        defaultStats.put("exchangeShippedCount", 0L);
        defaultStats.put("exchangeNotShippedCount", 0L);
        defaultStats.put("returnRefundedCount", 0L);
        defaultStats.put("returnNotRefundedCount", 0L);
        defaultStats.put("paymentCompletedCount", 0L);
        defaultStats.put("paymentPendingCount", 0L);
        defaultStats.put("completedCount", 0L);
        defaultStats.put("incompletedCount", 0L);
        defaultStats.put("overdueTenDaysCount", 0L);
        defaultStats.put("todayCount", 0L);
        return defaultStats;
    }

         /**
      * 안전한 Long 값 추출
      */
     private Long getLongValue(Map<String, Object> map, String key) {
         Object value = map.get(key);
         if (value instanceof Number) {
             return ((Number) value).longValue();
         }
         return 0L;
     }
     
     /**
     * 🎯 대표님 요청: 리스트에서 직접 날짜 일괄 수정 API
     */
    @PostMapping("/api/bulk-update-dates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkUpdateDates(
            @RequestBody List<ReturnItemBulkDateUpdateDTO> updates) {
        
        log.info("일괄 날짜 업데이트 요청 - 대상 건수: {}", updates.size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 유효성 검사
            if (updates == null || updates.isEmpty()) {
                response.put("success", false);
                response.put("message", "업데이트할 데이터가 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 현재 로그인 사용자 정보 가져오기 (기존 등록 방식과 동일)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String updatedBy = authentication.getName();
            log.info("날짜 수정자: {}", updatedBy);
            
            // 모든 업데이트 항목에 수정자 정보 추가
            for (ReturnItemBulkDateUpdateDTO update : updates) {
                update.setUpdatedBy(updatedBy);
            }
            
            // 일괄 업데이트 실행
            int updatedCount = returnItemService.bulkUpdateDates(updates);
            
            response.put("success", true);
            response.put("message", "성공적으로 업데이트되었습니다.");
            response.put("updatedCount", updatedCount);
            response.put("totalCount", updates.size());
            response.put("updatedBy", updatedBy);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("일괄 날짜 업데이트 성공 - 업데이트된 건수: {}/{}, 수정자: {}", updatedCount, updates.size(), updatedBy);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일괄 날짜 업데이트 실패", e);
            
            response.put("success", false);
            response.put("message", "업데이트 중 오류가 발생했습니다: " + e.getMessage());
            response.put("updatedCount", 0);
            response.put("totalCount", updates.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 🎯 대표님 요청: 완료 상태 업데이트 API
     */
    @PostMapping("/api/update-completion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCompletionStatus(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long id = Long.valueOf(request.get("id").toString());
            Boolean isCompleted = (Boolean) request.get("isCompleted");
            
            log.info("완료 상태 업데이트 요청 - ID: {}, 완료: {}", id, isCompleted);
            
            // 완료 상태 업데이트 실행
            boolean updated = returnItemService.updateCompletionStatus(id, isCompleted);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "완료 상태가 성공적으로 업데이트되었습니다.");
                log.info("완료 상태 업데이트 성공 - ID: {}, 완료: {}", id, isCompleted);
            } else {
                response.put("success", false);
                response.put("message", "해당 항목을 찾을 수 없습니다.");
                log.warn("완료 상태 업데이트 실패 - ID: {} (항목을 찾을 수 없음)", id);
            }
            
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("완료 상태 업데이트 실패", e);
            
            response.put("success", false);
            response.put("message", "완료 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 🎯 다중 필터 + 검색 조건 함께 적용 메서드
     */
    private Page<ReturnItemDTO> applyMultipleFiltersWithSearch(String filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 + 검색 처리 시작 - filters: {}, 검색조건: {}", filters, searchDTO.getKeyword());
        
        // 필터를 쉼표로 분리
        String[] filterArray = filters.split(",");
        List<String> filterList = Arrays.asList(filterArray);
        
        Page<ReturnItemDTO> result = null;
        
        try {
            // 🎯 서비스에서 필터와 검색을 함께 처리
            result = returnItemService.findByMultipleFiltersWithSearch(filterList, searchDTO);
            log.info("✅ 다중 필터 + 검색 적용 완료 - 필터: {}, 검색: {}, 결과: {} 건", 
                filterList, searchDTO.getKeyword(), result.getTotalElements());
            
        } catch (Exception e) {
            log.error("❌ 다중 필터 + 검색 적용 중 오류 발생, fallback 처리", e);
            
            // fallback: 검색만 적용
            result = returnItemService.search(searchDTO);
            log.info("🔄 검색만 적용으로 fallback - 결과: {} 건", result.getTotalElements());
        }
        
        return result;
    }
    
    /**
     * 🎯 다중 필터 적용 메서드
     */
    private Page<ReturnItemDTO> applyMultipleFilters(String filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 처리 시작 - filters: {}", filters);
        
        // 필터를 쉼표로 분리
        String[] filterArray = filters.split(",");
        List<String> filterList = Arrays.asList(filterArray);
        
        // 각 필터 타입별로 결과를 가져온 후 교집합 처리
        // 하지만 교집합 처리는 복잡하므로, 우선 첫 번째 필터를 적용하고 
        // 추가 필터는 서비스 레이어에서 처리하도록 함
        
        Page<ReturnItemDTO> result = null;
        
        try {
            // 다중 필터를 서비스에 전달
            result = returnItemService.findByMultipleFilters(filterList, searchDTO);
            log.info("✅ 다중 필터 적용 완료 - 필터: {}, 결과: {} 건", filterList, result.getTotalElements());
            
        } catch (Exception e) {
            log.error("❌ 다중 필터 적용 중 오류 발생, 개별 필터로 fallback 처리", e);
            
            // 서비스에서 다중 필터를 지원하지 않는 경우 첫 번째 필터만 적용
            if (filterList.size() > 0) {
                String firstFilter = filterList.get(0).trim();
                log.info("🔄 첫 번째 필터로 fallback: {}", firstFilter);
                result = applySingleFilter(firstFilter, searchDTO);
            } else {
                // 필터가 없으면 전체 조회
                result = returnItemService.findAll(searchDTO.getPage(), searchDTO.getSize(), 
                    searchDTO.getSortBy(), searchDTO.getSortDir());
            }
        }
        
        return result;
    }
    
    /**
     * 🎯 단일 필터 적용 메서드 (기존 로직 재사용)
     */
    private Page<ReturnItemDTO> applySingleFilter(String filterType, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 단일 필터 적용 - filterType: {}", filterType);
        
        try {
            switch (filterType) {
                case "collection-completed":
                    return returnItemService.findByCollectionCompleted(searchDTO);
                case "collection-pending":
                    return returnItemService.findByCollectionPending(searchDTO);
                case "logistics-confirmed":
                    return returnItemService.findByLogisticsConfirmed(searchDTO);
                case "logistics-pending":
                    return returnItemService.findByLogisticsPending(searchDTO);
                case "shipping-completed":
                    return returnItemService.findByShippingCompleted(searchDTO);
                case "shipping-pending":
                    return returnItemService.findByShippingPending(searchDTO);
                case "refund-completed":
                    return returnItemService.findByRefundCompleted(searchDTO);
                case "refund-pending":
                    return returnItemService.findByRefundPending(searchDTO);
                case "payment-completed":
                    return returnItemService.findByPaymentCompleted(searchDTO);
                case "payment-pending":
                    return returnItemService.findByPaymentPending(searchDTO);
                case "completed":
                    return returnItemService.findByCompleted(searchDTO);
                case "incompleted":
                    return returnItemService.findByIncompleted(searchDTO);
                case "overdue-ten-days":
                    return returnItemService.findOverdueTenDays(searchDTO);
                default:
                    log.warn("⚠️ 알 수 없는 필터 타입: {}", filterType);
                    return returnItemService.findAll(searchDTO.getPage(), searchDTO.getSize(), 
                        searchDTO.getSortBy(), searchDTO.getSortDir());
            }
        } catch (Exception e) {
            log.error("❌ 단일 필터 적용 중 오류 발생: {}", e.getMessage(), e);
            return returnItemService.findAll(searchDTO.getPage(), searchDTO.getSize(), 
                searchDTO.getSortBy(), searchDTO.getSortDir());
        }
    }
} 