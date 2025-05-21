package com.wio.crm.model;

import java.util.Date;

public class Banner {
    private Long id;              // NUMBER(10,0) Not Null
    private String name;          // VARCHAR2(100 BYTE) Not Null
    private String imageUrl;      // VARCHAR2(255 BYTE) Not Null
    private String linkUrl;       // VARCHAR2(255 BYTE) Not Null
    private String position;      // VARCHAR2(50 BYTE) Not Null
    private Integer active;       // NUMBER(1,0) Null 허용
    private Integer displayOrder; // NUMBER(10,0) Null 허용
    private Date startDate;       // DATE Null 허용
    private Date endDate;         // DATE Null 허용
    
    // 기본 생성자
    public Banner() {
        // 기본값 설정
        this.active = 1;         // 기본적으로 활성화
        this.displayOrder = 0;   // 기본 표시 순서는 0
    }
    
    // Getter와 Setter 메소드
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public Integer getActive() { return active; }
    public void setActive(Integer active) { this.active = active; }
    
    // boolean 타입 변환을 위한 편의 메서드
    public boolean isActive() { return active == 1; }
    public void setActive(boolean active) { this.active = active ? 1 : 0; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}