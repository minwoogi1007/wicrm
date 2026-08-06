package com.wio.crm.mapper;

import com.wio.crm.model.AiReport;
import com.wio.crm.model.AiReportSubscription;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiReportMapper {
    
    // ========== 구독 관련 ==========
    
    @Select("SELECT s.*, " +
            "(SELECT CUST_NAME FROM TCST01 WHERE CUST_CODE = s.CUST_CODE) as CUST_NAME " +
            "FROM AI_REPORT_SUBSCRIPTION s ORDER BY s.IN_DATE DESC")
    @Results(id = "subscriptionResultMap", value = {
        @Result(property = "subscriptionId", column = "SUBSCRIPTION_ID"),
        @Result(property = "custCode", column = "CUST_CODE"),
        @Result(property = "planType", column = "PLAN_TYPE"),
        @Result(property = "monthlyFee", column = "MONTHLY_FEE"),
        @Result(property = "startDate", column = "START_DATE"),
        @Result(property = "endDate", column = "END_DATE"),
        @Result(property = "autoRenewal", column = "AUTO_RENEWAL"),
        @Result(property = "paymentMethod", column = "PAYMENT_METHOD"),
        @Result(property = "contactEmail", column = "CONTACT_EMAIL"),
        @Result(property = "contactName", column = "CONTACT_NAME"),
        @Result(property = "contactPhone", column = "CONTACT_PHONE"),
        @Result(property = "inDate", column = "IN_DATE"),
        @Result(property = "inEmpno", column = "IN_EMPNO"),
        @Result(property = "upDate", column = "UP_DATE"),
        @Result(property = "upEmpno", column = "UP_EMPNO"),
        @Result(property = "custName", column = "CUST_NAME")
    })
    List<AiReportSubscription> findAllSubscriptions();
    
    @Select("SELECT s.*, " +
            "(SELECT CUST_NAME FROM TCST01 WHERE CUST_CODE = s.CUST_CODE) as CUST_NAME " +
            "FROM AI_REPORT_SUBSCRIPTION s WHERE s.SUBSCRIPTION_ID = #{id}")
    @ResultMap("subscriptionResultMap")
    AiReportSubscription findSubscriptionById(Long id);
    
    @Select("SELECT s.*, " +
            "(SELECT CUST_NAME FROM TCST01 WHERE CUST_CODE = s.CUST_CODE) as CUST_NAME " +
            "FROM AI_REPORT_SUBSCRIPTION s WHERE s.CUST_CODE = #{custCode} AND s.STATUS = 'ACTIVE'")
    @ResultMap("subscriptionResultMap")
    AiReportSubscription findActiveSubscriptionByCustCode(String custCode);
    
    @Select("SELECT * FROM AI_REPORT_SUBSCRIPTION WHERE STATUS = 'ACTIVE' " +
            "AND (END_DATE IS NULL OR END_DATE >= SYSDATE)")
    List<AiReportSubscription> findActiveSubscriptions();
    
    @Insert("INSERT INTO AI_REPORT_SUBSCRIPTION " +
            "(CUST_CODE, PLAN_TYPE, STATUS, MONTHLY_FEE, START_DATE, END_DATE, " +
            "AUTO_RENEWAL, PAYMENT_METHOD, CONTACT_EMAIL, CONTACT_NAME, CONTACT_PHONE, " +
            "IN_EMPNO, MEMO) " +
            "VALUES (#{custCode}, #{planType}, #{status}, #{monthlyFee}, #{startDate}, #{endDate}, " +
            "#{autoRenewal}, #{paymentMethod}, #{contactEmail}, #{contactName}, #{contactPhone}, " +
            "#{inEmpno}, #{memo})")
    @Options(useGeneratedKeys = true, keyProperty = "subscriptionId", keyColumn = "SUBSCRIPTION_ID")
    void insertSubscription(AiReportSubscription subscription);
    
    @Update("UPDATE AI_REPORT_SUBSCRIPTION SET " +
            "PLAN_TYPE = #{planType}, STATUS = #{status}, MONTHLY_FEE = #{monthlyFee}, " +
            "START_DATE = #{startDate}, END_DATE = #{endDate}, AUTO_RENEWAL = #{autoRenewal}, " +
            "PAYMENT_METHOD = #{paymentMethod}, CONTACT_EMAIL = #{contactEmail}, " +
            "CONTACT_NAME = #{contactName}, CONTACT_PHONE = #{contactPhone}, " +
            "UP_DATE = SYSDATE, UP_EMPNO = #{upEmpno}, MEMO = #{memo} " +
            "WHERE SUBSCRIPTION_ID = #{subscriptionId}")
    void updateSubscription(AiReportSubscription subscription);
    
    @Delete("DELETE FROM AI_REPORT_SUBSCRIPTION WHERE SUBSCRIPTION_ID = #{id}")
    void deleteSubscription(Long id);
    
    // ========== 리포트 관련 ==========
    
    @Select("SELECT r.*, " +
            "(SELECT CUST_NAME FROM TCST01 WHERE CUST_CODE = r.CUST_CODE) as CUST_NAME, " +
            "(SELECT PLAN_TYPE FROM AI_REPORT_SUBSCRIPTION WHERE SUBSCRIPTION_ID = r.SUBSCRIPTION_ID) as PLAN_TYPE " +
            "FROM AI_REPORT r ORDER BY r.IN_DATE DESC")
    @Results(id = "reportResultMap", value = {
        @Result(property = "reportId", column = "REPORT_ID"),
        @Result(property = "subscriptionId", column = "SUBSCRIPTION_ID"),
        @Result(property = "custCode", column = "CUST_CODE"),
        @Result(property = "reportMonth", column = "REPORT_MONTH"),
        @Result(property = "reportType", column = "REPORT_TYPE"),
        @Result(property = "reportTitle", column = "REPORT_TITLE"),
        @Result(property = "totalConsultations", column = "TOTAL_CONSULTATIONS"),
        @Result(property = "completedCount", column = "COMPLETED_COUNT"),
        @Result(property = "urgentCount", column = "URGENT_COUNT"),
        @Result(property = "claimCount", column = "CLAIM_COUNT"),
        @Result(property = "avgProcessTime", column = "AVG_PROCESS_TIME"),
        @Result(property = "summaryJson", column = "SUMMARY_JSON"),
        @Result(property = "keywordsJson", column = "KEYWORDS_JSON"),
        @Result(property = "sentimentJson", column = "SENTIMENT_JSON"),
        @Result(property = "productJson", column = "PRODUCT_JSON"),
        @Result(property = "trendJson", column = "TREND_JSON"),
        @Result(property = "recommendations", column = "RECOMMENDATIONS"),
        @Result(property = "pdfPath", column = "PDF_PATH"),
        @Result(property = "excelPath", column = "EXCEL_PATH"),
        @Result(property = "errorMessage", column = "ERROR_MESSAGE"),
        @Result(property = "tokenUsed", column = "TOKEN_USED"),
        @Result(property = "processingTime", column = "PROCESSING_TIME"),
        @Result(property = "emailSent", column = "EMAIL_SENT"),
        @Result(property = "emailSentDate", column = "EMAIL_SENT_DATE"),
        @Result(property = "inDate", column = "IN_DATE"),
        @Result(property = "completedDate", column = "COMPLETED_DATE"),
        @Result(property = "custName", column = "CUST_NAME"),
        @Result(property = "planType", column = "PLAN_TYPE")
    })
    List<AiReport> findAllReports();
    
    @Select("SELECT r.*, " +
            "(SELECT CUST_NAME FROM TCST01 WHERE CUST_CODE = r.CUST_CODE) as CUST_NAME " +
            "FROM AI_REPORT r WHERE r.REPORT_ID = #{id}")
    @ResultMap("reportResultMap")
    AiReport findReportById(Long id);
    
    @Select("SELECT r.* FROM AI_REPORT r " +
            "WHERE r.CUST_CODE = #{custCode} ORDER BY r.REPORT_MONTH DESC")
    @ResultMap("reportResultMap")
    List<AiReport> findReportsByCustCode(String custCode);
    
    @Select("SELECT r.* FROM AI_REPORT r " +
            "WHERE r.CUST_CODE = #{custCode} AND r.REPORT_MONTH = #{reportMonth}")
    @ResultMap("reportResultMap")
    AiReport findReportByCustCodeAndMonth(@Param("custCode") String custCode, 
                                           @Param("reportMonth") String reportMonth);
    
    @Select("SELECT r.* FROM AI_REPORT r WHERE r.STATUS = 'PENDING'")
    @ResultMap("reportResultMap")
    List<AiReport> findPendingReports();
    
    @Insert("INSERT INTO AI_REPORT " +
            "(SUBSCRIPTION_ID, CUST_CODE, REPORT_MONTH, REPORT_TYPE, REPORT_TITLE, " +
            "TOTAL_CONSULTATIONS, COMPLETED_COUNT, URGENT_COUNT, CLAIM_COUNT, AVG_PROCESS_TIME, " +
            "STATUS) " +
            "VALUES (#{subscriptionId}, #{custCode}, #{reportMonth}, #{reportType}, #{reportTitle}, " +
            "#{totalConsultations}, #{completedCount}, #{urgentCount}, #{claimCount}, #{avgProcessTime}, " +
            "#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "reportId", keyColumn = "REPORT_ID")
    void insertReport(AiReport report);
    
    @Update("UPDATE AI_REPORT SET " +
            "SUMMARY_JSON = #{summaryJson}, KEYWORDS_JSON = #{keywordsJson}, " +
            "SENTIMENT_JSON = #{sentimentJson}, PRODUCT_JSON = #{productJson}, " +
            "TREND_JSON = #{trendJson}, RECOMMENDATIONS = #{recommendations}, " +
            "PDF_PATH = #{pdfPath}, EXCEL_PATH = #{excelPath}, " +
            "STATUS = #{status}, ERROR_MESSAGE = #{errorMessage}, " +
            "TOKEN_USED = #{tokenUsed}, PROCESSING_TIME = #{processingTime}, " +
            "COMPLETED_DATE = #{completedDate} " +
            "WHERE REPORT_ID = #{reportId}")
    void updateReport(AiReport report);
    
    @Update("UPDATE AI_REPORT SET STATUS = #{status}, ERROR_MESSAGE = #{errorMessage} " +
            "WHERE REPORT_ID = #{reportId}")
    void updateReportStatus(@Param("reportId") Long reportId, 
                            @Param("status") String status, 
                            @Param("errorMessage") String errorMessage);
    
    @Update("UPDATE AI_REPORT SET EMAIL_SENT = 'Y', EMAIL_SENT_DATE = SYSDATE " +
            "WHERE REPORT_ID = #{reportId}")
    void updateEmailSent(Long reportId);
    
    @Delete("DELETE FROM AI_REPORT WHERE REPORT_ID = #{id}")
    void deleteReport(Long id);
    
    // ========== 통계 관련 ==========
    
    @Select("SELECT COUNT(*) FROM AI_REPORT_SUBSCRIPTION WHERE STATUS = 'ACTIVE'")
    int countActiveSubscriptions();
    
    @Select("SELECT COUNT(*) FROM AI_REPORT WHERE REPORT_MONTH = #{reportMonth}")
    int countReportsByMonth(String reportMonth);
    
    @Select("SELECT SUM(TOKEN_USED) FROM AI_REPORT WHERE REPORT_MONTH = #{reportMonth}")
    Integer sumTokenUsedByMonth(String reportMonth);
}

