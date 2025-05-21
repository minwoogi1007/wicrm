package com.wio.crm.service;

import com.wio.crm.mapper.InvoiceMapper;
import com.wio.crm.mapper.OrderMapper;
import com.wio.crm.model.Invoice;
import com.wio.crm.model.Order;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private InvoiceMapper invoiceMapper;
    @Transactional
    public void saveOrdersFromFile(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = getWorkbook(file, is);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 헤더 건너뛰기
                Order order = new Order();

                // order_id 값을 설정합니다.
                order.setOrderId(orderMapper.getNextOrderId());

                order.setSequence(getCellValueAsString(row.getCell(0)));
                order.setOrderTime(getCellValueAsString(row.getCell(1)));
                order.setOrderDate(getCellValueAsString(row.getCell(2)));
                order.setSaOrderNumber(getCellValueAsString(row.getCell(3)));
                order.setRmk3(getCellValueAsString(row.getCell(4)));
                order.setRmk1(getCellValueAsString(row.getCell(5)));
                order.setShoppingOrder(getCellValueAsString(row.getCell(6)));
                order.setRecipientName(getCellValueAsString(row.getCell(7)));
                order.setOrderMall(getCellValueAsString(row.getCell(8)));
                order.setContact1(getCellValueAsString(row.getCell(9)));
                order.setContact1(getCellValueAsString(row.getCell(10)));
                order.setZipCode(getCellValueAsString(row.getCell(11)));
                order.setAddress( getCellValueAsString(row.getCell(12)));
                order.setQuantity(getCellValueAsString(row.getCell(13)));
                order.setProductCode(getCellValueAsString(row.getCell(14)));
                order.setProductOption(getCellValueAsString(row.getCell(15)));
                order.setSalePrice(getCellValueAsString(row.getCell(16)));
                order.setPaymentAmount(getCellValueAsString(row.getCell(17)));
                order.setGift(getCellValueAsString(row.getCell(18)));
                order.setShippingType(getCellValueAsString(row.getCell(19)));
                order.setDeliveryMemo(getCellValueAsString(row.getCell(20)));
                order.setReleaseNumber(getCellValueAsString(row.getCell(21)));
                order.setdMemo(getCellValueAsString(row.getCell(22)));
                order.setRmk2(getCellValueAsString(row.getCell(23)));
                order.setShoppingMallName(getCellValueAsString(row.getCell(24)));
                order.setProductCode2(getCellValueAsString(row.getCell(25)));
                order.setOrderName(getCellValueAsString(row.getCell(26)));

                orderMapper.insertOrder(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public List<Order> getMissingOrders() {
        List<Order> orders = orderMapper.getAllOrders();
        List<Invoice> invoices = invoiceMapper.getAllInvoices();

        // 송장과 주문서를 비교하여 미출 주문서를 찾기 위한 로직
        return orders.stream()
                .filter(order -> {
                    String combinedOrderItem = order.getProductCode() + "-" + order.getProductOption() + "-" + order.getQuantity() + ".";
                    return invoices.stream().noneMatch(invoice ->
                            order.getRecipientName().equals(invoice.getRecipientName()) &&
                                    order.getContact1().equals(invoice.getRecipientPhone()) &&
                                    order.getAddress().equals(invoice.getRecipientAddress()) &&
                                    combinedOrderItem.equals(invoice.getProductName())
                    );
                })
                .collect(Collectors.toList());
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