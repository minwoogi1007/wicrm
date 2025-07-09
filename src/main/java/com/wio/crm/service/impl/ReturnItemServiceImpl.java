package com.wio.crm.service.impl;

import com.wio.crm.dto.ReturnItemDTO;
import com.wio.crm.dto.ReturnItemSearchDTO;
import com.wio.crm.dto.ReturnItemBulkDateUpdateDTO;
// import com.wio.crm.dto.ExchangeStatsRequestDto; // 제거됨 - 새로운 ExchangeStatsService 사용
import com.wio.crm.dto.ExchangeStatsResponseDto;
import com.wio.crm.mapper.ReturnItemMapper;
import com.wio.crm.model.ReturnItem;
import com.wio.crm.repository.ReturnItemRepository;
import com.wio.crm.service.ReturnItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 교환/반품 서비스 구현체
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReturnItemServiceImpl implements ReturnItemService {

    private final ReturnItemRepository returnItemRepository;
    private final ReturnItemMapper returnItemMapper;

    @Override
    public Page<ReturnItemDTO> findAll(int page, int size, String sortBy, String sortDir) {
        try {
            log.info("📋 Oracle 11g 호환 전체 목록 조회 - page: {}, size: {}", page, size);
            
            // Oracle 11g 호환: ROWNUM 기반 페이징
            int startRow = page * size;
            int endRow = startRow + size;
            
            log.info("🔢 페이징 파라미터 - startRow: {}, endRow: {}", startRow, endRow);
            
            List<ReturnItem> items = returnItemRepository.findAllWithPagination(startRow, endRow);
            long totalCount = returnItemRepository.countAllItems();
            
            log.info("✅ 조회 결과 - items: {}, totalCount: {}", items.size(), totalCount);
            
            List<ReturnItemDTO> dtoList = convertToDTO(items);
            
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(dtoList, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 전체 목록 조회 실패: {}", e.getMessage(), e);
            // 빈 페이지 반환
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(new ArrayList<>(), pageable, 0L);
        }
    }



    @Override
    public ReturnItemDTO findById(Long id) {
        Optional<ReturnItem> item = returnItemRepository.findById(id);
        return item.map(this::convertToDTO).orElse(null);
    }

    @Override
    public ReturnItemDTO save(ReturnItemDTO returnItemDTO) {
        ReturnItem item = convertToEntity(returnItemDTO);
        ReturnItem saved = returnItemRepository.save(item);
        return convertToDTO(saved);
    }
    
    @Override
    @Transactional
    public ReturnItemDTO updateReturnItem(ReturnItemDTO returnItemDTO) {
        log.info("📝 교환/반품 건 수정 - ID: {}, 고객명: {}", 
            returnItemDTO.getId(), returnItemDTO.getCustomerName());
        
        try {
            // 기존 데이터 조회
            Optional<ReturnItem> existingOpt = returnItemRepository.findById(returnItemDTO.getId());
            if (!existingOpt.isPresent()) {
                throw new RuntimeException("수정할 교환/반품 건을 찾을 수 없습니다: " + returnItemDTO.getId());
            }
            
            // DTO를 Entity로 변환
            ReturnItem item = convertToEntity(returnItemDTO);
            
            // 수정 시점 정보 설정
            item.setUpdateDate(LocalDateTime.now());
            
            // 저장
            ReturnItem saved = returnItemRepository.save(item);
            
            log.info("✅ 교환/반품 건 수정 완료 - ID: {}", saved.getId());
            return convertToDTO(saved);
            
        } catch (Exception e) {
            log.error("❌ 교환/반품 건 수정 실패: {}", e.getMessage(), e);
            throw new RuntimeException("교환/반품 건 수정에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ReturnItemDTO createReturnItem(ReturnItemDTO returnItemDTO) {
        log.info("📝 새 교환/반품 건 생성 - 고객명: {}, 주문번호: {}", 
            returnItemDTO.getCustomerName(), returnItemDTO.getOrderNumber());
        
        try {
            // DTO를 Entity로 변환
            ReturnItem item = convertToEntity(returnItemDTO);
            
            // 생성 시점 정보 설정
            item.setCreateDate(LocalDateTime.now());
            item.setUpdateDate(LocalDateTime.now());
            if (item.getCreatedBy() == null) {
                item.setCreatedBy("EXCEL_UPLOAD");
            }
            
            // 저장
            ReturnItem saved = returnItemRepository.save(item);
            
            log.info("✅ 교환/반품 건 생성 완료 - ID: {}", saved.getId());
            return convertToDTO(saved);
            
        } catch (Exception e) {
            log.error("❌ 교환/반품 건 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("교환/반품 건 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        returnItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusCounts() {
        try {
            // 🚀 통합 쿼리로 모든 통계를 한 번에 조회 (기존 6개 쿼리 → 1개 쿼리)
            Map<String, Object> allStats = getDashboardStats();
            
            Map<String, Long> statusCounts = new HashMap<>();
            statusCounts.put("collectionCompleted", (Long) allStats.get("collectionCompletedCount"));
            statusCounts.put("collectionPending", (Long) allStats.get("collectionPendingCount"));
            statusCounts.put("logisticsConfirmed", (Long) allStats.get("logisticsConfirmedCount"));
            statusCounts.put("logisticsPending", (Long) allStats.get("logisticsPendingCount"));
            statusCounts.put("completed", (Long) allStats.get("completedCount"));
            statusCounts.put("incompleted", (Long) allStats.get("incompletedCount"));
            
            return statusCounts;
            
        } catch (Exception e) {
            log.error("❌ 상태별 통계 조회 실패: {}", e.getMessage());
            // 기본값 반환
            Map<String, Long> defaultCounts = new HashMap<>();
            defaultCounts.put("collectionCompleted", 0L);
            defaultCounts.put("collectionPending", 0L);
            defaultCounts.put("logisticsConfirmed", 0L);
            defaultCounts.put("logisticsPending", 0L);
            defaultCounts.put("completed", 0L);
            defaultCounts.put("incompleted", 0L);
            return defaultCounts;
        }
    }

    @Override
    public Map<String, Long> getSiteCounts() {
        try {
            List<ReturnItem> allItems = returnItemRepository.findAll();
            return allItems.stream()
                    .filter(item -> item.getSiteName() != null && !item.getSiteName().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                        ReturnItem::getSiteName,
                        Collectors.counting()
                    ));
        } catch (Exception e) {
            log.error("❌ 사이트별 통계 조회 실패: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Long> getTypeCounts() {
        try {
            List<ReturnItem> allItems = returnItemRepository.findAll();
            Map<String, Long> rawCounts = allItems.stream()
                    .filter(item -> item.getReturnTypeCode() != null && !item.getReturnTypeCode().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                        ReturnItem::getReturnTypeCode,
                        Collectors.counting()
                    ));
            
            // 영어 코드를 한글로 변환
            Map<String, Long> counts = new HashMap<>();
            for (Map.Entry<String, Long> entry : rawCounts.entrySet()) {
                String label = convertTypeCodeToLabel(entry.getKey());
                counts.put(label, entry.getValue());
            }
            return counts;
        } catch (Exception e) {
            log.error("❌ 유형별 통계 조회 실패: {}", e.getMessage());
            Map<String, Long> counts = new HashMap<>();
            counts.put("교환", 0L);
            counts.put("반품", 0L);
            counts.put("전체교환", 0L);
            counts.put("부분교환", 0L);
            counts.put("전체반품", 0L);
            counts.put("부분반품", 0L);
            return counts;
        }
    }
    
    /**
     * 🎯 유형 코드를 한글 라벨로 변환 (4가지만)
     */
    private String convertTypeCodeToLabel(String typeCode) {
        if (typeCode == null) {
            return "기타";
        }
        switch (typeCode) {
            case "FULL_EXCHANGE": return "전체교환";
            case "PARTIAL_EXCHANGE": return "부분교환";
            case "FULL_RETURN": return "전체반품";
            case "PARTIAL_RETURN": return "부분반품";
            default: return "기타"; // 혹시 다른 값이 있을 경우
        }
    }

    @Override
    public Map<String, Long> getReasonCounts() {
        try {
            List<ReturnItem> allItems = returnItemRepository.findAll();
            return allItems.stream()
                    .filter(item -> item.getReturnReason() != null && !item.getReturnReason().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                        ReturnItem::getReturnReason,
                        Collectors.counting()
                    ));
        } catch (Exception e) {
            log.error("❌ 사유별 통계 조회 실패: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getAmountSummary() {
        try {
            List<ReturnItem> allItems = returnItemRepository.findAll();
            
            Map<String, Object> summary = new HashMap<>();
            
            // 환불금액 합계
            Long totalRefundAmount = allItems.stream()
                    .filter(item -> item.getRefundAmount() != null)
                    .mapToLong(ReturnItem::getRefundAmount)
                    .sum();
            
            // 🎯 배송비 숫자 합계 (Repository 쿼리 사용 - 텍스트 제외)
            Long totalShippingFee = returnItemRepository.getTotalNumericShippingFee();
            
            summary.put("totalRefundAmount", totalRefundAmount);
            summary.put("totalShippingFee", totalShippingFee);
            summary.put("totalItems", (long) allItems.size());
            
            return summary;
        } catch (Exception e) {
            log.error("❌ 금액 요약 통계 조회 실패: {}", e.getMessage());
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRefundAmount", 0L);
            summary.put("totalShippingFee", 0L);
            return summary;
        }
    }

    @Override
    public Map<String, Long> getBrandCounts() {
        try {
            List<ReturnItem> allItems = returnItemRepository.findAll();
            
            Map<String, Long> brandCounts = new HashMap<>();
            brandCounts.put("레노마", 0L);
            brandCounts.put("코랄리크", 0L);
            brandCounts.put("기타", 0L);
            
            for (ReturnItem item : allItems) {
                String siteName = item.getSiteName();
                if (siteName != null) {
                    if (siteName.contains("레노마")) {
                        brandCounts.put("레노마", brandCounts.get("레노마") + 1);
                    } else if (siteName.contains("코랄리크")) {
                        brandCounts.put("코랄리크", brandCounts.get("코랄리크") + 1);
                    } else {
                        brandCounts.put("기타", brandCounts.get("기타") + 1);
                    }
                }
            }
            
            return brandCounts;
        } catch (Exception e) {
            log.error("❌ 브랜드별 통계 조회 실패: {}", e.getMessage());
            Map<String, Long> defaultCounts = new HashMap<>();
            defaultCounts.put("레노마", 0L);
            defaultCounts.put("코랄리크", 0L);
            defaultCounts.put("기타", 0L);
            return defaultCounts;
        }
    }

    @Override
    public Long getTodayCount() {
        try {
            // 🎯 오늘 실제 등록 건수 조회 (Repository 사용)
            return returnItemRepository.countTodayItems();
        } catch (Exception e) {
            log.error("❌ 금일 등록 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }
    
    @Override
    public Long getTotalCount() {
        try {
            // 🎯 전체 등록 건수 조회 (Repository 사용)
            return returnItemRepository.count();
        } catch (Exception e) {
            log.error("❌ 전체 등록 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }
    
    @Override
    public Long getUntilYesterdayCount() {
        try {
            // 🎯 어제까지 등록 건수 조회
            return returnItemRepository.countUntilYesterdayItems();
        } catch (Exception e) {
            log.error("❌ 어제까지 등록 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }
    
    @Override
    public Double getCompletionRate() {
        try {
            // 🎯 완료율 계산: (완료 건수 / 전체 건수) * 100
            Long totalCount = returnItemRepository.countAllItems();
            if (totalCount == 0) {
                return 0.0;
            }
            Long completedCount = returnItemRepository.countByIsCompletedTrue();
            return (completedCount.doubleValue() / totalCount.doubleValue()) * 100.0;
        } catch (Exception e) {
            log.error("❌ 완료율 계산 실패: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public Long getCollectionCompletedCount() {
        try {
            return returnItemRepository.countByCollectionCompletedDateIsNotNull();
        } catch (Exception e) {
            log.error("❌ 회수완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getCollectionPendingCount() {
        try {
            return returnItemRepository.countByCollectionCompletedDateIsNull();
        } catch (Exception e) {
            log.error("❌ 회수미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getLogisticsConfirmedCount() {
        try {
            return returnItemRepository.countByLogisticsConfirmedDateIsNotNull();
        } catch (Exception e) {
            log.error("물류확인 완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getLogisticsPendingCount() {
        try {
            return returnItemRepository.countByLogisticsConfirmedDateIsNull();
        } catch (Exception e) {
            log.error("❌ 물류확인 미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getExchangeShippedCount() {
        try {
            return returnItemRepository.countExchangeShipped();
        } catch (Exception e) {
            log.error("❌ 교환 출고완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getExchangeNotShippedCount() {
        try {
            return returnItemRepository.countExchangeNotShipped();
        } catch (Exception e) {
            log.error("❌ 교환 출고미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getReturnRefundedCount() {
        try {
            return returnItemRepository.countReturnRefunded();
        } catch (Exception e) {
            log.error("❌ 반품 환불완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getReturnNotRefundedCount() {
        try {
            return returnItemRepository.countReturnNotRefunded();
        } catch (Exception e) {
            log.error("❌ 반품 환불미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getPaymentCompletedCount() {
        try {
            return returnItemRepository.countExchangePaymentCompleted();
        } catch (Exception e) {
            log.error("❌ 배송비 입금완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getPaymentPendingCount() {
        try {
            return returnItemRepository.countExchangePaymentPending();
        } catch (Exception e) {
            log.error("❌ 배송비 입금대기 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getCompletedCount() {
        try {
            return returnItemRepository.countByIsCompletedTrue();
        } catch (Exception e) {
            log.error("❌ 완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getIncompletedCount() {
        try {
            return returnItemRepository.countByIsCompletedFalseOrNull();
        } catch (Exception e) {
            log.error("❌ 미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }
    
    @Override
    public Long getOverdueTenDaysCount() {
        try {
            // 10일 전 날짜 계산 (Sample에서 통합)
            LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
            long count = returnItemRepository.countOverdueTenDays(tenDaysAgo);
            log.info("🚨 10일 경과 미완료 건수: {}", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 10일 경과 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public int bulkUpdateDates(List<ReturnItemBulkDateUpdateDTO> updates) {
        int updatedCount = 0;
        
        log.info("📅 일괄 날짜 업데이트 시작 - 대상 건수: {}", updates.size());
        
        for (ReturnItemBulkDateUpdateDTO update : updates) {
            try {
                Optional<ReturnItem> itemOpt = returnItemRepository.findById(update.getId());
                if (itemOpt.isPresent()) {
                    ReturnItem item = itemOpt.get();
                    boolean hasChanges = false;
                    
                    // 🎯 변경된 필드만 선택적으로 업데이트 (프론트엔드에서 전송된 필드만)
                    String updatedBy = update.getUpdatedBy() != null ? update.getUpdatedBy() : "SYSTEM";
                    LocalDateTime now = LocalDateTime.now();
                    
                    // 프론트엔드에서 전송된 필드만 업데이트 (단순하게 처리)
                    // JavaScript에서 undefined !== undefined 체크로 변경된 필드만 전송하므로
                    // 모든 전송된 필드를 업데이트 (null 값 포함)
                    
                        item.setCollectionCompletedDate(update.getCollectionCompletedDate());
                    item.setCollectionUpdatedBy(updatedBy);
                    item.setCollectionUpdatedDate(now);
                    hasChanges = true;
                    log.debug("회수완료일 업데이트: ID={}, 값={}", update.getId(), update.getCollectionCompletedDate());
                    
                        item.setLogisticsConfirmedDate(update.getLogisticsConfirmedDate());
                    item.setLogisticsUpdatedBy(updatedBy);
                    item.setLogisticsUpdatedDate(now);
                    log.debug("물류확인일 업데이트: ID={}, 값={}", update.getId(), update.getLogisticsConfirmedDate());
                    
                        item.setShippingDate(update.getShippingDate());
                    item.setShippingUpdatedBy(updatedBy);
                    item.setShippingUpdatedDate(now);
                    log.debug("출고일 업데이트: ID={}, 값={}", update.getId(), update.getShippingDate());
                    
                        item.setRefundDate(update.getRefundDate());
                    item.setRefundUpdatedBy(updatedBy);
                    item.setRefundUpdatedDate(now);
                    log.debug("환불일 업데이트: ID={}, 값={}", update.getId(), update.getRefundDate());
                    
                    // 🆕 비고 업데이트 로직 추가
                    if (update.getRemarks() != null) {
                        item.setRemarks(update.getRemarks());
                        hasChanges = true;
                        log.debug("비고 업데이트: ID={}, 값={}", update.getId(), update.getRemarks());
                    }
                    
                    // 변경사항이 있을 때만 저장
                    if (hasChanges) {
                    returnItemRepository.save(item);
                    updatedCount++;
                        log.info("✅ 날짜 및 비고 업데이트 성공: ID={}", update.getId());
                    }
                } else {
                    log.warn("⚠️ 해당 ID의 아이템을 찾을 수 없음: {}", update.getId());
                }
            } catch (Exception e) {
                log.error("❌ 날짜 업데이트 실패 - ID: {}, 오류: {}", update.getId(), e.getMessage(), e);
            }
        }
        
        log.info("📅 일괄 날짜 업데이트 완료: 성공 {}/{}건", updatedCount, updates.size());
        return updatedCount;
    }

    @Override
    public boolean updateCompletionStatus(Long id, Boolean isCompleted) {
        try {
            Optional<ReturnItem> itemOpt = returnItemRepository.findById(id);
            if (itemOpt.isPresent()) {
                ReturnItem item = itemOpt.get();
                item.setIsCompleted(isCompleted);
                item.setUpdatedBy("SYSTEM");
                returnItemRepository.save(item);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("완료 상태 업데이트 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return false;
        }
    }



    @Override
    public Map<String, Object> validateAndCleanSearchParams(Map<String, Object> params) {
        Map<String, Object> cleanParams = new HashMap<>();
        
        if (params != null) {
            // keyword 정리
            if (params.get("keyword") != null) {
                String keyword = params.get("keyword").toString().trim();
                if (!keyword.isEmpty()) {
                    cleanParams.put("keyword", keyword);
                }
            }
            
            // 날짜 정리
            if (params.get("startDate") != null) {
                cleanParams.put("startDate", params.get("startDate"));
            }
            if (params.get("endDate") != null) {
                cleanParams.put("endDate", params.get("endDate"));
            }
            
            // 페이징 정보
            cleanParams.put("pageNum", params.getOrDefault("pageNum", 1));
            cleanParams.put("pageSize", params.getOrDefault("pageSize", 20));
        }
        
        return cleanParams;
    }

    @Override
    public Map<String, Object> getReturnItemList(Map<String, Object> searchParams) {
        // 기존 MyBatis 방식과 호환성을 위한 메서드
        Map<String, Object> result = new HashMap<>();
        try {
            // 페이징 처리
            int pageNum = 1;
            int pageSize = 20;
            
            if (searchParams.get("pageNum") != null) {
                pageNum = Integer.parseInt(searchParams.get("pageNum").toString());
            }
            if (searchParams.get("pageSize") != null) {
                pageSize = Integer.parseInt(searchParams.get("pageSize").toString());
            }
            
            Page<ReturnItemDTO> page = findAll(pageNum - 1, pageSize, "id", "DESC");
            
            result.put("success", true);
            result.put("list", page.getContent());
            result.put("totalCount", page.getTotalElements());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("totalPages", page.getTotalPages());
            result.put("hasNext", page.hasNext());
            result.put("hasPrev", page.hasPrevious());
            
        } catch (Exception e) {
            log.error("목록 조회 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("list", new ArrayList<>());
            result.put("totalCount", 0);
        }
        
        return result;
    }

    @Override
    public Object getReturnItemById(Long returnId) {
        return findById(returnId);
    }

    @Override
    public boolean updateReturnItemStatus(Long returnId, String returnStatusCode, String updatedBy) {
        try {
            Optional<ReturnItem> itemOpt = returnItemRepository.findById(returnId);
            if (itemOpt.isPresent()) {
                ReturnItem item = itemOpt.get();
                item.setReturnStatusCode(returnStatusCode);
                item.setUpdatedBy(updatedBy);
                returnItemRepository.save(item);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("상태 업데이트 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateReturnItemStatusBatch(List<Long> returnIds, String returnStatusCode, String updatedBy) {
        try {
            for (Long id : returnIds) {
                updateReturnItemStatus(id, returnStatusCode, updatedBy);
            }
            return true;
        } catch (Exception e) {
            log.error("일괄 상태 업데이트 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getReturnItemStatusStats() {
        // 상태별 통계를 Map 형태로 반환
        List<Map<String, Object>> stats = new ArrayList<>();
        Map<String, Long> statusCounts = getStatusCounts();
        
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("statusCode", entry.getKey());
            stat.put("count", entry.getValue());
            stats.add(stat);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getReturnItemTypeStats(String startDate, String endDate) {
        List<Map<String, Object>> stats = new ArrayList<>();
        try {
            // 교환/반품 타입별 통계 데이터 생성
            Map<String, Object> exchangeStat = new HashMap<>();
            exchangeStat.put("type", "교환");
            exchangeStat.put("count", getExchangeShippedCount() + getExchangeNotShippedCount());
            exchangeStat.put("percentage", 65.0); // 임시 데이터
            stats.add(exchangeStat);

            Map<String, Object> returnStat = new HashMap<>();
            returnStat.put("type", "반품");
            returnStat.put("count", getReturnRefundedCount() + getReturnNotRefundedCount());
            returnStat.put("percentage", 35.0); // 임시 데이터
            stats.add(returnStat);
            
        } catch (Exception e) {
            log.error("타입별 통계 조회 실패: {}", e.getMessage());
            // 기본값 반환
            Map<String, Object> defaultStat = new HashMap<>();
            defaultStat.put("type", "전체");
            defaultStat.put("count", 0L);
            defaultStat.put("percentage", 0.0);
            stats.add(defaultStat);
        }
        return stats;
    }

    // 변환 메서드들
    private List<ReturnItemDTO> convertToDTO(List<ReturnItem> items) {
        List<ReturnItemDTO> dtos = new ArrayList<>();
        for (ReturnItem item : items) {
            dtos.add(convertToDTO(item));
        }
        return dtos;
    }

    private ReturnItemDTO convertToDTO(ReturnItem item) {
        if (item == null) return null;
        
        return ReturnItemDTO.builder()
                .id(item.getId())
                .returnTypeCode(item.getReturnTypeCode())
                .orderDate(item.getOrderDate())
                .csReceivedDate(item.getCsReceivedDate())
                .siteName(item.getSiteName())
                .orderNumber(item.getOrderNumber())
                .refundAmount(item.getRefundAmount())
                .customerName(item.getCustomerName())
                .customerPhone(item.getCustomerPhone())
                .orderItemCode(item.getOrderItemCode())
                .productColor(item.getProductColor())
                .productSize(item.getProductSize())
                .quantity(item.getQuantity())
                .shippingFee(item.getShippingFee())
                .returnReason(item.getReturnReason())
                .defectDetail(item.getDefectDetail())
                .defectPhotoUrl(item.getDefectPhotoUrl())
                .trackingNumber(item.getTrackingNumber())
                .collectionCompletedDate(item.getCollectionCompletedDate())
                .logisticsConfirmedDate(item.getLogisticsConfirmedDate())
                .shippingDate(item.getShippingDate())
                .refundDate(item.getRefundDate())
                .isCompleted(item.getIsCompleted())
                .remarks(item.getRemarks())
                .returnStatusCode(item.getReturnStatusCode())
                .processor(item.getProcessor())
                .createDate(item.getCreateDate())
                .updateDate(item.getUpdateDate())
                .createdBy(item.getCreatedBy())
                .updatedBy(item.getUpdatedBy())
                .paymentStatus(item.getPaymentStatus())
                .paymentId(item.getPaymentId())
                .build();
    }

    private ReturnItem convertToEntity(ReturnItemDTO dto) {
        if (dto == null) return null;
        
        return ReturnItem.builder()
                .id(dto.getId())
                .returnTypeCode(dto.getReturnTypeCode())
                .orderDate(dto.getOrderDate())
                .csReceivedDate(dto.getCsReceivedDate())
                .siteName(dto.getSiteName())
                .orderNumber(dto.getOrderNumber())
                .refundAmount(dto.getRefundAmount())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .orderItemCode(dto.getOrderItemCode())
                .productColor(dto.getProductColor())
                .productSize(dto.getProductSize())
                .quantity(dto.getQuantity())
                .shippingFee(dto.getShippingFee())
                .returnReason(dto.getReturnReason())
                .defectDetail(dto.getDefectDetail())
                .defectPhotoUrl(dto.getDefectPhotoUrl())
                .trackingNumber(dto.getTrackingNumber())
                .collectionCompletedDate(dto.getCollectionCompletedDate())
                .logisticsConfirmedDate(dto.getLogisticsConfirmedDate())
                .shippingDate(dto.getShippingDate())
                .refundDate(dto.getRefundDate())
                .isCompleted(dto.getIsCompleted())
                .remarks(dto.getRemarks())
                .returnStatusCode(dto.getReturnStatusCode())
                .processor(dto.getProcessor())
                .createDate(dto.getCreateDate())
                .updateDate(dto.getUpdateDate())
                .createdBy(dto.getCreatedBy())
                .updatedBy(dto.getUpdatedBy())
                .paymentStatus(dto.getPaymentStatus())
                .paymentId(dto.getPaymentId())
                .build();
    }

    // 🎯 카드 필터링 메서드들 - sample 폴더에서 참고하여 구현
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByCollectionCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📦 회수완료 필터링 조회");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            log.info("🔢 Oracle ROWNUM 페이징 - page: {}, size: {}, startRow: {}, endRow: {}", 
                searchDTO.getPage(), searchDTO.getSize(), startRow, endRow);
            
            List<ReturnItem> entities = returnItemRepository.findByCollectionCompletedDateIsNotNull(startRow, endRow);
            long totalCount = returnItemRepository.countByCollectionCompletedDateIsNotNull();
            
            log.info("✅ 회수완료 조회 결과 - entities: {}, totalCount: {}", entities.size(), totalCount);
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 회수완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByCollectionPending(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📦 회수미완료 필터링 조회");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByCollectionCompletedDateIsNull(startRow, endRow);
            long totalCount = returnItemRepository.countByCollectionCompletedDateIsNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 회수미완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByLogisticsConfirmed(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("🚛 물류확인완료 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByLogisticsConfirmedDateIsNotNull(startRow, endRow);
            long totalCount = returnItemRepository.countByLogisticsConfirmedDateIsNotNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 물류확인완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByLogisticsPending(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("🚛 물류확인미완료 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByLogisticsConfirmedDateIsNull(startRow, endRow);
            long totalCount = returnItemRepository.countByLogisticsConfirmedDateIsNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 물류확인미완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByShippingCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📤 출고완료 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            log.info("🔢 출고완료 페이징 - page: {}, size: {}, startRow: {}, endRow: {}", 
                searchDTO.getPage(), searchDTO.getSize(), startRow, endRow);
            
            List<ReturnItem> entities = returnItemRepository.findExchangeShipped(startRow, endRow);
            long totalCount = returnItemRepository.countExchangeShipped();
            
            log.info("✅ 출고완료 조회 결과 - entities: {}, totalCount: {}", entities.size(), totalCount);
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 출고완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByShippingPending(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📤 출고대기 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findExchangeNotShipped(startRow, endRow);
            long totalCount = returnItemRepository.countExchangeNotShipped();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 출고대기 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByRefundCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("💰 환불완료 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            log.info("🔢 환불완료 페이징 - page: {}, size: {}, startRow: {}, endRow: {}", 
                searchDTO.getPage(), searchDTO.getSize(), startRow, endRow);
            
            List<ReturnItem> entities = returnItemRepository.findReturnRefunded(startRow, endRow);
            long totalCount = returnItemRepository.countReturnRefunded();
            
            log.info("✅ 환불완료 조회 결과 - entities: {}, totalCount: {}", entities.size(), totalCount);
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 환불완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByRefundPending(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("💰 환불대기 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            log.info("🔢 환불대기 페이징 - page: {}, size: {}, startRow: {}, endRow: {}", 
                searchDTO.getPage(), searchDTO.getSize(), startRow, endRow);
            
            List<ReturnItem> entities = returnItemRepository.findReturnNotRefunded(startRow, endRow);
            long totalCount = returnItemRepository.countReturnNotRefunded();
            
            log.info("✅ 환불대기 조회 결과 - entities: {}, totalCount: {}", entities.size(), totalCount);
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 환불대기 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByPaymentCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("💳 배송비 입금완료 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            log.info("🔢 입금완료 페이징 - page: {}, size: {}, startRow: {}, endRow: {}", 
                searchDTO.getPage(), searchDTO.getSize(), startRow, endRow);
            
            List<ReturnItem> entities = returnItemRepository.findExchangePaymentCompleted(startRow, endRow);
            long totalCount = returnItemRepository.countExchangePaymentCompleted();
            
            log.info("✅ 입금완료 조회 결과 - entities: {}, totalCount: {}", entities.size(), totalCount);
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 배송비 입금완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByPaymentPending(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("💳 배송비 입금대기 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            log.info("🔢 입금대기 페이징 - page: {}, size: {}, startRow: {}, endRow: {}", 
                searchDTO.getPage(), searchDTO.getSize(), startRow, endRow);
            
            List<ReturnItem> entities = returnItemRepository.findExchangePaymentPending(startRow, endRow);
            long totalCount = returnItemRepository.countExchangePaymentPending();
            
            log.info("✅ 입금대기 조회 결과 - entities: {}, totalCount: {}", entities.size(), totalCount);
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 배송비 입금대기 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("✅ 전체완료 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByIsCompletedTrue(startRow, endRow);
            long totalCount = returnItemRepository.countByIsCompletedTrue();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 전체완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByIncompleted(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("❌ 미완료 필터링 조회 (원본 Repository 방식)");
            
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByIsCompletedFalseOrNull(startRow, endRow);
            long totalCount = returnItemRepository.countByIsCompletedFalseOrNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::convertToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 미완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }
    
    /**
     * 🎯 MyBatis를 사용한 필터링 포함 검색 조건 기반 페이징 조회 (Oracle ROWNUM 방식)
     */
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findBySearchCriteria(Map<String, Object> requestParams) {
        try {
            log.info("🔍 MyBatis 필터링 검색 조건 페이징 조회: {}", requestParams);
            
            // 페이징 처리 (Oracle ROWNUM 방식)
            int page = (Integer) requestParams.getOrDefault("page", 0);
            int size = (Integer) requestParams.getOrDefault("size", 20);
            
            // 🔧 중첩된 searchParams 추출
            Map<String, Object> searchParams = new HashMap<>();
            if (requestParams.containsKey("searchParams")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedParams = (Map<String, Object>) requestParams.get("searchParams");
                searchParams.putAll(nestedParams);
                log.info("🔧 중첩된 searchParams 추출 완료: {}", searchParams);
            } else {
                // searchParams가 중첩되지 않은 경우 직접 사용
                searchParams.putAll(requestParams);
                log.info("🔧 일반 파라미터 사용: {}", searchParams);
            }
            
            // Oracle ROWNUM은 1부터 시작
            int startRow = page * size + 1;     // 1, 21, 41, 61...
            int endRow = (page + 1) * size;     // 20, 40, 60, 80...
            
            searchParams.put("startRow", startRow);
            searchParams.put("endRow", endRow);
            
            log.debug("📊 페이징 정보 - page: {}, size: {}, startRow: {}, endRow: {}", 
              page, size, startRow, endRow);
            log.debug("🔍 최종 검색 파라미터: {}", searchParams);
            
            // MyBatis 매퍼 호출로 필터링 조건 포함 조회
            List<ReturnItem> entities = returnItemMapper.findBySearchCriteria(searchParams);
            long totalCount = returnItemMapper.countBySearchCriteria(searchParams);
            
            log.info("📋 MyBatis 조회 결과 - entities: {}, totalCount: {}", entities.size(), totalCount);
            
            // DTO 변환
            List<ReturnItemDTO> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
            
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(dtos, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ MyBatis 필터링 검색 조건 조회 실패: {}", e.getMessage(), e);
            // 빈 페이지 반환
            int page = (Integer) requestParams.getOrDefault("page", 0);
            int size = (Integer) requestParams.getOrDefault("size", 20);
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(new ArrayList<>(), pageable, 0L);
        }
    }

    // ==================== 성능 최적화된 메서드들 ====================
    
    /**
     * 🚀 성능 최적화된 검색 (기존 인덱스 활용)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> search(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("🚀 성능 최적화된 검색 실행 - 조건: {}", searchDTO);
            
            Map<String, Object> searchParams = buildOptimizedSearchParams(searchDTO);
            
            // 최적화된 목록 조회 (인덱스 힌트 포함)
            List<ReturnItem> items = returnItemMapper.selectReturnItemListOptimized(searchParams);
            
            // 최적화된 COUNT 쿼리 (인덱스 활용)
            Long totalCount = returnItemMapper.selectReturnItemCountOptimized(searchParams);
            
            List<ReturnItemDTO> dtoList = convertToDTO(items);
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            return new PageImpl<>(dtoList, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("❌ 최적화된 검색 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }
    
    /**
     * 🚀 통합 대시보드 통계 (한 번의 쿼리로 모든 통계 조회)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        return getDashboardStats(null);
    }
    
    @Override
    @Transactional(readOnly = true) 
    public Map<String, Object> getDashboardStats(Map<String, Object> searchParams) {
        try {
            log.info("🚀 통합 대시보드 통계 조회 시작");
            long startTime = System.currentTimeMillis();
            
            // 한 번의 쿼리로 모든 통계 조회 (기존 12개 쿼리 → 1개 쿼리)
            Map<String, Object> stats = returnItemMapper.getDashboardStatsUnified(searchParams);
            
            long endTime = System.currentTimeMillis();
            log.info("✅ 통합 대시보드 통계 조회 완료 - 소요시간: {}ms", endTime - startTime);
            log.info("📊 쿼리 결과: {}", stats);
            
            return stats;
            
        } catch (Exception e) {
            log.error("❌ 통합 대시보드 통계 조회 실패: {}", e.getMessage(), e);
            
            // 실패 시 기본값 반환
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalCount", 0L);
            defaultStats.put("collectionCompletedCount", 0L);
            defaultStats.put("collectionPendingCount", 0L);
            defaultStats.put("logisticsConfirmedCount", 0L);
            defaultStats.put("logisticsPendingCount", 0L);
            defaultStats.put("exchangeShippedCount", 0L);
            defaultStats.put("exchangeNotShippedCount", 0L);
            defaultStats.put("returnRefundedCount", 0L);
            defaultStats.put("returnNotRefundedCount", 0L);
            defaultStats.put("paymentCompletedCount", 0L);
            defaultStats.put("paymentPendingCount", 0L);
            defaultStats.put("completedCount", 0L);
            defaultStats.put("incompletedCount", 0L);
            defaultStats.put("delayedCount", 0L);
            defaultStats.put("totalRefundAmount", 0L);
            defaultStats.put("totalShippingFee", 0L);
            defaultStats.put("avgRefundAmount", 0L);
            
            return defaultStats;
        }
    }
    
    /**
     * 🚀 성능 최적화된 검색 파라미터 구성
     */
    private Map<String, Object> buildOptimizedSearchParams(ReturnItemSearchDTO searchDTO) {
        Map<String, Object> params = new HashMap<>();
        
        // 페이징 파라미터
        int startRow = searchDTO.getPage() * searchDTO.getSize();
        int endRow = startRow + searchDTO.getSize();
        params.put("startRow", startRow);
        params.put("endRow", endRow);
        
        // 검색 조건들 (ReturnItemSearchDTO에 실제 존재하는 필드들만 사용)
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            params.put("keyword", searchDTO.getKeyword());
        }
        if (StringUtils.hasText(searchDTO.getReturnTypeCode())) {
            params.put("returnTypeCode", searchDTO.getReturnTypeCode());
        }
        if (StringUtils.hasText(searchDTO.getReturnStatusCode())) {
            params.put("returnStatusCode", searchDTO.getReturnStatusCode());
        }
        if (StringUtils.hasText(searchDTO.getPaymentStatus())) {
            params.put("paymentStatus", searchDTO.getPaymentStatus());
        }
        if (StringUtils.hasText(searchDTO.getSiteName())) {
            params.put("siteName", searchDTO.getSiteName());
        }
        if (searchDTO.getStartDate() != null) {
            params.put("startDate", searchDTO.getStartDate().toString());
        }
        if (searchDTO.getEndDate() != null) {
            params.put("endDate", searchDTO.getEndDate().toString());
        }
        // 물류확인일 검색 조건
        if (searchDTO.getLogisticsStartDate() != null) {
            params.put("logisticsStartDate", searchDTO.getLogisticsStartDate().toString());
        }
        if (searchDTO.getLogisticsEndDate() != null) {
            params.put("logisticsEndDate", searchDTO.getLogisticsEndDate().toString());
        }
        
        // 🎯 필터 조건 추가
        if (StringUtils.hasText(searchDTO.getFilters())) {
            params.put("filters", searchDTO.getFilters());
        }
        
        // 정렬 정보
        params.put("sortBy", searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "id");
        params.put("sortDir", searchDTO.getSortDir() != null ? searchDTO.getSortDir() : "DESC");
        
        return params;
    }
    
    /**
     * 🚀 실시간 8개 통계 조회 구현 (신규)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRealtimeStats() {
        try {
            log.info("🚀 실시간 8개 통계 조회 시작");
            long startTime = System.currentTimeMillis();
            
            Map<String, Object> stats = new HashMap<>();
            
            // 1️⃣ 전체 건수 통계 (오늘/어제까지)
            long totalCount = returnItemRepository.count();
            long todayCount = getTodayCount();
            long untilYesterdayCount = getUntilYesterdayCount();
            
            Map<String, Object> totalStats = new HashMap<>();
            totalStats.put("total", totalCount);
            totalStats.put("today", todayCount);
            totalStats.put("untilYesterday", untilYesterdayCount);
            stats.put("totalCount", totalStats);
            
            // 2️⃣ 교환반품 현황 (완료/미완료)
            long completedCount = getCompletedCount();
            long incompleteCount = getIncompletedCount();
            double completionRate = getCompletionRate();
            
            Map<String, Object> completionStats = new HashMap<>();
            completionStats.put("completed", completedCount);
            completionStats.put("incomplete", incompleteCount);
            completionStats.put("completionRate", Math.round(completionRate));
            stats.put("completionStatus", completionStats);
            
            // 3️⃣ 유형별 현황
            Map<String, Long> typeStats = getTypeCounts();
            stats.put("typeStats", typeStats);
            
            // 4️⃣ 배송비 합계 (숫자 데이터만)
            Map<String, Object> amountSummary = getAmountSummary();
            Long totalShippingFee = (Long) amountSummary.getOrDefault("totalShippingFee", 0L);
            stats.put("totalShippingFee", totalShippingFee);
            
            // 5️⃣ 환불금 합계 및 평균
            Long totalRefundAmount = (Long) amountSummary.getOrDefault("totalRefundAmount", 0L);
            stats.put("totalRefundAmount", totalRefundAmount);
            
            // 평균 환불금액 계산
            long avgRefundAmount = totalCount > 0 ? totalRefundAmount / totalCount : 0;
            stats.put("avgRefundAmount", avgRefundAmount);
            
            // 6️⃣ 반품교환 사유 통계
            Map<String, Long> reasonStats = getReasonCounts();
            stats.put("reasonStats", reasonStats);
            
            // 7️⃣ 사이트별 통계
            Map<String, Long> siteStats = getSiteCounts();
            stats.put("siteStats", siteStats);
            
            // 8️⃣ 브랜드별 통계
            Map<String, Long> brandStats = getBrandCounts();
            stats.put("brandStats", brandStats);
            
            // 📊 추가 통계 정보
            Map<String, Object> additionalStats = new HashMap<>();
            additionalStats.put("collectionCompleted", getCollectionCompletedCount());
            additionalStats.put("collectionPending", getCollectionPendingCount());
            additionalStats.put("logisticsConfirmed", getLogisticsConfirmedCount());
            additionalStats.put("logisticsPending", getLogisticsPendingCount());
            additionalStats.put("exchangeShipped", getExchangeShippedCount());
            additionalStats.put("exchangeNotShipped", getExchangeNotShippedCount());
            additionalStats.put("returnRefunded", getReturnRefundedCount());
            additionalStats.put("returnNotRefunded", getReturnNotRefundedCount());
            additionalStats.put("paymentCompleted", getPaymentCompletedCount());
            additionalStats.put("paymentPending", getPaymentPendingCount());
            // 🚨 처리기간 임박 통계 추가
            additionalStats.put("overdueTenDaysCount", getOverdueTenDaysCount());
            stats.put("processStats", additionalStats);
            
            long endTime = System.currentTimeMillis();
            stats.put("queryTime", endTime - startTime);
            
            log.info("✅ 실시간 8개 통계 조회 완료 - 소요시간: {}ms", endTime - startTime);
            log.debug("📊 통계 결과: {}", stats);
            
            return stats;
            
        } catch (Exception e) {
            log.error("❌ 실시간 통계 조회 실패: {}", e.getMessage(), e);
            
            // 실패 시 기본값 반환
            Map<String, Object> defaultStats = new HashMap<>();
            
            Map<String, Object> totalStats = new HashMap<>();
            totalStats.put("total", 0L);
            totalStats.put("today", 0L);
            totalStats.put("untilYesterday", 0L);
            defaultStats.put("totalCount", totalStats);
            
            Map<String, Object> completionStats = new HashMap<>();
            completionStats.put("completed", 0L);
            completionStats.put("incomplete", 0L);
            completionStats.put("completionRate", 0);
            defaultStats.put("completionStatus", completionStats);
            
            defaultStats.put("typeStats", new HashMap<>());
            defaultStats.put("totalShippingFee", 0L);
            defaultStats.put("totalRefundAmount", 0L);
            defaultStats.put("reasonStats", new HashMap<>());
            defaultStats.put("siteStats", new HashMap<>());
            defaultStats.put("brandStats", new HashMap<>());
            defaultStats.put("processStats", new HashMap<>());
            defaultStats.put("queryTime", 0L);
            
            return defaultStats;
        }
    }

    
    // ✅ ExchangeStatsRequestDto 관련 메서드들 제거 완료 (새로운 ExchangeStatsService 사용)
    
    // getTrendData(ExchangeStatsRequestDto) 메서드 제거됨
    
    // getTypeCounts(ExchangeStatsRequestDto) 메서드 제거됨
    
    // ExchangeStatsRequestDto 관련 메서드들은 새로운 ExchangeStatsService로 이전됨
    
    // ========== ExchangeStatsRequestDto 관련 메서드들 모두 새로운 ExchangeStatsService로 이전됨 ==========
    
    // ========== 나머지 ExchangeStatsRequestDto 관련 메서드들도 제거 ==========

    // ============================================================================
    // 🎯 검색 조건 기반 통계 메서드들 (ReturnItemSearchDTO 사용)
    // ============================================================================

    /**
     * 검색 조건에 맞는 데이터 조회 (ReturnItemSearchDTO용)
     */
    private List<ReturnItemDTO> getFilteredData(ReturnItemSearchDTO searchDTO) {
        try {
            if (searchDTO == null || !searchDTO.hasSearchCondition()) {
                // 검색 조건이 없으면 전체 데이터 조회 (첫 5000건만)
                Page<ReturnItemDTO> page = findAll(0, 5000, "id", "DESC");
                return page.getContent();
            } else {
                // 검색 조건이 있으면 검색 결과 조회 (첫 5000건만)
                Page<ReturnItemDTO> page = search(searchDTO);
                return page.getContent();
            }
        } catch (Exception e) {
            log.error("❌ 필터링된 데이터 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 📈 트렌드 차트용 경량 데이터 조회 (성능 최적화)
     */
    private List<Map<String, Object>> getFilteredDataForTrend(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📈 트렌드 차트용 경량 데이터 조회 - 조건: {}", searchDTO);
            
            // 검색 파라미터 구성 (페이징 제외)
            Map<String, Object> searchParams = new HashMap<>();
            
            if (searchDTO.getStartDate() != null) {
                searchParams.put("startDate", searchDTO.getStartDate().toString());
            }
            if (searchDTO.getEndDate() != null) {
                searchParams.put("endDate", searchDTO.getEndDate().toString());
            }
            if (StringUtils.hasText(searchDTO.getKeyword())) {
                searchParams.put("keyword", searchDTO.getKeyword());
            }
            if (StringUtils.hasText(searchDTO.getReturnTypeCode())) {
                searchParams.put("returnTypeCode", searchDTO.getReturnTypeCode());
            }
            if (StringUtils.hasText(searchDTO.getReturnStatusCode())) {
                searchParams.put("returnStatusCode", searchDTO.getReturnStatusCode());
            }
            if (StringUtils.hasText(searchDTO.getPaymentStatus())) {
                searchParams.put("paymentStatus", searchDTO.getPaymentStatus());
            }
            if (StringUtils.hasText(searchDTO.getSiteName())) {
                searchParams.put("siteName", searchDTO.getSiteName());
            }
            
            // 🚨 DEBUG: searchParams 상세 출력
            log.info("🚨 DEBUG: searchParams 전체 내용: {}", searchParams);
            log.info("🚨 DEBUG: searchParams.size(): {}", searchParams.size());
            for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
                log.info("🚨 DEBUG: searchParams['{}'] = '{}' (타입: {})", 
                    entry.getKey(), entry.getValue(), 
                    entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
            }
            
            // 트렌드 차트용 경량 데이터 조회 (페이징 없음, 컬럼 3개만)
            List<Map<String, Object>> items = returnItemMapper.selectReturnItemListForTrend(searchParams);
            
            log.info("📈 트렌드 차트용 경량 데이터 조회 완료 - 총 {} 건", items.size());
            
            // 🚨 DEBUG: 조회 결과 샘플 출력 (최대 3개)
            if (!items.isEmpty()) {
                log.info("🚨 DEBUG: 조회 결과 샘플 (최대 3개):");
                for (int i = 0; i < Math.min(3, items.size()); i++) {
                    log.info("🚨 DEBUG: items[{}] = {}", i, items.get(i));
                }
            } else {
                log.warn("🚨 WARNING: 트렌드 조회 결과가 0건입니다! 검색 조건을 확인하세요.");
            }
            
            return items;
            
        } catch (Exception e) {
            log.error("❌ 트렌드 차트용 경량 데이터 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusCountsBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 상태별 통계 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            Map<String, Long> statusCounts = new HashMap<>();
            for (ReturnItemDTO item : filteredData) {
                String status = item.getReturnStatusCode();
                if (status != null && !status.trim().isEmpty()) {
                    statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
                }
            }
            
            log.info("✅ 검색 조건 기반 상태별 통계: {}", statusCounts);
            return statusCounts;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 상태별 통계 조회 실패: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getSiteCountsBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 사이트별 통계 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            Map<String, Long> siteCounts = new HashMap<>();
            for (ReturnItemDTO item : filteredData) {
                String siteName = item.getSiteName();
                if (siteName != null && !siteName.trim().isEmpty()) {
                    siteCounts.put(siteName, siteCounts.getOrDefault(siteName, 0L) + 1);
                }
            }
            
            log.info("✅ 검색 조건 기반 사이트별 통계: {}", siteCounts);
            return siteCounts;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 사이트별 통계 조회 실패: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTypeCountsBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 유형별 통계 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            Map<String, Long> typeCounts = new HashMap<>();
            for (ReturnItemDTO item : filteredData) {
                String typeCode = item.getReturnTypeCode();
                if (typeCode != null && !typeCode.trim().isEmpty()) {
                    String typeLabel = convertTypeCodeToLabel(typeCode);
                    typeCounts.put(typeLabel, typeCounts.getOrDefault(typeLabel, 0L) + 1);
                }
            }
            
            log.info("✅ 검색 조건 기반 유형별 통계: {}", typeCounts);
            return typeCounts;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 유형별 통계 조회 실패: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getReasonCountsBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 사유별 통계 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            Map<String, Long> reasonCounts = new HashMap<>();
            for (ReturnItemDTO item : filteredData) {
                String reason = item.getReturnReason();
                if (reason != null && !reason.trim().isEmpty()) {
                    reasonCounts.put(reason, reasonCounts.getOrDefault(reason, 0L) + 1);
                }
            }
            
            log.info("✅ 검색 조건 기반 사유별 통계: {}", reasonCounts);
            return reasonCounts;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 사유별 통계 조회 실패: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAmountSummaryBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("💰 검색 조건 기반 금액 요약 조회 시작 - 조건: {}", searchDTO);
            log.info("💰 검색 조건 세부사항 - startDate: {}, endDate: {}, returnType: {}", 
                searchDTO.getStartDate(), searchDTO.getEndDate(), searchDTO.getReturnTypeCode());
            
            // 🔥 성능 최적화: DB 직접 집계 쿼리 사용 (페이징 없이 전체 데이터 기준)
            Map<String, Object> searchParams = new HashMap<>();
            
            if (searchDTO.getStartDate() != null) {
                searchParams.put("startDate", searchDTO.getStartDate().toString());
            }
            if (searchDTO.getEndDate() != null) {
                searchParams.put("endDate", searchDTO.getEndDate().toString());
            }
            if (StringUtils.hasText(searchDTO.getReturnTypeCode())) {
                searchParams.put("returnTypeCode", searchDTO.getReturnTypeCode());
            }
            if (StringUtils.hasText(searchDTO.getBrandFilter())) {
                searchParams.put("brandFilter", searchDTO.getBrandFilter());
            }
            
            log.info("💰 DB 직접 집계 쿼리 파라미터: {}", searchParams);
            
            // DB에서 직접 SUM 쿼리로 금액 통계 조회
            Map<String, Object> amountStats = returnItemMapper.getAmountStatsBySearch(searchParams);
            
            if (amountStats == null) {
                amountStats = new HashMap<>();
            }
            
            // 기본값 설정 (NULL 처리)
            Long totalRefundAmount = getLongValue(amountStats, "TOTAL_REFUND_AMOUNT");
            Long totalShippingFee = getLongValue(amountStats, "TOTAL_SHIPPING_FEE"); 
            Long refundCount = getLongValue(amountStats, "REFUND_COUNT");
            Long totalCount = getLongValue(amountStats, "TOTAL_COUNT");
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRefundAmount", totalRefundAmount);
            summary.put("totalShippingFee", totalShippingFee);
            summary.put("avgRefundAmount", refundCount > 0 ? totalRefundAmount / refundCount : 0L);
            summary.put("totalCount", totalCount);
            
            log.info("💰 DB 직접 집계 결과:");
            log.info("  - 총 환불금액: {} 원 ({} 건에서 집계)", totalRefundAmount, refundCount);
            log.info("  - 총 배송비: {} 원", totalShippingFee);
            log.info("  - 평균 환불금액: {} 원", refundCount > 0 ? totalRefundAmount / refundCount : 0L);
            log.info("  - 전체 건수: {} 건", totalCount);
            log.info("✅ 검색 조건 기반 금액 요약 완료: {}", summary);
            
            return summary;
            
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 금액 요약 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> emptySummary = new HashMap<>();
            emptySummary.put("totalRefundAmount", 0L);
            emptySummary.put("totalShippingFee", 0L);
            emptySummary.put("avgRefundAmount", 0L);
            emptySummary.put("totalCount", 0L);
            return emptySummary;
        }
    }
    
    /**
     * Map에서 Long 값 안전하게 추출
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("⚠️ Long 변환 실패: {} = {}", key, value);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getBrandCountsBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 브랜드별 통계 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            Map<String, Long> brandCounts = new HashMap<>();
            for (ReturnItemDTO item : filteredData) {
                String siteName = item.getSiteName();
                if (siteName != null) {
                    String brand = extractBrandFromSiteName(siteName);
                    if (brand != null) {
                        brandCounts.put(brand, brandCounts.getOrDefault(brand, 0L) + 1);
                    }
                }
            }
            
            log.info("✅ 검색 조건 기반 브랜드별 통계: {}", brandCounts);
            return brandCounts;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 브랜드별 통계 조회 실패: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTodayCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 금일 등록 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            LocalDate today = LocalDate.now();
            long todayCount = filteredData.stream()
                    .filter(item -> item.getCreateDate() != null)
                    .filter(item -> item.getCreateDate().toLocalDate().equals(today))
                    .count();
            
            log.info("✅ 검색 조건 기반 금일 등록 건수: {} 건", todayCount);
            return todayCount;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 금일 등록 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCollectionCompletedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 회수완료 건수 조회 - 조건: {}", searchDTO);
            Long count = returnItemMapper.getCollectionCompletedCountBySearch(
                searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null,
                searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null,
                searchDTO.getReturnTypeCode(),
                searchDTO.getBrandFilter()
            );
            log.info("✅ 검색 조건 기반 회수완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 회수완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCollectionPendingCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 회수미완료 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long count = filteredData.stream()
                    .filter(item -> item.getCollectionCompletedDate() == null)
                    .count();
            
            log.info("✅ 검색 조건 기반 회수미완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 회수미완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLogisticsConfirmedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 물류확인 완료 건수 조회 - 조건: {}", searchDTO);
            Long count = returnItemMapper.getLogisticsConfirmedCountBySearch(
                searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null,
                searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null,
                searchDTO.getReturnTypeCode(),
                searchDTO.getBrandFilter()
            );
            log.info("✅ 검색 조건 기반 물류확인 완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 물류확인 완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLogisticsPendingCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 물류확인 미완료 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long count = filteredData.stream()
                    .filter(item -> item.getLogisticsConfirmedDate() == null)
                    .count();
            
            log.info("✅ 검색 조건 기반 물류확인 미완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 물류확인 미완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getExchangeShippedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 교환 출고완료 건수 조회 - 조건: {}", searchDTO);
            Long count = returnItemMapper.getExchangeShippedCountBySearch(
                searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null,
                searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null,
                searchDTO.getReturnTypeCode(),
                searchDTO.getBrandFilter()
            );
            log.info("✅ 검색 조건 기반 교환 출고완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 교환 출고완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getExchangeNotShippedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 교환 출고미완료 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long count = filteredData.stream()
                    .filter(item -> isExchangeType(item.getReturnTypeCode()))
                    .filter(item -> item.getShippingDate() == null)
                    .count();
            
            log.info("✅ 검색 조건 기반 교환 출고미완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 교환 출고미완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReturnRefundedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 반품 환불완료 건수 조회 - 조건: {}", searchDTO);
            Long count = returnItemMapper.getReturnRefundedCountBySearch(
                searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null,
                searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null,
                searchDTO.getReturnTypeCode(),
                searchDTO.getBrandFilter()
            );
            log.info("✅ 검색 조건 기반 반품 환불완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 반품 환불완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReturnNotRefundedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 반품 환불미완료 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long count = filteredData.stream()
                    .filter(item -> isReturnType(item.getReturnTypeCode()))
                    .filter(item -> item.getRefundDate() == null)
                    .count();
            
            log.info("✅ 검색 조건 기반 반품 환불미완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 반품 환불미완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPaymentCompletedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 배송비 입금완료 건수 조회 - 조건: {}", searchDTO);
            Long count = returnItemMapper.getPaymentCompletedCountBySearch(
                searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null,
                searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null,
                searchDTO.getReturnTypeCode(),
                searchDTO.getBrandFilter()
            );
            log.info("✅ 검색 조건 기반 배송비 입금완료 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 배송비 입금완료 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPaymentPendingCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 배송비 입금대기 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long count = filteredData.stream()
                    .filter(item -> "입금예정".equals(item.getShippingFee()))
                    .count();
            
            log.info("✅ 검색 조건 기반 배송비 입금대기 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 배송비 입금대기 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCompletedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 매퍼 직접 조회 방식으로 완료 건수 조회 - 조건: {}", searchDTO);
            
            // 매퍼 메서드 파라미터 준비 (LocalDate → String 변환)
            String startDate = searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null;
            String endDate = searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null;
            String returnTypeCode = searchDTO.getReturnTypeCode();
            String brandFilter = searchDTO.getBrandFilter();
            
            log.info("📋 매퍼 파라미터: startDate={}, endDate={}, returnType={}, brand={}", 
                    startDate, endDate, returnTypeCode, brandFilter);
            
            // 매퍼에서 직접 IS_COMPLETED = 1 조회
            Long completedCount = returnItemMapper.getCompletedCountBySearch(startDate, endDate, returnTypeCode, brandFilter);
            
            log.info("✅ 매퍼 직접 조회 완료 건수: {} 건", completedCount);
            return completedCount != null ? completedCount : 0L;
            
        } catch (Exception e) {
            log.error("❌ 매퍼 직접 조회 완료 건수 실패: {}", e.getMessage(), e);
            
            // 실패 시 기존 방식으로 폴백
            log.info("🔄 기존 스트림 방식으로 폴백 시도...");
            return getCompletedCountBySearchFallback(searchDTO);
        }
    }
    
    // 폴백 메서드 (기존 방식)
    private Long getCompletedCountBySearchFallback(ReturnItemSearchDTO searchDTO) {
        try {
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long trueCount = filteredData.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getIsCompleted()))
                    .count();
            
            long falseCount = filteredData.stream()
                    .filter(item -> Boolean.FALSE.equals(item.getIsCompleted()))
                    .count();
            
            long nullCount = filteredData.stream()
                    .filter(item -> item.getIsCompleted() == null)
                    .count();
            
            log.info("🔍 폴백 방식 IS_COMPLETED 분석: TRUE={}, FALSE={}, NULL={}", trueCount, falseCount, nullCount);
            
            return trueCount;
        } catch (Exception e) {
            log.error("❌ 폴백 방식도 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getIncompletedCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 매퍼 직접 조회 방식으로 미완료 건수 조회 - 조건: {}", searchDTO);
            
            // 매퍼 메서드 파라미터 준비 (LocalDate → String 변환)
            String startDate = searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null;
            String endDate = searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null;
            String returnTypeCode = searchDTO.getReturnTypeCode();
            String brandFilter = searchDTO.getBrandFilter();
            
            log.info("📋 매퍼 파라미터: startDate={}, endDate={}, returnType={}, brand={}", 
                    startDate, endDate, returnTypeCode, brandFilter);
            
            // 매퍼에서 직접 IS_COMPLETED = 0 OR NULL 조회
            Long incompletedCount = returnItemMapper.getIncompletedCountBySearch(startDate, endDate, returnTypeCode, brandFilter);
            
            log.info("✅ 매퍼 직접 조회 미완료 건수: {} 건", incompletedCount);
            return incompletedCount != null ? incompletedCount : 0L;
            
        } catch (Exception e) {
            log.error("❌ 매퍼 직접 조회 미완료 건수 실패: {}", e.getMessage(), e);
            
            // 실패 시 기존 방식으로 폴백
            log.info("🔄 기존 스트림 방식으로 폴백 시도...");
            return getIncompletedCountBySearchFallback(searchDTO);
        }
    }
    
    // 폴백 메서드 (기존 방식)
    private Long getIncompletedCountBySearchFallback(ReturnItemSearchDTO searchDTO) {
        try {
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long incompletedCount = filteredData.stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getIsCompleted()))
                    .count();
            
            log.info("🔍 폴백 방식 미완료 건수: {} 건", incompletedCount);
            return incompletedCount;
            
        } catch (Exception e) {
            log.error("❌ 폴백 방식 미완료 건수도 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 교환 유형인지 확인
     */
    private boolean isExchangeType(String returnTypeCode) {
        return "PARTIAL_EXCHANGE".equals(returnTypeCode) || 
               "FULL_EXCHANGE".equals(returnTypeCode) || 
               "EXCHANGE".equals(returnTypeCode);
    }

    /**
     * 반품 유형인지 확인
     */
    private boolean isReturnType(String returnTypeCode) {
        return "PARTIAL_RETURN".equals(returnTypeCode) || 
               "FULL_RETURN".equals(returnTypeCode) || 
               "RETURN".equals(returnTypeCode);
    }

    /**
     * 사이트명에서 브랜드 추출
     */
    private String extractBrandFromSiteName(String siteName) {
        if (siteName == null) return null;
        
        if (siteName.contains("레노마")) {
            return "레노마";
        } else if (siteName.contains("코랄리크")) {
            return "코랄리크";
        } else if (siteName.contains("라코스테")) {
            return "라코스테";
        } else if (siteName.contains("폴햄")) {
            return "폴햄";
        } else {
            return "기타";
        }
    }

    // 📊 통계 페이지 전용 추가 메서드 구현

    @Override
    @Transactional(readOnly = true)
    public Long getExchangeCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 교환 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long count = filteredData.stream()
                    .filter(item -> isExchangeType(item.getReturnTypeCode()))
                    .count();
            
            log.info("✅ 검색 조건 기반 교환 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 교환 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReturnCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 반품 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            long count = filteredData.stream()
                    .filter(item -> isReturnType(item.getReturnTypeCode()))
                    .count();
            
            log.info("✅ 검색 조건 기반 반품 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 반품 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPaymentRequiredCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 배송비 필요 건수 조회 - 조건: {}", searchDTO);
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            
            // 교환 건 중에서 배송비 입금이 필요한 건수 (PENDING 또는 COMPLETED)
            long count = filteredData.stream()
                    .filter(item -> isExchangeType(item.getReturnTypeCode()))
                    .filter(item -> "PENDING".equals(item.getPaymentStatus()) || 
                                   "COMPLETED".equals(item.getPaymentStatus()))
                    .count();
            
            log.info("✅ 검색 조건 기반 배송비 필요 건수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 배송비 필요 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCountByTypeAndSearch(String returnType, ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 최적화된 유형별 건수 조회 - 유형: {}, 조건: {}", returnType, searchDTO);
            
            // 🚀 매퍼 직접 COUNT 쿼리 사용 (성능 최적화)
            Map<String, Object> searchParams = buildOptimizedSearchParams(searchDTO);
            
            // 매퍼 쿼리 호출
            Long count = returnItemMapper.getCountByTypeAndSearch(returnType, searchParams);
            
            log.info("✅ 최적화된 유형별 건수 ({}): {} 건", returnType, count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("❌ 최적화된 유형별 건수 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTrendDataBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건 기반 트렌드 데이터 조회 (경량화) - 조건: {}", searchDTO);
            
            // 🚨 DEBUG: searchDTO 상세 분석
            log.info("🚨 DEBUG: searchDTO != null? {}", searchDTO != null);
            if (searchDTO != null) {
                log.info("🚨 DEBUG: searchDTO.getStartDate(): {}", searchDTO.getStartDate());
                log.info("🚨 DEBUG: searchDTO.getEndDate(): {}", searchDTO.getEndDate());
                log.info("🚨 DEBUG: searchDTO.getReturnTypeCode(): {}", searchDTO.getReturnTypeCode());
                log.info("🚨 DEBUG: searchDTO.hasSearchCondition(): {}", searchDTO.hasSearchCondition());
            }
            
            // 페이징 없는 경량 데이터 조회 사용
            List<Map<String, Object>> filteredData = getFilteredDataForTrend(searchDTO);
            
            log.info("📊 필터링된 경량 데이터 수: {} 건", filteredData.size());
            
            // 🔍 실제 데이터가 있는 날짜 범위 출력
            if (!filteredData.isEmpty()) {
                LocalDate minDate = filteredData.stream()
                        .map(item -> parseLocalDate(item.get("CSRECEIVEDDATE")))
                        .filter(date -> date != null)
                        .min(LocalDate::compareTo)
                        .orElse(null);
                        
                LocalDate maxDate = filteredData.stream()
                        .map(item -> parseLocalDate(item.get("CSRECEIVEDDATE")))
                        .filter(date -> date != null)
                        .max(LocalDate::compareTo)
                        .orElse(null);
                        
                log.info("🔍 실제 데이터 날짜 범위: {} ~ {}", minDate, maxDate);
                
                // 샘플 데이터 출력
                Map<String, Object> sampleItem = filteredData.get(0);
                LocalDate sampleDate = parseLocalDate(sampleItem.get("CSRECEIVEDDATE"));
                String sampleType = (String) sampleItem.get("RETURNTYPECODE");
                log.info("📊 샘플 데이터 - {}: {}", sampleDate, sampleType);
            } else {
                log.warn("⚠️ 경량 데이터가 0건입니다!");
            }
            
            // 🔧 조회조건에 맞는 전체 날짜 범위 생성
            LocalDate startDate = searchDTO.getStartDate();
            LocalDate endDate = searchDTO.getEndDate();
            
            // 조회조건이 없으면 기본 기간 설정 (최근 7일)
            if (startDate == null || endDate == null) {
                endDate = LocalDate.now();
                startDate = endDate.minusDays(6);
                log.info("📅 조회조건 없음 - 기본 기간 설정: {} ~ {}", startDate, endDate);
            }
            
            log.info("📅 트렌드 차트 기간 설정: {} ~ {} ({} 일)", startDate, endDate, 
                    ChronoUnit.DAYS.between(startDate, endDate) + 1);
            
            // CS접수일 기준으로 데이터 그룹화 (Map 방식)
            Map<LocalDate, List<Map<String, Object>>> dateGroups = filteredData.stream()
                    .filter(item -> item.get("CSRECEIVEDDATE") != null)
                    .collect(Collectors.groupingBy(item -> parseLocalDate(item.get("CSRECEIVEDDATE"))));
            
            log.info("📊 실제 데이터가 있는 날짜 수: {} 개", dateGroups.size());
            
            // 🎯 조회조건 전체 기간에 대해 라벨과 데이터 생성 (데이터 없는 날짜는 0)
            List<String> labels = new ArrayList<>();
            List<Long> exchangeData = new ArrayList<>();
            List<Long> returnData = new ArrayList<>();
            
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                String dateLabel = currentDate.format(DateTimeFormatter.ofPattern("MM-dd"));
                labels.add(dateLabel);
                
                // 해당 날짜의 데이터가 있으면 집계, 없으면 0
                List<Map<String, Object>> dayData = dateGroups.getOrDefault(currentDate, new ArrayList<>());
                
                long exchangeCount = dayData.stream()
                        .filter(item -> isExchangeType((String) item.get("RETURNTYPECODE")))
                        .count();
                long returnCount = dayData.stream()
                        .filter(item -> isReturnType((String) item.get("RETURNTYPECODE")))
                        .count();
                
                exchangeData.add(exchangeCount);
                returnData.add(returnCount);
                
                if (exchangeCount > 0 || returnCount > 0) {
                    log.info("📊 날짜별 통계 - {}: 교환 {} 건, 반품 {} 건", 
                            dateLabel, exchangeCount, returnCount);
                }
                
                currentDate = currentDate.plusDays(1);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("labels", labels);
            result.put("exchange", exchangeData);
            result.put("return", returnData);
            
            log.info("✅ 검색 조건 기반 트렌드 데이터 조회 완료");
            log.info("📊 전체 기간: {} 일, 라벨 개수: {}", labels.size(), labels.size());
            log.info("📊 조회조건 기간: {} ~ {}", startDate, endDate);
            log.info("📊 그래프 라벨: {}", labels);
            log.info("📊 교환 데이터 합계: {}", exchangeData.stream().mapToLong(Long::longValue).sum());
            log.info("📊 반품 데이터 합계: {}", returnData.stream().mapToLong(Long::longValue).sum());
            
            return result;
        } catch (Exception e) {
            log.error("❌ 검색 조건 기반 트렌드 데이터 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("labels", new ArrayList<>());
            emptyResult.put("exchange", new ArrayList<>());
            emptyResult.put("return", new ArrayList<>());
            return emptyResult;
        }
    }

    /**
     * Object를 LocalDate로 변환하는 헬퍼 메서드
     */
    private LocalDate parseLocalDate(Object dateObj) {
        if (dateObj == null) return null;
        
        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else if (dateObj instanceof java.sql.Timestamp) {
            // 🔧 TIMESTAMP 타입 지원 추가 (Oracle에서 TIMESTAMP 반환)
            return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
        } else if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if (dateObj instanceof java.util.Date) {
            return ((java.util.Date) dateObj).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (dateObj instanceof String) {
            try {
                // 문자열인 경우 다양한 형태 지원
                String dateStr = (String) dateObj;
                if (dateStr.contains(" ")) {
                    // "2025-07-04 00:00:00.0" 형태 처리
                    dateStr = dateStr.split(" ")[0];
                }
                return LocalDate.parse(dateStr);
            } catch (Exception e) {
                log.warn("⚠️ 날짜 파싱 실패: {} (타입: {})", dateObj, dateObj.getClass().getSimpleName());
                return null;
            }
        }
        
        log.warn("⚠️ 지원하지 않는 날짜 타입: {} (값: {})", dateObj.getClass().getSimpleName(), dateObj);
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalCountBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 매퍼 직접 조회 방식으로 전체 건수 조회 - 조건: {}", searchDTO);
            
            // 매퍼 메서드 파라미터 준비 (LocalDate → String 변환)
            String startDate = searchDTO.getStartDate() != null ? searchDTO.getStartDate().toString() : null;
            String endDate = searchDTO.getEndDate() != null ? searchDTO.getEndDate().toString() : null;
            String returnTypeCode = searchDTO.getReturnTypeCode();
            
            // 매퍼에서 직접 전체 건수 조회
            Long totalCount = returnItemMapper.getTotalCountBySearch(startDate, endDate, returnTypeCode, null);
            
            log.info("✅ 매퍼 직접 조회 전체 건수: {} 건", totalCount);
            return totalCount != null ? totalCount : 0L;
            
        } catch (Exception e) {
            log.error("❌ 매퍼 직접 조회 전체 건수 실패: {}", e.getMessage(), e);
            
            // 실패 시 기존 방식으로 폴백
            log.info("🔄 기존 스트림 방식으로 폴백 시도...");
            return getTotalCountBySearchFallback(searchDTO);
        }
    }
    
    // 폴백 메서드 (기존 방식)
    private Long getTotalCountBySearchFallback(ReturnItemSearchDTO searchDTO) {
        try {
            List<ReturnItemDTO> filteredData = getFilteredData(searchDTO);
            long totalCount = filteredData.size();
            log.info("🔍 폴백 방식 전체 건수: {} 건", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("❌ 폴백 방식 전체 건수도 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnItemDTO> findBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            log.info("📊 검색 조건에 맞는 전체 데이터 조회 - 조건: {}", searchDTO);
            // 🔥 성능 최적화: 페이징 없는 전체 데이터 조회로 변경
            List<ReturnItemDTO> result = getFilteredDataUnlimited(searchDTO);
            
            log.info("✅ 검색 조건에 맞는 데이터 조회 완료 - {} 건", result.size());
            return result;
        } catch (Exception e) {
            log.error("❌ 검색 조건에 맞는 데이터 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🚀 페이징 없는 전체 데이터 조회 (성능 최적화용)
     */
    private List<ReturnItemDTO> getFilteredDataUnlimited(ReturnItemSearchDTO searchDTO) {
        try {
            if (searchDTO == null || !searchDTO.hasSearchCondition()) {
                // 검색 조건이 없으면 전체 데이터 조회 (페이징 없음)
                log.info("📊 검색 조건 없음 - 전체 데이터 조회 (페이징 없음)");
                List<ReturnItem> allItems = returnItemRepository.findAll();
                return convertToDTO(allItems);
            } else if (StringUtils.hasText(searchDTO.getFilters())) {
                // 🎯 필터 조건이 있는 경우 다중 필터 처리
                log.info("📊 필터 조건 있음 - 다중 필터 처리로 데이터 조회");
                String filters = searchDTO.getFilters();
                List<String> filterList = Arrays.asList(filters.split(","));
                
                // 엑셀 다운로드용으로 전체 데이터 조회를 위해 임시로 큰 size 설정
                ReturnItemSearchDTO excelSearchDTO = new ReturnItemSearchDTO();
                excelSearchDTO.setKeyword(searchDTO.getKeyword());
                excelSearchDTO.setStartDate(searchDTO.getStartDate());
                excelSearchDTO.setEndDate(searchDTO.getEndDate());
                excelSearchDTO.setLogisticsStartDate(searchDTO.getLogisticsStartDate());
                excelSearchDTO.setLogisticsEndDate(searchDTO.getLogisticsEndDate());
                excelSearchDTO.setReturnTypeCode(searchDTO.getReturnTypeCode());
                excelSearchDTO.setReturnStatusCode(searchDTO.getReturnStatusCode());
                excelSearchDTO.setSiteName(searchDTO.getSiteName());
                excelSearchDTO.setPaymentStatus(searchDTO.getPaymentStatus());
                excelSearchDTO.setBrandFilter(searchDTO.getBrandFilter());
                excelSearchDTO.setFilters(searchDTO.getFilters());
                excelSearchDTO.setPage(0);
                excelSearchDTO.setSize(10000); // 엑셀 다운로드용 큰 사이즈
                excelSearchDTO.setSortBy(searchDTO.getSortBy());
                excelSearchDTO.setSortDir(searchDTO.getSortDir());
                
                // 다중 필터와 검색 조건을 함께 처리
                if (StringUtils.hasText(excelSearchDTO.getKeyword()) || 
                    excelSearchDTO.getStartDate() != null || 
                    excelSearchDTO.getEndDate() != null ||
                    excelSearchDTO.getLogisticsStartDate() != null ||
                    excelSearchDTO.getLogisticsEndDate() != null) {
                    
                    log.info("📊 필터 + 검색 조건 함께 처리");
                    Page<ReturnItemDTO> result = findByMultipleFiltersWithSearch(filterList, excelSearchDTO);
                    return result.getContent();
                } else {
                    log.info("📊 필터만 처리");
                    Page<ReturnItemDTO> result = findByMultipleFilters(filterList, excelSearchDTO);
                    return result.getContent();
                }
            } else {
                // 검색 조건이 있으면 Mapper의 findBySearch 사용 (페이징 없음)
                log.info("📊 검색 조건 있음 - 페이징 없는 검색 데이터 조회");
                Map<String, Object> searchParams = buildOptimizedSearchParams(searchDTO);
                List<ReturnItem> searchResults = returnItemMapper.findBySearch(searchParams);
                return convertToDTO(searchResults);
            }
        } catch (Exception e) {
            log.error("❌ 페이징 없는 전체 데이터 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnItemDTO> findAll() {
        try {
            log.info("📊 전체 데이터 조회 (페이징 없음)");
            List<ReturnItem> allItems = returnItemRepository.findAll();
            List<ReturnItemDTO> result = convertToDTO(allItems);
            
            log.info("✅ 전체 데이터 조회 완료 - {} 건", result.size());
            return result;
        } catch (Exception e) {
            log.error("❌ 전체 데이터 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🚀 [성능 최적화] 통합 대시보드 통계 조회 (기존 12개 쿼리 → 1개 쿼리)
     * 기존: 각각의 통계를 위해 12번의 개별 DB 호출
     * 개선: 모든 통계를 한 번의 DB 호출로 처리
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatsUnified() {
        try {
            long startTime = System.currentTimeMillis();
            
            // 🚀 통합 쿼리로 모든 통계를 한 번에 조회
            Map<String, Object> unifiedStats = returnItemMapper.getDashboardStatsUnified(null);
            
            long endTime = System.currentTimeMillis();
            log.info("🚀 통합 통계 조회 완료 - 소요시간: {}ms, 결과: {}", 
                endTime - startTime, unifiedStats);
            
            return unifiedStats;
            
        } catch (Exception e) {
            log.error("❌ 통합 통계 조회 실패: {}", e.getMessage(), e);
            // 기본값으로 초기화된 결과 반환
            return createDefaultStats();
        }
    }

    /**
     * 🚀 [성능 최적화] 검색 조건별 통합 대시보드 통계 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatsUnifiedBySearch(ReturnItemSearchDTO searchDTO) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 검색 조건을 Map으로 변환
            Map<String, Object> searchParams = buildOptimizedSearchParams(searchDTO);
            
            // 🚀 검색 조건이 포함된 통합 쿼리 실행
            Map<String, Object> unifiedStats = returnItemMapper.getDashboardStatsBySearch(searchParams);
            
            long endTime = System.currentTimeMillis();
            log.info("🚀 검색 조건별 통합 통계 조회 완료 - 소요시간: {}ms, 검색조건: {}, 결과: {}", 
                endTime - startTime, searchDTO, unifiedStats);
            
            return unifiedStats;
            
        } catch (Exception e) {
            log.error("❌ 검색 조건별 통합 통계 조회 실패: {}", e.getMessage(), e);
            return createDefaultStats();
        }
    }

    /**
     * 🔧 기본 통계 값 생성 (오류 발생 시 사용)
     */
    private Map<String, Object> createDefaultStats() {
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("totalCount", 0L);
        defaultStats.put("todayCount", 0L);
        defaultStats.put("collectionCompletedCount", 0L);
        defaultStats.put("collectionPendingCount", 0L);
        defaultStats.put("logisticsConfirmedCount", 0L);
        defaultStats.put("logisticsPendingCount", 0L);
        defaultStats.put("exchangeShippedCount", 0L);
        defaultStats.put("exchangeNotShippedCount", 0L);
        defaultStats.put("returnRefundedCount", 0L);
        defaultStats.put("returnNotRefundedCount", 0L);
        defaultStats.put("paymentCompletedCount", 0L);
        defaultStats.put("paymentPendingCount", 0L);
        defaultStats.put("completedCount", 0L);
        defaultStats.put("incompletedCount", 0L);
        // 🚨 처리기간 임박 통계 추가
        defaultStats.put("overdueTenDaysCount", 0L);
        defaultStats.put("totalRefundAmount", 0L);
        defaultStats.put("avgRefundAmount", 0L);
        defaultStats.put("fullExchangeCount", 0L);
        defaultStats.put("partialExchangeCount", 0L);
        defaultStats.put("fullReturnCount", 0L);
        defaultStats.put("partialReturnCount", 0L);
        defaultStats.put("renomaCount", 0L);
        defaultStats.put("coralicCount", 0L);
        
        return defaultStats;
    }
    
    // ==================== 🚨 Sample 통합: 10일 경과 건 및 다중 필터 메소드들 ====================
    
    /**
     * 🚨 접수일 기준 10일 이상 미완료 데이터 조회 (Sample 통합)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findOverdueTenDays(ReturnItemSearchDTO searchDTO) {
        log.info("🚨 처리기간 임박 데이터 조회 시작 - 접수일 기준 10일 이상 미완료 건");
        log.info("🔍 검색 조건: {}", searchDTO);
        
        try {
            // 10일 전 날짜 계산
            LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
            log.info("📅 기준 날짜: {} (10일 전)", tenDaysAgo);
            
            // Oracle ROWNUM 페이징 처리
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            // 10일 전 이전에 접수되었으면서 아직 완료되지 않은 건 조회
            List<ReturnItem> entities = returnItemRepository.findOverdueTenDays(tenDaysAgo, startRow, endRow);
            log.info("📊 접수일 기준 10일 이상 미완료 데이터 조회 결과: {} 건", entities.size());
            
            // 전체 카운트 조회
            long totalElements = returnItemRepository.countOverdueTenDays(tenDaysAgo);
            log.info("📊 전체 접수일 기준 10일 이상 미완료 건수: {} 건", totalElements);
            
            // DTO 변환
            List<ReturnItemDTO> dtoList = convertToDTO(entities);
            
            // 정렬 처리 (null 체크 추가)
            String sortDir = searchDTO.getSortDir();
            String sortBy = searchDTO.getSortBy();
            
            // 기본값 설정
            if (sortDir == null || sortDir.trim().isEmpty()) {
                sortDir = Sort.Direction.DESC.name();
            }
            if (sortBy == null || sortBy.trim().isEmpty()) {
                sortBy = "id";
            }
            
            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
            
            Page<ReturnItemDTO> result = new PageImpl<>(dtoList, pageable, totalElements);
            
            log.info("✅ 처리기간 임박 데이터 조회 완료 - 총 {} 건 (페이지: {}/{})", 
                result.getTotalElements(), searchDTO.getPage() + 1, result.getTotalPages());
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ 처리기간 임박 데이터 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }
    
    /**
     * 🎯 다중 필터 처리 메서드 (Sample 통합)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByMultipleFilters(List<String> filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 처리 시작 - filters: {}", filters);
        
        if (filters == null || filters.isEmpty()) {
            log.warn("⚠️ 필터 목록이 비어있음, 전체 조회로 처리");
            return findAll(searchDTO.getPage(), searchDTO.getSize(), searchDTO.getSortBy(), searchDTO.getSortDir());
        }
        
        try {
            // 🎯 첫 번째 필터를 기본으로 하고 나머지 필터들로 교집합 처리
            Page<ReturnItemDTO> resultPage = null;
            
            // 다중 필터를 서비스에 전달 (우선 첫 번째 필터만 적용)
            String firstFilter = filters.get(0).trim();
            log.info("🔄 첫 번째 필터로 기본 조회: {}", firstFilter);
            resultPage = applySingleFilter(firstFilter, searchDTO);
            
            // 추가 필터가 있으면 교집합 처리 (메모리에서)
            if (filters.size() > 1) {
                log.info("🔍 추가 필터 교집합 처리: {}", filters.subList(1, filters.size()));
                
                // 현재 결과를 기반으로 추가 필터링
                List<ReturnItemDTO> filteredData = resultPage.getContent().stream()
                    .filter(item -> filters.stream().allMatch(filter -> matchesFilter(item, filter.trim())))
                    .collect(Collectors.toList());
                
                // 페이징 처리
                int start = searchDTO.getPage() * searchDTO.getSize();
                int end = Math.min(start + searchDTO.getSize(), filteredData.size());
                
                List<ReturnItemDTO> pagedData;
                if (start < filteredData.size()) {
                    pagedData = filteredData.subList(start, end);
                } else {
                    pagedData = new ArrayList<>();
                }
                
                Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
                resultPage = new PageImpl<>(pagedData, pageable, filteredData.size());
            }
            
            log.info("✅ 다중 필터 적용 완료 - 필터: {}, 결과: {} 건", filters, resultPage.getTotalElements());
            return resultPage;
            
        } catch (Exception e) {
            log.error("❌ 다중 필터 적용 중 오류 발생, 개별 필터로 fallback 처리", e);
            
            // 서비스에서 다중 필터를 지원하지 않는 경우 첫 번째 필터만 적용
            if (filters.size() > 0) {
                String firstFilter = filters.get(0).trim();
                log.info("🔄 첫 번째 필터로 fallback: {}", firstFilter);
                return applySingleFilter(firstFilter, searchDTO);
            } else {
                // 필터가 없으면 전체 조회
                return findAll(searchDTO.getPage(), searchDTO.getSize(), 
                    searchDTO.getSortBy(), searchDTO.getSortDir());
            }
        }
    }
    
    /**
     * 🎯 다중 필터 + 검색 조건 함께 처리 구현 (Sample 통합)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByMultipleFiltersWithSearch(List<String> filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 + 검색 교집합 처리 시작 - 필터: {}, 검색: {}", filters, searchDTO.getKeyword());
        
        if (filters == null || filters.isEmpty()) {
            log.warn("⚠️ 필터 목록이 비어있음, 검색만 적용");
            return search(searchDTO);
        }
        
        try {
            // 🎯 먼저 검색 조건으로 데이터 필터링
            log.info("🔍 1단계: 검색 조건 적용");
            
            // 검색 조건이 있으면 먼저 검색하여 기본 데이터셋 구성
            List<ReturnItemDTO> searchResults;
            
            if (searchDTO.hasSearchCondition()) {
                // 🔥 성능 최적화: 페이징 없는 검색 조건 전체 데이터 조회
                searchResults = findBySearch(searchDTO);
                log.info("📊 검색 결과: {} 건", searchResults.size());
            } else {
                // 🔥 성능 최적화: 페이징 없는 전체 데이터 조회
                searchResults = findAll();
                log.info("📊 전체 데이터: {} 건", searchResults.size());
            }
            
            // 🎯 2단계: 검색 결과에 필터 적용
            log.info("🔍 2단계: 검색 결과에 필터 적용");
            List<ReturnItemDTO> filteredData = new ArrayList<>(searchResults);
            
            for (String filter : filters) {
                String filterType = filter.trim();
                log.info("🔍 필터 적용 중: {} (현재 데이터: {} 건)", filterType, filteredData.size());
                
                // 현재 필터 조건에 맞는 데이터 필터링
                filteredData = filteredData.stream()
                    .filter(item -> matchesFilter(item, filterType))
                    .collect(Collectors.toList());
                
                log.info("✅ 필터 적용 완료: {} -> {} 건", filterType, filteredData.size());
            }
            
            log.info("🎯 최종 교집합 결과: {} 건", filteredData.size());
            
            // 🎯 3단계: 페이징 처리
            int start = searchDTO.getPage() * searchDTO.getSize();
            int end = Math.min(start + searchDTO.getSize(), filteredData.size());
            
            List<ReturnItemDTO> pagedData;
            if (start < filteredData.size()) {
                pagedData = filteredData.subList(start, end);
            } else {
                pagedData = new ArrayList<>();
            }
            
            // 정렬 처리 (필요한 경우) - null 체크 추가
            if ("id".equals(searchDTO.getSortBy())) {
                String sortDir = searchDTO.getSortDir();
                boolean isAsc = sortDir != null && "ASC".equalsIgnoreCase(sortDir);
                pagedData.sort((a, b) -> isAsc ? 
                    Long.compare(a.getId(), b.getId()) : Long.compare(b.getId(), a.getId()));
            }
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            Page<ReturnItemDTO> result = new PageImpl<>(pagedData, pageable, filteredData.size());
            
            log.info("✅ 다중 필터 + 검색 교집합 처리 완료 - 최종 결과: {} 건 (페이지: {}/{})", 
                result.getTotalElements(), searchDTO.getPage() + 1, result.getTotalPages());
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ 다중 필터 + 검색 교집합 처리 중 오류 발생, fallback 처리", e);
            
            // fallback: 검색만 적용
            Page<ReturnItemDTO> result = search(searchDTO);
            log.info("🔄 검색만 적용으로 fallback - 결과: {} 건", result.getTotalElements());
            return result;
        }
    }
    
    /**
     * 🎯 단일 필터 적용 메서드 (Sample 통합)
     */
    private Page<ReturnItemDTO> applySingleFilter(String filterType, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 단일 필터 적용 - filterType: {}", filterType);
        
        try {
            switch (filterType) {
                case "collection-completed":
                    return findByCollectionCompleted(searchDTO);
                case "collection-pending":
                    return findByCollectionPending(searchDTO);
                case "logistics-confirmed":
                    return findByLogisticsConfirmed(searchDTO);
                case "logistics-pending":
                    return findByLogisticsPending(searchDTO);
                case "shipping-completed":
                    return findByShippingCompleted(searchDTO);
                case "shipping-pending":
                    return findByShippingPending(searchDTO);
                case "refund-completed":
                    return findByRefundCompleted(searchDTO);
                case "refund-pending":
                    return findByRefundPending(searchDTO);
                case "payment-completed":
                    return findByPaymentCompleted(searchDTO);
                case "payment-pending":
                    return findByPaymentPending(searchDTO);
                case "completed":
                    return findByCompleted(searchDTO);
                case "incompleted":
                    return findByIncompleted(searchDTO);
                case "overdue-ten-days":
                    return findOverdueTenDays(searchDTO);
                default:
                    log.warn("⚠️ 알 수 없는 필터 타입: {}", filterType);
                    return findAll(searchDTO.getPage(), searchDTO.getSize(), 
                        searchDTO.getSortBy(), searchDTO.getSortDir());
            }
        } catch (Exception e) {
            log.error("❌ 단일 필터 적용 중 오류 발생: {}", e.getMessage(), e);
            return findAll(searchDTO.getPage(), searchDTO.getSize(), 
                searchDTO.getSortBy(), searchDTO.getSortDir());
        }
    }
    
    /**
     * 🎯 개별 필터 조건 매칭 (Sample 통합)
     */
    private boolean matchesFilter(ReturnItemDTO item, String filterType) {
        switch (filterType) {
            case "collection-completed":
                return item.getCollectionCompletedDate() != null;
            case "collection-pending":
                return item.getCollectionCompletedDate() == null;
            case "logistics-confirmed":
                return item.getLogisticsConfirmedDate() != null;
            case "logistics-pending":
                return item.getLogisticsConfirmedDate() == null;
            case "shipping-completed":
                // 교환 타입이면서 출고일이 있는 경우
                return isExchangeType(item.getReturnTypeCode()) && item.getShippingDate() != null;
            case "shipping-pending":
                // 교환 타입이면서 출고일이 없는 경우
                return isExchangeType(item.getReturnTypeCode()) && item.getShippingDate() == null;
            case "refund-completed":
                // 반품 타입이면서 환불일이 있는 경우
                return isReturnType(item.getReturnTypeCode()) && item.getRefundDate() != null;
            case "refund-pending":
                // 반품 타입이면서 환불일이 없는 경우
                return isReturnType(item.getReturnTypeCode()) && item.getRefundDate() == null;
            case "payment-completed":
                return "COMPLETED".equals(item.getPaymentStatus());
            case "payment-pending":
                return "PENDING".equals(item.getPaymentStatus()) || "입금예정".equals(item.getShippingFee());
            case "completed":
                return Boolean.TRUE.equals(item.getIsCompleted());
            case "incompleted":
                return item.getIsCompleted() == null || Boolean.FALSE.equals(item.getIsCompleted());
            case "overdue-ten-days":
                // 처리기간 임박 필터 - 접수일 기준 10일 이상 미완료 건
                if (item.getCsReceivedDate() != null && 
                    (item.getIsCompleted() == null || Boolean.FALSE.equals(item.getIsCompleted()))) {
                    LocalDate tenDaysAgo = LocalDate.now().minusDays(10);
                    return item.getCsReceivedDate().isBefore(tenDaysAgo);
                }
                return false;
            default:
                log.warn("⚠️ 알 수 없는 필터 타입: {}", filterType);
                return true; // 알 수 없는 필터는 모든 데이터 통과
        }
    }

    // 🆕 이미지 관리 메소드 구현
    
    @Override
    @Transactional
    public void updateDefectPhotoUrl(Long itemId, String imageUrl) {
        log.info("📷 이미지 URL 업데이트 - itemId: {}, imageUrl: {}", itemId, imageUrl);
        
        try {
            // 데이터 존재 여부 확인
            Optional<ReturnItem> itemOpt = returnItemRepository.findById(itemId);
            if (!itemOpt.isPresent()) {
                throw new RuntimeException("해당 항목을 찾을 수 없습니다: " + itemId);
            }
            
            ReturnItem item = itemOpt.get();
            String oldImageUrl = item.getDefectPhotoUrl();
            
            // 이미지 URL 업데이트
            item.setDefectPhotoUrl(imageUrl);
            item.setUpdateDate(LocalDateTime.now());
            
            // 저장
            returnItemRepository.save(item);
            
            log.info("✅ 이미지 URL 업데이트 완료 - itemId: {}, 기존: {}, 신규: {}", 
                itemId, oldImageUrl, imageUrl);
            
        } catch (Exception e) {
            log.error("❌ 이미지 URL 업데이트 실패 - itemId: {}, 오류: {}", itemId, e.getMessage(), e);
            throw new RuntimeException("이미지 URL 업데이트 실패: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void updateDefectDetail(Long itemId, String defectDetail) {
        log.info("📝 불량상세 메모 업데이트 - itemId: {}, defectDetail: {}", itemId, defectDetail);
        
        try {
            // 데이터 존재 여부 확인
            Optional<ReturnItem> itemOpt = returnItemRepository.findById(itemId);
            if (!itemOpt.isPresent()) {
                throw new RuntimeException("해당 항목을 찾을 수 없습니다: " + itemId);
            }
            
            ReturnItem item = itemOpt.get();
            String oldDefectDetail = item.getDefectDetail();
            
            // 불량상세 메모 업데이트
            item.setDefectDetail(defectDetail);
            item.setUpdateDate(LocalDateTime.now());
            
            // 저장
            returnItemRepository.save(item);
            
            log.info("✅ 불량상세 메모 업데이트 완료 - itemId: {}, 기존: {}, 신규: {}", 
                itemId, oldDefectDetail, defectDetail);
            
        } catch (Exception e) {
            log.error("❌ 불량상세 메모 업데이트 실패 - itemId: {}, 오류: {}", itemId, e.getMessage(), e);
            throw new RuntimeException("불량상세 메모 업데이트 실패: " + e.getMessage(), e);
        }
    }

} 