package com.wio.crm.model;

public class DashboardData {
    private String count_Miss;
    private String count_Com;

    private String count_sum;

    private String processing_rate;

    private String dailyPoint;

    private String cs_type;

    public String getCount_Miss() {
        return count_Miss;
    }

    public void setCount_Miss(String count_Miss) {
        this.count_Miss = count_Miss;
    }

    public String getCount_Com() {
        return count_Com;
    }

    public void setCount_Com(String count_Com) {
        this.count_Com = count_Com;
    }

    public String getCount_sum() {
        return count_sum;
    }

    public void setCount_sum(String count_sum) {
        this.count_sum = count_sum;
    }

    public String getProcessing_rate() {
        return processing_rate;
    }

    public void setProcessing_rate(String processing_rate) {
        this.processing_rate = processing_rate;
    }

    public String getDailyPoint() {
        return dailyPoint;
    }

    public void setDailyPoint(String dailyPoint) {
        this.dailyPoint = dailyPoint;
    }

    public String getCs_type() {
        return cs_type;
    }

    public void setCs_type(String cs_type) {
        this.cs_type = cs_type;
    }
}
