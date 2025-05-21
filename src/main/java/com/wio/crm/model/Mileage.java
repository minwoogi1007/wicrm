package com.wio.crm.model;

import org.apache.ibatis.annotations.Mapper;

public class Mileage {
    private String remainingMileage;

    // Constructors, getters, and setters
    public Mileage() {
        this.remainingMileage = "0";
    }
    
    public Mileage(String remainingMileage) {
        this.remainingMileage = remainingMileage;
    }

    public String getRemainingMileage() {
        return remainingMileage;
    }

    public void setRemainingMileage(String remainingMileage) {
        this.remainingMileage = remainingMileage;
    }
}

