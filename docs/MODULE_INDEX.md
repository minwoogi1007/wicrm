# WICRM 모듈 인덱스

소스 위치를 찾기 위한 목록. 아키텍처 서술은 [`SOURCE_ANALYSIS.md`](./SOURCE_ANALYSIS.md).

패키지 루트: `src/main/java/com/wio/crm` (별도 `com.wicrm.api` 1파일).

---

## 부트·설정

| 파일 | 역할 |
|------|------|
| `CrmApplication.java` | Spring Boot 진입, 스케줄/트랜잭션 |
| `config/SecurityConfig.java` | 인증 규칙, CSRF, 세션, 헤더 |
| `config/CustomUserDetails.java` | 내부(`Temp01`) / 거래처(`Tcnt01Emp`) principal |
| `config/CustomSuccessHandler.java` | 로그인 성공 → 세션 메뉴·custCode |
| `config/AjaxAuthenticationFailureHandler.java` | 로그인 실패·시도 잠금 |
| `config/CustomAuthenticationFailureHandler.java` | 레거시 JSON 실패 핸들러 (체인에 미연결로 보임) |
| `config/WebMvcConfig.java` | 배너 인터셉터, 로컬 업로드 정적 매핑 |
| `config/BannerInterceptor.java` | 배너 모델 주입 |
| `config/WebClientConfig.java` | `WebClient.Builder` |
| `config/MybatisConfig.java` | MyBatis |
| `config/ImageCompressUtil.java` | 이미지 압축 |

리소스: `application.properties`, `application-dev.properties`, `application-prod.properties`, `mybatis-config.xml`, `logback-spring.xml`.

---

## 인증·계정

| 파일 | 역할 |
|------|------|
| `controller/LoginController.java` | `/`, `/login`, 아이디 중복, 가입 신청 |
| `controller/AccountController.java` | 내 정보·비밀번호 |
| `controller/UserApprovalListController.java` | 가입 승인 |
| `controller/EncryptionController.java` | 비밀번호 재해시 UI (**permitAll**) |
| `controller/UserController.java` | 빈 `@RestController` + CORS |
| `service/CustomUserDetailsService.java` | `gubn` 분기 로드 |
| `service/LoginService.java` | 신청 처리 |
| `service/LoginAttemptService.java` | 메모리 잠금 |
| `service/LoginHistoryService.java` | 로그인 이력 |
| `service/PasswordEncryptionService.java` | BCrypt 일괄/단건 |
| `service/UserApprovalService.java` | 승인·업체 매핑 |
| `service/AccountService.java` | 프로필 |
| `mapper/TipdwMapper.java` + XML | `N_TIPDW` |
| `mapper/Tcnt01EmpMapper.java` + XML | `N_TCNT01_EMP` |
| `mapper/Temp01Mapper.java` | 내부 직원 |
| `mapper/AccountMapper.xml` | 계정 수정 |
| `mapper/UserApprovalMapper.xml` | 승인 |
| `mapper/LoginHistoryMapper.java` | 이력 |
| `templates/sign-in.html` | 실제 로그인 화면 |

---

## 메뉴·쉘

| 파일 | 역할 |
|------|------|
| `controller/MainController.java` | `/main` |
| `service/MenuService.java` | 권한별 메뉴 |
| `mapper/MenuMapper.java` + `menuMapper.xml` | `N_WMENU*` |
| `templates/main.html`, `layouts/main.html` | 레이아웃 |
| `templates/fragments/sidebar.html` | `USER_MENUS` → DOM |
| `templates/contents.html` | 대시보드 마크업 |
| `static/assets/js/common/dashboard.js` | 25초 폴링 |

---

## 대시보드 (실시간 조회)

| 파일 | 역할 |
|------|------|
| `controller/DashboardController.java` | `/api/dashboard-*`, `/empl/dashboard-*` |
| `service/DashboardService.java` | custCode 스코프, 카드/차트 조립 |
| `mapper/DashboardMapper.java` + XML | `CALL_LOG_D`, `TBND01`, `TCNT01_POINT` |
| `model/DashboardData.java` | 카드 DTO |

---

## CTI 상담 원장 (고객사 CRM 뷰)

| 파일 | 역할 |
|------|------|
| `controller/ConController.java` | `/cons`, `/api/consultations*` |
| `service/ConService.java` | 목록/상세/메모/완료코드 |
| `mapper/ConsMapper.java` + XML | `TBND01`, `TBND01_MEMO`, `TPER01`, `TPRJ01` |
| `model/Consultation.java`, `Comment.java`, `History.java`, `CompletionCode.java` | |
| `templates/cons/consulting.html` | 상담 그리드 |
| `service/AdminCodeService.java` | `TSYS01` 코드 |
| `Entity/AdminCode.java`, `mapper/AdminCodeMapper.java` | |

---

## 운영 통계

| 파일 | 역할 |
|------|------|
| `controller/StatController.java` | `/stat/*`, `/statCons`, `/statResult`, `/statTime` |
| `service/StatisticsService.java` | 일/주/월·그래프 |
| `mapper/StaticsMapper.xml` | `CALL_LOG_D` + `TBND01` 집계 |
| `dto/Daily*`, `Weekly*`, `Monthly*`, `HourlyCallsDataDto`, `YearlyComparisonDataDto` | |
| `templates/statistics/*` | |
| `static/assets/js/stat/*` | |

---

## 웹 문의 (`CONSULTING_INQUIRY`, CTI 원장과 별개)

| 파일 | 역할 |
|------|------|
| `controller/ConsultingController.java` | `/consulting/list|add|detail` |
| `service/ConsultingService.java` | 페이징, 더미 데이터 생성 가능 |
| `mapper/ConsultingMapper.xml` | inquiry / history / attachment |
| `model/ConsultingInquiry.java` | |
| `templates/consulting/*` | |

---

## 교환/반품·물류·입금

| 파일 | 역할 |
|------|------|
| `controller/ExchangeController.java` | `/exchange/**` (목록, 저장, 엑셀, 이미지, realtime-stats) |
| `controller/ExchangeStatsController.java` | `/exchange/stats` |
| `controller/PaymentController.java` | `/payment/**` |
| `controller/LogisticsDirectReturnController.java` | `/logistics/direct-return/**` |
| `service/ReturnItemService.java` + `impl/` | |
| `service/ExchangeStatsService.java` + `impl/` | |
| `service/ShippingPaymentService.java` + `impl/` | |
| `service/LogisticsDirectReturnService.java` | |
| `mapper/ReturnItemMapper.xml`, `ExchangeStatsMapper.xml`, `ShippingPaymentMapper.xml`, `LogisticsDirectReturnMapper.xml` | |
| `model/ReturnItem.java`, `repository/ReturnItemRepository.java` | JPA+MyBatis 혼용 |
| `templates/exchange/*`, `payment/*`, `logistics/*` | |

---

## LMS·주문·마일리지

| 파일 | 역할 |
|------|------|
| `controller/LmsTrackingController.java` | `/lms-tracking` |
| `service/LmsTrackingService.java` | LMS ↔ `CALL_LOG_D` |
| `mapper/LmsTrackingMapper.xml` | `LMS_LOG` |
| `controller/OrderController.java` | 엑셀 주문/미송장 |
| `service/OrderService.java`, `InvoiceService.java` | |
| `mapper/OrderMapper.xml`, `InvoiceMapper.xml` | |
| `controller/MileageController.java` | 포인트 현황·차감 |
| `service/MileageService.java` | `TCNT01_POINT` |
| `mapper/MileageMapper.xml` | |

---

## 게시판·배너·AI

| 파일 | 역할 |
|------|------|
| `controller/BoardController.java` | `/board/**` |
| `service/BoardService.java` + `BoardServiceImpl.java` | `CAT_GROUP = CUST_CODE` |
| `mapper/BoardMapper.xml` | `BOARD_CALL` |
| `controller/BannerController.java` | `/admin/banners` (`ROLE_EMPLOYEE`) |
| `service/BannerService.java` | |
| `controller/AiReportController.java` | `/ai-report/**` |
| `service/AiReportService.java`, `AiAnalysisService.java` | OpenAI |
| `scheduler/AiReportScheduler.java` | 기본 비활성 |
| `mapper/AiReportMapper.java` | |

---

## 파일·프록시·기타

| 파일 | 역할 |
|------|------|
| `controller/FileDownloadController.java` | `/upload`, `/download/**` (permitAll) |
| `controller/ImageProxyController.java` | `/api/image-proxy` → 파일 서버 |
| `com.wicrm.api.ImageProxyController` | 동일 매핑 복제 |
| `controller/SslController.java` | Cafe24 SSL 검증 파일 |
| `controller/ProjectPlanController.java` | `/project-plan` 공개 모니터 |
| `controller/DesignSampleController.java` | `/design-samples` 공개 |
| `controller/UserActionLogController.java` | `/api/log/user-action` |
| `service/UserActionLogService.java` | |
| `exception/GlobalExceptionHandler.java` | |
| `exception/UserNotConfirmedException.java` | 미승인 계정 |

---

## MyBatis XML (`src/main/resources/mapper/`)

`Account`, `Board`, `Cons`, `Consulting`, `Dashboard`, `ExchangeStats`, `Invoice`, `LmsTracking`, `LogisticsDirectReturn`, `menu`, `Mileage`, `Order`, `ReturnItem`, `ShippingPayment`, `Statics`, `Tcnt01Emp`, `UserApproval`.

인터페이스만 있고 XML이 없거나 어노테이션 SQL인 것: `AdminCodeMapper`, `AiReportMapper`, `BannerMapper`, `LoginHistoryMapper`, `Temp01Mapper`, `TipdwMapper`, `UserMapper` 등.

---

## 화면 템플릿 (`src/main/resources/templates/`)

업무: `contents`, `cons`, `consulting`, `statistics`, `exchange`, `payment`, `logistics`, `lms-tracking`, `board`, `account`, `user`, `mileage`, `order`, `ai-report`, `admin/banners`.

쉘: `main`, `layouts/main`, `fragments/*`, `sign-in`.

비업무/샘플: `design-samples`, `sample`, `encrypt`, `shopping`, `activities`.

에러: `error/404`, `500`, `general`.

---

## 프론트 JS (업무)

| 경로 | 역할 |
|------|------|
| `static/assets/js/common/dashboard.js` | 대시보드 AJAX·25초 폴링 |
| `static/assets/js/stat/statCons.js` 등 | 통계 차트 |
| `static/js/consulting/*` | 문의 화면 보조 |

테마 번들(`plugins.bundle.js`, `widgets.bundle.js`, `style.bundle.css`)은 Metronic/Keen 벤더 자산.

---

## 이 repo 밖 (참조만)

| 이름 | 관계 |
|------|------|
| 2024WIO | 사내 CTI 클라이언트. 동일 Oracle 상담/콜 테이블에 기록. 이 소스에 없음 |
| repair | 파일 서버. `FILE_SERVER_URL`로 업로드/정적 이미지 |
| Cafe24 | OAuth 설정만 존재, 구현 없음 |
