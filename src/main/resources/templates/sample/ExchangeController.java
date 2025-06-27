/**
 * 교환/반품 관리 컨트롤러
 * 사용 화면: exchange/list.html, exchange/form.html, exchange/view.html
 * 
 * 주요 기능:
 * - 교환/반품 목록 조회 (검색, 필터링, 페이징)
 * - 교환/반품 등록/수정/삭제
 * - 상태 관리 (접수대기 → 처리중 → 배송중 → 완료)
 * - 통계 정보 제공
 */
package com.wio.repairsystem.controller;

import com.wio.repairsystem.dto.ReturnItemDTO;
import com.wio.repairsystem.dto.ReturnItemSearchDTO;
import com.wio.repairsystem.dto.ReturnItemBulkDateUpdateDTO;
import com.wio.repairsystem.model.ReturnStatus;
import com.wio.repairsystem.model.ReturnType;
import com.wio.repairsystem.service.ReturnItemService;
import com.wio.repairsystem.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {

    private final ReturnItemService returnItemService;
    private final AttachmentService attachmentService;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadBaseDir;

    /**
     * 교환/반품 목록 페이지 (exchange/list.html)
     */
    @GetMapping({"/list", ""})
    public String list(
            Model model, 
            @ModelAttribute ReturnItemSearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        log.info("교환/반품 목록 조회 - 검색조건: {}", searchDTO);
        
        // 검색 조건이 있으면 검색, 없으면 전체 조회
        Page<ReturnItemDTO> returnItems;
        try {
            // 페이징 정보를 searchDTO에 설정
            if (searchDTO == null) {
                searchDTO = new ReturnItemSearchDTO();
            }
            searchDTO.setPage(page);
            searchDTO.setSize(size);
            searchDTO.setSortBy(sortBy);
            searchDTO.setSortDir(sortDir);
            
            if (searchDTO.hasSearchCondition()) {
                // 검색 조건이 있으면 검색 수행
                returnItems = returnItemService.search(searchDTO);
                log.info("검색 조건으로 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            } else {
                // 검색 조건이 없으면 전체 조회
                returnItems = returnItemService.findAll(page, size, sortBy, sortDir);
                log.info("전체 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            }
        } catch (Exception e) {
            log.error("목록 조회 실패, 빈 페이지 반환: {}", e.getMessage(), e);
            returnItems = Page.empty(PageRequest.of(page, size));
        }
        
        // 상태별 통계 (임시로 빈 Map 사용)
        Map<ReturnStatus, Long> statusCounts = new java.util.HashMap<>();
        Map<String, Long> siteCounts = new java.util.HashMap<>();
        Map<String, Long> typeCounts = new java.util.HashMap<>();
        Map<String, Long> reasonCounts = new java.util.HashMap<>();
        Map<String, Object> amountSummary = new java.util.HashMap<>();
        Map<String, Long> brandCounts = new java.util.HashMap<>();
        
        try {
            statusCounts = returnItemService.getStatusCounts();
            
            // 사이트별 통계 - 상위 6개만 (내림차순 정렬)
            Map<String, Long> allSiteCounts = returnItemService.getSiteCounts();
            siteCounts = allSiteCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(6)
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));
            
            typeCounts = returnItemService.getTypeCounts();
            
            // 사유별 통계 - 상위 5개만 (내림차순 정렬)
            Map<String, Long> allReasonCounts = returnItemService.getReasonCounts();
            reasonCounts = allReasonCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));
            
            amountSummary = returnItemService.getAmountSummary();
            brandCounts = returnItemService.getBrandCounts();
        } catch (Exception e) {
            log.error("통계 조회 실패: {}", e.getMessage());
            // 빈 맵으로 초기화
            for (ReturnStatus status : ReturnStatus.values()) {
                statusCounts.put(status, 0L);
            }
        }
        
        // 금일 등록 건수 계산
        Long todayCount = 0L;
        try {
            todayCount = returnItemService.getTodayCount();
        } catch (Exception e) {
            log.error("금일 등록 건수 조회 실패: {}", e.getMessage());
            todayCount = 0L;
        }
        
        // 🎯 상단 카드 대시보드 통계 계산
        Map<String, Long> cardStats = new java.util.HashMap<>();
        try {
            // ① 회수완료 카드
            cardStats.put("collectionCompleted", returnItemService.getCollectionCompletedCount());
            cardStats.put("collectionPending", returnItemService.getCollectionPendingCount());
            
            // ② 물류확인 카드
            cardStats.put("logisticsConfirmed", returnItemService.getLogisticsConfirmedCount());
            cardStats.put("logisticsPending", returnItemService.getLogisticsPendingCount());
            
            // ③ 교환 출고일자 카드
            cardStats.put("exchangeShipped", returnItemService.getExchangeShippedCount());
            cardStats.put("exchangeNotShipped", returnItemService.getExchangeNotShippedCount());
            
            // ④ 반품 환불일자 카드
            cardStats.put("returnRefunded", returnItemService.getReturnRefundedCount());
            cardStats.put("returnNotRefunded", returnItemService.getReturnNotRefundedCount());
            
            // ⑤ 배송비입금 카드
            cardStats.put("paymentCompleted", returnItemService.getPaymentCompletedCount());
            cardStats.put("paymentPending", returnItemService.getPaymentPendingCount());
            
            // ⑥ 완료 상태 카드 통계 추가
            cardStats.put("completedCount", returnItemService.getCompletedCount());
            cardStats.put("incompletedCount", returnItemService.getIncompletedCount());
            
        } catch (Exception e) {
            log.error("상단 카드 통계 조회 실패: {}", e.getMessage());
            // 기본값으로 초기화
            cardStats.put("collectionCompleted", 0L);
            cardStats.put("collectionPending", 0L);
            cardStats.put("logisticsConfirmed", 0L);
            cardStats.put("logisticsPending", 0L);
            cardStats.put("exchangeShipped", 0L);
            cardStats.put("exchangeNotShipped", 0L);
            cardStats.put("returnRefunded", 0L);
            cardStats.put("returnNotRefunded", 0L);
            cardStats.put("paymentCompleted", 0L);
            cardStats.put("paymentPending", 0L);
            cardStats.put("completedCount", 0L);
            cardStats.put("incompletedCount", 0L);
        }
        
        // searchDTO가 null이면 빈 객체 생성
        if (searchDTO == null) {
            searchDTO = new ReturnItemSearchDTO();
        }
        
        model.addAttribute("returnItems", returnItems);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("siteCounts", siteCounts);
        model.addAttribute("typeCounts", typeCounts);
        model.addAttribute("reasonCounts", reasonCounts);
        model.addAttribute("amountSummary", amountSummary);
        model.addAttribute("brandCounts", brandCounts);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("cardStats", cardStats); // 🎯 상단 카드 통계 추가
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", returnItems.getTotalPages());
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("ASC") ? "DESC" : "ASC");
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());
        
        return "exchange/list";
    }

    /**
     * 교환/반품 등록 폼 페이지 (exchange/form.html)
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        log.info("교환/반품 등록 폼 페이지 접근");
        
        ReturnItemDTO returnItem = new ReturnItemDTO();
        returnItem.setPaymentStatus("NOT_REQUIRED"); // 기본값 설정
        
        model.addAttribute("returnItem", returnItem);
        model.addAttribute("returnStatuses", ReturnStatus.values());
        model.addAttribute("returnTypes", ReturnType.values());
        model.addAttribute("isEdit", false);
        
        return "exchange/form";
    }

    /**
     * 교환/반품 수정 폼 페이지 (exchange/form.html)
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        log.info("교환/반품 수정 폼 페이지 접근 - ID: {}", id);
        
        try {
            ReturnItemDTO returnItem = returnItemService.getReturnItemById(id);
            
            // paymentStatus가 null인 경우 기본값 설정
            if (returnItem.getPaymentStatus() == null) {
                returnItem.setPaymentStatus("NOT_REQUIRED");
            }
            
            // 🔥 대표님 긴급: 모든 날짜 필드 디버깅 로그
            log.info("🔥🔥🔥 대표님 모든 날짜 필드 확인!");
            log.info("🔍 조회된 ReturnItem ID: {}", returnItem.getId());
            log.info("🔍 CS 접수일: {}", returnItem.getCsReceivedDate());
            log.info("🔍 주문일: {}", returnItem.getOrderDate());
            log.info("🔍 회수완료일: {}", returnItem.getCollectionCompletedDate());
            log.info("🔍 물류확인일: {}", returnItem.getLogisticsConfirmedDate());
            log.info("🔍 출고일자: {}", returnItem.getShippingDate());
            log.info("🔍 환불일자: {}", returnItem.getRefundDate());
            log.info("🔍 전체 데이터: {}", returnItem);
            
            model.addAttribute("returnItem", returnItem);
            model.addAttribute("returnStatuses", ReturnStatus.values());
            model.addAttribute("returnTypes", ReturnType.values());
            model.addAttribute("isEdit", true);
            
            return "exchange/form";
        } catch (Exception e) {
            log.error("교환/반품 수정 폼 로드 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return "redirect:/exchange/list";
        }
    }

    /**
     * 교환/반품 상세 보기 페이지 (exchange/view.html)
     */
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        log.info("교환/반품 상세 보기 - ID: {}", id);
        
        try {
            ReturnItemDTO returnItem = returnItemService.getReturnItemById(id);
            model.addAttribute("returnItem", returnItem);
            model.addAttribute("returnStatuses", ReturnStatus.values());
            model.addAttribute("returnTypes", ReturnType.values());
            
            return "exchange/view";
        } catch (Exception e) {
            log.error("교환/반품 상세 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return "redirect:/exchange/list";
        }
    }

    /**
     * 교환/반품 저장 (등록/수정)
     */
    @PostMapping("/save")
    public String save(@ModelAttribute ReturnItemDTO returnItemDTO, 
                       @RequestParam Map<String, String> allParams,
                       @RequestParam(value = "attachmentPhoto", required = false) MultipartFile attachmentPhoto,
                       @RequestParam(value = "attachmentImageData", required = false) String attachmentImageData,
                       RedirectAttributes redirectAttributes) {
        log.info("교환/반품 저장 - 데이터: {}", returnItemDTO);
        log.info("모든 파라미터: {}", allParams);
        log.info("첨부 파일: {}", attachmentPhoto != null ? attachmentPhoto.getOriginalFilename() : "없음");
        log.info("붙여넣기 이미지 데이터: {}", attachmentImageData != null ? "있음" : "없음");
        
        // 🔥 대표님 요청: CS 접수일 처리 로직 추가
        if (returnItemDTO.getCsReceivedDate() == null && returnItemDTO.getId() == null) {
            // 신규 등록이고 CS 접수일이 없으면 오늘 날짜로 설정
            returnItemDTO.setCsReceivedDate(java.time.LocalDate.now());
            log.info("🔥 CS 접수일 자동 설정 (신규): {}", returnItemDTO.getCsReceivedDate());
        }
        log.info("🔥 CS 접수일 최종 값: {}", returnItemDTO.getCsReceivedDate());

        // 필수 필드 수동 설정 (디버깅용)
        if (returnItemDTO.getReturnTypeCode() == null || returnItemDTO.getReturnTypeCode().isEmpty()) {
            String typeCode = allParams.get("returnTypeCode");
            log.info("returnTypeCode 수동 설정: {}", typeCode);
            returnItemDTO.setReturnTypeCode(typeCode);
        }
        
        if (returnItemDTO.getReturnStatusCode() == null || returnItemDTO.getReturnStatusCode().isEmpty()) {
            String statusCode = allParams.get("returnStatusCode");
            log.info("returnStatusCode 수동 설정: {}", statusCode);
            returnItemDTO.setReturnStatusCode(statusCode != null ? statusCode : "PENDING");
        }
        
        // 필수 필드 검증
        if (returnItemDTO.getReturnTypeCode() == null || returnItemDTO.getReturnTypeCode().isEmpty()) {
            log.error("RETURN_TYPE_CODE가 null입니다!");
            redirectAttributes.addFlashAttribute("error", "유형을 선택해주세요.");
            return "redirect:/exchange/create";
        }
        
        // 현재 로그인 사용자 정보 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        log.info("현재 로그인 사용자: {}", currentUsername);
        
        if (returnItemDTO.getId() == null) {
            // 신규 등록시에만 등록자 설정
            returnItemDTO.setCreatedBy(currentUsername);
        }
        // 수정자는 항상 현재 사용자로 설정
        returnItemDTO.setUpdatedBy(currentUsername);
        
        // 🔥 대표님 요청: 날짜 필드 변경 감지 및 수정자 기록 (수정 모드일 때만)
        if (returnItemDTO.getId() != null) {
            try {
                ReturnItemDTO existingItem = returnItemService.getReturnItemById(returnItemDTO.getId());
                LocalDateTime now = LocalDateTime.now();
                
                // 회수완료일 변경 감지
                if (!Objects.equals(existingItem.getCollectionCompletedDate(), returnItemDTO.getCollectionCompletedDate())) {
                    returnItemDTO.setCollectionUpdatedBy(currentUsername);
                    returnItemDTO.setCollectionUpdatedDate(now);
                    log.info("🔥 회수완료일 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 물류확인일 변경 감지
                if (!Objects.equals(existingItem.getLogisticsConfirmedDate(), returnItemDTO.getLogisticsConfirmedDate())) {
                    returnItemDTO.setLogisticsUpdatedBy(currentUsername);
                    returnItemDTO.setLogisticsUpdatedDate(now);
                    log.info("🔥 물류확인일 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 출고일자 변경 감지
                if (!Objects.equals(existingItem.getShippingDate(), returnItemDTO.getShippingDate())) {
                    returnItemDTO.setShippingUpdatedBy(currentUsername);
                    returnItemDTO.setShippingUpdatedDate(now);
                    log.info("🔥 출고일자 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 환불일자 변경 감지
                if (!Objects.equals(existingItem.getRefundDate(), returnItemDTO.getRefundDate())) {
                    returnItemDTO.setRefundUpdatedBy(currentUsername);
                    returnItemDTO.setRefundUpdatedDate(now);
                    log.info("🔥 환불일자 변경 감지 - 수정자: {}, 시간: {}", currentUsername, now);
                }
                
                // 완료 상태 변경 시 PROCESSOR 컬럼에 처리자 기록
                if (!Objects.equals(existingItem.getIsCompleted(), returnItemDTO.getIsCompleted()) && 
                    returnItemDTO.getIsCompleted() != null && returnItemDTO.getIsCompleted() == 1) {
                    returnItemDTO.setProcessor(currentUsername);
                    log.info("🔥 완료 상태 변경 감지 - 처리자: {}", currentUsername);
                }
                
            } catch (Exception e) {
                log.error("기존 데이터 조회 실패: {}", e.getMessage());
            }
        }
        
        // 이미지 파일 처리
        try {
            String imageUrl = processImageUpload(attachmentPhoto, attachmentImageData);
            if (imageUrl != null) {
                returnItemDTO.setDefectPhotoUrl(imageUrl);
                log.info("이미지 저장 완료: {}", imageUrl);
            }
        } catch (Exception e) {
            log.error("이미지 저장 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
            return returnItemDTO.getId() != null ? "redirect:/exchange/edit/" + returnItemDTO.getId() : "redirect:/exchange/create";
        }
        
        log.info("최종 저장 데이터: {}", returnItemDTO);
        
        try {
            if (returnItemDTO.getId() != null) {
                // 수정
                returnItemService.updateReturnItem(returnItemDTO);
                redirectAttributes.addFlashAttribute("success", "교환/반품 정보가 수정되었습니다.");
                return "redirect:/exchange/list?success=update";
            } else {
                // 등록
                ReturnItemDTO savedItem = returnItemService.createReturnItem(returnItemDTO);
                redirectAttributes.addFlashAttribute("success", "교환/반품이 등록되었습니다.");
                return "redirect:/exchange/list?success=create";
            }
        } catch (Exception e) {
            log.error("교환/반품 저장 실패 - 오류: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다: " + e.getMessage());
            
            if (returnItemDTO.getId() != null) {
                return "redirect:/exchange/edit/" + returnItemDTO.getId() + "?error=true";
            } else {
                return "redirect:/exchange/create?error=true";
            }
        }
    }

    /**
     * 교환/반품 삭제
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("교환/반품 삭제 - ID: {}", id);
        
        try {
            returnItemService.deleteReturnItem(id);
            redirectAttributes.addFlashAttribute("message", "교환/반품이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("교환/반품 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/exchange/list";
    }

    /**
     * 상태별 목록 조회
     */
    @GetMapping("/status/{status}")
    public String listByStatus(
            @PathVariable String status,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("상태별 교환/반품 목록 조회 - 상태: {}", status);
        
        try {
            ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
            Page<ReturnItemDTO> returnItems = returnItemService.findByStatus(returnStatus, page, size);
            Map<ReturnStatus, Long> statusCounts = returnItemService.getStatusCounts();
            
            model.addAttribute("returnItems", returnItems);
            model.addAttribute("statusCounts", statusCounts);
            model.addAttribute("currentStatus", returnStatus);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", returnItems.getTotalPages());
            model.addAttribute("totalItems", returnItems.getTotalElements());
            model.addAttribute("returnStatuses", ReturnStatus.values());
            model.addAttribute("returnTypes", ReturnType.values());
            
            return "exchange/list";
        } catch (Exception e) {
            log.error("상태별 목록 조회 실패 - 상태: {}, 오류: {}", status, e.getMessage());
            return "redirect:/exchange/list";
        }
    }

    // ========== AJAX API 엔드포인트 ==========

    /**
     * 상태 변경 (AJAX)
     */
    @PostMapping("/api/{id}/status")
    @ResponseBody
    public ResponseEntity<ReturnItemDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("교환/반품 상태 변경 - ID: {}, 상태: {}", id, status);
        
        try {
            ReturnStatus returnStatus = ReturnStatus.valueOf(status.toUpperCase());
            ReturnItemDTO updatedItem = returnItemService.updateStatus(id, returnStatus);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            log.error("상태 변경 실패 - ID: {}, 상태: {}, 오류: {}", id, status, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 삭제 (AJAX)
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAjax(@PathVariable Long id) {
        log.info("교환/반품 삭제 (AJAX) - ID: {}", id);
        
        try {
            returnItemService.deleteReturnItem(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("삭제 실패 (AJAX) - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 상태별 통계 조회 (AJAX)
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<ReturnStatus, Long>> getStats() {
        try {
            Map<ReturnStatus, Long> statusCounts = returnItemService.getStatusCounts();
            return ResponseEntity.ok(statusCounts);
        } catch (Exception e) {
            log.error("통계 조회 실패 - 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 미처리 항목 조회 (AJAX)
     */
    @GetMapping("/api/unprocessed")
    @ResponseBody
    public ResponseEntity<List<ReturnItemDTO>> getUnprocessedItems() {
        try {
            List<ReturnItemDTO> unprocessedItems = returnItemService.findUnprocessed();
            return ResponseEntity.ok(unprocessedItems);
        } catch (Exception e) {
            log.error("미처리 항목 조회 실패 - 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 이미지 업로드 처리 (파일 업로드 또는 붙여넣기)
     */
    private String processImageUpload(MultipartFile attachmentPhoto, String attachmentImageData) throws IOException {
        // 1. 파일 업로드가 있는 경우
        if (attachmentPhoto != null && !attachmentPhoto.isEmpty()) {
            log.info("파일 업로드 처리: {}", attachmentPhoto.getOriginalFilename());
            return saveUploadedFile(attachmentPhoto);
        }
        
        // 2. 붙여넣기 이미지 데이터가 있는 경우
        if (attachmentImageData != null && !attachmentImageData.trim().isEmpty()) {
            log.info("붙여넣기 이미지 처리");
            return savePastedImage(attachmentImageData);
        }
        
        return null;
    }
    
    /**
     * 업로드된 파일 저장
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        // 업로드 디렉토리 준비
        Path uploadsPath = Paths.get(uploadBaseDir);
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        
        // images 서브디렉토리 생성
        Path imagesDir = uploadsPath.resolve("images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        
        // 파일명 생성 (중복 방지)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String uniqueFilename = UUID.randomUUID().toString() + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                            fileExtension;
        
        // 파일 저장
        Path filePath = imagesDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("파일 저장 완료: {}", filePath);
        
        // 웹에서 접근 가능한 상대 경로 반환
        return "images/" + uniqueFilename;
    }
    
    /**
     * 붙여넣기 이미지 저장
     */
    private String savePastedImage(String base64Data) throws IOException {
        // Base64 데이터에서 타입과 실제 데이터 분리
        String[] parts = base64Data.split(",");
        String imageData = parts.length > 1 ? parts[1] : parts[0];
        
        // 파일 확장자 결정
        String fileExtension = ".png"; // 기본값
        
        if (parts.length > 1 && parts[0].contains("image/")) {
            String mimeInfo = parts[0];
            if (mimeInfo.contains("image/jpeg")) {
                fileExtension = ".jpg";
            } else if (mimeInfo.contains("image/png")) {
                fileExtension = ".png";
            } else if (mimeInfo.contains("image/gif")) {
                fileExtension = ".gif";
            }
        }
        
        // 업로드 디렉토리 준비
        Path uploadsPath = Paths.get(uploadBaseDir);
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        
        // images 서브디렉토리 생성
        Path imagesDir = uploadsPath.resolve("images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        
        // 파일명 생성 (중복 방지)
        String uniqueFilename = "pasted_" + UUID.randomUUID().toString() + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                            fileExtension;
        
        // Base64 디코딩하여 파일 저장
        Path filePath = imagesDir.resolve(uniqueFilename);
        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        Files.write(filePath, imageBytes);
        
        log.info("붙여넣기 이미지 저장 완료: {}", filePath);
        
        // 웹에서 접근 가능한 상대 경로 반환
        return "images/" + uniqueFilename;
    }

    /**
     * 카드 필터링 API - 조건에 맞는 데이터를 서버에서 조회
     */
    @GetMapping("/filter/{filterType}")
    public String filterByCard(
            @PathVariable String filterType,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        log.info("카드 필터링 요청 - filterType: {}", filterType);
        
        Page<ReturnItemDTO> returnItems;
        ReturnItemSearchDTO searchDTO = new ReturnItemSearchDTO();
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDir(sortDir);
        
        try {
            // 필터 타입에 따라 조건 설정
            switch (filterType) {
                case "collection-completed":
                    returnItems = returnItemService.findByCollectionCompleted(searchDTO);
                    break;
                case "collection-pending":
                    returnItems = returnItemService.findByCollectionPending(searchDTO);
                    break;
                case "logistics-confirmed":
                    returnItems = returnItemService.findByLogisticsConfirmed(searchDTO);
                    break;
                case "logistics-pending":
                    returnItems = returnItemService.findByLogisticsPending(searchDTO);
                    break;
                case "shipping-completed":
                    returnItems = returnItemService.findByShippingCompleted(searchDTO);
                    break;
                case "shipping-pending":
                    returnItems = returnItemService.findByShippingPending(searchDTO);
                    break;
                case "refund-completed":
                    returnItems = returnItemService.findByRefundCompleted(searchDTO);
                    break;
                case "refund-pending":
                    returnItems = returnItemService.findByRefundPending(searchDTO);
                    break;
                case "payment-completed":
                    returnItems = returnItemService.findByPaymentCompleted(searchDTO);
                    break;
                case "payment-pending":
                    returnItems = returnItemService.findByPaymentPending(searchDTO);
                    break;
                case "completed":
                    log.info("🎯 완료 상태 필터링 요청");
                    returnItems = returnItemService.findByCompleted(searchDTO);
                    log.info("✅ 완료 상태 필터링 결과: {} 건", returnItems.getTotalElements());
                    break;
                case "incompleted":
                    log.info("🎯 미완료 상태 필터링 요청");
                    returnItems = returnItemService.findByIncompleted(searchDTO);
                    log.info("❌ 미완료 상태 필터링 결과: {} 건", returnItems.getTotalElements());
                    break;
                default:
                    log.warn("알 수 없는 필터 타입: {}", filterType);
                    returnItems = returnItemService.findAll(page, size, sortBy, sortDir);
                    break;
            }
            
            log.info("필터링 조회 완료 - 결과 수: {}", returnItems.getTotalElements());
            
        } catch (Exception e) {
            log.error("필터링 조회 실패: {}", e.getMessage(), e);
            returnItems = Page.empty(PageRequest.of(page, size));
        }
        
        // 기본 통계 정보도 함께 제공 (기존 list 메서드와 동일)
        Map<ReturnStatus, Long> statusCounts = new java.util.HashMap<>();
        Map<String, Long> siteCounts = new java.util.HashMap<>();
        Map<String, Long> typeCounts = new java.util.HashMap<>();
        Map<String, Long> reasonCounts = new java.util.HashMap<>();
        Map<String, Object> amountSummary = new java.util.HashMap<>();
        Map<String, Long> brandCounts = new java.util.HashMap<>();
        
        try {
            statusCounts = returnItemService.getStatusCounts();
            siteCounts = returnItemService.getSiteCounts();
            typeCounts = returnItemService.getTypeCounts();
            reasonCounts = returnItemService.getReasonCounts();
            amountSummary = returnItemService.getAmountSummary();
            brandCounts = returnItemService.getBrandCounts();
        } catch (Exception e) {
            log.error("통계 조회 실패: {}", e.getMessage());
        }
        
        // 카드 통계 정보
        Map<String, Long> cardStats = new java.util.HashMap<>();
        try {
            cardStats.put("collectionCompleted", returnItemService.getCollectionCompletedCount());
            cardStats.put("collectionPending", returnItemService.getCollectionPendingCount());
            cardStats.put("logisticsConfirmed", returnItemService.getLogisticsConfirmedCount());
            cardStats.put("logisticsPending", returnItemService.getLogisticsPendingCount());
            cardStats.put("exchangeShipped", returnItemService.getExchangeShippedCount());
            cardStats.put("exchangeNotShipped", returnItemService.getExchangeNotShippedCount());
            cardStats.put("returnRefunded", returnItemService.getReturnRefundedCount());
            cardStats.put("returnNotRefunded", returnItemService.getReturnNotRefundedCount());
            cardStats.put("paymentCompleted", returnItemService.getPaymentCompletedCount());
            cardStats.put("paymentPending", returnItemService.getPaymentPendingCount());
            cardStats.put("completedCount", returnItemService.getCompletedCount());
            cardStats.put("incompletedCount", returnItemService.getIncompletedCount());
        } catch (Exception e) {
            log.error("카드 통계 조회 실패: {}", e.getMessage());
        }
        
        Long todayCount = 0L;
        try {
            todayCount = returnItemService.getTodayCount();
        } catch (Exception e) {
            log.error("금일 등록 건수 조회 실패: {}", e.getMessage());
        }
        
        // 모델에 데이터 추가
        model.addAttribute("returnItems", returnItems);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("siteCounts", siteCounts);
        model.addAttribute("typeCounts", typeCounts);
        model.addAttribute("reasonCounts", reasonCounts);
        model.addAttribute("amountSummary", amountSummary);
        model.addAttribute("brandCounts", brandCounts);
        model.addAttribute("cardStats", cardStats);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("totalItems", returnItems.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("currentFilter", filterType); // 현재 적용된 필터
        
        return "exchange/list";
    }

    /**
     * 🚀 성능 최적화된 목록 조회 (만 개 데이터 대응)
     */
    @GetMapping("/optimized")
    public String listOptimized(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               Model model) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 성능 최적화된 검색 사용
            Page<ReturnItemDTO> returnItems = returnItemService.searchOptimized(keyword, startDate, endDate, page, size);
            
            // 카드 통계 (캐싱 고려)
            Map<String, Long> cardStats = getCardStatistics();
            
            model.addAttribute("returnItems", returnItems);
            model.addAttribute("cardStats", cardStats);
            model.addAttribute("keyword", keyword);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            
            long endTime = System.currentTimeMillis();
            model.addAttribute("queryTime", endTime - startTime);
            
            return "exchange/list";
            
        } catch (Exception e) {
            log.error("최적화된 목록 조회 중 오류 발생", e);
            model.addAttribute("error", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            return "exchange/list";
        }
    }

    /**
     * 🚀 성능 통계 API (성능 모니터링용)
     */
    @GetMapping("/api/performance-stats")
    @ResponseBody
    public Map<String, Object> getPerformanceStats() {
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 기본 통계
            stats.put("totalCount", returnItemService.getTodayCount());
            
            // 카드 통계 (개별 측정)
            long cardStartTime = System.currentTimeMillis();
            Map<String, Long> cardStats = getCardStatistics();
            long cardEndTime = System.currentTimeMillis();
            
            stats.put("cardStats", cardStats);
            stats.put("cardQueryTime", cardEndTime - cardStartTime);
            
            long endTime = System.currentTimeMillis();
            stats.put("totalQueryTime", endTime - startTime);
            stats.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            stats.put("error", e.getMessage());
            stats.put("timestamp", LocalDateTime.now());
        }
        
        return stats;
    }

    /**
     * 🚀 성능 최적화된 검색 API
     */
    @GetMapping("/api/search-optimized")
    @ResponseBody
    public Map<String, Object> searchOptimizedApi(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            Page<ReturnItemDTO> returnItems = returnItemService.searchOptimized(keyword, startDate, endDate, page, size);
            
            result.put("data", returnItems.getContent());
            result.put("totalElements", returnItems.getTotalElements());
            result.put("totalPages", returnItems.getTotalPages());
            result.put("currentPage", page);
            result.put("pageSize", size);
            
            long endTime = System.currentTimeMillis();
            result.put("queryTime", endTime - startTime);
            result.put("optimized", true);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("optimized", false);
        }
        
        return result;
    }

    /**
     * 캐시된 카드 통계 조회 (성능 개선)
     */
    private Map<String, Long> getCardStatistics() {
        // TODO: 실제 운영에서는 Redis 등을 활용한 캐싱 고려
        Map<String, Long> cardStats = new HashMap<>();
        
        try {
            cardStats.put("collectionCompleted", returnItemService.getCollectionCompletedCount());
            cardStats.put("collectionPending", returnItemService.getCollectionPendingCount());
            cardStats.put("logisticsConfirmed", returnItemService.getLogisticsConfirmedCount());
            cardStats.put("logisticsPending", returnItemService.getLogisticsPendingCount());
            cardStats.put("exchangeShipped", returnItemService.getExchangeShippedCount());
            cardStats.put("exchangeNotShipped", returnItemService.getExchangeNotShippedCount());
            cardStats.put("returnRefunded", returnItemService.getReturnRefundedCount());
            cardStats.put("returnNotRefunded", returnItemService.getReturnNotRefundedCount());
            cardStats.put("paymentCompleted", returnItemService.getPaymentCompletedCount());
            cardStats.put("paymentPending", returnItemService.getPaymentPendingCount());
            cardStats.put("completedCount", returnItemService.getCompletedCount());
            cardStats.put("incompletedCount", returnItemService.getIncompletedCount());
        } catch (Exception e) {
            log.error("카드 통계 조회 중 오류", e);
            // 오류 시 기본값 설정
            cardStats.put("error", 1L);
        }
        
        return cardStats;
    }
    
    /**
     * 🎯 대표님 요청: 리스트에서 직접 날짜 일괄 수정 API
     */
    @PostMapping("/api/bulk-update-dates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkUpdateDates(
            @RequestBody List<ReturnItemBulkDateUpdateDTO> updates) {
        
        log.info("일괄 날짜 업데이트 요청 - 대상 건수: {}", updates.size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 유효성 검사
            if (updates == null || updates.isEmpty()) {
                response.put("success", false);
                response.put("message", "업데이트할 데이터가 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 현재 로그인 사용자 정보 가져오기 (기존 등록 방식과 동일)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String updatedBy = authentication.getName();
            log.info("날짜 수정자: {}", updatedBy);
            
            // 모든 업데이트 항목에 수정자 정보 추가
            for (ReturnItemBulkDateUpdateDTO update : updates) {
                update.setUpdatedBy(updatedBy);
            }
            
            // 일괄 업데이트 실행
            int updatedCount = returnItemService.bulkUpdateDates(updates);
            
            response.put("success", true);
            response.put("message", "성공적으로 업데이트되었습니다.");
            response.put("updatedCount", updatedCount);
            response.put("totalCount", updates.size());
            response.put("updatedBy", updatedBy);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("일괄 날짜 업데이트 성공 - 업데이트된 건수: {}/{}, 수정자: {}", updatedCount, updates.size(), updatedBy);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일괄 날짜 업데이트 실패", e);
            
            response.put("success", false);
            response.put("message", "업데이트 중 오류가 발생했습니다: " + e.getMessage());
            response.put("updatedCount", 0);
            response.put("totalCount", updates.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 🎯 대표님 요청: 완료 상태 업데이트 API
     */
    @PostMapping("/api/update-completion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCompletionStatus(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long id = Long.valueOf(request.get("id").toString());
            Boolean isCompleted = (Boolean) request.get("isCompleted");
            
            log.info("완료 상태 업데이트 요청 - ID: {}, 완료: {}", id, isCompleted);
            
            // 완료 상태 업데이트 실행
            boolean updated = returnItemService.updateCompletionStatus(id, isCompleted);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "완료 상태가 성공적으로 업데이트되었습니다.");
                log.info("완료 상태 업데이트 성공 - ID: {}, 완료: {}", id, isCompleted);
            } else {
                response.put("success", false);
                response.put("message", "해당 항목을 찾을 수 없습니다.");
                log.warn("완료 상태 업데이트 실패 - ID: {} (항목을 찾을 수 없음)", id);
            }
            
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("완료 상태 업데이트 실패", e);
            
            response.put("success", false);
            response.put("message", "완료 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 🎯 대표님 요청: 실시간 카드 통계 업데이트 API
     */
    @GetMapping("/api/card-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCardStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("카드 통계 요청 - keyword: {}, startDate: {}, endDate: {}", keyword, startDate, endDate);
            
            // 10개 카드 통계 데이터 계산 (기존 로직 재사용)
            Map<String, Object> cardStats = new HashMap<>();
            
            cardStats.put("todayCount", returnItemService.getTodayCount());
            cardStats.put("collectionCompletedCount", returnItemService.getCollectionCompletedCount());
            cardStats.put("collectionPendingCount", returnItemService.getCollectionPendingCount());
            cardStats.put("logisticsConfirmedCount", returnItemService.getLogisticsConfirmedCount());
            cardStats.put("logisticsPendingCount", returnItemService.getLogisticsPendingCount());
            cardStats.put("exchangeShippedCount", returnItemService.getExchangeShippedCount());
            cardStats.put("exchangeNotShippedCount", returnItemService.getExchangeNotShippedCount());
            cardStats.put("returnRefundedCount", returnItemService.getReturnRefundedCount());
            cardStats.put("returnNotRefundedCount", returnItemService.getReturnNotRefundedCount());
            cardStats.put("paymentCompletedCount", returnItemService.getPaymentCompletedCount());
            cardStats.put("paymentPendingCount", returnItemService.getPaymentPendingCount());
            
            // ⑥ 완료 상태 카드 통계 추가
            cardStats.put("completedCount", returnItemService.getCompletedCount());
            cardStats.put("incompletedCount", returnItemService.getIncompletedCount());
            
            log.info("카드 통계 응답 데이터: {}", cardStats);
            
            return ResponseEntity.ok(cardStats);
            
        } catch (Exception e) {
            log.error("카드 통계 조회 오류", e);
            response.put("error", "카드 통계 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }
} 