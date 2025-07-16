package com.wio.crm.controller;

import com.wio.crm.dto.DirectReturnBulkRequestDTO;
import com.wio.crm.dto.LogisticsDirectReturnDTO;
import com.wio.crm.dto.LogisticsDirectReturnSearchDTO;
import com.wio.crm.model.LogisticsDirectReturn;
import com.wio.crm.service.LogisticsDirectReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 물류센터 직접 입고 관리 컨트롤러
 */
@Controller
@RequestMapping("/logistics/direct-return")
@RequiredArgsConstructor
@Slf4j
public class LogisticsDirectReturnController {
    
    private final LogisticsDirectReturnService logisticsDirectReturnService;
    
    // ========== 화면 제공 ==========
    
    /**
     * 목록 화면
     */
    @GetMapping("/list")
    public String list(LogisticsDirectReturnSearchDTO search, Model model) {
        log.debug("물류 직접입고 목록 화면 - 검색조건: {}", search);
        
        try {
            // 기본 검색 조건 설정
            if (search.getStartDate() == null && search.getEndDate() == null) {
                // 기본적으로 최근 30일 조회
                search.setEndDate(LocalDate.now());
                search.setStartDate(LocalDate.now().minusDays(30));
            }
            
            // 데이터 조회
            Page<LogisticsDirectReturnDTO> directReturns = logisticsDirectReturnService.findAll(search);
            
            // 통계 정보 조회
            Map<String, Object> mappingStats = logisticsDirectReturnService.getMappingStatistics();
            Map<String, Object> statusStats = logisticsDirectReturnService.getStatusStatistics();
            
            // 모델에 데이터 추가 - HTML에서 기대하는 변수명으로 전달
            model.addAttribute("page", directReturns);  // HTML에서 page?.content 사용
            model.addAttribute("search", search);
            
            // 통계 정보 - HTML에서 기대하는 변수명으로 전달
            model.addAttribute("totalCount", mappingStats.get("TOTAL_COUNT"));
            model.addAttribute("matchedCount", mappingStats.get("MATCHED_COUNT"));
            model.addAttribute("pendingCount", mappingStats.get("PENDING_COUNT"));
            model.addAttribute("unmatchedCount", mappingStats.get("UNMATCHED_COUNT"));
            model.addAttribute("receivedCount", statusStats.get("RECEIVED_COUNT"));
            model.addAttribute("processedCount", statusStats.get("PROCESSED_COUNT"));
            
            // 오늘 입고 건수 계산
            Map<String, Object> todayStats = logisticsDirectReturnService.getTodayStatistics();
            model.addAttribute("todayCount", todayStats.get("TODAY_COUNT"));
            
            log.debug("물류 직접입고 목록 조회 완료 - 총 {}건", directReturns.getTotalElements());
            
        } catch (Exception e) {
            log.error("물류 직접입고 목록 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "logistics/direct-return-list";
    }
    
    // ========== API 엔드포인트 ==========
    
    /**
     * 목록 조회 API
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getList(LogisticsDirectReturnSearchDTO search) {
        log.debug("물류 직접입고 목록 API 조회 - 검색조건: {}", search);
        
        try {
            Page<LogisticsDirectReturnDTO> directReturns = logisticsDirectReturnService.findAll(search);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", directReturns.getContent());
            response.put("page", directReturns.getNumber());
            response.put("totalPages", directReturns.getTotalPages());
            response.put("totalElements", directReturns.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("물류 직접입고 목록 API 조회 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 단건 조회 API
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id) {
        log.debug("물류 직접입고 상세 조회 - ID: {}", id);
        
        try {
            LogisticsDirectReturnDTO directReturn = logisticsDirectReturnService.findById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", directReturn);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("물류 직접입고 상세 조회 중 오류 발생 - ID: {}", id, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 등록 API
     */
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> save(@RequestBody LogisticsDirectReturn logisticsDirectReturn) {
        log.debug("물류 직접입고 등록 - 데이터: {}", logisticsDirectReturn);
        
        try {
            String currentUser = getCurrentUser();
            LogisticsDirectReturn saved = logisticsDirectReturnService.save(logisticsDirectReturn, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "등록이 완료되었습니다.");
            response.put("data", saved);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("물류 직접입고 등록 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 수정 API
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, 
                                                     @RequestBody LogisticsDirectReturn logisticsDirectReturn) {
        log.debug("물류 직접입고 수정 - ID: {}, 데이터: {}", id, logisticsDirectReturn);
        
        try {
            logisticsDirectReturn.setId(id);
            String currentUser = getCurrentUser();
            LogisticsDirectReturn updated = logisticsDirectReturnService.update(logisticsDirectReturn, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "수정이 완료되었습니다.");
            response.put("data", updated);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("물류 직접입고 수정 중 오류 발생 - ID: {}", id, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 삭제 API
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        log.debug("물류 직접입고 삭제 - ID: {}", id);
        
        try {
            String currentUser = getCurrentUser();
            logisticsDirectReturnService.delete(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "삭제가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("물류 직접입고 삭제 중 오류 발생 - ID: {}", id, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== 매핑 관련 API ==========
    
    /**
     * 운송장번호로 교환반품 데이터 찾기 API
     */
    @GetMapping("/api/find-return-items")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> findReturnItems(@RequestParam String trackingNumber) {
        log.debug("운송장번호로 교환반품 데이터 조회 - 운송장번호: {}", trackingNumber);
        
        try {
            List<Map<String, Object>> returnItems = 
                logisticsDirectReturnService.findReturnItemsByTrackingNumber(trackingNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", returnItems);
            response.put("count", returnItems.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("운송장번호로 교환반품 데이터 조회 중 오류 발생 - 운송장번호: {}", trackingNumber, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 매핑 처리 API
     */
    @PostMapping("/api/{id}/mapping")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMapping(@PathVariable Long id, 
                                                            @RequestParam Long matchedReturnId) {
        log.debug("매핑 처리 - 직접입고 ID: {}, 교환반품 ID: {}", id, matchedReturnId);
        
        try {
            String currentUser = getCurrentUser();
            logisticsDirectReturnService.updateMapping(id, matchedReturnId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "매핑이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("매핑 처리 중 오류 발생 - 직접입고 ID: {}, 교환반품 ID: {}", id, matchedReturnId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 매핑 해제 API
     */
    @DeleteMapping("/api/{id}/mapping")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearMapping(@PathVariable Long id) {
        log.debug("매핑 해제 - 직접입고 ID: {}", id);
        
        try {
            String currentUser = getCurrentUser();
            logisticsDirectReturnService.clearMapping(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "매핑이 해제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("매핑 해제 중 오류 발생 - 직접입고 ID: {}", id, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 자동 매핑 API
     */
    @PostMapping("/api/auto-mapping")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> autoMapping() {
        log.debug("자동 매핑 시작");
        
        try {
            String currentUser = getCurrentUser();
            int mappedCount = logisticsDirectReturnService.autoMapping(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", mappedCount + "건의 데이터가 자동 매핑되었습니다.");
            response.put("mappedCount", mappedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("자동 매핑 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== 일괄 처리 API ==========
    
    /**
     * 일괄 삭제 API
     */
    @DeleteMapping("/api/batch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBatch(@RequestBody List<Long> ids) {
        log.debug("일괄 삭제 - 대상 개수: {}", ids.size());
        
        try {
            String currentUser = getCurrentUser();
            logisticsDirectReturnService.deleteBatch(ids, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", ids.size() + "건의 데이터가 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일괄 삭제 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 일괄 상태 변경 API
     */
    @PutMapping("/api/batch/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatusBatch(@RequestBody Map<String, Object> request) {
        log.debug("일괄 상태 변경 - 요청: {}", request);
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            String processingStatus = (String) request.get("processingStatus");
            
            String currentUser = getCurrentUser();
            logisticsDirectReturnService.updateStatusBatch(ids, processingStatus, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", ids.size() + "건의 상태가 변경되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일괄 상태 변경 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== 엑셀 다운로드 ==========
    
    /**
     * 엑셀 다운로드
     */
    @PostMapping("/downloadExcel")
    public void downloadExcel(LogisticsDirectReturnSearchDTO search, HttpServletResponse response) {
        log.debug("엑셀 다운로드 - 검색조건: {}", search);
        
        try {
            List<LogisticsDirectReturnDTO> data = logisticsDirectReturnService.findAllForExcel(search);
            
            // 엑셀 파일 생성
            Workbook workbook = createExcelWorkbook(data);
            
            // 파일명 설정
            String fileName = "물류직접입고_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            
            // 응답 헤더 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            // 엑셀 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();
            
            log.info("엑셀 다운로드 완료 - 파일명: {}, 데이터 건수: {}", fileName, data.size());
            
        } catch (Exception e) {
            log.error("엑셀 다운로드 중 오류 발생", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "엑셀 다운로드 중 오류가 발생했습니다.");
            } catch (IOException ioException) {
                log.error("응답 오류 처리 중 추가 오류 발생", ioException);
            }
        }
    }
    
    // ========== 유틸리티 메서드 ==========
    
    /**
     * 현재 사용자 정보 조회
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
    
    /**
     * 엑셀 워크북 생성
     */
    private Workbook createExcelWorkbook(List<LogisticsDirectReturnDTO> data) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("물류직접입고");
        
        // 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "ID", "입고일자", "사이트명", "고객명", "연락처", "제품코드", 
            "색상", "사이즈", "수량", "운송장번호", "택배사", "특이사항",
            "처리상태", "매핑상태", "매핑된교환반품ID", "등록일시"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 데이터 행 생성
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < data.size(); i++) {
            LogisticsDirectReturnDTO item = data.get(i);
            Row dataRow = sheet.createRow(i + 1);
            
            dataRow.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            dataRow.createCell(1).setCellValue(item.getReceivedDate() != null ? item.getReceivedDate().format(dateFormatter) : "");
            dataRow.createCell(2).setCellValue(item.getSiteName() != null ? item.getSiteName() : "");
            dataRow.createCell(3).setCellValue(item.getCustomerName() != null ? item.getCustomerName() : "");
            dataRow.createCell(4).setCellValue(item.getCustomerPhone() != null ? item.getCustomerPhone() : "");
            dataRow.createCell(5).setCellValue(item.getProductCode() != null ? item.getProductCode() : "");
            dataRow.createCell(6).setCellValue(item.getProductColor() != null ? item.getProductColor() : "");
            dataRow.createCell(7).setCellValue(item.getProductSize() != null ? item.getProductSize() : "");
            dataRow.createCell(8).setCellValue(item.getQuantity() != null ? item.getQuantity() : 0);
            dataRow.createCell(9).setCellValue(item.getTrackingNumber() != null ? item.getTrackingNumber() : "");
            dataRow.createCell(10).setCellValue(item.getCourierCompany() != null ? item.getCourierCompany() : "");
            dataRow.createCell(11).setCellValue(item.getRemarks() != null ? item.getRemarks() : "");
            dataRow.createCell(12).setCellValue(item.getProcessingStatusText());
            dataRow.createCell(13).setCellValue(item.getMappingStatusText());
            dataRow.createCell(14).setCellValue(item.getMatchedReturnId() != null ? item.getMatchedReturnId().toString() : "");
            dataRow.createCell(15).setCellValue(item.getCreatedDate() != null ? item.getCreatedDate().format(dateTimeFormatter) : "");
        }
        
        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        return workbook;
    }
    
    /**
     * 여러 제품 일괄 등록 API
     */
    @PostMapping("/api/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBulkItems(@RequestBody DirectReturnBulkRequestDTO request) {
        log.debug("물류 직접입고 일괄 등록 요청 - 고객: {}, 제품 수: {}", 
                  request.getCustomerName(), request.getProducts().size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 요청 데이터 로깅
            log.debug("일괄 등록 요청 상세: {}", request);
            
            // 일괄 저장 실행
            logisticsDirectReturnService.saveBulkItems(request);
            
            response.put("success", true);
            response.put("message", "총 " + request.getProducts().size() + "개 제품이 성공적으로 등록되었습니다.");
            response.put("savedCount", request.getProducts().size());
            
            log.info("물류 직접입고 일괄 등록 성공 - 고객: {}, 제품 수: {}", 
                     request.getCustomerName(), request.getProducts().size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("물류 직접입고 일괄 등록 유효성 검증 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("물류 직접입고 일괄 등록 실패", e);
            response.put("success", false);
            response.put("message", "등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 