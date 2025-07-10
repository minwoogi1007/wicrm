package com.wio.repairsystem.service.impl;

import com.wio.repairsystem.dto.ReturnItemDTO;
import com.wio.repairsystem.dto.ReturnItemSearchDTO;
import com.wio.repairsystem.dto.ReturnItemBulkDateUpdateDTO;
import com.wio.repairsystem.model.ReturnItem;
import com.wio.repairsystem.model.ReturnStatus;
import com.wio.repairsystem.model.ReturnType;
import com.wio.repairsystem.repository.ReturnItemRepository;
import com.wio.repairsystem.service.ReturnItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 교환/반품 서비스 구현체
 * 
 * 사용 화면:
 * - templates/exchange/list.html : 목록 조회, 검색, 상태별 집계
 * - templates/exchange/form.html : 등록, 수정 처리
 * - templates/exchange/view.html : 상세 조회
 * 
 * 주요 기능: 
 * - CRUD 작업 (생성, 조회, 수정, 삭제)
 * - 검색 및 필터링 (주문번호, 고객명, 상태, 유형, 날짜범위)
 * - 상태 관리 및 통계 제공
 */

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReturnItemServiceImpl implements ReturnItemService {

    private final ReturnItemRepository returnItemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReturnItemDTO> getAllReturnItems(ReturnItemSearchDTO searchDTO) {
        if (searchDTO == null) {
            // 검색 조건이 없으면 전체 조회
            return returnItemRepository.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } else {
            // 검색 조건이 있으면 검색 수행
            Page<ReturnItemDTO> searchResult = search(searchDTO);
            return searchResult.getContent();
        }
    }

    @Override
    public ReturnItemDTO createReturnItem(ReturnItemDTO returnItemDTO) {
        return save(returnItemDTO);
    }

    @Override
    public ReturnItemDTO updateReturnItem(ReturnItemDTO returnItemDTO) {
        if (returnItemDTO.getId() == null) {
            throw new RuntimeException("ID가 없어서 수정할 수 없습니다.");
        }
        return update(returnItemDTO.getId(), returnItemDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnItemDTO getReturnItemById(Long id) {
        return findById(id);
    }

    @Override
    public void deleteReturnItem(Long id) {
        delete(id);
    }

    @Override
    public ReturnItemDTO save(ReturnItemDTO dto) {
        ReturnItem returnItem = mapToEntity(dto);
        ReturnItem savedReturnItem = returnItemRepository.save(returnItem);
        return mapToDTO(savedReturnItem);
    }

    @Override
    public ReturnItemDTO update(Long id, ReturnItemDTO dto) {
        ReturnItem returnItem = returnItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return item not found with id: " + id));

        updateEntityFromDTO(returnItem, dto);
        ReturnItem updatedReturnItem = returnItemRepository.save(returnItem);
        return mapToDTO(updatedReturnItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnItemDTO findById(Long id) {
        ReturnItem returnItem = returnItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return item not found with id: " + id));
        return mapToDTO(returnItem);
    }

    @Override
    public void delete(Long id) {
        returnItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findAll(int page, int size, String sortBy, String sortDir) {
        // Oracle 11g 호환을 위한 네이티브 쿼리 사용
        int startRow = page * size;
        int endRow = startRow + size;
        
        List<ReturnItem> items = returnItemRepository.findAllWithPagination(startRow, endRow);
        long totalCount = returnItemRepository.countAllItems();
        
        List<ReturnItemDTO> dtoList = items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtoList, pageable, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> search(ReturnItemSearchDTO searchDTO) {
        log.info("🔍 검색 시작 - 키워드: '{}', 시작날짜: {}, 종료날짜: {}", 
                searchDTO.getKeyword(), searchDTO.getStartDate(), searchDTO.getEndDate());
        
        // 완료 상태 검색 키워드 체크
        if (searchDTO.getKeyword() != null) {
            String keyword = searchDTO.getKeyword().trim();
            log.info("🎯 완료 상태 검색 키워드 체크: '{}'", keyword);
            
            if (keyword.equals("완료") || keyword.equals("전체완료") || keyword.equals("Y")) {
                log.info("✅ 완료 상태 검색 감지: {}", keyword);
            } else if (keyword.equals("미완료") || keyword.equals("진행중") || keyword.equals("N")) {
                log.info("❌ 미완료 상태 검색 감지: {}", keyword);
            }
        }
        
        // Oracle 11g 호환을 위한 수동 페이징 처리
        int startRow = searchDTO.getPage() * searchDTO.getSize();
        int endRow = startRow + searchDTO.getSize();
        
        // 검색 결과 조회
        List<ReturnItem> content = returnItemRepository.findBySearchCriteria(
            searchDTO.getKeyword(),
            searchDTO.getStartDate(),
            searchDTO.getEndDate(),
            startRow,
            endRow
        );
        
        log.info("🔍 검색 결과: {} 건 조회됨", content.size());
        
        // 총 개수 조회
        long totalElements = returnItemRepository.countBySearchCriteria(
            searchDTO.getKeyword(),
            searchDTO.getStartDate(),
            searchDTO.getEndDate()
        );
        
        log.info("📊 전체 결과 수: {} 건", totalElements);
        
        // DTO 변환
        List<ReturnItemDTO> dtoList = content.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        // Pageable 객체 생성
        Sort sort = searchDTO.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(searchDTO.getSortBy()).ascending() : Sort.by(searchDTO.getSortBy()).descending();
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        
        // Page 객체 생성하여 반환
        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByStatus(ReturnStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return returnItemRepository.findByReturnStatusCode(status.name(), pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByType(ReturnType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return returnItemRepository.findByReturnTypeCode(type.name(), pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByOrderNumber(String orderNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return returnItemRepository.findByOrderNumberContaining(orderNumber, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByCustomerName(String customerName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return returnItemRepository.findByCustomerNameContaining(customerName, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return returnItemRepository.findByCsReceivedDateBetween(startDate, endDate, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnItemDTO> findUnprocessed() {
        // 🚀 성능 최적화: 인덱스를 활용한 미완료 항목 조회
        return returnItemRepository.findByIsCompletedFalseOptimized().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReturnItemDTO updateStatus(Long id, ReturnStatus status) {
        ReturnItem returnItem = returnItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return item not found with id: " + id));
        
        returnItem.setReturnStatusCode(status.name());
        ReturnItem updatedReturnItem = returnItemRepository.save(returnItem);
        return mapToDTO(updatedReturnItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ReturnStatus, Long> getStatusCounts() {
        Map<ReturnStatus, Long> counts = new HashMap<>();
        
        Arrays.stream(ReturnStatus.values()).forEach(status -> {
            long count = returnItemRepository.countByStatus(status.name());
            counts.put(status, count);
        });
        
        return counts;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getSiteCounts() {
        List<ReturnItem> allItems = returnItemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getSiteName() != null && !item.getSiteName().trim().isEmpty())
                .collect(Collectors.groupingBy(
                    ReturnItem::getSiteName,
                    Collectors.counting()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTypeCounts() {
        List<ReturnItem> allItems = returnItemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getReturnTypeCode() != null && !item.getReturnTypeCode().trim().isEmpty())
                .collect(Collectors.groupingBy(
                    ReturnItem::getReturnTypeCode,
                    Collectors.counting()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getReasonCounts() {
        List<ReturnItem> allItems = returnItemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getReturnReason() != null && !item.getReturnReason().trim().isEmpty())
                .collect(Collectors.groupingBy(
                    ReturnItem::getReturnReason,
                    Collectors.counting()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAmountSummary() {
        List<ReturnItem> allItems = returnItemRepository.findAll();
        
        Map<String, Object> summary = new HashMap<>();
        
        // 환불금액 합계
        Long totalRefundAmount = allItems.stream()
                .filter(item -> item.getRefundAmount() != null)
                .mapToLong(ReturnItem::getRefundAmount)
                .sum();
        
        // 배송비 합계 (문자열을 숫자로 변환, 숫자가 아닌 경우 0으로 처리)
        Long totalShippingFee = allItems.stream()
                .filter(item -> item.getShippingFee() != null && !item.getShippingFee().trim().isEmpty())
                .mapToLong(item -> {
                    try {
                        return Long.parseLong(item.getShippingFee().trim());
                    } catch (NumberFormatException e) {
                        return 0L; // "입금대기" 등 숫자가 아닌 경우 0으로 처리
                    }
                })
                .sum();
        
        // 평균 환불금액
        Double avgRefundAmount = allItems.stream()
                .filter(item -> item.getRefundAmount() != null)
                .mapToLong(ReturnItem::getRefundAmount)
                .average()
                .orElse(0.0);
        
        summary.put("totalRefundAmount", totalRefundAmount);
        summary.put("totalShippingFee", totalShippingFee);
        summary.put("avgRefundAmount", avgRefundAmount.longValue());
        summary.put("totalItems", (long) allItems.size());
        
        return summary;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getBrandCounts() {
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
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getTodayCount() {
        try {
            return returnItemRepository.countTodayItems();
        } catch (Exception e) {
            log.error("금일 등록 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    // 🎯 상단 카드 대시보드 통계 메서드들
    
    @Override
    @Transactional(readOnly = true)
    public Long getCollectionCompletedCount() {
        try {
            return returnItemRepository.countByCollectionCompletedDateIsNotNull();
        } catch (Exception e) {
            log.error("회수완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCollectionPendingCount() {
        try {
            return returnItemRepository.countByCollectionCompletedDateIsNull();
        } catch (Exception e) {
            log.error("회수미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLogisticsConfirmedCount() {
        try {
            return returnItemRepository.countByLogisticsConfirmedDateIsNotNull();
        } catch (Exception e) {
            log.error("물류확인 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLogisticsPendingCount() {
        try {
            return returnItemRepository.countByLogisticsConfirmedDateIsNull();
        } catch (Exception e) {
            log.error("물류미확인 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getExchangeShippedCount() {
        try {
            return returnItemRepository.countExchangeShipped();
        } catch (Exception e) {
            log.error("교환 출고완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getExchangeNotShippedCount() {
        try {
            return returnItemRepository.countExchangeNotShipped();
        } catch (Exception e) {
            log.error("교환 출고미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReturnRefundedCount() {
        try {
            return returnItemRepository.countReturnRefunded();
        } catch (Exception e) {
            log.error("반품 환불완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReturnNotRefundedCount() {
        try {
            return returnItemRepository.countReturnNotRefunded();
        } catch (Exception e) {
            log.error("반품 환불미완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPaymentCompletedCount() {
        try {
            // 🔥 전체반품, 부분반품, 교환 모든 경우 배송비가 입금완료로 되어있는 경우 (정확한 조건 적용)
            return returnItemRepository.countExchangeByPaymentCompleted();
        } catch (Exception e) {
            log.error("입금완료 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPaymentPendingCount() {
        try {
            // 🔥 전체반품, 부분반품, 교환 모든 경우 배송비가 입금예정으로 되어있는 경우 (두 조건 모두 고려)
            // 1. payment_status = 'PENDING' 또는 2. shipping_fee = '입금예정'
            return returnItemRepository.countExchangeByPaymentPending();
        } catch (Exception e) {
            log.error("입금예정 건수 조회 실패: {}", e.getMessage());
            return 0L;
        }
    }

    // Helper methods for entity <-> DTO conversion
    private ReturnItem mapToEntity(ReturnItemDTO dto) {
        ReturnItem.ReturnItemBuilder builder = ReturnItem.builder()
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
                .createdBy(dto.getCreatedBy())
                .updatedBy(dto.getUpdatedBy())
                .paymentStatus(dto.getPaymentStatus())
                .paymentId(dto.getPaymentId());
                // createDate와 updateDate는 DB DEFAULT로 처리됨

        return builder.build();
    }
    
    private ReturnItemDTO mapToDTO(ReturnItem entity) {
        return ReturnItemDTO.builder()
                .id(entity.getId())
                .returnTypeCode(entity.getReturnTypeCode())
                .orderDate(entity.getOrderDate())
                .csReceivedDate(entity.getCsReceivedDate())
                .siteName(entity.getSiteName())
                .orderNumber(entity.getOrderNumber())
                .refundAmount(entity.getRefundAmount())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .orderItemCode(entity.getOrderItemCode())
                .productColor(entity.getProductColor())
                .productSize(entity.getProductSize())
                .quantity(entity.getQuantity())
                .shippingFee(entity.getShippingFee())
                .returnReason(entity.getReturnReason())
                .defectDetail(entity.getDefectDetail())
                .defectPhotoUrl(entity.getDefectPhotoUrl())
                .trackingNumber(entity.getTrackingNumber())
                .collectionCompletedDate(entity.getCollectionCompletedDate())
                .logisticsConfirmedDate(entity.getLogisticsConfirmedDate())
                .shippingDate(entity.getShippingDate())
                .refundDate(entity.getRefundDate())
                .isCompleted(entity.getIsCompleted())
                .remarks(entity.getRemarks())
                .returnStatusCode(entity.getReturnStatusCode())
                .processor(entity.getProcessor())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .paymentStatus(entity.getPaymentStatus())
                .paymentId(entity.getPaymentId())
                .collectionUpdatedBy(entity.getCollectionUpdatedBy())
                .collectionUpdatedDate(entity.getCollectionUpdatedDate())
                .logisticsUpdatedBy(entity.getLogisticsUpdatedBy())
                .logisticsUpdatedDate(entity.getLogisticsUpdatedDate())
                .shippingUpdatedBy(entity.getShippingUpdatedBy())
                .shippingUpdatedDate(entity.getShippingUpdatedDate())
                .refundUpdatedBy(entity.getRefundUpdatedBy())
                .refundUpdatedDate(entity.getRefundUpdatedDate())
                .build();
    }
    
    private void updateEntityFromDTO(ReturnItem entity, ReturnItemDTO dto) {
        entity.setReturnTypeCode(dto.getReturnTypeCode());
        entity.setOrderDate(dto.getOrderDate());
        entity.setCsReceivedDate(dto.getCsReceivedDate());
        entity.setSiteName(dto.getSiteName());
        entity.setOrderNumber(dto.getOrderNumber());
        entity.setRefundAmount(dto.getRefundAmount());
        entity.setCustomerName(dto.getCustomerName());
        entity.setCustomerPhone(dto.getCustomerPhone());
        entity.setOrderItemCode(dto.getOrderItemCode());
        entity.setProductColor(dto.getProductColor());
        entity.setProductSize(dto.getProductSize());
        entity.setQuantity(dto.getQuantity());
        entity.setShippingFee(dto.getShippingFee());
        entity.setReturnReason(dto.getReturnReason());
        entity.setDefectDetail(dto.getDefectDetail());
        entity.setDefectPhotoUrl(dto.getDefectPhotoUrl());
        entity.setTrackingNumber(dto.getTrackingNumber());
        entity.setCollectionCompletedDate(dto.getCollectionCompletedDate());
        entity.setLogisticsConfirmedDate(dto.getLogisticsConfirmedDate());
        entity.setShippingDate(dto.getShippingDate());
        entity.setRefundDate(dto.getRefundDate());
        entity.setIsCompleted(dto.getIsCompleted());
        entity.setRemarks(dto.getRemarks());
        entity.setReturnStatusCode(dto.getReturnStatusCode());
        entity.setProcessor(dto.getProcessor());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setPaymentStatus(dto.getPaymentStatus());
        entity.setPaymentId(dto.getPaymentId());
        entity.setCollectionUpdatedBy(dto.getCollectionUpdatedBy());
        entity.setCollectionUpdatedDate(dto.getCollectionUpdatedDate());
        entity.setLogisticsUpdatedBy(dto.getLogisticsUpdatedBy());
        entity.setLogisticsUpdatedDate(dto.getLogisticsUpdatedDate());
        entity.setShippingUpdatedBy(dto.getShippingUpdatedBy());
        entity.setShippingUpdatedDate(dto.getShippingUpdatedDate());
        entity.setRefundUpdatedBy(dto.getRefundUpdatedBy());
        entity.setRefundUpdatedDate(dto.getRefundUpdatedDate());
        // createDate와 updateDate는 DB가 자동 처리
    }

    // 🎯 카드 필터링 메서드들 - 실제 조건에 맞는 데이터 조회
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByCollectionCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByCollectionCompletedDateIsNotNull(startRow, endRow);
            long totalCount = returnItemRepository.countByCollectionCompletedDateIsNotNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("회수완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByCollectionPending(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByCollectionCompletedDateIsNull(startRow, endRow);
            long totalCount = returnItemRepository.countByCollectionCompletedDateIsNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("회수미완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByLogisticsConfirmed(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByLogisticsConfirmedDateIsNotNull(startRow, endRow);
            long totalCount = returnItemRepository.countByLogisticsConfirmedDateIsNotNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("물류확인완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByLogisticsPending(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findByLogisticsConfirmedDateIsNull(startRow, endRow);
            long totalCount = returnItemRepository.countByLogisticsConfirmedDateIsNull();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("물류확인미완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByShippingCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findExchangeShipped(startRow, endRow);
            long totalCount = returnItemRepository.countExchangeShipped();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("출고완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByShippingPending(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findExchangeNotShipped(startRow, endRow);
            long totalCount = returnItemRepository.countExchangeNotShipped();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("출고대기 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByRefundCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findReturnRefunded(startRow, endRow);
            long totalCount = returnItemRepository.countReturnRefunded();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("환불완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByRefundPending(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            List<ReturnItem> entities = returnItemRepository.findReturnNotRefunded(startRow, endRow);
            long totalCount = returnItemRepository.countReturnNotRefunded();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("환불대기 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByPaymentCompleted(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            // 🔥 전체반품, 부분반품, 교환 모든 경우 배송비가 입금완료인 경우 (정확한 조건 적용)
            List<ReturnItem> entities = returnItemRepository.findExchangeByPaymentCompleted(startRow, endRow);
            long totalCount = returnItemRepository.countExchangeByPaymentCompleted();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("입금완료 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByPaymentPending(ReturnItemSearchDTO searchDTO) {
        try {
            int startRow = searchDTO.getPage() * searchDTO.getSize();
            int endRow = startRow + searchDTO.getSize();
            
            // 🔥 전체반품, 부분반품, 교환 모든 경우 배송비가 입금예정인 경우 (두 조건 모두 고려)
            // 1. payment_status = 'PENDING' 또는 2. shipping_fee = '입금예정'
            List<ReturnItem> entities = returnItemRepository.findExchangeByPaymentPending(startRow, endRow);
            long totalCount = returnItemRepository.countExchangeByPaymentPending();
            
            Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
            List<ReturnItemDTO> dtos = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            
            return new PageImpl<>(dtos, pageable, totalCount);
        } catch (Exception e) {
            log.error("입금예정 필터링 조회 실패: {}", e.getMessage(), e);
            return Page.empty(PageRequest.of(searchDTO.getPage(), searchDTO.getSize()));
        }
    }

    // 🚀 성능 최적화된 검색 메서드 추가
    @Override
    public Page<ReturnItemDTO> searchOptimized(String keyword, LocalDate startDate, LocalDate endDate, int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;
        
        // 키워드 패턴 준비 (최적화된 LIKE 검색)
        String keywordPattern = keyword != null ? "%" + keyword + "%" : null;
        
        List<ReturnItem> items = returnItemRepository.findBySearchCriteriaOptimized(
            keyword, keywordPattern, startDate, endDate, startRow, endRow);
        long total = returnItemRepository.countBySearchCriteriaOptimized(
            keyword, keywordPattern, startDate, endDate);
        
        List<ReturnItemDTO> dtoList = items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, PageRequest.of(page, size), total);
    }

    // 🚀 성능 최적화된 상태별 조회
    @Override
    public Page<ReturnItemDTO> findByStatusOptimized(String status, int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;
        
        List<ReturnItem> items = returnItemRepository.findByReturnStatusCodeOptimized(status, startRow, endRow);
        long total = returnItemRepository.countByReturnStatusCodeOptimized(status);
        
        List<ReturnItemDTO> dtoList = items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, PageRequest.of(page, size), total);
    }

    // 🚀 성능 최적화된 타입별 조회
    @Override
    public Page<ReturnItemDTO> findByTypeOptimized(String type, int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;
        
        List<ReturnItem> items = returnItemRepository.findByReturnTypeCodeOptimized(type, startRow, endRow);
        long total = returnItemRepository.countByReturnTypeCodeOptimized(type);
        
        List<ReturnItemDTO> dtoList = items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, PageRequest.of(page, size), total);
    }

    // 🚀 성능 최적화된 배송비 관련 조회
    @Override
    public List<ReturnItemDTO> findByPaymentStatusOptimized(String paymentStatus) {
        List<ReturnItem> items = returnItemRepository.findByPaymentStatusOptimized(paymentStatus);
        return items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReturnItemDTO> findByCustomerAndPaymentStatusOptimized(String customerName, String customerPhone, String paymentStatus) {
        List<ReturnItem> items = returnItemRepository.findByCustomerNameAndCustomerPhoneAndPaymentStatusOptimized(
            customerName, customerPhone, paymentStatus);
        return items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 🚀 성능 최적화된 미완료 항목 조회
    @Override
    public List<ReturnItemDTO> findUnprocessedOptimized() {
        List<ReturnItem> items = returnItemRepository.findByIsCompletedFalseOptimized();
        return items.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    // 🎯 대표님 요청: 리스트에서 직접 날짜 일괄 수정 기능
    @Override
    @Transactional
    public int bulkUpdateDates(List<ReturnItemBulkDateUpdateDTO> updates) {
        log.info("일괄 날짜 업데이트 시작 - 대상 건수: {}", updates.size());
        
        int updatedCount = 0;
        
        try {
            for (ReturnItemBulkDateUpdateDTO updateDTO : updates) {
                if (updateDTO.getId() == null) {
                    log.warn("ID가 null인 항목 건너뜀: {}", updateDTO);
                    continue;
                }
                
                // 기존 엔티티 조회
                ReturnItem existingItem = returnItemRepository.findById(updateDTO.getId())
                    .orElse(null);
                
                if (existingItem == null) {
                    log.warn("존재하지 않는 ID: {}", updateDTO.getId());
                    continue;
                }
                
                // 날짜 필드만 업데이트 (기존 로직 절대 건드리지 않음)
                // JavaScript에서 변경된 필드만 전송되므로, 해당 필드만 업데이트 (null 포함)
                boolean hasChanges = false;
                
                // JavaScript에서 전송된 필드만 업데이트 (변경된 필드만 DTO에 포함됨)
                // JSON에서 필드가 존재하면 업데이트 (null 값도 포함)
                
                // 각 필드별로 개별 처리 - 실제로는 변경된 필드만 전송됨
                // 하지만 간단하게 하기 위해 모든 필드를 체크하고 업데이트
                
                // 회수완료 날짜 (변경된 경우만)
                if (updateDTO.getCollectionCompletedDate() != null || 
                    (updateDTO.getCollectionCompletedDate() == null && 
                     !Objects.equals(existingItem.getCollectionCompletedDate(), updateDTO.getCollectionCompletedDate()))) {
                    existingItem.setCollectionCompletedDate(updateDTO.getCollectionCompletedDate());
                    existingItem.setCollectionUpdatedBy(updateDTO.getUpdatedBy());
                    existingItem.setCollectionUpdatedDate(LocalDateTime.now());
                    hasChanges = true;
                }
                
                // 물류확인 날짜 (변경된 경우만)
                if (updateDTO.getLogisticsConfirmedDate() != null || 
                    (updateDTO.getLogisticsConfirmedDate() == null && 
                     !Objects.equals(existingItem.getLogisticsConfirmedDate(), updateDTO.getLogisticsConfirmedDate()))) {
                    existingItem.setLogisticsConfirmedDate(updateDTO.getLogisticsConfirmedDate());
                    existingItem.setLogisticsUpdatedBy(updateDTO.getUpdatedBy());
                    existingItem.setLogisticsUpdatedDate(LocalDateTime.now());
                    hasChanges = true;
                }
                
                // 출고 날짜 (변경된 경우만)
                if (updateDTO.getShippingDate() != null || 
                    (updateDTO.getShippingDate() == null && 
                     !Objects.equals(existingItem.getShippingDate(), updateDTO.getShippingDate()))) {
                    existingItem.setShippingDate(updateDTO.getShippingDate());
                    existingItem.setShippingUpdatedBy(updateDTO.getUpdatedBy());
                    existingItem.setShippingUpdatedDate(LocalDateTime.now());
                    hasChanges = true;
                }
                
                // 환불 날짜 (변경된 경우만)
                if (updateDTO.getRefundDate() != null || 
                    (updateDTO.getRefundDate() == null && 
                     !Objects.equals(existingItem.getRefundDate(), updateDTO.getRefundDate()))) {
                    existingItem.setRefundDate(updateDTO.getRefundDate());
                    existingItem.setRefundUpdatedBy(updateDTO.getUpdatedBy());
                    existingItem.setRefundUpdatedDate(LocalDateTime.now());
                    hasChanges = true;
                }
                
                if (hasChanges) {
                    // UPDATE_DATE는 JPA @PreUpdate가 자동 처리
                    returnItemRepository.save(existingItem);
                    updatedCount++;
                    log.debug("날짜 업데이트 완료 - ID: {}", updateDTO.getId());
                }
            }
            
            log.info("일괄 날짜 업데이트 완료 - 업데이트된 건수: {}/{}", updatedCount, updates.size());
            
        } catch (Exception e) {
            log.error("일괄 날짜 업데이트 중 오류 발생", e);
            throw new RuntimeException("날짜 업데이트 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
        
        return updatedCount;
    }
    
    // 🎯 대표님 요청: 완료 상태 업데이트 기능
    @Override
    @Transactional
    public boolean updateCompletionStatus(Long id, Boolean isCompleted) {
        log.info("완료 상태 업데이트 시작 - ID: {}, 완료: {}", id, isCompleted);
        
        try {
            // 기존 엔티티 조회
            ReturnItem existingItem = returnItemRepository.findById(id)
                .orElse(null);
            
            if (existingItem == null) {
                log.warn("존재하지 않는 ID: {}", id);
                return false;
            }
            
            // IS_COMPLETED 컬럼 업데이트 (기존 로직 절대 건드리지 않음)
            // Boolean을 Integer로 변환 (true: 1, false: 0)
            existingItem.setIsCompleted(isCompleted ? 1 : 0);
            
            // 완료 처리자는 PROCESSOR 컬럼에 기록 (대표님 요청)
            if (isCompleted) {
                // 현재 로그인 사용자 정보 가져오기
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String processor = authentication != null ? authentication.getName() : "시스템";
                existingItem.setProcessor(processor);
                log.info("완료 처리자 설정: {}", processor);
            }
            
            // 저장 (UPDATE_DATE는 JPA @PreUpdate가 자동 처리)
            returnItemRepository.save(existingItem);
            
            log.info("완료 상태 업데이트 성공 - ID: {}, 완료: {}", id, isCompleted);
            return true;
            
        } catch (Exception e) {
            log.error("완료 상태 업데이트 중 오류 발생 - ID: {}", id, e);
            throw new RuntimeException("완료 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 전체 완료 건수 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Long getCompletedCount() {
        try {
            long count = returnItemRepository.countByIsCompletedTrue();
            log.info("✅ 완료된 항목 통계: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("완료된 항목 통계 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * 미완료 건수 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Long getIncompletedCount() {
        try {
            long count = returnItemRepository.countByIsCompletedFalse();
            log.info("❌ 미완료 항목 통계: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("미완료 항목 통계 조회 실패: {}", e.getMessage(), e);
            return 0L;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByCompleted(ReturnItemSearchDTO searchDTO) {
        log.info("🎯 완료된 항목 조회 시작 - 페이지: {}, 크기: {}", searchDTO.getPage(), searchDTO.getSize());
        
        int startRow = searchDTO.getPage() * searchDTO.getSize();
        int endRow = startRow + searchDTO.getSize();
        
        List<ReturnItem> entities = returnItemRepository.findByCompleted(startRow, endRow);
        log.info("✅ 완료된 항목 조회 결과: {} 건", entities.size());
        
        long totalElements = returnItemRepository.countByIsCompletedTrue();
        log.info("📊 전체 완료된 항목 수: {} 건", totalElements);
        
        List<ReturnItemDTO> dtoList = entities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        Sort sort = searchDTO.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(searchDTO.getSortBy()).ascending() : Sort.by(searchDTO.getSortBy()).descending();
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        
        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByIncompleted(ReturnItemSearchDTO searchDTO) {
        log.info("🎯 미완료 항목 조회 시작 - 페이지: {}, 크기: {}", searchDTO.getPage(), searchDTO.getSize());
        
        int startRow = searchDTO.getPage() * searchDTO.getSize();
        int endRow = startRow + searchDTO.getSize();
        
        List<ReturnItem> entities = returnItemRepository.findByIncompleted(startRow, endRow);
        log.info("❌ 미완료 항목 조회 결과: {} 건", entities.size());
        
        long totalElements = returnItemRepository.countByIsCompletedFalse();
        log.info("📊 전체 미완료 항목 수: {} 건", totalElements);
        
        List<ReturnItemDTO> dtoList = entities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        Sort sort = searchDTO.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(searchDTO.getSortBy()).ascending() : Sort.by(searchDTO.getSortBy()).descending();
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        
        return new PageImpl<>(dtoList, pageable, totalElements);
    }
    
    /**
     * 🎯 다중 필터 처리 구현 (교집합 처리)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByMultipleFilters(List<String> filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 교집합 처리 시작 - 필터: {}", filters);
        
        if (filters == null || filters.isEmpty()) {
            log.warn("⚠️ 필터 목록이 비어있음, 전체 조회로 fallback");
            return findAll(searchDTO.getPage(), searchDTO.getSize(), searchDTO.getSortBy(), searchDTO.getSortDir());
        }
        
        // 단일 필터인 경우 기존 로직 사용
        if (filters.size() == 1) {
            String singleFilter = filters.get(0).trim();
            log.info("🎯 단일 필터 적용: {}", singleFilter);
            return applySingleFilterForMultiple(singleFilter, searchDTO);
        }
        
        // 🎯 다중 필터 교집합 처리
        log.info("🔄 다중 필터 교집합 처리 시작 - 필터 개수: {}", filters.size());
        
        // 먼저 전체 데이터를 가져와서 메모리에서 필터링 (성능 이슈가 있을 수 있지만 정확성 우선)
        // 페이징 없이 전체 데이터 조회
        ReturnItemSearchDTO fullSearchDTO = new ReturnItemSearchDTO();
        fullSearchDTO.setPage(0);
        fullSearchDTO.setSize(Integer.MAX_VALUE); // 전체 데이터
        fullSearchDTO.setSortBy(searchDTO.getSortBy());
        fullSearchDTO.setSortDir(searchDTO.getSortDir());
        
        List<ReturnItemDTO> allData = findAll(0, Integer.MAX_VALUE, searchDTO.getSortBy(), searchDTO.getSortDir()).getContent();
        log.info("📊 전체 데이터 조회 완료: {} 건", allData.size());
        
        // 각 필터 조건을 순차적으로 적용하여 교집합 생성
        List<ReturnItemDTO> filteredData = new ArrayList<>(allData);
        
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
        
        // 페이징 처리
        int start = searchDTO.getPage() * searchDTO.getSize();
        int end = Math.min(start + searchDTO.getSize(), filteredData.size());
        
        List<ReturnItemDTO> pagedData = filteredData.subList(start, end);
        
        // 정렬 처리 (필요한 경우)
        if ("id".equals(searchDTO.getSortBy())) {
            pagedData.sort((a, b) -> "ASC".equalsIgnoreCase(searchDTO.getSortDir()) ? 
                Long.compare(a.getId(), b.getId()) : Long.compare(b.getId(), a.getId()));
        }
        
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
        Page<ReturnItemDTO> result = new PageImpl<>(pagedData, pageable, filteredData.size());
        
        log.info("✅ 다중 필터 교집합 처리 완료 - 최종 결과: {} 건 (페이지: {}/{})", 
            result.getTotalElements(), searchDTO.getPage() + 1, result.getTotalPages());
        
        return result;
    }
    
    /**
     * 🎯 개별 필터 조건 확인 메서드
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
                return item.getShippingDate() != null;
            case "shipping-pending":
                return item.getShippingDate() == null;
            case "refund-completed":
                return item.getRefundDate() != null;
            case "refund-pending":
                return item.getRefundDate() == null;
            case "payment-completed":
                return "입금완료".equals(item.getPaymentStatusText());
            case "payment-pending":
                return "입금예정".equals(item.getPaymentStatusText());
            case "completed":
                return item.getIsCompleted() != null && item.getIsCompleted() == 1;
            case "incompleted":
                return item.getIsCompleted() == null || item.getIsCompleted() != 1;
            case "overdue-ten-days":
                // 처리기간 임박 필터 - 접수일 기준 10일 이상 미완료 건 (데이터베이스와 동일한 기준)
                if (item.getCsReceivedDate() != null && 
                    (item.getIsCompleted() == null || item.getIsCompleted() != 1)) {
                    LocalDate tenDaysAgo = LocalDate.now().minusDays(10);
                    // 데이터베이스와 동일하게 <= 기준으로 통일 (10일 전 이하)
                    return item.getCsReceivedDate().isBefore(tenDaysAgo) || item.getCsReceivedDate().isEqual(tenDaysAgo);
                }
                return false;
            default:
                log.warn("⚠️ 알 수 없는 필터 타입: {}", filterType);
                return true; // 알 수 없는 필터는 모든 데이터 통과
        }
    }
    
    /**
     * 🎯 단일 필터 적용 메서드 (다중 필터용)
     */
    private Page<ReturnItemDTO> applySingleFilterForMultiple(String filterType, ReturnItemSearchDTO searchDTO) {
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
                log.warn("⚠️ 알 수 없는 필터: {}", filterType);
                return findAll(searchDTO.getPage(), searchDTO.getSize(), searchDTO.getSortBy(), searchDTO.getSortDir());
        }
    }
    
    /**
     * 🎯 다중 필터 + 검색 조건 함께 처리 구현
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findByMultipleFiltersWithSearch(List<String> filters, ReturnItemSearchDTO searchDTO) {
        log.info("🔍 다중 필터 + 검색 교집합 처리 시작 - 필터: {}, 검색: {}", filters, searchDTO.getKeyword());
        
        if (filters == null || filters.isEmpty()) {
            log.warn("⚠️ 필터 목록이 비어있음, 검색만 적용");
            return search(searchDTO);
        }
        
        // 🎯 먼저 검색 조건으로 데이터 필터링
        log.info("🔍 1단계: 검색 조건 적용");
        
        // 검색 조건이 있으면 먼저 검색하여 기본 데이터셋 구성
        List<ReturnItemDTO> searchResults;
        long totalSearchCount;
        
        if (searchDTO.hasSearchCondition()) {
            // 검색 조건으로 전체 데이터 조회 (페이징 없이)
            ReturnItemSearchDTO fullSearchDTO = new ReturnItemSearchDTO();
            fullSearchDTO.setKeyword(searchDTO.getKeyword());
            fullSearchDTO.setStartDate(searchDTO.getStartDate());
            fullSearchDTO.setEndDate(searchDTO.getEndDate());
            fullSearchDTO.setPage(0);
            fullSearchDTO.setSize(Integer.MAX_VALUE);
            fullSearchDTO.setSortBy(searchDTO.getSortBy());
            fullSearchDTO.setSortDir(searchDTO.getSortDir());
            
            Page<ReturnItemDTO> searchPage = search(fullSearchDTO);
            searchResults = searchPage.getContent();
            totalSearchCount = searchPage.getTotalElements();
            log.info("📊 검색 결과: {} 건", totalSearchCount);
        } else {
            // 검색 조건이 없으면 전체 데이터
            searchResults = findAll(0, Integer.MAX_VALUE, searchDTO.getSortBy(), searchDTO.getSortDir()).getContent();
            totalSearchCount = searchResults.size();
            log.info("📊 전체 데이터: {} 건", totalSearchCount);
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
        
        // 정렬 처리 (필요한 경우)
        if ("id".equals(searchDTO.getSortBy())) {
            pagedData.sort((a, b) -> "ASC".equalsIgnoreCase(searchDTO.getSortDir()) ? 
                Long.compare(a.getId(), b.getId()) : Long.compare(b.getId(), a.getId()));
        }
        
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
        Page<ReturnItemDTO> result = new PageImpl<>(pagedData, pageable, filteredData.size());
        
        log.info("✅ 다중 필터 + 검색 교집합 처리 완료 - 최종 결과: {} 건 (페이지: {}/{})", 
            result.getTotalElements(), searchDTO.getPage() + 1, result.getTotalPages());
        
        return result;
    }
    
    /**
     * 🎯 처리기간 임박 필터 - 접수일 기준 10일 이상 미완료 데이터 개수 조회 - Oracle SYSDATE 기준으로 통일
     */
    @Override
    @Transactional(readOnly = true)
    public Long getOverdueTenDaysCount() {
        log.info("🔍 처리기간 임박 카운트 조회 시작 - 접수일 기준 10일 이상 미완료 건");
        
        // Oracle SYSDATE 기준으로 통일 (Repository에서 자체 계산)
        log.info("📅 기준: Oracle SYSDATE - 10일 (데이터베이스 시간 기준)");
        
        // 10일 전 이전에 접수되었으면서 아직 완료되지 않은 건 조회
        long count = returnItemRepository.countOverdueTenDays();
        log.info("📊 접수일 기준 10일 이상 미완료 건수: {} 건", count);
        
        return count;
    }
    
    /**
     * 🎯 처리기간 임박 필터 - 접수일 기준 10일 이상 미완료 데이터 조회 - Oracle SYSDATE 기준으로 통일
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReturnItemDTO> findOverdueTenDays(ReturnItemSearchDTO searchDTO) {
        log.info("🔍 처리기간 임박 데이터 조회 시작 - 접수일 기준 10일 이상 미완료 건");
        log.info("🔍 검색 조건: {}", searchDTO);
        
        // Oracle SYSDATE 기준으로 통일 (Repository에서 자체 계산)
        log.info("📅 기준: Oracle SYSDATE - 10일 (데이터베이스 시간 기준)");
        
        // 페이징 처리
        int startRow = searchDTO.getPage() * searchDTO.getSize();
        int endRow = startRow + searchDTO.getSize();
        
        // 10일 전 이전에 접수되었으면서 아직 완료되지 않은 건 조회
        List<ReturnItem> entities = returnItemRepository.findOverdueTenDays(startRow, endRow);
        log.info("📊 접수일 기준 10일 이상 미완료 데이터 조회 결과: {} 건", entities.size());
        
        // 전체 카운트 조회
        long totalElements = returnItemRepository.countOverdueTenDays();
        log.info("📊 전체 접수일 기준 10일 이상 미완료 건수: {} 건", totalElements);
        
        // DTO 변환
        List<ReturnItemDTO> dtoList = entities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        // 정렬 처리
        Sort sort = searchDTO.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(searchDTO.getSortBy()).ascending() : Sort.by(searchDTO.getSortBy()).descending();
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        
        Page<ReturnItemDTO> result = new PageImpl<>(dtoList, pageable, totalElements);
        
        log.info("✅ 처리기간 임박 데이터 조회 완료 - 총 {} 건 (페이지: {}/{})", 
            result.getTotalElements(), searchDTO.getPage() + 1, result.getTotalPages());
        
        return result;
    }
} 