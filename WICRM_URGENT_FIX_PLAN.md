# WICRM 긴급 수정 완료 보고서

> 작성일: 2026-02-05
> 수정 완료일: 2026-02-05
> 대상 프로젝트: wicrm (외부 업체용 CRM)
> Spring Boot: 3.2.1 / Java 17 / Oracle DB

---

## 전체 요약

| 구분 | 수정 파일 | 상태 |
|------|----------|------|
| Task 1: DB 비밀번호 환경변수화 + 환경별 설정 분리 | 3개 파일 | ✅ 완료 |
| Task 2: SQL 인젝션 방지 | 3개 매퍼 | ✅ 완료 |
| Task 3: CSRF 보호 재정비 | 2개 파일 | ✅ 완료 |
| Task 4: 하드코딩 URL 설정파일 분리 | 11개 파일 | ✅ 완료 |
| Task 5: 에러 처리 통일 + 하드코딩 제거 | 2개 파일 | ✅ 완료 |
| **합계** | **약 21개 파일** | **✅ 전체 완료** |

---

## Task 1: DB 비밀번호 환경변수화 + 환경별 설정 분리 ✅ 완료

### 수정 전 문제
```properties
# application.properties 하나에 개발/운영 설정이 주석으로 혼재
# DB 비밀번호가 소스코드에 평문으로 노출
spring.datasource.username=<REDACTED>
spring.datasource.password=<REDACTED>
# SSL 키스토어 비밀번호도 주석으로 노출: <REDACTED>
```

### 수정 내용

#### `application.properties` → 공통 설정만 유지
- Thymeleaf, MyBatis, JPA, HikariCP 등 환경 무관 설정만 유지
- `spring.profiles.active=dev` 기본값 설정
- OpenAI API Key 환경변수화: `openai.api.key=${OPENAI_API_KEY:}`

#### `application-dev.properties` (신규 생성) — 로컬 개발용
```properties
# DB: 환경변수 우선, 없으면 기본값 사용 (로컬 개발 편의)
spring.datasource.url=${DB_URL:jdbc:oracle:thin:@<HOST>:1521:<SID>}
spring.datasource.username=${DB_USERNAME:}
spring.datasource.password=${DB_PASSWORD:}

# HTTP 모드, 세션 120분, Thymeleaf 캐시 OFF, 디버그 로깅
server.port=8081
server.ssl.enabled=false

# 파일 서버
app.file-server.url=http://localhost:8080
app.file-server.upload-path=/uploads/
```

#### `application-prod.properties` (신규 생성) — 운영용
```properties
# DB: 환경변수 필수 (기본값 없음 → 설정 안하면 기동 실패)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# HTTPS, SSL 키스토어 비밀번호도 환경변수
server.port=443
server.ssl.enabled=true
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}

# 파일 서버
app.file-server.url=${FILE_SERVER_URL:http://175.119.224.45:8080}
app.file-server.upload-path=/uploads/

# 세션 60분, Thymeleaf 캐시 ON, INFO 로깅
```

### 검증 결과
- [x] 평문 비밀번호 소스코드 노출 제거
- [x] SSL 키스토어 비밀번호 환경변수화
- [x] 개발/운영 설정 명확히 분리
- [x] 운영 배포 시 환경변수 미설정 시 즉시 감지 가능

---

## Task 2: SQL 인젝션 방지 ✅ 완료

### 수정 전 문제
MyBatis `${}` (문자열 직접 치환) 사용 → SQL 인젝션 공격 가능

### 수정 내용

#### 2-1. `ReturnItemMapper.xml`
```xml
<!-- 수정 전: 위험 -->
${searchParams.sortBy} ${searchParams.sortOrder}

<!-- 수정 후: 화이트리스트 11개 컬럼 + ASC/DESC -->
<choose>
    <when test="searchParams.sortBy == 'RETURN_ID'">RETURN_ID</when>
    <when test="searchParams.sortBy == 'RETURN_DATE'">RETURN_DATE</when>
    <when test="searchParams.sortBy == 'CUSTOMER_NAME'">CUSTOMER_NAME</when>
    <when test="searchParams.sortBy == 'PRODUCT_NAME'">PRODUCT_NAME</when>
    <when test="searchParams.sortBy == 'SITE_NAME'">SITE_NAME</when>
    <when test="searchParams.sortBy == 'RETURN_TYPE_CODE'">RETURN_TYPE_CODE</when>
    <when test="searchParams.sortBy == 'ORDER_NUMBER'">ORDER_NUMBER</when>
    <when test="searchParams.sortBy == 'ORDER_DATE'">ORDER_DATE</when>
    <when test="searchParams.sortBy == 'CS_RECEIVED_DATE'">CS_RECEIVED_DATE</when>
    <when test="searchParams.sortBy == 'REFUND_AMOUNT'">REFUND_AMOUNT</when>
    <when test="searchParams.sortBy == 'IS_COMPLETED'">IS_COMPLETED</when>
    <otherwise>RETURN_ID</otherwise>
</choose>
<choose>
    <when test="searchParams.sortOrder == 'ASC'">ASC</when>
    <otherwise>DESC</otherwise>
</choose>
```

#### 2-2. `ConsultingMapper.xml` (2곳 모두 수정)
```xml
<!-- 수정 전: 위험 -->
ci.${sortField}

<!-- 수정 후: 화이트리스트 8개 컬럼 -->
<choose>
    <when test="sortField == 'created_date'">ci.created_date</when>
    <when test="sortField == 'updated_date'">ci.updated_date</when>
    <when test="sortField == 'inquiry_type'">ci.inquiry_type</when>
    <when test="sortField == 'status'">ci.status</when>
    <when test="sortField == 'customer_name'">ci.customer_name</when>
    <when test="sortField == 'phone_number'">ci.phone_number</when>
    <when test="sortField == 'order_number'">ci.order_number</when>
    <when test="sortField == 'inquiry_id'">ci.inquiry_id</when>
    <otherwise>ci.created_date</otherwise>
</choose>
```

#### 2-3. `LogisticsDirectReturnMapper.xml`
```xml
<!-- 수정 전: 위험 -->
${search.sortDirection}

<!-- 수정 후: ASC/DESC만 허용 -->
<choose>
    <when test="search.sortDirection == 'ASC'">ASC</when>
    <otherwise>DESC</otherwise>
</choose>
```

### 검증 결과
- [x] 매퍼 파일 내 `${}` 사용: **0건** (grep 확인 완료)
- [x] 허용되지 않은 정렬값 입력 시 기본값(DESC) 적용

---

## Task 3: CSRF 보호 재정비 ✅ 완료

### 수정 전 문제
거의 모든 API 경로(17개 패턴)에서 CSRF 보호 비활성화

### 수정 내용

#### 3-1. `SecurityConfig.java`

**CSRF 예외 경로: 17개 → 3개로 축소**
```java
// 수정 전: 17개 예외 경로
.csrf(csrf -> csrf.ignoringRequestMatchers(
    "/logout", "/api/**", "/download/**", "/upload",
    "/board/update", "/board/uploadImage", "/board/readBoard/comments",
    "/board/create/saveBoard", "/consulting/**", "/stat/**",
    "/return/**", "/exchange/**", "/payment/**",
    "/logistics/**", "/admin/banners/**", "/project-plan/**", "/error"
))

// 수정 후: 3개 예외 경로만 유지
.csrf(csrf -> csrf.ignoringRequestMatchers(
    new AntPathRequestMatcher("/logout"),       // Spring Security 기본
    new AntPathRequestMatcher("/error"),        // 에러 페이지
    new AntPathRequestMatcher("/api/log/**")    // 사용자 액션 로깅
).csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
```

**의존성 주입: 필드 주입 → 생성자 주입**
```java
// 수정 전
@Autowired
private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;
@Autowired
private CustomSuccessHandler customSuccessHandler;

// 수정 후
@RequiredArgsConstructor
public class SecurityConfig {
    private final AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;
    private final CustomSuccessHandler customSuccessHandler;
```

**불필요한 import 정리** (미사용 세션 관련 import 제거)

#### 3-2. `layouts/main.html` — CSRF 토큰 자동 전달 공통 스크립트 추가

jQuery 로드 직후에 삽입:
```javascript
(function() {
    var csrfToken = document.querySelector('meta[name="_csrf"]');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    if (csrfToken && csrfHeader && csrfToken.content) {
        // jQuery AJAX 전역 설정
        $.ajaxSetup({
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader.content, csrfToken.content);
            }
        });
        // fetch API 래퍼
        window.csrfFetch = function(url, options) {
            options = options || {};
            options.headers = options.headers || {};
            options.headers[csrfHeader.content] = csrfToken.content;
            return fetch(url, options);
        };
    }
})();
```

### 검증 결과
- [x] CSRF 예외 경로 17개 → 3개 축소
- [x] 모든 jQuery AJAX 요청에 CSRF 토큰 자동 포함
- [x] `window.csrfFetch()` 함수로 fetch API에서도 CSRF 지원

### 테스트 필요 항목 (배포 전)
- [ ] 게시판 글쓰기/수정/댓글
- [ ] 교환/반품 등록/수정
- [ ] 상담 문의 등록
- [ ] 배송비 입금 등록
- [ ] 물류 직접입고 등록
- [ ] 배너 관리
- [ ] 통계 조회
- [ ] 파일 업로드

---

## Task 4: 하드코딩 URL 설정파일 분리 ✅ 완료

### 수정 전 문제
`http://175.119.224.45:8080` 이 Java 5개 파일 + 프론트엔드 3개 파일에 하드코딩

### 수정 내용

#### 설정 파일에 통합 설정값 추가
```properties
# application-dev.properties
app.file-server.url=http://localhost:8080
app.file-server.upload-path=/uploads/

# application-prod.properties
app.file-server.url=${FILE_SERVER_URL:http://175.119.224.45:8080}
app.file-server.upload-path=/uploads/
```

#### Java 파일 수정 (5개)

| 파일 | 수정 내용 |
|------|----------|
| `ImageProxyController.java` (com.wio.crm) | `static final` 상수 → `@Value("${app.file-server.url}${app.file-server.upload-path}")` |
| `ImageProxyController.java` (com.wicrm.api) | 동일 |
| `ConsultingController.java` | 3곳 하드코딩 → `@Value` 필드 `fileServerUploadUrl` |
| `ConsultingService.java` | 1곳 하드코딩 → `@Value` 필드 `fileServerUploadUrl` |
| `ExchangeController.java` | `file.server-url` → `app.file-server.url` 통일 + 하드코딩 제거 |

#### 프론트엔드 파일 수정 (3개)

| 파일 | 수정 내용 |
|------|----------|
| `exchange/list.html` | Thymeleaf inline JS로 `FILE_SERVER_UPLOAD_URL` 변수 선언, 7곳 대체 |
| `exchange/form.html` | 동일 방식, 3곳 대체 |
| `consulting/debug.js` | `window.FILE_SERVER_UPLOAD_URL` 참조, 4곳 대체 |

#### 컨트롤러에서 모델 전달 (ExchangeController)
```java
// list 페이지, create 폼, edit 폼 모두에 전달
model.addAttribute("fileServerUploadUrl", fileServerUrl + "/uploads/");
```

### 검증 결과
- [x] Java 코드 내 `175.119.224.45` 하드코딩: **0건** (grep 확인 완료)
- [x] HTML 템플릿 내 하드코딩: **0건**
- [x] 남은 참조: `application-prod.properties` (설정값) + `debug.js` (디버깅 비교용) 만 존재

---

## Task 5: 에러 처리 통일 + 하드코딩 제거 ✅ 완료

### 수정 내용

#### 5-1. `BoardController.java` — printStackTrace 제거 (3곳)
```java
// 수정 전
catch (Exception ex) {
    ex.printStackTrace();
    return ResponseEntity.status(500).body("Error occurred: " + ex.getMessage());
}

// 수정 후
catch (Exception ex) {
    logger.error("게시글 저장 실패: {}", ex.getMessage(), ex);
    return ResponseEntity.status(500).body("게시글 저장 중 오류가 발생했습니다");
}
```

변경 위치:
- `saveBoard()` — 게시글 저장
- `uploadImage()` — 이미지 업로드
- `updateBoard()` — 게시글 수정

#### 5-2. `BoardController.java` — "MINWOOGI" 하드코딩 제거
```java
// 수정 전: 특정 사용자명으로 권한 판단
private boolean isAuthorizedUser(Authentication authentication) {
    return authentication != null &&
            authentication.getName().equals("MINWOOGI");
}

// 수정 후: 역할(ROLE) 기반 권한 판단
private boolean isAuthorizedUser(Authentication authentication) {
    return authentication != null &&
            authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
}
```

#### 5-3. `BoardServiceImpl.java` — "MINWOOGI" 하드코딩 제거
```java
// 수정 전: G 카테고리일 때 하드코딩 사용자명
if("G".equals(board.getCAT_GROUP())){
    board.setID("MINWOOGI");
}else{
    board.setID(userId);
}

// 수정 후: 항상 현재 로그인 사용자 ID 사용
board.setID(userId);
```

### 검증 결과
- [x] `printStackTrace` 잔존: **0건** (grep 확인 완료)
- [x] `MINWOOGI` 하드코딩: **0건** (grep 확인 완료)
- [x] 모든 에러가 `logger.error()`로 기록됨
- [x] 사용자에게 내부 에러 메시지 노출 차단

---

## 전체 수정 파일 체크리스트

### 설정 파일
- [x] `application.properties` → 공통 설정만 유지, `spring.profiles.active=dev` 기본값
- [x] `application-dev.properties` → 신규 생성 (로컬 개발용)
- [x] `application-prod.properties` → 신규 생성 (운영용, HTTPS 설정 포함)

### 보안 (Java)
- [x] `SecurityConfig.java` → CSRF 17개→3개 축소 + 생성자 주입 전환 + import 정리

### SQL 인젝션 (MyBatis XML)
- [x] `ReturnItemMapper.xml` → `${}` 제거, 화이트리스트 11개 컬럼
- [x] `ConsultingMapper.xml` → `${}` 2곳 제거, 화이트리스트 8개 컬럼
- [x] `LogisticsDirectReturnMapper.xml` → `${}` 제거, ASC/DESC 화이트리스트

### 하드코딩 URL (Java)
- [x] `ExchangeController.java` → `app.file-server.url` 통일 + 하드코딩 제거 + 모델 전달
- [x] `ImageProxyController.java` (com.wio.crm) → `@Value` 주입
- [x] `ImageProxyController.java` (com.wicrm.api) → `@Value` 주입
- [x] `ConsultingService.java` → `@Value` 주입
- [x] `ConsultingController.java` → `@Value` 주입 (3곳)

### 하드코딩 URL (프론트엔드)
- [x] `exchange/list.html` → Thymeleaf 변수 `FILE_SERVER_UPLOAD_URL` (7곳)
- [x] `exchange/form.html` → Thymeleaf 변수 `FILE_SERVER_UPLOAD_URL` (3곳)
- [x] `consulting/debug.js` → `window.FILE_SERVER_UPLOAD_URL` (4곳)

### 에러 처리 (Java)
- [x] `BoardController.java` → `printStackTrace` 3곳 → `logger.error()`
- [x] `BoardController.java` → `MINWOOGI` → `ROLE_EMPLOYEE` 권한 체크
- [x] `BoardServiceImpl.java` → `MINWOOGI` → 현재 로그인 사용자 ID

### 공통 JS (프론트엔드)
- [x] `layouts/main.html` → CSRF 토큰 자동 전달 공통 스크립트 추가

---

## 최종 검증 결과 (grep 확인)

| 검색 항목 | 결과 |
|----------|------|
| Java 코드 내 `printStackTrace` | **0건** |
| 소스 내 `MINWOOGI` | **0건** |
| MyBatis 매퍼 내 `${}` | **0건** |
| Java 코드 내 `175.119.224.45` | **0건** |

---

## 서버 접속 정보

| 프로필 | 포트 | 프로토콜 | 접속 주소 | 설정 파일 |
|--------|------|----------|-----------|-----------|
| dev (로컬 개발) | 8081 | HTTP | `http://localhost:8081` | `application-dev.properties` |
| prod (운영) | 443 | HTTPS | `https://localhost` 또는 `https://{서버IP}` | `application-prod.properties` |

### 프로필 전환 방법

```bash
# dev 프로필 (기본값)
java -jar wicrm.war --spring.profiles.active=dev

# prod 프로필
java -jar wicrm.war --spring.profiles.active=prod
```

IntelliJ 실행 시: VM options에 `-Dspring.profiles.active=dev` 또는 `prod` 추가

> **참고**: `application.properties`의 `spring.profiles.active` 값이 기본 프로필을 결정합니다. 현재 `prod`로 설정되어 있으므로, 로컬 개발 시 `dev`로 변경하거나 VM options로 오버라이드하세요.

---

## 운영 배포 가이드

### 1. 필수 환경변수 설정

```bash
# 데이터베이스
export DB_URL="jdbc:oracle:thin:@211.110.44.40:1521:o?useUnicode=true&characterEncoding=UTF-8"
export DB_USERNAME="twowincall"
export DB_PASSWORD="실제비밀번호"

# SSL (HTTPS)
export SSL_KEYSTORE_PASSWORD="실제비밀번호"

# 파일 서버
export FILE_SERVER_URL="http://175.119.224.45:8080"

# AI 리포트 (사용 시)
export OPENAI_API_KEY="sk-..."
```

### 2. 운영 프로파일로 실행
```bash
java -jar wicrm.war --spring.profiles.active=prod
```

### 3. 배포 전 테스트 체크리스트
- [ ] 로그인/로그아웃 정상 동작
- [ ] 교환/반품 목록 조회 + 정렬 (ASC/DESC)
- [ ] 교환/반품 등록/수정 (이미지 업로드 포함)
- [ ] 상담 문의 목록 + 정렬
- [ ] 상담 문의 등록 + 첨부파일
- [ ] 게시판 글쓰기/수정/댓글/이미지 업로드
- [ ] 배송비 입금 등록
- [ ] 물류 직접입고 등록
- [ ] 배너 관리 (ROLE_EMPLOYEE)
- [ ] 통계 조회 (일일/주간/월간)
- [ ] LMS 추적 목록
- [ ] 대시보드 데이터 표시
- [ ] 이미지 프록시 정상 동작

### 4. 롤백 방법
```bash
# 기본 프로파일(dev)로 복원
java -jar wicrm.war
# 또는 명시적으로
java -jar wicrm.war --spring.profiles.active=dev
```

---

## 추가 수정 사항 (2026-02-05 이후)

### Task 6: 상담 조회 화면 디자인 변경 ✅ 완료 (2026-02-05)

#### 변경 이유
- 기존 테이블 기반 목록이 정보 밀도가 높아 눈에 잘 안 들어옴

#### 수정 파일 (6개)
| 파일 | 변경 내용 |
|------|----------|
| `static/consulting/list.css` (신규) | 카드 기반 레이아웃, WIO 디자인 시스템 색상 변수 적용 |
| `templates/consulting/list.html` | 테이블 → 카드 기반 레이아웃, 상태 탭 필터, 날짜 범위 검색, 플로팅 액션바 |
| `controller/ConsultingController.java` | `startDate`, `endDate` 파라미터 추가 |
| `service/ConsultingService.java` | 날짜 범위 필터 지원, `getStatusCounts` 오버로드 |
| `mapper/ConsultingMapper.xml` | `startDate`, `endDate` WHERE 조건 추가 |

---

### Task 7: 교환/반품 이미지 업로드/삭제 수정 ✅ 완료 (2026-02-05)

#### 수정 내용

**1. 이미지 업로드 URL 수정**
| 구분 | 이전 (잘못됨) | 수정 (올바름) |
|------|-------------|-------------|
| 파일 업로드 | `fileServerUrl + "/api/exchange/upload-image"` | `fileServerUrl + "/exchange/api/upload"` |
| Base64 업로드 | `fileServerUrl + "/api/exchange/upload-image"` | `fileServerUrl + "/exchange/api/upload"` |

> **파일서버는 `repair` 프로젝트** (`http://175.119.224.45:8080`)이며, 엔드포인트는 `/exchange/api/upload`

**2. 이미지 삭제 기능 추가**
| 파일 | 변경 내용 |
|------|----------|
| `controller/ExchangeController.java` | `@PostMapping("/api/delete-image/{itemId}")` 추가 (DELETE → POST 변경) |
| `templates/exchange/list.html` | JS `fetch` 메서드 `DELETE` → `POST` 변경 |
| `templates/exchange/form.html` | "🗑️ 이미지 삭제" 버튼 및 `deleteExistingImage()` 함수 추가 |

---

## 향후 개선 과제 (다음 단계)

| 우선순위 | 과제 | 설명 |
|---------|------|------|
| 높음 | Spring Boot 업그레이드 | 3.2.1 → 3.5.x (보안 패치, 성능 개선) |
| 높음 | 나머지 @Autowired 전환 | 40개 클래스 필드 주입 → 생성자 주입 |
| 높음 | SSE 실시간 알림 도입 | 상담 처리 상태 실시간 알림 (2024WIO 참조) |
| **높음** | **게시판 파일 → repair 파일서버 연동** | **로컬 저장 → repair 서버 포워딩으로 통합** |
| 중간 | MyBatis/JPA 통일 | ReturnItem 관련 혼용 정리 |
| 중간 | N+1 쿼리 최적화 | ConsMapper.xml 서브쿼리 → JOIN 전환 |
| 중간 | 상담 메모 시스템 | WIO↔WICRM 실시간 소통 (2024WIO 참조) |
| 낮음 | UI/UX 리뉴얼 | WIO 디자인 시스템 적용 |
| 낮음 | 테스트 코드 작성 | 핵심 서비스 단위 테스트 |
| 낮음 | API 문서화 | Swagger/OpenAPI 도입 |

---

### 게시판 파일 → repair 파일서버 연동 상세 (2026-02-20 분석)

#### 현재 상태

게시판 파일은 wicrm **로컬 서버**에 저장/서빙되고 있음. 교환/반품 이미지는 repair 파일서버를 사용하므로 **두 가지 방식이 혼재**.

| 구분 | 업로드 | 다운로드 | 저장 위치 |
|------|--------|---------|----------|
| 교환/반품 이미지 | RestTemplate → repair | `http://175.119.224.45:8080/uploads/...` | repair 서버 |
| 게시판 파일 | `Files.copy` → 로컬 | `/download/{filename}`, `/uploads/{filename}` | wicrm 로컬 `uploads/` |

#### repair 서버 제약사항 (연동 전 해결 필요)

1. **파일 형식**: 현재 이미지만 허용 (JPG, PNG, GIF, WebP) → PDF, DOC 등 일반 파일 허용 필요
2. **IP 제한**: `/exchange/api/upload`는 `175.126.176.206`만 허용 → wicrm 서버 IP 추가 필요
3. **파일 크기**: 10MB 제한 (게시판 파일에 충분한지 검토)

#### 수정 대상 (repair 서버)

| 파일 | 수정 내용 |
|------|----------|
| `ExchangeController.java` | `isImageFile()` 검증 확장 또는 범용 업로드 API (`/api/board/upload`) 신규 추가 |
| `SecurityConfig.java` | wicrm 서버 IP 접근 허용 추가 |
| `WebConfig.java` | 일반 파일 저장 경로 추가 (예: `/uploads/board/`) |

#### 수정 대상 (wicrm 서버)

| 파일 | 수정 내용 |
|------|----------|
| `BoardController.java` | `saveBoard()`, `uploadImage()`, `updateBoard()` → RestTemplate 포워딩 |
| `FileDownloadController.java` | repair 서버 URL로 리다이렉트 또는 프록시 |
| `WebMvcConfig.java` | 로컬 `/uploads/**` 리소스 핸들러 제거 (게시판 파일 로컬 서빙 중단) |
| `readBoard.html` | 다운로드 링크를 repair 서버 URL로 변경 |
| `createBoard.html` | 파일 업로드 JS 수정 (필요 시) |

> **상세 문서**: `docs/troubleshooting_image_attachment.md` 참조
