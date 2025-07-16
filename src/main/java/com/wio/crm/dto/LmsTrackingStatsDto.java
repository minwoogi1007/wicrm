package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * LMS 추적 통계 DTO
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LmsTrackingStatsDto {
    
    // 기본 통계
    private Integer totalSent;              // 총 문자 발송 건수
    private Integer recontacted;            // 재연락 건수
    private Double recontactRate;           // 재연락률 (%)
    private Integer callSuccess;            // 통화 성공 건수
    private Double callSuccessRate;         // 통화 성공률 (%)
    private Double avgResponseTime;         // 평균 응답시간 (시간)
    
    // 시간대별 분석
    private TimeAnalysis timeAnalysis;
    
    // 상담원별 성과
    private List<AgentStats> agentStats;
    
    // 일별 트렌드 (최근 7일)
    private List<DailyTrend> dailyTrends;
    
    // 메시지 유형별 분석
    private List<MessageTypeStats> messageTypeStats;
    
    /**
     * 시간대별 분석 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeAnalysis {
        private TimeSlotStats morning;      // 오전 (08:00-12:00)
        private TimeSlotStats afternoon;    // 오후 (12:00-18:00)
        private TimeSlotStats evening;      // 저녁 (18:00-22:00)
    }
    
    /**
     * 시간대별 세부 통계
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlotStats {
        private Integer sent;           // 발송 건수
        private Integer recontacted;    // 재연락 건수
        private Double rate;            // 재연락률 (%)
    }
    
    /**
     * 상담원별 성과 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentStats {
        private String agentName;       // 상담원명
        private String empno;           // 직원번호
        private Integer sent;           // 발송 건수
        private Integer recontacted;    // 재연락 건수
        private Double rate;            // 재연락률 (%)
        private Integer callSuccess;    // 통화 성공 건수
        private Double callSuccessRate; // 통화 성공률 (%)
        private Double avgResponseTime; // 평균 응답시간 (시간)
    }
    
    /**
     * 일별 트렌드 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyTrend {
        private String date;            // 날짜 (YYYY-MM-DD)
        private Integer sent;           // 발송 건수
        private Integer recontacted;    // 재연락 건수
        private Double rate;            // 재연락률 (%)
        private Integer callSuccess;    // 통화 성공 건수
    }
    
    /**
     * 메시지 유형별 통계 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageTypeStats {
        private String messageType;     // 메시지 유형 (상담요청, 재상담안내 등)
        private Integer sent;           // 발송 건수
        private Integer recontacted;    // 재연락 건수
        private Double rate;            // 재연락률 (%)
    }
    
    /**
     * 재연락률 계산
     */
    public void calculateRecontactRate() {
        if (totalSent != null && totalSent > 0 && recontacted != null) {
            this.recontactRate = Math.round((double) recontacted / totalSent * 100 * 10.0) / 10.0;
        } else {
            this.recontactRate = 0.0;
        }
    }
    
    /**
     * 통화 성공률 계산
     */
    public void calculateCallSuccessRate() {
        if (recontacted != null && recontacted > 0 && callSuccess != null) {
            this.callSuccessRate = Math.round((double) callSuccess / recontacted * 100 * 10.0) / 10.0;
        } else {
            this.callSuccessRate = 0.0;
        }
    }
    
    /**
     * 모든 비율 계산
     */
    public void calculateAllRates() {
        calculateRecontactRate();
        calculateCallSuccessRate();
        
        // 상담원별 통계의 비율 계산
        if (agentStats != null) {
            agentStats.forEach(agent -> {
                if (agent.getSent() != null && agent.getSent() > 0 && agent.getRecontacted() != null) {
                    agent.setRate(Math.round((double) agent.getRecontacted() / agent.getSent() * 100 * 10.0) / 10.0);
                }
                if (agent.getRecontacted() != null && agent.getRecontacted() > 0 && agent.getCallSuccess() != null) {
                    agent.setCallSuccessRate(Math.round((double) agent.getCallSuccess() / agent.getRecontacted() * 100 * 10.0) / 10.0);
                }
            });
        }
        
        // 일별 트렌드의 비율 계산
        if (dailyTrends != null) {
            dailyTrends.forEach(trend -> {
                if (trend.getSent() != null && trend.getSent() > 0 && trend.getRecontacted() != null) {
                    trend.setRate(Math.round((double) trend.getRecontacted() / trend.getSent() * 100 * 10.0) / 10.0);
                }
            });
        }
        
        // 메시지 유형별 통계의 비율 계산
        if (messageTypeStats != null) {
            messageTypeStats.forEach(msgType -> {
                if (msgType.getSent() != null && msgType.getSent() > 0 && msgType.getRecontacted() != null) {
                    msgType.setRate(Math.round((double) msgType.getRecontacted() / msgType.getSent() * 100 * 10.0) / 10.0);
                }
            });
        }
        
        // 시간대별 분석의 비율 계산
        if (timeAnalysis != null) {
            calculateTimeSlotRate(timeAnalysis.getMorning());
            calculateTimeSlotRate(timeAnalysis.getAfternoon());
            calculateTimeSlotRate(timeAnalysis.getEvening());
        }
    }
    
    /**
     * 시간대 통계 비율 계산 헬퍼 메소드
     */
    private void calculateTimeSlotRate(TimeSlotStats timeSlot) {
        if (timeSlot != null && timeSlot.getSent() != null && timeSlot.getSent() > 0 && timeSlot.getRecontacted() != null) {
            timeSlot.setRate(Math.round((double) timeSlot.getRecontacted() / timeSlot.getSent() * 100 * 10.0) / 10.0);
        }
    }
    
    /**
     * 기본값으로 0 설정
     */
    public void setDefaultZeros() {
        if (totalSent == null) totalSent = 0;
        if (recontacted == null) recontacted = 0;
        if (callSuccess == null) callSuccess = 0;
        if (recontactRate == null) recontactRate = 0.0;
        if (callSuccessRate == null) callSuccessRate = 0.0;
        if (avgResponseTime == null) avgResponseTime = 0.0;
    }
} 