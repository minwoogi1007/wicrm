# 첨부파일 이미지 URL 문제 해결 방법

## 문제 상황
상담 문의 상세(consulting/detail) 페이지에서 첨부파일 이미지가 보이지 않는 문제가 있었습니다.

## 문제 원인
1. DB에 저장된 file_path가 `images/파일명.jpg` 형식인데, 이 값만으로 이미지 경로를 구성했습니다.
2. 실제 이미지에 접근하기 위해서는 서버 기본 URL(`http://175.119.224.45:8080/uploads/`)과 file_path를 결합해야 합니다.
3. 콘솔 로그 확인 결과, 이미지 경로는 `http://175.119.224.45:8080/uploads/images/pasted_dda2056f-5b63-4dec-a486-0f21da442485_20250422142313.jpg` 형태가 되어야 합니다.

## 해결 방법

### 1. 이미지 URL 올바르게 구성하기

#### 직접 문자열 결합
```html
<img th:src="${'http://175.119.224.45:8080/uploads/' + attachment['file_path']}" 
     th:alt="${attachment['file_name']}">
```

#### Thymeleaf 리터럴 대체 구문 사용 (권장)
```html
<img th:src="@{|http://175.119.224.45:8080/uploads/${attachment['file_path']}|}" 
     th:alt="${attachment['file_name']}">
```

### 2. 컨트롤러 코드 개선
컨트롤러에서 첨부파일 정보를 가져올 때 전체 URL을 미리 구성하여 모델에 추가:

```java
// 첨부파일 URL 정보 추가
for (Map<String, Object> attachment : attachments) {
    if (attachment.containsKey("file_path")) {
        String filePath = (String) attachment.get("file_path");
        String serverUrl = "http://175.119.224.45:8080/uploads/";
        attachment.put("full_url", serverUrl + filePath);
    }
}
```

### 3. 다운로드 링크 설정
다운로드 링크에도 전체 URL을 적용하고, download 속성을 빈 값으로 설정하여 원본 파일명으로 다운로드되도록 함:

```html
<a th:href="${'http://175.119.224.45:8080/uploads/' + attachment['file_path']}" 
   class="btn btn-sm btn-light-primary w-100 btn-download" 
   download>
    다운로드
</a>
```

### 4. 디버깅 방법

이미지 URL이 올바르게 구성되었는지 확인하기 위해 다음 디버깅 기법을 활용할 수 있습니다:

1. **컨트롤러 로깅**: 첨부파일 정보를 로그에 출력하여 확인
   ```java
   logger.info("  - 기본 URL: {}", serverUrl);
   logger.info("  - 파일 경로: {}", filePath);
   logger.info("  - 전체 URL: {}", fullUrl);
   ```

2. **콘솔 로깅**: 브라우저에서 이미지 로드 성공/실패 여부 확인
   ```html
   <img src="..." 
        onload="console.log('이미지 로드 성공:', this.src);"
        onerror="console.error('이미지 로드 실패:', this.src); this.src='https://via.placeholder.com/150';">
   ```

3. **테스트 페이지**: 다양한 이미지 URL 구성 방식 테스트 (image_test.html)

## 결론

첨부파일 이미지 URL은 서버 기본 URL과 DB에 저장된 file_path를 올바르게 결합해야 합니다. Thymeleaf의 리터럴 대체 구문(`@{|...|}`), 직접 문자열 결합 방식 등 여러 방법이 있지만, 중요한 점은 완전한 URL을 구성하는 것입니다.

가장 안정적인 해결책은:
1. URL 구성: `@{|http://175.119.224.45:8080/uploads/${attachment['file_path']}|}`
2. 오류 처리: `onerror="this.onerror=null; this.src='대체이미지';"`
3. 다운로드: `download` 속성을 빈 값으로 설정하여 원본 파일명 유지
