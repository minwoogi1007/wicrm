package com.wio.crm.service.impl;

import com.wio.crm.dto.*;
import com.wio.crm.mapper.ExchangeStatsMapper;
import com.wio.crm.service.ExchangeStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 🚀 교환반품 통계 서비스 구현체 (신규 설계)
 * 
 * 특징:
 * - TB_RETURN_ITEM 테이블 기준 단순한 통계 조회
 * - 복잡한 로직 최소화, 명확한 SQL 쿼리 사용
 * - 에러 처리 및 로깅 강화
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeStatsServiceImpl implements ExchangeStatsService {

    private final ExchangeStatsMapper exchangeStatsMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public ExchangeStatsResponseDto getExchangeStats(ExchangeStatsRequestDto requestDto) {
        log.info("📊 교환반품 통계 조회 시작 - 조건: {}", requestDto);
        long startTime = System.currentTimeMillis();
        
        try {
            ExchangeStatsResponseDto response = new ExchangeStatsResponseDto();
            
            // 1. 📈 핵심 지표 조회
            log.info("1️⃣ 핵심 지표 조회 시작...");
            ExchangeStatsData.CoreStats coreStats = exchangeStatsMapper.getCoreStats(requestDto);
            response.setSummary(convertToCoreStatsDto(coreStats, requestDto));
            log.info("✅ 핵심 지표 조회 완료 - 총 건수: {}", coreStats != null ? coreStats.getTotalCount() : 0);
            
            // 2. 📊 트렌드 데이터 조회
            log.info("2️⃣ 트렌드 데이터 조회 시작...");
            List<ExchangeStatsData.TrendPoint> trendPoints = exchangeStatsMapper.getTrendData(requestDto);
            response.setTrendData(convertToTrendDataDto(trendPoints));
            log.info("✅ 트렌드 데이터 조회 완료 - 데이터 포인트: {}", trendPoints != null ? trendPoints.size() : 0);
            
            // 3. 🥧 유형별 분포 조회  
            log.info("3️⃣ 유형별 분포 조회 시작...");
            List<ExchangeStatsData.TypeDistribution> typeDistributions = exchangeStatsMapper.getTypeDistribution(requestDto);
            response.setTypeDistribution(convertToTypeDistributionDto(typeDistributions, coreStats));
            log.info("✅ 유형별 분포 조회 완료 - 유형 수: {}", typeDistributions != null ? typeDistributions.size() : 0);
            
            // 4. 🍩 상태별 분포 조회
            log.info("4️⃣ 상태별 분포 조회 시작...");
            List<ExchangeStatsData.StatusDistribution> statusDistributions = exchangeStatsMapper.getStatusDistribution(requestDto);
            response.setStatusDistribution(convertToStatusDistributionDto(statusDistributions, coreStats));
            log.info("✅ 상태별 분포 조회 완료 - 상태 수: {}", statusDistributions != null ? statusDistributions.size() : 0);
            
            // 5. 📋 사유별 분석 조회
            log.info("5️⃣ 사유별 분석 조회 시작...");
            List<ExchangeStatsData.ReasonAnalysis> reasonAnalyses = exchangeStatsMapper.getReasonAnalysis(requestDto);
            response.setReasonAnalysis(convertToReasonAnalysisDto(reasonAnalyses, coreStats));
            log.info("✅ 사유별 분석 조회 완료 - 사유 수: {}", reasonAnalyses != null ? reasonAnalyses.size() : 0);
            
            // 6. 🏢 사이트별 성과 조회
            log.info("6️⃣ 사이트별 성과 조회 시작...");
            List<ExchangeStatsData.SitePerformance> sitePerformances = exchangeStatsMapper.getSitePerformance(requestDto);
            response.setSitePerformance(convertToSitePerformanceDto(sitePerformances, coreStats));
            log.info("✅ 사이트별 성과 조회 완료 - 사이트 수: {}", sitePerformances != null ? sitePerformances.size() : 0);
            
            // 7. 📝 메타 정보 설정
            setMetaInfo(response, requestDto, coreStats);
            
            long endTime = System.currentTimeMillis();
            log.info("🎉 교환반품 통계 조회 완료 - 총 소요시간: {}ms, 전체 레코드: {}", 
                    (endTime - startTime), response.getTotalRecords());
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ 교환반품 통계 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("교환반품 통계 조회 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getDebugInfo(ExchangeStatsRequestDto requestDto) {
        log.info("🐛 교환반품 통계 디버깅 정보 조회 시작");
        
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            long startTime = System.currentTimeMillis();
            
            // 1. 요청 조건 정보
            debugInfo.put("requestConditions", buildRequestConditionsInfo(requestDto));
            
            // 2. 기본 통계 정보
            ExchangeStatsData.CoreStats coreStats = exchangeStatsMapper.getCoreStats(requestDto);
            debugInfo.put("coreStats", buildCoreStatsInfo(coreStats));
            
            // 3. 데이터 분포 정보
            debugInfo.put("dataDistribution", buildDataDistributionInfo(requestDto));
            
            // 4. 성능 정보
            long endTime = System.currentTimeMillis();
            debugInfo.put("performanceInfo", buildPerformanceInfo(startTime, endTime));
            
            // 5. 시스템 정보
            debugInfo.put("systemInfo", buildSystemInfo());
            
            log.info("✅ 디버깅 정보 조회 완료");
            return debugInfo;
            
        } catch (Exception e) {
            log.error("❌ 디버깅 정보 조회 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", true);
            errorInfo.put("message", e.getMessage());
            errorInfo.put("timestamp", LocalDate.now().format(DATE_FORMATTER));
            return errorInfo;
        }
    }

    @Override
    public Map<String, Object> getHealthCheck() {
        log.info("📈 교환반품 통계 시스템 헬스체크 시작");
        
        try {
            Map<String, Object> healthInfo = new HashMap<>();
            
            // 1. 데이터베이스 연결 확인
            boolean dbConnected = exchangeStatsMapper.checkDatabaseConnection();
            healthInfo.put("databaseConnected", dbConnected);
            
            // 2. 필수 테이블 존재 확인
            boolean tableExists = exchangeStatsMapper.checkTableExists("TB_RETURN_ITEM");
            healthInfo.put("tableExists", tableExists);
            
            // 3. 최근 데이터 확인
            Long recentDataCount = exchangeStatsMapper.getRecentDataCount();
            healthInfo.put("recentDataCount", recentDataCount);
            
            // 4. 전체 시스템 상태
            boolean systemHealthy = dbConnected && tableExists && (recentDataCount != null && recentDataCount > 0);
            healthInfo.put("systemHealthy", systemHealthy);
            healthInfo.put("timestamp", LocalDate.now().format(DATE_FORMATTER));
            
            log.info("✅ 헬스체크 완료 - 시스템 상태: {}", systemHealthy ? "정상" : "비정상");
            return healthInfo;
            
        } catch (Exception e) {
            log.error("❌ 헬스체크 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("systemHealthy", false);
            errorInfo.put("error", e.getMessage());
            errorInfo.put("timestamp", LocalDate.now().format(DATE_FORMATTER));
            return errorInfo;
        }
    }
    
    // ==================== 내부 변환 메서드들 ====================
    
    /**
     * 핵심 통계를 반환 (새로운 구조 - 변환 불필요)
     */
    private ExchangeStatsData.CoreStats convertToCoreStatsDto(ExchangeStatsData.CoreStats coreStats, ExchangeStatsRequestDto requestDto) {
        if (coreStats == null) {
            return createEmptyCoreSummaryDto(requestDto);
        }
        
        // ExchangeStatsData.CoreStats를 그대로 반환 (이미 최적화된 구조)
        return coreStats;
    }
    
    /**
     * 트렌드 데이터를 반환 (새로운 구조 - 변환 불필요)
     */
    private List<ExchangeStatsData.TrendPoint> convertToTrendDataDto(List<ExchangeStatsData.TrendPoint> trendPoints) {
        if (trendPoints == null || trendPoints.isEmpty()) {
            return new ArrayList<>();
        }
        
        // ExchangeStatsData.TrendPoint 리스트를 그대로 반환 (이미 최적화된 구조)
        return trendPoints;
    }
    
    /**
     * 유형별 분포를 반환 (새로운 구조 - 변환 불필요)
     */
    private List<ExchangeStatsData.TypeDistribution> convertToTypeDistributionDto(
            List<ExchangeStatsData.TypeDistribution> typeDistributions, ExchangeStatsData.CoreStats coreStats) {
        
        if (typeDistributions == null || typeDistributions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // ExchangeStatsData.TypeDistribution 리스트를 그대로 반환 (이미 최적화된 구조)
        return typeDistributions;
    }
    
    /**
     * 상태별 분포를 반환 (새로운 구조 - 변환 불필요)
     */
    private List<ExchangeStatsData.StatusDistribution> convertToStatusDistributionDto(
            List<ExchangeStatsData.StatusDistribution> statusDistributions, ExchangeStatsData.CoreStats coreStats) {
        
        if (statusDistributions == null || statusDistributions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // ExchangeStatsData.StatusDistribution 리스트를 그대로 반환 (이미 최적화된 구조)
        return statusDistributions;
    }
    
    /**
     * 사유별 분석을 반환 (새로운 구조 - 변환 불필요)
     */
    private List<ExchangeStatsData.ReasonAnalysis> convertToReasonAnalysisDto(
            List<ExchangeStatsData.ReasonAnalysis> reasonAnalyses, ExchangeStatsData.CoreStats coreStats) {
        
        if (reasonAnalyses == null || reasonAnalyses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // ExchangeStatsData.ReasonAnalysis 리스트를 그대로 반환 (이미 최적화된 구조)
        return reasonAnalyses;
    }
    
    /**
     * 사이트별 성과를 반환 (새로운 구조 - 변환 불필요)
     */
    private List<ExchangeStatsData.SitePerformance> convertToSitePerformanceDto(
            List<ExchangeStatsData.SitePerformance> sitePerformances, ExchangeStatsData.CoreStats coreStats) {
        
        if (sitePerformances == null || sitePerformances.isEmpty()) {
            return new ArrayList<>();
        }
        
        // ExchangeStatsData.SitePerformance 리스트를 그대로 반환 (이미 최적화된 구조)
        return sitePerformances;
    }
    
    // ==================== 유틸리티 메서드들 ====================
    
    /**
     * 유형 코드를 한글 라벨로 변환
     */
    private String convertTypeCodeToLabel(String typeCode) {
        if (typeCode == null) return "기타";
        
        switch (typeCode) {
            case "FULL_EXCHANGE": return "전체교환";
            case "PARTIAL_EXCHANGE": return "부분교환";
            case "FULL_RETURN": return "전체반품";
            case "PARTIAL_RETURN": return "부분반품";
            default: return "기타";
        }
    }
    
    /**
     * 날짜 범위 문자열 생성
     */
    private String getDateRangeString(ExchangeStatsRequestDto requestDto) {
        if ("CS_RECEIVED".equals(requestDto.getDateFilterType())) {
            return requestDto.getCsStartDate() + " ~ " + requestDto.getCsEndDate();
        } else if ("ORDER_DATE".equals(requestDto.getDateFilterType())) {
            return requestDto.getOrderStartDate() + " ~ " + requestDto.getOrderEndDate();
        }
        return "날짜 미지정";
    }
    
    /**
     * 빈 핵심 통계 DTO 생성 (새로운 구조)
     */
    private ExchangeStatsData.CoreStats createEmptyCoreSummaryDto(ExchangeStatsRequestDto requestDto) {
        return ExchangeStatsData.CoreStats.builder()
                .totalCount(0L)
                .completedCount(0L)
                .incompleteCount(0L)
                .completionRate(0.0)
                .totalRefundAmount(0L)
                .totalShippingFee(0L)
                .build();
    }
    
    /**
     * 메타 정보 설정
     */
    private void setMetaInfo(ExchangeStatsResponseDto response, ExchangeStatsRequestDto requestDto, ExchangeStatsData.CoreStats coreStats) {
        response.setSearchPeriod(getDateRangeString(requestDto));
        response.setLastUpdated(LocalDate.now().format(DATE_FORMATTER));
        response.setTotalRecords(coreStats != null ? coreStats.getTotalCount() : 0L);
    }
    
    // ==================== 디버깅 정보 생성 메서드들 ====================
    
    private Map<String, Object> buildRequestConditionsInfo(ExchangeStatsRequestDto requestDto) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("dateFilterType", requestDto.getDateFilterType());
        conditions.put("csStartDate", requestDto.getCsStartDate());
        conditions.put("csEndDate", requestDto.getCsEndDate());
        conditions.put("orderStartDate", requestDto.getOrderStartDate());
        conditions.put("orderEndDate", requestDto.getOrderEndDate());
        conditions.put("returnType", requestDto.getReturnType());
        conditions.put("completionStatus", requestDto.getCompletionStatus());
        conditions.put("siteName", requestDto.getSiteName());
        conditions.put("periodType", requestDto.getPeriodType());
        return conditions;
    }
    
    private Map<String, Object> buildCoreStatsInfo(ExchangeStatsData.CoreStats coreStats) {
        Map<String, Object> stats = new HashMap<>();
        if (coreStats != null) {
            stats.put("totalCount", coreStats.getTotalCount());
            stats.put("completedCount", coreStats.getCompletedCount());
            stats.put("incompleteCount", coreStats.getIncompleteCount());
            stats.put("completionRate", coreStats.getCompletionRate());
            stats.put("totalRefundAmount", coreStats.getTotalRefundAmount());
            stats.put("totalShippingFee", coreStats.getTotalShippingFee());
        } else {
            stats.put("message", "핵심 통계 데이터 없음");
        }
        return stats;
    }
    
    private Map<String, Object> buildDataDistributionInfo(ExchangeStatsRequestDto requestDto) {
        Map<String, Object> distribution = new HashMap<>();
        
        try {
            List<ExchangeStatsData.TypeDistribution> typeDistributions = exchangeStatsMapper.getTypeDistribution(requestDto);
            distribution.put("typeCount", typeDistributions != null ? typeDistributions.size() : 0);
            
            List<ExchangeStatsData.SitePerformance> sitePerformances = exchangeStatsMapper.getSitePerformance(requestDto);
            distribution.put("siteCount", sitePerformances != null ? sitePerformances.size() : 0);
            
            List<ExchangeStatsData.ReasonAnalysis> reasonAnalyses = exchangeStatsMapper.getReasonAnalysis(requestDto);
            distribution.put("reasonCount", reasonAnalyses != null ? reasonAnalyses.size() : 0);
            
        } catch (Exception e) {
            distribution.put("error", e.getMessage());
        }
        
        return distribution;
    }
    
    private Map<String, Object> buildPerformanceInfo(long startTime, long endTime) {
        Map<String, Object> performance = new HashMap<>();
        performance.put("totalProcessingTimeMs", endTime - startTime);
        performance.put("startTime", startTime);
        performance.put("endTime", endTime);
        return performance;
    }
    
    private Map<String, Object> buildSystemInfo() {
        Map<String, Object> system = new HashMap<>();
        system.put("timestamp", LocalDate.now().format(DATE_FORMATTER));
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        system.put("maxMemoryMB", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        system.put("freeMemoryMB", Runtime.getRuntime().freeMemory() / 1024 / 1024);
        return system;
    }
} 