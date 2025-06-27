package com.wio.repairsystem.service;

import com.wio.repairsystem.dto.ShippingPaymentRegisterDTO;
import com.wio.repairsystem.model.ReturnItem;
import com.wio.repairsystem.model.ShippingPaymentRegister;
import com.wio.repairsystem.repository.ReturnItemRepository;
import com.wio.repairsystem.repository.ShippingPaymentRegisterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 배송비 입금 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShippingPaymentService {
    
    private final ShippingPaymentRegisterRepository shippingPaymentRepository;
    private final ReturnItemRepository returnItemRepository;
    
    /**
     * 입금 내역 등록
     */
    public ShippingPaymentRegisterDTO registerPayment(ShippingPaymentRegisterDTO dto) {
        log.info("배송비 입금 내역 등록 시작: {}", dto.getCustomerName());
        
        // DTO를 Entity로 변환
        ShippingPaymentRegister entity = ShippingPaymentRegister.builder()
                .brand(dto.getBrand())
                .siteName(dto.getSiteName())
                .bankName(dto.getBankName())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .amount(dto.getAmount())
                .paymentDate(dto.getPaymentDate())
                .registrar(dto.getRegistrar())
                .notes(dto.getNotes())
                .mappingStatus("PENDING")
                .build();
        
        // 저장
        ShippingPaymentRegister saved = shippingPaymentRepository.save(entity);
        
        // 자동 매핑 시도
        tryAutoMapping(saved);
        
        log.info("배송비 입금 내역 등록 완료: ID={}", saved.getRegisterId());
        return convertToDTO(saved);
    }
    
    /**
     * 자동 매핑 시도
     */
    private void tryAutoMapping(ShippingPaymentRegister payment) {
        log.info("자동 매핑 시도: 고객명={}, 연락처={}", payment.getCustomerName(), payment.getCustomerPhone());
        
        // 고객명과 연락처로 매칭되는 교환/반품 찾기 (배송비가 입금대기 상태인 것만)
        List<ReturnItem> matchingReturns = returnItemRepository.findByCustomerNameAndCustomerPhoneAndPaymentStatus(
                payment.getCustomerName(), 
                payment.getCustomerPhone(), 
                "PENDING"
        );
        
        if (!matchingReturns.isEmpty()) {
            // 사이트명도 일치하는 것 우선 선택
            Optional<ReturnItem> siteMatch = matchingReturns.stream()
                    .filter(r -> payment.getSiteName().equals(r.getSiteName()))
                    .findFirst();
            
            ReturnItem targetReturn = siteMatch.orElse(matchingReturns.get(0));
            
            // 매핑 처리
            performMapping(payment.getRegisterId(), targetReturn.getId());
            
            log.info("자동 매핑 성공: 입금ID={}, 교환/반품ID={}", payment.getRegisterId(), targetReturn.getId());
        } else {
            log.info("자동 매핑 실패: 매칭되는 교환/반품 없음");
        }
    }
    
    /**
     * 수동 매핑 처리
     */
    public void performMapping(Long paymentId, Long returnItemId) {
        log.info("수동 매핑 처리 시작: 입금ID={}, 교환/반품ID={}", paymentId, returnItemId);
        
        // 입금 내역 조회
        ShippingPaymentRegister payment = shippingPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("입금 내역을 찾을 수 없습니다: " + paymentId));
        
        // 교환/반품 내역 조회
        ReturnItem returnItem = returnItemRepository.findById(returnItemId)
                .orElseThrow(() -> new RuntimeException("교환/반품 내역을 찾을 수 없습니다: " + returnItemId));
        
        // 이미 매핑된 입금인지 확인
        if ("MAPPED".equals(payment.getMappingStatus())) {
            throw new RuntimeException("이미 매핑된 입금 내역입니다.");
        }
        
        // 이미 입금 완료된 교환/반품인지 확인
        if ("COMPLETED".equals(returnItem.getPaymentStatus())) {
            throw new RuntimeException("이미 입금이 완료된 교환/반품입니다.");
        }
        
        // 🎯 매핑 처리 및 상태 업데이트
        payment.setMappingStatus("MAPPED");
        payment.setReturnItemId(returnItemId);
        shippingPaymentRepository.save(payment);
        
        returnItem.setPaymentStatus("COMPLETED");
        returnItem.setPaymentId(paymentId);
        returnItemRepository.save(returnItem);
        
        log.info("🎯 매핑 완료: 입금ID={}, 교환/반품ID={}, 고객명={}", 
                paymentId, returnItemId, returnItem.getCustomerName());
        
        // 🚀 교환 상품인 경우 출고 가능 알림 로그
        if (returnItem.getReturnTypeCode() != null && 
            returnItem.getReturnTypeCode().contains("EXCHANGE")) {
            log.info("🚀 교환 상품 출고 가능: 주문번호={}, 고객명={}, 주문품번={}", 
                    returnItem.getOrderNumber(), 
                    returnItem.getCustomerName(),
                    returnItem.getOrderItemCode());
        }
    }
    
    /**
     * 매핑 해제
     */
    public void unmapping(Long paymentId) {
        log.info("매핑 해제 시작: 입금ID={}", paymentId);
        
        ShippingPaymentRegister payment = shippingPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("입금 내역을 찾을 수 없습니다: " + paymentId));
        
        if (payment.getReturnItemId() != null) {
            // 교환/반품 상태 원복
            ReturnItem returnItem = returnItemRepository.findById(payment.getReturnItemId())
                    .orElse(null);
            if (returnItem != null) {
                returnItem.setPaymentStatus("PENDING");
                returnItem.setPaymentId(null);
                returnItemRepository.save(returnItem);
            }
        }
        
        // 입금 내역 매핑 해제
        payment.setMappingStatus("PENDING");
        payment.setReturnItemId(null);
        shippingPaymentRepository.save(payment);
        
        log.info("매핑 해제 완료: 입금ID={}", paymentId);
    }
    
    /**
     * 입금 내역 목록 조회 (Oracle 페이징 호환)
     */
    @Transactional(readOnly = true)
    public Page<ShippingPaymentRegisterDTO> getPaymentList(String brand, String status, String customerName, 
                                                          String customerPhone, String siteName, Pageable pageable) {
        
        // Oracle 11g 호환을 위해 페이징 없이 전체 조회 후 메모리에서 페이징 처리
        List<ShippingPaymentRegister> allEntities;
        
        // 다중 필터 조건 처리 (페이징 없이)
        if (hasMultipleFilters(brand, status, customerName, customerPhone, siteName)) {
            // 커스텀 쿼리 메서드 사용 (페이징 없음)
            allEntities = findByMultipleFiltersWithoutPaging(brand, status, customerName, customerPhone, siteName);
        } else if (brand != null && !brand.isEmpty()) {
            allEntities = shippingPaymentRepository.findByBrand(brand, Sort.by(Sort.Direction.DESC, "registerDate"));
        } else if (status != null && !status.isEmpty()) {
            allEntities = shippingPaymentRepository.findByMappingStatus(status, Sort.by(Sort.Direction.DESC, "registerDate"));
        } else if (customerName != null && !customerName.isEmpty()) {
            allEntities = shippingPaymentRepository.findByCustomerNameContaining(customerName, Sort.by(Sort.Direction.DESC, "registerDate"));
        } else if (customerPhone != null && !customerPhone.isEmpty()) {
            allEntities = shippingPaymentRepository.findByCustomerPhoneContaining(customerPhone, Sort.by(Sort.Direction.DESC, "registerDate"));
        } else if (siteName != null && !siteName.isEmpty()) {
            allEntities = shippingPaymentRepository.findBySiteNameContaining(siteName, Sort.by(Sort.Direction.DESC, "registerDate"));
        } else {
            allEntities = shippingPaymentRepository.findAll(Sort.by(Sort.Direction.DESC, "registerDate"));
        }
        
        // 메모리에서 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allEntities.size());
        
        List<ShippingPaymentRegister> pageContent = allEntities.subList(start, end);
        List<ShippingPaymentRegisterDTO> dtoList = pageContent.stream()
                .map(this::convertToDTOWithReturnInfo)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, allEntities.size());
    }
    
    /**
     * 다중 필터 조건으로 조회 (페이징 없음)
     */
    private List<ShippingPaymentRegister> findByMultipleFiltersWithoutPaging(String brand, String status, 
                                                                            String customerName, String customerPhone, String siteName) {
        // 간단한 구현: 전체 조회 후 필터링
        return shippingPaymentRepository.findAll(Sort.by(Sort.Direction.DESC, "registerDate"))
                .stream()
                .filter(entity -> (brand == null || brand.isEmpty() || brand.equals(entity.getBrand())))
                .filter(entity -> (status == null || status.isEmpty() || status.equals(entity.getMappingStatus())))
                .filter(entity -> (customerName == null || customerName.isEmpty() || 
                                 entity.getCustomerName().toLowerCase().contains(customerName.toLowerCase())))
                .filter(entity -> (customerPhone == null || customerPhone.isEmpty() || 
                                 entity.getCustomerPhone().contains(customerPhone)))
                .filter(entity -> (siteName == null || siteName.isEmpty() || 
                                 entity.getSiteName().toLowerCase().contains(siteName.toLowerCase())))
                .collect(Collectors.toList());
    }
    
    /**
     * 다중 필터 조건 체크
     */
    private boolean hasMultipleFilters(String brand, String status, String customerName, 
                                     String customerPhone, String siteName) {
        int filterCount = 0;
        if (brand != null && !brand.isEmpty()) filterCount++;
        if (status != null && !status.isEmpty()) filterCount++;
        if (customerName != null && !customerName.isEmpty()) filterCount++;
        if (customerPhone != null && !customerPhone.isEmpty()) filterCount++;
        if (siteName != null && !siteName.isEmpty()) filterCount++;
        return filterCount > 1;
    }
    
    /**
     * 통계 데이터 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getPaymentStats() {
        Map<String, Long> stats = new HashMap<>();
        
        // 전체 입금 수
        stats.put("total", shippingPaymentRepository.count());
        
        // 매핑 완료 수
        stats.put("mapped", shippingPaymentRepository.countByMappingStatus("MAPPED"));
        
        // 미매핑 수
        stats.put("unmapped", shippingPaymentRepository.countByMappingStatus("PENDING"));
        
        // 브랜드별 통계
        stats.put("renoma", shippingPaymentRepository.countByBrand("RENOMA"));
        stats.put("coralik", shippingPaymentRepository.countByBrand("CORALIK"));
        
        return stats;
    }
    
    /**
     * 입금 내역 상세 조회
     */
    @Transactional(readOnly = true)
    public ShippingPaymentRegisterDTO getPaymentDetail(Long paymentId) {
        ShippingPaymentRegister entity = shippingPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("입금 내역을 찾을 수 없습니다: " + paymentId));
        
        return convertToDTOWithReturnInfo(entity);
    }
    
    /**
     * 입금 내역 삭제
     */
    public void deletePayment(Long paymentId) {
        log.info("입금 내역 삭제 시작: ID={}", paymentId);
        
        ShippingPaymentRegister payment = shippingPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("입금 내역을 찾을 수 없습니다: " + paymentId));
        
        // 매핑되어 있다면 먼저 해제
        if ("MAPPED".equals(payment.getMappingStatus())) {
            unmapping(paymentId);
        }
        
        // 삭제
        shippingPaymentRepository.delete(payment);
        
        log.info("입금 내역 삭제 완료: ID={}", paymentId);
    }
    
    /**
     * 매핑 가능한 교환/반품 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReturnItem> getMappableReturns(String customerName, String customerPhone) {
        return returnItemRepository.findByCustomerNameAndCustomerPhoneAndPaymentStatus(
                customerName, customerPhone, "PENDING"
        );
    }
    
    /**
     * Entity를 DTO로 변환 (기본)
     */
    private ShippingPaymentRegisterDTO convertToDTO(ShippingPaymentRegister entity) {
        return ShippingPaymentRegisterDTO.builder()
                .registerId(entity.getRegisterId())
                .brand(entity.getBrand())
                .siteName(entity.getSiteName())
                .bankName(entity.getBankName())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .amount(entity.getAmount())
                .paymentDate(entity.getPaymentDate())
                .registerDate(entity.getRegisterDate())
                .registrar(entity.getRegistrar())
                .mappingStatus(entity.getMappingStatus())
                .returnItemId(entity.getReturnItemId())
                .notes(entity.getNotes())
                .build();
    }
    
    /**
     * Entity를 DTO로 변환 (교환/반품 정보 포함)
     */
    private ShippingPaymentRegisterDTO convertToDTOWithReturnInfo(ShippingPaymentRegister entity) {
        ShippingPaymentRegisterDTO dto = convertToDTO(entity);
        
        // 매핑된 교환/반품 정보 추가
        if (entity.getReturnItemId() != null) {
            returnItemRepository.findById(entity.getReturnItemId())
                    .ifPresent(returnItem -> {
                        dto.setReturnOrderNumber(returnItem.getOrderNumber());
                        dto.setReturnCustomerName(returnItem.getCustomerName());
                        dto.setReturnCustomerPhone(returnItem.getCustomerPhone());
                    });
        }
        
        return dto;
    }
} 