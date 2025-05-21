# 첨부파일 이미지 경로 null 처리 개선 작업

## 문제 상황
- 첨부파일 이미지 표시 시 `file_path` 속성이 null이어서 이미지가 표시되지 않는 문제 발생
- http://175.119.224.45:8080/uploads/null 와 같은 URL로 이미지 요청이 발생
- 로그에 "404 Not Found" 오류 발생

## 개선 방법
1. 첨부파일 객체의 `file_path` 필드가 null인 경우에 대한 처리 추가
2. 템플릿에서 아래와 같은 조건부 렌더링 추가:
   - 이미지 표시 영역: `th:if="${attachment['file_path'] != null}"`
   - 경로가 null일 경우 오류 메시지 표시: `th:if="${attachment['file_path'] == null}"`
3. 이미지 로드 함수(showImageFullScreen)에도 null 체크 추가
4. 다운로드/원본보기 링크에도 조건부 처리(경로가 있는 경우에만 버튼 표시)

## 수정 내용 요약
1. **임지 태그 조건부 렌더링:**
```html
<img th:if="${attachment['file_path'] != null}" 
     th:src="@{|http://175.119.224.45:8080/uploads/${attachment['file_path']}|}"
     ... />

<!-- 파일 경로가 없는 경우 오류 표시 -->
<div th:if="${attachment['file_path'] == null}" class="alert alert-danger text-center p-2 mt-2">
    <i class="ki-duotone ki-warning-2 fs-2x mb-2">...</i>
    <p>파일 경로 정보가 없습니다.</p>
</div>
```

2. **다운로드/원본보기 버튼 조건부 표시:**
```html
<!-- 파일 경로가 있는 경우에만 버튼 표시 -->
<th:block th:if="${attachment['file_path'] != null}">
    <!-- 원본 이미지 보기 버튼 -->
    <a th:href="@{|http://175.119.224.45:8080/uploads/${attachment['file_path']}|}" ...>원본보기</a>
    <!-- 다운로드 버튼 -->
    <a th:href="@{|http://175.119.224.45:8080/uploads/${attachment['file_path']}|}" download ...>다운로드</a>
</th:block>

<!-- 파일 경로가 없는 경우 안내 버튼 -->
<th:block th:if="${attachment['file_path'] == null}">
    <button type="button" class="btn btn-sm btn-light-danger w-100" disabled>
        <i class="ki-duotone ki-information-5 fs-6 me-1">...</i>
        파일 경로 없음
    </button>
</th:block>
```

3. **이미지 전체화면 보기 함수에 null 체크 추가:**
```javascript
function showImageFullScreen(filePath, fileName) {
    console.log('이미지 전체 화면 보기 시도:', fileName);
    
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
    
    // 이후 로직...
}
```

4. **디버깅 정보 표시에도 null 체크 추가:**
```html
<div><b>경로:</b> <span th:text="${attachment['file_path'] != null ? attachment['file_path'] : '경로 없음'}"></span></div>
<div><b>타입:</b> <span th:text="${attachment['file_type'] != null ? attachment['file_type'] : '타입 정보 없음'}"></span></div>
<div><b>전체URL:</b> <span th:text="${attachment['file_path'] != null ? ('http://175.119.224.45:8080/uploads/' + attachment['file_path']) : '경로 정보 없음'}"></span></div>
```

## 테스트 결과
- file_path가 null인 경우:
  - 이미지 대신 오류 메시지 표시됨
  - 다운로드/원본보기 버튼 대신 "파일 경로 없음" 표시
  - 이미지 클릭 시 적절한 오류 메시지 표시
- file_path가 정상적인 경우:
  - 이미지 정상 표시
  - 다운로드/원본보기 버튼 정상 작동
  - 전체화면 보기 정상 작동

## 후속 작업
- 업로드 시스템 개선으로 file_path가 null이 되지 않도록 근본적인 해결책 마련 필요
- 첨부파일 DB 스키마 검토하여 NOT NULL 제약 조건 추가 검토
- 파일 경로가 null인 첨부파일 데이터 정리/복구 작업 필요