package com.wio.repairsystem.service.impl;

import com.wio.repairsystem.dto.ShippingPaymentMappingDTO;
import com.wio.repairsystem.model.ReturnItem;
import com.wio.repairsystem.model.ShippingPaymentRegister;
import com.wio.repairsystem.repository.ReturnItemRepository;
import com.wio.repairsystem.repository.ShippingPaymentRegisterRepository;
import com.wio.repairsystem.service.ShippingPaymentMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 배송비 입금 매핑 서비스 구현체
 * 스마트 매칭 알고리즘과 상담원 지원 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShippingPaymentMappingServiceImpl implements ShippingPaymentMappingService {
    
    private final ShippingPaymentRegisterRepository paymentRepository;
    private final ReturnItemRepository returnItemRepository;
    
    @Override
    public ShippingPaymentMappingDTO.DashboardStats getDashboardStats() {
        log.info("대시보드 통계 정보 조회 시작");
        
        try {
            // 전체 통계
            long totalPayments = paymentRepository.count();
            long mappedPayments = paymentRepository.countByMappingStatus("MAPPED");
            long pendingPayments = paymentRepository.countByMappingStatus("PENDING");
            
            // 브랜드별 통계
            long renomaCount = paymentRepository.countByBrand("레노마");
            long coralikCount = paymentRepository.countByBrand("코랄리크");
            
            // 오늘 날짜 범위 계산
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            log.info("오늘 날짜 범위: {} ~ {}", startOfDay, endOfDay);
            
            // 오늘 통계
            long todayRegistered = paymentRepository.countTodayRegistered(startOfDay, endOfDay);
            long todayMapped = paymentRepository.countTodayMapped(startOfDay, endOfDay);
            
            log.info("통계 조회 완료 - 전체: {}, 매핑완료: {}, 미매핑: {}, 오늘등록: {}, 오늘매핑: {}", 
                    totalPayments, mappedPayments, pendingPayments, todayRegistered, todayMapped);
            
            return ShippingPaymentMappingDTO.DashboardStats.builder()
                    .totalPayments(totalPayments)
                    .mappedPayments(mappedPayments)
                    .pendingPayments(pendingPayments)
                    .renomaCount(renomaCount)
                    .coralikCount(coralikCount)
                    .todayRegistered(todayRegistered)
                    .todayMapped(todayMapped)
                    .build();
                    
        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패: {}", e.getMessage(), e);
            return ShippingPaymentMappingDTO.DashboardStats.builder()
                    .totalPayments(0L)
                    .mappedPayments(0L)
                    .pendingPayments(0L)
                    .todayRegistered(0L)
                    .todayMapped(0L)
                    .build();
        }
    }
    
    @Override
    public Page<ShippingPaymentMappingDTO.PaymentInfo> getPendingPayments(Pageable pageable) {
        log.info("미매핑 입금 내역 조회 - 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<ShippingPaymentRegister> pendingPayments = 
                paymentRepository.findByMappingStatus("PENDING", pageable);
            
            List<ShippingPaymentMappingDTO.PaymentInfo> paymentInfos = pendingPayments.getContent()
                    .stream()
                    .map(this::convertToPaymentInfo)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(paymentInfos, pageable, pendingPayments.getTotalElements());
            
        } catch (Exception e) {
            log.error("미매핑 입금 내역 조회 실패: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }
    
    @Override
    public List<ShippingPaymentMappingDTO.ReturnItemCandidate> findCandidatesForPayment(Long paymentId) {
        log.info("입금 내역 {}에 대한 교환/반품 후보 조회 시작", paymentId);
        
        try {
            ShippingPaymentRegister payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("입금 내역을 찾을 수 없습니다: " + paymentId));
            
            log.info("입금 내역 정보 - 고객명: {}, 연락처: {}, 금액: {}, 브랜드: {}", 
                    payment.getCustomerName(), payment.getCustomerPhone(), 
                    payment.getAmount(), payment.getBrand());
            
            // 브랜드를 한글로 변환
            String koreanBrand = convertBrandToKorean(payment.getBrand());
            log.info("브랜드 변환: {} → {}", payment.getBrand(), koreanBrand);
            
            // 1차: 우선순위 후보 - PAYMENT_STATUS='PENDING'이고 아직 매핑되지 않은 교환/반품
            List<ReturnItem> primaryCandidates = returnItemRepository.findByPaymentStatusAndPaymentIdIsNull("PENDING");
            log.info("1차 우선순위 후보: PAYMENT_STATUS='PENDING'이고 아직 매핑되지 않은 교환/반품 {}개", primaryCandidates.size());
            
            // 2차: 확장 후보 - 브랜드가 매칭되는 모든 교환/반품 (이미 매핑된 것도 포함)
            List<ReturnItem> extendedCandidates = returnItemRepository.findBySiteNameContainingBrand(koreanBrand);
            log.info("2차 확장 후보: 브랜드 '{}' 매칭 전체 교환/반품 {}개", koreanBrand, extendedCandidates.size());
            
            // 중복 제거를 위한 Set
            Set<Long> processedIds = new HashSet<>();
            List<ReturnItem> allReturnItems = new ArrayList<>();
            
            // 1차 후보를 먼저 추가 (우선순위)
            for (ReturnItem item : primaryCandidates) {
                allReturnItems.add(item);
                processedIds.add(item.getId());
            }
            
            // 2차 후보 중 중복되지 않은 것만 추가
            for (ReturnItem item : extendedCandidates) {
                if (!processedIds.contains(item.getId())) {
                    allReturnItems.add(item);
                    processedIds.add(item.getId());
                }
            }
            
            log.info("전체 후보 조합 완료: 1차 {}개 + 2차 추가 {}개 = 총 {}개", 
                    primaryCandidates.size(), 
                    allReturnItems.size() - primaryCandidates.size(), 
                    allReturnItems.size());
            
            if (allReturnItems.isEmpty()) {
                log.warn("⚠️ 브랜드 '{}'와 매칭되는 교환/반품 데이터가 전혀 없습니다!", koreanBrand);
                log.warn("💡 해결방법: 교환/반품 등록 시 사이트명에 브랜드명을 포함하세요");
                return new ArrayList<>();
            }
            
            List<ShippingPaymentMappingDTO.ReturnItemCandidate> candidates = new ArrayList<>();
            
            for (ReturnItem returnItem : allReturnItems) {
                // 브랜드 매칭 확인 (상세 로그 추가)
                log.info("🔍 ReturnItem[{}] 브랜드 매칭 검사 - 사이트명: '{}', 요구브랜드: '{}'", 
                        returnItem.getId(), returnItem.getSiteName(), koreanBrand);
                
                boolean brandMatched = isBrandMatched(koreanBrand, returnItem.getSiteName());
                log.info("🔍 ReturnItem[{}] 브랜드 매칭 결과: {}", returnItem.getId(), brandMatched);
                
                if (!brandMatched) {
                    log.warn("❌ 브랜드 불일치로 제외 - 사이트명: '{}', 요구브랜드: '{}'", returnItem.getSiteName(), koreanBrand);
                    continue;
                }
                
                log.info("✅ 브랜드 매칭 성공 - ReturnItem[{}] 진행", returnItem.getId());
                
                // 매핑 상태 확인
                boolean isAlreadyMapped = returnItem.getPaymentId() != null;
                boolean isPendingPayment = "PENDING".equals(returnItem.getPaymentStatus());
                
                log.debug("교환/반품 검토 - ID: {}, 고객명: {}, 연락처: {}, 배송비: {}, 사이트: {}, 매핑상태: {}, 결제상태: {}", 
                        returnItem.getId(), returnItem.getCustomerName(), 
                        returnItem.getCustomerPhone(), returnItem.getShippingFee(), returnItem.getSiteName(),
                        isAlreadyMapped ? "매핑완료" : "미매핑", returnItem.getPaymentStatus());
                
                int matchScore = calculateMatchScore(payment, returnItem);
                
                // 이미 매핑된 경우 점수를 낮춤 (참고용으로만 표시)
                if (isAlreadyMapped) {
                    matchScore = Math.max(0, matchScore - 50); // 50점 차감
                    log.info("🎯 ReturnItem[{}] 매칭 점수: {}점 (이미 매핑됨, -50점 적용)", returnItem.getId(), matchScore);
                } else {
                    log.info("🎯 ReturnItem[{}] 매칭 점수: {}점 (매핑 가능)", returnItem.getId(), matchScore);
                }
                
                // 모든 브랜드 매칭 후보를 추가 (매핑 상태와 상관없이)
                ShippingPaymentMappingDTO.ReturnItemCandidate candidate = 
                        convertToCandidate(returnItem, matchScore);
                candidates.add(candidate);
                
                String statusInfo = isAlreadyMapped ? " (이미 매핑됨)" : (isPendingPayment ? " (매핑 가능)" : " (결제완료)");
                log.info("✅ 후보 추가 - 점수: {}점, 고객명: {}, 연락처: {}, 배송비: {}{}", 
                        matchScore, returnItem.getCustomerName(), returnItem.getCustomerPhone(), 
                        returnItem.getShippingFee(), statusInfo);
            }
            
            // 매칭 점수 순으로 정렬
            candidates.sort(Comparator.comparing(
                    ShippingPaymentMappingDTO.ReturnItemCandidate::getMatchScore, 
                    Comparator.reverseOrder()));
            
            log.info("입금 내역 {}에 대한 후보 {}개 찾음 (브랜드 {} 매칭, 전체 교환/반품 {}개 중)", 
                    paymentId, candidates.size(), koreanBrand, allReturnItems.size());
            
            if (candidates.isEmpty()) {
                log.warn("⚠️ 브랜드가 매칭되는 후보가 없습니다!");
                log.warn("💡 브랜드만 일치하면 점수와 상관없이 모든 후보를 표시합니다");
            }
            
            return candidates;
            
        } catch (Exception e) {
            log.error("후보 조회 실패 - 입금ID: {}, 오류: {}", paymentId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public ShippingPaymentMappingDTO.MappingResult executeMapping(ShippingPaymentMappingDTO.MappingRequest request) {
        log.info("매핑 실행 시작 - 입금ID: {}, 교환반품ID: {}", request.getPaymentId(), request.getReturnItemId());
        
        try {
            // 입금 내역 조회
            ShippingPaymentRegister payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("입금 내역을 찾을 수 없습니다"));
            
            // 교환/반품 내역 조회
            ReturnItem returnItem = returnItemRepository.findById(request.getReturnItemId())
                    .orElseThrow(() -> new RuntimeException("교환/반품 내역을 찾을 수 없습니다"));
            
            // 매핑 실행
            payment.setMappingStatus("MAPPED");
            payment.setReturnItemId(request.getReturnItemId());
            payment.setNotes(request.getNotes());
            payment.setMappedDate(LocalDateTime.now()); // 매핑 완료 시점 기록
            paymentRepository.save(payment);
            
            // 교환/반품 상태 업데이트
            returnItem.setPaymentStatus("COMPLETED");
            returnItem.setPaymentId(request.getPaymentId());
            // 배송비를 입금 테이블의 실제 입금 금액으로 업데이트
            returnItem.setShippingFee(payment.getAmount().toString());
            returnItemRepository.save(returnItem);
            
            log.info("교환/반품 업데이트 완료 - ID: {}, PAYMENT_STATUS: COMPLETED, 배송비: {}원", 
                    returnItem.getId(), payment.getAmount());
            
            // 교환 상품 출고 가능 여부 확인
            boolean canShipExchange = isExchangeType(returnItem.getReturnTypeCode());
            
            log.info("매핑 완료 - 입금ID: {}, 교환반품ID: {}, 출고가능: {}", 
                    request.getPaymentId(), request.getReturnItemId(), canShipExchange);
            
            return ShippingPaymentMappingDTO.MappingResult.builder()
                    .payment(convertToPaymentInfo(payment))
                    .returnItem(convertToCandidate(returnItem, 100))
                    .mappedDate(LocalDateTime.now())
                    .mappedBy(request.getMappedBy())
                    .notes(request.getNotes())
                    .canShipExchange(canShipExchange)
                    .resultMessage(canShipExchange ? "✅ 매핑 완료! 교환 상품 출고 가능합니다." : "✅ 매핑 완료!")
                    .build();
                    
        } catch (Exception e) {
            log.error("매핑 실행 실패: {}", e.getMessage(), e);
            throw new RuntimeException("매핑 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public boolean cancelMapping(Long paymentId, String canceledBy, String reason) {
        log.info("매핑 취소 시작 - 입금ID: {}, 취소자: {}", paymentId, canceledBy);
        
        try {
            ShippingPaymentRegister payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("입금 내역을 찾을 수 없습니다"));
            
            Long returnItemId = payment.getReturnItemId();
            
            // 입금 내역 초기화
            payment.setMappingStatus("PENDING");
            payment.setReturnItemId(null);
            payment.setNotes(reason);
            payment.setMappedDate(null); // 매핑 일시 초기화
            paymentRepository.save(payment);
            
            // 교환/반품 내역 초기화
            if (returnItemId != null) {
                ReturnItem returnItem = returnItemRepository.findById(returnItemId).orElse(null);
                if (returnItem != null) {
                    returnItem.setPaymentStatus("PENDING");
                    returnItem.setPaymentId(null);
                    // 배송비를 다시 "입금예정"으로 설정
                    returnItem.setShippingFee("입금예정");
                    returnItemRepository.save(returnItem);
                    
                    log.info("교환/반품 매핑 취소 완료 - ID: {}, PAYMENT_STATUS: PENDING, 배송비: 입금예정", 
                            returnItem.getId());
                }
            }
            
            log.info("매핑 취소 완료 - 입금ID: {}", paymentId);
            return true;
            
        } catch (Exception e) {
            log.error("매핑 취소 실패: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<ShippingPaymentMappingDTO.MappingResult> getRecentMappings(int limit) {
        log.info("최근 매핑 내역 {} 건 조회", limit);
        
        try {
            List<ShippingPaymentRegister> recentMappings = paymentRepository
                    .findByMappingStatus("MAPPED", 
                            org.springframework.data.domain.Sort.by(
                                    org.springframework.data.domain.Sort.Direction.DESC, "registerDate"))
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            
            return recentMappings.stream()
                    .map(payment -> {
                        ReturnItem returnItem = returnItemRepository.findById(payment.getReturnItemId()).orElse(null);
                        return ShippingPaymentMappingDTO.MappingResult.builder()
                                .payment(convertToPaymentInfo(payment))
                                .returnItem(returnItem != null ? convertToCandidate(returnItem, 100) : null)
                                .mappedDate(payment.getRegisterDate())
                                .mappedBy(payment.getRegistrar())
                                .notes(payment.getNotes())
                                .canShipExchange(returnItem != null && isExchangeType(returnItem.getReturnTypeCode()))
                                .build();
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("최근 매핑 내역 조회 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<ShippingPaymentMappingDTO.MappingResult> executeAutoMapping(String executedBy) {
        log.info("자동 매핑 실행 시작 - 실행자: {}", executedBy);
        
        List<ShippingPaymentMappingDTO.MappingResult> results = new ArrayList<>();
        
        try {
            List<ShippingPaymentRegister> pendingPayments = paymentRepository.findAllPendingPayments();
            
            for (ShippingPaymentRegister payment : pendingPayments) {
                List<ShippingPaymentMappingDTO.ReturnItemCandidate> candidates = 
                        findCandidatesForPayment(payment.getRegisterId());
                
                // 90점 이상의 고신뢰도 후보만 자동 매핑
                candidates.stream()
                        .filter(candidate -> candidate.getMatchScore() >= 90)
                        .findFirst()
                        .ifPresent(candidate -> {
                            try {
                                ShippingPaymentMappingDTO.MappingRequest request = 
                                        ShippingPaymentMappingDTO.MappingRequest.builder()
                                                .paymentId(payment.getRegisterId())
                                                .returnItemId(candidate.getReturnItemId())
                                                .mappedBy(executedBy)
                                                .notes("자동 매핑 (신뢰도: " + candidate.getMatchScore() + "%)")
                                                .build();
                                
                                ShippingPaymentMappingDTO.MappingResult result = executeMapping(request);
                                results.add(result);
                                
                            } catch (Exception e) {
                                log.error("자동 매핑 실패 - 입금ID: {}: {}", payment.getRegisterId(), e.getMessage());
                            }
                        });
            }
            
            log.info("자동 매핑 완료 - 총 {} 건 매핑됨", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("자동 매핑 실행 실패: {}", e.getMessage(), e);
            return results;
        }
    }
    
    @Override
    public List<ShippingPaymentMappingDTO.ReturnItemCandidate> searchReturnItemCandidates(
            String customerName, Integer amount, String keyword) {
        log.info("교환/반품 후보 검색 - 고객명: {}, 금액: {}, 키워드: {}", customerName, amount, keyword);
        
        try {
            List<ReturnItem> returnItems = returnItemRepository.findByPaymentStatus("PENDING");
            
            return returnItems.stream()
                    .filter(item -> {
                        boolean matches = true;
                        if (customerName != null && !customerName.trim().isEmpty()) {
                            matches &= item.getCustomerName().contains(customerName);
                        }
                        if (amount != null) {
                            matches &= item.getShippingFee() != null && 
                                      item.getShippingFee().equals(amount.toString());
                        }
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            matches &= item.getOrderNumber().contains(keyword) ||
                                      item.getOrderItemCode().contains(keyword) ||
                                      item.getSiteName().contains(keyword);
                        }
                        return matches;
                    })
                    .map(item -> convertToCandidate(item, 75)) // 검색 결과는 기본 75점
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("후보 검색 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 매칭 점수 계산 (0-100점)
     * 주요 매칭 조건: 브랜드(필수) + 고객명(40점) + 입금금액(30점) + 입금일자근접도(30점) + 연락처(보너스10점)
     */
    private int calculateMatchScore(ShippingPaymentRegister payment, ReturnItem returnItem) {
        int score = 0;
        log.info("🎯 매칭 점수 계산 시작");
        log.info("🎯 입금정보 - 고객명: {}, 금액: {}원, 입금일: {}, 연락처: {}", 
                payment.getCustomerName(), payment.getAmount(), payment.getPaymentDate(), payment.getCustomerPhone());
        log.info("🎯 교환/반품정보 - 고객명: {}, 배송비: {}원, 접수일: {}, 연락처: {}", 
                returnItem.getCustomerName(), returnItem.getShippingFee(), returnItem.getCsReceivedDate(), returnItem.getCustomerPhone());
        
        try {
            // 1. 고객명 매칭 (40점) - 가장 중요한 조건
            if (payment.getCustomerName() != null && returnItem.getCustomerName() != null) {
                if (payment.getCustomerName().equals(returnItem.getCustomerName())) {
                    score += 40;
                    log.info("🎯 고객명 완전일치: +40점 (총 {}점)", score);
                } else if (isNameSimilar(payment.getCustomerName(), returnItem.getCustomerName())) {
                    score += 30; // 유사한 이름 점수 상향
                    log.info("🎯 고객명 유사일치: +30점 (총 {}점)", score);
                } else if (payment.getCustomerName().contains(returnItem.getCustomerName()) || 
                          returnItem.getCustomerName().contains(payment.getCustomerName())) {
                    score += 20; // 부분 포함 시 점수 부여
                    log.info("🎯 고객명 부분일치: +20점 (총 {}점)", score);
                } else {
                    log.info("🎯 고객명 불일치: +0점 ('{}' vs '{}')", payment.getCustomerName(), returnItem.getCustomerName());
                }
            } else {
                log.info("🎯 고객명 null: +0점");
            }
            
            // 2. 입금 금액 매칭 (30점) - 두 번째로 중요한 조건
            if (payment.getAmount() != null && returnItem.getShippingFee() != null) {
                try {
                    // 배송비가 "입금예정" 등 문자열인 경우 처리
                    if ("입금예정".equals(returnItem.getShippingFee()) || 
                        "PENDING".equals(returnItem.getShippingFee()) ||
                        "입금불필요".equals(returnItem.getShippingFee()) ||
                        "NOT_REQUIRED".equals(returnItem.getShippingFee())) {
                        log.info("🎯 배송비 상태값: +0점 ('{}')", returnItem.getShippingFee());
                    } else {
                        int shippingFee = Integer.parseInt(returnItem.getShippingFee());
                        if (payment.getAmount().equals(shippingFee)) {
                            score += 30;
                            log.info("🎯 금액 완전일치: +30점 ({}원, 총 {}점)", payment.getAmount(), score);
                        } else {
                            // 금액 차이가 작으면 부분 점수 부여
                            int diff = Math.abs(payment.getAmount() - shippingFee);
                            if (diff <= 1000) {
                                score += 25; // 1000원 이하 차이
                                log.info("🎯 금액 거의일치: +25점 (차이: {}원, 총 {}점)", diff, score);
                            } else if (diff <= 5000) {
                                score += 15; // 5000원 이하 차이
                                log.info("🎯 금액 유사일치: +15점 (차이: {}원, 총 {}점)", diff, score);
                            } else {
                                log.info("🎯 금액 불일치: +0점 ({}원 vs {}원, 차이: {}원)", 
                                        payment.getAmount(), shippingFee, diff);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    log.info("🎯 배송비 숫자 변환 실패: +0점 ('{}')", returnItem.getShippingFee());
                }
            } else {
                log.info("🎯 금액 null: +0점");
            }
            
            // 3. 입금일자 근접도 (30점) - 세 번째로 중요한 조건
            if (payment.getPaymentDate() != null && returnItem.getCsReceivedDate() != null) {
                LocalDateTime paymentDateTime = payment.getPaymentDate();
                LocalDateTime csDateTime = returnItem.getCsReceivedDate().atStartOfDay();
                
                long daysDiff = Math.abs(ChronoUnit.DAYS.between(paymentDateTime.toLocalDate(), csDateTime.toLocalDate()));
                
                if (daysDiff == 0) {
                    score += 30; // 같은 날
                    log.info("🎯 입금일자 동일: +30점 (총 {}점)", score);
                } else if (daysDiff <= 1) {
                    score += 25; // 1일 차이
                    log.info("🎯 입금일자 1일차이: +25점 (총 {}점)", score);
                } else if (daysDiff <= 3) {
                    score += 20; // 3일 이내
                    log.info("🎯 입금일자 3일이내: +20점 ({}일 차이, 총 {}점)", daysDiff, score);
                } else if (daysDiff <= 7) {
                    score += 15; // 1주일 이내
                    log.info("🎯 입금일자 1주일이내: +15점 ({}일 차이, 총 {}점)", daysDiff, score);
                } else if (daysDiff <= 14) {
                    score += 10; // 2주일 이내
                    log.info("🎯 입금일자 2주일이내: +10점 ({}일 차이, 총 {}점)", daysDiff, score);
                } else {
                    log.info("🎯 입금일자 차이큼: +0점 ({}일 차이)", daysDiff);
                }
            } else {
                log.info("🎯 입금일자 null: +0점");
            }
            
            // 4. 연락처 부분 매칭 (보너스 10점) - 있으면 좋은 조건
            if (payment.getCustomerPhone() != null && returnItem.getCustomerPhone() != null) {
                if (payment.getCustomerPhone().equals(returnItem.getCustomerPhone())) {
                    score += 10;
                    log.info("🎯 연락처 완전일치: +10점 ({}, 총 {}점)", payment.getCustomerPhone(), score);
                } else if (hasPartialPhoneMatch(payment.getCustomerPhone(), returnItem.getCustomerPhone())) {
                    score += 5;
                    log.info("🎯 연락처 부분일치: +5점 ({} vs {}, 총 {}점)", 
                            payment.getCustomerPhone(), returnItem.getCustomerPhone(), score);
                } else {
                    log.info("🎯 연락처 불일치: +0점 ({} vs {})", 
                            payment.getCustomerPhone(), returnItem.getCustomerPhone());
                }
            } else {
                log.info("🎯 연락처 null: +0점 (입금: {}, 교환/반품: {})", 
                        payment.getCustomerPhone(), returnItem.getCustomerPhone());
            }
            
        } catch (Exception e) {
            log.warn("매칭 점수 계산 중 오류: {}", e.getMessage());
        }
        
        int finalScore = Math.min(100, score);
        log.info("🎯 최종 매칭 점수: {}점 (고객명+금액+일자+연락처)", finalScore);
        return finalScore;
    }
    
    /**
     * 이름 유사도 검사
     */
    private boolean isNameSimilar(String name1, String name2) {
        if (name1 == null || name2 == null) return false;
        if (name1.length() != name2.length()) return false;
        
        // 한 글자만 다른 경우 유사하다고 판단
        int diffCount = 0;
        for (int i = 0; i < name1.length(); i++) {
            if (name1.charAt(i) != name2.charAt(i)) {
                diffCount++;
            }
        }
        return diffCount <= 1;
    }
    
    /**
     * 연락처 부분 매칭 검사
     */
    private boolean hasPartialPhoneMatch(String phone1, String phone2) {
        if (phone1 == null || phone2 == null) return false;
        
        // 숫자만 추출
        String digits1 = phone1.replaceAll("[^0-9]", "");
        String digits2 = phone2.replaceAll("[^0-9]", "");
        
        if (digits1.length() < 4 || digits2.length() < 4) return false;
        
        // 뒤 4자리 비교
        String last4_1 = digits1.substring(Math.max(0, digits1.length() - 4));
        String last4_2 = digits2.substring(Math.max(0, digits2.length() - 4));
        
        return last4_1.equals(last4_2);
    }
    
    /**
     * 교환 유형 확인
     */
    private boolean isExchangeType(String returnTypeCode) {
        return "FULL_EXCHANGE".equals(returnTypeCode) || 
               "PARTIAL_EXCHANGE".equals(returnTypeCode) ||
               "EXCHANGE".equals(returnTypeCode);
    }
    
    /**
     * 브랜드를 영문에서 한글로 변환
     */
    private String convertBrandToKorean(String englishBrand) {
        if (englishBrand == null) {
            return "";
        }
        
        switch (englishBrand.toUpperCase()) {
            case "RENOMA":
                return "레노마";
            case "CORALIK":
                return "코랄리크";
            default:
                log.warn("알 수 없는 브랜드: {}", englishBrand);
                return englishBrand;
        }
    }
    
    /**
     * 브랜드 매칭 여부 확인 (사이트명에 한글 브랜드명이 포함되어 있는지 확인)
     */
    private boolean isBrandMatched(String koreanBrand, String siteName) {
        log.info("🔎 브랜드 매칭 상세 검사 시작");
        log.info("🔎 koreanBrand: '{}' (length: {})", koreanBrand, koreanBrand != null ? koreanBrand.length() : "null");
        log.info("🔎 siteName: '{}' (length: {})", siteName, siteName != null ? siteName.length() : "null");
        
        if (koreanBrand == null || siteName == null) {
            log.warn("🔎 브랜드 매칭 실패: null 값 발견 - koreanBrand: {}, siteName: {}", koreanBrand, siteName);
            return false;
        }
        
        // 사이트명에 한글 브랜드명이 포함되어 있는지 확인
        boolean matched = siteName.contains(koreanBrand);
        
        log.info("🔎 브랜드 매칭 결과: '{}' contains '{}' = {}", siteName, koreanBrand, matched);
        
        // 추가 디버깅: 각 문자 확인
        if (!matched) {
            log.info("🔎 매칭 실패 상세 분석:");
            log.info("🔎 siteName 문자들: {}", siteName.chars().mapToObj(c -> String.valueOf((char)c)).toArray());
            log.info("🔎 koreanBrand 문자들: {}", koreanBrand.chars().mapToObj(c -> String.valueOf((char)c)).toArray());
        }
        
        return matched;
    }
    
    /**
     * ShippingPaymentRegister를 PaymentInfo로 변환
     */
    private ShippingPaymentMappingDTO.PaymentInfo convertToPaymentInfo(ShippingPaymentRegister payment) {
        return ShippingPaymentMappingDTO.PaymentInfo.builder()
                .paymentId(payment.getRegisterId())  // REGISTER_ID 사용
                .customerName(payment.getCustomerName())  // CUSTOMER_NAME
                .customerPhone(payment.getCustomerPhone())  // CUSTOMER_PHONE
                .amount(payment.getAmount())  // AMOUNT
                .paymentDate(payment.getPaymentDate())  // PAYMENT_DATE
                .bankName(payment.getBankName())  // BANK_NAME
                .brand(payment.getBrand())  // BRAND
                .siteName(payment.getSiteName())  // SITE_NAME
                .mappingStatus(payment.getMappingStatus())  // MAPPING_STATUS
                .notes(payment.getNotes())  // NOTES
                .registerDate(payment.getRegisterDate())  // REGISTER_DATE (이미 LocalDateTime)
                .registeredBy(payment.getRegistrar())  // REGISTRAR
                .build();
    }
    
    /**
     * ReturnItem을 ReturnItemCandidate로 변환
     */
    private ShippingPaymentMappingDTO.ReturnItemCandidate convertToCandidate(ReturnItem returnItem, int matchScore) {
        String matchLevel;
        if (matchScore >= 90) matchLevel = "HIGH";
        else if (matchScore >= 70) matchLevel = "MEDIUM";
        else matchLevel = "LOW";
        
        // 매핑 상태 확인
        boolean isAlreadyMapped = returnItem.getPaymentId() != null;
        boolean isPendingPayment = "PENDING".equals(returnItem.getPaymentStatus());
        
        String matchReason = generateMatchReason(matchScore);
        
        // 매핑 상태에 따른 추가 정보
        if (isAlreadyMapped) {
            matchReason += " (이미 매핑됨)";
        } else if (!isPendingPayment) {
            matchReason += " (결제완료)";
        }
        
        return ShippingPaymentMappingDTO.ReturnItemCandidate.builder()
                .returnItemId(returnItem.getId())  // RETURN_ID 사용 (getId() 메서드가 RETURN_ID 반환)
                .orderNumber(returnItem.getOrderNumber())  // ORDER_NUMBER
                .orderItemCode(returnItem.getOrderItemCode())  // ORDER_ITEM_CODE
                .customerName(returnItem.getCustomerName())  // CUSTOMER_NAME
                .customerPhone(returnItem.getCustomerPhone())  // CUSTOMER_PHONE
                .siteName(returnItem.getSiteName())  // SITE_NAME
                .shippingFee(parseShippingFee(returnItem.getShippingFee()))  // SHIPPING_FEE
                .returnTypeCode(returnItem.getReturnTypeCode())  // RETURN_TYPE_CODE
                .returnReason(returnItem.getReturnReason())  // RETURN_REASON
                .csReceivedDate(returnItem.getCsReceivedDate())  // CS_RECEIVED_DATE
                .paymentStatus(returnItem.getPaymentStatus())  // PAYMENT_STATUS
                .matchScore(matchScore)
                .matchLevel(matchLevel)
                .matchReason(matchReason)
                .productColor(returnItem.getProductColor())  // PRODUCT_COLOR
                .productSize(returnItem.getProductSize())  // PRODUCT_SIZE
                .quantity(returnItem.getQuantity())  // QUANTITY
                .trackingNumber(returnItem.getTrackingNumber())  // TRACKING_NUMBER
                .canShipExchange(isExchangeType(returnItem.getReturnTypeCode()))
                .build();
    }
    
    /**
     * 매칭 근거 생성
     */
    private String generateMatchReason(int score) {
        if (score >= 90) return "고객명, 금액, 시간이 모두 일치";
        else if (score >= 70) return "고객명과 금액이 일치";
        else if (score >= 50) return "일부 정보가 일치";
        else return "유사한 정보 발견";
    }
    
    /**
     * 배송비 문자열을 정수로 변환
     */
    private Integer parseShippingFee(String shippingFee) {
        if (shippingFee == null || "PENDING".equals(shippingFee)) {
            return null;
        }
        try {
            return Integer.parseInt(shippingFee);
        } catch (NumberFormatException e) {
            return null;
        }
    }
} 