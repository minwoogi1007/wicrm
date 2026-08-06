# AI 상담 분석 리포트 서비스

## 📋 개요

월간 상담 데이터를 AI가 분석하여 비즈니스 인사이트를 제공하는 유료 서비스입니다.

## 🏗️ 아키텍처

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   상담 DB    │────▶│  데이터 전처리 │────▶│   AI 분석    │
│  (TBND01)    │     │  (Spring)    │     │  (OpenAI)    │
└──────────────┘     └──────────────┘     └──────────────┘
                                                  │
                                                  ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  PDF/Excel   │◀────│  리포트 생성  │◀────│  결과 저장   │
│   다운로드   │     │  (Template)  │     │   (DB)      │
└──────────────┘     └──────────────┘     └──────────────┘
```

## 📁 파일 구조

```
src/main/java/com/wio/crm/
├── controller/
│   └── AiReportController.java      # API 컨트롤러
├── service/
│   ├── AiReportService.java         # 리포트 비즈니스 로직
│   └── AiAnalysisService.java       # OpenAI API 연동
├── mapper/
│   └── AiReportMapper.java          # DB 매퍼
├── model/
│   ├── AiReport.java                # 리포트 모델
│   └── AiReportSubscription.java    # 구독 모델
└── scheduler/
    └── AiReportScheduler.java       # 자동 생성 스케줄러

src/main/resources/templates/ai-report/
├── my-reports.html                  # 업체용 - 리포트 목록
├── view.html                        # 업체용 - 리포트 상세
├── subscribe.html                   # 구독 신청 폼
└── admin/
    └── dashboard.html               # 관리자 대시보드

docs/
├── ai_report_table_script.sql       # DB 테이블 생성 스크립트
└── ai_report_service.md             # 이 문서
```

## 🗄️ DB 테이블

### AI_REPORT_SUBSCRIPTION (구독 관리)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| SUBSCRIPTION_ID | NUMBER | PK |
| CUST_CODE | VARCHAR2(20) | 업체 코드 |
| PLAN_TYPE | VARCHAR2(20) | BASIC/PREMIUM/ENTERPRISE |
| STATUS | VARCHAR2(20) | ACTIVE/EXPIRED/CANCELLED |
| MONTHLY_FEE | NUMBER | 월 요금 |
| START_DATE | DATE | 시작일 |
| END_DATE | DATE | 종료일 |

### AI_REPORT (리포트)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| REPORT_ID | NUMBER | PK |
| CUST_CODE | VARCHAR2(20) | 업체 코드 |
| REPORT_MONTH | VARCHAR2(6) | 대상 월 (YYYYMM) |
| SUMMARY_JSON | CLOB | AI 요약 분석 결과 |
| KEYWORDS_JSON | CLOB | 키워드 분석 결과 |
| SENTIMENT_JSON | CLOB | 감정 분석 결과 |
| RECOMMENDATIONS | CLOB | AI 개선 제안 |
| STATUS | VARCHAR2(20) | PENDING/PROCESSING/COMPLETED/FAILED |

## ⚙️ 설정

### application.properties

```properties
# AI 기능 활성화
ai.report.enabled=false

# OpenAI API 설정
openai.api.key=${OPENAI_API_KEY:}
openai.api.url=https://api.openai.com/v1/chat/completions
openai.model=gpt-4

# 스케줄러 설정
ai.report.scheduler.enabled=false
```

## 🚀 사용 방법

### 1. DB 테이블 생성
```sql
@docs/ai_report_table_script.sql
```

### 2. OpenAI API 키 설정
```properties
openai.api.key=${OPENAI_API_KEY:}
ai.report.enabled=true
```
> 실제 API Key는 문서/코드에 직접 적지 말고 **환경변수(`OPENAI_API_KEY`)로만** 주입합니다.

### 3. 스케줄러 활성화 (선택)
```properties
ai.report.scheduler.enabled=true
```

## 🔗 API 엔드포인트

### 업체용
| Method | URL | 설명 |
|--------|-----|------|
| GET | /ai-report | 내 리포트 목록 |
| GET | /ai-report/view/{id} | 리포트 상세 |
| POST | /ai-report/request | 리포트 생성 요청 |
| GET | /ai-report/subscribe | 구독 신청 폼 |
| POST | /ai-report/subscribe | 구독 신청 처리 |

### 관리자용
| Method | URL | 설명 |
|--------|-----|------|
| GET | /ai-report/admin/dashboard | 관리 대시보드 |
| GET | /ai-report/admin/subscriptions | 구독 관리 |
| GET | /ai-report/admin/reports | 리포트 관리 |
| GET | /ai-report/admin/test-ai | AI 연결 테스트 |
| POST | /ai-report/admin/generate | 리포트 수동 생성 |

## 💰 요금제

| 플랜 | 가격 | 포함 내용 |
|------|------|----------|
| Basic | ₩50,000/월 | 월간 리포트 1회, 기본 분석 |
| Premium | ₩100,000/월 | 주간 리포트, 상세 분석, PDF |
| Enterprise | ₩200,000/월 | 실시간 분석, API 연동, 맞춤 리포트 |

## 📊 분석 항목

1. **요약 분석** - 전체 상담 현황, 주요 이슈
2. **키워드 분석** - 제품별/유형별 빈도 분석
3. **감정 분석** - 긍정/중립/부정 비율
4. **AI 개선 제안** - 구체적인 액션 아이템

## 🔧 개발 노트

- AI 기능이 비활성화된 경우 샘플 데이터로 동작
- 토큰 제한으로 최대 100건의 상담만 분석
- 매월 1일 오전 9시 자동 생성 (스케줄러 활성화 시)

---
작성일: 2025-12-17

