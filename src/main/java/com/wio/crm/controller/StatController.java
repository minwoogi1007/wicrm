package com.wio.crm.controller;

import com.wio.crm.dto.*;
import com.wio.crm.model.DateRange;
import com.wio.crm.model.Statics;
import com.wio.crm.service.StatisticsService;
import com.wio.crm.service.ReturnItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Controller
public class StatController {
    private static final Logger logger = LoggerFactory.getLogger(StatController.class);

    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private ReturnItemService returnItemService;
    
    // 새로 추가된 일일/주간/월간 운영현황 컨트롤러
    @GetMapping("/stat/daily")
    public String dailyOperationStat(Model model) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        model.addAttribute("today", today.format(formatter));
        return "statistics/daily_operation";
    }

    @GetMapping("/stat/weekly")
    public String weeklyOperationStat(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        model.addAttribute("startDate", startOfWeek.format(formatter));
        model.addAttribute("endDate", endOfWeek.format(formatter));
        return "statistics/weekly_operation";
    }

    @GetMapping("/stat/monthly")
    public String monthlyOperationStat(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        model.addAttribute("startDate", firstDayOfMonth.format(formatter));
        model.addAttribute("endDate", lastDayOfMonth.format(formatter));
        return "statistics/monthly_operation";
    }
    
    @GetMapping("/statCons")
    public String statCons() {
        return "statistics/statCons";
    }

    @PostMapping("/statCons/searchCons")  // 리스트
    public String getConsultationStats(Model model,   @RequestParam("start_date") String startDate,
                                       @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }


        List<Statics> stats = statisticsService.getStatisticsCons(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statCons"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statCons/searchCons")//그래프
    public ResponseEntity<Map<String, Object>> getStatisticsConsG(@RequestParam("start_date") String startDate,
                                                                    @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }


        Map<String, Object> data = statisticsService.getStatisticsConsG(startDate, endDate);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/statResult")
    public String statResult() {
        return "statistics/statResult";
    }

    @PostMapping("/statResult/searchResult")  // 리스트
    public String getConsultationResult(Model model,   @RequestParam("start_date") String startDate,
                                       @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }

        List<Statics> stats = statisticsService.getConsultationResult(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statResult"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statResult/searchResult")//그래프
    public ResponseEntity<Map<String, Object>> getConsultationResultG(@RequestParam("start_date") String startDate,
                                                                  @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }

        Map<String, Object> data = statisticsService.getConsultationResultG(startDate, endDate);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/statTime")
    public String statTime() {
        return "statistics/statTime";
    }

    @PostMapping("/statTime/searchTime")  // 리스트
    public String getConsultationTime(Model model,   @RequestParam("start_date") String startDate,
                                        @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }


        List<Statics> stats = statisticsService.getConsultationTime(startDate, endDate);
        model.addAttribute("hidden_start_date", startDate);
        model.addAttribute("hidden_end_date", endDate);
        model.addAttribute("stats", stats);
        return "statistics/statTime"; // Ensure this points to the correct Thymeleaf template
    }
    @GetMapping("/api/statTime/searchTime")//그래프
    public ResponseEntity<Map<String, Object>> getConsultationTimeG(@RequestParam("start_date") String startDate,
                                                                      @RequestParam("end_date") String endDate) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (startDate == null || startDate.isEmpty()) {
            startDate = today.format(formatter);
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = today.format(formatter);
        }



        Map<String, Object> data = statisticsService.getConsultationTimeG(startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statCall")
    public String statCall() {
        return "statistics/statCall";
    }

    @GetMapping("/stat/exchange")
    public String statExchange() {
        return "statistics/statExchange";
    }

    @PostMapping("/stat/daily/search")
    @ResponseBody
    public ResponseEntity<DailyOperationStatsResponseDto> searchDailyOperationStats(@RequestParam("date") String date) {
        logger.info("Received request for daily operation stats with date: {}", date);
        try {
            // 날짜 형식 검증
            if (date == null || date.isEmpty()) {
                logger.error("날짜가 비어있습니다.");
                return ResponseEntity.badRequest().body(null);
            }

            // 숫자로만 구성되어 있는지 확인 (YYYYMMDD 형식)
            if (!date.matches("^\\d{8}$")) {
                logger.error("잘못된 날짜 형식: {}", date);
                return ResponseEntity.badRequest().body(null);
            }

            // 유효한 날짜인지 추가 검증 (예: 20250231 같은 경우)
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate.parse(date, formatter); // 파싱만 시도하여 유효성 검사
            } catch (DateTimeParseException e) {
                logger.error("유효하지 않은 날짜 값: {}", date, e);
                return ResponseEntity.badRequest().body(null);
            }
            
            DailyOperationStatsResponseDto statsDto = statisticsService.getDailyOperationStatistics(date);
            return ResponseEntity.ok(statsDto);
        } catch (IllegalStateException e) {
            logger.error("Error fetching daily operation stats due to illegal state (e.g., auth issue) for date {}: {}", date, e.getMessage());
            // IllegalStateException은 getCurrentCustCode에서 사용자 인증 문제 시 발생 가능
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 또는 적절한 오류 DTO
        } catch (DateTimeParseException e) {
            logger.error("Error fetching daily operation stats for date {}: {}", date, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            logger.error("Error fetching daily operation stats for date {}: {}", date, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 또는 적절한 오류 DTO
        }
    }

    @PostMapping("/stat/weekly/search")
    @ResponseBody
    public ResponseEntity<WeeklyOperationStatsResponseDto> searchWeeklyOperationStats(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        logger.info("Received request for weekly operation stats with startDate: {} and endDate: {}", startDate, endDate);
        try {
            // 날짜 형식 변환: "2024-05-26" -> "20240526"
            String dbFormatStartDate = startDate.replaceAll("-", "");
            String dbFormatEndDate = endDate.replaceAll("-", "");
            
            WeeklyOperationStatsResponseDto statsDto = statisticsService.getWeeklyOperationStatistics(dbFormatStartDate, dbFormatEndDate);
            return ResponseEntity.ok(statsDto);
        } catch (IllegalStateException e) {
            logger.error("Error fetching weekly operation stats due to illegal state (e.g., auth issue) for date range {} - {}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (RuntimeException e) {
            logger.error("Error fetching weekly operation stats for date range {} - {}: {}", startDate, endDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/stat/monthly/search")
    @ResponseBody
    public ResponseEntity<MonthlyOperationStatsResponseDto> searchMonthlyOperationStats(@RequestParam("yearMonth") String yearMonth) {
        logger.info("Received request for monthly operation stats with yearMonth: {}", yearMonth);
        try {
            // 월간 운영현황 데이터 조회
            // yearMonth (예: "2025-01")를 해당 월의 시작일과 끝일로 변환
            String startDate = yearMonth.replaceAll("-", "") + "01"; // 예: "20250101"
            
            // 해당 월의 마지막 날 계산
            YearMonth ym = YearMonth.parse(yearMonth);
            String endDate = yearMonth.replaceAll("-", "") + String.format("%02d", ym.lengthOfMonth()); // 예: "20250131"
            
            MonthlyOperationStatsResponseDto monthlyData = statisticsService.getMonthlyOperationStatistics(startDate, endDate);
            
            // 디버깅: 주차별 통계 데이터 확인
            if (monthlyData != null) {
                if (monthlyData.getWeeklyStatsList() != null) {
                    logger.info("주차별 통계 데이터 수: {}", monthlyData.getWeeklyStatsList().size());
                    for (int i = 0; i < monthlyData.getWeeklyStatsList().size(); i++) {
                        logger.info("주차 데이터 {}: {}", i+1, monthlyData.getWeeklyStatsList().get(i));
                    }
                } else {
                    logger.warn("주차별 통계 데이터가 null입니다.");
                }
            } else {
                logger.warn("월간 통계 응답 DTO가 null입니다.");
            }
            
            return ResponseEntity.ok(monthlyData);
        } catch (IllegalStateException e) {
            logger.error("Error fetching monthly operation stats due to illegal state (e.g., auth issue) for yearMonth {}: {}", yearMonth, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (RuntimeException e) {
            logger.error("Error fetching monthly operation stats for yearMonth {}: {}", yearMonth, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 📥 일일 운영현황 이미지 포함 엑셀 다운로드 API (기존 방식)
     */
    @PostMapping("/stat/daily/excel-with-images")
    public void downloadDailyOperationExcelWithImages(
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) throws IOException {
        
        try {
            logger.info("📥 일일 운영현황 이미지 포함 엑셀 다운로드 시작");
            
            // 날짜 추출
            String date = (String) params.get("date");
            logger.info("📅 조회 날짜: {}", date);
            
            // 받은 파라미터 전체 정보 로깅
            logger.info("📦 받은 파라미터 키: {}", params.keySet());
            
            // 이미지 데이터 추출 (차트 이미지들만, 테이블 이미지는 상세데이터 시트에서 제공)
            String counselingTypeChartImage = extractImageData(params.get("counselingTypeChartImage"));
            String hourlyCallsChartImage = extractImageData(params.get("hourlyCallsChartImage"));
            
            // 이미지 추출 결과 로깅
            logger.info("🖼️ 이미지 추출 결과: counselingTypeChart={}, hourlyCallsChart={}",
                       counselingTypeChartImage != null ? "✅" : "❌",
                       hourlyCallsChartImage != null ? "✅" : "❌");
            
            // 일일 운영현황 데이터 조회
            DailyOperationStatsResponseDto dailyData = statisticsService.getDailyOperationStatistics(date);
            logger.info("📊 일일 운영현황 데이터 조회 완료");
            
            // 이미지 포함 엑셀 파일 생성
            XSSFWorkbook workbook = createDailyOperationExcelFileWithImages(dailyData, date,
                    counselingTypeChartImage, hourlyCallsChartImage);
            
            // 파일명 설정
            String fileName = "일일운영현황(이미지포함)_" + date + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            // 응답 헤더 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            workbook.write(response.getOutputStream());
            workbook.close();
            
            logger.info("📥 일일 운영현황 이미지 포함 엑셀 다운로드 완료: {}", fileName);
            
        } catch (Exception e) {
            logger.error("❌ 일일 운영현황 이미지 포함 엑셀 다운로드 실패: {}", e.getMessage(), e);
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📊 일일 운영현황 이미지 포함 엑셀 파일 생성
     */
    private XSSFWorkbook createDailyOperationExcelFileWithImages(DailyOperationStatsResponseDto dailyData, 
                                                                String date,
                                                                String counselingTypeChartImage,
                                                                String hourlyCallsChartImage) {
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
        
        // 시트 1: 일일 대시보드 (차트 이미지 포함)
        Sheet dashboardSheet = workbook.createSheet("📊 일일 운영현황 대시보드");
        createDailyDashboardSheetWithImages(dashboardSheet, workbook, date,
                                          counselingTypeChartImage, hourlyCallsChartImage, headerStyle, dataStyle);
        
        // 시트 2: 일일 상세 데이터 (테이블 데이터)
        Sheet dataSheet = workbook.createSheet("📋 일일 상세 데이터");
        createDailyDataSheet(dataSheet, dailyData, headerStyle, dataStyle);
        
        return workbook;
    }

    /**
     * 📊 일일 운영현황 차트 이미지 포함 대시보드 시트 생성
     */
    private void createDailyDashboardSheetWithImages(Sheet sheet, XSSFWorkbook workbook, String date,
                                                    String counselingTypeChartImage, String hourlyCallsChartImage,
                                                    CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 일일 운영현황 대시보드 (" + date + ")");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        try {
            // 이미지들을 순차적으로 추가
            if (counselingTypeChartImage != null && !counselingTypeChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, counselingTypeChartImage, "상담유형 분포 차트", rowIndex, headerStyle);
            }
            
            if (hourlyCallsChartImage != null && !hourlyCallsChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, hourlyCallsChartImage, "시간대별 통화량 차트", rowIndex, headerStyle);
            }
            
        } catch (Exception e) {
            logger.error("❌ 일일 운영현황 차트 이미지 추가 실패: {}", e.getMessage(), e);
        }
        
        // 컬럼 너비 설정
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 15000);
        sheet.setColumnWidth(2, 15000);
    }

    /**
     * 📊 일일 운영현황 상세 데이터 시트 생성
     */
    private void createDailyDataSheet(Sheet sheet, DailyOperationStatsResponseDto dailyData, 
                                     CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📋 일일 운영현황 상세 데이터");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        // 1. 통계 현황 데이터
        if (dailyData.getSummaryStats() != null) {
            rowIndex = addDailySummaryStatsToSheet(sheet, dailyData.getSummaryStats(), rowIndex, headerStyle, dataStyle);
        }
        
        rowIndex++; // 빈 행
        
        // 2. 통화 시간 현황 데이터
        if (dailyData.getCallTimeStats() != null) {
            rowIndex = addDailyCallTimeStatsToSheet(sheet, dailyData.getCallTimeStats(), rowIndex, headerStyle, dataStyle);
        }
        
        rowIndex++; // 빈 행
        
        // 3. 상담유형 현황 데이터
        if (dailyData.getCounselingTypeStats() != null && !dailyData.getCounselingTypeStats().isEmpty()) {
            rowIndex = addDailyCounselingTypeStatsToSheet(sheet, dailyData.getCounselingTypeStats(), rowIndex, headerStyle, dataStyle);
        }
        
        // 컬럼 너비 설정
        for (int i = 0; i < 15; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 📊 일일 통계 현황 데이터를 시트에 추가
     */
    private int addDailySummaryStatsToSheet(Sheet sheet, DailySummaryStatsDto summaryStats, 
                                           int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = startRow;
        
        // 섹션 제목
        Row sectionTitleRow = sheet.createRow(rowIndex++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("📊 통계 현황");
        sectionTitleCell.setCellStyle(headerStyle);
        
        // 헤더
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"일자", "총콜", "중복제거호", "응대호", "포기호", "부재호(BUSY)", 
                           "총상담완료호", "수신호", "발신호", "응대율", "완료율", "수신비율", "발신비율"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 데이터
        Row dataRow = sheet.createRow(rowIndex++);
        dataRow.createCell(0).setCellValue(summaryStats.getStatDate() != null ? summaryStats.getStatDate() : "");
        dataRow.createCell(1).setCellValue(summaryStats.getTotalCalls());
        dataRow.createCell(2).setCellValue(summaryStats.getUniqueInboundCalls());
        dataRow.createCell(3).setCellValue(summaryStats.getAnsweredCalls());
        dataRow.createCell(4).setCellValue(summaryStats.getAbandonedCalls());
        dataRow.createCell(5).setCellValue(summaryStats.getBusyCalls());
        dataRow.createCell(6).setCellValue(summaryStats.getCompletedCounselingCalls());
        dataRow.createCell(7).setCellValue(summaryStats.getInboundCalls());
        dataRow.createCell(8).setCellValue(summaryStats.getOutboundCalls());
        dataRow.createCell(9).setCellValue(summaryStats.getAnswerRate() != null ? summaryStats.getAnswerRate().doubleValue() + "%" : "0.0%");
        dataRow.createCell(10).setCellValue(summaryStats.getCompletionRate() != null ? summaryStats.getCompletionRate().doubleValue() + "%" : "0.0%");
        dataRow.createCell(11).setCellValue(summaryStats.getInboundRate() != null ? summaryStats.getInboundRate().doubleValue() + "%" : "0.0%");
        dataRow.createCell(12).setCellValue(summaryStats.getOutboundRate() != null ? summaryStats.getOutboundRate().doubleValue() + "%" : "0.0%");
        
        // 데이터 스타일 적용
        for (int i = 0; i < headers.length; i++) {
            dataRow.getCell(i).setCellStyle(dataStyle);
        }
        
        return rowIndex;
    }

    /**
     * 📊 일일 통화 시간 현황 데이터를 시트에 추가
     */
    private int addDailyCallTimeStatsToSheet(Sheet sheet, DailyCallTimeStatsDto callTimeStats, 
                                            int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = startRow;
        
        // 섹션 제목
        Row sectionTitleRow = sheet.createRow(rowIndex++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("📊 통화 시간 현황");
        sectionTitleCell.setCellStyle(headerStyle);
        
        // 헤더
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"구분", "총통화시간(초)", "평균통화시간(초)"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 데이터
        Row dataRow = sheet.createRow(rowIndex++);
        dataRow.createCell(0).setCellValue("통화 시간 현황");
        dataRow.createCell(1).setCellValue(callTimeStats.getTotalCallDurationSeconds());
        dataRow.createCell(2).setCellValue(callTimeStats.getAverageCallDurationSeconds() != null ? callTimeStats.getAverageCallDurationSeconds() : 0.0);
        
        // 데이터 스타일 적용
        for (int i = 0; i < headers.length; i++) {
            dataRow.getCell(i).setCellStyle(dataStyle);
        }
        
        return rowIndex;
    }

    /**
     * 📊 일일 상담유형 현황 데이터를 시트에 추가
     */
    private int addDailyCounselingTypeStatsToSheet(Sheet sheet, List<DailyCounselingTypeStatsDto> counselingTypeStats, 
                                                  int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = startRow;
        
        // 섹션 제목
        Row sectionTitleRow = sheet.createRow(rowIndex++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("📊 상담유형 현황");
        sectionTitleCell.setCellStyle(headerStyle);
        
        // 헤더
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"상담유형", "상담수", "비율"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 데이터
        for (DailyCounselingTypeStatsDto stats : counselingTypeStats) {
            Row dataRow = sheet.createRow(rowIndex++);
            
            dataRow.createCell(0).setCellValue(stats.getCounselingTypeName() != null ? stats.getCounselingTypeName() : "");
            dataRow.createCell(1).setCellValue(stats.getCount());
            dataRow.createCell(2).setCellValue(stats.getPercentage() != null ? String.format("%.1f%%", stats.getPercentage()) : "0.0%");
            
            // 데이터 스타일 적용
            for (int i = 0; i < headers.length; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        return rowIndex;
    }

    /**
     * 📥 일일 운영현황 테이블 데이터 + 차트 이미지 엑셀 다운로드 API (신규 방식)
     */
    @PostMapping("/stat/daily/excel-with-data")
    public void downloadDailyOperationExcelWithData(
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) throws IOException {
        
        try {
            logger.info("📥 일일 운영현황 테이블 데이터 + 차트 이미지 엑셀 다운로드 시작");
            
            // 날짜 추출
            String date = (String) params.get("date");
            logger.info("📅 조회 날짜: {}", date);
            
            // 받은 파라미터 전체 정보 로깅
            logger.info("📦 받은 파라미터 키: {}", params.keySet());
            
            // 차트 이미지 데이터 추출
            String counselingTypeChartImage = extractImageData(params.get("counselingTypeChartImage"));
            String hourlyCallsChartImage = extractImageData(params.get("hourlyCallsChartImage"));
            
            // 테이블 데이터 추출
            @SuppressWarnings("unchecked")
            List<Map<String, String>> summaryTableData = (List<Map<String, String>>) params.get("summaryTableData");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> comparisonTableData = (List<Map<String, String>>) params.get("comparisonTableData");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> counselingTableData = (List<Map<String, String>>) params.get("counselingTableData");
            
            // 이미지 추출 결과 로깅
            logger.info("🖼️ 차트 이미지 추출 결과: counselingTypeChart={}, hourlyCallsChart={}",
                       counselingTypeChartImage != null ? "✅" : "❌",
                       hourlyCallsChartImage != null ? "✅" : "❌");
            
            // 테이블 데이터 추출 결과 로깅
            logger.info("📊 테이블 데이터 추출 결과: summaryTable={}, comparisonTable={}, counselingTable={}",
                       summaryTableData != null ? summaryTableData.size() + "개 행" : "❌",
                       comparisonTableData != null ? comparisonTableData.size() + "개 행" : "❌",
                       counselingTableData != null ? counselingTableData.size() + "개 행" : "❌");
            
            // 첫 번째 항목 샘플 로깅
            if (summaryTableData != null && !summaryTableData.isEmpty()) {
                logger.info("📊 통계 현황 테이블 샘플: {}", summaryTableData.get(0));
            }
            if (comparisonTableData != null && !comparisonTableData.isEmpty()) {
                logger.info("📊 통화 시간 현황 테이블 샘플: {}", comparisonTableData.get(0));
            }
            if (counselingTableData != null && !counselingTableData.isEmpty()) {
                logger.info("📊 상담유형 현황 테이블 샘플: {}", counselingTableData.get(0));
            } else {
                logger.warn("⚠️ 상담유형 현황 테이블 데이터가 비어있습니다!");
            }
            
            // 일일 운영현황 데이터 조회
            DailyOperationStatsResponseDto dailyData = statisticsService.getDailyOperationStatistics(date);
            logger.info("📊 일일 운영현황 데이터 조회 완료");
            
            // 테이블 데이터 + 차트 이미지 엑셀 파일 생성
            XSSFWorkbook workbook = createDailyOperationExcelFileWithData(dailyData, date,
                    summaryTableData, comparisonTableData, counselingTableData,
                    counselingTypeChartImage, hourlyCallsChartImage);
            
            // 파일명 설정
            String fileName = "일일운영현황_" + date + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            // 응답 헤더 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            workbook.write(response.getOutputStream());
            workbook.close();
            
            logger.info("📥 일일 운영현황 테이블 데이터 + 차트 이미지 엑셀 다운로드 완료: {}", fileName);
            
        } catch (Exception e) {
            logger.error("❌ 일일 운영현황 테이블 데이터 + 차트 이미지 엑셀 다운로드 실패: {}", e.getMessage(), e);
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📊 일일 운영현황 테이블 데이터 + 차트 이미지 엑셀 파일 생성 (신규 방식)
     */
    private XSSFWorkbook createDailyOperationExcelFileWithData(DailyOperationStatsResponseDto dailyData, 
                                                              String date,
                                                              List<Map<String, String>> summaryTableData,
                                                              List<Map<String, String>> comparisonTableData,
                                                              List<Map<String, String>> counselingTableData,
                                                              String counselingTypeChartImage,
                                                              String hourlyCallsChartImage) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // 스타일 설정
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 시트 생성
        Sheet sheet = workbook.createSheet("📊 일일 운영현황");
        createDailyDataSheetWithTables(sheet, workbook, date, summaryTableData, comparisonTableData, 
                                     counselingTableData, counselingTypeChartImage, hourlyCallsChartImage, 
                                     headerStyle, dataStyle);
        
        return workbook;
    }

    /**
     * 📊 일일 운영현황 테이블 데이터 + 차트 이미지 시트 생성 (신규 방식)
     */
    private void createDailyDataSheetWithTables(Sheet sheet, XSSFWorkbook workbook, String date,
                                              List<Map<String, String>> summaryTableData,
                                              List<Map<String, String>> comparisonTableData,
                                              List<Map<String, String>> counselingTableData,
                                              String counselingTypeChartImage,
                                              String hourlyCallsChartImage,
                                              CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 일일 운영현황 (" + date + ")");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        try {
            // 1. 통계 현황 테이블 데이터 추가
            if (summaryTableData != null && !summaryTableData.isEmpty()) {
                logger.info("📊 통계 현황 테이블 데이터 추가 시작");
                rowIndex = addTableDataToSheet(sheet, summaryTableData, "📈 통계 현황", 
                                             rowIndex, headerStyle, dataStyle, 
                                             new String[]{"항목", "값"});
                logger.info("📊 통계 현황 테이블 데이터 추가 완료");
                rowIndex += 2; // 여백
            }
            
            // 2. 통화 시간 현황 테이블 데이터 추가
            if (comparisonTableData != null && !comparisonTableData.isEmpty()) {
                logger.info("📊 통화 시간 현황 테이블 데이터 추가 시작");
                rowIndex = addTableDataToSheet(sheet, comparisonTableData, "⏰ 통화 시간 현황", 
                                             rowIndex, headerStyle, dataStyle, 
                                             new String[]{"시간", "오늘", "어제", "변화"});
                logger.info("📊 통화 시간 현황 테이블 데이터 추가 완료");
                rowIndex += 2; // 여백
            }
            
            // 3. 상담유형 현황 테이블 데이터 추가
            if (counselingTableData != null && !counselingTableData.isEmpty()) {
                logger.info("📊 상담유형 현황 테이블 데이터 추가 시작");
                rowIndex = addTableDataToSheet(sheet, counselingTableData, "📋 상담유형 현황", 
                                             rowIndex, headerStyle, dataStyle, 
                                             new String[]{"유형", "건수", "비율"});
                logger.info("📊 상담유형 현황 테이블 데이터 추가 완료");
                rowIndex += 2; // 여백
            }
            
            // 4. 상담유형 차트 이미지 추가
            if (counselingTypeChartImage != null && !counselingTypeChartImage.isEmpty()) {
                logger.info("📊 상담유형 차트 이미지 추가 시작");
                rowIndex = addImageToSheet(sheet, workbook, counselingTypeChartImage, 
                                         "📊 상담유형 분포 차트", rowIndex, headerStyle);
                logger.info("📊 상담유형 차트 이미지 추가 완료");
                rowIndex += 2; // 여백
            }
            
            // 5. 시간대별 차트 이미지 추가
            if (hourlyCallsChartImage != null && !hourlyCallsChartImage.isEmpty()) {
                logger.info("📊 시간대별 차트 이미지 추가 시작");
                rowIndex = addImageToSheet(sheet, workbook, hourlyCallsChartImage, 
                                         "📈 시간대별 통화량 차트", rowIndex, headerStyle);
                logger.info("📊 시간대별 차트 이미지 추가 완료");
            }
            
        } catch (Exception e) {
            logger.error("❌ 일일 운영현황 시트 생성 실패: {}", e.getMessage(), e);
        }
        
        // 시트 전체 컬럼 너비 최종 설정 (autoSizeColumn 대신 직접 설정)
        sheet.setColumnWidth(0, 6000); // 첫 번째 컬럼 (항목명, 유형 등)
        sheet.setColumnWidth(1, 4000); // 두 번째 컬럼 (값, 건수 등)
        sheet.setColumnWidth(2, 4000); // 세 번째 컬럼 (어제, 비율 등)
        sheet.setColumnWidth(3, 4000); // 네 번째 컬럼 (변화)
        
        logger.info("✅ 일일 운영현황 시트 생성 완료");
    }

    /**
     * 📊 테이블 데이터를 엑셀 시트에 추가하는 헬퍼 메서드
     */
    private int addTableDataToSheet(Sheet sheet, List<Map<String, String>> tableData, String title,
                                  int startRow, CellStyle headerStyle, CellStyle dataStyle,
                                  String[] headers) {
        logger.info("📊 테이블 데이터 추가 시작: {}, 행 수: {}", title, tableData != null ? tableData.size() : 0);
        
        int currentRow = startRow;
        
        // 섹션 제목
        Row titleRow = sheet.createRow(currentRow++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);
        
        currentRow++; // 빈 행
        
        // 테이블 헤더
        Row headerRow = sheet.createRow(currentRow++);
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 컬럼 너비 설정 (테이블에 맞게)
        if (headers.length == 2) { // 통계 현황
            sheet.setColumnWidth(0, 5000); // 항목명
            sheet.setColumnWidth(1, 3000); // 값
        } else if (headers.length == 4) { // 통화 시간 현황
            sheet.setColumnWidth(0, 4000); // 시간
            sheet.setColumnWidth(1, 3000); // 오늘
            sheet.setColumnWidth(2, 3000); // 어제
            sheet.setColumnWidth(3, 3000); // 증감률
        } else if (headers.length == 3) { // 상담유형 현황
            sheet.setColumnWidth(0, 4000); // 유형
            sheet.setColumnWidth(1, 3000); // 건수
            sheet.setColumnWidth(2, 3000); // 비율
        }
        
        // 테이블 데이터가 없는 경우 처리
        if (tableData == null || tableData.isEmpty()) {
            Row noDataRow = sheet.createRow(currentRow++);
            Cell noDataCell = noDataRow.createCell(0);
            noDataCell.setCellValue("데이터가 없습니다.");
            noDataCell.setCellStyle(dataStyle);
            logger.warn("⚠️ {} 테이블 데이터가 비어있음", title);
            return currentRow + 1; // 여백 추가
        }
        
        // 테이블 데이터
        for (Map<String, String> rowData : tableData) {
            Row dataRow = sheet.createRow(currentRow++);
            
            if (headers.length == 2) { // 통계 현황 (label, value)
                String label = rowData.get("label");
                String value = rowData.get("value");
                if (label != null) dataRow.createCell(0).setCellValue(label);
                if (value != null) dataRow.createCell(1).setCellValue(value);
            } else if (headers.length == 4) { // 통화 시간 현황 (time, today, yesterday, change)
                String time = rowData.get("time");
                String today = rowData.get("today");
                String yesterday = rowData.get("yesterday");
                String change = rowData.get("change");
                if (time != null) dataRow.createCell(0).setCellValue(time);
                if (today != null) dataRow.createCell(1).setCellValue(today);
                if (yesterday != null) dataRow.createCell(2).setCellValue(yesterday);
                if (change != null) dataRow.createCell(3).setCellValue(change);
            } else if (headers.length == 3) { // 상담유형 현황 (type, count, percentage)
                String type = rowData.get("type");
                String count = rowData.get("count");
                String percentage = rowData.get("percentage");
                if (type != null) dataRow.createCell(0).setCellValue(type);
                if (count != null) dataRow.createCell(1).setCellValue(count);
                if (percentage != null) dataRow.createCell(2).setCellValue(percentage);
            }
            
            // 데이터 셀에 스타일 적용
            for (int i = 0; i < headers.length; i++) {
                if (dataRow.getCell(i) != null) {
                    dataRow.getCell(i).setCellStyle(dataStyle);
                }
            }
        }
        
        logger.info("✅ {} 테이블 데이터 추가 완료: {} 행", title, tableData.size());
        return currentRow + 1; // 여백 추가
    }

    /**
     * 📊 일일 운영현황 이미지를 엑셀에 추가하는 헬퍼 메서드
     */
    private int addImageToSheet(Sheet sheet, XSSFWorkbook workbook, String base64Image, 
                               String title, int startRow, CellStyle headerStyle) {
        try {
            logger.info("🖼️ 이미지 추가 시작: {}, 시작 행: {}", title, startRow);
            
            // 제목 추가
            Row titleRow = sheet.createRow(startRow++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(headerStyle);
            
            // Base64 이미지 데이터 확인
            if (base64Image == null || base64Image.isEmpty()) {
                logger.warn("⚠️ {} 이미지 데이터가 비어있음", title);
                return startRow + 2; // 빈 공간 추가
            }
            
            logger.info("🖼️ {} 이미지 데이터 길이: {}", title, base64Image.length());
            
            // Base64 이미지 데이터 디코딩
            if (!base64Image.contains(",")) {
                logger.error("❌ {} Base64 형식이 잘못됨: 구분자가 없음", title);
                return startRow + 2;
            }
            
            String[] parts = base64Image.split(",");
            if (parts.length < 2) {
                logger.error("❌ {} Base64 형식이 잘못됨: 데이터 부분이 없음", title);
                return startRow + 2;
            }
            
            String base64Data = parts[1]; // "data:image/png;base64," 부분 제거
            
            try {
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                logger.info("🖼️ {} 이미지 바이트 크기: {}", title, imageBytes.length);
                
                if (imageBytes.length == 0) {
                    logger.error("❌ {} 이미지 바이트가 비어있음", title);
                    return startRow + 2;
                }
                
                // 이미지 바이트 배열의 첫 몇 바이트 확인 (PNG 시그니처 확인)
                if (imageBytes.length >= 8) {
                    String signature = String.format("%02X%02X%02X%02X", 
                        imageBytes[0] & 0xFF, imageBytes[1] & 0xFF, 
                        imageBytes[2] & 0xFF, imageBytes[3] & 0xFF);
                    logger.info("🔍 {} 이미지 시그니처: {}", title, signature);
                    
                    // PNG 시그니처 확인 (89504E47)
                    if (!signature.equals("89504E47")) {
                        logger.warn("⚠️ {} PNG 시그니처가 아님: {}", title, signature);
                    }
                }
                
                // 이미지를 워크북에 추가
                int pictureIndex = workbook.addPicture(imageBytes, XSSFWorkbook.PICTURE_TYPE_PNG);
                logger.info("✅ {} 이미지를 워크북에 추가 완료, pictureIndex: {}", title, pictureIndex);
                
                // 드로잉 생성
                XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
                logger.info("📐 {} 드로잉 객체 생성 완료", title);
                
                // 이미지 위치 설정 (적절한 크기로 조정)
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, startRow, 6, startRow + 15);
                logger.info("📍 {} 앵커 설정 완료: startRow={}, endRow={}, 컬럼: 0~6", title, startRow, startRow + 15);
                
                // 이미지 삽입
                XSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
                logger.info("🖼️ {} 픽처 객체 생성 완료", title);
                
                // 적절한 크기로 조정 (테이블과 균형 맞춤)
                picture.resize(0.8);  // 0.8 = 원본 크기의 80%
                logger.info("📏 {} 이미지 크기 조정 완료 (80%)", title);
                
                // 이미지 차지 영역 계산 (적절한 크기)
                int imageRowSpan = 18; // 이미지 높이에 따라 조정
                
                // 컬럼 너비를 적절하게 설정
                for (int i = 0; i < 8; i++) {
                    sheet.setColumnWidth(i, 3200); // 적절한 컬럼 너비
                }
                
                logger.info("✅ {} 이미지 추가 완료. 다음 행: {}", title, startRow + imageRowSpan + 2);
                return startRow + imageRowSpan + 2; // 이미지 다음 행 반환 (여백 포함)
                
            } catch (IllegalArgumentException e) {
                logger.error("❌ {} Base64 디코딩 실패: {}", title, e.getMessage());
                return startRow + 2;
            }
            
        } catch (Exception e) {
            logger.error("❌ {} 이미지 추가 실패: {}", title, e.getMessage(), e);
            return startRow + 2; // 실패 시 2행만 건너뛰기
        }
    }

    /**
     * 📥 주간 운영현황 이미지 포함 엑셀 다운로드 API
     */
    @PostMapping("/stat/weekly/excel-with-images")
    public void downloadWeeklyOperationExcelWithImages(
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) throws IOException {
        
        try {
            logger.info("📥 주간 운영현황 이미지 포함 엑셀 다운로드 시작");
            
            // 주간 파라미터 추출
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            
            // 이미지 데이터 추출 (차트 이미지들만, 테이블 이미지는 상세데이터 시트에서 제공)
            String weeklyTrendChartImage = extractImageData(params.get("weeklyTrendChartImage"));
            String counselingTypesChartImage = extractImageData(params.get("counselingTypesChartImage"));
            String weeklyComparisonChartImage = extractImageData(params.get("weeklyComparisonChartImage"));
            
            // 주간 운영현황 데이터 조회
            WeeklyOperationStatsResponseDto weeklyData = statisticsService.getWeeklyOperationStatistics(startDate, endDate);
            
            // 이미지 포함 엑셀 파일 생성
            XSSFWorkbook workbook = createWeeklyOperationExcelFileWithImages(weeklyData, startDate, endDate,
                    weeklyTrendChartImage, counselingTypesChartImage, weeklyComparisonChartImage);
            
            // 파일명 설정
            String fileName = "주간운영현황(이미지포함)_" + startDate + "_" + endDate + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            // 응답 헤더 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            workbook.write(response.getOutputStream());
            workbook.close();
            
            logger.info("📥 주간 운영현황 이미지 포함 엑셀 다운로드 완료: {}", fileName);
            
        } catch (Exception e) {
            logger.error("❌ 주간 운영현황 이미지 포함 엑셀 다운로드 실패: {}", e.getMessage(), e);
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📊 주간 운영현황 이미지 포함 엑셀 파일 생성
     */
    private XSSFWorkbook createWeeklyOperationExcelFileWithImages(WeeklyOperationStatsResponseDto weeklyData, 
                                                                  String startDate, String endDate,
                                                                  String weeklyTrendChartImage,
                                                                  String counselingTypesChartImage,
                                                                  String weeklyComparisonChartImage) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // 스타일 생성
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 시트 1: 주간 대시보드 (차트 이미지 포함)
        Sheet dashboardSheet = workbook.createSheet("📊 주간 운영현황 대시보드");
        createWeeklyDashboardSheetWithImages(dashboardSheet, workbook, startDate, endDate,
                                             weeklyTrendChartImage, counselingTypesChartImage, 
                                             weeklyComparisonChartImage, headerStyle, dataStyle);
        
        // 시트 2: 주간 상세 데이터
        Sheet dataSheet = workbook.createSheet("📋 주간 상세 데이터");
        createWeeklyDataSheet(dataSheet, weeklyData, headerStyle, dataStyle);
        
        return workbook;
    }

    /**
     * 📊 주간 운영현황 이미지 포함 대시보드 시트 생성
     */
    private void createWeeklyDashboardSheetWithImages(Sheet sheet, XSSFWorkbook workbook, String startDate, String endDate,
                                                      String weeklyTrendChartImage, String counselingTypesChartImage,
                                                      String weeklyComparisonChartImage,
                                                      CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 주간 운영현황 대시보드 (" + startDate + " ~ " + endDate + ")");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        try {
            // 차트 이미지들만 추가 (테이블 이미지는 상세데이터 시트에서 제공)
            if (weeklyTrendChartImage != null && !weeklyTrendChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, weeklyTrendChartImage, "주간 운영 추이", rowIndex, headerStyle);
            }
            
            if (counselingTypesChartImage != null && !counselingTypesChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, counselingTypesChartImage, "상담유형 차트", rowIndex, headerStyle);
            }
            
            if (weeklyComparisonChartImage != null && !weeklyComparisonChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, weeklyComparisonChartImage, "주간 현황 비교 (금주 vs 전주)", rowIndex, headerStyle);
            }
            
        } catch (Exception e) {
            logger.error("❌ 주간 운영현황 이미지 추가 실패: {}", e.getMessage(), e);
        }
        
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 15000);
    }

    /**
     * 📊 주간 운영현황 상세 데이터 시트 생성
     */
    private void createWeeklyDataSheet(Sheet sheet, WeeklyOperationStatsResponseDto weeklyData, 
                                       CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📋 주간 운영현황 상세 데이터");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        // 주간 통계 현황 데이터
        if (weeklyData.getSummaryStatsList() != null && !weeklyData.getSummaryStatsList().isEmpty()) {
            rowIndex = addWeeklySummaryDataToSheet(sheet, weeklyData.getSummaryStatsList(), rowIndex, headerStyle, dataStyle);
        }
        
        rowIndex++; // 빈 행
        
        // 상담유형 통계
        if (weeklyData.getCounselingTypeStats() != null && !weeklyData.getCounselingTypeStats().isEmpty()) {
            rowIndex = addWeeklyCounselingTypeStatsToSheet(sheet, weeklyData.getCounselingTypeStats(), rowIndex, headerStyle, dataStyle);
        }
    }

    /**
     * 📊 주간 통계 현황 데이터를 시트에 추가
     */
    private int addWeeklySummaryDataToSheet(Sheet sheet, List<WeeklySummaryStatsDto> summaryStatsList, 
                                           int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = startRow;
        
        // 섹션 제목
        Row sectionTitleRow = sheet.createRow(rowIndex++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("📊 주간 통계 현황");
        sectionTitleCell.setCellStyle(headerStyle);
        
        // 헤더
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"일자", "총콜", "중복제거호", "응대호", "포기호", "부재호(BUSY)", 
                           "총상담완료호", "수신호", "발신호", "응대율", "완료율", "수신비율", "발신비율"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 데이터
        for (WeeklySummaryStatsDto stats : summaryStatsList) {
            Row dataRow = sheet.createRow(rowIndex++);
            
            dataRow.createCell(0).setCellValue(stats.getStatDate() != null ? stats.getStatDate() : "");
            dataRow.createCell(1).setCellValue(stats.getTotalCalls());
            dataRow.createCell(2).setCellValue(stats.getUniqueInboundCalls());
            dataRow.createCell(3).setCellValue(stats.getAnsweredCalls());
            dataRow.createCell(4).setCellValue(stats.getAbandonedCalls());
            dataRow.createCell(5).setCellValue(stats.getBusyCalls());
            dataRow.createCell(6).setCellValue(stats.getCompletedCounselingCalls());
            dataRow.createCell(7).setCellValue(stats.getInboundCalls());
            dataRow.createCell(8).setCellValue(stats.getOutboundCalls());
            dataRow.createCell(9).setCellValue(stats.getAnswerRate() != null ? String.format("%.1f%%", stats.getAnswerRate()) : "0.0%");
            dataRow.createCell(10).setCellValue(stats.getCompletionRate() != null ? String.format("%.1f%%", stats.getCompletionRate()) : "0.0%");
            dataRow.createCell(11).setCellValue(stats.getInboundRate() != null ? String.format("%.1f%%", stats.getInboundRate()) : "0.0%");
            dataRow.createCell(12).setCellValue(stats.getOutboundRate() != null ? String.format("%.1f%%", stats.getOutboundRate()) : "0.0%");
            
            // 데이터 스타일 적용
            for (int i = 0; i < headers.length; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        return rowIndex + 2; // 빈 행 포함
    }

    /**
     * 📊 주간 상담유형 통계 데이터를 시트에 추가
     */
    private int addWeeklyCounselingTypeStatsToSheet(Sheet sheet, List<WeeklyCounselingTypeStatsDto> counselingTypeStats, 
                                                    int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = startRow;
        
        // 섹션 제목
        Row sectionTitleRow = sheet.createRow(rowIndex++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("📊 상담유형 통계");
        sectionTitleCell.setCellStyle(headerStyle);
        
        // 헤더
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"상담유형", "상담수", "비율"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 데이터
        for (WeeklyCounselingTypeStatsDto stats : counselingTypeStats) {
            Row dataRow = sheet.createRow(rowIndex++);
            
            dataRow.createCell(0).setCellValue(stats.getCounselingTypeName() != null ? stats.getCounselingTypeName() : "");
            dataRow.createCell(1).setCellValue(stats.getCount());
            dataRow.createCell(2).setCellValue(stats.getPercentage() != null ? String.format("%.1f%%", stats.getPercentage()) : "0.0%");
            
            // 데이터 스타일 적용
            for (int i = 0; i < headers.length; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        return rowIndex + 2; // 빈 행 포함
    }

    /**
     * 📥 월간 운영현황 이미지 포함 엑셀 다운로드 API
     */
    @PostMapping("/stat/monthly/excel-with-images")
    public void downloadMonthlyOperationExcelWithImages(
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) throws IOException {
        
        try {
            logger.info("📥 월간 운영현황 이미지 포함 엑셀 다운로드 시작");
            
            // 월간 파라미터 추출
            String yearMonth = (String) params.get("yearMonth");
            
            // 이미지 데이터 추출 (차트 이미지들만, 테이블 이미지는 상세데이터 시트에서 제공)
            String monthlyTrendChartImage = extractImageData(params.get("monthlyTrendChartImage"));
            String counselingTypesChartImage = extractImageData(params.get("counselingTypesChartImage"));
            String monthlyComparisonChartImage = extractImageData(params.get("monthlyComparisonChartImage"));
            
            // 월간 운영현황 데이터 조회
            // yearMonth (예: "2025-01")를 해당 월의 시작일과 끝일로 변환
            String startDate = yearMonth.replaceAll("-", "") + "01"; // 예: "20250101"
            
            // 해당 월의 마지막 날 계산
            YearMonth ym = YearMonth.parse(yearMonth);
            String endDate = yearMonth.replaceAll("-", "") + String.format("%02d", ym.lengthOfMonth()); // 예: "20250131"
            
            MonthlyOperationStatsResponseDto monthlyData = statisticsService.getMonthlyOperationStatistics(startDate, endDate);
            
            // 이미지 포함 엑셀 파일 생성
            XSSFWorkbook workbook = createMonthlyOperationExcelFileWithImages(monthlyData, yearMonth,
                    monthlyTrendChartImage, counselingTypesChartImage, monthlyComparisonChartImage);
            
            // 파일명 설정
            String fileName = "월간운영현황(이미지포함)_" + yearMonth + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            // 응답 헤더 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            workbook.write(response.getOutputStream());
            workbook.close();
            
            logger.info("📥 월간 운영현황 이미지 포함 엑셀 다운로드 완료: {}", fileName);
            
        } catch (Exception e) {
            logger.error("❌ 월간 운영현황 이미지 포함 엑셀 다운로드 실패: {}", e.getMessage(), e);
            response.reset();
            response.setContentType("text/plain; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 📊 월간 운영현황 이미지 포함 엑셀 파일 생성
     */
    private XSSFWorkbook createMonthlyOperationExcelFileWithImages(MonthlyOperationStatsResponseDto monthlyData, 
                                                                  String yearMonth,
                                                                  String monthlyTrendChartImage,
                                                                  String counselingTypesChartImage,
                                                                  String monthlyComparisonChartImage) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // 스타일 생성
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 시트 1: 월간 대시보드 (차트 이미지 포함)
        Sheet dashboardSheet = workbook.createSheet("📊 월간 운영현황 대시보드");
        createMonthlyDashboardSheetWithImages(dashboardSheet, workbook, yearMonth,
                                             monthlyTrendChartImage, counselingTypesChartImage,
                                             monthlyComparisonChartImage, headerStyle, dataStyle);
        
        // 시트 2: 월간 상세 데이터 (테이블 데이터)
        Sheet dataSheet = workbook.createSheet("📋 월간 상세 데이터");
        createMonthlyDataSheet(dataSheet, monthlyData, headerStyle, dataStyle);
        
        return workbook;
    }

    /**
     * 📊 월간 운영현황 차트 이미지 포함 대시보드 시트 생성
     */
    private void createMonthlyDashboardSheetWithImages(Sheet sheet, XSSFWorkbook workbook, String yearMonth,
                                                      String monthlyTrendChartImage, String counselingTypesChartImage,
                                                      String monthlyComparisonChartImage,
                                                      CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 월간 운영현황 대시보드 (" + yearMonth + ")");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        try {
            // 이미지들을 순차적으로 추가 (차트 이미지들만)
            if (monthlyTrendChartImage != null && !monthlyTrendChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, monthlyTrendChartImage, "월간 통계 추이", rowIndex, headerStyle);
            }
            
            if (counselingTypesChartImage != null && !counselingTypesChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, counselingTypesChartImage, "상담유형 차트", rowIndex, headerStyle);
            }
            
            if (monthlyComparisonChartImage != null && !monthlyComparisonChartImage.isEmpty()) {
                rowIndex = addImageToSheet(sheet, workbook, monthlyComparisonChartImage, "전월 대비 실적 비교", rowIndex, headerStyle);
            }
            
        } catch (Exception e) {
            logger.error("❌ 월간 운영현황 차트 이미지 추가 실패: {}", e.getMessage(), e);
        }
        
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 15000);
    }

    /**
     * 📊 월간 운영현황 상세 데이터 시트 생성
     */
    private void createMonthlyDataSheet(Sheet sheet, MonthlyOperationStatsResponseDto monthlyData, 
                                       CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = 0;
        
        // 제목
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📋 월간 운영현황 상세 데이터");
        titleCell.setCellStyle(headerStyle);
        
        rowIndex++; // 빈 행
        
        // 월간 요약 통계
        if (monthlyData.getSummaryStats() != null) {
            rowIndex = addMonthlySummaryStatsToSheet(sheet, monthlyData.getSummaryStats(), rowIndex, headerStyle, dataStyle);
        }
        
        // 주차별 통계
        if (monthlyData.getWeeklyStatsList() != null && !monthlyData.getWeeklyStatsList().isEmpty()) {
            rowIndex = addWeeklyStatsListToSheet(sheet, monthlyData.getWeeklyStatsList(), rowIndex, headerStyle, dataStyle);
        }
    }

    /**
     * 📊 스타일 생성 헬퍼 메서드들
     */
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
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
        
        return headerStyle;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        return dataStyle;
    }

    /**
     * 📊 월간 요약 통계 데이터를 시트에 추가
     */
    private int addMonthlySummaryStatsToSheet(Sheet sheet, MonthlySummaryStatsDto summaryStats, 
                                             int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = startRow;
        
        // 섹션 제목
        Row sectionTitleRow = sheet.createRow(rowIndex++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("📊 월간 요약 통계");
        sectionTitleCell.setCellStyle(headerStyle);
        
        // 헤더
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"월", "총콜", "중복제거호", "응대호", "포기호", "부재호(BUSY)", 
                           "총상담완료호", "수신호", "발신호", "응대율", "완료율", "수신비율", "발신비율"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 데이터
        Row dataRow = sheet.createRow(rowIndex++);
        dataRow.createCell(0).setCellValue(summaryStats.getStatDate() != null ? summaryStats.getStatDate() : "");
        dataRow.createCell(1).setCellValue(summaryStats.getTotalCalls());
        dataRow.createCell(2).setCellValue(summaryStats.getUniqueInboundCalls());
        dataRow.createCell(3).setCellValue(summaryStats.getAnsweredCalls());
        dataRow.createCell(4).setCellValue(summaryStats.getAbandonedCalls());
        dataRow.createCell(5).setCellValue(summaryStats.getBusyCalls());
        dataRow.createCell(6).setCellValue(summaryStats.getCompletedCounselingCalls());
        dataRow.createCell(7).setCellValue(summaryStats.getInboundCalls());
        dataRow.createCell(8).setCellValue(summaryStats.getOutboundCalls());
        dataRow.createCell(9).setCellValue(summaryStats.getAnswerRate() != null ? String.format("%.1f%%", summaryStats.getAnswerRate()) : "0.0%");
        dataRow.createCell(10).setCellValue(summaryStats.getCompletionRate() != null ? String.format("%.1f%%", summaryStats.getCompletionRate()) : "0.0%");
        dataRow.createCell(11).setCellValue(summaryStats.getInboundRate() != null ? String.format("%.1f%%", summaryStats.getInboundRate()) : "0.0%");
        dataRow.createCell(12).setCellValue(summaryStats.getOutboundRate() != null ? String.format("%.1f%%", summaryStats.getOutboundRate()) : "0.0%");
        
        // 데이터 스타일 적용
        for (int i = 0; i < headers.length; i++) {
            dataRow.getCell(i).setCellStyle(dataStyle);
        }
        
        return rowIndex + 2; // 빈 행 포함
    }

    /**
     * 📊 주차별 통계 데이터를 시트에 추가
     */
    private int addWeeklyStatsListToSheet(Sheet sheet, List<WeeklySummaryStatsDto> weeklyStatsList, 
                                         int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowIndex = startRow;
        
        // 섹션 제목
        Row sectionTitleRow = sheet.createRow(rowIndex++);
        Cell sectionTitleCell = sectionTitleRow.createCell(0);
        sectionTitleCell.setCellValue("📊 주차별 통계");
        sectionTitleCell.setCellStyle(headerStyle);
        
        // 헤더
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"일자", "총콜", "중복제거호", "응대호", "포기호", "부재호(BUSY)", 
                           "총상담완료호", "수신호", "발신호", "응대율", "완료율", "수신비율", "발신비율"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }
        
        // 데이터
        for (WeeklySummaryStatsDto stats : weeklyStatsList) {
            Row dataRow = sheet.createRow(rowIndex++);
            
            dataRow.createCell(0).setCellValue(stats.getStatDate() != null ? stats.getStatDate() : "");
            dataRow.createCell(1).setCellValue(stats.getTotalCalls());
            dataRow.createCell(2).setCellValue(stats.getUniqueInboundCalls());
            dataRow.createCell(3).setCellValue(stats.getAnsweredCalls());
            dataRow.createCell(4).setCellValue(stats.getAbandonedCalls());
            dataRow.createCell(5).setCellValue(stats.getBusyCalls());
            dataRow.createCell(6).setCellValue(stats.getCompletedCounselingCalls());
            dataRow.createCell(7).setCellValue(stats.getInboundCalls());
            dataRow.createCell(8).setCellValue(stats.getOutboundCalls());
            dataRow.createCell(9).setCellValue(stats.getAnswerRate() != null ? String.format("%.1f%%", stats.getAnswerRate()) : "0.0%");
            dataRow.createCell(10).setCellValue(stats.getCompletionRate() != null ? String.format("%.1f%%", stats.getCompletionRate()) : "0.0%");
            dataRow.createCell(11).setCellValue(stats.getInboundRate() != null ? String.format("%.1f%%", stats.getInboundRate()) : "0.0%");
            dataRow.createCell(12).setCellValue(stats.getOutboundRate() != null ? String.format("%.1f%%", stats.getOutboundRate()) : "0.0%");
            
            // 데이터 스타일 적용
            for (int i = 0; i < headers.length; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        return rowIndex + 2; // 빈 행 포함
    }

    /**
     * 📷 이미지 데이터를 안전하게 추출하는 헬퍼 메서드
     */
    private String extractImageData(Object imageData) {
        if (imageData == null) {
            logger.info("🔍 이미지 데이터가 null입니다.");
            return null;
        }
        
        logger.info("🔍 이미지 데이터 타입: {}", imageData.getClass().getSimpleName());
        
        // String인 경우 그대로 반환
        if (imageData instanceof String) {
            String imageString = (String) imageData;
            if (imageString.startsWith("data:image/")) {
                logger.info("✅ 유효한 Base64 이미지 데이터 (길이: {})", imageString.length());
                return imageString;
            } else {
                logger.warn("⚠️ 유효하지 않은 이미지 데이터 형식: {}", imageString.substring(0, Math.min(50, imageString.length())));
                return null;
            }
        }
        
        // Map인 경우 (ApexCharts dataURI 결과가 객체로 감싸진 경우)
        if (imageData instanceof Map) {
            Map<?, ?> imageMap = (Map<?, ?>) imageData;
            logger.info("🔍 Map 형태의 이미지 데이터. 키: {}", imageMap.keySet());
            
            // 'dataURI', 'data', 'uri' 등의 키로 실제 데이터 URI를 찾아본다
            for (String key : new String[]{"dataURI", "data", "uri", "image", "base64"}) {
                Object value = imageMap.get(key);
                if (value instanceof String) {
                    String imageString = (String) value;
                    if (imageString.startsWith("data:image/")) {
                        logger.info("✅ Map에서 유효한 Base64 이미지 데이터 찾음 (키: {}, 길이: {})", key, imageString.length());
                        return imageString;
                    }
                }
            }
            
            // 첫 번째 String 값을 찾아서 반환
            for (Object value : imageMap.values()) {
                if (value instanceof String && ((String) value).startsWith("data:image/")) {
                    logger.info("✅ Map에서 유효한 Base64 이미지 데이터 찾음 (길이: {})", ((String) value).length());
                    return (String) value;
                }
            }
        }
        
        logger.warn("⚠️ 알 수 없는 이미지 데이터 타입: {}", imageData.getClass().getSimpleName());
        return null;
    }

}
