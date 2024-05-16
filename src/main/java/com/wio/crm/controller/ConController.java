package com.wio.crm.controller;

import com.wio.crm.Entity.AdminCode;
import com.wio.crm.model.Consultation;
import com.wio.crm.service.AdminCodeService;
import com.wio.crm.service.ConService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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



        model.addAttribute("constat", constat);
        model.addAttribute("contype", contype);
        model.addAttribute("conbuy", conbuy);
// Print adminCodes to console for debugging

        // Convert to JSON for better readability

        return "cons/consulting";
    }

    @GetMapping("/api/consultations")
    public ResponseEntity<Map<String, Object>> getConsultations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String mall,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String keyword,
            Principal principal) {
// Log authenticated user details for debugging
        System.out.println("Authenticated user: " + principal.getName());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authorities: " + authentication.getAuthorities());


        List<Consultation> consultations = conService.getConsultations(page, pageSize, startDate, endDate, status, type, mall, keyword,filter);
        int total = conService.countTotal(startDate, endDate, status, type, mall, keyword,filter);

        Map<String, Object> response = new HashMap<>();
        response.put("data", consultations);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/consultations/excel")
    public void downloadExcel(@RequestParam String startDate,
                              @RequestParam String endDate,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String type,
                              @RequestParam(required = false) String mall,
                              @RequestParam(required = false) String filter,
                              @RequestParam(required = false) String keyword,
                              HttpServletResponse response) throws IOException {


        // 데이터를 조회하고 엑셀 파일 생성
        List<Consultation> consultations = conService.getConsultationsForExcel(startDate, endDate, status, type, mall, filter, keyword);

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
