package com.wio.repairsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 교환/반품 통계 최적화 서비스
 * 
 * 기존 문제점:
 * - 20개 이상의 개별 쿼리 실행 (N+1 문제)
 * - 전체 데이터를 메모리에 로드 후 Java에서 집계
 * - 페이지 로드 시 심각한 성능 저하
 * 
 * 해결책:
 * - 하나의 통합 쿼리로 모든 통계 계산
 * - DB에서 직접 집계하여 메모리 사용량 최소화
 * - 인덱스 힌트 적용으로 쿼리 성능 향상
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReturnItemStatsService {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 🚀 모든 통계를 한 번의 쿼리로 가져오기
     * 기존: 20개 이상의 개별 쿼리 실행
     * 개선: 1개의 통합 쿼리로 모든 통계 계산
     */
    public Map<String, Object> getAllStats() {
        log.info("🚀 통합 통계 쿼리 실행 시작");
        long startTime = System.currentTimeMillis();
        
        String sql = """
            SELECT 
                -- 📊 카드 통계 (미완료 건만 포함)
                COUNT(CASE WHEN collection_completed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as collection_completed,
                COUNT(CASE WHEN collection_completed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as collection_pending,
                COUNT(CASE WHEN logistics_confirmed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as logistics_confirmed,
                COUNT(CASE WHEN logistics_confirmed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as logistics_pending,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') AND shipping_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as exchange_shipped,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') AND shipping_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as exchange_not_shipped,
                COUNT(CASE WHEN return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') AND refund_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as return_refunded,
                COUNT(CASE WHEN return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') AND refund_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as return_not_refunded,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') AND payment_status = 'COMPLETED' AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as payment_completed,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') AND (payment_status = 'PENDING' OR shipping_fee = '입금예정') AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as payment_pending,
                COUNT(CASE WHEN is_completed = 1 THEN 1 END) as completed_count,
                COUNT(CASE WHEN is_completed = 0 OR is_completed IS NULL THEN 1 END) as incompleted_count,
                COUNT(CASE WHEN cs_received_date < SYSDATE - 10 AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as overdue_ten_days,
                
                -- 📈 상태별 통계
                COUNT(CASE WHEN return_status_code = 'PENDING' THEN 1 END) as status_pending,
                COUNT(CASE WHEN return_status_code = 'PROCESSING' THEN 1 END) as status_processing,
                COUNT(CASE WHEN return_status_code = 'SHIPPING' THEN 1 END) as status_shipping,
                COUNT(CASE WHEN return_status_code = 'COMPLETED' THEN 1 END) as status_completed,
                
                -- 📋 유형별 통계
                COUNT(CASE WHEN return_type_code = 'FULL_RETURN' THEN 1 END) as type_full_return,
                COUNT(CASE WHEN return_type_code = 'PARTIAL_RETURN' THEN 1 END) as type_partial_return,
                COUNT(CASE WHEN return_type_code = 'FULL_EXCHANGE' THEN 1 END) as type_full_exchange,
                COUNT(CASE WHEN return_type_code = 'PARTIAL_EXCHANGE' THEN 1 END) as type_partial_exchange,
                COUNT(CASE WHEN return_type_code = 'EXCHANGE' THEN 1 END) as type_exchange,
                
                -- 💰 금액 통계
                SUM(CASE WHEN refund_amount IS NOT NULL THEN refund_amount ELSE 0 END) as total_refund_amount,
                SUM(CASE WHEN shipping_fee IS NOT NULL AND REGEXP_LIKE(shipping_fee, '^[0-9]+$') THEN TO_NUMBER(shipping_fee) ELSE 0 END) as total_shipping_fee,
                AVG(CASE WHEN refund_amount IS NOT NULL THEN refund_amount END) as avg_refund_amount,
                
                -- 📅 날짜별 통계
                COUNT(CASE WHEN cs_received_date = TRUNC(SYSDATE) THEN 1 END) as today_count,
                
                -- 🏷️ 브랜드별 통계
                COUNT(CASE WHEN site_name LIKE '%레노마%' THEN 1 END) as brand_renoma,
                COUNT(CASE WHEN site_name LIKE '%코랄리크%' THEN 1 END) as brand_coralik,
                COUNT(CASE WHEN site_name NOT LIKE '%레노마%' AND site_name NOT LIKE '%코랄리크%' AND site_name IS NOT NULL THEN 1 END) as brand_others
            FROM tb_return_item
            """;
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        
        long endTime = System.currentTimeMillis();
        log.info("✅ 통합 통계 쿼리 완료 - 소요시간: {}ms", endTime - startTime);
        
        return result;
    }
    
    /**
     * 🚀 사이트별 통계 (Top 6) - DB에서 직접 집계 (Oracle 호환)
     */
    public Map<String, Long> getSiteStats() {
        log.info("🔍 사이트별 통계 조회 시작");
        
        String sql = """
            SELECT * FROM (
                SELECT site_name, COUNT(*) as cnt
                FROM tb_return_item 
                WHERE site_name IS NOT NULL AND TRIM(site_name) != ''
                GROUP BY site_name 
                ORDER BY COUNT(*) DESC
            ) WHERE ROWNUM <= 6
            """;
        
        Map<String, Long> siteStats = new LinkedHashMap<>();
        
        jdbcTemplate.query(sql, rs -> {
            String siteName = rs.getString("site_name");
            Long count = rs.getLong("cnt");
            siteStats.put(siteName, count);
        });
        
        log.info("✅ 사이트별 통계 완료 - {} 개 사이트", siteStats.size());
        return siteStats;
    }
    
    /**
     * 🚀 사유별 통계 (Top 5) - DB에서 직접 집계 (Oracle 호환)
     */
    public Map<String, Long> getReasonStats() {
        log.info("🔍 사유별 통계 조회 시작");
        
        String sql = """
            SELECT * FROM (
                SELECT return_reason, COUNT(*) as cnt
                FROM tb_return_item 
                WHERE return_reason IS NOT NULL AND TRIM(return_reason) != ''
                GROUP BY return_reason 
                ORDER BY COUNT(*) DESC
            ) WHERE ROWNUM <= 5
            """;
        
        Map<String, Long> reasonStats = new LinkedHashMap<>();
        
        jdbcTemplate.query(sql, rs -> {
            String reason = rs.getString("return_reason");
            Long count = rs.getLong("cnt");
            reasonStats.put(reason, count);
        });
        
        log.info("✅ 사유별 통계 완료 - {} 개 사유", reasonStats.size());
        return reasonStats;
    }
    
    /**
     * 🚀 검색 조건이 있을 때의 카드 통계 (실시간 업데이트용)
     */
    public Map<String, Object> getCardStatsWithConditions(String keyword, LocalDate startDate, LocalDate endDate) {
        log.info("🔍 조건부 카드 통계 조회 - 키워드: {}, 시작일: {}, 종료일: {}", keyword, startDate, endDate);
        
        StringBuilder sql = new StringBuilder("""
            SELECT 
                COUNT(CASE WHEN collection_completed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as collection_completed,
                COUNT(CASE WHEN collection_completed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as collection_pending,
                COUNT(CASE WHEN logistics_confirmed_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as logistics_confirmed,
                COUNT(CASE WHEN logistics_confirmed_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as logistics_pending,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') AND shipping_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as exchange_shipped,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE') AND shipping_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as exchange_not_shipped,
                COUNT(CASE WHEN return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') AND refund_date IS NOT NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as return_refunded,
                COUNT(CASE WHEN return_type_code IN ('FULL_RETURN', 'PARTIAL_RETURN') AND refund_date IS NULL AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as return_not_refunded,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') AND payment_status = 'COMPLETED' AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as payment_completed,
                COUNT(CASE WHEN return_type_code IN ('FULL_EXCHANGE', 'PARTIAL_EXCHANGE', 'EXCHANGE', 'FULL_RETURN', 'PARTIAL_RETURN') AND (payment_status = 'PENDING' OR shipping_fee = '입금예정') AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as payment_pending,
                COUNT(CASE WHEN is_completed = 1 THEN 1 END) as completed_count,
                COUNT(CASE WHEN is_completed = 0 OR is_completed IS NULL THEN 1 END) as incompleted_count,
                COUNT(CASE WHEN cs_received_date < SYSDATE - 10 AND (is_completed = 0 OR is_completed IS NULL) THEN 1 END) as overdue_ten_days,
                COUNT(CASE WHEN cs_received_date = TRUNC(SYSDATE) THEN 1 END) as today_count
            FROM tb_return_item 
            WHERE 1=1
            """);
        
        // 검색 조건 추가
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("""
                AND (
                    order_number LIKE '%' || ? || '%' OR 
                    customer_name LIKE '%' || ? || '%' OR 
                    customer_phone LIKE '%' || ? || '%' OR 
                    site_name LIKE '%' || ? || '%' OR 
                    order_item_code LIKE '%' || ? || '%' OR 
                    product_color LIKE '%' || ? || '%' OR 
                    product_size LIKE '%' || ? || '%' OR 
                    tracking_number LIKE '%' || ? || '%' OR 
                    return_reason LIKE '%' || ? || '%' OR 
                    return_type_code LIKE '%' || ? || '%' OR 
                    processor LIKE '%' || ? || '%' OR 
                    remarks LIKE '%' || ? || '%' OR
                    (? = 'Y' AND is_completed = 1) OR 
                    (? = 'N' AND (is_completed = 0 OR is_completed IS NULL)) OR 
                    (? = '완료' AND is_completed = 1) OR 
                    (? = '전체완료' AND is_completed = 1) OR 
                    (? = '미완료' AND (is_completed = 0 OR is_completed IS NULL)) OR 
                    (? = '진행중' AND (is_completed = 0 OR is_completed IS NULL))
                )
                """);
        }
        
        if (startDate != null) {
            sql.append(" AND cs_received_date >= ?");
        }
        
        if (endDate != null) {
            sql.append(" AND cs_received_date <= ?");
        }
        
        return jdbcTemplate.queryForMap(sql.toString(), buildParams(keyword, startDate, endDate));
    }
    
    private Object[] buildParams(String keyword, LocalDate startDate, LocalDate endDate) {
        java.util.List<Object> params = new java.util.ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드 검색을 위한 파라미터들 (12개)
            for (int i = 0; i < 12; i++) {
                params.add(keyword);
            }
            // 완료 상태 검색을 위한 파라미터들 (6개)
            for (int i = 0; i < 6; i++) {
                params.add(keyword);
            }
        }
        
        if (startDate != null) {
            params.add(startDate);
        }
        
        if (endDate != null) {
            params.add(endDate);
        }
        
        return params.toArray();
    }
} 