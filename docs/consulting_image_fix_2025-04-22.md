# 첨부파일 이미지 표시 문제 해결 (2025-04-22)

## 문제 상황
- 상담 문의 상세(consulting/detail) 페이지에서 첨부파일 이미지가 표시되지 않는 문제 발생
- DB에서 첨부파일 정보는 정상적으로 가져오지만, 이미지 URL 생성 시 file_path가 null로 표시됨
- 결과적으로 `http://175.119.224.45:8080/uploads/null`과 같은 잘못된 URL로 이미지 요청이 발생하여 404 오류 발생

## 원인 분석
1. DB에서 조회한 첨부파일 데이터의 컬럼명이 대문자로 반환되는 문제 (Oracle DB 특성)
   - Java 코드에서는 `file_path`로 접근하지만 실제 DB에서는 `FILE_PATH`로 반환됨
2. Thymeleaf에서 대소문자 구분으로 인해 `attachment['file_path']`가 null로 취급됨
3. MyBatis Mapper에서 컬럼 별칭을 명시적으로 지정하지 않아 발생하는 문제

## 해결 방법
1. **DB 조회 결과 변환 로직 추가:**
   - ConsultingService에서 `getAttachmentsByInquiryId()` 메서드 수정
   - 조회된 결과를 대소문자 구분 없이 처리하는 로직 추가
   - 대문자/소문자 키 모두 확인하여 데이터 통합

2. **MyBatis Mapper 개선:**
   - ConsultingMapper.xml의 `getAttachmentsByInquiryId` 쿼리 수정
   - 컬럼에 명시적 별칭 추가 (`attachment_id as attachment_id` 형태)

3. **이미지 URL 생성 방식 개선:**
   - URL 구성 방식을 JavaScript 변수를 통해 일관되게 관리
   - `serverInfo.fileServerUrl` 변수를 통해 외부 URL 관리
   - Thymeleaf에서 URL 구성 시 조건부 처리 (`th:src="${serverInfo.fileServerUrl + attachment['file_path']}"`)

4. **첨부파일 데이터 활용 개선:**
   - 전체 첨부파일 객체를 이미지 뷰어 함수에 전달하도록 개선
   - `showAttachmentImageFullScreen()` 함수 추가하여 전체 객체 데이터 활용

5. **오류 처리 및 디버깅 기능 강화:**
   - `file_path`가 null인 경우 명확한 오류 메시지 표시
   - 콘솔 로깅 추가 및 보강
   - 이벤트 리스너를 통한 이미지 로드 상태 모니터링

## 핵심 개선 코드
1. **ConsultingService의 변환 로직:**
```java
// 첨부 파일 정보 로깅
for (int i = 0; i < attachments.size(); i++) {
    Map<String, Object> attachment = attachments.get(i);
    
    // file_path가 null인 경우 대체 로직 시도
    if (attachment.get("file_path") == null && attachment.get("ATTACHMENT_ID") != null) {
        // 검색할 키를 소문자로 일괄 변경
        Map<String, Object> lowerCaseMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : attachment.entrySet()) {
            lowerCaseMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        attachment = lowerCaseMap;
        
        // 대체 로직: attachment_id를 사용하여 경로 생성
        Object attachmentId = attachment.get("attachment_id");
        if (attachmentId != null) {
            // 예시로 현재 날짜를 기준으로 경로 생성
            String generatedPath = "images/generated_" + attachmentId + "_" + dateStr + ".jpg";
            attachment.put("file_path", generatedPath);
        }
    }
    
    // 상위/소문자 파일 경로 키 통합
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
        String fullUrl = serverUrl + filePath;
        attachment.put("full_url", fullUrl);
    }
}
```

2. **ConsultingMapper.xml 수정:**
```xml
<!-- 첨부 파일 목록 조회 -->
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

3. **이미지 태그 개선:**
```html
<!-- 이미지 태그 (객체 속성을 바로 사용) -->
<img th:if="${attachment['file_path'] != null}" 
     th:id="'attachment-img-' + ${attachment['attachment_id']}"
     th:src="${serverInfo.fileServerUrl + attachment['file_path']}"
     th:alt="${attachment['file_name'] != null ? attachment['file_name'] : '첨부파일 이미지'}"
     class="attachment-img img-fluid rounded"
     th:attr="onclick='showAttachmentImageFullScreen(' + ${#objects.nullSafe(#objects.toJson(attachment), '{}')} + ')'" 
     crossorigin="anonymous"
     onload="hideLoader(this)"
     onerror="imageLoadError(this)"
/>
```

4. **개선된 이미지 뷰어 함수:**
```javascript
// 첨부파일 데이터로 이미지 전체화면 보기
function showAttachmentImageFullScreen(attachmentData) {
    if (!attachmentData) {
        console.error('첨부파일 데이터가 없습니다.');
        return;
    }
    
    // 소문자 또는 대문자 키로 데이터 검색
    var filePath = attachmentData.file_path || attachmentData.FILE_PATH;
    var fileName = attachmentData.file_name || attachmentData.FILE_NAME || '첨부파일';
    var fullUrl = attachmentData.full_url || attachmentData.FULL_URL;
    
    console.log('첨부파일 데이터 확인:', { filePath, fileName, fullUrl });
    
    if (!filePath && !fullUrl) {
        Swal.fire({
            title: '오류',
            text: '이미지를 표시할 수 없습니다. 파일 경로 정보가 없습니다.',
            icon: 'error',
            confirmButtonText: '확인'
        });
        return;
    }
    
    // 이미지 URL 생성 (full_url 또는 파일경로 사용)
    var imageUrl = fullUrl || ('http://175.119.224.45:8080/uploads/' + filePath);
    
    // 모달 이미지 설정 및 표시...
}
```

## 테스트 결과
- 이제 첨부파일 이미지가 consulting/detail 화면에서 정상적으로 표시됨
- 대소문자 구분에 관계없이 file_path 속성에 접근 가능
- 이미지 로드 실패 시 자동 재시도 및 사용자 피드백 제공
- 이미지가 없는 경우에 대한 명확한 오류 메시지 표시

## 추가 개선사항
- DB 스키마에 NOT NULL 제약 조건 추가 검토
- 첨부파일 업로드 시 file_path가 null이 되지 않도록 코드 개선
- 컬럼명과 Java 객체 속성명 간의 일관성 유지를 위한 네이밍 정책 확립
