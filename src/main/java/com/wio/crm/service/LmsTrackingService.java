package com.wio.crm.service;

import com.wio.crm.dto.CallLogDto;
import com.wio.crm.dto.LmsLogDto;
import com.wio.crm.dto.LmsTrackingSearchDto;
import com.wio.crm.dto.LmsTrackingStatsDto;
import com.wio.crm.mapper.LmsTrackingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * LMS 문자 발송 추적 서비스
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LmsTrackingService {

    private final LmsTrackingMapper lmsTrackingMapper;

    /**
     * LMS 발송 내역 목록 조회 (페이징)
     */
    public Page<LmsLogDto> getLmsList(LmsTrackingSearchDto searchDto) {
        log.info("LMS 발송 내역 조회 - 검색조건: {}", searchDto);
        
        try {
            // 검색 조건 기본값 설정 및 검증
            searchDto.setDefaults();
            
            if (!searchDto.isValid()) {
                log.warn("유효하지 않은 검색 조건: {}", searchDto);
                return Page.empty();
            }
            
            // 데이터 조회
            List<LmsLogDto> lmsList = lmsTrackingMapper.findLmsList(searchDto);
            long totalCount = lmsTrackingMapper.countLmsList(searchDto);
            
            // 후속 연락 여부 확인 (각 LMS에 대해)
            enrichWithFollowUpInfo(lmsList);
            
            // 페이지 객체 생성
            Pageable pageable = PageRequest.of(searchDto.getPage() - 1, searchDto.getSize());
            
            log.info("LMS 발송 내역 조회 완료 - 총 {}건, 현재 페이지: {}/{}", 
                    totalCount, searchDto.getPage(), (totalCount / searchDto.getSize()) + 1);
            
            return new PageImpl<>(lmsList, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("LMS 발송 내역 조회 중 오류 발생", e);
            throw new RuntimeException("LMS 발송 내역 조회에 실패했습니다.", e);
        }
    }

    /**
     * LMS 발송 통계 조회 (통합 쿼리 최적화)
     */
    public LmsTrackingStatsDto getStatistics(LmsTrackingSearchDto searchDto) {
        log.info("LMS 발송 통계 조회 - 검색조건: {}", searchDto);
        long startTime = System.currentTimeMillis();
        
        try {
            // 검색 조건 기본값 설정
            searchDto.setDefaults();
            
            // 🚀 통합 쿼리로 모든 통계를 한 번에 조회 (6개 쿼리 → 1개 쿼리)
            Map<String, Object> unifiedStats = lmsTrackingMapper.getUnifiedStats(searchDto);
            
            // 🔍 디버그: 통계 쿼리 결과 상세 로그
            log.info("📊 통계 쿼리 결과 상세:");
            log.info("  - TOTAL_SENT: {}", unifiedStats.get("TOTAL_SENT"));
            log.info("  - RECONTACTED: {}", unifiedStats.get("RECONTACTED"));
            log.info("  - CALL_SUCCESS: {}", unifiedStats.get("CALL_SUCCESS"));
            log.info("  - RECONTACT_RATE: {}", unifiedStats.get("RECONTACT_RATE"));
            log.info("  - CALL_SUCCESS_RATE: {}", unifiedStats.get("CALL_SUCCESS_RATE"));
            log.info("  - AVG_RESPONSE_TIME: {}", unifiedStats.get("AVG_RESPONSE_TIME"));
            
            // 통계 DTO 빌드 (통합 결과 기반)
            LmsTrackingStatsDto statsDto = buildUnifiedStatsDto(unifiedStats);
            
            long endTime = System.currentTimeMillis();
            log.info("LMS 통합 통계 조회 완료 - 총 발송: {}건, 재연락률: {}%, 실행시간: {}ms", 
                    statsDto.getTotalSent(), statsDto.getRecontactRate(), (endTime - startTime));
            
            return statsDto;
            
        } catch (Exception e) {
            log.error("LMS 발송 통계 조회 중 오류 발생", e);
            throw new RuntimeException("LMS 발송 통계 조회에 실패했습니다.", e);
        }
    }

    /**
     * 특정 LMS 발송 건의 후속 통화 내역 조회
     */
    public List<CallLogDto> getCallHistory(Long lmsId) {
        log.info("LMS 후속 통화 내역 조회 - LMS ID: {}", lmsId);
        
        try {
            // LMS 정보 먼저 조회
            LmsLogDto lmsLog = lmsTrackingMapper.findLmsById(lmsId);
            if (lmsLog == null) {
                log.warn("존재하지 않는 LMS ID: {}", lmsId);
                return new ArrayList<>();
            }
            
            // 전화번호와 발송일시 기준으로 후속 통화 내역 조회
            // Oracle이 인식할 수 있는 형식으로 날짜 포맷팅
            String formattedDate = lmsLog.getSendDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            List<CallLogDto> callHistory = lmsTrackingMapper.findCallHistoryByPhoneAndDate(
                    lmsLog.getClid(),
                    formattedDate,
                    "P000000179" // 기본 고객코드
            );
            
            log.info("LMS 후속 통화 내역 조회 완료 - LMS ID: {}, 통화 건수: {}", lmsId, callHistory.size());
            
            return callHistory;
            
        } catch (Exception e) {
            log.error("LMS 후속 통화 내역 조회 중 오류 발생 - LMS ID: {}", lmsId, e);
            throw new RuntimeException("통화 내역 조회에 실패했습니다.", e);
        }
    }

    /**
     * 시스템 상태 확인
     */
    public Map<String, Object> getSystemStatus() {
        log.info("LMS 추적 시스템 상태 확인");
        
        try {
            // 테이블 존재 여부 확인
            int lmsTableExists = lmsTrackingMapper.checkLmsLogTableExists();
            int callTableExists = lmsTrackingMapper.checkCallLogTableExists();
            
            // 샘플 데이터 개수 조회
            Map<String, Object> sampleData = lmsTrackingMapper.getSampleDataCount("P000000179");
            
            return Map.of(
                "lmsTableExists", lmsTableExists > 0,
                "callTableExists", callTableExists > 0,
                "sampleData", sampleData,
                "systemReady", lmsTableExists > 0 && callTableExists > 0
            );
            
        } catch (Exception e) {
            log.error("시스템 상태 확인 중 오류 발생", e);
            return Map.of(
                "systemReady", false,
                "error", e.getMessage()
            );
        }
    }

    /**
     * 후속 연락 정보 보강 (N+1 문제 해결 버전)
     */
    private void enrichWithFollowUpInfo(List<LmsLogDto> lmsList) {
        if (lmsList.isEmpty()) {
            return;
        }

        try {
            // 모든 LMS ID 추출
            List<Long> lmsIds = lmsList.stream()
                    .map(LmsLogDto::getId)
                    .collect(Collectors.toList());

            // 한 번의 쿼리로 모든 후속 연락 정보 조회
            List<Map<String, Object>> callHistoryBatch = lmsTrackingMapper.findCallHistoryBatchByLmsIds(
                    lmsIds, "P000000179"
            );

            // LMS ID별로 그룹핑
            Map<Long, List<Map<String, Object>>> callHistoryByLmsId = callHistoryBatch.stream()
                    .collect(Collectors.groupingBy(call -> ((Number) call.get("LMS_ID")).longValue()));

            // 각 LMS에 후속 연락 정보 설정
            for (LmsLogDto lms : lmsList) {
                List<Map<String, Object>> calls = callHistoryByLmsId.getOrDefault(lms.getId(), Collections.emptyList());
                
                lms.setHasFollowUp(!calls.isEmpty());
                lms.setFollowUpCount(calls.size());
                
                if (!calls.isEmpty()) {
                    Map<String, Object> lastCall = calls.get(0); // 정렬되어 있음
                    lms.setLastCallResult((String) lastCall.get("RESULT"));
                    // lms.setLastCallDate는 필요시 구현
                }
            }

            log.debug("후속 연락 정보 조회 완료 - LMS: {}건, 통화: {}건", lmsList.size(), callHistoryBatch.size());

        } catch (Exception e) {
            log.warn("후속 연락 정보 일괄 조회 실패", e);
            // 실패 시 기본값 설정
            for (LmsLogDto lms : lmsList) {
                lms.setHasFollowUp(false);
                lms.setFollowUpCount(0);
            }
        }
    }

    /**
     * 🚀 통합 통계 결과에서 DTO 빌드 (성능 최적화 버전)
     */
    private LmsTrackingStatsDto buildStatsFromUnified(Map<String, Object> unifiedStats) {
        
        LmsTrackingStatsDto.LmsTrackingStatsDtoBuilder builder = LmsTrackingStatsDto.builder();
        
        // 기본 통계 설정
        builder.totalSent(getIntValue(unifiedStats, "TOTAL_SENT"))
               .recontacted(getIntValue(unifiedStats, "RECONTACTED"))
               .callSuccess(getIntValue(unifiedStats, "CALL_SUCCESS"));
        
        // 평균 응답시간 설정 (이미 분 단위로 계산됨)
        builder.avgResponseTime(getDoubleValue(unifiedStats, "AVG_RESPONSE_TIME"));
        
        // 시간대별 분석 설정 (통합 결과에서 추출)
        builder.timeAnalysis(buildTimeAnalysisFromUnified(unifiedStats));
        
        // 상담원별 통계 설정 (간소화 - 상위 상담원만)
        builder.agentStats(buildAgentStatsFromUnified(unifiedStats));
        
        // 일별 트렌드 설정 (통합 결과에서 파싱)
        builder.dailyTrends(buildDailyTrendsFromUnified(unifiedStats));
        
        // 메시지 유형별 통계 설정 (통합 결과에서 추출)
        builder.messageTypeStats(buildMessageTypeStatsFromUnified(unifiedStats));
        
        LmsTrackingStatsDto statsDto = builder.build();
        
        // 기본값 설정 및 비율 계산
        statsDto.setDefaultZeros();
        statsDto.calculateAllRates();
        
        return statsDto;
    }

    /**
     * 🚀 통합 통계 DTO 빌드 (신규 최적화 버전)
     */
    private LmsTrackingStatsDto buildUnifiedStatsDto(Map<String, Object> unifiedStats) {
        LmsTrackingStatsDto.LmsTrackingStatsDtoBuilder builder = LmsTrackingStatsDto.builder();
        
        // 기본 통계 설정
        builder.totalSent(getIntValue(unifiedStats, "TOTAL_SENT"))
               .recontacted(getIntValue(unifiedStats, "RECONTACTED"))
               .callSuccess(getIntValue(unifiedStats, "CALL_SUCCESS"))
               .avgResponseTime(getDoubleValue(unifiedStats, "AVG_RESPONSE_TIME"));
        
        // 시간대별 분석 설정 (통합 결과에서 직접 추출)
        LmsTrackingStatsDto.TimeAnalysis timeAnalysis = LmsTrackingStatsDto.TimeAnalysis.builder()
                .morning(LmsTrackingStatsDto.TimeSlotStats.builder()
                        .sent(getIntValue(unifiedStats, "MORNING_SENT"))
                        .recontacted(getIntValue(unifiedStats, "MORNING_RECONTACTED"))
                        .rate(calculateRate(getIntValue(unifiedStats, "MORNING_RECONTACTED"), 
                                          getIntValue(unifiedStats, "MORNING_SENT")))
                        .build())
                .afternoon(LmsTrackingStatsDto.TimeSlotStats.builder()
                        .sent(getIntValue(unifiedStats, "AFTERNOON_SENT"))
                        .recontacted(getIntValue(unifiedStats, "AFTERNOON_RECONTACTED"))
                        .rate(calculateRate(getIntValue(unifiedStats, "AFTERNOON_RECONTACTED"), 
                                          getIntValue(unifiedStats, "AFTERNOON_SENT")))
                        .build())
                .evening(LmsTrackingStatsDto.TimeSlotStats.builder()
                        .sent(getIntValue(unifiedStats, "EVENING_SENT"))
                        .recontacted(getIntValue(unifiedStats, "EVENING_RECONTACTED"))
                        .rate(calculateRate(getIntValue(unifiedStats, "EVENING_RECONTACTED"), 
                                          getIntValue(unifiedStats, "EVENING_SENT")))
                        .build())
                .build();
        
        builder.timeAnalysis(timeAnalysis);
        
        // 나머지는 기본값 또는 빈 리스트로 설정 (성능 최적화를 위해)
        builder.agentStats(new ArrayList<>())
               .dailyTrends(new ArrayList<>())
               .messageTypeStats(new ArrayList<>());
        
        LmsTrackingStatsDto statsDto = builder.build();
        
        // 기본값 설정 및 비율 계산
        statsDto.setDefaultZeros();
        statsDto.calculateAllRates();
        
        return statsDto;
    }
    
    /**
     * 통계 DTO 빌드 (기존 버전 - 호환성 유지)
     */
    private LmsTrackingStatsDto buildStatsDto(Map<String, Object> basicStats,
                                             List<Map<String, Object>> timeSlotStats,
                                             List<Map<String, Object>> agentStats,
                                             List<Map<String, Object>> dailyTrends,
                                             List<Map<String, Object>> messageTypeStats,
                                             Double avgResponseTime) {
        
        LmsTrackingStatsDto.LmsTrackingStatsDtoBuilder builder = LmsTrackingStatsDto.builder();
        
        // 기본 통계 설정
        if (basicStats != null) {
            builder.totalSent(getIntValue(basicStats, "TOTAL_SENT"))
                   .recontacted(getIntValue(basicStats, "RECONTACTED"))
                   .callSuccess(getIntValue(basicStats, "CALL_SUCCESS"));
        }
        
        // 평균 응답시간 설정
        builder.avgResponseTime(avgResponseTime != null ? avgResponseTime : 0.0);
        
        // 시간대별 분석 설정
        builder.timeAnalysis(buildTimeAnalysis(timeSlotStats));
        
        // 상담원별 통계 설정
        builder.agentStats(buildAgentStatsList(agentStats));
        
        // 일별 트렌드 설정
        builder.dailyTrends(buildDailyTrendsList(dailyTrends));
        
        // 메시지 유형별 통계 설정
        builder.messageTypeStats(buildMessageTypeStatsList(messageTypeStats));
        
        LmsTrackingStatsDto statsDto = builder.build();
        
        // 기본값 설정 및 비율 계산
        statsDto.setDefaultZeros();
        statsDto.calculateAllRates();
        
        return statsDto;
    }

    /**
     * 시간대별 분석 빌드
     */
    private LmsTrackingStatsDto.TimeAnalysis buildTimeAnalysis(List<Map<String, Object>> timeSlotStats) {
        // 구현 필요 - 시간대별 데이터를 분석하여 TimeAnalysis 객체 생성
        return LmsTrackingStatsDto.TimeAnalysis.builder()
                .morning(LmsTrackingStatsDto.TimeSlotStats.builder().sent(0).recontacted(0).rate(0.0).build())
                .afternoon(LmsTrackingStatsDto.TimeSlotStats.builder().sent(0).recontacted(0).rate(0.0).build())
                .evening(LmsTrackingStatsDto.TimeSlotStats.builder().sent(0).recontacted(0).rate(0.0).build())
                .build();
    }

    /**
     * 상담원별 통계 리스트 빌드
     */
    private List<LmsTrackingStatsDto.AgentStats> buildAgentStatsList(List<Map<String, Object>> agentStats) {
        List<LmsTrackingStatsDto.AgentStats> result = new ArrayList<>();
        
        if (agentStats != null) {
            for (Map<String, Object> stat : agentStats) {
                result.add(LmsTrackingStatsDto.AgentStats.builder()
                        .agentName(getStringValue(stat, "AGENT_NAME"))
                        .empno(getStringValue(stat, "EMPNO"))
                        .sent(getIntValue(stat, "SENT"))
                        .recontacted(getIntValue(stat, "RECONTACTED"))
                        .callSuccess(getIntValue(stat, "CALL_SUCCESS"))
                        .build());
            }
        }
        
        return result;
    }

    /**
     * 일별 트렌드 리스트 빌드
     */
    private List<LmsTrackingStatsDto.DailyTrend> buildDailyTrendsList(List<Map<String, Object>> dailyTrends) {
        List<LmsTrackingStatsDto.DailyTrend> result = new ArrayList<>();
        
        if (dailyTrends != null) {
            for (Map<String, Object> trend : dailyTrends) {
                result.add(LmsTrackingStatsDto.DailyTrend.builder()
                        .date(getStringValue(trend, "DATE"))
                        .sent(getIntValue(trend, "SENT"))
                        .recontacted(getIntValue(trend, "RECONTACTED"))
                        .callSuccess(getIntValue(trend, "CALL_SUCCESS"))
                        .build());
            }
        }
        
        return result;
    }

    /**
     * 메시지 유형별 통계 리스트 빌드
     */
    private List<LmsTrackingStatsDto.MessageTypeStats> buildMessageTypeStatsList(List<Map<String, Object>> messageTypeStats) {
        List<LmsTrackingStatsDto.MessageTypeStats> result = new ArrayList<>();
        
        if (messageTypeStats != null) {
            for (Map<String, Object> stat : messageTypeStats) {
                result.add(LmsTrackingStatsDto.MessageTypeStats.builder()
                        .messageType(getStringValue(stat, "MESSAGE_TYPE"))
                        .sent(getIntValue(stat, "SENT"))
                        .recontacted(getIntValue(stat, "RECONTACTED"))
                        .build());
            }
        }
        
        return result;
    }

    /**
     * Map에서 정수값 안전하게 추출
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("정수 변환 실패 - key: {}, value: {}", key, value);
            return 0;
        }
    }

    /**
     * Map에서 문자열값 안전하게 추출
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Map에서 실수값 안전하게 추출
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("실수 변환 실패 - key: {}, value: {}", key, value);
            return 0.0;
        }
    }

    /**
     * 비율 계산 (백분율)
     */
    private Double calculateRate(Integer numerator, Integer denominator) {
        if (denominator == null || denominator == 0) return 0.0;
        if (numerator == null) numerator = 0;
        return Math.round((numerator * 100.0 / denominator) * 10.0) / 10.0; // 소수점 1자리
    }

    /**
     * 🚀 통합 결과에서 시간대별 분석 빌드
     */
    private LmsTrackingStatsDto.TimeAnalysis buildTimeAnalysisFromUnified(Map<String, Object> unifiedStats) {
        return LmsTrackingStatsDto.TimeAnalysis.builder()
                .morning(LmsTrackingStatsDto.TimeSlotStats.builder()
                        .sent(getIntValue(unifiedStats, "MORNING_SENT"))
                        .recontacted(getIntValue(unifiedStats, "MORNING_RECONTACTED"))
                        .rate(calculateRate(getIntValue(unifiedStats, "MORNING_RECONTACTED"), getIntValue(unifiedStats, "MORNING_SENT")))
                        .build())
                .afternoon(LmsTrackingStatsDto.TimeSlotStats.builder()
                        .sent(getIntValue(unifiedStats, "AFTERNOON_SENT"))
                        .recontacted(getIntValue(unifiedStats, "AFTERNOON_RECONTACTED"))
                        .rate(calculateRate(getIntValue(unifiedStats, "AFTERNOON_RECONTACTED"), getIntValue(unifiedStats, "AFTERNOON_SENT")))
                        .build())
                .evening(LmsTrackingStatsDto.TimeSlotStats.builder()
                        .sent(getIntValue(unifiedStats, "EVENING_SENT"))
                        .recontacted(getIntValue(unifiedStats, "EVENING_RECONTACTED"))
                        .rate(calculateRate(getIntValue(unifiedStats, "EVENING_RECONTACTED"), getIntValue(unifiedStats, "EVENING_SENT")))
                        .build())
                .build();
    }

    /**
     * 🚀 통합 결과에서 상담원별 통계 빌드 (간소화)
     */
    private List<LmsTrackingStatsDto.AgentStats> buildAgentStatsFromUnified(Map<String, Object> unifiedStats) {
        List<LmsTrackingStatsDto.AgentStats> result = new ArrayList<>();
        
        String topAgent = getStringValue(unifiedStats, "TOP_AGENT");
        if (!topAgent.isEmpty()) {
            result.add(LmsTrackingStatsDto.AgentStats.builder()
                    .agentName(topAgent)
                    .empno(topAgent)
                    .sent(getIntValue(unifiedStats, "TOTAL_SENT"))  // 전체 통계로 대체
                    .recontacted(getIntValue(unifiedStats, "RECONTACTED"))
                    .callSuccess(getIntValue(unifiedStats, "CALL_SUCCESS"))
                    .build());
        }
        
        return result;
    }

    /**
     * 🚀 통합 결과에서 일별 트렌드 빌드 (간소화)
     */
    private List<LmsTrackingStatsDto.DailyTrend> buildDailyTrendsFromUnified(Map<String, Object> unifiedStats) {
        List<LmsTrackingStatsDto.DailyTrend> result = new ArrayList<>();
        
        // 오늘 데이터만 추가 (간소화)
        String todayDate = getStringValue(unifiedStats, "TODAY_DATE");
        if (!todayDate.isEmpty()) {
            result.add(LmsTrackingStatsDto.DailyTrend.builder()
                    .date(todayDate)
                    .sent(getIntValue(unifiedStats, "TODAY_SENT"))
                    .recontacted(getIntValue(unifiedStats, "TODAY_RECONTACTED"))
                    .callSuccess(0)  // 간소화
                    .build());
        }
        
        return result;
    }

    /**
     * 🚀 통합 결과에서 메시지 유형별 통계 빌드
     */
    private List<LmsTrackingStatsDto.MessageTypeStats> buildMessageTypeStatsFromUnified(Map<String, Object> unifiedStats) {
        List<LmsTrackingStatsDto.MessageTypeStats> result = new ArrayList<>();
        
        // 상담 요청
        result.add(LmsTrackingStatsDto.MessageTypeStats.builder()
                .messageType("상담 요청")
                .sent(getIntValue(unifiedStats, "CONSULT_REQUEST_SENT"))
                .recontacted(getIntValue(unifiedStats, "CONSULT_REQUEST_RECONTACTED"))
                .build());
        
        // 재상담 안내
        result.add(LmsTrackingStatsDto.MessageTypeStats.builder()
                .messageType("재상담 안내")
                .sent(getIntValue(unifiedStats, "RECONSULT_SENT"))
                .recontacted(getIntValue(unifiedStats, "RECONSULT_RECONTACTED"))
                .build());
        
        // 상담 완료
        result.add(LmsTrackingStatsDto.MessageTypeStats.builder()
                .messageType("상담 완료")
                .sent(getIntValue(unifiedStats, "COMPLETE_SENT"))
                .recontacted(getIntValue(unifiedStats, "COMPLETE_RECONTACTED"))
                .build());
        
        // 기타
        result.add(LmsTrackingStatsDto.MessageTypeStats.builder()
                .messageType("기타")
                .sent(getIntValue(unifiedStats, "OTHER_SENT"))
                .recontacted(getIntValue(unifiedStats, "OTHER_RECONTACTED"))
                .build());
        
        return result;
    }

    /**
     * 비율 계산 헬퍼
     */
    private Double calculateRate(int recontacted, int sent) {
        if (sent == 0) return 0.0;
        return Math.round((double) recontacted / sent * 100.0 * 100.0) / 100.0;
    }

    /**
     * 안전한 정수 파싱
     */
    private Integer parseIntSafely(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
} 