# WICRM 소스 아키텍처 분석

> 읽기 전용 코드 분석. 런타임 코드/비밀값은 이 문서에 포함하지 않는다.  
> 분석 기준: 저장소 `main` (Gradle 프로젝트명 `crm`, 패키지 `com.wio.crm`).  
> 작성일: 2026-09-21

## 1. 한 줄 요약

이 저장소는 **거래처(고객사) 직원이 브라우저로 상담/통화 CRM을 조회하는 Spring Boot 웹 포털**이다.  
사내 CTI 클라이언트(문서상 `2024WIO`, Edge/ActiveX)가 아니다. 통화·상담 원천은 Oracle의 `CALL_LOG_D` / `TBND01`이며, 화면은 **WebSocket/SSE가 아니라 요청 시점 조회 + 대시보드 25초 AJAX 폴링**으로 “실시간”을 구현한다.

제품 오너 설명과의 대조:

| 주장 | 코드에서 확인한 내용 |
|------|----------------------|
| 고객사가 CRM을 실시간으로 본다 | 맞음. `gubn=1` 거래처 계정 + `CUST_CODE` 범위 조회. 대시보드는 25초마다 `/api/dashboard-*`를 다시 친다. |
| 사내 2024WIO(Edge/ActiveX CTI)와 별개 | 맞음. 이 앱에는 ActiveX/CTI 클라이언트 코드가 없다. CTI가 `TBND01_CUST`를 만든다는 주석만 존재. 파일 서버는 `2024WIO`·`repair`와 공유한다는 기존 문서가 있다. |
| “뷰어 전용” | 부분적으로만 맞음. 상담 목록/통계는 조회가 중심이지만, 메모·완료코드·교환/반품·문의 등록 등 **쓰기 경로가 있다.** |

공개 서비스 브랜딩은 템플릿 타이틀 `WIO SERVICE`, 운영 도메인 주석 `https://wioservice.kr`.

---

## 2. 스택

| 구분 | 실제 사용 |
|------|-----------|
| 언어 | Java 17 (`sourceCompatibility = VERSION_17`) |
| 앱 프레임워크 | Spring Boot **3.2.1** (`CrmApplication`, `@EnableScheduling`, `@EnableTransactionManagement`) |
| 보안 | Spring Security **6.2.1**, form login, BCrypt, 쿠키 CSRF |
| 웹 UI | Thymeleaf + Layout Dialect, Keen/Metronic 계열 정적 번들 (`/assets`), jQuery, ApexCharts/Chart.js, DataTables, Bootstrap 5 |
| 영속화 | **MyBatis 3.0.3** (상담/통계/대시보드 주력) + **Spring Data JPA / Hibernate 6.4.1** (교환/반품 `TB_RETURN_ITEM` 등) |
| DB | Oracle 11g 호환 JDBC (`ojdbc8`, `orai18n`), HikariCP |
| 빌드 | Gradle **8.3** wrapper, `java` + `war` 플러그인, `bootJar` |
| HTTP 클라이언트 | `spring-boot-starter-webflux` (`WebClient.Builder`), 일부 `RestTemplate` (이미지 업로드 포워딩, OpenAI) |
| 기타 | Lombok, Apache POI 5.2.3 (엑셀), Actuator(`health`만 노출), OpenAI Chat Completions (기본 비활성) |
| 테스트 | `CrmApplicationTests` 컨텍스트 로드 1개. 실질 단위/통합 테스트 없음 |

프론트는 SPA가 아니다. 서버가 HTML을 렌더하고, 페이지별 JS가 JSON API를 호출한다.

---

## 3. 디렉터리 맵

```
wicrm/
├── build.gradle / settings.gradle / gradle/wrapper/
├── src/main/java/
│   ├── com.wio.crm/                 # 본 애플리케이션
│   │   ├── CrmApplication.java      # 부트 엔트리
│   │   ├── config/                  # Security, MVC, CSRF, WebClient, 로그인 핸들러
│   │   ├── controller/              # 화면 + JSON API
│   │   ├── service/ (+ impl/)       # 비즈니스 로직, custCode 스코프
│   │   ├── mapper/                  # MyBatis 인터페이스
│   │   ├── model/                   # 상담/계정/대시보드 등 POJO
│   │   ├── Entity/                  # JPA 엔티티 (AdminCode, UserActionLog)
│   │   ├── repository/              # JPA Repository
│   │   ├── dto/
│   │   ├── scheduler/               # AI 리포트 스케줄러
│   │   └── exception/
│   └── com.wicrm.api/               # ImageProxyController 복제본 (아래 리스크)
├── src/main/resources/
│   ├── application.properties       # 공통 (dev 프로파일 기본)
│   ├── application-dev.properties   # 로컬 HTTP :8081
│   ├── application-prod.properties  # HTTPS :443 (.gitignore 대상이지만 워크스페이스에 존재)
│   ├── mybatis-config.xml
│   ├── mapper/*.xml                 # 17개 XML
│   ├── templates/                   # Thymeleaf
│   └── static/assets/               # JS/CSS/테마
├── src/test/java/com/wio/crm/CrmApplicationTests.java
├── docs/                            # 운영/설계 문서 (이 파일 포함)
├── uploads/                         # 로컬 업로드 디렉터리 (gitignore)
└── logs/
```

기능별 화면 루트:

| 경로 접두 | 템플릿 | 역할 |
|-----------|--------|------|
| `/login`, `/` | `sign-in.html` (`login.html`은 레거시로 보임) | 로그인·가입 신청 |
| `/main` | `contents.html` (`layout:decorate` → `main.html`) | 고객사 대시보드 |
| `/cons` | `cons/consulting.html` | CTI 상담 원장 조회 |
| `/consulting` | `consulting/*` | `CONSULTING_INQUIRY` 문의 관리 |
| `/stat/*` | `statistics/*` | 일/주/월 운영·상담 통계 |
| `/exchange` | `exchange/*` | 교환/반품 (`TB_RETURN_ITEM`) |
| `/payment` | `payment/register.html` | 배송비 입금 |
| `/logistics` | `logistics/*` | 직반품 |
| `/board` | `board/*` | `BOARD_CALL` |
| `/lms-tracking` | `lms-tracking/list.html` | LMS 문자 ↔ 콜 추적 |
| `/mileage*` | `mileage/*` | 포인트/마일리지 |
| `/ai-report` | `ai-report/*` | 유료 AI 월간 리포트 (기본 off) |
| `/admin/banners` | `admin/banners/*` | 내부 직원 배너 |
| `/design-samples` | `design-samples/*` | 로그인 없이 공개되는 UI 샘플 |

레이아웃: `templates/main.html` + `fragments/sidebar.html`이 세션 `USER_MENUS`로 메뉴를 그린다.

---

## 4. 엔트리 포인트와 요청 흐름

### 4.1 프로세스 기동

- `com.wio.crm.CrmApplication#main` → `SpringApplication.run`
- 기본 프로파일: `spring.profiles.active=dev`
- 로컬: HTTP `8081`. 운영 프로파일: HTTPS `443` + PKCS12 키스토어(`classpath:keystore.p12`, 비밀번호는 환경변수)

### 4.2 브라우저 진입

```
GET /                    LoginController → redirect:/login
GET /login               sign-in.html (form POST /login, username=userId)
                         Security formLogin → CustomUserDetailsService
                         성공: CustomSuccessHandler
                           - 세션: USER_MENUS, loginUserId, loginUserName,
                                   loginUserAuthority, custCode
                           - LoginHistoryService 기록
                         실패: AjaxAuthenticationFailureHandler + 시도 잠금
GET /main                MainController → DashboardService.getTcntEmp()
                         템플릿 contents.html + /assets/js/common/dashboard.js
```

`SecurityConfig`에 `defaultSuccessUrl`이 없다. `CustomSuccessHandler`는 `SavedRequestAwareAuthenticationSuccessHandler`를 그대로 호출한다. 저장된 요청이 없으면 Spring 기본 타깃 `/`로 가고, `/`는 다시 `/login`으로 보낸다. 실제 사용은 보호된 URL(`/main` 등)로 들어왔거나, 메뉴에서 `/main`을 누르는 흐름에 의존한다.

`templates/main.html` / `layouts/main.html`은 `session.loginUserId`가 없으면 JS로 `/login`에 보낸다 (서버 세션과 이중 가드).

### 4.3 메뉴

로그인 후 `MenuService.getCompanyUserMenus(userId, authority)` → `menuMapper.xml`.

- 내부 직원: `N_WMENU` + `N_WMENU_USER` + `N_TEMP01` (`USER_ID = POSITION`)
- 거래처: 동일 메뉴 테이블 + `N_TCNT01_EMP` (`USER_ID = CUST_CODE`)
- `authority != 1`이면 `MU.ADMIN IS NULL` (관리 메뉴 숨김)
- 사이드바는 `PROG_NM`을 `href`로 사용

---

## 5. 인증·권한

### 5.1 계정 모델

자격 증명은 `N_TIPDW` (`TipdwMapper.findByUserId`). `gubn`으로 사용자 종류를 나눈다.

| gubn | 의미 | 부가 테이블 | Spring 권한 | 세션 custCode |
|------|------|-------------|-------------|---------------|
| `0` | 내부 직원 | `TEMP01` (`work_gubn != 0`이어야 활성) | `ROLE_EMPLOYEE` | 핸들러가 `"INTERNAL"` |
| `1` | 거래처 직원 | `N_TCNT01_EMP` | `ROLE_USER` (+ `authority=1`이면 `ROLE_ADMIN`) | `N_TCNT01_EMP.CUST_CODE` |

가입 신청: 로그인 화면의 “직원 신청” / “업체 직원 신청” → `POST /apply-userid` (`permitAll`). `confirmYn=N`이면 `UserNotConfirmedException` (“아직 승인되지 않은 사용자”). 승인은 `/user-approval-list`.

비밀번호: `BCryptPasswordEncoder`. 마이그레이션용 `PasswordEncryptionService`가 평문을 재암호화한다.

### 5.2 SecurityFilterChain 요약

인증 필요(기본 `anyRequest().authenticated()`):

- 대시보드/상담/통계/교환/게시판 등 업무 URL

명시 허용:

- `/login`, `/check-userid-availability`, `/apply-userid`
- `/encrypt-passwords`, `/encrypt-password`, `/encryption` (**관리용 암호 재해시, 인증 없음**)
- `/download/**`, `/upload`, `/board/uploadImage`
- `/design-samples/**`, `/project-plan/**`, `/project-plan/api/**`
- `/.well-known/**` (SSL 검증)
- `/integrations/cafe24/oauth/callback` (카페24 콜백; **이 저장소에 컨트롤러 없음**)
- 정적 `/assets/**` (`WebSecurityCustomizer` ignore)

역할 제한:

- `/empl/**`, `/admin/banners/**` → `ROLE_EMPLOYEE`

기타:

- CSRF: 쿠키 `XSRF-TOKEN` (`HttpOnly=false`). 예외: `/logout`, `/error`, `/api/log/**`, 카페24 콜백
- 세션 고정 공격 완화: `changeSessionId`, `maximumSessions(1)`, 만료 시 `/login?expired`
- 헤더: `X-Frame-Options=DENY`, HSTS, Referrer-Policy, Permissions-Policy
- AJAX(`X-Requested-With`) 미인증은 401
- 로그인 잠금: `LoginAttemptService` (메모리 `ConcurrentHashMap`, 5회/15분). 재시작·다중 인스턴스에서 공유되지 않음

`/integrations/cafe24/**`는 인증만 요구하고, 주석의 “거래처 custCode 가드”를 수행할 컨트롤러가 이 repo에 없다.

---

## 6. 실시간 CRM 데이터 경로 (핵심)

**푸시가 아니다.** WebSocket, SSE, STOMP 구현은 없다. 기존 긴급 수정 문서도 SSE를 “향후 과제 (2024WIO 참조)”로 적어 두었다.

### 6.1 홈 대시보드 (고객사가 보는 “실시간”)

```
브라우저 /main
  → contents.html 위젯 DOM
  → static/assets/js/common/dashboard.js
       DOMContentLoaded: fetchData, fetchCallData, fetchPersonData,
                         fetchMonthData, fetchDailyData, fetchWeeklyData,
                         fetchYearData, fetchPoint
       setInterval(fetchData, 25000)
       setInterval(fetchMonthData, 25000)
  → GET /api/dashboard-data
  → GET /api/dashboard-callCount-data
  → GET /api/dashboard-personCount-data
  → GET /api/dashboard-month-data
  → GET /api/dashboard-daily-data | weekly-data | monthly-data
  → GET /api/dashboard-point-data | count-data
  → DashboardController → DashboardService
       getCurrentUserCustCode()
         거래처: Tcnt01Emp.custCode
         없거나 빈 값: 하드코딩 폴백 "P000000011"   ← 리스크
         내부 직원: "" (매퍼 <if>가 CUST_CODE 조건을 생략할 수 있음)
  → DashboardMapper.xml
       CALL_LOG_D   : 당일/시간대 콜, RESULT<>'ANSWER' 미스 등
       TBND01       : 완료/긴급 상담 건수
       TCNT01_POINT : 포인트
       TCNT01       : 업체명/등급
```

카드 의미(매퍼 기준): 오늘 콜 합계, 완료율, 미완료, 긴급, 시간대 콜, 상담 유형별 포인트. 내부 직원 화면의 “상담원/업체 처리 현황”은 `/empl/dashboard-employee`, `/empl/dashboard-custom` (`ROLE_EMPLOYEE`).

### 6.2 상담 원장 (`/cons`) — CTI 데이터 뷰어

이것이 고객사용 “CRM 상담 조회”의 본체다.

```
GET /cons
  AdminCodeService (TSYS01: 4003 처리상태, 4002 유형, 5000 몰, 9500 완료코드)
GET /api/consultations          목록 (page/pageSize, 기간, 필터)
GET /api/consultations/stats
GET /api/consultations/details  단건 + 메모 + 이력
POST /api/consultations/addComment
POST /api/consultations/updateCompletionCode
GET  /api/consultations/excel
```

서비스: `ConService`가 세션 사용자에서 `custCode`를 꺼내 모든 SELECT에 넣는다.  
매퍼: `ConsMapper.xml` → `TBND01` (키: `CUST_CODE, PROJECT_CODE, PERSON_CODE, CALL_CODE`).

조인/서브쿼리:

- `TPER01` — 고객명·전화
- `TPRJ01` — 프로젝트
- `TSYS01` — 코드명
- `TBND01_MEMO` — 댓글 (`insertComment`)

`ConController` 주석: 완료코드 갱신 시 레코드가 없으면 *“CTI 시스템에서 아직 생성되지 않음”*. 현재 `updateCompletionCode` SQL은 `TBND01`을 직접 업데이트한다.

`cons/consulting.html`의 AJAX는 **검색/페이지 이동 시점에만** 호출한다 (대시보드와 달리 자동 폴링 없음). 새로고침해야 최신 CTI 건이 보인다.

### 6.3 운영 통계

`StatController` + `StatisticsService` + `StaticsMapper.xml`.

- 페이지: `/stat/daily`, `/stat/weekly`, `/stat/monthly`, `/statCons`, `/statResult`, `/statTime`
- 데이터: 동일하게 `CALL_LOG_D` + `TBND01`를 `custCode`로 집계
- 화면 JS: `static/assets/js/stat/*.js`

### 6.4 교환/반품 “실시간 통계”

`GET /exchange/api/realtime-stats`는 이름과 달리 **온디맨드 GET**이다. `TB_RETURN_ITEM`을 조건으로 다시 센다. 샘플 매핑 대시보드는 5분 `setInterval`이 있다 (`templates/sample/…`, 운영 메뉴와 별개).

### 6.5 이 앱이 쓰지 않는 것

- CTI 소프트폰, 녹취 재생 클라이언트, ActiveX, Edge 전용 브리지
- 콜 이벤트 구독
- `CALL_LOG_D` / `TBND01` insert (상담 원장은 읽기 + 메모/완료코드)

원천 기록은 사내 CTI/2024WIO 쪽이 넣는다고 보는 것이 코드와 맞다.

---

## 7. API / 백엔드

이 서비스 자체가 백엔드다. 별도 BFF나 CRM SaaS API는 없다.

### 7.1 이 프로세스가 노출하는 HTTP

인증된 세션 JSON이 대부분이다. 대표 그룹:

| 그룹 | 컨트롤러 | 예시 |
|------|----------|------|
| 대시보드 | `DashboardController` | `/api/dashboard-data`, `/empl/dashboard-*` |
| 상담 원장 | `ConController` | `/api/consultations*` |
| 통계 | `StatController` | `/stat/*/search`, `/api/statCons/searchCons` |
| 교환/반품 | `ExchangeController` | `/exchange/api/list`, `/exchange/api/realtime-stats`, 엑셀, 이미지 |
| 입금 | `PaymentController` | `/payment/api/*` |
| 직반품 | `LogisticsDirectReturnController` | `/logistics/direct-return/api/*` |
| 문의 | `ConsultingController` | `/consulting/list`, `/consulting/add` (서버 렌더 + POST) |
| LMS | `LmsTrackingController` | `/lms-tracking/api/lms-list` |
| 계정 | `AccountController` | `/api/account/update`, `/api/change-password` |
| 게시판 | `BoardController` | `/board/**` |
| 배너 | `BannerController` | `/admin/banners/**` |
| 이미지 프록시 | `ImageProxyController` (패키지 2곳) | `/api/image-proxy` |
| 파일 | `FileDownloadController` | `/download/{filename}`, `/upload` |
| 액션 로그 | `UserActionLogController` | `/api/log/user-action` |
| SSL | `SslController` | `/.well-known/pki-validation/{filename}` |
| 빈 클래스 | `UserController` | `@CrossOrigin(https://wioservice.kr)` 만 존재 |

Actuator: `health`만, details 비공개.

### 7.2 아웃바운드

| 대상 | 용도 | 설정 키 (값은 문서화하지 않음) |
|------|------|--------------------------------|
| Oracle | 모든 업무 데이터 | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| 파일 서버 (`repair`로 문서화) | 교환/상담 이미지 HTTP 업로드 | `FILE_SERVER_URL`, `app.file-server.url`, `remote.upload.api-url` |
| OpenAI | AI 월간 리포트 | `OPENAI_API_KEY`, `openai.api.url` (기본 `ai.report.enabled=false`) |
| Cafe24 쇼핑몰 API | OAuth 스코프만 properties에 존재 | `WIO_CAFE24_*` — **구현 클래스 없음** |

로컬 업로드는 `file.upload-dir=uploads`, `remote.upload.method=local|http`.

---

## 8. 데이터 모델 (이 앱이 실제로 치는 테이블)

### 8.1 공유 CTI/레거시 CRM (읽기 중심)

워크스페이스 규칙·매퍼와 일치:

| 테이블 | 용도 |
|--------|------|
| `TBND01` | 상담 원장 (CUST/PROJECT/CALL_CODE, CS_TYPE, PRC_GUBN, EMG_GUBN, COMPLETION_CODE) |
| `TBND01_MEMO` | 고객사/상담 메모 |
| `TBND01_CUST` | 코드 주석에만 등장 (CTI가 생성한다고 기술) |
| `CALL_LOG_D` | 통화 로그 (CALLDATE, CLID, RESULT, CONTEXT) |
| `TPRJ01` | 프로젝트 |
| `TPER01` | 고객(담당) 연락처 |
| `TSYS01` | 시스템 코드 (`adm_gubn` 4002/4003/5000/9500) |
| `TCNT01` | 거래처 마스터·등급 |
| `TCNT01_POINT` | 직원/업체 포인트 |
| `TEMP01` | 내부 직원 |
| `LMS_LOG` | 문자 발송, `CALL_LOG_D`와 후속 통화 매칭 |

### 8.2 이 웹 포털 계정/메뉴 (N_ 접두)

| 테이블 | 용도 |
|--------|------|
| `N_TIPDW` | 로그인 ID/PW/`gubn`/`confirmYn` |
| `N_TCNT01_EMP` | 거래처 직원, CUST_CODE, authority |
| `N_TEMP01` | 메뉴 조인용 내부 직원 뷰/테이블 |
| `N_WMENU`, `N_WMENU_USER` | 동적 메뉴 |

### 8.3 이 앱이 소유하는 운영 테이블

| 테이블 | 용도 |
|--------|------|
| `TB_RETURN_ITEM` | 교환/반품 |
| `TB_SHIPPING_PAYMENT_REGISTER` | 배송비 입금 |
| `TB_LOGISTICS_DIRECT_RETURN` | 직반품 |
| `TB_SITE_MASTER` | 사이트 |
| `CONSULTING_INQUIRY` (+ HISTORY, ATTACHMENT) | 웹 문의 (CTI 원장과 별계열) |
| `BOARD_CALL` | 게시판 |
| `USER_ACTION_LOG` | UI 액션 |
| `AI_REPORT`, `AI_REPORT_SUBSCRIPTION` | AI 리포트 |
| `BANNER` | 배너 |

MyBatis XML 17개 + 일부 `@Select` 매퍼(`AiReportMapper`, `AdminCodeMapper` 등). JPA는 `ReturnItem` 등에 혼용.

---

## 9. 다른 WIO 시스템과의 관계

코드·기존 문서에서 보이는 구도:

```
[사내 2024WIO]  Edge/ActiveX CTI 상담원 클라이언트
       │  통화 종료 시 Oracle에 CALL_LOG_D / TBND01 기록
       │  이미지 업로드는 repair 파일서버로 RestTemplate
       ▼
[Oracle 11g]  공유 DB
       ▲
       │  CUST_CODE 범위 SELECT (+ 메모/완료코드 UPDATE)
       │
[이 저장소 wicrm]  브라우저 CRM 포털 (wioservice.kr)
       │
       ├── 거래처 직원: 자사 콜/상담/통계/교환 화면
       ├── 내부 직원: 승인, 배너, 전사 대시보드(/empl)
       └── 이미지: FILE_SERVER_URL → repair (문서상 :8080 /uploads, /exchange/api/upload)

[repair]  파일 서버 (별도 프로젝트, 이 repo 아님)
```

근거:

- `ConController`: “CTI 시스템에서 아직 생성되지 않음”
- `docs/troubleshooting_image_attachment.md`, `WICRM_URGENT_FIX_PLAN.md`: `2024WIO` / `wicrm` / `repair` 3원 구조, SSE·메모를 2024WIO에서 가져오려는 향후 과제
- 이 repo: ActiveX/OCX/CTI 에이전트 없음. 일반 HTTPS 웹
- `WICRM_URGENT_FIX_PLAN.md` 대상 표기: “wicrm (외부 업체용 CRM)”

카페24: 운영 URL/리다이렉트/스코프만 설정. 연동 코드는 없음.

`com.wicrm.api.ImageProxyController`는 `com.wio.crm.controller.ImageProxyController`와 동일 `@RequestMapping("/api")`를 쓴다. 빈 충돌 또는 한쪽만 등록될 수 있다.

---

## 10. 배포·실행 노트

워크스페이스 규칙: **이 환경에서 Java 컴파일/서버 기동을 하지 말 것.** 아래는 코드·기존 문서에서 읽은 내용이다.

### 10.1 로컬 (dev)

- `spring.profiles.active=dev` (기본)
- 포트 8081, SSL 꺼짐, Thymeleaf 캐시 꺼짐, 세션 120분
- JDBC는 환경변수 우선. **프로파일 파일에 기본 URL/계정 폴백이 커밋되어 있다** (비밀값 취급, 여기 적지 않음)
- 파일 서버 기본 `http://localhost:8080`
- `gradle.properties`에 Windows JDK 홈이 박혀 있다 (`C:\Program Files\Eclipse Adoptium\...`). Linux/CI에서는 무시하거나 덮어써야 한다
- `.gitignore`: `keystore.p12`, `application-prod.properties`, `gradle.properties`, `logs/`, `uploads/`, `*.bat`

### 10.2 운영 (prod, 문서·프로파일 기준)

환경변수:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (username/password는 프로파일에서 기본값 없음)
- `SSL_KEYSTORE_PASSWORD`
- `FILE_SERVER_URL`
- `OPENAI_API_KEY` (AI 사용 시)
- `WIO_CAFE24_CLIENT_ID`, `WIO_CAFE24_CLIENT_SECRET`, `WIO_CAFE24_REDIRECT_URI`

기동 형태(기존 `docs/project_plan.md`): `./gradlew clean bootJar` 후 `java -jar`를 **443에서 직접 HTTPS**. 도메인 `wioservice.kr` → 호스트로 포트 포워딩. Tomcat `x-forwarded-*` 설정이 있어 리버스 프록시도 가정한다.

세션: 60분, cookie `httpOnly` + `secure` + `SameSite=Lax`.

### 10.3 빌드 산출물

`bootJar`와 `war`(`providedRuntime` 임베디드 톰캣)가 같이 있다. 문서의 운영 절차는 JAR. 컨텍스트 패스는 `''`.

### 10.4 스케줄

`AiReportScheduler`는 `ai.report.scheduler.enabled`가 켜져 있을 때만 의미가 있다. 공통 properties 기본값은 `false`.

---

## 11. 리스크 (코드에서 관측)

우선순위는 영향 기준으로만 적는다. 수정은 이 작업 범위 밖이다.

1. **인증 없이 열리는 관리/파일 엔드포인트**  
   `/encryption`, `/encrypt-password`, `/encrypt-passwords`가 `permitAll`. 계정 비밀번호를 BCrypt로 다시 쓸 수 있다. `/upload`, `/download/**`, `/board/uploadImage`도 공개. `/project-plan/**`은 로그인 없이 태스크 JSON을 읽는다.

2. **커밋된 개발 DB 폴백**  
   `application-dev.properties`에 JDBC URL/사용자/비밀번호 기본값이 있다. prod URL 폴백도 프로파일에 남아 있다. `.gitignore`가 prod를 막아도 이미 추적 중이면 노출된다.

3. **custCode 폴백 / 빈 필터**  
   `DashboardService.getCurrentUserCustCode()`가 빈 거래처 코드에 `"P000000011"`을 넣는다. 내부 직원은 `""`를 넘겨 매퍼가 `CUST_CODE` 조건을 빼면 **전 고객사 집계**가 될 수 있다.  
   `ConService.addComment`는 요청 body의 `custCode`를 그대로 INSERT한다 (세션 값으로 덮어쓰지 않음).

4. **“실시간”의 한계**  
   대시보드 25초 폴링 + 상담 화면은 수동 조회. CTI 적재 지연과 맞물리면 고객사 화면은 즉시가 아니다. 폴링은 DB 부하를 만든다.

5. **카페24 설정만 존재**  
   Security·env는 준비됨. OAuth 컨트롤러/서비스 없음. 콜백 `permitAll`은 죽은 경로이거나 다른 배포본에만 있다.

6. **중복 `ImageProxyController`**  
   `com.wio.crm.controller`와 `com.wicrm.api`가 같은 매핑. 기동 실패 또는 어느 빈이 이겼는지 불명확.

7. **로그인 성공 후 기본 URL**  
   `defaultSuccessUrl` 없음. 북마크 없이 `/login`만 치면 `/` → 다시 로그인 페이지로 떨어질 수 있다.

8. **로그인 잠금이 프로세스 메모리**  
   인스턴스/재시작마다 초기화. 분산 배포 시 우회 가능.

9. **SslController**  
   CA 검증 문자열이 소스에 하드코딩. `/ssl-debug`는 인증만 되면 응답.

10. **문의 모듈의 더미 데이터**  
    `ConsultingController`가 `createDummyDataIfNeeded()`를 호출한다. 운영에서 테스트 행이 생길 수 있다.

11. **비밀값이 다른 문서에 남아 있음**  
    `docs/project_plan.md` 등 히스토리 문서에 키스토어 비밀번호·내부 IP가 평문으로 있다. 이 분석 문서는 재인용하지 않는다.

12. **테스트/관측 공백**  
    컨텍스트 로드 테스트만 있음. Actuator는 health만.

13. **쓰기 면적이 “뷰어”보다 큼**  
    교환/반품·입금·문의·게시판은 고객사 데이터가 이 앱에서 생성/수정된다. CTI 원장과 권한 모델이 다르다.

---

## 12. 제품 해석 (코드 기준)

WICRM는 **고객사 셀프서브 포털**이다.

- 사내 상담원이 쓰는 데스크톱 CTI가 아니다.
- 같은 Oracle CRM을 `CUST_CODE`로 잘라 보여 준다.
- 홈은 25초 폴링으로 오늘 콜/상담 지표를 갱신한다.
- `/cons`는 CTI가 쌓은 `TBND01`을 검색한다.
- 교환/반품·입금·웹 문의는 이 포털이 주인인 부가 업무다.

사내 2024WIO와는 **DB·파일 서버를 공유하는 형제 시스템**이고, UI/클라이언트 기술은 완전히 갈린다.

---

## 13. 관련 문서

- 모듈/파일 인덱스: [`MODULE_INDEX.md`](./MODULE_INDEX.md)
- 문서 목록: [`README.md`](./README.md)
- 기존 마스터 가이드: [`project_plan.md`](./project_plan.md) (일부 운영 비밀이 포함되어 있을 수 있음)
- 이미지/파일 서버: [`troubleshooting_image_attachment.md`](./troubleshooting_image_attachment.md)
- AI 리포트: [`ai_report_service.md`](./ai_report_service.md)
- 루트 `WICRM_URGENT_FIX_PLAN.md` — “외부 업체용 CRM”, 2024WIO 참조 과제
