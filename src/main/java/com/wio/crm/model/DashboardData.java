package com.wio.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DashboardData {
    private String count_Miss;
    private String count_Com;

    private String count_sum;

    private String processing_rate;

    private String dailyPoint;

    private int dailyPointN;

    private String cs_type;

    private String todayMiss;
    private String todayCom;
    private String todayEme;
    private String yesterdayEme;
    private String yesterdayCom;
    private String yesterdayMiss;


    private String statPeriod;
    private String cs_Name;
    private String cs_Type_Point;
    private String cs_Type_Count;

    private String cs_Type_Percentage;

    private String prc_Date;
    
    @JsonProperty("gubn")
    private String gubn;
    
    @JsonIgnore
    private String GUBN;
    
    @JsonIgnore
    private String PRC_DATE;
    
    // 추가: dashStatCount 쿼리 결과를 받기 위한 필드
    private int totalCount;
    
    @JsonProperty("hour_09")
    private int hour_09;
    
    @JsonProperty("hour_10")
    private int hour_10;
    
    @JsonProperty("hour_11")
    private int hour_11;
    
    @JsonProperty("hour_12")
    private int hour_12;
    
    @JsonProperty("hour_13")
    private int hour_13;
    
    @JsonProperty("hour_14")
    private int hour_14;
    
    @JsonProperty("hour_15")
    private int hour_15;
    
    @JsonProperty("hour_16")
    private int hour_16;
    
    @JsonProperty("hour_17")
    private int hour_17;
    
    @JsonProperty("hour_18")
    private int hour_18;
    
    @JsonProperty("hour_19")
    private int hour_19;

    @JsonProperty("callSum")
    private int callSum;

    // CALLSUM 필드 추가 (대문자 버전)
    @JsonProperty("CALLSUM")
    private int CALLSUM;

    private String personMonth;

    private int newPersonCount;

    private int oldPersonCount;

    private int thisMonth;

    private int previousMonth;

    private int percentChange;

    private String weekDay;
    private int avg1week;
    private int avg1month;
    private int avg3months;
    private int avg6months;
    private int avg9months;
    private int avg12months;

    private String period;
    private String empName;
    private int prcSum;
    private int pointSum;
    private int callP;
    private int total;
    private int DAILY_PRCSUM;
    private int MONTHLY_PRCSUM;
    private int YEARLY_PRCSUM;
    private String DAILY_POINTSUM;
    private String MONTHLY_POINTSUM;
    private String YEARLY_POINTSUM;
    private int DAILY_CALLP;
    private int MONTHLY_CALLP;
    private int YEARLY_CALLP;
    private String TDAILY_TOTAL;
    private String MONTHLY_TOTAL;
    private String TYEARLY_TOTAL;
    private int MONTHLY_POINTSUM_PERCENT;

    private String WEEK;
    private int MONDAY;

    private int TUESDAY;
    private int WEDNESDAY;
    private int THURSDAY;
    private int FRIDAY;

    private String MONTH;
    private int countMonthSum;

    private String DPOINTSUM;

    private String DPOINTCHARGE;
    private String DPOINTUSE;
    private String DPOINTUSEDAY;

    @JsonProperty("DPOINTUSEWEEK")
    private String DPOINTUSEWEEK;

    @JsonProperty("SUM_POINT")
    private int SUM_POINT;
    
    @JsonProperty("POINT_DATE")
    private String POINT_DATE;

    private String SUMCOM;

    private String SUMMISS;
    private String SUMMILE;
    private String SUMLOSS;
    private String CUST_NAME;
    private String SUMMONTH;

    private String SUMLOSSMONTH;


    public String getCUST_NAME() {
        return CUST_NAME;
    }

    public void setCUST_NAME(String CUST_NAME) {
        this.CUST_NAME = CUST_NAME;
    }

    public String getSUMCOM() {
        return SUMCOM;
    }

    public void setSUMCOM(String SUMCOM) {
        this.SUMCOM = SUMCOM;
    }

    public String getSUMMISS() {
        return SUMMISS;
    }

    public void setSUMMISS(String SUMMISS) {
        this.SUMMISS = SUMMISS;
    }

    public String getSUMMILE() {
        return SUMMILE;
    }

    public void setSUMMILE(String SUMMILE) {
        this.SUMMILE = SUMMILE;
    }

    public String getSUMLOSS() {
        return SUMLOSS;
    }

    public void setSUMLOSS(String SUMLOSS) {
        this.SUMLOSS = SUMLOSS;
    }

    public String getDPOINTSUM() {
        return DPOINTSUM;
    }

    public void setDPOINTSUM(String DPOINTSUM) {
        this.DPOINTSUM = DPOINTSUM;
    }

    public String getDPOINTCHARGE() {
        return DPOINTCHARGE;
    }

    public void setDPOINTCHARGE(String DPOINTCHARGE) {
        this.DPOINTCHARGE = DPOINTCHARGE;
    }

    public String getDPOINTUSE() {
        return DPOINTUSE;
    }

    public void setDPOINTUSE(String DPOINTUSE) {
        this.DPOINTUSE = DPOINTUSE;
    }

    public String getDPOINTUSEDAY() {
        return DPOINTUSEDAY;
    }

    public void setDPOINTUSEDAY(String DPOINTUSEDAY) {
        this.DPOINTUSEDAY = DPOINTUSEDAY;
    }

    public String getDPOINTUSEWEEK() {
        return DPOINTUSEWEEK;
    }

    public void setDPOINTUSEWEEK(String DPOINTUSEWEEK) {
        this.DPOINTUSEWEEK = DPOINTUSEWEEK;
    }

    public int getSUM_POINT() {
        return SUM_POINT;
    }

    public void setSUM_POINT(int SUM_POINT) {
        this.SUM_POINT = SUM_POINT;
    }

    public String getPOINT_DATE() {
        return POINT_DATE;
    }

    public void setPOINT_DATE(String POINT_DATE) {
        this.POINT_DATE = POINT_DATE;
    }

    public String getWEEK() {
        return WEEK;
    }

    public void setWEEK(String WEEK) {
        this.WEEK = WEEK;
    }

    public int getMONDAY() {
        return MONDAY;
    }

    public void setMONDAY(int MONDAY) {
        this.MONDAY = MONDAY;
    }

    public int getTUESDAY() {
        return TUESDAY;
    }

    public void setTUESDAY(int TUESDAY) {
        this.TUESDAY = TUESDAY;
    }

    public int getWEDNESDAY() {
        return WEDNESDAY;
    }

    public void setWEDNESDAY(int WEDNESDAY) {
        this.WEDNESDAY = WEDNESDAY;
    }

    public int getTHURSDAY() {
        return THURSDAY;
    }

    public void setTHURSDAY(int THURSDAY) {
        this.THURSDAY = THURSDAY;
    }

    public int getFRIDAY() {
        return FRIDAY;
    }

    public void setFRIDAY(int FRIDAY) {
        this.FRIDAY = FRIDAY;
    }

    public String getMONTH() {
        return MONTH;
    }

    public void setMONTH(String MONTH) {
        this.MONTH = MONTH;
    }

    public int getCountMonthSum() {
        return countMonthSum;
    }

    public void setCountMonthSum(int countMonthSum) {
        this.countMonthSum = countMonthSum;
    }

    public int getMONTHLY_POINTSUM_PERCENT() {
        return MONTHLY_POINTSUM_PERCENT;
    }

    public void setMONTHLY_POINTSUM_PERCENT(int MONTHLY_POINTSUM_PERCENT) {
        this.MONTHLY_POINTSUM_PERCENT = MONTHLY_POINTSUM_PERCENT;
    }

    public int getDAILY_PRCSUM() {
        return DAILY_PRCSUM;
    }

    public void setDAILY_PRCSUM(int DAILY_PRCSUM) {
        this.DAILY_PRCSUM = DAILY_PRCSUM;
    }

    public int getMONTHLY_PRCSUM() {
        return MONTHLY_PRCSUM;
    }

    public void setMONTHLY_PRCSUM(int MONTHLY_PRCSUM) {
        this.MONTHLY_PRCSUM = MONTHLY_PRCSUM;
    }

    public int getYEARLY_PRCSUM() {
        return YEARLY_PRCSUM;
    }

    public void setYEARLY_PRCSUM(int YEARLY_PRCSUM) {
        this.YEARLY_PRCSUM = YEARLY_PRCSUM;
    }

    public String getDAILY_POINTSUM() {
        return DAILY_POINTSUM;
    }

    public void setDAILY_POINTSUM(String DAILY_POINTSUM) {
        this.DAILY_POINTSUM = DAILY_POINTSUM;
    }

    public String getMONTHLY_POINTSUM() {
        return MONTHLY_POINTSUM;
    }

    public void setMONTHLY_POINTSUM(String MONTHLY_POINTSUM) {
        this.MONTHLY_POINTSUM = MONTHLY_POINTSUM;
    }

    public String getYEARLY_POINTSUM() {
        return YEARLY_POINTSUM;
    }

    public void setYEARLY_POINTSUM(String YEARLY_POINTSUM) {
        this.YEARLY_POINTSUM = YEARLY_POINTSUM;
    }

    public int getDAILY_CALLP() {
        return DAILY_CALLP;
    }

    public void setDAILY_CALLP(int DAILY_CALLP) {
        this.DAILY_CALLP = DAILY_CALLP;
    }

    public int getMONTHLY_CALLP() {
        return MONTHLY_CALLP;
    }

    public void setMONTHLY_CALLP(int MONTHLY_CALLP) {
        this.MONTHLY_CALLP = MONTHLY_CALLP;
    }

    public int getYEARLY_CALLP() {
        return YEARLY_CALLP;
    }

    public void setYEARLY_CALLP(int YEARLY_CALLP) {
        this.YEARLY_CALLP = YEARLY_CALLP;
    }

    public String getTDAILY_TOTAL() {
        return TDAILY_TOTAL;
    }

    public void setTDAILY_TOTAL(String TDAILY_TOTAL) {
        this.TDAILY_TOTAL = TDAILY_TOTAL;
    }

    public String getMONTHLY_TOTAL() {
        return MONTHLY_TOTAL;
    }

    public void setMONTHLY_TOTAL(String MONTHLY_TOTAL) {
        this.MONTHLY_TOTAL = MONTHLY_TOTAL;
    }

    public String getTYEARLY_TOTAL() {
        return TYEARLY_TOTAL;
    }

    public void setTYEARLY_TOTAL(String TYEARLY_TOTAL) {
        this.TYEARLY_TOTAL = TYEARLY_TOTAL;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public int getPrcSum() {
        return prcSum;
    }

    public void setPrcSum(int prcSum) {
        this.prcSum = prcSum;
    }

    public int getPointSum() {
        return pointSum;
    }

    public void setPointSum(int pointSum) {
        this.pointSum = pointSum;
    }

    public int getCallP() {
        return callP;
    }

    public void setCallP(int callP) {
        this.callP = callP;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getCount_Miss() {
        return count_Miss;
    }

    public void setCount_Miss(String count_Miss) {
        this.count_Miss = count_Miss;
    }

    public String getCount_Com() {
        if (count_Com != null) {
            return count_Com;
        }
        return todayCom;
    }

    public void setCount_Com(String count_Com) {
        this.count_Com = count_Com;
        this.todayCom = count_Com;
    }

    public void setCount_Com(Integer count_Com) {
        if (count_Com != null) {
            this.count_Com = String.valueOf(count_Com);
            this.todayCom = String.valueOf(count_Com);
        }
    }

    public void setCount_Com(Long count_Com) {
        if (count_Com != null) {
            this.count_Com = String.valueOf(count_Com);
            this.todayCom = String.valueOf(count_Com);
        }
    }

    public String getCount_sum() {
        return count_sum;
    }

    public void setCount_sum(String count_sum) {
        this.count_sum = count_sum;
    }
    
    public void setCount_sum(Integer count_sum) {
        if (count_sum != null) {
            this.count_sum = String.valueOf(count_sum);
        }
    }

    public String getProcessing_rate() {
        return processing_rate;
    }

    public void setProcessing_rate(String processing_rate) {
        this.processing_rate = processing_rate;
    }

    public void setProcessing_rate(Double processing_rate) {
        if (processing_rate != null) {
            this.processing_rate = String.valueOf(processing_rate);
        }
    }

    public String getDailyPoint() {
        return dailyPoint;
    }

    public void setDailyPoint(String dailyPoint) {
        this.dailyPoint = dailyPoint;
    }

    public int getDailyPointN() {
        return dailyPointN;
    }

    public void setDailyPointN(int dailyPointN) {
        this.dailyPointN = dailyPointN;
    }

    public String getCs_type() {
        return cs_type;
    }

    public void setCs_type(String cs_type) {
        this.cs_type = cs_type;
    }

    public String getTodayMiss() {
        return todayMiss;
    }

    public void setTodayMiss(String todayMiss) {
        this.todayMiss = todayMiss;
    }

    public void setTodayMiss(Integer todayMiss) {
        if (todayMiss != null) {
            this.todayMiss = String.valueOf(todayMiss);
        }
    }

    public void setTodayMiss(Long todayMiss) {
        if (todayMiss != null) {
            this.todayMiss = String.valueOf(todayMiss);
        }
    }

    public String getTodayCom() {
        if (count_Com != null) {
            return count_Com;
        }
        return todayCom;
    }

    public void setTodayCom(String todayCom) {
        this.todayCom = todayCom;
        this.count_Com = todayCom;
    }

    public void setTodayCom(Integer todayCom) {
        if (todayCom != null) {
            this.todayCom = String.valueOf(todayCom);
            this.count_Com = String.valueOf(todayCom);
        }
    }

    public void setTodayCom(Long todayCom) {
        if (todayCom != null) {
            this.todayCom = String.valueOf(todayCom);
            this.count_Com = String.valueOf(todayCom);
        }
    }

    public String getTodayEme() {
        return todayEme;
    }

    public void setTodayEme(String todayEme) {
        this.todayEme = todayEme;
    }

    public void setTodayEme(Integer todayEme) {
        if (todayEme != null) {
            this.todayEme = String.valueOf(todayEme);
        }
    }

    public String getYesterdayEme() {
        return yesterdayEme;
    }

    public void setYesterdayEme(String yesterdayEme) {
        this.yesterdayEme = yesterdayEme;
    }

    public String getYesterdayCom() {
        return yesterdayCom;
    }

    public void setYesterdayCom(String yesterdayCom) {
        this.yesterdayCom = yesterdayCom;
    }

    public String getYesterdayMiss() {
        return yesterdayMiss;
    }

    public void setYesterdayMiss(String yesterdayMiss) {
        this.yesterdayMiss = yesterdayMiss;
    }



    public String getCs_Type_Point() {
        return cs_Type_Point;
    }

    public void setCs_Type_Point(String cs_Type_Point) {
        this.cs_Type_Point = cs_Type_Point;
    }

    public String getCs_Type_Count() {
        return cs_Type_Count;
    }

    public void setCs_Type_Count(String cs_Type_Count) {
        this.cs_Type_Count = cs_Type_Count;
    }


    public String getStatPeriod() {
        return statPeriod;
    }

    public void setStatPeriod(String statPeriod) {
        this.statPeriod = statPeriod;
    }

    public String getCs_Name() {
        return cs_Name;
    }

    public void setCs_Name(String cs_Name) {
        this.cs_Name = cs_Name;
    }

    public String getCs_Type_Percentage() {
        return cs_Type_Percentage;
    }

    public void setCs_Type_Percentage(String cs_Type_Percentage) {
        this.cs_Type_Percentage = cs_Type_Percentage;
    }

    public String getPrc_Date() {
        return prc_Date;
    }

    public void setPrc_Date(String prc_Date) {
        this.prc_Date = prc_Date;
        this.PRC_DATE = prc_Date;
    }

    @JsonIgnore
    public String getGUBN() {
        return GUBN;
    }

    @JsonIgnore
    public void setGUBN(String GUBN) {
        this.GUBN = GUBN;
        this.gubn = GUBN;
    }

    @JsonIgnore
    public String getPRC_DATE() {
        return PRC_DATE;
    }
    
    @JsonIgnore
    public void setPRC_DATE(String PRC_DATE) {
        this.PRC_DATE = PRC_DATE;
        this.prc_Date = PRC_DATE;
    }

    public int getHour_09() {
        return hour_09;
    }

    public void setHour_09(int hour_09) {
        this.hour_09 = hour_09;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_09 메서드
    public void setHour_09(String hour_09) {
        try {
            this.hour_09 = Integer.parseInt(hour_09);
        } catch (NumberFormatException e) {
            this.hour_09 = 0;
        }
    }

    public int getHour_10() {
        return hour_10;
    }

    public void setHour_10(int hour_10) {
        this.hour_10 = hour_10;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_10 메서드
    public void setHour_10(String hour_10) {
        try {
            this.hour_10 = Integer.parseInt(hour_10);
        } catch (NumberFormatException e) {
            this.hour_10 = 0;
        }
    }

    public int getHour_11() {
        return hour_11;
    }

    public void setHour_11(int hour_11) {
        this.hour_11 = hour_11;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_11 메서드
    public void setHour_11(String hour_11) {
        try {
            this.hour_11 = Integer.parseInt(hour_11);
        } catch (NumberFormatException e) {
            this.hour_11 = 0;
        }
    }

    public int getHour_12() {
        return hour_12;
    }

    public void setHour_12(int hour_12) {
        this.hour_12 = hour_12;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_12 메서드
    public void setHour_12(String hour_12) {
        try {
            this.hour_12 = Integer.parseInt(hour_12);
        } catch (NumberFormatException e) {
            this.hour_12 = 0;
        }
    }

    public int getHour_13() {
        return hour_13;
    }

    public void setHour_13(int hour_13) {
        this.hour_13 = hour_13;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_13 메서드
    public void setHour_13(String hour_13) {
        try {
            this.hour_13 = Integer.parseInt(hour_13);
        } catch (NumberFormatException e) {
            this.hour_13 = 0;
        }
    }

    public int getHour_14() {
        return hour_14;
    }

    public void setHour_14(int hour_14) {
        this.hour_14 = hour_14;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_14 메서드
    public void setHour_14(String hour_14) {
        try {
            this.hour_14 = Integer.parseInt(hour_14);
        } catch (NumberFormatException e) {
            this.hour_14 = 0;
        }
    }

    public int getHour_15() {
        return hour_15;
    }

    public void setHour_15(int hour_15) {
        this.hour_15 = hour_15;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_15 메서드
    public void setHour_15(String hour_15) {
        try {
            this.hour_15 = Integer.parseInt(hour_15);
        } catch (NumberFormatException e) {
            this.hour_15 = 0;
        }
    }

    public int getHour_16() {
        return hour_16;
    }

    public void setHour_16(int hour_16) {
        this.hour_16 = hour_16;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_16 메서드
    public void setHour_16(String hour_16) {
        try {
            this.hour_16 = Integer.parseInt(hour_16);
        } catch (NumberFormatException e) {
            this.hour_16 = 0;
        }
    }

    public int getHour_17() {
        return hour_17;
    }

    public void setHour_17(int hour_17) {
        this.hour_17 = hour_17;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_17 메서드
    public void setHour_17(String hour_17) {
        try {
            this.hour_17 = Integer.parseInt(hour_17);
        } catch (NumberFormatException e) {
            this.hour_17 = 0;
        }
    }

    public int getHour_18() {
        return hour_18;
    }

    public void setHour_18(int hour_18) {
        this.hour_18 = hour_18;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_18 메서드
    public void setHour_18(String hour_18) {
        try {
            this.hour_18 = Integer.parseInt(hour_18);
        } catch (NumberFormatException e) {
            this.hour_18 = 0;
        }
    }

    public int getHour_19() {
        return hour_19;
    }

    public void setHour_19(int hour_19) {
        this.hour_19 = hour_19;
    }
    
    // 추가: 문자열로 설정할 수 있는 setHour_19 메서드
    public void setHour_19(String hour_19) {
        try {
            this.hour_19 = Integer.parseInt(hour_19);
        } catch (NumberFormatException e) {
            this.hour_19 = 0;
        }
    }

    // 소문자 callSum getter
    public int getCallSum() {
        return callSum;
    }
    
    // 모든 타입을 처리할 수 있는 단일 setter
    public void setCallSum(Object callSum) {
        if (callSum == null) {
            this.callSum = 0;
            this.CALLSUM = 0;
            return;
        }
        
        if (callSum instanceof Number) {
            this.callSum = ((Number)callSum).intValue();
            this.CALLSUM = this.callSum;
        } else if (callSum instanceof String) {
            try {
                this.callSum = Integer.parseInt((String)callSum);
                this.CALLSUM = this.callSum;
            } catch (NumberFormatException e) {
                this.callSum = 0;
                this.CALLSUM = 0;
            }
        }
    }

    // 대문자 CALLSUM getter
    public int getCALLSUM() {
        return CALLSUM;
    }
    
    // 모든 타입을 처리할 수 있는 단일 setter
    public void setCALLSUM(Object CALLSUM) {
        if (CALLSUM == null) {
            this.CALLSUM = 0;
            this.callSum = 0;
            return;
        }
        
        if (CALLSUM instanceof Number) {
            this.CALLSUM = ((Number)CALLSUM).intValue();
            this.callSum = this.CALLSUM;
        } else if (CALLSUM instanceof String) {
            try {
                this.CALLSUM = Integer.parseInt((String)CALLSUM);
                this.callSum = this.CALLSUM;
            } catch (NumberFormatException e) {
                this.CALLSUM = 0;
                this.callSum = 0;
            }
        }
    }

    // 추가: Javascript 호환을 위한 문자열 버전 getter (JsonIgnore 제거)
    @JsonIgnore
    public String getcallSum() {
        return String.valueOf(callSum);
    }
    
    @JsonIgnore
    public String getcallsum() {
        return String.valueOf(callSum);
    }

    public String getPersonMonth() {
        return personMonth;
    }

    public void setPersonMonth(String personMonth) {
        this.personMonth = personMonth;
    }

    public int getNewPersonCount() {
        return newPersonCount;
    }

    public void setNewPersonCount(int newPersonCount) {
        this.newPersonCount = newPersonCount;
    }

    public int getOldPersonCount() {
        return oldPersonCount;
    }

    public void setOldPersonCount(int oldPersonCount){
        this.oldPersonCount = oldPersonCount;
    }

    public int getThisMonth() {
        return thisMonth;
    }

    public void setThisMonth(int thisMonth) {
        this.thisMonth = thisMonth;
    }

    public int getPreviousMonth() {
        return previousMonth;
    }

    public void setPreviousMonth(int previousMonth) {
        this.previousMonth = previousMonth;
    }

    public int getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(int percentChange) {
        this.percentChange = percentChange;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public int getAvg1week() {
        return avg1week;
    }

    public void setAvg1week(int avg1week) {
        this.avg1week = avg1week;
    }

    public int getAvg1month() {
        return avg1month;
    }

    public void setAvg1month(int avg1month) {
        this.avg1month = avg1month;
    }

    public int getAvg3months() {
        return avg3months;
    }

    public void setAvg3months(int avg3months) {
        this.avg3months = avg3months;
    }

    public int getAvg6months() {
        return avg6months;
    }

    public void setAvg6months(int avg6months) {
        this.avg6months = avg6months;
    }

    public int getAvg9months() {
        return avg9months;
    }

    public void setAvg9months(int avg9months) {
        this.avg9months = avg9months;
    }

    public int getAvg12months() {
        return avg12months;
    }

    public void setAvg12months(int avg12months) {
        this.avg12months = avg12months;
    }

    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getGubn() {
        return gubn;
    }

    public void setGubn(String gubn) {
        this.gubn = gubn;
        this.GUBN = gubn;
    }
}
