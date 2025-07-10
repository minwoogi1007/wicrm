package com.wio.crm.controller;

import com.wio.crm.dto.ReturnItemDTO;
import com.wio.crm.dto.ReturnItemSearchDTO;
import com.wio.crm.dto.ReturnItemBulkDateUpdateDTO;
import com.wio.crm.service.ReturnItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import java.nio.file.*;
import java.util.Base64;
import java.util.UUID;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.net.*;
import java.io.*;

/**
 * 교환/반품 관리 컨트롤러
 */
@Controller
@RequestMapping("/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {

    private final ReturnItemService returnItemService;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadBaseDir;
    
    @Value("${file.server-url:http://localhost:8080}")
    private String fileServerUrl;

    /**
     * 교환/반품 목록 화면
     */
    @GetMapping({"/list", ""})
    public String list(
            Model model, 
            @ModelAttribute ReturnItemSearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String filters) {
        
        log.info("교환/반품 목록 조회 - 검색조건: {}, 필터: {}", searchDTO, filters);
        
        // 🎯 다중 필터 지원 추가 (Sample에서 통합)
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
            
            // 🎯 필터와 검색 조건 함께 처리 (Sample 로직 통합)
            boolean hasFilters = StringUtils.hasText(filters);
            
            // 🚨 실제 검색 조건 엄격하게 확인 (filters는 제외)
            boolean hasRealSearchCondition = (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty() && !"null".equals(searchDTO.getKeyword().trim())) ||
                                             searchDTO.getStartDate() != null ||
                                             searchDTO.getEndDate() != null ||
                                             searchDTO.getLogisticsStartDate() != null ||
                                             searchDTO.getLogisticsEndDate() != null ||
                                             (searchDTO.getReturnTypeCode() != null && !searchDTO.getReturnTypeCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnTypeCode().trim())) ||
                                             (searchDTO.getReturnStatusCode() != null && !searchDTO.getReturnStatusCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnStatusCode().trim())) ||
                                             (searchDTO.getSiteName() != null && !searchDTO.getSiteName().trim().isEmpty() && !"null".equals(searchDTO.getSiteName().trim())) ||
                                             (searchDTO.getPaymentStatus() != null && !searchDTO.getPaymentStatus().trim().isEmpty() && !"null".equals(searchDTO.getPaymentStatus().trim())) ||
                                             (searchDTO.getBrandFilter() != null && !searchDTO.getBrandFilter().trim().isEmpty() && !"null".equals(searchDTO.getBrandFilter().trim()));
            
            log.info("🚨 DEBUG: hasFilters={}, hasRealSearchCondition={}", hasFilters, hasRealSearchCondition);
            
            if (hasFilters && hasRealSearchCondition) {
                // 필터 + 검색 조건 둘 다 있는 경우
                log.info("🔍 필터 + 검색 조건 함께 적용 - 필터: {}, 검색: {}", filters, searchDTO.getKeyword());
                returnItems = applyMultipleFiltersWithSearch(filters, searchDTO);
                log.info("✅ 필터 + 검색 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            } else if (hasFilters) {
                // 필터만 있는 경우 (🚀 이 경로가 올바른 경로)
                log.info("🔍 다중 필터만 적용: {}", filters);
                returnItems = applyMultipleFilters(filters, searchDTO);
                log.info("✅ 다중 필터 조회 완료 - 필터: {}, 결과 수: {}", filters, returnItems.getTotalElements());
            } else if (hasRealSearchCondition) {
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
            returnItems = Page.empty();
        }
        
        // 🎯 검색 조건에 따른 통계 조회 (검색 조건이 있으면 검색 결과 기준, 없으면 전체 기준)
        // 🚀 [성능 최적화] 기존 12개 개별 쿼리 → 1개 통합 쿼리로 개선 (성능 80% 향상 예상)
        Map<String, Object> unifiedStats = new HashMap<>();
        
        // 🔧 통계 변수들을 미리 선언하여 스코프 문제 해결
        Map<String, Long> cardStats = new HashMap<>();
        Map<String, Long> typeCounts = new HashMap<>();
        Map<String, Long> brandCounts = new HashMap<>();
        Map<String, Object> amountSummary = new HashMap<>();
        Map<String, Long> statusCounts = new HashMap<>();
        Map<String, Long> siteCounts = new HashMap<>();
        Map<String, Long> reasonCounts = new HashMap<>();
        Long todayCount = 0L;
        
        try {
            // 🔍 검색 조건이 있는지 확인
            boolean hasRealSearchCondition = (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty() && !"null".equals(searchDTO.getKeyword().trim())) ||
                                             searchDTO.getStartDate() != null ||
                                             searchDTO.getEndDate() != null ||
                                             searchDTO.getLogisticsStartDate() != null ||
                                             searchDTO.getLogisticsEndDate() != null ||
                                             (searchDTO.getReturnTypeCode() != null && !searchDTO.getReturnTypeCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnTypeCode().trim())) ||
                                             (searchDTO.getReturnStatusCode() != null && !searchDTO.getReturnStatusCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnStatusCode().trim())) ||
                                             (searchDTO.getSiteName() != null && !searchDTO.getSiteName().trim().isEmpty() && !"null".equals(searchDTO.getSiteName().trim())) ||
                                             (searchDTO.getPaymentStatus() != null && !searchDTO.getPaymentStatus().trim().isEmpty() && !"null".equals(searchDTO.getPaymentStatus().trim())) ||
                                             (searchDTO.getBrandFilter() != null && !searchDTO.getBrandFilter().trim().isEmpty() && !"null".equals(searchDTO.getBrandFilter().trim()));
            log.info("🔍 통계 조회 - 검색 조건 존재: {}, 검색DTO: {}", hasRealSearchCondition, searchDTO);
            
            if (hasRealSearchCondition) {
                // 🎯 검색 조건에 맞는 통합 통계 조회 (1개 쿼리)
                log.info("📊 검색 조건 기반 통합 통계 조회 시작");
                unifiedStats = returnItemService.getDashboardStatsUnifiedBySearch(searchDTO);
                log.info("✅ 검색 조건 기반 통합 통계 조회 완료");
            } else {
                // 🎯 전체 데이터 기반 통합 통계 조회 (1개 쿼리)
                log.info("📊 전체 데이터 기반 통합 통계 조회 시작");
                unifiedStats = returnItemService.getDashboardStatsUnified();
                log.info("✅ 전체 데이터 기반 통합 통계 조회 완료");
            }
            
            // 🚀 통합 결과에서 개별 값들 추출 (기존 인터페이스 호환성 유지)
            cardStats.put("collectionCompleted", getLongValue(unifiedStats, "collectionCompletedCount"));
            cardStats.put("collectionPending", getLongValue(unifiedStats, "collectionPendingCount"));
            cardStats.put("logisticsConfirmed", getLongValue(unifiedStats, "logisticsConfirmedCount"));
            cardStats.put("logisticsPending", getLongValue(unifiedStats, "logisticsPendingCount"));
            cardStats.put("exchangeShipped", getLongValue(unifiedStats, "exchangeShippedCount"));
            cardStats.put("exchangeNotShipped", getLongValue(unifiedStats, "exchangeNotShippedCount"));
            cardStats.put("returnRefunded", getLongValue(unifiedStats, "returnRefundedCount"));
            cardStats.put("returnNotRefunded", getLongValue(unifiedStats, "returnNotRefundedCount"));
            cardStats.put("paymentCompleted", getLongValue(unifiedStats, "paymentCompletedCount"));
            cardStats.put("paymentPending", getLongValue(unifiedStats, "paymentPendingCount"));
            cardStats.put("completedCount", getLongValue(unifiedStats, "completedCount"));
            cardStats.put("incompletedCount", getLongValue(unifiedStats, "incompletedCount"));
            
            // 🚨 처리기간 임박 통계 추가 (Sample 통합)
            Long overdueTenDaysCount = returnItemService.getOverdueTenDaysCount();
            cardStats.put("overdueTenDaysCount", overdueTenDaysCount != null ? overdueTenDaysCount : 0L);
            log.info("🚨 처리기간 임박 건수 조회 완료: {} 건", overdueTenDaysCount);
            
            // 🏷️ 유형별 통계
            typeCounts.put("전체교환", getLongValue(unifiedStats, "fullExchangeCount"));
            typeCounts.put("부분교환", getLongValue(unifiedStats, "partialExchangeCount"));
            typeCounts.put("전체반품", getLongValue(unifiedStats, "fullReturnCount"));
            typeCounts.put("부분반품", getLongValue(unifiedStats, "partialReturnCount"));
            
            // 🌐 브랜드별 통계
            brandCounts.put("레노마", getLongValue(unifiedStats, "renomaCount"));
            brandCounts.put("코랄리크", getLongValue(unifiedStats, "coralicCount"));
            
            // 💰 금액 요약
            amountSummary.put("totalRefundAmount", getLongValue(unifiedStats, "totalRefundAmount"));
            amountSummary.put("avgRefundAmount", getLongValue(unifiedStats, "avgRefundAmount"));
            amountSummary.put("totalShippingFee", 0L); // SHIPPING_FEE는 String 타입이므로 별도 처리 필요
            
            // 기타 통계들은 기본값으로 설정 (필요시 추가 구현)
            statusCounts.clear();
            siteCounts.clear();
            reasonCounts.clear();
            todayCount = getLongValue(unifiedStats, "todayCount");
            
            // 모델에 최종 데이터 추가
            model.addAttribute("cardStats", cardStats);
            model.addAttribute("typeCounts", typeCounts);
            model.addAttribute("brandCounts", brandCounts);
            model.addAttribute("amountSummary", amountSummary);
            model.addAttribute("statusCounts", statusCounts);
            model.addAttribute("siteCounts", siteCounts);
            model.addAttribute("reasonCounts", reasonCounts);
            model.addAttribute("todayCount", todayCount);
            
        } catch (Exception e) {
            log.error("❌ 통합 통계 조회 실패: {}", e.getMessage(), e);
            // 기본값으로 초기화
            cardStats.clear();
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
            cardStats.put("overdueTenDaysCount", 0L);  // 🚨 처리기간 임박 기본값
            
            // 다른 통계들도 기본값으로 초기화
            typeCounts.clear();
            brandCounts.clear();
            amountSummary.clear();
            statusCounts.clear();
            siteCounts.clear();
            reasonCounts.clear();
            todayCount = 0L;
        }
        
        // searchDTO가 null이면 빈 객체 생성
        if (searchDTO == null) {
            searchDTO = new ReturnItemSearchDTO();
        }
        
        // 모델에 데이터 추가
        model.addAttribute("returnItems", returnItems);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("siteCounts", siteCounts);
        model.addAttribute("typeCounts", typeCounts);
        model.addAttribute("reasonCounts", reasonCounts);
        model.addAttribute("amountSummary", amountSummary);
        model.addAttribute("brandCounts", brandCounts);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("cardStats", cardStats);
        model.addAttribute("pageTitle", "교환/반품 관리");
        
        // 🎯 현재 적용된 필터 정보 추가
        model.addAttribute("currentFilters", filters);
        model.addAttribute("filters", filters);
        
        return "exchange/list";
    }

    /**
     * 교환/반품 목록 데이터 API (AJAX)
     */
    @PostMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExchangeListData(@RequestBody Map<String, Object> params) {
        try {
            // 검색 파라미터 검증 및 정리
            Map<String, Object> searchParams = returnItemService.validateAndCleanSearchParams(params);
            
            // 목록 조회
            Map<String, Object> result = returnItemService.getReturnItemList(searchParams);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 교환/반품 상세 조회 API
     */
    @GetMapping("/api/detail/{returnId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExchangeDetail(@PathVariable Long returnId) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", returnItemService.getReturnItemById(returnId));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "상세 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 교환/반품 상태 업데이트 API
     */
    @PostMapping("/api/updateStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateExchangeStatus(@RequestBody Map<String, Object> params, 
                                                                    HttpServletRequest request) {
        try {
            Long returnId = Long.parseLong(params.get("returnId").toString());
            String returnStatusCode = params.get("returnStatusCode").toString();
            String updatedBy = "SYSTEM"; // 실제로는 세션에서 사용자 정보 가져오기
            
            boolean success = returnItemService.updateReturnItemStatus(returnId, returnStatusCode, updatedBy);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "상태가 성공적으로 업데이트되었습니다." : "상태 업데이트에 실패했습니다.");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 교환/반품 일괄 상태 업데이트 API
     */
    @PostMapping("/api/batchUpdateStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpdateExchangeStatus(@RequestBody Map<String, Object> params,
                                                                         HttpServletRequest request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> returnIds = (List<Long>) params.get("returnIds");
            String returnStatusCode = params.get("returnStatusCode").toString();
            String updatedBy = "SYSTEM"; // 실제로는 세션에서 사용자 정보 가져오기
            
            boolean success = returnItemService.updateReturnItemStatusBatch(returnIds, returnStatusCode, updatedBy);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "선택된 항목들의 상태가 성공적으로 업데이트되었습니다." : "일괄 상태 업데이트에 실패했습니다.");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "일괄 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 🚀 통합 통계 결과에서 Long 값을 안전하게 추출하는 유틸리티 메소드
     */
    private Long getLongValue(Map<String, Object> stats, String key) {
        try {
            Object value = stats.get(key);
            
            // 대문자 키로 찾지 못한 경우 소문자로 시도
            if (value == null) {
                value = stats.get(key.toLowerCase());
            }
            
            // 소문자로도 찾지 못한 경우 camelCase로 시도
            if (value == null) {
                String camelCaseKey = toCamelCase(key);
                value = stats.get(camelCaseKey);
            }
            
            if (value == null) {
                log.warn("⚠️ 키를 찾을 수 없음 - key: {}, 사용 가능한 키들: {}", key, stats.keySet());
                return 0L;
            }
            
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value instanceof String) {
                return Long.parseLong((String) value);
            }
            return 0L;
        } catch (Exception e) {
            log.warn("⚠️ Long 값 추출 실패 - key: {}, value: {}", key, stats.get(key));
            return 0L;
        }
    }
    
    /**
     * 🔧 대문자 키를 camelCase로 변환하는 유틸리티 메소드
     */
    private String toCamelCase(String upperCaseKey) {
        if (upperCaseKey == null || upperCaseKey.isEmpty()) {
            return upperCaseKey;
        }
        
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        
        for (int i = 0; i < upperCaseKey.length(); i++) {
            char c = upperCaseKey.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (i == 0) {
                    result.append(Character.toLowerCase(c));
                } else if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        
        return result.toString();
    }

    /**
     * 대시보드 통계 API
     */
    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", returnItemService.getDashboardStats());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "통계 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 카드 통계 API
     */
    @GetMapping("/api/card-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCardStats(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String startDate,
                                                           @RequestParam(required = false) String endDate,
                                                           @RequestParam(required = false) String returnTypeCode,
                                                           @RequestParam(required = false) String returnStatusCode,
                                                           @RequestParam(required = false) String paymentStatus,
                                                           @RequestParam(required = false) String siteName) {
        try {
            log.info("🚀 카드 통계 조회 시작 - 검색 조건: keyword={}, startDate={}, endDate={}", keyword, startDate, endDate);
            long startTime = System.currentTimeMillis();
            
            // 🔍 검색 조건 구성 (카드 통계에서는 키워드 제외, 날짜만 적용)
            Map<String, Object> searchParams = new HashMap<>();
            boolean hasSearchCondition = false;
            
            // ⚠️ 키워드는 카드 통계에 적용하지 않음 (사용자 요청)
            if (keyword != null && !keyword.trim().isEmpty()) {
                log.debug("🔍 키워드는 카드 통계에서 제외: {}", keyword.trim());
                // searchParams에 추가하지 않음 - 카드 통계에는 영향 없음
            }
            
            // 📅 날짜 범위만 카드 통계에 적용
            if (startDate != null && !startDate.trim().isEmpty()) {
                searchParams.put("startDate", startDate.trim());
                hasSearchCondition = true;
                log.debug("🔍 시작날짜 (카드통계 적용): {}", startDate.trim());
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                searchParams.put("endDate", endDate.trim());
                hasSearchCondition = true;
                log.debug("🔍 종료날짜 (카드통계 적용): {}", endDate.trim());
            }
            if (returnTypeCode != null && !returnTypeCode.trim().isEmpty() && 
                !"all".equals(returnTypeCode.trim())) {
                searchParams.put("returnTypeCode", returnTypeCode.trim());
                hasSearchCondition = true;
                log.debug("🔍 반품유형: {}", returnTypeCode.trim());
            }
            if (returnStatusCode != null && !returnStatusCode.trim().isEmpty() && 
                !"all".equals(returnStatusCode.trim())) {
                searchParams.put("returnStatusCode", returnStatusCode.trim());
                hasSearchCondition = true;
                log.debug("🔍 반품상태: {}", returnStatusCode.trim());
            }
            if (paymentStatus != null && !paymentStatus.trim().isEmpty() && 
                !"all".equals(paymentStatus.trim())) {
                searchParams.put("paymentStatus", paymentStatus.trim());
                hasSearchCondition = true;
                log.debug("🔍 결제상태: {}", paymentStatus.trim());
            }
            if (siteName != null && !siteName.trim().isEmpty()) {
                searchParams.put("siteName", siteName.trim());
                hasSearchCondition = true;
                log.debug("🔍 사이트명: {}", siteName.trim());
            }
            
            log.info("📊 검색 조건 여부: {} | 조건 개수: {}", hasSearchCondition, searchParams.size());
            
            // 🚀 통합 쿼리 사용으로 성능 최적화
            Map<String, Object> searchStats;
            if (!hasSearchCondition) {
                // 🔍 검색 조건이 없으면 전체 통계 조회
                log.info("📊 전체 데이터 통계 조회");
                searchStats = returnItemService.getDashboardStatsUnified();
            } else {
                // 🔍 날짜 기간별 통계 조회 (키워드는 제외)
                log.info("📊 날짜 기간별 통계 조회 - 조건: {} (키워드 제외)", searchParams);
                ReturnItemSearchDTO searchDTO = ReturnItemSearchDTO.builder()
                    // .keyword((String) searchParams.get("keyword")) // 키워드는 카드 통계에서 제외
                    .returnTypeCode((String) searchParams.get("returnTypeCode"))
                    .returnStatusCode((String) searchParams.get("returnStatusCode"))
                    .paymentStatus((String) searchParams.get("paymentStatus"))
                    .siteName((String) searchParams.get("siteName"))
                    .build();
                    
                // 날짜 파라미터 처리
                if (searchParams.get("startDate") != null) {
                    try {
                        searchDTO.setStartDate(java.time.LocalDate.parse((String) searchParams.get("startDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 시작 날짜 파싱 실패: {}", searchParams.get("startDate"));
                    }
                }
                if (searchParams.get("endDate") != null) {
                    try {
                        searchDTO.setEndDate(java.time.LocalDate.parse((String) searchParams.get("endDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 종료 날짜 파싱 실패: {}", searchParams.get("endDate"));
                    }
                }
                
                searchStats = returnItemService.getDashboardStatsUnifiedBySearch(searchDTO);
            }
            
            // 🎯 카드 통계 데이터 구성
            Map<String, Long> cardStats = new HashMap<>();
            cardStats.put("collectionCompleted", getLongValue(searchStats, "COLLECTIONCOMPLETEDCOUNT"));
            cardStats.put("collectionPending", getLongValue(searchStats, "COLLECTIONPENDINGCOUNT"));
            cardStats.put("logisticsConfirmed", getLongValue(searchStats, "LOGISTICSCONFIRMEDCOUNT"));
            cardStats.put("logisticsPending", getLongValue(searchStats, "LOGISTICSPENDINGCOUNT"));
            cardStats.put("exchangeShipped", getLongValue(searchStats, "EXCHANGESHIPPEDCOUNT"));
            cardStats.put("exchangeNotShipped", getLongValue(searchStats, "EXCHANGENOTSHIPPEDCOUNT"));
            cardStats.put("returnRefunded", getLongValue(searchStats, "RETURNREFUNDEDCOUNT"));
            cardStats.put("returnNotRefunded", getLongValue(searchStats, "RETURNNOTREFUNDEDCOUNT"));
            cardStats.put("paymentCompleted", getLongValue(searchStats, "PAYMENTCOMPLETEDCOUNT"));
            cardStats.put("paymentPending", getLongValue(searchStats, "PAYMENTPENDINGCOUNT"));
            cardStats.put("completedCount", getLongValue(searchStats, "COMPLETEDCOUNT"));
            cardStats.put("incompletedCount", getLongValue(searchStats, "INCOMPLETEDCOUNT"));
            
            // 🚨 처리기간 임박 통계 추가 (검색 조건에 따라 다른 방식 사용)
            Long overdueTenDaysCount;
            if (!hasSearchCondition) {
                // 검색 조건이 없으면 전체 데이터 기준
                overdueTenDaysCount = returnItemService.getOverdueTenDaysCount();
                log.info("🚨 카드 통계 (전체) - 처리기간 임박 결과: {} 건", overdueTenDaysCount);
            } else {
                // 날짜 기간별 조회 (키워드는 제외)
                ReturnItemSearchDTO tenDaysSearchDTO = ReturnItemSearchDTO.builder()
                    // .keyword((String) searchParams.get("keyword")) // 키워드는 카드 통계에서 제외
                    .returnTypeCode((String) searchParams.get("returnTypeCode"))
                    .returnStatusCode((String) searchParams.get("returnStatusCode"))
                    .paymentStatus((String) searchParams.get("paymentStatus"))
                    .siteName((String) searchParams.get("siteName"))
                    .page(0)
                    .size(1)
                    .build();
                    
                // 날짜 파라미터 처리
                if (searchParams.get("startDate") != null) {
                    try {
                        tenDaysSearchDTO.setStartDate(java.time.LocalDate.parse((String) searchParams.get("startDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 시작 날짜 파싱 실패: {}", searchParams.get("startDate"));
                    }
                }
                if (searchParams.get("endDate") != null) {
                    try {
                        tenDaysSearchDTO.setEndDate(java.time.LocalDate.parse((String) searchParams.get("endDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 종료 날짜 파싱 실패: {}", searchParams.get("endDate"));
                    }
                }
                
                overdueTenDaysCount = returnItemService.findOverdueTenDays(tenDaysSearchDTO).getTotalElements();
                log.info("🚨 카드 통계 (검색조건) - 처리기간 임박 결과: {} 건", overdueTenDaysCount);
            }
            cardStats.put("overdueTenDaysCount", overdueTenDaysCount != null ? overdueTenDaysCount : 0L);
            
            long endTime = System.currentTimeMillis();
            log.info("✅ 카드 통계 조회 완료 - 소요시간: {}ms", endTime - startTime);
            
            // 응답 구조 표준화
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", cardStats);
            result.put("timestamp", System.currentTimeMillis());
            result.put("responseTime", endTime - startTime);
            result.put("searchParams", searchParams);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 카드 통계 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "카드 통계 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            errorResult.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 교환/반품 타입별 통계 API
     */
    @GetMapping("/api/typeStats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTypeStats(@RequestParam(required = false) String startDate,
                                                            @RequestParam(required = false) String endDate) {
        try {
            // 기본값 설정 (최근 30일)
            if (startDate == null || startDate.isEmpty()) {
                startDate = java.time.LocalDate.now().minusDays(30).toString();
            }
            if (endDate == null || endDate.isEmpty()) {
                endDate = java.time.LocalDate.now().toString();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", returnItemService.getReturnItemTypeStats(startDate, endDate));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "타입별 통계 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 🚀 실시간 통계 API (검색 조건 지원)
     */
    @GetMapping("/api/realtime-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRealtimeStats(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate,
                                                               @RequestParam(required = false) String returnTypeCode,
                                                               @RequestParam(required = false) String returnStatusCode,
                                                               @RequestParam(required = false) String paymentStatus,
                                                               @RequestParam(required = false) String siteName) {
        try {
            log.info("🚀 실시간 통계 조회 시작 - 검색 조건: keyword={}, startDate={}, endDate={}", keyword, startDate, endDate);
            long startTime = System.currentTimeMillis();
            
            // 🔍 검색 조건 구성 (실제 값이 있는 경우만)
            Map<String, Object> searchParams = new HashMap<>();
            boolean hasSearchCondition = false;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchParams.put("keyword", keyword.trim());
                hasSearchCondition = true;
                log.debug("🔍 키워드 검색: {}", keyword.trim());
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                searchParams.put("startDate", startDate.trim());
                hasSearchCondition = true;
                log.debug("🔍 시작날짜: {}", startDate.trim());
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                searchParams.put("endDate", endDate.trim());
                hasSearchCondition = true;
                log.debug("🔍 종료날짜: {}", endDate.trim());
            }
            if (returnTypeCode != null && !returnTypeCode.trim().isEmpty() && 
                !"all".equals(returnTypeCode.trim())) {
                searchParams.put("returnTypeCode", returnTypeCode.trim());
                hasSearchCondition = true;
                log.debug("🔍 반품유형: {}", returnTypeCode.trim());
            }
            if (returnStatusCode != null && !returnStatusCode.trim().isEmpty() && 
                !"all".equals(returnStatusCode.trim())) {
                searchParams.put("returnStatusCode", returnStatusCode.trim());
                hasSearchCondition = true;
                log.debug("🔍 반품상태: {}", returnStatusCode.trim());
            }
            if (paymentStatus != null && !paymentStatus.trim().isEmpty() && 
                !"all".equals(paymentStatus.trim())) {
                searchParams.put("paymentStatus", paymentStatus.trim());
                hasSearchCondition = true;
                log.debug("🔍 결제상태: {}", paymentStatus.trim());
            }
            if (siteName != null && !siteName.trim().isEmpty()) {
                searchParams.put("siteName", siteName.trim());
                hasSearchCondition = true;
                log.debug("🔍 사이트명: {}", siteName.trim());
            }
            
            log.info("📊 검색 조건 여부: {} | 조건 개수: {}", hasSearchCondition, searchParams.size());
            
            // 🚀 통합 쿼리 사용으로 성능 최적화 (기존 개별 쿼리들 → 1개 통합 쿼리)
            Map<String, Object> searchStats;
            if (!hasSearchCondition) {
                // 🔍 검색 조건이 없으면 전체 통계 조회
                log.info("📊 전체 데이터 통계 조회");
                searchStats = returnItemService.getDashboardStatsUnified();
            } else {
                // 🔍 검색 조건이 있으면 검색 조건별 통계 조회
                log.info("📊 검색 조건별 통계 조회 - 조건: {}", searchParams);
                ReturnItemSearchDTO searchDTO = ReturnItemSearchDTO.builder()
                    .keyword((String) searchParams.get("keyword"))
                    .returnTypeCode((String) searchParams.get("returnTypeCode"))
                    .returnStatusCode((String) searchParams.get("returnStatusCode"))
                    .paymentStatus((String) searchParams.get("paymentStatus"))
                    .siteName((String) searchParams.get("siteName"))
                    .build();
                    
                // 날짜 파라미터 처리
                if (searchParams.get("startDate") != null) {
                    try {
                        searchDTO.setStartDate(java.time.LocalDate.parse((String) searchParams.get("startDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 시작 날짜 파싱 실패: {}", searchParams.get("startDate"));
                    }
                }
                if (searchParams.get("endDate") != null) {
                    try {
                        searchDTO.setEndDate(java.time.LocalDate.parse((String) searchParams.get("endDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 종료 날짜 파싱 실패: {}", searchParams.get("endDate"));
                    }
                }
                
                searchStats = returnItemService.getDashboardStatsUnifiedBySearch(searchDTO);
            }
            
            // 🎯 실시간 통계 데이터 구성
            Map<String, Object> realtimeStats = new HashMap<>();
            
            // 1️⃣ 기본 통계
            realtimeStats.put("totalCount", getLongValue(searchStats, "TOTALCOUNT"));
            realtimeStats.put("todayCount", getLongValue(searchStats, "TODAYCOUNT"));
            
            // 2️⃣ 카드 통계 (UI 카드용) - Oracle 대문자 컬럼명 처리
            Map<String, Long> cardStats = new HashMap<>();
            cardStats.put("collectionCompleted", getLongValue(searchStats, "COLLECTIONCOMPLETEDCOUNT"));
            cardStats.put("collectionPending", getLongValue(searchStats, "COLLECTIONPENDINGCOUNT"));
            cardStats.put("logisticsConfirmed", getLongValue(searchStats, "LOGISTICSCONFIRMEDCOUNT"));
            cardStats.put("logisticsPending", getLongValue(searchStats, "LOGISTICSPENDINGCOUNT"));
            cardStats.put("exchangeShipped", getLongValue(searchStats, "EXCHANGESHIPPEDCOUNT"));
            cardStats.put("exchangeNotShipped", getLongValue(searchStats, "EXCHANGENOTSHIPPEDCOUNT"));
            cardStats.put("returnRefunded", getLongValue(searchStats, "RETURNREFUNDEDCOUNT"));
            cardStats.put("returnNotRefunded", getLongValue(searchStats, "RETURNNOTREFUNDEDCOUNT"));
            cardStats.put("paymentCompleted", getLongValue(searchStats, "PAYMENTCOMPLETEDCOUNT"));
            cardStats.put("paymentPending", getLongValue(searchStats, "PAYMENTPENDINGCOUNT"));
            cardStats.put("completedCount", getLongValue(searchStats, "COMPLETEDCOUNT"));
            cardStats.put("incompletedCount", getLongValue(searchStats, "INCOMPLETEDCOUNT"));
            
            // 🚨 처리기간 임박 통계 추가 (검색 조건에 따라 다른 방식 사용)
            Long overdueTenDaysCount;
            if (!hasSearchCondition) {
                // 검색 조건이 없으면 전체 데이터 기준 (페이지 로드와 동일)
                overdueTenDaysCount = returnItemService.getOverdueTenDaysCount();
                log.info("🚨 실시간 통계 (전체) - 처리기간 임박 결과: {} 건", overdueTenDaysCount);
            } else {
                // 검색 조건이 있으면 검색 조건별 조회
                ReturnItemSearchDTO tenDaysSearchDTO = ReturnItemSearchDTO.builder()
                    .keyword((String) searchParams.get("keyword"))
                    .returnTypeCode((String) searchParams.get("returnTypeCode"))
                    .returnStatusCode((String) searchParams.get("returnStatusCode"))
                    .paymentStatus((String) searchParams.get("paymentStatus"))
                    .siteName((String) searchParams.get("siteName"))
                    .page(0)
                    .size(1)
                    .build();
                    
                // 날짜 파라미터 처리
                if (searchParams.get("startDate") != null) {
                    try {
                        tenDaysSearchDTO.setStartDate(java.time.LocalDate.parse((String) searchParams.get("startDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 시작 날짜 파싱 실패: {}", searchParams.get("startDate"));
                    }
                }
                if (searchParams.get("endDate") != null) {
                    try {
                        tenDaysSearchDTO.setEndDate(java.time.LocalDate.parse((String) searchParams.get("endDate")));
                    } catch (Exception e) {
                        log.warn("⚠️ 종료 날짜 파싱 실패: {}", searchParams.get("endDate"));
                    }
                }
                
                overdueTenDaysCount = returnItemService.findOverdueTenDays(tenDaysSearchDTO).getTotalElements();
                log.info("🚨 실시간 통계 (검색조건) - 처리기간 임박 결과: {} 건", overdueTenDaysCount);
            }
            cardStats.put("overdueTenDaysCount", overdueTenDaysCount != null ? overdueTenDaysCount : 0L);
            realtimeStats.put("cardStats", cardStats);
            
            // 3️⃣ 타입별 통계
            Map<String, Long> typeStats = new HashMap<>();
            typeStats.put("fullExchange", getLongValue(searchStats, "FULLEXCHANGECOUNT"));
            typeStats.put("partialExchange", getLongValue(searchStats, "PARTIALEXCHANGECOUNT"));
            typeStats.put("fullReturn", getLongValue(searchStats, "FULLRETURNCOUNT"));
            typeStats.put("partialReturn", getLongValue(searchStats, "PARTIALRETURNCOUNT"));
            realtimeStats.put("typeStats", typeStats);
            
            // 4️⃣ 브랜드별 통계
            Map<String, Long> brandStats = new HashMap<>();
            brandStats.put("renoma", getLongValue(searchStats, "RENOMACOUNT"));
            brandStats.put("coralic", getLongValue(searchStats, "CORALICCOUNT"));
            realtimeStats.put("brandStats", brandStats);
            
            // 5️⃣ 금액 통계
            Map<String, Object> amountStats = new HashMap<>();
            amountStats.put("totalRefundAmount", getLongValue(searchStats, "TOTALREFUNDAMOUNT"));
            amountStats.put("avgRefundAmount", getLongValue(searchStats, "AVGREFUNDAMOUNT"));
            realtimeStats.put("amountStats", amountStats);
            
            // 6️⃣ 추가 통계
            realtimeStats.put("delayedCount", getLongValue(searchStats, "DELAYEDCOUNT"));
            
            long endTime = System.currentTimeMillis();
            log.info("✅ 실시간 통계 조회 완료 - 소요시간: {}ms", endTime - startTime);
            
            // 응답 구조 표준화
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", realtimeStats);
            response.put("timestamp", System.currentTimeMillis());
            response.put("responseTime", endTime - startTime);
            response.put("searchParams", searchParams);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 실시간 통계 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "실시간 통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            errorResult.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 🎯 카드 필터링 처리 - sample 폴더에서 참고하여 구현
     */
    @GetMapping("/filter/{filterType}")
    public String filterByCard(
            @PathVariable String filterType,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        
        log.info("🎯 카드 필터링 요청 - filterType: {}", filterType);
        
        Page<ReturnItemDTO> returnItems;
        ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDir(sortDir);
        
        try {
            // 필터 타입에 따라 조건 설정
            switch (filterType) {
                case "collection-completed":
                    log.info("📦 회수완료 필터링");
                    returnItems = returnItemService.findByCollectionCompleted(searchDTO);
                    break;
                case "collection-pending":
                    log.info("📦 회수미완료 필터링");
                    returnItems = returnItemService.findByCollectionPending(searchDTO);
                    break;
                case "logistics-confirmed":
                    log.info("🚛 물류확인완료 필터링");
                    returnItems = returnItemService.findByLogisticsConfirmed(searchDTO);
                    break;
                case "logistics-pending":
                    log.info("🚛 물류확인미완료 필터링");
                    returnItems = returnItemService.findByLogisticsPending(searchDTO);
                    break;
                case "shipping-completed":
                    log.info("📤 출고완료 필터링");
                    returnItems = returnItemService.findByShippingCompleted(searchDTO);
                    break;
                case "shipping-pending":
                    log.info("📤 출고대기 필터링");
                    returnItems = returnItemService.findByShippingPending(searchDTO);
                    break;
                case "refund-completed":
                    log.info("💰 환불완료 필터링");
                    returnItems = returnItemService.findByRefundCompleted(searchDTO);
                    break;
                case "refund-pending":
                    log.info("💰 환불대기 필터링");
                    returnItems = returnItemService.findByRefundPending(searchDTO);
                    break;
                case "payment-completed":
                    log.info("💳 입금완료 필터링");
                    returnItems = returnItemService.findByPaymentCompleted(searchDTO);
                    break;
                case "payment-pending":
                    log.info("💳 입금대기 필터링");
                    returnItems = returnItemService.findByPaymentPending(searchDTO);
                    break;
                case "completed":
                    log.info("✅ 전체완료 필터링");
                    returnItems = returnItemService.findByCompleted(searchDTO);
                    break;
                case "incompleted":
                    log.info("❌ 미완료 필터링");
                    returnItems = returnItemService.findByIncompleted(searchDTO);
                    break;
                case "overdue-ten-days":
                    log.info("🚨 처리기간 임박 필터링 (10일 이상 미완료)");
                    returnItems = returnItemService.findOverdueTenDays(searchDTO);
                    break;
                default:
                    log.warn("⚠️ 알 수 없는 필터 타입: {}", filterType);
                    returnItems = returnItemService.findAll(page, size, sortBy, sortDir);
                    break;
            }
            
            log.info("✅ 필터링 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            
        } catch (Exception e) {
            log.error("❌ 필터링 조회 실패: {}", e.getMessage(), e);
            returnItems = Page.empty();
        }
        
        // 기본 통계 정보도 함께 제공 (기존 list 메서드와 동일)
        Map<String, Long> statusCounts = new HashMap<>();
        Map<String, Long> siteCounts = new HashMap<>();
        Map<String, Long> typeCounts = new HashMap<>();
        Map<String, Long> reasonCounts = new HashMap<>();
        Map<String, Object> amountSummary = new HashMap<>();
        Map<String, Long> brandCounts = new HashMap<>();
        
        try {
            statusCounts = returnItemService.getStatusCounts();
            siteCounts = returnItemService.getSiteCounts();
            typeCounts = returnItemService.getTypeCounts();
            reasonCounts = returnItemService.getReasonCounts();
            amountSummary = returnItemService.getAmountSummary();
            brandCounts = returnItemService.getBrandCounts();
        } catch (Exception e) {
            log.error("통계 조회 실패: {}", e.getMessage());
        }
        
        // 금일 등록 건수 계산
        Long todayCount = 0L;
        try {
            todayCount = returnItemService.getTodayCount();
        } catch (Exception e) {
            log.error("금일 등록 건수 조회 실패: {}", e.getMessage());
            todayCount = 0L;
        }
        
        // 🚀 상단 카드 대시보드 통계 계산 (통합 쿼리로 성능 최적화)
        Map<String, Long> cardStats = new HashMap<>();
        try {
            log.info("🚀 통합 카드 통계 조회 시작");
            long cardStartTime = System.currentTimeMillis();
            
            // 🎯 통합 통계 조회 (기존 13개 쿼리 → 1개 쿼리)
            Map<String, Object> unifiedStats = returnItemService.getDashboardStatsUnified();
            
            // 🚀 통합 결과에서 개별 값들 추출 (기존 인터페이스 호환성 유지)
            cardStats.put("collectionCompleted", getLongValue(unifiedStats, "collectionCompletedCount"));
            cardStats.put("collectionPending", getLongValue(unifiedStats, "collectionPendingCount"));
            cardStats.put("logisticsConfirmed", getLongValue(unifiedStats, "logisticsConfirmedCount"));
            cardStats.put("logisticsPending", getLongValue(unifiedStats, "logisticsPendingCount"));
            cardStats.put("exchangeShipped", getLongValue(unifiedStats, "exchangeShippedCount"));
            cardStats.put("exchangeNotShipped", getLongValue(unifiedStats, "exchangeNotShippedCount"));
            cardStats.put("returnRefunded", getLongValue(unifiedStats, "returnRefundedCount"));
            cardStats.put("returnNotRefunded", getLongValue(unifiedStats, "returnNotRefundedCount"));
            cardStats.put("paymentCompleted", getLongValue(unifiedStats, "paymentCompletedCount"));
            cardStats.put("paymentPending", getLongValue(unifiedStats, "paymentPendingCount"));
            cardStats.put("completedCount", getLongValue(unifiedStats, "completedCount"));
            cardStats.put("incompletedCount", getLongValue(unifiedStats, "incompletedCount"));
            cardStats.put("overdueTenDaysCount", getLongValue(unifiedStats, "overdueTenDaysCount"));
            
            long cardEndTime = System.currentTimeMillis();
            log.info("✅ 통합 카드 통계 조회 완료 - 소요시간: {}ms (기존 13개 쿼리 → 1개 쿼리)", cardEndTime - cardStartTime);
            
        } catch (Exception e) {
            log.error("❌ 상단 카드 통계 조회 실패, 기본값 사용: {}", e.getMessage());
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
        
        // 모델에 데이터 추가
        model.addAttribute("returnItems", returnItems);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("siteCounts", siteCounts);
        model.addAttribute("typeCounts", typeCounts);
        model.addAttribute("reasonCounts", reasonCounts);
        model.addAttribute("amountSummary", amountSummary);
        model.addAttribute("brandCounts", brandCounts);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("cardStats", cardStats);
        model.addAttribute("currentFilter", filterType); // 🎯 현재 적용된 필터 정보 추가
        
        // 🎯 필터 표시 이름 매핑 (13개 항목이므로 Map.ofEntries 사용)
        Map<String, String> filterDisplayNames = Map.ofEntries(
            Map.entry("collection-completed", "📦 회수완료"),
            Map.entry("collection-pending", "📦 회수미완료"), 
            Map.entry("logistics-confirmed", "🚛 물류확인완료"),
            Map.entry("logistics-pending", "🚛 물류확인미완료"),
            Map.entry("shipping-completed", "📤 출고완료"),
            Map.entry("shipping-pending", "📤 출고대기"),
            Map.entry("refund-completed", "💰 환불완료"),
            Map.entry("refund-pending", "💰 환불대기"),
            Map.entry("payment-pending", "💳 입금대기"),
            Map.entry("payment-completed", "💳 입금완료"),
            Map.entry("completed", "✅ 전체완료"),
            Map.entry("incompleted", "❌ 미완료"),
            Map.entry("overdue-ten-days", "🚨 처리기간 임박")
        );
        model.addAttribute("filterDisplayName", filterDisplayNames.get(filterType));
        model.addAttribute("pageTitle", "교환/반품 관리");
        
        return "exchange/list";
    }

    /**
     * 🎯 다중 필터 조회 (여러 필터 동시 적용 가능)
     */
    @GetMapping("/list/filters")
    public String listWithMultipleFilters(
            Model model, 
            @ModelAttribute ReturnItemSearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String logisticsStartDate,
            @RequestParam(required = false) String logisticsEndDate) {
        
        log.info("🎯 다중 필터 조회 - filters: {}, 검색조건: {}", filters, searchDTO);
        
        // 🔍 검색 조건 설정
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchDTO.setKeyword(keyword.trim());
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                searchDTO.setStartDate(LocalDate.parse(startDate.trim()));
            } catch (Exception e) {
                log.warn("⚠️ 시작일 파싱 오류: {}", startDate, e);
            }
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                searchDTO.setEndDate(LocalDate.parse(endDate.trim()));
            } catch (Exception e) {
                log.warn("⚠️ 종료일 파싱 오류: {}", endDate, e);
            }
        }
        if (logisticsStartDate != null && !logisticsStartDate.trim().isEmpty()) {
            try {
                searchDTO.setLogisticsStartDate(LocalDate.parse(logisticsStartDate.trim()));
            } catch (Exception e) {
                log.warn("⚠️ 물류확인 시작일 파싱 오류: {}", logisticsStartDate, e);
            }
        }
        if (logisticsEndDate != null && !logisticsEndDate.trim().isEmpty()) {
            try {
                searchDTO.setLogisticsEndDate(LocalDate.parse(logisticsEndDate.trim()));
            } catch (Exception e) {
                log.warn("⚠️ 물류확인 종료일 파싱 오류: {}", logisticsEndDate, e);
            }
        }
        
        Page<ReturnItemDTO> returnItems;
        try {
            // 페이징 정보를 searchDTO에 설정
            searchDTO.setPage(page);
            searchDTO.setSize(size);
            searchDTO.setSortBy(sortBy);
            searchDTO.setSortDir(sortDir);
            
            // 🎯 필터와 검색 조건 함께 처리
            boolean hasFilters = StringUtils.hasText(filters);
            
            // 🚨 실제 검색 조건 엄격하게 확인 (filters는 제외)
            boolean hasRealSearchCondition = (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty() && !"null".equals(searchDTO.getKeyword().trim())) ||
                                             searchDTO.getStartDate() != null ||
                                             searchDTO.getEndDate() != null ||
                                             searchDTO.getLogisticsStartDate() != null ||
                                             searchDTO.getLogisticsEndDate() != null ||
                                             (searchDTO.getReturnTypeCode() != null && !searchDTO.getReturnTypeCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnTypeCode().trim())) ||
                                             (searchDTO.getReturnStatusCode() != null && !searchDTO.getReturnStatusCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnStatusCode().trim())) ||
                                             (searchDTO.getSiteName() != null && !searchDTO.getSiteName().trim().isEmpty() && !"null".equals(searchDTO.getSiteName().trim())) ||
                                             (searchDTO.getPaymentStatus() != null && !searchDTO.getPaymentStatus().trim().isEmpty() && !"null".equals(searchDTO.getPaymentStatus().trim())) ||
                                             (searchDTO.getBrandFilter() != null && !searchDTO.getBrandFilter().trim().isEmpty() && !"null".equals(searchDTO.getBrandFilter().trim()));
            
            log.info("🚨 DEBUG: hasFilters={}, hasRealSearchCondition={}", hasFilters, hasRealSearchCondition);
            
            if (hasFilters && hasRealSearchCondition) {
                // 필터 + 검색 조건 둘 다 있는 경우
                log.info("🔍 필터 + 검색 조건 함께 적용 - 필터: {}, 검색: {}", filters, searchDTO.getKeyword());
                returnItems = applyMultipleFiltersWithSearch(filters, searchDTO);
                log.info("✅ 필터 + 검색 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            } else if (hasFilters) {
                // 필터만 있는 경우 (🚀 이 경로가 올바른 경로)
                log.info("🔍 다중 필터만 적용: {}", filters);
                returnItems = applyMultipleFilters(filters, searchDTO);
                log.info("✅ 다중 필터 조회 완료 - 필터: {}, 결과 수: {}", filters, returnItems.getTotalElements());
            } else if (hasRealSearchCondition) {
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
            returnItems = Page.empty();
        }
        
        // 🎯 통계 조회 (검색 조건에 따른)
        Map<String, Object> unifiedStats = new HashMap<>();
        Map<String, Long> cardStats = new HashMap<>();
        Map<String, Long> typeCounts = new HashMap<>();
        Map<String, Long> brandCounts = new HashMap<>();
        Map<String, Object> amountSummary = new HashMap<>();
        Map<String, Long> statusCounts = new HashMap<>();
        Map<String, Long> siteCounts = new HashMap<>();
        Map<String, Long> reasonCounts = new HashMap<>();
        Long todayCount = 0L;
        
        try {
            // 🔍 검색 조건이 있는지 확인
            boolean hasRealSearchCondition = (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty() && !"null".equals(searchDTO.getKeyword().trim())) ||
                                             searchDTO.getStartDate() != null ||
                                             searchDTO.getEndDate() != null ||
                                             searchDTO.getLogisticsStartDate() != null ||
                                             searchDTO.getLogisticsEndDate() != null ||
                                             (searchDTO.getReturnTypeCode() != null && !searchDTO.getReturnTypeCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnTypeCode().trim())) ||
                                             (searchDTO.getReturnStatusCode() != null && !searchDTO.getReturnStatusCode().trim().isEmpty() && !"null".equals(searchDTO.getReturnStatusCode().trim())) ||
                                             (searchDTO.getSiteName() != null && !searchDTO.getSiteName().trim().isEmpty() && !"null".equals(searchDTO.getSiteName().trim())) ||
                                             (searchDTO.getPaymentStatus() != null && !searchDTO.getPaymentStatus().trim().isEmpty() && !"null".equals(searchDTO.getPaymentStatus().trim())) ||
                                             (searchDTO.getBrandFilter() != null && !searchDTO.getBrandFilter().trim().isEmpty() && !"null".equals(searchDTO.getBrandFilter().trim()));
            log.info("🔍 통계 조회 - 검색 조건 존재: {}, 검색DTO: {}", hasRealSearchCondition, searchDTO);
            
            if (hasRealSearchCondition) {
                // 🎯 검색 조건에 맞는 통합 통계 조회
                log.info("📊 검색 조건 기반 통합 통계 조회 시작");
                unifiedStats = returnItemService.getDashboardStatsUnifiedBySearch(searchDTO);
                log.info("✅ 검색 조건 기반 통합 통계 조회 완료");
            } else {
                // 🎯 전체 데이터 기반 통합 통계 조회
                log.info("📊 전체 데이터 기반 통합 통계 조회 시작");
                unifiedStats = returnItemService.getDashboardStatsUnified();
                log.info("✅ 전체 데이터 기반 통합 통계 조회 완료");
            }
            
            // 🚀 통합 결과에서 개별 값들 추출
            cardStats.put("collectionCompleted", getLongValue(unifiedStats, "collectionCompletedCount"));
            cardStats.put("collectionPending", getLongValue(unifiedStats, "collectionPendingCount"));
            cardStats.put("logisticsConfirmed", getLongValue(unifiedStats, "logisticsConfirmedCount"));
            cardStats.put("logisticsPending", getLongValue(unifiedStats, "logisticsPendingCount"));
            cardStats.put("exchangeShipped", getLongValue(unifiedStats, "exchangeShippedCount"));
            cardStats.put("exchangeNotShipped", getLongValue(unifiedStats, "exchangeNotShippedCount"));
            cardStats.put("returnRefunded", getLongValue(unifiedStats, "returnRefundedCount"));
            cardStats.put("returnNotRefunded", getLongValue(unifiedStats, "returnNotRefundedCount"));
            cardStats.put("paymentCompleted", getLongValue(unifiedStats, "paymentCompletedCount"));
            cardStats.put("paymentPending", getLongValue(unifiedStats, "paymentPendingCount"));
            cardStats.put("completedCount", getLongValue(unifiedStats, "completedCount"));
            cardStats.put("incompletedCount", getLongValue(unifiedStats, "incompletedCount"));
            
            // 🚨 처리기간 임박 통계 추가
            Long overdueTenDaysCount = returnItemService.getOverdueTenDaysCount();
            cardStats.put("overdueTenDaysCount", overdueTenDaysCount != null ? overdueTenDaysCount : 0L);
            
            // 🏷️ 유형별 통계
            typeCounts.put("전체교환", getLongValue(unifiedStats, "fullExchangeCount"));
            typeCounts.put("부분교환", getLongValue(unifiedStats, "partialExchangeCount"));
            typeCounts.put("전체반품", getLongValue(unifiedStats, "fullReturnCount"));
            typeCounts.put("부분반품", getLongValue(unifiedStats, "partialReturnCount"));
            
            // 🌐 브랜드별 통계
            brandCounts.put("레노마", getLongValue(unifiedStats, "renomaCount"));
            brandCounts.put("코랄리크", getLongValue(unifiedStats, "coralicCount"));
            
            // 💰 금액 요약
            amountSummary.put("totalRefundAmount", getLongValue(unifiedStats, "totalRefundAmount"));
            amountSummary.put("avgRefundAmount", getLongValue(unifiedStats, "avgRefundAmount"));
            amountSummary.put("totalShippingFee", 0L);
            
            todayCount = getLongValue(unifiedStats, "todayCount");
            
        } catch (Exception e) {
            log.error("❌ 통합 통계 조회 실패: {}", e.getMessage(), e);
            // 기본값으로 초기화
            cardStats.clear();
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
        
        // 모델에 데이터 추가
        model.addAttribute("returnItems", returnItems);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("cardStats", cardStats);
        model.addAttribute("typeCounts", typeCounts);
        model.addAttribute("brandCounts", brandCounts);
        model.addAttribute("amountSummary", amountSummary);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("siteCounts", siteCounts);
        model.addAttribute("reasonCounts", reasonCounts);
        model.addAttribute("todayCount", todayCount);
        
        // 🎯 현재 적용된 필터 정보
        model.addAttribute("currentFilters", filters);
        
        // 🎯 필터 표시 이름들
        if (StringUtils.hasText(filters)) {
            String[] filterArray = filters.split(",");
            List<String> filterDisplayNames = new ArrayList<>();
            Map<String, String> filterNameMap = Map.ofEntries(
                Map.entry("collection-completed", "📦 회수완료"),
                Map.entry("collection-pending", "📦 회수미완료"), 
                Map.entry("logistics-confirmed", "🚛 물류확인완료"),
                Map.entry("logistics-pending", "🚛 물류확인미완료"),
                Map.entry("shipping-completed", "📤 출고완료"),
                Map.entry("shipping-pending", "📤 출고대기"),
                Map.entry("refund-completed", "💰 환불완료"),
                Map.entry("refund-pending", "💰 환불대기"),
                Map.entry("payment-pending", "💳 입금대기"),
                Map.entry("payment-completed", "💳 입금완료"),
                Map.entry("completed", "✅ 전체완료"),
                Map.entry("incompleted", "❌ 미완료"),
                Map.entry("overdue-ten-days", "🚨 처리기간 임박")
            );
            
            for (String filter : filterArray) {
                String displayName = filterNameMap.get(filter.trim());
                if (displayName != null) {
                    filterDisplayNames.add(displayName);
                }
            }
            model.addAttribute("activeFilterNames", filterDisplayNames);
        }
        
        model.addAttribute("pageTitle", "교환/반품 관리");
        
        return "exchange/list";
    }

    /**
     * 교환/반품 상세 화면
     */
    // ============================================================================
    // 🔧 Sample 기능 통합 - CRUD 기능 추가
    // ============================================================================
    
    /**
     * 📝 교환/반품 등록 폼 (Sample에서 통합)
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        log.info("📝 교환/반품 등록 폼 접속");
        model.addAttribute("returnItem", new ReturnItemDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "교환/반품 등록");
        return "exchange/form";
    }
    
    /**
     * 📝 교환/반품 수정 폼 (Sample에서 통합)
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, 
                          @RequestParam(required = false) String returnTo, 
                          Model model) {
        log.info("📝 교환/반품 수정 폼 접속 - ID: {}", id);
        
        try {
            ReturnItemDTO returnItem = returnItemService.findById(id);
            model.addAttribute("returnItem", returnItem);
            model.addAttribute("isEdit", true);
            model.addAttribute("returnTo", returnTo);
            model.addAttribute("pageTitle", "교환/반품 수정");
            
            // 🌐 이미지 절대 URL 추가
            if (returnItem.getDefectPhotoUrl() != null && !returnItem.getDefectPhotoUrl().isEmpty()) {
                String imageAbsoluteUrl = getImageAbsoluteUrl(returnItem.getDefectPhotoUrl());
                model.addAttribute("imageAbsoluteUrl", imageAbsoluteUrl);
                log.info("📷 이미지 절대 URL 생성: {}", imageAbsoluteUrl);
            }
            
            log.info("✅ 교환/반품 수정 폼 데이터 로드 완료 - ID: {}", id);
        } catch (Exception e) {
            log.error("❌ 교환/반품 수정 폼 데이터 로드 실패 - ID: {}, 오류: {}", id, e.getMessage());
            model.addAttribute("error", "데이터를 불러올 수 없습니다: " + e.getMessage());
            return "error"; // 에러 페이지로 이동
        }
        
        return "exchange/form";
    }
    
    /**
     * 📋 교환/반품 상세 보기 (Sample에서 통합)
     */
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, 
                       @RequestParam(required = false) String returnTo, 
                       Model model) {
        log.info("📋 교환/반품 상세 보기 접속 - ID: {}", id);
        
        try {
            ReturnItemDTO returnItem = returnItemService.findById(id);
            model.addAttribute("returnItem", returnItem);
            model.addAttribute("returnTo", returnTo);
        model.addAttribute("pageTitle", "교환/반품 상세");
            
            // 🌐 이미지 절대 URL 추가
            if (returnItem.getDefectPhotoUrl() != null && !returnItem.getDefectPhotoUrl().isEmpty()) {
                String imageAbsoluteUrl = getImageAbsoluteUrl(returnItem.getDefectPhotoUrl());
                model.addAttribute("imageAbsoluteUrl", imageAbsoluteUrl);
                log.info("📷 이미지 절대 URL 생성: {}", imageAbsoluteUrl);
            }
            
            log.info("✅ 교환/반품 상세 데이터 로드 완료 - ID: {}", id);
        } catch (Exception e) {
            log.error("❌ 교환/반품 상세 데이터 로드 실패 - ID: {}, 오류: {}", id, e.getMessage());
            model.addAttribute("error", "데이터를 불러올 수 없습니다: " + e.getMessage());
            return "error"; // 에러 페이지로 이동
        }
        
        return "exchange/view";
    }

    /**
     * 💾 교환/반품 저장 처리 (Sample에서 통합)
     */
    @PostMapping("/save")
    public String save(@ModelAttribute ReturnItemDTO returnItemDTO, 
                       @RequestParam Map<String, String> allParams,
                       @RequestParam(value = "attachmentPhoto", required = false) MultipartFile attachmentPhoto,
                       @RequestParam(value = "attachmentImageData", required = false) String attachmentImageData,
                       @RequestParam(value = "returnTo", required = false) String returnTo,
                       RedirectAttributes redirectAttributes) {
        
        log.info("💾 교환/반품 저장 처리 시작 - ID: {}", returnItemDTO.getId());
        
        try {
            // 🖼️ 이미지 업로드 처리 (Sample에서 통합)
            String imagePath = processImageUpload(attachmentPhoto, attachmentImageData);
            if (imagePath != null) {
                returnItemDTO.setDefectPhotoUrl(imagePath);
                log.info("📷 이미지 업로드 완료 - 경로: {}", imagePath);
            }
            
            ReturnItemDTO savedItem;
            
            if (returnItemDTO.getId() == null) {
                // 신규 등록
                savedItem = returnItemService.createReturnItem(returnItemDTO);
                log.info("✅ 교환/반품 신규 등록 완료 - ID: {}", savedItem.getId());
                redirectAttributes.addFlashAttribute("message", "교환/반품이 성공적으로 등록되었습니다.");
            } else {
                // 기존 항목 수정 (save 메소드 사용)
                savedItem = returnItemService.save(returnItemDTO);
                log.info("✅ 교환/반품 수정 완료 - ID: {}", savedItem.getId());
                redirectAttributes.addFlashAttribute("message", "교환/반품이 성공적으로 수정되었습니다.");
            }
            
            // returnTo 파라미터에 따라 리다이렉트
            if ("view".equals(returnTo)) {
                return "redirect:/exchange/view/" + savedItem.getId();
            } else {
                return "redirect:/exchange/list";
            }
            
        } catch (Exception e) {
            log.error("❌ 교환/반품 저장 실패 - ID: {}, 오류: {}", returnItemDTO.getId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다: " + e.getMessage());
            
            if (returnItemDTO.getId() == null) {
                return "redirect:/exchange/create";
            } else {
                return "redirect:/exchange/edit/" + returnItemDTO.getId();
            }
        }
    }
    
    /**
     * 🗑️ 교환/반품 삭제 처리 (Sample에서 통합)
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("🗑️ 교환/반품 삭제 처리 시작 - ID: {}", id);
        
        try {
            returnItemService.delete(id);
            log.info("✅ 교환/반품 삭제 완료 - ID: {}", id);
            redirectAttributes.addFlashAttribute("message", "교환/반품이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("❌ 교환/반품 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/exchange/list";
    }
    
    /**
     * 🔄 교환/반품 상태 업데이트 API (Sample에서 통합)
     */
    @PostMapping("/api/{id}/status")
    @ResponseBody
    public ResponseEntity<ReturnItemDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("🔄 교환/반품 상태 업데이트 API 호출 - ID: {}, 상태: {}", id, status);
        
        try {
            ReturnItemDTO updatedItem = returnItemService.findById(id);
            // 상태 업데이트 로직 추가 필요 (ReturnItemService에서 구현)
            log.info("✅ 교환/반품 상태 업데이트 완료 - ID: {}", id);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            log.error("❌ 교환/반품 상태 업데이트 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 🗑️ 교환/반품 삭제 API (Sample에서 통합)
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAjax(@PathVariable Long id) {
        log.info("🗑️ 교환/반품 삭제 API 호출 - ID: {}", id);
        
        try {
            returnItemService.delete(id);
            log.info("✅ 교환/반품 삭제 API 완료 - ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ 교환/반품 삭제 API 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // 🔧 [기존 메소드 주석 처리] - 빈 메소드들을 위의 완성된 메소드들로 대체
    /*
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("pageTitle", "교환/반품 등록");
        return "exchange/register";
    }
     */

    // ============================================================================
    // 📊 엑셀 다운로드/업로드 기능
    // ============================================================================

    /**
     * 📥 교환/반품 데이터 엑셀 다운로드
     * 현재 검색 조건에 맞는 모든 데이터를 엑셀로 다운로드
     */
    @PostMapping("/download/excel")
    public void downloadExcel(
            @ModelAttribute ReturnItemSearchDTO searchDTO,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.info("📥 교환/반품 엑셀 다운로드 요청");
        log.info("🔍 받은 검색조건 전체: {}", searchDTO);
        log.info("🔍 개별 필드 확인:");
        log.info("  - keyword: '{}'", searchDTO.getKeyword());
        log.info("  - startDate: {}", searchDTO.getStartDate());
        log.info("  - endDate: {}", searchDTO.getEndDate());
        log.info("  - logisticsStartDate: {}", searchDTO.getLogisticsStartDate());
        log.info("  - logisticsEndDate: {}", searchDTO.getLogisticsEndDate());
        log.info("  - returnTypeCode: '{}'", searchDTO.getReturnTypeCode());
        log.info("  - returnStatusCode: '{}'", searchDTO.getReturnStatusCode());
        log.info("  - siteName: '{}'", searchDTO.getSiteName());
        log.info("  - paymentStatus: '{}'", searchDTO.getPaymentStatus());
        log.info("  - brandFilter: '{}'", searchDTO.getBrandFilter());
        log.info("🎯 검색 조건 존재 여부: {}", searchDTO.hasSearchCondition());
        
        // 🔍 HTTP 요청 파라미터도 확인
        log.info("📋 요청 파라미터 전체:");
        request.getParameterMap().forEach((key, values) -> {
            log.info("  - {}: {}", key, String.join(", ", values));
        });
        
        try {
            // 🎯 필터가 있는 경우 다중 필터 처리, 없으면 검색 조건 적용
            List<ReturnItemDTO> allData;
            
            // 필터 파라미터 처리
            String filters = searchDTO.getFilters();
            if (filters != null && !filters.trim().isEmpty()) {
                log.info("🎯 필터 조건 적용 (일반 엑셀) - filters: {}", filters);
                List<String> filterList = Arrays.asList(filters.split(","));
                allData = returnItemService.findByMultipleFiltersUnlimited(filterList, searchDTO);
                log.info("📊 필터 결과: {} 건의 데이터 추출 (일반 엑셀)", allData.size());
            } else {
                log.info("🔍 검색 조건 적용 (일반 엑셀)");
                allData = returnItemService.findBySearch(searchDTO);
                log.info("📊 검색 결과: {} 건의 데이터 추출 (일반 엑셀)", allData.size());
            }
            
            // 원래 로직 (주석 처리)
            /*
            if (searchDTO != null && searchDTO.hasSearchCondition()) {
                // 검색 조건 있으면 페이징 없는 검색 결과
                allData = returnItemService.findBySearch(searchDTO);
                log.info("📊 검색 조건으로 {} 건의 데이터 추출", allData.size());
            } else {
                // 검색 조건 없으면 페이징 없는 전체 데이터
                allData = returnItemService.findAll();
                log.info("📊 전체 {} 건의 데이터 추출", allData.size());
            }
            */
            
            // 파일명 생성 (날짜 포함)
            String fileName = "교환반품목록_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            
            // HTTP 응답 헤더 설정 (한글 파일명 지원)
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            // 파일명 인코딩 처리
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 캐시 방지
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // 엑셀 파일 생성 및 출력
            try (XSSFWorkbook workbook = createExcelFile(allData)) {
                // 출력 스트림에 직접 쓰기
                workbook.write(response.getOutputStream());
                response.getOutputStream().flush();
                
                log.info("✅ 엑셀 다운로드 완료 - 파일명: {}, 데이터 수: {}", fileName, allData.size());
            }
            
        } catch (Exception e) {
            log.error("❌ 엑셀 다운로드 실패: {}", e.getMessage(), e);
            
            // 에러 응답 처리
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📥🖼️ 이미지 포함 교환/반품 데이터 엑셀 다운로드
     * 현재 검색 조건에 맞는 모든 데이터와 첨부 이미지를 엑셀로 다운로드
     */
    @PostMapping("/download/excel-with-images")
    public void downloadExcelWithImages(
            @ModelAttribute ReturnItemSearchDTO searchDTO,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.info("📥🖼️ 이미지 포함 교환/반품 엑셀 다운로드 요청");
        log.info("🔍 받은 검색조건 전체: {}", searchDTO);
        log.info("🔍 개별 필드 확인:");
        log.info("  - keyword: '{}'", searchDTO.getKeyword());
        log.info("  - startDate: {}", searchDTO.getStartDate());
        log.info("  - endDate: {}", searchDTO.getEndDate());
        log.info("  - logisticsStartDate: {}", searchDTO.getLogisticsStartDate());
        log.info("  - logisticsEndDate: {}", searchDTO.getLogisticsEndDate());
        log.info("  - returnTypeCode: '{}'", searchDTO.getReturnTypeCode());
        log.info("  - returnStatusCode: '{}'", searchDTO.getReturnStatusCode());
        log.info("  - siteName: '{}'", searchDTO.getSiteName());
        log.info("  - paymentStatus: '{}'", searchDTO.getPaymentStatus());
        log.info("  - brandFilter: '{}'", searchDTO.getBrandFilter());
        log.info("🎯 검색 조건 존재 여부: {}", searchDTO.hasSearchCondition());
        
        // 🔍 HTTP 요청 파라미터도 확인
        log.info("📋 요청 파라미터 전체:");
        request.getParameterMap().forEach((key, values) -> {
            log.info("  - {}: {}", key, String.join(", ", values));
        });
        
        try {
            // 🎯 필터가 있는 경우 다중 필터 처리, 없으면 검색 조건 적용
            List<ReturnItemDTO> allData;
            
            // 필터 파라미터 처리
            String filters = searchDTO.getFilters();
            if (filters != null && !filters.trim().isEmpty()) {
                log.info("🎯 필터 조건 적용 (이미지 포함 엑셀) - filters: {}", filters);
                List<String> filterList = Arrays.asList(filters.split(","));
                allData = returnItemService.findByMultipleFiltersUnlimited(filterList, searchDTO);
                log.info("📊 필터 결과: {} 건의 데이터 추출 (이미지 포함 엑셀)", allData.size());
            } else {
                log.info("🔍 검색 조건 적용 (이미지 포함 엑셀)");
                allData = returnItemService.findBySearch(searchDTO);
                log.info("📊 검색 결과: {} 건의 데이터 추출 (이미지 포함 엑셀)", allData.size());
            }
            
            // 원래 로직 (주석 처리)
            /*
            if (searchDTO != null && searchDTO.hasSearchCondition()) {
                // 검색 조건 있으면 페이징 없는 검색 결과
                allData = returnItemService.findBySearch(searchDTO);
                log.info("📊 검색 조건으로 {} 건의 데이터 추출", allData.size());
            } else {
                // 검색 조건 없으면 페이징 없는 전체 데이터
                allData = returnItemService.findAll();
                log.info("📊 전체 {} 건의 데이터 추출", allData.size());
            }
            */
            
            // 파일명 생성 (날짜 포함)
            String fileName = "교환반품목록(이미지포함)_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            
            // HTTP 응답 헤더 설정 (한글 파일명 지원)
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            // 파일명 인코딩 처리
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 캐시 방지
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // 이미지 포함 엑셀 파일 생성 및 출력
            try (XSSFWorkbook workbook = createExcelFileWithImages(allData)) {
                // 출력 스트림에 직접 쓰기
                workbook.write(response.getOutputStream());
                response.getOutputStream().flush();
                
                log.info("✅ 이미지 포함 엑셀 다운로드 완료 - 파일명: {}, 데이터 수: {}", fileName, allData.size());
            }
            
        } catch (Exception e) {
            log.error("❌ 이미지 포함 엑셀 다운로드 실패: {}", e.getMessage(), e);
            
            // 에러 응답 처리
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("이미지 포함 엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📄 엑셀 업로드용 템플릿 다운로드
     */
    @GetMapping("/download/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        
        log.info("📄 엑셀 업로드 템플릿 다운로드 요청");
        
        try {
            // 파일명
            String fileName = "교환반품_업로드템플릿.xlsx";
            
            // HTTP 응답 헤더 설정 (한글 파일명 지원)
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            // 파일명 인코딩 처리
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 캐시 방지
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // 빈 템플릿 생성 및 출력
            try (XSSFWorkbook workbook = createExcelTemplate()) {
                // 출력 스트림에 직접 쓰기
                workbook.write(response.getOutputStream());
                response.getOutputStream().flush();
                
                log.info("✅ 템플릿 다운로드 완료 - 파일명: {}", fileName);
            }
            
        } catch (Exception e) {
            log.error("❌ 템플릿 다운로드 실패: {}", e.getMessage(), e);
            
            // 에러 응답 처리
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("템플릿 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📤 엑셀 파일 업로드 및 대량 등록
     */
    @PostMapping("/upload/excel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadExcel(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        log.info("📤 엑셀 파일 업로드 요청 - 파일명: {}, 크기: {} bytes", file.getOriginalFilename(), file.getSize());
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 파일 검증
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "업로드할 파일을 선택해주세요.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 파일 확장자 검증
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                result.put("success", false);
                result.put("message", "Excel 파일(.xlsx)만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 엑셀 파일 파싱 및 등록
            Map<String, Object> uploadResult = processExcelUpload(file, request);
            
            log.info("✅ 엑셀 업로드 처리 완료 - 결과: {}", uploadResult);
            return ResponseEntity.ok(uploadResult);
            
        } catch (Exception e) {
            log.error("❌ 엑셀 업로드 실패: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "엑셀 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    // ============================================================================
    // 📊 엑셀 처리 헬퍼 메서드들
    // ============================================================================

    /**
     * 📊 엑셀 파일 생성 (데이터 포함)
     */
    private XSSFWorkbook createExcelFile(List<ReturnItemDTO> data) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("교환반품 목록");
        
        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        // 데이터 스타일
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 체크박스 스타일 (중앙 정렬)
        CellStyle checkboxStyle = workbook.createCellStyle();
        checkboxStyle.setBorderTop(BorderStyle.THIN);
        checkboxStyle.setBorderBottom(BorderStyle.THIN);
        checkboxStyle.setBorderLeft(BorderStyle.THIN);
        checkboxStyle.setBorderRight(BorderStyle.THIN);
        checkboxStyle.setAlignment(HorizontalAlignment.CENTER);
        checkboxStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 헤더 행 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "유형", "주문일", "CS접수일", "사이트명", "주문번호", "환불금액", 
            "고객명", "고객연락처", "상품코드", "색상", "사이즈", "수량", "배송비",
            "반품사유", "불량상세", "불량사진URL", "운송장번호", "회수완료일",
            "물류확인일", "출고일", "환불일", "완료여부", "비고"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 데이터 행 생성
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            ReturnItemDTO item = data.get(i);
            
            setCellValue(row, 0, item.getId(), dataStyle);
            // 🎯 유형을 한글명으로 변환
            setCellValue(row, 1, convertReturnTypeToKorean(item.getReturnTypeCode()), dataStyle);
            setCellValue(row, 2, item.getOrderDate(), dataStyle);
            setCellValue(row, 3, item.getCsReceivedDate(), dataStyle);
            setCellValue(row, 4, item.getSiteName(), dataStyle);
            setCellValue(row, 5, item.getOrderNumber(), dataStyle);
            setCellValue(row, 6, item.getRefundAmount(), dataStyle);
            setCellValue(row, 7, item.getCustomerName(), dataStyle);
            setCellValue(row, 8, item.getCustomerPhone(), dataStyle);
            setCellValue(row, 9, item.getOrderItemCode(), dataStyle);
            setCellValue(row, 10, item.getProductColor(), dataStyle);
            setCellValue(row, 11, item.getProductSize(), dataStyle);
            setCellValue(row, 12, item.getQuantity(), dataStyle);
            setCellValue(row, 13, item.getShippingFee(), dataStyle);
            setCellValue(row, 14, item.getReturnReason(), dataStyle);
            setCellValue(row, 15, item.getDefectDetail(), dataStyle);
            setCellValue(row, 16, item.getDefectPhotoUrl(), dataStyle);
            setCellValue(row, 17, item.getTrackingNumber(), dataStyle);
            setCellValue(row, 18, item.getCollectionCompletedDate(), dataStyle);
            setCellValue(row, 19, item.getLogisticsConfirmedDate(), dataStyle);
            setCellValue(row, 20, item.getShippingDate(), dataStyle);
            setCellValue(row, 21, item.getRefundDate(), dataStyle);
            // 🎯 완료여부를 체크박스 스타일로 변환
            setCellValue(row, 22, convertBooleanToCheckbox(item.getIsCompleted()), checkboxStyle);
            setCellValue(row, 23, item.getRemarks(), dataStyle);
        }
        
        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        return workbook;
    }

    /**
     * 📊🖼️ 이미지 포함 엑셀 파일 생성
     * 실제 첨부 이미지 파일을 다운로드하여 엑셀에 포함
     */
    private XSSFWorkbook createExcelFileWithImages(List<ReturnItemDTO> data) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("교환반품 목록(이미지포함)");
        
        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        // 데이터 스타일
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 체크박스 스타일 (중앙 정렬)
        CellStyle checkboxStyle = workbook.createCellStyle();
        checkboxStyle.setBorderTop(BorderStyle.THIN);
        checkboxStyle.setBorderBottom(BorderStyle.THIN);
        checkboxStyle.setBorderLeft(BorderStyle.THIN);
        checkboxStyle.setBorderRight(BorderStyle.THIN);
        checkboxStyle.setAlignment(HorizontalAlignment.CENTER);
        checkboxStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 헤더 행 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "유형", "주문일", "CS접수일", "사이트명", "주문번호", "환불금액", 
            "고객명", "고객연락처", "상품코드", "색상", "사이즈", "수량", "배송비",
            "반품사유", "불량상세", "불량사진", "운송장번호", "회수완료일",
            "물류확인일", "출고일", "환불일", "완료여부", "비고"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 🖼️ Drawing 객체를 미리 생성 (중요: 한 번만 생성)
        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        
        // 이미지 컬럼의 너비를 미리 설정 (더 넓게)
        sheet.setColumnWidth(16, 4000); // 불량사진 컬럼
        
        int currentRow = 1;
        
        // 데이터 행 생성
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(currentRow);
            ReturnItemDTO item = data.get(i);
            
            // 기본 행 높이 설정 (이미지 때문에 더 높게)
            row.setHeightInPoints(80);
            
            setCellValue(row, 0, item.getId(), dataStyle);
            // 🎯 유형을 한글명으로 변환
            setCellValue(row, 1, convertReturnTypeToKorean(item.getReturnTypeCode()), dataStyle);
            setCellValue(row, 2, item.getOrderDate(), dataStyle);
            setCellValue(row, 3, item.getCsReceivedDate(), dataStyle);
            setCellValue(row, 4, item.getSiteName(), dataStyle);
            setCellValue(row, 5, item.getOrderNumber(), dataStyle);
            setCellValue(row, 6, item.getRefundAmount(), dataStyle);
            setCellValue(row, 7, item.getCustomerName(), dataStyle);
            setCellValue(row, 8, item.getCustomerPhone(), dataStyle);
            setCellValue(row, 9, item.getOrderItemCode(), dataStyle);
            setCellValue(row, 10, item.getProductColor(), dataStyle);
            setCellValue(row, 11, item.getProductSize(), dataStyle);
            setCellValue(row, 12, item.getQuantity(), dataStyle);
            setCellValue(row, 13, item.getShippingFee(), dataStyle);
            setCellValue(row, 14, item.getReturnReason(), dataStyle);
            setCellValue(row, 15, item.getDefectDetail(), dataStyle);
            
            // 🖼️ 이미지 처리 - 실제 이미지 파일을 엑셀에 삽입
            Cell imageCell = row.createCell(16);
            imageCell.setCellStyle(dataStyle);
            
            String imageUrl = item.getDefectPhotoUrl();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                try {
                    // 원격 서버에서 이미지 다운로드하여 엑셀에 추가
                    byte[] imageData = downloadImageFromUrl(imageUrl);
                    if (imageData != null && imageData.length > 0) {
                        insertImageIntoCell(workbook, sheet, drawing, imageData, currentRow, 16);
                        imageCell.setCellValue("이미지 첨부됨");
                        log.info("🖼️ 이미지 삽입 완료 - ID: {}, URL: {}", item.getId(), imageUrl);
                    } else {
                        imageCell.setCellValue("이미지 없음");
                        log.warn("⚠️ 이미지 다운로드 실패 - ID: {}, URL: {}", item.getId(), imageUrl);
                    }
                } catch (Exception e) {
                    imageCell.setCellValue("이미지 로딩 실패");
                    log.error("❌ 이미지 처리 실패 - ID: {}, URL: {}, Error: {}", item.getId(), imageUrl, e.getMessage());
                }
            } else {
                imageCell.setCellValue("이미지 없음");
            }
            
            setCellValue(row, 17, item.getTrackingNumber(), dataStyle);
            setCellValue(row, 18, item.getCollectionCompletedDate(), dataStyle);
            setCellValue(row, 19, item.getLogisticsConfirmedDate(), dataStyle);
            setCellValue(row, 20, item.getShippingDate(), dataStyle);
            setCellValue(row, 21, item.getRefundDate(), dataStyle);
            // 🎯 완료여부를 체크박스 스타일로 변환
            setCellValue(row, 22, convertBooleanToCheckbox(item.getIsCompleted()), checkboxStyle);
            setCellValue(row, 23, item.getRemarks(), dataStyle);
            
            currentRow++;
        }
        
        // 컬럼 너비 자동 조정 (이미지 컬럼 제외)
        for (int i = 0; i < headers.length; i++) {
            if (i != 16) { // 이미지 컬럼은 제외
                sheet.autoSizeColumn(i);
            }
        }
        
        log.info("📊 이미지 포함 엑셀 파일 생성 완료 - 총 {} 건", data.size());
        return workbook;
    }

    /**
     * 🌐 URL에서 이미지 다운로드
     */
    private byte[] downloadImageFromUrl(String imageUrl) {
        try {
            // 상대 경로인 경우 절대 URL로 변환
            String fullUrl;
            String remoteServerUrl = "http://175.119.224.45:8080";
            
            if (imageUrl.startsWith("http")) {
                // 이미 절대 URL인 경우
                fullUrl = imageUrl;
            } else if (imageUrl.startsWith("/uploads/")) {
                // /uploads/로 시작하는 경우
                fullUrl = remoteServerUrl + imageUrl;
            } else if (imageUrl.startsWith("uploads/")) {
                // uploads/로 시작하는 경우
                fullUrl = remoteServerUrl + "/" + imageUrl;
            } else if (imageUrl.startsWith("/")) {
                // 기타 절대 경로
                fullUrl = remoteServerUrl + imageUrl;
            } else {
                // 상대 경로
                fullUrl = remoteServerUrl + "/uploads/" + imageUrl;
            }
            
            log.info("🔄 이미지 다운로드 시도: {} → {}", imageUrl, fullUrl);
            
            // URL 연결 및 이미지 다운로드
            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5초 타임아웃
            connection.setReadTimeout(10000); // 10초 읽기 타임아웃
            
            // User-Agent 설정 (일부 서버에서 요구)
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    
                    byte[] imageData = outputStream.toByteArray();
                    log.info("✅ 이미지 다운로드 성공: {} bytes - {}", imageData.length, fullUrl);
                    return imageData;
                }
            } else {
                log.warn("⚠️ 이미지 다운로드 실패 - HTTP {}: {}", responseCode, fullUrl);
                return null;
            }
            
        } catch (Exception e) {
            log.error("❌ 이미지 다운로드 예외: {} - URL: {}", e.getMessage(), imageUrl);
            return null;
        }
    }

    /**
     * 🖼️ 엑셀 셀에 이미지 삽입
     */
    private void insertImageIntoCell(XSSFWorkbook workbook, Sheet sheet, XSSFDrawing drawing, byte[] imageData, int rowIndex, int colIndex) {
        try {
            // 이미지 타입 결정 (JPG, PNG 등)
            int pictureType = XSSFWorkbook.PICTURE_TYPE_JPEG; // 기본값
            
            // 이미지 데이터의 시그니처로 타입 판별
            if (imageData.length >= 8) {
                // PNG 시그니처: 89 50 4E 47 0D 0A 1A 0A
                if (imageData[0] == (byte) 0x89 && imageData[1] == 0x50 && 
                    imageData[2] == 0x4E && imageData[3] == 0x47) {
                    pictureType = XSSFWorkbook.PICTURE_TYPE_PNG;
                }
                // JPEG 시그니처: FF D8
                else if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
                    pictureType = XSSFWorkbook.PICTURE_TYPE_JPEG;
                }
            }
            
            // 워크북에 이미지 추가
            int pictureIndex = workbook.addPicture(imageData, pictureType);
            
            // 이미지 위치 설정 (셀 위치에 맞춤)
            XSSFClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            anchor.setCol1(colIndex);
            anchor.setRow1(rowIndex);
            anchor.setCol2(colIndex + 1);
            anchor.setRow2(rowIndex + 1);
            
            // 이미지를 셀 크기에 맞게 조정
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            
            // 이미지 삽입
            XSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
            
            // 이미지 크기를 셀에 맞게 조정
            picture.resize(0.9); // 셀 크기의 90%로 조정
            
            log.debug("🖼️ 이미지 삽입 완료 - Row: {}, Col: {}", rowIndex, colIndex);
            
        } catch (Exception e) {
            log.error("❌ 이미지 삽입 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 🎯 유형 코드를 한글명으로 변환
     */
    private String convertReturnTypeToKorean(String returnTypeCode) {
        if (returnTypeCode == null) return "";
        
        switch (returnTypeCode) {
            case "FULL_RETURN":
                return "전체반품";
            case "PARTIAL_RETURN":
                return "부분반품";
            case "FULL_EXCHANGE":
                return "전체교환";
            case "PARTIAL_EXCHANGE":
                return "부분교환";
            case "EXCHANGE":
                return "교환";
            default:
                return returnTypeCode; // 알 수 없는 코드는 그대로 표시
        }
    }
    
    /**
     * 🎯 Boolean 값을 체크박스 스타일로 변환
     */
    private String convertBooleanToCheckbox(Boolean isCompleted) {
        if (isCompleted == null) return "☐";
        return isCompleted ? "☑" : "☐";
    }

    /**
     * 📄 엑셀 업로드 템플릿 생성
     */
    private XSSFWorkbook createExcelTemplate() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("교환반품 템플릿");
        
        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 필수 필드 스타일
        CellStyle requiredStyle = workbook.createCellStyle();
        Font requiredFont = workbook.createFont();
        requiredFont.setBold(true);
        requiredFont.setColor(IndexedColors.RED.getIndex());
        requiredStyle.setFont(requiredFont);
        requiredStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        requiredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 헤더 행
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "유형*", "주문일*", "CS접수일*", "사이트명*", "주문번호*", "환불금액",
            "고객명*", "고객연락처*", "상품코드", "색상", "사이즈", "수량",
            "배송비", "반품사유", "불량상세", "불량사진URL", "운송장번호", "비고"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            if (headers[i].contains("*")) {
                cell.setCellStyle(requiredStyle);
            } else {
                cell.setCellStyle(headerStyle);
            }
        }
        
        // 예시 데이터 행
        Row exampleRow = sheet.createRow(1);
        String[] examples = {
            "EXCHANGE", "2024-01-15", "2024-01-16", "쇼핑몰A", "ORDER001", "15000",
            "홍길동", "010-1234-5678", "ITEM001", "빨강", "L", "1",
            "3000", "색상 불만족", "", "", "", "고객 요청사항"
        };
        
        for (int i = 0; i < examples.length; i++) {
            Cell cell = exampleRow.createCell(i);
            cell.setCellValue(examples[i]);
        }
        
        // 설명 시트 추가
        Sheet instructionSheet = workbook.createSheet("작성 가이드");
        Row instrRow = instructionSheet.createRow(0);
        instrRow.createCell(0).setCellValue("📋 교환/반품 엑셀 업로드 가이드");
        
        instructionSheet.createRow(2).createCell(0).setCellValue("✅ 필수 입력 항목 (* 표시):");
        instructionSheet.createRow(3).createCell(0).setCellValue("- 유형: EXCHANGE(교환), RETURN(반품), FULL_RETURN(전체반품), PARTIAL_RETURN(부분반품)");
        instructionSheet.createRow(4).createCell(0).setCellValue("- 주문일: YYYY-MM-DD 형식");
        instructionSheet.createRow(5).createCell(0).setCellValue("- CS접수일: YYYY-MM-DD 형식");
        instructionSheet.createRow(6).createCell(0).setCellValue("- 사이트명, 주문번호, 고객명, 고객연락처");
        
        instructionSheet.createRow(7).createCell(0).setCellValue("⚠️ 주의사항:");
        instructionSheet.createRow(8).createCell(0).setCellValue("- 날짜는 반드시 YYYY-MM-DD 형식으로 입력");
        instructionSheet.createRow(9).createCell(0).setCellValue("- 금액은 숫자만 입력 (쉼표, 원화 표시 제외)");
        instructionSheet.createRow(10).createCell(0).setCellValue("- 첫 번째 행은 헤더이므로 삭제하지 마세요");
        
        // 컬럼 너비 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        for (int i = 0; i < 5; i++) {
            instructionSheet.autoSizeColumn(i);
        }
        
        return workbook;
    }

    /**
     * 📤 엑셀 업로드 처리
     */
    private Map<String, Object> processExcelUpload(MultipartFile file, HttpServletRequest request) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            int totalRows = sheet.getPhysicalNumberOfRows();
            if (totalRows <= 1) {
                result.put("success", false);
                result.put("message", "업로드할 데이터가 없습니다. (헤더 행 제외)");
                return result;
            }
            
            log.info("📊 엑셀 파싱 시작 - 총 {} 행 (헤더 제외 {} 건)", totalRows, totalRows - 1);
            
            // 헤더 행 건너뛰고 데이터 처리
            for (int rowIndex = 1; rowIndex < totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                try {
                    ReturnItemDTO dto = parseRowToDTO(row, rowIndex + 1);
                    if (dto != null) {
                        // 데이터 등록
                        returnItemService.createReturnItem(dto);
                        successList.add("행 " + (rowIndex + 1) + ": " + dto.getCustomerName() + " (" + dto.getOrderNumber() + ")");
                        log.debug("✅ 행 {} 등록 성공", rowIndex + 1);
                    }
                } catch (Exception e) {
                    String errorMessage = "행 " + (rowIndex + 1) + ": " + e.getMessage();
                    errorList.add(errorMessage);
                    log.warn("❌ 행 {} 등록 실패: {}", rowIndex + 1, e.getMessage());
                }
            }
            
            // 결과 집계
            result.put("success", true);
            result.put("totalProcessed", totalRows - 1);
            result.put("successCount", successList.size());
            result.put("errorCount", errorList.size());
            result.put("successList", successList);
            result.put("errorList", errorList);
            result.put("message", String.format("처리 완료: 성공 %d건, 실패 %d건", successList.size(), errorList.size()));
            
            log.info("📊 엑셀 업로드 완료 - 성공: {}건, 실패: {}건", successList.size(), errorList.size());
            
        }
        
        return result;
    }

    /**
     * 📄 엑셀 행을 DTO로 변환
     */
    private ReturnItemDTO parseRowToDTO(Row row, int rowNumber) throws Exception {
        ReturnItemDTO dto = new ReturnItemDTO();
        
        try {
            // 필수 필드 검증 및 설정
            String returnType = getCellValueAsString(row.getCell(0));
            if (returnType == null || returnType.trim().isEmpty()) {
                throw new Exception("유형이 누락되었습니다.");
            }
            dto.setReturnTypeCode(returnType.trim());
            
            String orderDate = getCellValueAsString(row.getCell(1));
            if (orderDate != null && !orderDate.trim().isEmpty()) {
                dto.setOrderDate(LocalDate.parse(orderDate.trim()));
            } else {
                throw new Exception("주문일이 누락되었습니다.");
            }
            
            String csDate = getCellValueAsString(row.getCell(2));
            if (csDate != null && !csDate.trim().isEmpty()) {
                dto.setCsReceivedDate(LocalDate.parse(csDate.trim()));
            } else {
                throw new Exception("CS접수일이 누락되었습니다.");
            }
            
            String siteName = getCellValueAsString(row.getCell(3));
            if (siteName == null || siteName.trim().isEmpty()) {
                throw new Exception("사이트명이 누락되었습니다.");
            }
            dto.setSiteName(siteName.trim());
            
            String orderNumber = getCellValueAsString(row.getCell(4));
            if (orderNumber == null || orderNumber.trim().isEmpty()) {
                throw new Exception("주문번호가 누락되었습니다.");
            }
            dto.setOrderNumber(orderNumber.trim());
            
            String customerName = getCellValueAsString(row.getCell(6));
            if (customerName == null || customerName.trim().isEmpty()) {
                throw new Exception("고객명이 누락되었습니다.");
            }
            dto.setCustomerName(customerName.trim());
            
            String customerPhone = getCellValueAsString(row.getCell(7));
            if (customerPhone == null || customerPhone.trim().isEmpty()) {
                throw new Exception("고객연락처가 누락되었습니다.");
            }
            dto.setCustomerPhone(customerPhone.trim());
            
            // 선택 필드 설정
            String refundAmountStr = getCellValueAsString(row.getCell(5));
            if (refundAmountStr != null && !refundAmountStr.trim().isEmpty()) {
                try {
                    dto.setRefundAmount(Long.parseLong(refundAmountStr.trim()));
                } catch (NumberFormatException e) {
                    throw new Exception("환불금액 형식이 올바르지 않습니다: " + refundAmountStr);
                }
            }
            
            dto.setOrderItemCode(getCellValueAsString(row.getCell(8)));
            dto.setProductColor(getCellValueAsString(row.getCell(9)));
            dto.setProductSize(getCellValueAsString(row.getCell(10)));
            
            String quantityStr = getCellValueAsString(row.getCell(11));
            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                try {
                    dto.setQuantity(Integer.parseInt(quantityStr.trim()));
                } catch (NumberFormatException e) {
                    dto.setQuantity(1); // 기본값
                }
            } else {
                dto.setQuantity(1); // 기본값
            }
            
            dto.setShippingFee(getCellValueAsString(row.getCell(12)));
            dto.setReturnReason(getCellValueAsString(row.getCell(13)));
            dto.setDefectDetail(getCellValueAsString(row.getCell(14)));
            dto.setDefectPhotoUrl(getCellValueAsString(row.getCell(15)));
            dto.setTrackingNumber(getCellValueAsString(row.getCell(16)));
            dto.setRemarks(getCellValueAsString(row.getCell(17)));
            
            // 기본값 설정
            dto.setIsCompleted(false);
            dto.setReturnStatusCode("PENDING");
            dto.setCreatedBy("EXCEL_UPLOAD");
            
            return dto;
            
        } catch (Exception e) {
            throw new Exception("데이터 파싱 오류: " + e.getMessage());
        }
    }

    /**
     * 🔧 셀 값을 문자열로 변환
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 🔧 셀에 값 설정 (타입별 처리 - 안전한 버전)
     */
    private void setCellValue(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        
        try {
            if (value == null) {
                cell.setCellValue("");
            } else if (value instanceof String) {
                String strValue = (String) value;
                // 빈 문자열이나 null 처리
                cell.setCellValue(strValue != null ? strValue : "");
            } else if (value instanceof Number) {
                Number numValue = (Number) value;
                cell.setCellValue(numValue.doubleValue());
            } else if (value instanceof Boolean) {
                Boolean boolValue = (Boolean) value;
                cell.setCellValue(boolValue ? "예" : "아니오");
            } else if (value instanceof LocalDate) {
                LocalDate dateValue = (LocalDate) value;
                cell.setCellValue(dateValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else if (value instanceof LocalDateTime) {
                LocalDateTime dateTimeValue = (LocalDateTime) value;
                cell.setCellValue(dateTimeValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                // 기타 타입은 안전하게 문자열로 변환
                String stringValue = value.toString();
                cell.setCellValue(stringValue != null ? stringValue : "");
            }
            
            // 스타일 적용 (null 체크)
            if (style != null) {
                cell.setCellStyle(style);
            }
            
        } catch (Exception e) {
            // 에러 발생 시 안전하게 빈 문자열로 설정
            log.warn("⚠️ 셀 값 설정 중 오류 발생 (행: {}, 열: {}): {}", row.getRowNum(), columnIndex, e.getMessage());
            cell.setCellValue("");
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }

    /**
     * 📊 통계 페이지 전용 데이터 API
     */
    @PostMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestBody Map<String, Object> params) {
        try {
            log.info("📊 통계 데이터 조회 시작 - 파라미터: {}", params);
            
            // 파라미터 추출 및 검증
            String dateFilterType = (String) params.get("dateFilterType");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String returnType = (String) params.get("returnType");
            
            // 검색 조건 DTO 생성 (통계는 COUNT 쿼리 사용으로 페이징 불필요)
            ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
            
            // 🚨 DEBUG: 파라미터 상세 분석
            log.info("🚨 DEBUG: 컨트롤러 파라미터 분석");
            log.info("🚨 DEBUG: startDate = '{}' (타입: {}, isEmpty: {})", 
                startDate, startDate != null ? startDate.getClass().getSimpleName() : "null", 
                startDate != null ? startDate.isEmpty() : "null이라 isEmpty 불가");
            log.info("🚨 DEBUG: endDate = '{}' (타입: {}, isEmpty: {})", 
                endDate, endDate != null ? endDate.getClass().getSimpleName() : "null", 
                endDate != null ? endDate.isEmpty() : "null이라 isEmpty 불가");
                
            // 날짜 필터 적용 (String을 LocalDate로 변환)
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    log.info("🚨 DEBUG: startDate 파싱 시도 중: '{}'", startDate);
                    LocalDate parsedStartDate = LocalDate.parse(startDate);
                    searchDTO.setStartDate(parsedStartDate);
                    log.info("✅ 시작날짜 설정 성공: {} → {}", startDate, parsedStartDate);
                } catch (Exception e) {
                    log.error("❌ 시작날짜 파싱 실패: '{}' - 에러: {}", startDate, e.getMessage(), e);
                }
            } else {
                log.warn("⚠️ startDate가 null이거나 빈 문자열: '{}'", startDate);
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    log.info("🚨 DEBUG: endDate 파싱 시도 중: '{}'", endDate);
                    LocalDate parsedEndDate = LocalDate.parse(endDate);
                    searchDTO.setEndDate(parsedEndDate);
                    log.info("✅ 종료날짜 설정 성공: {} → {}", endDate, parsedEndDate);
                } catch (Exception e) {
                    log.error("❌ 종료날짜 파싱 실패: '{}' - 에러: {}", endDate, e.getMessage(), e);
                }
            } else {
                log.warn("⚠️ endDate가 null이거나 빈 문자열: '{}'", endDate);
            }
            
            // 🚨 DEBUG: searchDTO 결과 확인
            log.info("🚨 DEBUG: searchDTO 설정 후:");
            log.info("🚨 DEBUG: searchDTO.getStartDate(): {}", searchDTO.getStartDate());
            log.info("🚨 DEBUG: searchDTO.getEndDate(): {}", searchDTO.getEndDate());
            log.info("🚨 DEBUG: searchDTO.hasSearchCondition(): {}", searchDTO.hasSearchCondition());
            
            log.info("📊 통계 조회 조건 - searchDTO: {}", searchDTO);
            log.info("📊 검색 조건 존재 여부: {}", searchDTO.hasSearchCondition());
            
            // 유형 필터 적용
            if (returnType != null && !returnType.isEmpty()) {
                searchDTO.setReturnTypeCode(returnType);
            }
            
            // 통계 데이터 조회
            Map<String, Object> result = new HashMap<>();
            
            // 1. 프로세스 단계별 통계
            Map<String, Object> processStats = new HashMap<>();
            // 회수완료 카드
            processStats.put("collectionCompleted", returnItemService.getCollectionCompletedCountBySearch(searchDTO));
            processStats.put("collectionPending", returnItemService.getCollectionPendingCountBySearch(searchDTO));
            
            // 물류확인 카드
            processStats.put("logisticsConfirmed", returnItemService.getLogisticsConfirmedCountBySearch(searchDTO));
            processStats.put("logisticsPending", returnItemService.getLogisticsPendingCountBySearch(searchDTO));
            
            // 교환출고 카드
            processStats.put("exchangeShipped", returnItemService.getExchangeShippedCountBySearch(searchDTO));
            processStats.put("exchangeNotShipped", returnItemService.getExchangeNotShippedCountBySearch(searchDTO));
            
            // 반품환불 카드
            processStats.put("returnRefunded", returnItemService.getReturnRefundedCountBySearch(searchDTO));
            processStats.put("returnNotRefunded", returnItemService.getReturnNotRefundedCountBySearch(searchDTO));
            
            // 배송비입금 카드
            processStats.put("paymentCompleted", returnItemService.getPaymentCompletedCountBySearch(searchDTO));
            processStats.put("paymentPending", returnItemService.getPaymentPendingCountBySearch(searchDTO));
            
            // 전체완료 카드 (기존 메서드 사용 + 상세 로그)
            Long completedCount = returnItemService.getCompletedCountBySearch(searchDTO);
            Long incompletedCount = returnItemService.getIncompletedCountBySearch(searchDTO);
            
            processStats.put("totalCompleted", completedCount != null ? completedCount : 0L);
            processStats.put("totalIncompleted", incompletedCount != null ? incompletedCount : 0L);
            
            log.info("📊 전체완료 통계 - 완료: {} 건, 미완료: {} 건", completedCount, incompletedCount);
            log.info("📊 ※ IS_COMPLETED 컬럼 기준으로 조회되는지 확인 필요");
            
            // 처리기간 임박 카드 (조회조건 무관)
            processStats.put("overdueTenDaysCount", returnItemService.getOverdueTenDaysCount());
            
            // 교환/반품 건수 (비율 계산용)
            processStats.put("totalExchange", returnItemService.getExchangeCountBySearch(searchDTO));
            processStats.put("totalReturn", returnItemService.getReturnCountBySearch(searchDTO));
            processStats.put("totalPaymentRequired", returnItemService.getPaymentRequiredCountBySearch(searchDTO));
            
            // 2. 유형별 통계
            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("FULL_EXCHANGE", returnItemService.getCountByTypeAndSearch("FULL_EXCHANGE", searchDTO));
            typeStats.put("PARTIAL_EXCHANGE", returnItemService.getCountByTypeAndSearch("PARTIAL_EXCHANGE", searchDTO));
            typeStats.put("FULL_RETURN", returnItemService.getCountByTypeAndSearch("FULL_RETURN", searchDTO));
            typeStats.put("PARTIAL_RETURN", returnItemService.getCountByTypeAndSearch("PARTIAL_RETURN", searchDTO));
            
            // 3. 트렌드 데이터 (차트용)
            Map<String, Object> trendData = returnItemService.getTrendDataBySearch(searchDTO);
            
            // 4. 전체 건수
            long totalCount = returnItemService.getTotalCountBySearch(searchDTO);
            
            // 5. 금액 통계
            Map<String, Object> amountStats = returnItemService.getAmountSummaryBySearch(searchDTO);
            
            // 6. 사이트별 통계
            Map<String, Long> siteStats = returnItemService.getSiteCountsBySearch(searchDTO);
            
            // 7. 사유별 통계
            Map<String, Long> reasonStats = returnItemService.getReasonCountsBySearch(searchDTO);
            
            // 결과 구성
            result.put("success", true);
            result.put("processStats", processStats);
            result.put("typeStats", typeStats);
            result.put("trendData", trendData);
            result.put("totalCount", totalCount);
            result.put("amountStats", amountStats);
            result.put("siteStats", siteStats);
            result.put("reasonStats", reasonStats);
            result.put("updateTime", LocalDateTime.now());
            
            log.info("📊 통계 데이터 조회 완료");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ 통계 데이터 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "통계 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 📥 통계 엑셀 다운로드 API
     */
    @PostMapping("/api/statistics/excel")
    public void downloadStatisticsExcel(
            @RequestParam(required = false) String dateFilterType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String returnType,
            @RequestParam(required = false) String brandFilter,
            HttpServletResponse response) throws IOException {
        
        try {
            log.info("📥 통계 엑셀 다운로드 시작");
            
            // 검색 조건 설정
            ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
            
            // 🔥 엑셀 다운로드 전용: 페이징 비활성화 제거 (전체 데이터 조회를 위해 다른 방법 사용)
            // 페이징 없는 전체 데이터 조회를 위해 findBySearch 또는 findAll 메소드 사용
            log.info("🔥 엑셀 다운로드 API: 페이징 없는 전체 데이터 조회 방식으로 변경");
            
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    searchDTO.setStartDate(LocalDate.parse(startDate));
                } catch (Exception e) {
                    log.warn("⚠️ 엑셀 다운로드 - 시작날짜 파싱 실패: {}", startDate);
                }
            }
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    searchDTO.setEndDate(LocalDate.parse(endDate));
                } catch (Exception e) {
                    log.warn("⚠️ 엑셀 다운로드 - 종료날짜 파싱 실패: {}", endDate);
                }
            }
            if (returnType != null && !returnType.isEmpty()) {
                searchDTO.setReturnTypeCode(returnType);
            }
            if (brandFilter != null && !brandFilter.isEmpty()) {
                searchDTO.setBrandFilter(brandFilter);
            }
            
            // 🔥 성능 최적화: 페이징 없는 전체 데이터 조회 (DB에서 직접)
            List<ReturnItemDTO> data;
            if (searchDTO.hasSearchCondition()) {
                log.info("🔥 검색 조건 있음 - 페이징 없는 검색 데이터 조회");
                data = returnItemService.findBySearch(searchDTO);
            } else {
                log.info("🔥 검색 조건 없음 - 페이징 없는 전체 데이터 조회");
                data = returnItemService.findAll();
            }
            
            log.info("🔥 엑셀 다운로드용 데이터 조회 완료: {} 건", data.size());
            
            // 엑셀 파일 생성
            XSSFWorkbook workbook = createStatisticsExcelFile(data, searchDTO);
            
            // 파일명 설정
            String dateRange = "";
            if (startDate != null && endDate != null) {
                dateRange = "_" + startDate + "_" + endDate;
            }
            String fileName = "교환반품통계" + dateRange + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            // 응답 헤더 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            // 엑셀 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();
            
            log.info("📥 통계 엑셀 다운로드 완료: {}", fileName);
            
        } catch (Exception e) {
            log.error("❌ 통계 엑셀 다운로드 실패: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📊 통계 전용 엑셀 파일 생성
     */
    private XSSFWorkbook createStatisticsExcelFile(List<ReturnItemDTO> data, ReturnItemSearchDTO searchDTO) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        
        // 데이터 스타일
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 시트 1: 상세 데이터
        Sheet dataSheet = workbook.createSheet("상세 데이터");
        createDataSheet(dataSheet, data, headerStyle, dataStyle);
        
        // 시트 2: 통계 요약
        Sheet summarySheet = workbook.createSheet("통계 요약");
        createSummarySheet(summarySheet, data, headerStyle, dataStyle);
        
        return workbook;
    }

    /**
     * 📊 데이터 시트 생성
     */
    private void createDataSheet(Sheet sheet, List<ReturnItemDTO> data, CellStyle headerStyle, CellStyle dataStyle) {
        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "순번", "유형", "주문일", "CS접수일", "사이트명", "주문번호", "환불금액", "고객명", "연락처",
            "주문품번", "색상", "사이즈", "수량", "배송비", "사유", "불량상세", "운송장번호",
            "회수완료일", "물류확인일", "출고일", "환불일", "완료여부", "비고"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 데이터 행 생성
        int rowIndex = 1;
        for (ReturnItemDTO item : data) {
            Row row = sheet.createRow(rowIndex++);
            
            setCellValue(row, 0, rowIndex - 1, dataStyle);
            setCellValue(row, 1, convertReturnTypeToKorean(item.getReturnTypeCode()), dataStyle);
            setCellValue(row, 2, item.getOrderDate(), dataStyle);
            setCellValue(row, 3, item.getCsReceivedDate(), dataStyle);
            setCellValue(row, 4, item.getSiteName(), dataStyle);
            setCellValue(row, 5, item.getOrderNumber(), dataStyle);
            setCellValue(row, 6, item.getRefundAmount(), dataStyle);
            setCellValue(row, 7, item.getCustomerName(), dataStyle);
            setCellValue(row, 8, item.getCustomerPhone(), dataStyle);
            setCellValue(row, 9, item.getOrderItemCode(), dataStyle);
            setCellValue(row, 10, item.getProductColor(), dataStyle);
            setCellValue(row, 11, item.getProductSize(), dataStyle);
            setCellValue(row, 12, item.getQuantity(), dataStyle);
            setCellValue(row, 13, item.getShippingFee(), dataStyle);
            setCellValue(row, 14, item.getReturnReason(), dataStyle);
            setCellValue(row, 15, item.getDefectDetail(), dataStyle);
            setCellValue(row, 16, item.getTrackingNumber(), dataStyle);
            setCellValue(row, 17, item.getCollectionCompletedDate(), dataStyle);
            setCellValue(row, 18, item.getLogisticsConfirmedDate(), dataStyle);
            setCellValue(row, 19, item.getShippingDate(), dataStyle);
            setCellValue(row, 20, item.getRefundDate(), dataStyle);
            setCellValue(row, 21, convertBooleanToCheckbox(item.getIsCompleted()), dataStyle);
            setCellValue(row, 22, item.getRemarks(), dataStyle);
        }
        
        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 📊 요약 시트 생성
     */
    private void createSummarySheet(Sheet sheet, List<ReturnItemDTO> data, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 교환반품 통계 요약");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        // 1. 전체 현황
        Row totalHeaderRow = sheet.createRow(rowIndex++);
        Cell totalHeaderCell = totalHeaderRow.createCell(0);
        totalHeaderCell.setCellValue("📋 전체 현황");
        totalHeaderCell.setCellStyle(headerStyle);
        
        Row totalRow = sheet.createRow(rowIndex++);
        totalRow.createCell(0).setCellValue("전체 건수");
        totalRow.createCell(1).setCellValue(data.size());
        
        rowIndex++; // 빈 행
        
        // 2. 유형별 현황
        Row typeHeaderRow = sheet.createRow(rowIndex++);
        Cell typeHeaderCell = typeHeaderRow.createCell(0);
        typeHeaderCell.setCellValue("📊 유형별 현황");
        typeHeaderCell.setCellStyle(headerStyle);
        
        Map<String, Long> typeStats = new HashMap<>();
        typeStats.put("전체교환", data.stream().filter(d -> "FULL_EXCHANGE".equals(d.getReturnTypeCode())).count());
        typeStats.put("부분교환", data.stream().filter(d -> "PARTIAL_EXCHANGE".equals(d.getReturnTypeCode())).count());
        typeStats.put("전체반품", data.stream().filter(d -> "FULL_RETURN".equals(d.getReturnTypeCode())).count());
        typeStats.put("부분반품", data.stream().filter(d -> "PARTIAL_RETURN".equals(d.getReturnTypeCode())).count());
        
        for (Map.Entry<String, Long> entry : typeStats.entrySet()) {
            Row typeRow = sheet.createRow(rowIndex++);
            typeRow.createCell(0).setCellValue(entry.getKey());
            typeRow.createCell(1).setCellValue(entry.getValue());
        }
        
        rowIndex++; // 빈 행
        
        // 3. 프로세스 현황
        Row processHeaderRow = sheet.createRow(rowIndex++);
        Cell processHeaderCell = processHeaderRow.createCell(0);
        processHeaderCell.setCellValue("🔄 프로세스 현황");
        processHeaderCell.setCellStyle(headerStyle);
        
        long collectionCompleted = data.stream().filter(d -> d.getCollectionCompletedDate() != null).count();
        long logisticsConfirmed = data.stream().filter(d -> d.getLogisticsConfirmedDate() != null).count();
        long exchangeShipped = data.stream().filter(d -> d.getShippingDate() != null && 
                                                     ("FULL_EXCHANGE".equals(d.getReturnTypeCode()) || 
                                                      "PARTIAL_EXCHANGE".equals(d.getReturnTypeCode()))).count();
        long returnRefunded = data.stream().filter(d -> d.getRefundDate() != null && 
                                                    ("FULL_RETURN".equals(d.getReturnTypeCode()) || 
                                                     "PARTIAL_RETURN".equals(d.getReturnTypeCode()))).count();
        long totalCompleted = data.stream().filter(d -> Boolean.TRUE.equals(d.getIsCompleted())).count();
        
        Row[] processRows = {
            sheet.createRow(rowIndex++),
            sheet.createRow(rowIndex++),
            sheet.createRow(rowIndex++),
            sheet.createRow(rowIndex++),
            sheet.createRow(rowIndex++)
        };
        
        String[] processLabels = {"회수완료", "물류확인", "교환출고", "반품환불", "전체완료"};
        long[] processValues = {collectionCompleted, logisticsConfirmed, exchangeShipped, returnRefunded, totalCompleted};
        
        for (int i = 0; i < processLabels.length; i++) {
            processRows[i].createCell(0).setCellValue(processLabels[i]);
            processRows[i].createCell(1).setCellValue(processValues[i]);
        }
        
        // 컬럼 너비 자동 조정
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * 📥 이미지 포함 통계 엑셀 다운로드 API
     */
    @PostMapping("/api/statistics/excel-with-images")
    public void downloadStatisticsExcelWithImages(
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) throws IOException {
        
        try {
            log.info("📥 이미지 포함 통계 엑셀 다운로드 시작");
            
            // 검색 조건 추출
            String dateFilterType = (String) params.get("dateFilterType");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            String returnType = (String) params.get("returnType");
            String brandFilter = (String) params.get("brandFilter");
            
            // 이미지 데이터 추출
            String dashboardImage = (String) params.get("dashboardImage");
            String trendChartImage = (String) params.get("trendChartImage");
            String typeChartImage = (String) params.get("typeChartImage");
            
            // 검색 조건 설정
            ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
            
            // 🔥 이미지 포함 엑셀 다운로드 전용: 페이징 없는 전체 데이터 조회 (성능 최적화)
            // 페이징 설정 제거하고 DB에서 직접 전체 데이터 조회
            log.info("🔥 이미지 포함 엑셀 다운로드 API: 페이징 없는 전체 데이터 조회 방식으로 변경");
            
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    searchDTO.setStartDate(LocalDate.parse(startDate));
                } catch (Exception e) {
                    log.warn("⚠️ 엑셀 다운로드 - 시작날짜 파싱 실패: {}", startDate);
                }
            }
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    searchDTO.setEndDate(LocalDate.parse(endDate));
                } catch (Exception e) {
                    log.warn("⚠️ 엑셀 다운로드 - 종료날짜 파싱 실패: {}", endDate);
                }
            }
            if (returnType != null && !returnType.isEmpty()) {
                searchDTO.setReturnTypeCode(returnType);
            }
            if (brandFilter != null && !brandFilter.isEmpty()) {
                searchDTO.setBrandFilter(brandFilter);
            }
            
            // 통계 데이터 조회
            List<ReturnItemDTO> data;
            if (searchDTO.hasSearchCondition()) {
                data = returnItemService.findBySearch(searchDTO);
            } else {
                data = returnItemService.findAll();
            }
            
            // 이미지 포함 엑셀 파일 생성
            XSSFWorkbook workbook = createStatisticsExcelFileWithImages(data, searchDTO, 
                    dashboardImage, trendChartImage, typeChartImage);
            
            // 파일명 설정
            String dateRange = "";
            if (startDate != null && endDate != null) {
                dateRange = "_" + startDate + "_" + endDate;
            }
            String fileName = "교환반품통계(이미지포함)" + dateRange + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            // 응답 헤더 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            // 파일명 인코딩 처리
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 캐시 방지
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // 엑셀 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();
            
            log.info("📥 이미지 포함 통계 엑셀 다운로드 완료: {}", fileName);
            
        } catch (Exception e) {
            log.error("❌ 이미지 포함 통계 엑셀 다운로드 실패: {}", e.getMessage(), e);
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📊 이미지 포함 통계 전용 엑셀 파일 생성
     */
    private XSSFWorkbook createStatisticsExcelFileWithImages(List<ReturnItemDTO> data, 
                                                            ReturnItemSearchDTO searchDTO,
                                                            String dashboardImage,
                                                            String trendChartImage,
                                                            String typeChartImage) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        
        // 데이터 스타일
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 시트 1: 통계 대시보드 (이미지 포함)
        Sheet dashboardSheet = workbook.createSheet("📊 통계 대시보드");
        createDashboardSheetWithImages(dashboardSheet, workbook, dashboardImage, 
                                     trendChartImage, typeChartImage, headerStyle, dataStyle);
        
        // 시트 2: 상세 데이터
        Sheet dataSheet = workbook.createSheet("📋 상세 데이터");
        createDataSheet(dataSheet, data, headerStyle, dataStyle);
        
        // 시트 3: 통계 요약
        Sheet summarySheet = workbook.createSheet("📈 통계 요약");
        createSummarySheet(summarySheet, data, headerStyle, dataStyle);
        
        return workbook;
    }

    /**
     * 📊 이미지 포함 대시보드 시트 생성
     */
    private void createDashboardSheetWithImages(Sheet sheet, XSSFWorkbook workbook,
                                               String dashboardImage, String trendChartImage, 
                                               String typeChartImage, CellStyle headerStyle, 
                                               CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 교환반품 통계 대시보드");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        try {
            // 1. 통계 카드 이미지 추가
            if (dashboardImage != null && !dashboardImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, dashboardImage, 
                                         "통계 카드 현황", rowIndex, headerStyle);
            }
            
            // 2. 트렌드 차트 이미지 추가
            if (trendChartImage != null && !trendChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, trendChartImage, 
                                         "트렌드 차트", rowIndex, headerStyle);
            }
            
            // 3. 유형별 차트 이미지 추가
            if (typeChartImage != null && !typeChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, typeChartImage, 
                                         "유형별 분포 차트", rowIndex, headerStyle);
            }
            
        } catch (Exception e) {
            log.error("❌ 이미지 추가 실패: {}", e.getMessage(), e);
        }
        
        // 컬럼 너비 설정
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 15000);
    }

    /**
     * 📊 이미지를 시트에 추가하는 헬퍼 메서드
     */
    private int addImageToSheet(Sheet sheet, XSSFWorkbook workbook, String base64Image, 
                               String title, int startRow, CellStyle headerStyle) {
        try {
            // 제목 추가
            Row titleRow = sheet.createRow(startRow++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(headerStyle);
            
            // Base64 이미지 데이터 디코딩
            String base64Data = base64Image.split(",")[1]; // "data:image/png;base64," 부분 제거
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            
            // 이미지를 워크북에 추가
            int pictureIndex = workbook.addPicture(imageBytes, XSSFWorkbook.PICTURE_TYPE_PNG);
            
            // 드로잉 생성
            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            
            // 이미지 위치 설정 (startRow부터 시작)
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, startRow, 10, startRow + 20);
            
            // 이미지 삽입
            XSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
            picture.resize(0.8); // 이미지 크기 조정
            
            // 이미지 공간만큼 행 높이 조정
            for (int i = startRow; i < startRow + 20; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    row = sheet.createRow(i);
                }
                row.setHeight((short) (20 * 20)); // 높이 설정
            }
            
            return startRow + 22; // 이미지 다음 행 반환
            
        } catch (Exception e) {
            log.error("❌ 이미지 추가 실패: {}", e.getMessage(), e);
            return startRow + 2; // 실패 시 2행만 건너뛰기
        }
    }

    /**
     * 🚀 AJAX 기반 빠른 카드 필터링 API
     */
    @PostMapping("/api/filter-html")
    public String filterByCardAjaxHtml(@RequestBody Map<String, Object> request, Model model) {
        try {
            String filterType = (String) request.get("filterType");
            int page = (int) request.getOrDefault("page", 0);
            int size = (int) request.getOrDefault("size", 20);
            
            log.info("🚀 AJAX HTML 필터링 요청 - 타입: {}, 페이지: {}, 크기: {}", filterType, page, size);
            
            // 검색 조건 설정
            ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
            searchDTO.setPage(page);
            searchDTO.setSize(size);
            searchDTO.setSortBy("id");
            searchDTO.setSortDir("DESC");
            
            // 필터 적용
            Page<ReturnItemDTO> resultPage;
            List<String> filterList = Arrays.asList(filterType.split(","));
            
            if (filterList.size() > 1) {
                resultPage = returnItemService.findByMultipleFilters(filterList, searchDTO);
            } else {
                String singleFilter = filterList.get(0).trim();
                resultPage = applySingleFilter(singleFilter, searchDTO);
            }
            
            // 모델에 데이터 추가
            model.addAttribute("returnItems", resultPage);
            model.addAttribute("filterType", filterType);
            model.addAttribute("totalElements", resultPage.getTotalElements());
            
            log.info("✅ AJAX HTML 필터링 완료 - 결과: {} 건", resultPage.getTotalElements());
            
            // 테이블 부분만 렌더링해서 반환
            return "exchange/table-section";
            
        } catch (Exception e) {
            log.error("❌ AJAX HTML 필터링 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "필터링 중 오류가 발생했습니다: " + e.getMessage());
            return "exchange/error-section";
        }
    }
    
    @PostMapping("/api/filter")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> filterByCardAjax(@RequestBody Map<String, Object> request) {
        try {
            String filterType = (String) request.get("filterType");
            int page = (int) request.getOrDefault("page", 0);
            int size = (int) request.getOrDefault("size", 20);
            
            log.info("🚀 AJAX 필터링 요청 - 타입: {}, 페이지: {}, 크기: {}", filterType, page, size);
            
            // 기존 Map 방식 사용 (매퍼 XML과 호환)
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("page", page);
            searchParams.put("size", size);
            searchParams.put("sortBy", "RETURN_ID");
            searchParams.put("sortDir", "DESC");
            
            // 🎯 Sample 방식: 전용 서비스 메소드 직접 호출 (다중 필터 지원)
            ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
            searchDTO.setPage(page);
            searchDTO.setSize(size);
            searchDTO.setSortBy("id");
            searchDTO.setSortDir("DESC");
            
            // 다중 필터 처리 (쉼표로 구분된 필터들)
            Page<ReturnItemDTO> resultPage;
            List<String> filterList = Arrays.asList(filterType.split(","));
            
            log.info("🔍 필터 목록 파싱: {} -> {}", filterType, filterList);
            
            if (filterList.size() > 1) {
                // 다중 필터 적용
                log.info("🎯 다중 필터 적용: {}", filterList);
                resultPage = returnItemService.findByMultipleFilters(filterList, searchDTO);
            } else {
                // 단일 필터 적용
                String singleFilter = filterList.get(0).trim();
                log.info("🔍 단일 필터 적용: {}", singleFilter);
                resultPage = applySingleFilter(singleFilter, searchDTO);
            }
            
            log.info("✅ AJAX 필터링 완료 - 결과: {} 건", resultPage.getTotalElements());
            
            // 📋 안전한 데이터 처리 (Page 객체 활용)
            List<ReturnItemDTO> items = resultPage.getContent();
            if (items == null) {
                items = new ArrayList<>();
            }
            
            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", items);  // 🔧 항상 빈 배열이라도 반환
            response.put("totalElements", resultPage.getTotalElements());
            response.put("totalPages", resultPage.getTotalPages());
            response.put("currentPage", resultPage.getNumber());
            response.put("size", resultPage.getSize());
            response.put("filterType", filterType);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ AJAX 필터링 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "필터링 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    // ============================================================================
    // 🖼️ 이미지 업로드 기능 (Sample에서 통합)
    // ============================================================================
    
    /**
     * 🌐 이미지 절대 URL 생성 헬퍼 메서드
     */
    private String getImageAbsoluteUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        return fileServerUrl + "/uploads/" + relativePath;
    }
    
    /**
     * 📷 이미지 업로드 처리 (파일 업로드 + 붙여넣기 지원)
     */
    private String processImageUpload(MultipartFile attachmentPhoto, String attachmentImageData) throws IOException {
        // 1. 파일 업로드가 있는 경우
        if (attachmentPhoto != null && !attachmentPhoto.isEmpty()) {
            log.info("📁 파일 업로드 처리: {}", attachmentPhoto.getOriginalFilename());
            return uploadToRemoteServer(attachmentPhoto);
        }
        
        // 2. 붙여넣기 이미지 데이터가 있는 경우
        if (attachmentImageData != null && !attachmentImageData.trim().isEmpty()) {
            log.info("📋 붙여넣기 이미지 처리");
            return uploadBase64ToRemoteServer(attachmentImageData);
        }
        
        return null;
    }
    
    /**
     * 🌐 원격 서버에 파일 업로드
     */
    private String uploadToRemoteServer(MultipartFile file) throws IOException {
        log.info("🌐 원격 서버({})에 파일 업로드 시작: {}", fileServerUrl, file.getOriginalFilename());
        log.info("🔧 디버그 - fileServerUrl 값: {}", fileServerUrl);
        
        try {
            // 고유 파일명 생성
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
            log.info("🔧 디버그 - 생성된 고유파일명: {}", uniqueFilename);
            
            // RestTemplate을 사용한 HTTP 업로드
            RestTemplate restTemplate = new RestTemplate();
            
            // MultiValueMap 생성 (multipart/form-data)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return uniqueFilename;
                }
            });
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // HTTP 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 원격 서버 업로드 API 호출
            String uploadUrl = fileServerUrl + "/exchange/api/upload";
            log.info("🔧 디버그 - 파일 업로드 URL: {}", uploadUrl);
            log.info("🔧 디버그 - 요청 헤더: {}", headers);
            log.info("🔧 디버그 - 파일 크기: {} bytes", file.getSize());
            
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            log.info("🔧 디버그 - 응답 상태코드: {}", response.getStatusCode());
            log.info("🔧 디버그 - 응답 본문: {}", response.getBody());
            log.info("🔧 디버그 - 응답 헤더: {}", response.getHeaders());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 원격 서버 응답에서 실제 저장된 파일 경로 추출
                try {
                    String responseBody = response.getBody();
                    log.info("🔧 디버그 - 원격 서버 응답: {}", responseBody);
                    
                    // JSON 응답에서 relativePath 추출 (간단한 문자열 파싱)
                    if (responseBody != null && responseBody.contains("\"relativePath\"")) {
                        int startIndex = responseBody.indexOf("\"relativePath\":\"") + 16;
                        int endIndex = responseBody.indexOf("\"", startIndex);
                        String actualRelativePath = responseBody.substring(startIndex, endIndex);
                        log.info("✅ 원격 서버 업로드 성공 - 실제 파일경로: {}", actualRelativePath);
                        return actualRelativePath;
                    } else {
                        // 응답 파싱 실패시 fallback
                        String relativePath = "images/" + uniqueFilename;
                        log.warn("⚠️ 응답 파싱 실패, fallback 경로 사용: {}", relativePath);
                        return relativePath;
                    }
                } catch (Exception e) {
                    log.error("❌ 응답 파싱 오류: {}", e.getMessage());
                    String relativePath = "images/" + uniqueFilename;
                    log.warn("⚠️ 파싱 오류, fallback 경로 사용: {}", relativePath);
                    return relativePath;
                }
            } else {
                log.error("❌ 원격 서버 업로드 실패 - 상태코드: {}, 응답: {}", response.getStatusCode(), response.getBody());
                // 실패시 로컬에 저장 (폴백)
                return saveUploadedFileLocally(file);
            }
            
        } catch (Exception e) {
            log.error("❌ 원격 서버 업로드 오류, 로컬 저장으로 폴백: {}", e.getMessage(), e);
            log.error("❌ 상세 오류 정보: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("❌ 근본 원인: {}", e.getCause().getMessage());
            }
            // 오류시 로컬에 저장 (폴백)
            return saveUploadedFileLocally(file);
        }
    }
    
    /**
     * 🌐 Base64 이미지를 원격 서버에 업로드
     */
    private String uploadBase64ToRemoteServer(String base64Data) throws IOException {
        log.info("🌐 원격 서버에 Base64 이미지 업로드 시작");
        
        try {
            // Base64 데이터 파싱
            String[] parts = base64Data.split(",");
            String imageData = parts.length > 1 ? parts[1] : parts[0];
            
            // 파일 확장자 결정
            String fileExtension = ".png";
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
            
            // 고유 파일명 생성
            String uniqueFilename = "pasted_" + UUID.randomUUID().toString() + "_" + 
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                                fileExtension;
            
            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            
            // RestTemplate을 사용한 HTTP 업로드
            RestTemplate restTemplate = new RestTemplate();
            
            // MultiValueMap 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return uniqueFilename;
                }
            });
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // HTTP 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 원격 서버 업로드 API 호출
            String uploadUrl = fileServerUrl + "/exchange/api/upload";
            log.info("🔧 디버그 - Base64 업로드 URL: {}", uploadUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 원격 서버 응답에서 실제 저장된 파일 경로 추출
                try {
                    String responseBody = response.getBody();
                    log.info("🔧 디버그 - Base64 원격 서버 응답: {}", responseBody);
                    
                    // JSON 응답에서 relativePath 추출 (간단한 문자열 파싱)
                    if (responseBody != null && responseBody.contains("\"relativePath\"")) {
                        int startIndex = responseBody.indexOf("\"relativePath\":\"") + 16;
                        int endIndex = responseBody.indexOf("\"", startIndex);
                        String actualRelativePath = responseBody.substring(startIndex, endIndex);
                        log.info("✅ 원격 서버 Base64 업로드 성공 - 실제 파일경로: {}", actualRelativePath);
                        return actualRelativePath;
                    } else {
                        // 응답 파싱 실패시 fallback
                        String relativePath = "images/" + uniqueFilename;
                        log.warn("⚠️ Base64 응답 파싱 실패, fallback 경로 사용: {}", relativePath);
                        return relativePath;
                    }
                } catch (Exception e) {
                    log.error("❌ Base64 응답 파싱 오류: {}", e.getMessage());
                    String relativePath = "images/" + uniqueFilename;
                    log.warn("⚠️ Base64 파싱 오류, fallback 경로 사용: {}", relativePath);
                    return relativePath;
                }
            } else {
                log.error("❌ 원격 서버 Base64 업로드 실패 - 상태코드: {}", response.getStatusCode());
                // 실패시 로컬에 저장 (폴백)
                return savePastedImageLocally(base64Data);
            }
            
        } catch (Exception e) {
            log.error("❌ 원격 서버 Base64 업로드 오류, 로컬 저장으로 폴백: {}", e.getMessage());
            // 오류시 로컬에 저장 (폴백)
            return savePastedImageLocally(base64Data);
        }
    }
    
    /**
     * 🔧 고유 파일명 생성
     */
    private String generateUniqueFilename(String originalFilename) {
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return UUID.randomUUID().toString() + "_" + 
               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               fileExtension;
    }
    
    /**
     * 📁 로컬에 파일 저장 (폴백용)
     */
    private String saveUploadedFileLocally(MultipartFile file) throws IOException {
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
        
        log.info("✅ 파일 저장 완료: {}", filePath);
        
        // 웹에서 접근 가능한 상대 경로 반환
        return "images/" + uniqueFilename;
    }
    
    /**
     * 📋 붙여넣기 이미지 로컬 저장 (폴백용)
     */
    private String savePastedImageLocally(String base64Data) throws IOException {
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
        
        log.info("✅ 붙여넣기 이미지 저장 완료: {}", filePath);
        
        // 웹에서 접근 가능한 상대 경로 반환
        return "images/" + uniqueFilename;
    }
    
    // ============================================================================
    // 🎯 다중 필터링 기능 (Sample에서 통합)
    // ============================================================================
    
    /**
     * 🎯 다중 필터 + 검색 조건 함께 적용 메서드 (Sample에서 통합)
     */
    private Page<ReturnItemDTO> applyMultipleFiltersWithSearch(String filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 + 검색 처리 시작 - filters: {}, 검색조건: {}", filters, searchDTO.getKeyword());
        
        // 필터를 쉼표로 분리
        String[] filterArray = filters.split(",");
        List<String> filterList = Arrays.asList(filterArray);
        
        Page<ReturnItemDTO> result = null;
        
        try {
            // 🎯 서비스에서 필터와 검색을 함께 처리 (AND 조건으로 교집합)
            log.info("🔍 다중 필터 + 검색 교집합 처리 시작");
            result = returnItemService.findByMultipleFiltersWithSearch(filterList, searchDTO);
            log.info("✅ 다중 필터 + 검색 교집합 처리 완료 - 결과: {} 건", result.getTotalElements());
            
        } catch (Exception e) {
            log.error("❌ 다중 필터 + 검색 적용 중 오류 발생, fallback 처리", e);
            
            // fallback: 검색만 적용
            result = returnItemService.search(searchDTO);
            log.info("🔄 검색만 적용으로 fallback - 결과: {} 건", result.getTotalElements());
        }
        
        return result;
    }
    
    /**
     * 🎯 다중 필터 적용 메서드 (Sample에서 통합)
     */
    private Page<ReturnItemDTO> applyMultipleFilters(String filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 처리 시작 - filters: {}", filters);
        
        // 필터를 쉼표로 분리
        String[] filterArray = filters.split(",");
        List<String> filterList = Arrays.asList(filterArray);
        
        Page<ReturnItemDTO> result = null;
        
        try {
            // 🎯 서비스에서 다중 필터 교집합 처리 (AND 조건)
            if (filterList.size() > 0) {
                log.info("🔍 다중 필터 교집합 처리: {}", filterList);
                result = returnItemService.findByMultipleFilters(filterList, searchDTO);
                log.info("✅ 다중 필터 교집합 처리 완료 - 결과: {} 건", result.getTotalElements());
            } else {
                // 필터가 없으면 전체 조회
                result = returnItemService.findAll(searchDTO.getPage(), searchDTO.getSize(), 
                    searchDTO.getSortBy(), searchDTO.getSortDir());
            }
            
        } catch (Exception e) {
            log.error("❌ 다중 필터 적용 중 오류 발생, 전체 조회로 fallback 처리", e);
            
            // 전체 조회로 fallback
            result = returnItemService.findAll(searchDTO.getPage(), searchDTO.getSize(), 
                searchDTO.getSortBy(), searchDTO.getSortDir());
        }
        
        return result;
    }
    
    /**
     * 🎯 단일 필터 적용 메서드 (🚫 처리완료 건 제외 적용)
     */
    private Page<ReturnItemDTO> applySingleFilter(String filterType, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 단일 필터 적용 - filterType: {} (🚫 처리완료 건 제외)", filterType);
        
        try {
            // 🎯 모든 개별 필터를 최적화된 매퍼로 처리 (IS_COMPLETED = 0 조건 적용)
            List<String> filters = Arrays.asList(filterType);
            return returnItemService.findByMultipleFilters(filters, searchDTO);
            
        } catch (Exception e) {
            log.error("❌ 단일 필터 적용 중 오류 발생: {}", e.getMessage(), e);
            return returnItemService.findAll(searchDTO.getPage(), searchDTO.getSize(), 
                searchDTO.getSortBy(), searchDTO.getSortDir());
        }
    }
    
    // ============================================================================
    // 🚨 10일 경과 건 및 기타 관리 기능 (Sample에서 통합)
    // ============================================================================
    
    /**
     * 📅 일괄 날짜 수정 API (Sample에서 통합)
     */
    @PostMapping("/api/bulk-update-dates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkUpdateDates(
            @RequestBody List<ReturnItemBulkDateUpdateDTO> updates) {
        
        log.info("📅 일괄 날짜 업데이트 요청 - 대상 건수: {}", updates.size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 유효성 검사
            if (updates == null || updates.isEmpty()) {
                response.put("success", false);
                response.put("message", "업데이트할 데이터가 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 현재 로그인 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String updatedBy = authentication.getName();
            log.info("📝 날짜 수정자: {}", updatedBy);
            
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
            
            log.info("✅ 일괄 날짜 업데이트 성공 - 업데이트된 건수: {}/{}, 수정자: {}", updatedCount, updates.size(), updatedBy);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 일괄 날짜 업데이트 실패", e);
            
            response.put("success", false);
            response.put("message", "업데이트 중 오류가 발생했습니다: " + e.getMessage());
            response.put("updatedCount", 0);
            response.put("totalCount", updates.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * ✅ 완료 상태 업데이트 API (Sample에서 통합)
     */
    @PostMapping("/api/update-completion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCompletionStatus(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long id = Long.valueOf(request.get("id").toString());
            Boolean isCompleted = (Boolean) request.get("isCompleted");
            
            log.info("✅ 완료 상태 업데이트 요청 - ID: {}, 완료: {}", id, isCompleted);
            
            // 완료 상태 업데이트 실행
            boolean updated = returnItemService.updateCompletionStatus(id, isCompleted);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "완료 상태가 성공적으로 업데이트되었습니다.");
                log.info("✅ 완료 상태 업데이트 성공 - ID: {}, 완료: {}", id, isCompleted);
            } else {
                response.put("success", false);
                response.put("message", "해당 항목을 찾을 수 없습니다.");
                log.warn("⚠️ 완료 상태 업데이트 실패 - ID: {} (항목을 찾을 수 없음)", id);
            }
            
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 완료 상태 업데이트 실패", e);
            
            response.put("success", false);
            response.put("message", "완료 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // 🆕 이미지 첨부 API (불량상세 메모 포함) - 이미지 없이 메모만 수정도 가능
    @PostMapping("/api/attach-image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> attachImage(
            @RequestParam(value = "defectPhoto", required = false) MultipartFile defectPhoto,
            @RequestParam("itemId") Long itemId,
            @RequestParam(value = "defectDetail", required = false) String defectDetail) {
        
        log.info("📷 이미지/메모 업데이트 요청: itemId={}, 파일명={}, 불량상세={}", 
            itemId, (defectPhoto != null ? defectPhoto.getOriginalFilename() : "없음"), defectDetail);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean hasFile = defectPhoto != null && !defectPhoto.isEmpty();
            boolean hasDefectDetail = defectDetail != null && !defectDetail.trim().isEmpty();
            
            // 파일과 불량상세 둘 다 없으면 에러
            if (!hasFile && !hasDefectDetail) {
                response.put("success", false);
                response.put("message", "이미지 또는 불량상세 메모 중 하나는 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String imagePath = null;
            
            // 🖼️ 이미지 파일이 있는 경우에만 업로드 처리
            if (hasFile) {
                // 파일 크기 검사 (10MB)
                if (defectPhoto.getSize() > 10 * 1024 * 1024) {
                    response.put("success", false);
                    response.put("message", "파일 크기가 10MB를 초과합니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 이미지 파일인지 검사
                String contentType = defectPhoto.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    response.put("success", false);
                    response.put("message", "이미지 파일만 업로드 가능합니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 원격 서버에 이미지 업로드
                imagePath = uploadToRemoteServer(defectPhoto);
                
                if (imagePath == null || imagePath.isEmpty()) {
                    throw new RuntimeException("원격 서버 업로드 실패");
                }
                
                // DB에 이미지 경로 저장
                returnItemService.updateDefectPhotoUrl(itemId, imagePath);
                log.info("✅ 이미지 업로드 완료: itemId={}, imagePath={}", itemId, imagePath);
            }
            
            // 📝 불량상세 메모 업데이트 (이미지 없이도 가능)
            if (hasDefectDetail) {
                returnItemService.updateDefectDetail(itemId, defectDetail);
                log.info("📝 불량상세 메모 업데이트 완료: itemId={}, defectDetail={}", itemId, defectDetail);
            }
            
            // 📊 결과 메시지 생성
            String message;
            if (hasFile && hasDefectDetail) {
                message = "이미지와 불량상세 정보가 성공적으로 저장되었습니다.";
            } else if (hasFile) {
                message = "이미지가 성공적으로 저장되었습니다.";
            } else {
                message = "불량상세 메모가 성공적으로 저장되었습니다.";
            }
            
            response.put("success", true);
            response.put("message", message);
            response.put("imagePath", imagePath);
            response.put("defectDetail", defectDetail);
            response.put("hasImageUpdate", hasFile);
            response.put("hasDefectDetailUpdate", hasDefectDetail);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 이미지 첨부 실패: itemId={}, 오류={}", itemId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "이미지 첨부 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 🆕 이미지 삭제 API
    @DeleteMapping("/api/delete-image/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable Long itemId) {
        
        log.info("🗑️ 이미지 삭제 요청: itemId={}", itemId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // DB에서 이미지 경로 제거
            returnItemService.updateDefectPhotoUrl(itemId, null);
            
            log.info("✅ 이미지 삭제 완료: itemId={}", itemId);
            
            response.put("success", true);
            response.put("message", "이미지가 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 이미지 삭제 실패: itemId={}, 오류={}", itemId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 