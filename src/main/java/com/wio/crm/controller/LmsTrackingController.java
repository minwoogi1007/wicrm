package com.wio.crm.controller;

import com.wio.crm.dto.CallLogDto;
import com.wio.crm.dto.LmsLogDto;
import com.wio.crm.dto.LmsTrackingSearchDto;
import com.wio.crm.dto.LmsTrackingStatsDto;
import com.wio.crm.service.LmsTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LMS 문자 발송 추적 시스템 컨트롤러
 * 
 * 기능:
 * - 문자 발송 내역 조회
 * - 발송 후 통화 추적 관리
 * - 통계 대시보드
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Controller
@RequestMapping("/lms-tracking")
@RequiredArgsConstructor
@Slf4j
public class LmsTrackingController {

    private final LmsTrackingService lmsTrackingService;

    /**
     * LMS 추적 메인 화면
     */
    @GetMapping("/list")
    public String lmsTrackingList(Model model) {
        log.info("LMS 추적 화면 요청");
        
        // 기본 날짜 설정 (오늘 기준)
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        model.addAttribute("startDate", today.format(formatter));
        model.addAttribute("endDate", today.format(formatter));
        
        return "lms-tracking/list";
    }

    /**
     * LMS 발송 내역 조회 API
     */
    @GetMapping("/api/lms-list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLmsList(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        log.info("LMS 발송 내역 조회 - 시작일: {}, 종료일: {}, 전화번호: {}, 상태: {}, 페이지: {}", 
                startDate, endDate, phone, status, page);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 검색 조건 생성
            LmsTrackingSearchDto searchDto = LmsTrackingSearchDto.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .clid(phone)  // phone 파라미터를 clid 필드로 매핑
                    .status(status)
                    .page(page)
                    .size(size)
                    .custCode("P000000179")  // 고객 코드 명시적 설정
                    .build();
            
            // 기본값 설정 (페이징, 정렬 등)
            searchDto.setDefaults();
            
            // 실제 데이터 조회
            Page<LmsLogDto> lmsPage = lmsTrackingService.getLmsList(searchDto);
            
            response.put("success", true);
            response.put("data", lmsPage.getContent());
            response.put("totalCount", lmsPage.getTotalElements());
            response.put("currentPage", lmsPage.getNumber() + 1);
            response.put("totalPages", lmsPage.getTotalPages());
            
        } catch (Exception e) {
            log.error("LMS 발송 내역 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "데이터 조회 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * LMS 통계 조회 API
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "status", required = false) String status) {
        
        log.info("LMS 통계 조회 - 시작일: {}, 종료일: {}, 전화번호: {}, 상태: {}", 
                startDate, endDate, phone, status);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 검색 조건 생성
            LmsTrackingSearchDto searchDto = LmsTrackingSearchDto.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .clid(phone)  // phone 파라미터를 clid 필드로 매핑
                    .status(status)
                    .custCode("P000000179")  // 고객 코드 명시적 설정
                    .build();
            
            // 기본값 설정 (페이징, 정렬 등)
            searchDto.setDefaults();
            
            // 실제 통계 조회
            LmsTrackingStatsDto stats = lmsTrackingService.getStatistics(searchDto);
            
            response.put("success", true);
            response.put("data", stats);
            
        } catch (Exception e) {
            log.error("LMS 통계 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "통계 조회 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 LMS 발송 건의 후속 통화 내역 조회 API
     */
    @GetMapping("/api/call-history/{lmsId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCallHistory(@PathVariable Long lmsId) {
        
        log.info("LMS 후속 통화 내역 조회 - LMS ID: {}", lmsId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 실제 통화 내역 조회
            List<CallLogDto> callHistory = lmsTrackingService.getCallHistory(lmsId);
            
            response.put("success", true);
            response.put("data", callHistory);
            
        } catch (Exception e) {
            log.error("통화 내역 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "통화 내역 조회 중 오류가 발생했습니다.");
        }
        
        return ResponseEntity.ok(response);
    }


} 