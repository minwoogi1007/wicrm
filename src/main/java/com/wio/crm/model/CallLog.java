package com.wio.crm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 통화 로그 엔티티
 * CALL_LOG_D 테이블과 매핑
 * 
 * @author 개발팀
 * @since 2025.01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallLog {
    
    private String callDate;        // CALLDATE (VARCHAR2(30)) - 통화일시
    private String clid;            // CLID (VARCHAR2(255)) - 전화번호
    private String personCode;      // PERSON_CODE (VARCHAR2(255)) - 담당자 코드
    private String custCode;        // CUST_CODE (VARCHAR2(20)) - 고객 코드
    private String projectCode;     // PROJECT_CODE (VARCHAR2(255)) - 프로젝트 코드
    private String result;          // RESULT (VARCHAR2(255)) - 통화 결과
    private String empno;           // EMPNO (VARCHAR2(255)) - 직원번호
    private String incallNo;        // INCALL_NO (VARCHAR2(255)) - 수신번호
    private String context;         // CONTEXT (VARCHAR2(30)) - 통화 내용
    private String confirm;         // CONFIRM (VARCHAR2(20)) - 확인 상태
    private String regid;           // REGID (VARCHAR2(100)) - 등록자
} 