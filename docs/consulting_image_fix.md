# 상담 문의 상세 페이지 첨부파일 이미지 문제 해결

## 문제 상황

상담 문의 상세(consulting/detail) 페이지에서 첨부파일 이미지가 표시되지 않음

## 원인 분석

- 이미지 경로 문제
- 실제 이미지 파일은 'http://175.119.224.45:8080/uploads/' 경로에 있음
- DB에는 'images/파일명.png'와 같이 file_path가 저장되어 있음
- 두 값을 적절히 조합해야 이미지가 출력됨

## 해결 방법

### 이미지 경로 동적 생성

`detail.html` 파일에서 첨부파일 이미지 경로를 동적으로 생성하도록 수정

```html
<!-- 수정 전 -->
<img src="https://via.placeholder.com/300" 
     alt="첨부파일 이미지" 
     class="attachment-img"
     onclick="previewImage('https://via.placeholder.com/800', '첨부파일')"
     style="cursor: pointer;" />

<!-- 수정 후 -->
<img th:src="'http://175.119.224.45:8080/uploads/' + ${attachment['file_path']}" 
     th:alt="${attachment != null && attachment['file_name'] != null ? attachment['file_name'] : '첨부파일 이미지'}"
     class="attachment-img"
     th:onclick="'previewImage(\'' + 'http://175.119.224.45:8080/uploads/' + ${attachment['file_path']} + '\', \'' + (${attachment['file_name'] != null ? attachment['file_name'] : '첨부파일'}) + '\')'"
     style="cursor: pointer;" 
     onerror="this.onerror=null; this.src='https://via.placeholder.com/300'; console.error('이미지 로드 실패:', this.src);"/>
```

### 다운로드 링크 동적 생성

```html
<!-- 수정 전 -->
<a href="https://via.placeholder.com/800" class="btn btn-sm btn-light-primary w-100 btn-download" download="첨부파일.jpg">

<!-- 수정 후 -->
<a th:href="'http://175.119.224.45:8080/uploads/' + ${attachment['file_path']}" class="btn btn-sm btn-light-primary w-100 btn-download" th:download="${attachment != null && attachment['file_name'] != null ? attachment['file_name'] : '첨부파일.jpg'}">
```

### 미리보기 함수 유지

```javascript
// 미리보기 함수
function previewImage(src, fileName) {
    console.log('이미지 미리보기 호출:', src);
    
    // 이미지 소스 설정
    document.getElementById('previewImageSrc').src = src;
    
    // 제목 및 다운로드 링크 설정
    document.getElementById('previewImageTitle').textContent = fileName || '이미지';
    document.getElementById('previewImageDownload').href = src;
    
    // 모달 표시
    var imageModal = new bootstrap.Modal(document.getElementById('imagePreviewModal'));
    imageModal.show();
}
```

## 주의사항

- 실제 서버 URL은 'http://175.119.224.45:8080/uploads/'
- DB에 저장된 file_path 값은 'images/파일명.png' 형태
- 동적으로 결합해서 전체 URL 생성 (서버URL + file_path)

## 테스트 방법

1. 스프링부트 애플리케이션을 재시작합니다

2. 브라우저에서 로그인 후 상담 문의 상세 페이지 접속:
   ```
   http://localhost:8080/consulting/detail?id=1
   ```

3. 첨부파일 이미지가 정상적으로 표시되는지 확인

4. 이미지 클릭 시 모달창에서 확대된 이미지가 표시되는지 확인

