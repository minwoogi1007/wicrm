package com.wio.crm.mapper;

import com.wio.crm.model.Invoice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InvoiceMapper {
    void insertInvoice(Invoice invoice);

    Long getNextInvoiceId();  // getNextInvoiceId 메서드 추가
    List<Invoice> getAllInvoices();
}
