# 📎 첨부파일 이미지 문제 해결 가이드

> **문서 정보**
> - 작성일: 2025-04-22
> - 최종 업데이트: 2026-02-20
> - 관련 기능: 상담 문의, 교환/반품, 게시판 파일 첨부

---

## 🔍 문제 상황

상담 문의 상세(consulting/detail) 페이지에서 첨부파일 이미지가 표시되지 않는 문제

### 주요 증상
- 이미지 경로가 `http://175.119.224.45:8080/uploads/null`로 표시
- 404 Not Found 오류 발생
- DB에서 첨부파일 정보는 정상 조회되지만 이미지가 표시되지 않음

---

## 🎯 원인 분석

### 1. Oracle DB 컬럼명 대소문자 문제
- Oracle DB에서 컬럼명이 대문자로 반환됨 (`FILE_PATH`)
- Java 코드에서는 소문자로 접근 (`file_path`)
- Thymeleaf 대소문자 구분으로 인해 null 처리

### 2. 이미지 URL 구성 문제
- DB에 저장된 `file_path`: `images/파일명.jpg`
- 서버 기본 URL: `http://175.119.224.45:8080/uploads/`
- 전체 URL 구성 필요: 서버URL + file_path

---

## ✅ 해결 방법

### 1. MyBatis Mapper 수정 (명시적 별칭)

```xml
<select id="getAttachmentsByInquiryId" resultType="map">
    SELECT 
        attachment_id as attachment_id,
        inquiry_id as inquiry_id,
        file_name as file_name,
        file_path as file_path,
        file_type as file_type,
        file_size as file_size,
        CASE WHEN source = 'MAIN_IMAGE' THEN 1 ELSE 0 END as is_main_image,
        created_date as upload_date
    FROM 
        CONSULTING_INQUIRY_ATTACHMENT
    WHERE 
        inquiry_id = #{inquiryId}
    ORDER BY 
        CASE WHEN source = 'MAIN_IMAGE' THEN 0 ELSE 1 END, created_date DESC
</select>
```

### 2. Service 레이어 - 대소문자 통합 처리

```java
// 첨부파일 조회 후 대소문자 키 통합
for (Map<String, Object> attachment : attachments) {
    // 대소문자 키 통합
    for (String key : new String[]{"file_path", "FILE_PATH"}) {
        if (attachment.containsKey(key)) {
            String filePath = (String) attachment.get(key);
            if (filePath != null) {
                attachment.put("file_path", filePath);
            }
        }
    }
    
    // 전체 URL 구성
    if (attachment.containsKey("file_path") && attachment.get("file_path") != null) {
        String filePath = (String) attachment.get("file_path");
        String serverUrl = "http://175.119.224.45:8080/uploads/";
        attachment.put("full_url", serverUrl + filePath);
    }
}
```

### 3. Thymeleaf 템플릿 - 이미지 태그

```html
<!-- 이미지 태그 (조건부 렌더링) -->
<img th:if="${attachment['file_path'] != null}" 
     th:src="@{|http://175.119.224.45:8080/uploads/${attachment['file_path']}|}"
     th:alt="${attachment['file_name'] != null ? attachment['file_name'] : '첨부파일 이미지'}"
     class="attachment-img img-fluid rounded"
     onerror="this.onerror=null; this.src='https://via.placeholder.com/150';" />

<!-- 파일 경로가 없는 경우 오류 표시 -->
<div th:if="${attachment['file_path'] == null}" class="alert alert-danger text-center p-2 mt-2">
    <i class="ki-duotone ki-warning-2 fs-2x mb-2"></i>
    <p>파일 경로 정보가 없습니다.</p>
</div>
```

### 4. JavaScript - null 체크

```javascript
function showImageFullScreen(filePath, fileName) {
    // 파일 경로 null 처리
    if (!filePath) {
        console.error('파일 경로가 없습니다.');
        Swal.fire({
            title: '오류',
            text: '이미지를 표시할 수 없습니다. 파일 경로 정보가 없습니다.',
            icon: 'error',
            confirmButtonText: '확인'
        });
        return;
    }
    
    // 이미지 URL 생성
    var imageUrl = 'http://175.119.224.45:8080/uploads/' + filePath;
    
    // 모달 표시 로직...
}
```

---

## 📝 핵심 포인트

| 항목 | 설명 |
|------|------|
| 서버 URL | `http://175.119.224.45:8080/uploads/` |
| DB 저장 형식 | `images/파일명.jpg` |
| 전체 URL | 서버URL + file_path |
| 별칭 지정 | MyBatis에서 컬럼명 소문자 별칭 필수 |
| null 체크 | 모든 레이어에서 null 검증 필수 |

---

## 🔧 디버깅 방법

```javascript
// 이미지 로드 상태 확인
<img src="..." 
     onload="console.log('이미지 로드 성공:', this.src);"
     onerror="console.error('이미지 로드 실패:', this.src);">
```

---

## 🔄 교환/반품 이미지 업로드/삭제 수정 (2026-02-05)

### 아키텍처

```
[파일서버 = repair 프로젝트 = http://175.119.224.45:8080]
  ├── POST /exchange/api/upload         → ./uploads/images/ 저장
  └── GET  /uploads/images/{파일명}      → 이미지 서빙

[wicrm]  → RestTemplate → http://175.119.224.45:8080/exchange/api/upload
[2024WIO] → RestTemplate → http://175.119.224.45:8080/exchange/api/upload
```

### 수정 내역

| 파일 | 변경 |
|------|------|
| `ExchangeController.java` | 업로드 URL: `/exchange/api/upload` (repair 파일서버) |
| `ExchangeController.java` | 이미지 삭제: `@PostMapping("/api/delete-image/{itemId}")` 추가 |
| `exchange/list.html` | 삭제 JS: `method: 'DELETE'` → `method: 'POST'` |
| `exchange/form.html` | "🗑️ 이미지 삭제" 버튼 + `deleteExistingImage()` JS 추가 |

### 주의사항
- 파일서버 URL은 `app.file-server.url` 프로퍼티로 관리
- 업로드 API 엔드포인트: `/exchange/api/upload` (**`/api/exchange/upload-image` 아님!**)
- 이미지 표시: `http://175.119.224.45:8080/uploads/` + relativePath

---

## 🏗️ 전체 파일 서버 아키텍처 (2026-02-20)

3개 프로젝트(2024WIO, wicrm, repair)의 파일 업로드/다운로드 흐름 전체 정리.

### 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────────┐
│                    repair 파일서버 (http://175.119.224.45:8080)      │
│                                                                     │
│  [업로드 API]                                                       │
│  POST /exchange/api/upload (MultipartFile)                          │
│    → ./uploads/images/{UUID}_{timestamp}.{ext} 저장                 │
│    → 응답: { relativePath, fullUrl, filename, ... }                 │
│                                                                     │
│  [정적 파일 서빙]                                                    │
│  GET /uploads/images/{파일명}                                        │
│    → WebConfig: /uploads/** → file:./uploads/                       │
└─────────────────────────────────────────────────────────────────────┘
        ▲ RestTemplate                    ▲ RestTemplate
        │ POST /exchange/api/upload       │ POST /exchange/api/upload
        │                                 │
┌───────┴──────────┐            ┌─────────┴────────────┐
│   wicrm 서버      │            │   2024WIO 서버        │
│   (외부 업체용)    │            │   (내부 인하우스)       │
│                   │            │                       │
│ [교환/반품 이미지]  │            │ [교환/반품 이미지]      │
│  ExchangeCtrl     │            │  ExchangeCtrl         │
│  → repair 포워딩   │            │  → repair 포워딩       │
│                   │            │                       │
│ [게시판 파일] ⚠️   │            │                       │
│  BoardCtrl        │            │                       │
│  → wicrm 로컬 저장 │            │                       │
│  → uploads/ 디렉토리│            │                       │
└───────────────────┘            └───────────────────────┘
```

### 기능별 파일 처리 방식

| 프로젝트 | 기능 | 업로드 대상 | 다운로드 URL | 비고 |
|----------|------|------------|-------------|------|
| 2024WIO | 교환/반품 이미지 | repair 서버 (`RestTemplate` 포워딩) | `http://175.119.224.45:8080/uploads/images/{파일명}` | `app.file-server.url` 프로퍼티 사용 |
| wicrm | 교환/반품 이미지 | repair 서버 (`RestTemplate` 포워딩) | `http://175.119.224.45:8080/uploads/images/{파일명}` | `app.file-server.url` 프로퍼티 사용 |
| wicrm | 게시판 파일 첨부 | **wicrm 로컬** (`Files.copy`) | `/uploads/{파일명}` 또는 `/download/{파일명}` | 로컬 `uploads/` 디렉토리 |
| wicrm | 상담 첨부 이미지 | repair 서버 | `http://175.119.224.45:8080/uploads/images/{파일명}` | 상담 등록 시 이미지 |
| repair | 자체 이미지 | repair 로컬 (`./uploads/images/`) | `/uploads/images/{파일명}` | 파일서버 본체 |

---

## 🔒 repair 파일서버 제약사항 (2026-02-20)

### 파일 형식 제한

현재 **이미지 파일만** 업로드 허용. 일반 파일(PDF, DOC, Excel 등) 미지원.

허용 MIME 타입 (`ExchangeController.isImageFile()`):
- `image/jpeg` / `image/jpg`
- `image/png`
- `image/gif`
- `image/webp`

### 파일 크기 제한

- 단일 파일: **10MB**
- 요청 전체: **15MB**

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB
```

### 보안 설정 (SecurityConfig.java)

| 엔드포인트 | 접근 제한 | 비고 |
|-----------|----------|------|
| `POST /exchange/api/upload` | IP 제한: `175.126.176.206` | 운영 서버 IP만 허용 |
| `POST /api/upload` | 공개 (제한 없음) | 범용 업로드 API |
| `GET /uploads/**` | 공개 | 정적 파일 서빙 |

### CORS 설정 (CorsConfig.java)

- 모든 Origin 허용 (`*`)
- 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE, OPTIONS)
- 자격 증명(Credentials) 허용
- 캐시 시간: 1시간 (3600초)

### 파일 저장 경로

- 기본 디렉토리: `./uploads/`
- 이미지 저장: `./uploads/images/{UUID}_{yyyyMMddHHmmss}.{ext}`
- QA 이미지: `./uploads/qa-images/`

---

## 📁 wicrm 게시판 파일 첨부 현황 (2026-02-20)

게시판 파일은 교환/반품 이미지와 달리 **wicrm 서버 로컬**에 저장됩니다.

### 업로드 흐름

```
[사용자 브라우저]
  ↓ POST /board/create/saveBoard (files: MultipartFile[])
  ↓ POST /board/uploadImage (file: MultipartFile)  ← 에디터 이미지
[wicrm BoardController]
  → Files.copy(file, uploadPath.resolve(fileName))
  → 로컬 uploads/ 디렉토리에 직접 저장
  → DB에 파일명 저장 (세미콜론 구분)
```

### 다운로드 흐름

```
[사용자 브라우저]
  ↓ GET /download/{filename}
[wicrm FileDownloadController]
  → Paths.get(uploadDir).resolve(filename)
  → 로컬 파일 읽어서 응답 (Content-Disposition: attachment)

[이미지 인라인 표시]
  ↓ GET /uploads/{filename}
[wicrm WebMvcConfig]
  → addResourceHandler("/uploads/**") → file:uploads/
```

### 관련 파일

| 파일 | 역할 |
|------|------|
| `BoardController.java` | `saveBoard()`, `uploadImage()`, `updateBoard()` - 로컬 파일 저장 |
| `FileDownloadController.java` | `downloadFile()` - 로컬 파일 다운로드 (attachment) |
| `WebMvcConfig.java` | `/uploads/**` → `file:uploads/` 리소스 핸들러 |
| `templates/board/readBoard.html` | `@{/download/{filename}}` 다운로드 링크 |
| `templates/board/createBoard.html` | `<input type="file" name="files" multiple>` 업로드 UI |

### 설정 (application-*.properties)

```properties
# 공통
file.upload-dir=uploads

# dev
app.file-server.url=http://localhost:8080
remote.upload.method=local

# prod
app.file-server.url=${FILE_SERVER_URL:http://175.119.224.45:8080}
remote.upload.method=http
```

### 주의사항

- 게시판 파일명은 **원본 파일명 그대로** 저장 (UUID 미사용) → 동명 파일 덮어쓰기 위험
- 파일 타입 제한 없음 (이미지, PDF, DOC 등 모두 가능)
- 파일 크기 제한: `spring.servlet.multipart.max-file-size=10MB` (공통 설정)

### 외부 파일서버(repair) 연동 시 필요한 수정사항

게시판 파일도 repair 파일서버를 사용하려면 아래 수정이 필요합니다:

**repair 서버 수정:**
1. `ExchangeController.isImageFile()` 검증 확장 또는 범용 파일 업로드 API 신규 추가
2. `SecurityConfig.java`에서 wicrm 서버 IP 접근 허용 추가
3. 일반 파일 저장 경로 분리 (예: `./uploads/board/`)

**wicrm 서버 수정:**
1. `BoardController.java` - `saveBoard()`, `uploadImage()`, `updateBoard()` → RestTemplate 포워딩으로 변경
2. `FileDownloadController.java` - repair 서버로 리다이렉트 또는 프록시 방식 변경
3. `WebMvcConfig.java` - 로컬 `/uploads/**` 리소스 핸들러 제거
4. `readBoard.html` - 다운로드 링크를 repair 서버 URL로 변경

---

**📌 관련 파일:**
- `ConsultingService.java`
- `ConsultingMapper.xml`
- `detail.html` (상담 상세 템플릿)
- `ExchangeController.java` (교환/반품 이미지)
- `exchange/form.html`, `exchange/list.html`
- `BoardController.java` (게시판 파일 첨부)
- `FileDownloadController.java` (게시판 파일 다운로드)
- `WebMvcConfig.java` (정적 리소스 핸들러)

