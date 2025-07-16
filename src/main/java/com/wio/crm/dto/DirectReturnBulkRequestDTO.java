package com.wio.crm.dto;

import java.util.List;

public class DirectReturnBulkRequestDTO {
    
    // 공통 정보
    private String receivedDate;
    private String siteName;
    private String customerName;
    private String customerPhone;
    private String trackingNumber;
    private String processingStatus;
    private String mappingStatus;
    private String remarks;
    
    // 제품 목록
    private List<ProductInfo> products;
    
    // 생성자
    public DirectReturnBulkRequestDTO() {}
    
    // Getters and Setters
    public String getReceivedDate() {
        return receivedDate;
    }
    
    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }
    
    public String getSiteName() {
        return siteName;
    }
    
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getProcessingStatus() {
        return processingStatus;
    }
    
    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
    
    public String getMappingStatus() {
        return mappingStatus;
    }
    
    public void setMappingStatus(String mappingStatus) {
        this.mappingStatus = mappingStatus;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public List<ProductInfo> getProducts() {
        return products;
    }
    
    public void setProducts(List<ProductInfo> products) {
        this.products = products;
    }
    
    // 내부 클래스 - 제품 정보
    public static class ProductInfo {
        private String productCode;
        private Integer quantity;
        private String productColor;
        private String productSize;
        
        // 생성자
        public ProductInfo() {}
        
        public ProductInfo(String productCode, Integer quantity, String productColor, String productSize) {
            this.productCode = productCode;
            this.quantity = quantity;
            this.productColor = productColor;
            this.productSize = productSize;
        }
        
        // Getters and Setters
        public String getProductCode() {
            return productCode;
        }
        
        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public String getProductColor() {
            return productColor;
        }
        
        public void setProductColor(String productColor) {
            this.productColor = productColor;
        }
        
        public String getProductSize() {
            return productSize;
        }
        
        public void setProductSize(String productSize) {
            this.productSize = productSize;
        }
        
        @Override
        public String toString() {
            return "ProductInfo{" +
                    "productCode='" + productCode + '\'' +
                    ", quantity=" + quantity +
                    ", productColor='" + productColor + '\'' +
                    ", productSize='" + productSize + '\'' +
                    '}';
        }
    }
    
    @Override
    public String toString() {
        return "DirectReturnBulkRequestDTO{" +
                "receivedDate='" + receivedDate + '\'' +
                ", siteName='" + siteName + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", processingStatus='" + processingStatus + '\'' +
                ", mappingStatus='" + mappingStatus + '\'' +
                ", remarks='" + remarks + '\'' +
                ", products=" + products +
                '}';
    }
} 