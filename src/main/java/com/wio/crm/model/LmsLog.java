package com.wio.crm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LMS 문자 발송 로그 엔티티
 * LMS_LOG 테이블과 매핑
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LmsLog {
    
    private Long id;                    // ID (NUMBER) - 기본키 (자동증가)
    private String clid;                // CLID (VARCHAR2(20)) - 수신자 전화번호
    private String subject;             // SUBJECT (VARCHAR2(120)) - LMS 제목 (최대 40자)
    private String message;             // MESSAGE (VARCHAR2(2000)) - LMS 메시지 내용 (최대 2000자)
    private LocalDateTime sendDate;     // SEND_DATE (TIMESTAMP(6)) - 실제 전송 일시
    private String status;              // STATUS (VARCHAR2(20)) - 발송 상태 (PENDING/SENT/FAILED/ERROR)
    private String errorMessage;        // ERROR_MESSAGE (VARCHAR2(500)) - 오류 메시지
    private LocalDateTime createdDate;  // CREATED_DATE (TIMESTAMP(6)) - 레코드 생성 일시
    private Integer retryCount;         // RETRY_COUNT (NUMBER) - 재전송 시도 횟수
} 