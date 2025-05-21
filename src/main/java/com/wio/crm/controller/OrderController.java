package com.wio.crm.controller;

import com.wio.crm.service.InvoiceService;
import com.wio.crm.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/order")
    public String orderPage() {
        return "order/order";
    }

    @PostMapping("/uploadExcel")
    public String uploadExcel(@RequestParam("orderFile") MultipartFile orderFile,
                              @RequestParam("invoiceFile") MultipartFile invoiceFile,
                              Model model) {
        if (!orderFile.isEmpty()) {
            orderService.saveOrdersFromFile(orderFile);
        }
        if (!invoiceFile.isEmpty()) {
            invoiceService.saveInvoicesFromFile(invoiceFile);
        }
        model.addAttribute("message", "파일이 성공적으로 업로드되었습니다.");
        return "redirect:/order/order";
    }

    @GetMapping("/missingInvoice")
    public String missingInvoice(Model model) {
       // model.addAttribute("missingOrders", orderService.getMissingOrders());
        return "order/order";
    }
}