package com.wio.crm.controller;

import com.wio.crm.dto.ExchangeStatsRequestDto;
import com.wio.crm.dto.ExchangeStatsResponseDto;
import com.wio.crm.service.ExchangeStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * 🚀 교환반품 통계 전용 컨트롤러 (신규 설계)
 * 
 * 목적: 기존의 복잡한 StatController와 ExchangeController의 중복 기능을 통합
 * 기능: 교환반품 통계 화면 + 단일 통합 API
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/exchange/stats")
public class ExchangeStatsController {

    private final ExchangeStatsService exchangeStatsService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 📊 교환반품 통계 화면
     */
    @GetMapping
    public String exchangeStatsPage(Model model) {
        log.info("🎯 교환반품 통계 화면 요청");
        
        try {
            // 기본 날짜 설정 (이번달 1일 ~ 말일)
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
            
            // 모델에 기본값 설정
            model.addAttribute("defaultDateFilterType", "CS_RECEIVED");
            model.addAttribute("defaultCsStartDate", firstDayOfMonth.format(DATE_FORMATTER));
            model.addAttribute("defaultCsEndDate", lastDayOfMonth.format(DATE_FORMATTER));
            
            log.info("✅ 교환반품 통계 화면 로드 완료 - 기본 기간: {} ~ {}", 
                    firstDayOfMonth.format(DATE_FORMATTER), lastDayOfMonth.format(DATE_FORMATTER));
            
            return "statistics/statExchange";
            
        } catch (Exception e) {
            log.error("❌ 교환반품 통계 화면 로드 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "화면 로드 중 오류가 발생했습니다: " + e.getMessage());
            return "error/error";
        }
    }

    /**
     * 🔍 교환반품 통계 데이터 조회 API (통합)
     * 
     * 모든 통계 데이터를 한번에 조회하는 단일 API
     * - 핵심 지표 (총 건수, 완료율, 금액 등)
     * - 차트 데이터 (트렌드, 분포)
     * - 테이블 데이터 (사유별, 사이트별)
     */
    @PostMapping("/search")
    @ResponseBody
    public ResponseEntity<ExchangeStatsResponseDto> searchExchangeStats(
            @RequestParam("dateFilterType") String dateFilterType,
            @RequestParam(value = "csStartDate", required = false) String csStartDate,
            @RequestParam(value = "csEndDate", required = false) String csEndDate,
            @RequestParam(value = "orderStartDate", required = false) String orderStartDate,
            @RequestParam(value = "orderEndDate", required = false) String orderEndDate,
            @RequestParam(value = "returnType", required = false) String returnType,
            @RequestParam(value = "completionStatus", required = false) String completionStatus,
            @RequestParam(value = "siteName", required = false) String siteName,
            @RequestParam(value = "periodType", required = false, defaultValue = "CUSTOM") String periodType) {
        
        log.info("🔍 교환반품 통계 조회 요청:");
        log.info("   📅 날짜 기준: {}", dateFilterType);
        log.info("   📋 조건 - 유형: {}, 상태: {}, 사이트: {}", returnType, completionStatus, siteName);
        
        try {
            // 1. 요청 데이터 검증
            String validationError = validateRequest(dateFilterType, csStartDate, csEndDate, orderStartDate, orderEndDate);
            if (validationError != null) {
                log.error("❌ 요청 데이터 검증 실패: {}", validationError);
                return ResponseEntity.badRequest().body(null);
            }
            
            // 2. DTO 생성
            ExchangeStatsRequestDto requestDto = ExchangeStatsRequestDto.builder()
                    .dateFilterType(dateFilterType)
                    .csStartDate(csStartDate)
                    .csEndDate(csEndDate)
                    .orderStartDate(orderStartDate)
                    .orderEndDate(orderEndDate)
                    .returnType(returnType)
                    .completionStatus(completionStatus)
                    .siteName(siteName)
                    .periodType(periodType)
                    .build();
            
            log.info("✅ 요청 DTO 생성 완료: {}", requestDto);
            
            // 3. 통계 데이터 조회 (단일 서비스 호출)
            ExchangeStatsResponseDto responseDto = exchangeStatsService.getExchangeStats(requestDto);
            
            log.info("✅ 교환반품 통계 조회 완료 - 총 레코드: {}", 
                    responseDto != null ? responseDto.getTotalRecords() : 0);
            
            return ResponseEntity.ok(responseDto);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ 잘못된 요청 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            log.error("❌ 교환반품 통계 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 🐛 교환반품 통계 디버깅 API
     * 
     * 개발용 디버깅 정보 제공
     * - 실행된 쿼리 조건
     * - 실제 조회된 데이터 수
     * - 각 단계별 처리 시간
     */
    @PostMapping("/debug")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugExchangeStats(
            @RequestParam("dateFilterType") String dateFilterType,
            @RequestParam(value = "csStartDate", required = false) String csStartDate,
            @RequestParam(value = "csEndDate", required = false) String csEndDate,
            @RequestParam(value = "orderStartDate", required = false) String orderStartDate,
            @RequestParam(value = "orderEndDate", required = false) String orderEndDate,
            @RequestParam(value = "returnType", required = false) String returnType,
            @RequestParam(value = "completionStatus", required = false) String completionStatus,
            @RequestParam(value = "siteName", required = false) String siteName) {
        
        log.info("🐛 교환반품 통계 디버깅 요청");
        
        try {
            // DTO 생성
            ExchangeStatsRequestDto requestDto = ExchangeStatsRequestDto.builder()
                    .dateFilterType(dateFilterType)
                    .csStartDate(csStartDate)
                    .csEndDate(csEndDate)
                    .orderStartDate(orderStartDate)
                    .orderEndDate(orderEndDate)
                    .returnType(returnType)
                    .completionStatus(completionStatus)
                    .siteName(siteName)
                    .periodType("CUSTOM")
                    .build();
            
            // 디버깅 정보 조회
            Map<String, Object> debugInfo = exchangeStatsService.getDebugInfo(requestDto);
            
            log.info("✅ 디버깅 정보 조회 완료");
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            log.error("❌ 디버깅 요청 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", true);
            errorResult.put("message", e.getMessage());
            errorResult.put("timestamp", LocalDate.now().format(DATE_FORMATTER));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * 📝 요청 데이터 검증
     */
    private String validateRequest(String dateFilterType, String csStartDate, String csEndDate, 
                                 String orderStartDate, String orderEndDate) {
        // 날짜 기준 타입 검증
        if (!"CS_RECEIVED".equals(dateFilterType) && !"ORDER_DATE".equals(dateFilterType)) {
            return "잘못된 날짜 기준 타입: " + dateFilterType;
        }
        
        // CS접수일 기준 검증
        if ("CS_RECEIVED".equals(dateFilterType)) {
            if (csStartDate == null || csStartDate.trim().isEmpty() || 
                csEndDate == null || csEndDate.trim().isEmpty()) {
                return "CS접수일 기준 선택 시 시작일과 종료일이 필수입니다";
            }
            
            try {
                LocalDate.parse(csStartDate, DATE_FORMATTER);
                LocalDate.parse(csEndDate, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                return "CS접수일 날짜 형식이 올바르지 않습니다 (YYYY-MM-DD 형식 필요)";
            }
        }
        
        // 주문일 기준 검증
        if ("ORDER_DATE".equals(dateFilterType)) {
            if (orderStartDate == null || orderStartDate.trim().isEmpty() || 
                orderEndDate == null || orderEndDate.trim().isEmpty()) {
                return "주문일 기준 선택 시 시작일과 종료일이 필수입니다";
            }
            
            try {
                LocalDate.parse(orderStartDate, DATE_FORMATTER);
                LocalDate.parse(orderEndDate, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                return "주문일 날짜 형식이 올바르지 않습니다 (YYYY-MM-DD 형식 필요)";
            }
        }
        
        return null; // 검증 통과
    }
} 