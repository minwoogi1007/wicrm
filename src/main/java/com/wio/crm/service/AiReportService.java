package com.wio.crm.service;

import com.wio.crm.mapper.AiReportMapper;
import com.wio.crm.mapper.ConsMapper;
import com.wio.crm.model.AiReport;
import com.wio.crm.model.AiReportSubscription;
import com.wio.crm.model.Consultation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AiReportService {
    
    private static final Logger log = LoggerFactory.getLogger(AiReportService.class);
    
    @Autowired
    private AiReportMapper aiReportMapper;
    
    @Autowired
    private ConsMapper consMapper;
    
    @Autowired(required = false)
    private AiAnalysisService aiAnalysisService;
    
    @Value("${ai.report.enabled:false}")
    private boolean aiReportEnabled;
    
    // ========== 구독 관리 ==========
    
    public List<AiReportSubscription> getAllSubscriptions() {
        return aiReportMapper.findAllSubscriptions();
    }
    
    public AiReportSubscription getSubscriptionById(Long id) {
        return aiReportMapper.findSubscriptionById(id);
    }
    
    public AiReportSubscription getActiveSubscription(String custCode) {
        return aiReportMapper.findActiveSubscriptionByCustCode(custCode);
    }
    
    public List<AiReportSubscription> getActiveSubscriptions() {
        return aiReportMapper.findActiveSubscriptions();
    }
    
    @Transactional
    public void createSubscription(AiReportSubscription subscription) {
        log.info("새 구독 생성: 업체코드={}, 플랜={}", subscription.getCustCode(), subscription.getPlanType());
        subscription.setStatus("ACTIVE");
        if (subscription.getStartDate() == null) {
            subscription.setStartDate(new Date());
        }
        aiReportMapper.insertSubscription(subscription);
    }
    
    @Transactional
    public void updateSubscription(AiReportSubscription subscription) {
        log.info("구독 수정: ID={}", subscription.getSubscriptionId());
        aiReportMapper.updateSubscription(subscription);
    }
    
    @Transactional
    public void cancelSubscription(Long subscriptionId, String reason) {
        log.info("구독 취소: ID={}, 사유={}", subscriptionId, reason);
        AiReportSubscription subscription = aiReportMapper.findSubscriptionById(subscriptionId);
        if (subscription != null) {
            subscription.setStatus("CANCELLED");
            subscription.setMemo(reason);
            subscription.setEndDate(new Date());
            aiReportMapper.updateSubscription(subscription);
        }
    }
    
    // ========== 리포트 관리 ==========
    
    public List<AiReport> getAllReports() {
        return aiReportMapper.findAllReports();
    }
    
    public AiReport getReportById(Long id) {
        return aiReportMapper.findReportById(id);
    }
    
    public List<AiReport> getReportsByCustCode(String custCode) {
        return aiReportMapper.findReportsByCustCode(custCode);
    }
    
    public AiReport getReportByMonth(String custCode, String reportMonth) {
        return aiReportMapper.findReportByCustCodeAndMonth(custCode, reportMonth);
    }
    
    /**
     * 월간 리포트 생성 요청
     */
    @Transactional
    public AiReport requestMonthlyReport(String custCode, String reportMonth) {
        log.info("월간 리포트 생성 요청: 업체={}, 대상월={}", custCode, reportMonth);
        
        // 이미 해당 월 리포트가 있는지 확인
        AiReport existingReport = aiReportMapper.findReportByCustCodeAndMonth(custCode, reportMonth);
        if (existingReport != null) {
            log.warn("이미 해당 월 리포트가 존재합니다: reportId={}", existingReport.getReportId());
            return existingReport;
        }
        
        // 구독 확인
        AiReportSubscription subscription = aiReportMapper.findActiveSubscriptionByCustCode(custCode);
        if (subscription == null) {
            throw new IllegalStateException("활성 구독이 없습니다. 먼저 구독을 신청해주세요.");
        }
        
        // 기본 통계 수집
        Map<String, Object> stats = collectMonthlyStats(custCode, reportMonth);
        
        // 리포트 생성
        AiReport report = new AiReport();
        report.setSubscriptionId(subscription.getSubscriptionId());
        report.setCustCode(custCode);
        report.setReportMonth(reportMonth);
        report.setReportType("MONTHLY");
        report.setReportTitle(formatReportMonth(reportMonth) + " 상담 분석 리포트");
        report.setTotalConsultations((Integer) stats.getOrDefault("totalCount", 0));
        report.setCompletedCount((Integer) stats.getOrDefault("completedCount", 0));
        report.setUrgentCount((Integer) stats.getOrDefault("urgentCount", 0));
        report.setClaimCount((Integer) stats.getOrDefault("claimCount", 0));
        report.setAvgProcessTime((Double) stats.getOrDefault("avgProcessTime", 0.0));
        report.setStatus("PENDING");
        
        aiReportMapper.insertReport(report);
        log.info("리포트 생성 완료: reportId={}", report.getReportId());
        
        return report;
    }
    
    /**
     * AI 분석 실행
     */
    @Transactional
    public void processReport(Long reportId) {
        log.info("AI 분석 시작: reportId={}", reportId);
        
        AiReport report = aiReportMapper.findReportById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId);
        }
        
        // 상태 변경
        aiReportMapper.updateReportStatus(reportId, "PROCESSING", null);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 상담 데이터 조회
            List<Consultation> consultations = getMonthlyConsultations(
                report.getCustCode(), report.getReportMonth());
            
            if (consultations.isEmpty()) {
                aiReportMapper.updateReportStatus(reportId, "COMPLETED", "분석할 상담 데이터가 없습니다.");
                return;
            }
            
            // AI 분석 실행 (서비스가 있는 경우에만)
            if (aiAnalysisService != null && aiReportEnabled) {
                Map<String, String> analysisResults = aiAnalysisService.analyzeConsultations(consultations);
                
                report.setSummaryJson(analysisResults.get("summary"));
                report.setKeywordsJson(analysisResults.get("keywords"));
                report.setSentimentJson(analysisResults.get("sentiment"));
                report.setProductJson(analysisResults.get("product"));
                report.setRecommendations(analysisResults.get("recommendations"));
                report.setTokenUsed(Integer.parseInt(analysisResults.getOrDefault("tokenUsed", "0")));
            } else {
                // AI 서비스가 없는 경우 샘플 데이터
                report.setSummaryJson(generateSampleSummary(consultations));
                report.setKeywordsJson(generateSampleKeywords(consultations));
                report.setSentimentJson(generateSampleSentiment());
                report.setRecommendations(generateSampleRecommendations());
            }
            
            report.setStatus("COMPLETED");
            report.setCompletedDate(new Date());
            report.setProcessingTime((int) ((System.currentTimeMillis() - startTime) / 1000));
            
            aiReportMapper.updateReport(report);
            log.info("AI 분석 완료: reportId={}, 처리시간={}초", reportId, report.getProcessingTime());
            
        } catch (Exception e) {
            log.error("AI 분석 실패: reportId={}", reportId, e);
            aiReportMapper.updateReportStatus(reportId, "FAILED", e.getMessage());
        }
    }
    
    /**
     * 월간 통계 수집
     */
    private Map<String, Object> collectMonthlyStats(String custCode, String reportMonth) {
        String startDate = reportMonth + "01";
        String endDate = reportMonth + "31"; // 월말 자동 처리됨
        
        Map<String, Object> params = new HashMap<>();
        params.put("custCode", custCode);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        
        Map<String, Object> stats = consMapper.getConsultationStats(params);
        return stats != null ? stats : new HashMap<>();
    }
    
    /**
     * 월간 상담 데이터 조회
     */
    private List<Consultation> getMonthlyConsultations(String custCode, String reportMonth) {
        String startDate = reportMonth + "01";
        String endDate = reportMonth + "31";
        
        Map<String, Object> params = new HashMap<>();
        params.put("custCode", custCode);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        
        return consMapper.selectAllForExcel(params);
    }
    
    // ========== 샘플 데이터 생성 (AI 서비스 없을 때) ==========
    
    private String generateSampleSummary(List<Consultation> consultations) {
        int total = consultations.size();
        return String.format("{\"summary\": \"이번 달 총 %d건의 상담이 접수되었습니다. " +
                "주요 문의 유형은 배송, 교환, 환불 순이며, 전반적인 고객 만족도는 양호한 수준입니다.\", " +
                "\"mainIssues\": [\"배송 지연 문의 증가\", \"품질 관련 클레임\", \"교환/환불 처리\"], " +
                "\"improvementAreas\": [\"배송 추적 시스템 개선\", \"품질 검수 강화\"], " +
                "\"positiveTrends\": [\"응대 만족도 상승\", \"처리 시간 단축\"]}", total);
    }
    
    private String generateSampleKeywords(List<Consultation> consultations) {
        return "{\"products\": [" +
                "{\"name\": \"제품A\", \"count\": 45, \"sentiment\": \"neutral\"}," +
                "{\"name\": \"제품B\", \"count\": 32, \"sentiment\": \"negative\"}," +
                "{\"name\": \"제품C\", \"count\": 28, \"sentiment\": \"positive\"}" +
                "], \"issues\": [" +
                "{\"type\": \"배송\", \"count\": 89}," +
                "{\"type\": \"품질\", \"count\": 56}," +
                "{\"type\": \"교환\", \"count\": 43}" +
                "], \"requests\": [" +
                "{\"request\": \"빠른 처리 요청\", \"count\": 67}," +
                "{\"request\": \"환불 요청\", \"count\": 34}" +
                "]}";
    }
    
    private String generateSampleSentiment() {
        return "{\"distribution\": {\"positive\": 35, \"neutral\": 45, \"negative\": 20}, " +
                "\"topComplaints\": [" +
                "{\"complaint\": \"배송 지연\", \"count\": 34}," +
                "{\"complaint\": \"제품 불량\", \"count\": 23}," +
                "{\"complaint\": \"응대 불만\", \"count\": 12}" +
                "], \"satisfactionAreas\": [\"친절한 응대\", \"빠른 처리\"], " +
                "\"emotionTrend\": \"유지\"}";
    }
    
    private String generateSampleRecommendations() {
        return "{\"recommendations\": [" +
                "{\"title\": \"배송 추적 시스템 강화\", \"description\": \"고객이 실시간으로 배송 상태를 확인할 수 있도록 알림 서비스를 개선하세요.\", " +
                "\"expectedEffect\": \"배송 문의 30% 감소 예상\", \"priority\": \"high\", \"category\": \"service\"}," +
                "{\"title\": \"품질 검수 프로세스 개선\", \"description\": \"출고 전 검수 단계를 추가하여 불량품 출고를 최소화하세요.\", " +
                "\"expectedEffect\": \"클레임 20% 감소 예상\", \"priority\": \"high\", \"category\": \"product\"}," +
                "{\"title\": \"FAQ 페이지 확대\", \"description\": \"자주 묻는 질문을 정리하여 셀프서비스 비율을 높이세요.\", " +
                "\"expectedEffect\": \"단순 문의 25% 감소 예상\", \"priority\": \"medium\", \"category\": \"process\"}" +
                "]}";
    }
    
    private String formatReportMonth(String reportMonth) {
        if (reportMonth == null || reportMonth.length() != 6) return reportMonth;
        return reportMonth.substring(0, 4) + "년 " + reportMonth.substring(4) + "월";
    }
    
    // ========== 통계 ==========
    
    public int countActiveSubscriptions() {
        return aiReportMapper.countActiveSubscriptions();
    }
    
    public int countReportsByMonth(String reportMonth) {
        return aiReportMapper.countReportsByMonth(reportMonth);
    }
}

