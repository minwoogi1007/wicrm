package com.wio.crm.service;


import com.wio.crm.mapper.InvoiceMapper;
import com.wio.crm.model.Invoice;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Transactional
    public void saveInvoicesFromFile(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = getWorkbook(file, is);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 헤더 건너뛰기
                Invoice invoice = new Invoice();
                // 여기서 INVOICE_ID 값을 설정합니다.
                invoice.setInvoiceId(invoiceMapper.getNextInvoiceId());  // 또는 UUID를 사용하거나 기타 값을 사용하여 생성

                invoice.setRegion(getCellValueAsString(row.getCell(0)));
                invoice.setTrackingNumber(getCellValueAsString(row.getCell(1)));
                invoice.setRecipientName(getCellValueAsString(row.getCell(2)));
                invoice.setRecipientPhone(getCellValueAsString(row.getCell(3)));
                invoice.setRecipientManager(getCellValueAsString(row.getCell(4)));
                invoice.setRecipientMobile(getCellValueAsString(row.getCell(5)));
                invoice.setRecipientPostalCode(getCellValueAsString(row.getCell(6)));
                invoice.setRecipientAddress(getCellValueAsString(row.getCell(7)));
                invoice.setQuantity((int) getCellValueAsNumeric(row.getCell(8)));
                invoice.setProductName(getCellValueAsString(row.getCell(9)));
                invoice.setFreightType(getCellValueAsString(row.getCell(10)));
                invoice.setPaymentCondition(getCellValueAsString(row.getCell(11)));
                invoice.setReleaseNumber(getCellValueAsString(row.getCell(12)));
                invoice.setSpecialNote(getCellValueAsString(row.getCell(13)));
                invoice.setMemo1(getCellValueAsString(row.getCell(14)));
                invoice.setMemo2(getCellValueAsString(row.getCell(15)));
                invoice.setMemo3(getCellValueAsString(row.getCell(16)));
                invoice.setMemo4(getCellValueAsString(row.getCell(17)));
                invoiceMapper.insertInvoice(invoice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Workbook getWorkbook(MultipartFile file, InputStream is) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.endsWith(".xls")) {
            return new HSSFWorkbook(is);
        } else if (filename != null && filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("지원되지 않는 파일 형식입니다. 올바른 엑셀 파일을 업로드하세요.");
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // 날짜 셀 처리
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private double getCellValueAsNumeric(Cell cell) {
        if (cell == null) {
            return 0;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0; // 숫자로 변환 실패시 기본값 반환
            }
        } else {
            return 0;
        }
    }

    private java.util.Date getCellValueAsDate(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC || !DateUtil.isCellDateFormatted(cell)) {
            return null;
        }
        return cell.getDateCellValue();
    }
}