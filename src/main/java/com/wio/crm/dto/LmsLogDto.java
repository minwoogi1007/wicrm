package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LMS 문자 발송 로그 DTO
 * LMS_LOG 테이블과 매핑
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LmsLogDto {
    
    private Long id;                    // 기본키 (자동증가)
    private String clid;                // 수신자 전화번호
    private String subject;             // LMS 제목 (최대 40자)
    private String message;             // LMS 메시지 내용 (최대 2000자)
    private LocalDateTime sendDate;     // 실제 전송 일시
    private String status;              // 발송 상태 (PENDING/SENT/FAILED/ERROR)
    private String errorMessage;        // 오류 메시지
    private LocalDateTime createdDate;  // 레코드 생성 일시
    private Integer retryCount;         // 재전송 시도 횟수
    
    // 조인용 추가 필드들
    private boolean hasFollowUp;        // 후속 통화 여부
    private Integer followUpCount;      // 후속 통화 횟수
    private LocalDateTime lastCallDate; // 마지막 통화 일시
    private String lastCallResult;      // 마지막 통화 결과
    
    /**
     * 상태 텍스트 반환
     */
    public String getStatusText() {
        if (status == null) return "알 수 없음";
        
        switch (status.toUpperCase()) {
            case "SENT":
                return "발송완료";
            case "PENDING":
                return "대기중";
            case "FAILED":
                return "실패";
            case "ERROR":
                return "오류";
            default:
                return status;
        }
    }
    
    /**
     * 후속 연락 상태 텍스트 반환
     */
    public String getFollowUpStatusText() {
        if (hasFollowUp) {
            return followUpCount > 0 ? followUpCount + "회 연락" : "연락함";
        } else {
            return "연락없음";
        }
    }
    
    /**
     * 전화번호 마스킹 처리
     */
    public String getMaskedClid() {
        if (clid == null || clid.length() < 8) {
            return clid;
        }
        
        // 010-****-1234 형태로 마스킹
        if (clid.length() == 11 && clid.startsWith("010")) {
            return clid.substring(0, 3) + "-****-" + clid.substring(7);
        } else if (clid.length() == 13 && clid.contains("-")) {
            // 이미 하이픈이 있는 경우
            String[] parts = clid.split("-");
            if (parts.length == 3) {
                return parts[0] + "-****-" + parts[2];
            }
        }
        
        return clid;
    }
    
    /**
     * 메시지 요약 (50자 제한)
     */
    public String getMessageSummary() {
        if (message == null) return "";
        
        if (message.length() <= 50) {
            return message;
        } else {
            return message.substring(0, 47) + "...";
        }
    }
    
    /**
     * 발송 성공 여부
     */
    public boolean isSent() {
        return "SENT".equalsIgnoreCase(status);
    }
    
    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return "FAILED".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status);
    }
} 