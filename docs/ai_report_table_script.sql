-- ============================================================
-- AI 상담 분석 리포트 서비스 테이블 생성 스크립트 (Oracle 11g)
-- 작성일: 2025-12-17
-- ============================================================

-- ============================================================
-- 1. AI 리포트 구독 관리 테이블
-- ============================================================

-- 시퀀스 생성
CREATE SEQUENCE AI_SUBSCRIPTION_SEQ 
    START WITH 1 
    INCREMENT BY 1 
    NOCACHE 
    NOCYCLE;

-- 구독 테이블
CREATE TABLE AI_REPORT_SUBSCRIPTION (
    SUBSCRIPTION_ID     NUMBER(10) PRIMARY KEY,
    CUST_CODE           VARCHAR2(20) NOT NULL,          -- 업체 코드 (TBND01.CUST_CODE 참조)
    PLAN_TYPE           VARCHAR2(20) DEFAULT 'BASIC',   -- BASIC, PREMIUM, ENTERPRISE
    STATUS              VARCHAR2(20) DEFAULT 'ACTIVE',  -- ACTIVE, EXPIRED, CANCELLED, PENDING
    MONTHLY_FEE         NUMBER(10,0) DEFAULT 50000,     -- 월 요금
    START_DATE          DATE,                           -- 구독 시작일
    END_DATE            DATE,                           -- 구독 종료일
    AUTO_RENEWAL        CHAR(1) DEFAULT 'Y',            -- 자동 갱신 여부
    PAYMENT_METHOD      VARCHAR2(50),                   -- 결제 방법
    CONTACT_EMAIL       VARCHAR2(100),                  -- 리포트 발송 이메일
    CONTACT_NAME        VARCHAR2(50),                   -- 담당자명
    CONTACT_PHONE       VARCHAR2(20),                   -- 담당자 연락처
    IN_DATE             DATE DEFAULT SYSDATE,           -- 등록일
    IN_EMPNO            VARCHAR2(20),                   -- 등록자
    UP_DATE             DATE,                           -- 수정일
    UP_EMPNO            VARCHAR2(20),                   -- 수정자
    MEMO                VARCHAR2(500)                   -- 메모
);

-- 트리거: ID 자동 증가
CREATE OR REPLACE TRIGGER AI_SUBSCRIPTION_BI_TRG
BEFORE INSERT ON AI_REPORT_SUBSCRIPTION
FOR EACH ROW
BEGIN
    IF :NEW.SUBSCRIPTION_ID IS NULL THEN
        SELECT AI_SUBSCRIPTION_SEQ.NEXTVAL INTO :NEW.SUBSCRIPTION_ID FROM DUAL;
    END IF;
    :NEW.IN_DATE := SYSDATE;
END;
/

-- 인덱스
CREATE INDEX IDX_AI_SUB_CUST ON AI_REPORT_SUBSCRIPTION(CUST_CODE);
CREATE INDEX IDX_AI_SUB_STATUS ON AI_REPORT_SUBSCRIPTION(STATUS);

-- 코멘트
COMMENT ON TABLE AI_REPORT_SUBSCRIPTION IS 'AI 리포트 서비스 구독 관리';
COMMENT ON COLUMN AI_REPORT_SUBSCRIPTION.PLAN_TYPE IS 'BASIC: 기본, PREMIUM: 프리미엄, ENTERPRISE: 기업';
COMMENT ON COLUMN AI_REPORT_SUBSCRIPTION.STATUS IS 'ACTIVE: 활성, EXPIRED: 만료, CANCELLED: 취소, PENDING: 대기';

-- ============================================================
-- 2. AI 리포트 테이블
-- ============================================================

-- 시퀀스 생성
CREATE SEQUENCE AI_REPORT_SEQ 
    START WITH 1 
    INCREMENT BY 1 
    NOCACHE 
    NOCYCLE;

-- 리포트 테이블
CREATE TABLE AI_REPORT (
    REPORT_ID           NUMBER(10) PRIMARY KEY,
    SUBSCRIPTION_ID     NUMBER(10),                     -- 구독 ID (FK)
    CUST_CODE           VARCHAR2(20) NOT NULL,          -- 업체 코드
    REPORT_MONTH        VARCHAR2(6) NOT NULL,           -- 리포트 대상 월 (YYYYMM)
    REPORT_TYPE         VARCHAR2(20) DEFAULT 'MONTHLY', -- MONTHLY, WEEKLY, CUSTOM
    REPORT_TITLE        VARCHAR2(200),                  -- 리포트 제목
    
    -- 기본 통계
    TOTAL_CONSULTATIONS NUMBER(10) DEFAULT 0,           -- 총 상담 수
    COMPLETED_COUNT     NUMBER(10) DEFAULT 0,           -- 처리 완료 수
    URGENT_COUNT        NUMBER(10) DEFAULT 0,           -- 긴급 상담 수
    CLAIM_COUNT         NUMBER(10) DEFAULT 0,           -- 클레임 수
    AVG_PROCESS_TIME    NUMBER(5,1) DEFAULT 0,          -- 평균 처리 시간 (일)
    
    -- AI 분석 결과 (JSON)
    SUMMARY_JSON        CLOB,                           -- 요약 분석 결과
    KEYWORDS_JSON       CLOB,                           -- 키워드 분석 결과
    SENTIMENT_JSON      CLOB,                           -- 감정 분석 결과
    PRODUCT_JSON        CLOB,                           -- 제품별 분석 결과
    TREND_JSON          CLOB,                           -- 트렌드 분석 결과
    RECOMMENDATIONS     CLOB,                           -- AI 개선 제안
    
    -- 파일 정보
    PDF_PATH            VARCHAR2(500),                  -- PDF 파일 경로
    EXCEL_PATH          VARCHAR2(500),                  -- Excel 파일 경로
    
    -- 처리 정보
    STATUS              VARCHAR2(20) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    ERROR_MESSAGE       VARCHAR2(1000),                 -- 오류 메시지
    TOKEN_USED          NUMBER(10) DEFAULT 0,           -- AI API 토큰 사용량
    PROCESSING_TIME     NUMBER(10) DEFAULT 0,           -- 처리 시간 (초)
    
    -- 발송 정보
    EMAIL_SENT          CHAR(1) DEFAULT 'N',            -- 이메일 발송 여부
    EMAIL_SENT_DATE     DATE,                           -- 이메일 발송일
    
    -- 감사 정보
    IN_DATE             DATE DEFAULT SYSDATE,           -- 생성일
    COMPLETED_DATE      DATE,                           -- 완료일
    
    -- 외래키
    CONSTRAINT FK_AI_REPORT_SUB FOREIGN KEY (SUBSCRIPTION_ID) 
        REFERENCES AI_REPORT_SUBSCRIPTION(SUBSCRIPTION_ID)
);

-- 트리거: ID 자동 증가
CREATE OR REPLACE TRIGGER AI_REPORT_BI_TRG
BEFORE INSERT ON AI_REPORT
FOR EACH ROW
BEGIN
    IF :NEW.REPORT_ID IS NULL THEN
        SELECT AI_REPORT_SEQ.NEXTVAL INTO :NEW.REPORT_ID FROM DUAL;
    END IF;
    :NEW.IN_DATE := SYSDATE;
END;
/

-- 인덱스
CREATE INDEX IDX_AI_REPORT_CUST ON AI_REPORT(CUST_CODE);
CREATE INDEX IDX_AI_REPORT_MONTH ON AI_REPORT(REPORT_MONTH);
CREATE INDEX IDX_AI_REPORT_STATUS ON AI_REPORT(STATUS);

-- 코멘트
COMMENT ON TABLE AI_REPORT IS 'AI 분석 리포트';
COMMENT ON COLUMN AI_REPORT.STATUS IS 'PENDING: 대기, PROCESSING: 처리중, COMPLETED: 완료, FAILED: 실패';

-- ============================================================
-- 3. AI 프롬프트 템플릿 테이블
-- ============================================================

-- 시퀀스 생성
CREATE SEQUENCE AI_PROMPT_SEQ 
    START WITH 1 
    INCREMENT BY 1 
    NOCACHE 
    NOCYCLE;

-- 프롬프트 템플릿 테이블
CREATE TABLE AI_PROMPT_TEMPLATE (
    PROMPT_ID           NUMBER(10) PRIMARY KEY,
    PROMPT_TYPE         VARCHAR2(50) NOT NULL,          -- SUMMARY, KEYWORD, SENTIMENT, PRODUCT, RECOMMENDATION
    PROMPT_NAME         VARCHAR2(100),                  -- 프롬프트 이름
    PROMPT_TEXT         CLOB NOT NULL,                  -- 프롬프트 내용
    MODEL_NAME          VARCHAR2(50) DEFAULT 'gpt-4',   -- 사용할 AI 모델
    MAX_TOKENS          NUMBER(10) DEFAULT 2000,        -- 최대 토큰
    TEMPERATURE         NUMBER(3,2) DEFAULT 0.7,        -- 창의성 (0-1)
    VERSION             NUMBER(5) DEFAULT 1,            -- 버전
    IS_ACTIVE           CHAR(1) DEFAULT 'Y',            -- 활성 여부
    IN_DATE             DATE DEFAULT SYSDATE,
    UP_DATE             DATE
);

-- 트리거: ID 자동 증가
CREATE OR REPLACE TRIGGER AI_PROMPT_BI_TRG
BEFORE INSERT ON AI_PROMPT_TEMPLATE
FOR EACH ROW
BEGIN
    IF :NEW.PROMPT_ID IS NULL THEN
        SELECT AI_PROMPT_SEQ.NEXTVAL INTO :NEW.PROMPT_ID FROM DUAL;
    END IF;
END;
/

-- 코멘트
COMMENT ON TABLE AI_PROMPT_TEMPLATE IS 'AI 분석용 프롬프트 템플릿';

-- ============================================================
-- 4. 리포트 조회 로그 테이블
-- ============================================================

-- 시퀀스 생성
CREATE SEQUENCE AI_REPORT_LOG_SEQ 
    START WITH 1 
    INCREMENT BY 1 
    NOCACHE 
    NOCYCLE;

-- 조회 로그 테이블
CREATE TABLE AI_REPORT_VIEW_LOG (
    LOG_ID              NUMBER(10) PRIMARY KEY,
    REPORT_ID           NUMBER(10) NOT NULL,            -- 리포트 ID
    USER_ID             VARCHAR2(50),                   -- 조회한 사용자
    VIEW_DATE           DATE DEFAULT SYSDATE,           -- 조회 일시
    IP_ADDRESS          VARCHAR2(50),                   -- 접속 IP
    USER_AGENT          VARCHAR2(500),                  -- 브라우저 정보
    ACTION_TYPE         VARCHAR2(20) DEFAULT 'VIEW',    -- VIEW, DOWNLOAD_PDF, DOWNLOAD_EXCEL
    
    CONSTRAINT FK_AI_LOG_REPORT FOREIGN KEY (REPORT_ID) 
        REFERENCES AI_REPORT(REPORT_ID)
);

-- 트리거
CREATE OR REPLACE TRIGGER AI_REPORT_LOG_BI_TRG
BEFORE INSERT ON AI_REPORT_VIEW_LOG
FOR EACH ROW
BEGIN
    IF :NEW.LOG_ID IS NULL THEN
        SELECT AI_REPORT_LOG_SEQ.NEXTVAL INTO :NEW.LOG_ID FROM DUAL;
    END IF;
END;
/

-- ============================================================
-- 5. 초기 프롬프트 템플릿 데이터
-- ============================================================

-- 요약 분석 프롬프트
INSERT INTO AI_PROMPT_TEMPLATE (PROMPT_TYPE, PROMPT_NAME, PROMPT_TEXT, MODEL_NAME) VALUES (
    'SUMMARY',
    '월간 상담 요약',
    '당신은 고객 상담 데이터 분석 전문가입니다.

다음은 한 달간의 고객 상담 데이터입니다. 이 데이터를 분석하여 비즈니스에 유용한 인사이트를 제공해주세요.

[분석 요청 사항]
1. 전체 상담 현황 요약 (한 문단)
2. 주요 이슈 3가지
3. 개선이 필요한 영역
4. 긍정적인 트렌드

결과는 다음 JSON 형식으로 반환해주세요:
{
  "summary": "전체 요약 텍스트",
  "mainIssues": ["이슈1", "이슈2", "이슈3"],
  "improvementAreas": ["영역1", "영역2"],
  "positiveTrends": ["트렌드1", "트렌드2"]
}',
    'gpt-4'
);

-- 키워드 분석 프롬프트
INSERT INTO AI_PROMPT_TEMPLATE (PROMPT_TYPE, PROMPT_NAME, PROMPT_TEXT, MODEL_NAME) VALUES (
    'KEYWORD',
    '키워드 추출',
    '다음 고객 상담 내용에서 가장 자주 언급된 키워드를 추출해주세요.

[분석 대상]
- 제품명
- 문제 유형 (배송, 품질, 환불 등)
- 고객 요청 사항

결과는 다음 JSON 형식으로 반환해주세요:
{
  "products": [{"name": "제품명", "count": 숫자, "sentiment": "positive/negative/neutral"}],
  "issues": [{"type": "이슈유형", "count": 숫자}],
  "requests": [{"request": "요청사항", "count": 숫자}]
}',
    'gpt-4'
);

-- 감정 분석 프롬프트
INSERT INTO AI_PROMPT_TEMPLATE (PROMPT_TYPE, PROMPT_NAME, PROMPT_TEXT, MODEL_NAME) VALUES (
    'SENTIMENT',
    '감정 분석',
    '다음 고객 상담 내용의 전반적인 고객 감정을 분석해주세요.

[분석 항목]
1. 전체 감정 분포 (긍정/중립/부정 비율)
2. 주요 불만 사항 TOP 5
3. 만족도가 높은 영역
4. 감정 변화 추이

결과는 다음 JSON 형식으로 반환해주세요:
{
  "distribution": {"positive": 30, "neutral": 50, "negative": 20},
  "topComplaints": [{"complaint": "불만사항", "count": 숫자}],
  "satisfactionAreas": ["영역1", "영역2"],
  "emotionTrend": "상승/하락/유지"
}',
    'gpt-4'
);

-- 개선 제안 프롬프트
INSERT INTO AI_PROMPT_TEMPLATE (PROMPT_TYPE, PROMPT_NAME, PROMPT_TEXT, MODEL_NAME) VALUES (
    'RECOMMENDATION',
    'AI 개선 제안',
    '고객 상담 데이터 분석 결과를 바탕으로, 이 업체가 실행 가능한 개선 방안을 제안해주세요.

[제안 조건]
1. 구체적이고 실행 가능한 방안
2. 예상 효과 포함
3. 우선순위 표시 (높음/중간/낮음)
4. 최소 3개, 최대 5개 제안

결과는 다음 JSON 형식으로 반환해주세요:
{
  "recommendations": [
    {
      "title": "제안 제목",
      "description": "상세 설명",
      "expectedEffect": "예상 효과",
      "priority": "high/medium/low",
      "category": "product/service/process/communication"
    }
  ]
}',
    'gpt-4'
);

COMMIT;

-- ============================================================
-- 6. 확인 쿼리
-- ============================================================

-- 테이블 목록 확인
SELECT TABLE_NAME, COMMENTS 
FROM USER_TAB_COMMENTS 
WHERE TABLE_NAME LIKE 'AI_%';

-- 시퀀스 확인
SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME LIKE 'AI_%';

