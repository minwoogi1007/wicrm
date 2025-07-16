package com.wio.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 통화 로그 DTO
 * CALL_LOG_D 테이블과 매핑
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallLogDto {
    
    private String callDate;        // 통화일시 (VARCHAR2)
    private String clid;            // 전화번호
    private String personCode;      // 담당자 코드
    private String custCode;        // 고객 코드
    private String projectCode;     // 프로젝트 코드
    private String result;          // 통화 결과
    private String empno;           // 직원번호
    private String incallNo;        // 수신번호
    private String context;         // 통화 내용
    private String confirm;         // 확인 상태
    private String regid;           // 등록자
    
    // 조인용 추가 필드들
    private String empName;         // 직원명
    private String agentName;       // 상담원명
    private LocalDateTime callDateTime; // 변환된 통화일시
    private String customerName;    // 고객명
    private String projectName;     // 프로젝트명
    
    /**
     * 통화 결과 텍스트 반환
     */
    public String getResultText() {
        if (result == null) return "알 수 없음";
        
        switch (result.toUpperCase()) {
            case "CONNECTED":
            case "통화완료":
                return "통화완료";
            case "NO_ANSWER":
            case "미응답":
                return "미응답";
            case "BUSY":
            case "통화중":
                return "통화중";
            case "ATTEMPTED":
            case "통화시도":
                return "통화시도";
            case "FAILED":
            case "실패":
                return "통화실패";
            default:
                return result;
        }
    }
    
    /**
     * 확인 상태 텍스트 반환
     */
    public String getConfirmText() {
        if (confirm == null) return "미확인";
        
        switch (confirm.toUpperCase()) {
            case "CONFIRMED":
                return "확인됨";
            case "FAILED":
                return "실패";
            case "ATTEMPTED":
                return "시도중";
            case "PENDING":
                return "대기중";
            default:
                return confirm;
        }
    }
    
    /**
     * 통화 성공 여부
     */
    public boolean isSuccessful() {
        return result != null && 
               (result.toUpperCase().contains("완료") || 
                result.toUpperCase().contains("CONNECTED") ||
                result.toUpperCase().contains("SUCCESS"));
    }
    
    /**
     * 통화 시도 여부
     */
    public boolean isAttempted() {
        return result != null && 
               (result.toUpperCase().contains("시도") || 
                result.toUpperCase().contains("ATTEMPTED"));
    }
    
    /**
     * 통화 내용 요약 (100자 제한)
     */
    public String getContextSummary() {
        if (context == null) return "";
        
        if (context.length() <= 100) {
            return context;
        } else {
            return context.substring(0, 97) + "...";
        }
    }
    
    /**
     * 상담원명 또는 직원명 반환
     */
    public String getAgentDisplayName() {
        if (agentName != null && !agentName.trim().isEmpty()) {
            return agentName;
        } else if (empName != null && !empName.trim().isEmpty()) {
            return empName;
        } else {
            return empno != null ? empno : "알 수 없음";
        }
    }
} 