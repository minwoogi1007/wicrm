package com.wio.crm.controller;

import com.wio.crm.Entity.AdminCode;
import com.wio.crm.model.Comment;
import com.wio.crm.model.CompletionCode;
import com.wio.crm.model.Consultation;
import com.wio.crm.model.History;
import com.wio.crm.service.AdminCodeService;
import com.wio.crm.service.ConService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
public class ConController {
    @Autowired
    private AdminCodeService adminCodeService;

    @Autowired
    private ConService conService;


    @GetMapping("/cons")
    public String consulting( Model model) {

        List<AdminCode> constat = adminCodeService.getAdminCodesByGubn("4003");
        List<AdminCode> conbuy = adminCodeService.getAdminCodesByGubn("5000");
        List<AdminCode> contype = adminCodeService.getAdminCodesByGubn("4002");
        List<AdminCode> custStat = adminCodeService.getAdminCodesByGubn("9500");


        model.addAttribute("constat", constat);
        model.addAttribute("contype", contype);
        model.addAttribute("conbuy", conbuy);
        model.addAttribute("custStat", custStat);

// Print adminCodes to console for debugging

        // Convert to JSON for better readability

        return "cons/consulting";
    }

    @GetMapping("/api/consultations")
    public ResponseEntity<Map<String, Object>> getConsultations(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "25") int pageSize,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "mall", required = false) String mall,
            @RequestParam(value = "custStat", required = false) String custStat,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "cardFilter", required = false) String cardFilter) {

        List<Consultation> consultations = conService.getConsultations(page, pageSize, startDate, endDate, status, type, mall, custStat, keyword, filter, cardFilter);
        int total = conService.countTotal(startDate, endDate, status, type, mall, custStat, keyword, filter, cardFilter);

        Map<String, Object> response = new HashMap<>();
        response.put("data", consultations);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/consultations/stats")
    public ResponseEntity<Map<String, Object>> getConsultationStats(
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "mall", required = false) String mall,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Map<String, Object> stats = conService.getConsultationStats(startDate, endDate, status, type, mall, keyword);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/consultations/details")
    public ResponseEntity<Map<String, Object>> getConsultationDetails(@RequestParam(value = "projectCode") String projectCode,
                                                                              @RequestParam(value = "personCode") String personCode,
                                                                              @RequestParam(value = "callCode") String callCode) {
        Consultation consultation = conService.getConsultationDetails( projectCode, personCode, callCode);
        List<Comment> comments = conService.getComments( projectCode, personCode, callCode);
        List<History> history = conService.getHistory( projectCode, personCode, callCode); // 추가된 부분

        Map<String, Object> response = new HashMap<>();
        response.put("data", consultation);
        response.put("total", comments);
        response.put("history", history);
        // COMPLETION_CODE 값을 별도로 포함
        response.put("completionCode", consultation.getCompletionCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/consultations/addComment")
    public ResponseEntity<Void> addComment(@RequestBody Comment request) {
        conService.addComment(request);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/api/consultations/updateCompletionCode")
    public ResponseEntity<String> updateCompletionCode(@RequestBody CompletionCode request) {
        try {
            int updatedRows = conService.updateCompletionCode(request);
            if (updatedRows > 0) {
                return ResponseEntity.ok("Completion code updated successfully");
            } else {
                // TBND01_CUST 레코드가 없는 경우 (CTI 시스템에서 아직 생성되지 않음)
                return ResponseEntity.ok("NO_RECORD");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating completion code: " + e.getMessage());
        }
    }
    @GetMapping("/api/consultations/excel")
    public void downloadExcel(@RequestParam(value = "startDate") String startDate,
                              @RequestParam(value = "endDate") String endDate,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "mall", required = false) String mall,
                              @RequestParam(value = "custStat", required = false) String custStat,
                              @RequestParam(value = "filter", required = false) String filter,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              HttpServletResponse response) throws IOException {


        // 데이터를 조회하고 엑셀 파일 생성
        List<Consultation> consultations = conService.getConsultationsForExcel(startDate, endDate, status, type, mall,custStat, filter, keyword);

        // Workbook 생성
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Consultations");

        // Header row 생성
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("구분");
        headerRow.createCell(1).setCellValue("일자");
        headerRow.createCell(2).setCellValue("시간");
        headerRow.createCell(3).setCellValue("고객명");
        headerRow.createCell(4).setCellValue("전화번호");
        headerRow.createCell(5).setCellValue("상담유형");
        headerRow.createCell(6).setCellValue("구매몰");
        headerRow.createCell(7).setCellValue("사이트");
        headerRow.createCell(8).setCellValue("처리상태");
        headerRow.createCell(9).setCellValue("상담내용");
        headerRow.createCell(10).setCellValue("처리내용");
        headerRow.createCell(11).setCellValue("댓글");

        // Set cell style for wrapping text
        CellStyle wrapTextStyle = workbook.createCellStyle();
        wrapTextStyle.setWrapText(true);
        // 데이터 row 생성
        int rowIdx = 1;
        for (Consultation consultation : consultations) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(consultation.getEmgGubn().equals("1")? "긴급" : "일반");
            row.createCell(1).setCellValue(consultation.getInDate());
            row.createCell(2).setCellValue(consultation.getInTime());
            row.createCell(3).setCellValue(consultation.getEmpNo());
            row.createCell(4).setCellValue(consultation.getCustTell());
            row.createCell(5).setCellValue(consultation.getCsType());
            row.createCell(6).setCellValue(consultation.getBuyGubn());
            row.createCell(7).setCellValue(consultation.getProjectName());
            row.createCell(8).setCellValue(consultation.getPrcGubn());
            Cell csNoteCell = row.createCell(9);
            csNoteCell.setCellValue(consultation.getCsNote());
            csNoteCell.setCellStyle(wrapTextStyle);

            Cell prcNoteCell = row.createCell(10);
            prcNoteCell.setCellValue(consultation.getPrcNote());
            prcNoteCell.setCellStyle(wrapTextStyle);
            row.createCell(11).setCellValue(consultation.getCountRe());
        }
        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }
        // 파일명에 날짜와 시간을 포함시키기 위한 포맷터 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        // 파일명 설정
        String filename = "consultations_" + timestamp + ".xlsx";
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 엑셀 파일을 응답으로 출력
        workbook.write(out);
        workbook.close();

        response.getOutputStream().write(out.toByteArray());
    }


}
