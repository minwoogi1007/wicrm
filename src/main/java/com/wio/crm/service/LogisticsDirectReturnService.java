package com.wio.crm.service;

import com.wio.crm.dto.DirectReturnBulkRequestDTO;
import com.wio.crm.dto.LogisticsDirectReturnDTO;
import com.wio.crm.dto.LogisticsDirectReturnSearchDTO;
import com.wio.crm.mapper.LogisticsDirectReturnMapper;
import com.wio.crm.model.LogisticsDirectReturn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 물류센터 직접 입고 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LogisticsDirectReturnService {
    
    private final LogisticsDirectReturnMapper logisticsDirectReturnMapper;
    
    // ========== 기본 CRUD ==========
    
    /**
     * 전체 목록 조회 (페이징)
     */
    public Page<LogisticsDirectReturnDTO> findAll(LogisticsDirectReturnSearchDTO search) {
        log.debug("물류 직접입고 목록 조회 - 검색조건: {}", search);
        
        // 검색 조건 유효성 검증
        validateSearchCriteria(search);
        
        List<LogisticsDirectReturnDTO> content = logisticsDirectReturnMapper.findAll(search);
        long total = logisticsDirectReturnMapper.countAll(search);
        
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize());
        return new PageImpl<>(content, pageable, total);
    }
    
    /**
     * ID로 조회
     */
    public LogisticsDirectReturnDTO findById(Long id) {
        log.debug("물류 직접입고 조회 - ID: {}", id);
        
        LogisticsDirectReturnDTO dto = logisticsDirectReturnMapper.findById(id);
        if (dto == null) {
            throw new IllegalArgumentException("해당 직접입고 데이터를 찾을 수 없습니다. ID: " + id);
        }
        
        return dto;
    }
    
    /**
     * 등록
     */
    @Transactional
    public LogisticsDirectReturn save(LogisticsDirectReturn logisticsDirectReturn, String currentUser) {
        log.debug("물류 직접입고 등록 - 데이터: {}", logisticsDirectReturn);
        
        // 데이터 유효성 검증
        validateLogisticsDirectReturn(logisticsDirectReturn);
        
        // 운송장번호 중복 체크
        if (logisticsDirectReturn.getTrackingNumber() != null && 
            !logisticsDirectReturn.getTrackingNumber().trim().isEmpty()) {
            boolean exists = logisticsDirectReturnMapper.existsByTrackingNumber(
                logisticsDirectReturn.getTrackingNumber(), null);
            if (exists) {
                throw new IllegalArgumentException("이미 등록된 운송장번호입니다: " + logisticsDirectReturn.getTrackingNumber());
            }
        }
        
        // 등록자 정보 설정
        logisticsDirectReturn.setCreatedBy(currentUser);
        logisticsDirectReturn.setUpdatedBy(currentUser);
        
        // 기본값 설정
        if (logisticsDirectReturn.getProcessingStatus() == null) {
            logisticsDirectReturn.setProcessingStatus("RECEIVED");
        }
        if (logisticsDirectReturn.getMappingStatus() == null) {
            logisticsDirectReturn.setMappingStatus("PENDING");
        }
        if (logisticsDirectReturn.getQuantity() == null) {
            logisticsDirectReturn.setQuantity(1);
        }
        
        logisticsDirectReturnMapper.insert(logisticsDirectReturn);
        
        log.info("물류 직접입고 등록 완료 - ID: {}, 고객명: {}", 
                logisticsDirectReturn.getId(), logisticsDirectReturn.getCustomerName());
        
        return logisticsDirectReturn;
    }
    
    /**
     * 수정
     */
    @Transactional
    public LogisticsDirectReturn update(LogisticsDirectReturn logisticsDirectReturn, String currentUser) {
        log.debug("물류 직접입고 수정 - ID: {}", logisticsDirectReturn.getId());
        
        // 기존 데이터 존재 확인
        LogisticsDirectReturnDTO existing = findById(logisticsDirectReturn.getId());
        
        // 데이터 유효성 검증
        validateLogisticsDirectReturn(logisticsDirectReturn);
        
        // 운송장번호 중복 체크 (본인 제외)
        if (logisticsDirectReturn.getTrackingNumber() != null && 
            !logisticsDirectReturn.getTrackingNumber().trim().isEmpty()) {
            boolean exists = logisticsDirectReturnMapper.existsByTrackingNumber(
                logisticsDirectReturn.getTrackingNumber(), logisticsDirectReturn.getId());
            if (exists) {
                throw new IllegalArgumentException("이미 등록된 운송장번호입니다: " + logisticsDirectReturn.getTrackingNumber());
            }
        }
        
        // 수정자 정보 설정
        logisticsDirectReturn.setUpdatedBy(currentUser);
        
        logisticsDirectReturnMapper.update(logisticsDirectReturn);
        
        log.info("물류 직접입고 수정 완료 - ID: {}, 고객명: {}", 
                logisticsDirectReturn.getId(), logisticsDirectReturn.getCustomerName());
        
        return logisticsDirectReturn;
    }
    
    /**
     * 삭제
     */
    @Transactional
    public void delete(Long id, String currentUser) {
        log.debug("물류 직접입고 삭제 - ID: {}", id);
        
        // 기존 데이터 존재 확인
        LogisticsDirectReturnDTO existing = findById(id);
        
        // 매핑된 데이터가 있는 경우 삭제 제한
        if (existing.isMatched()) {
            throw new IllegalStateException("매핑된 데이터는 삭제할 수 없습니다. 먼저 매핑을 해제해주세요.");
        }
        
        logisticsDirectReturnMapper.delete(id);
        
        log.info("물류 직접입고 삭제 완료 - ID: {}, 고객명: {}", id, existing.getCustomerName());
    }
    
    // ========== 매핑 관련 ==========
    
    /**
     * 운송장번호로 기존 교환반품 데이터 찾기
     */
    public List<Map<String, Object>> findReturnItemsByTrackingNumber(String trackingNumber) {
        log.debug("운송장번호로 교환반품 데이터 조회 - 운송장번호: {}", trackingNumber);
        
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("운송장번호가 필요합니다.");
        }
        
        return logisticsDirectReturnMapper.findReturnItemsByTrackingNumber(trackingNumber);
    }
    
    /**
     * 매핑 처리
     */
    @Transactional
    public void updateMapping(Long id, Long matchedReturnId, String currentUser) {
        log.debug("매핑 처리 - 직접입고 ID: {}, 교환반품 ID: {}", id, matchedReturnId);
        
        // 기존 데이터 존재 확인
        LogisticsDirectReturnDTO existing = findById(id);
        
        // 매핑할 교환반품 데이터 유효성 확인 (여기서는 간단히 처리, 필요시 추가 검증)
        if (matchedReturnId == null) {
            throw new IllegalArgumentException("매핑할 교환반품 데이터가 필요합니다.");
        }
        
        logisticsDirectReturnMapper.updateMapping(id, matchedReturnId, "MATCHED", currentUser);
        
        log.info("매핑 처리 완료 - 직접입고 ID: {}, 교환반품 ID: {}", id, matchedReturnId);
    }
    
    /**
     * 매핑 해제
     */
    @Transactional
    public void clearMapping(Long id, String currentUser) {
        log.debug("매핑 해제 - 직접입고 ID: {}", id);
        
        // 기존 데이터 존재 확인
        LogisticsDirectReturnDTO existing = findById(id);
        
        if (!existing.isMatched()) {
            throw new IllegalStateException("매핑되지 않은 데이터입니다.");
        }
        
        logisticsDirectReturnMapper.clearMapping(id, currentUser);
        
        log.info("매핑 해제 완료 - 직접입고 ID: {}", id);
    }
    
    /**
     * 자동 매핑 시도
     */
    @Transactional
    public int autoMapping(String currentUser) {
        log.debug("자동 매핑 시작");
        
        // 미매핑 데이터 조회
        LogisticsDirectReturnSearchDTO search = LogisticsDirectReturnSearchDTO.builder()
                .mappingStatus("PENDING")
                .size(1000) // 한 번에 처리할 최대 개수
                .build();
        
        List<LogisticsDirectReturnDTO> pendingItems = logisticsDirectReturnMapper.findAll(search);
        int mappedCount = 0;
        
        for (LogisticsDirectReturnDTO item : pendingItems) {
            if (item.getTrackingNumber() != null && !item.getTrackingNumber().trim().isEmpty()) {
                List<Map<String, Object>> returnItems = 
                    logisticsDirectReturnMapper.findReturnItemsByTrackingNumber(item.getTrackingNumber());
                
                if (!returnItems.isEmpty()) {
                    // 첫 번째 매칭 항목으로 자동 매핑
                    Map<String, Object> firstMatch = returnItems.get(0);
                    Long returnId = ((Number) firstMatch.get("RETURN_ID")).longValue();
                    
                    try {
                        logisticsDirectReturnMapper.updateMapping(item.getId(), returnId, "MATCHED", currentUser);
                        mappedCount++;
                        log.debug("자동 매핑 완료 - 직접입고 ID: {}, 교환반품 ID: {}", item.getId(), returnId);
                    } catch (Exception e) {
                        log.warn("자동 매핑 실패 - 직접입고 ID: {}, 오류: {}", item.getId(), e.getMessage());
                    }
                }
            }
        }
        
        log.info("자동 매핑 완료 - 처리된 건수: {}/{}", mappedCount, pendingItems.size());
        return mappedCount;
    }
    
    // ========== 일괄 처리 ==========
    
    /**
     * 일괄 삭제
     */
    @Transactional
    public void deleteBatch(List<Long> ids, String currentUser) {
        log.debug("일괄 삭제 - 대상 개수: {}", ids.size());
        
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("삭제할 데이터가 없습니다.");
        }
        
        // 매핑된 데이터 체크
        for (Long id : ids) {
            LogisticsDirectReturnDTO item = findById(id);
            if (item.isMatched()) {
                throw new IllegalStateException("매핑된 데이터가 포함되어 있어 삭제할 수 없습니다. (ID: " + id + ")");
            }
        }
        
        logisticsDirectReturnMapper.deleteBatch(ids);
        
        log.info("일괄 삭제 완료 - 처리된 건수: {}", ids.size());
    }
    
    /**
     * 일괄 상태 변경
     */
    @Transactional
    public void updateStatusBatch(List<Long> ids, String processingStatus, String currentUser) {
        log.debug("일괄 상태 변경 - 대상 개수: {}, 상태: {}", ids.size(), processingStatus);
        
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("변경할 데이터가 없습니다.");
        }
        
        if (!"RECEIVED".equals(processingStatus) && !"PROCESSED".equals(processingStatus)) {
            throw new IllegalArgumentException("유효하지 않은 처리 상태입니다: " + processingStatus);
        }
        
        logisticsDirectReturnMapper.updateStatusBatch(ids, processingStatus, currentUser);
        
        log.info("일괄 상태 변경 완료 - 처리된 건수: {}, 상태: {}", ids.size(), processingStatus);
    }
    
    // ========== 통계 및 집계 ==========
    
    /**
     * 매핑 통계 조회
     */
    public Map<String, Object> getMappingStatistics() {
        return logisticsDirectReturnMapper.getMappingStatistics();
    }
    
    /**
     * 처리 상태별 통계
     */
    public Map<String, Object> getStatusStatistics() {
        return logisticsDirectReturnMapper.getStatusStatistics();
    }
    
    /**
     * 오늘 입고 통계 조회
     */
    public Map<String, Object> getTodayStatistics() {
        return logisticsDirectReturnMapper.getTodayStatistics();
    }
    
    // ========== 엑셀 다운로드 ==========
    
    /**
     * 엑셀 다운로드용 전체 데이터 조회
     */
    public List<LogisticsDirectReturnDTO> findAllForExcel(LogisticsDirectReturnSearchDTO search) {
        log.debug("엑셀 다운로드용 데이터 조회 - 검색조건: {}", search);
        
        validateSearchCriteria(search);
        
        return logisticsDirectReturnMapper.findAllForExcel(search);
    }
    
    // ========== 유틸리티 메서드 ==========
    
    /**
     * 검색 조건 유효성 검증
     */
    private void validateSearchCriteria(LogisticsDirectReturnSearchDTO search) {
        if (search == null) {
            throw new IllegalArgumentException("검색 조건이 필요합니다.");
        }
        
        if (!search.isValidDateRange()) {
            throw new IllegalArgumentException("날짜 범위가 유효하지 않습니다.");
        }
        
        if (search.getSize() <= 0 || search.getSize() > 1000) {
            search.setSize(20); // 기본값으로 재설정
        }
        
        if (search.getPage() < 0) {
            search.setPage(0);
        }
    }
    
    /**
     * 여러 제품 일괄 등록
     */
    @Transactional
    public void saveBulkItems(DirectReturnBulkRequestDTO request, String currentUser) {
        log.debug("물류 직접입고 일괄 등록 시작 - 공통정보: {}, 제품 수: {}, 등록자: {}", 
                  request.getCustomerName(), request.getProducts().size(), currentUser);
        
        // 요청 데이터 유효성 검증
        validateBulkRequest(request);
        
        try {
            // 입고일자 파싱
            LocalDate receivedDate = LocalDate.parse(request.getReceivedDate());
            
            // 각 제품별로 레코드 생성
            for (DirectReturnBulkRequestDTO.ProductInfo product : request.getProducts()) {
                LogisticsDirectReturn item = new LogisticsDirectReturn();
                
                // 공통 정보 설정
                item.setReceivedDate(receivedDate);
                item.setSiteName(request.getSiteName());
                item.setCustomerName(request.getCustomerName());
                item.setCustomerPhone(request.getCustomerPhone());
                item.setTrackingNumber(request.getTrackingNumber());
                item.setProcessingStatus(request.getProcessingStatus());
                item.setMappingStatus(request.getMappingStatus());
                item.setRemarks(request.getRemarks());
                
                // 개별 제품 정보 설정
                item.setProductCode(product.getProductCode());
                item.setQuantity(product.getQuantity());
                item.setProductColor(product.getProductColor());
                item.setProductSize(product.getProductSize());
                
                // 등록자 정보 설정
                item.setCreatedBy(currentUser);
                item.setUpdatedBy(currentUser);
                
                // 등록일시는 @PrePersist에서 자동 설정됨
                
                // 개별 저장
                logisticsDirectReturnMapper.insert(item);
                log.debug("제품 저장 완료 - 제품코드: {}, 수량: {}, 등록자: {}", 
                          product.getProductCode(), product.getQuantity(), currentUser);
            }
            
            log.info("물류 직접입고 일괄 등록 완료 - 고객: {}, 총 제품 수: {}, 등록자: {}", 
                     request.getCustomerName(), request.getProducts().size(), currentUser);
                     
        } catch (Exception e) {
            log.error("물류 직접입고 일괄 등록 실패", e);
            throw new RuntimeException("일괄 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 일괄 등록 요청 데이터 유효성 검증
     */
    private void validateBulkRequest(DirectReturnBulkRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("등록 데이터가 필요합니다.");
        }
        
        // 공통 필드 검증
        if (request.getReceivedDate() == null || request.getReceivedDate().trim().isEmpty()) {
            throw new IllegalArgumentException("입고일자가 필요합니다.");
        }
        
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("고객명이 필요합니다.");
        }
        
        // 제품 목록 검증
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 제품이 필요합니다.");
        }
        
        // 각 제품 정보 검증
        for (int i = 0; i < request.getProducts().size(); i++) {
            DirectReturnBulkRequestDTO.ProductInfo product = request.getProducts().get(i);
            String prefix = "제품 " + (i + 1) + ": ";
            
            if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
                throw new IllegalArgumentException(prefix + "제품코드가 필요합니다.");
            }
            
            if (product.getQuantity() == null || product.getQuantity() <= 0) {
                throw new IllegalArgumentException(prefix + "수량은 1개 이상이어야 합니다.");
            }
        }
        
        // 날짜 형식 검증
        try {
            LocalDate.parse(request.getReceivedDate());
        } catch (Exception e) {
            throw new IllegalArgumentException("입고일자 형식이 올바르지 않습니다. (YYYY-MM-DD 형식 필요)");
        }
    }

    /**
     * 물류 직접입고 데이터 유효성 검증
     */
    private void validateLogisticsDirectReturn(LogisticsDirectReturn logisticsDirectReturn) {
        if (logisticsDirectReturn == null) {
            throw new IllegalArgumentException("입고 데이터가 필요합니다.");
        }
        
        if (logisticsDirectReturn.getReceivedDate() == null) {
            throw new IllegalArgumentException("입고일자가 필요합니다.");
        }
        
        if (logisticsDirectReturn.getCustomerName() == null || 
            logisticsDirectReturn.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("고객명이 필요합니다.");
        }
        
        if (logisticsDirectReturn.getQuantity() != null && logisticsDirectReturn.getQuantity() <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
    }
} 