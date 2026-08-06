package com.wio.crm.scheduler;

import com.wio.crm.model.AiReport;
import com.wio.crm.model.AiReportSubscription;
import com.wio.crm.service.AiReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * AI 리포트 자동 생성 스케줄러
 * 
 * 매월 1일 오전 9시에 전월 리포트를 자동 생성합니다.
 */
@Component
public class AiReportScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(AiReportScheduler.class);
    
    @Autowired
    private AiReportService aiReportService;
    
    @Value("${ai.report.scheduler.enabled:false}")
    private boolean schedulerEnabled;
    
    /**
     * 월간 리포트 자동 생성
     * 매월 1일 오전 9시 실행
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 9 1 * ?")
    public void generateMonthlyReports() {
        if (!schedulerEnabled) {
            log.info("AI 리포트 스케줄러가 비활성화되어 있습니다.");
            return;
        }
        
        log.info("===== 월간 AI 리포트 자동 생성 시작 =====");
        
        // 전월 계산
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        String targetMonth = new SimpleDateFormat("yyyyMM").format(cal.getTime());
        
        log.info("대상 월: {}", targetMonth);
        
        // 활성 구독 조회
        List<AiReportSubscription> activeSubscriptions = aiReportService.getActiveSubscriptions();
        log.info("활성 구독 수: {}", activeSubscriptions.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (AiReportSubscription subscription : activeSubscriptions) {
            try {
                log.info("리포트 생성 시작: 업체={}", subscription.getCustCode());
                
                // 리포트 생성
                AiReport report = aiReportService.requestMonthlyReport(
                    subscription.getCustCode(), 
                    targetMonth
                );
                
                // AI 분석 실행
                aiReportService.processReport(report.getReportId());
                
                successCount++;
                log.info("리포트 생성 완료: 업체={}, reportId={}", 
                        subscription.getCustCode(), report.getReportId());
                
                // API 부하 방지를 위한 대기
                Thread.sleep(5000);
                
            } catch (Exception e) {
                failCount++;
                log.error("리포트 생성 실패: 업체={}", subscription.getCustCode(), e);
            }
        }
        
        log.info("===== 월간 AI 리포트 자동 생성 완료 =====");
        log.info("성공: {}, 실패: {}", successCount, failCount);
    }
    
    /**
     * 대기 중인 리포트 처리
     * 매시간 정각에 실행
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processPendingReports() {
        if (!schedulerEnabled) {
            return;
        }
        
        log.info("대기 중인 리포트 처리 시작");
        
        // 대기 중인 리포트 조회 및 처리
        // 실제 구현 시 aiReportService.findPendingReports() 등 사용
    }
    
    /**
     * 만료된 구독 처리
     * 매일 자정에 실행
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void processExpiredSubscriptions() {
        if (!schedulerEnabled) {
            return;
        }
        
        log.info("만료 구독 처리 시작");
        
        // 만료된 구독 상태 업데이트
        // 자동 갱신 처리 등
    }
}

