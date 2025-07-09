package com.wio.repairsystem.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShippingOrderDTO {
    private Long releaseId;
    private Long requestId;
    private String productCode;
    private String partCode;
    private int quantity;
    private String status;
}