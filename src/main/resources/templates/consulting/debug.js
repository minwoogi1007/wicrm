/**
 * 첨부파일 이미지 디버깅 스크립트
 */

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log("===== 첨부파일 이미지 디버깅 로그 시작 =====");
    
    // 전역 첨부파일 데이터 확인
    if (window.attachmentsData && window.attachmentsData.length) {
        console.log(`[첨부파일 확인] DB에서 ${attachmentsData.length}개의 첨부파일 데이터를 가져왔습니다.`);
        
        // 각 첨부파일 데이터 상세 출력
        attachmentsData.forEach(function(attachment, index) {
            console.log(`\n[첨부파일 ${index+1} 상세정보]`);
            
            // 모든 속성 출력
            for (var key in attachment) {
                console.log(`- ${key}: ${attachment[key]}`);
            }
            
            // 이미지 경로 정보 출력
            if (attachment.file_path) {
                console.log("\n[첨부파일 이미지 URL 분석]");
                console.log("- 파일 경로(file_path):", attachment.file_path);
                console.log("- 서버 기본 URL:", "http://175.119.224.45:8080/uploads/");
                
                // 전체 URL 생성
                var fullUrl = "http://175.119.224.45:8080/uploads/" + attachment.file_path;
                console.log("- 전체 이미지 URL:", fullUrl);
                
                // HTML 태그 생성 예시
                console.log("\n[HTML 이미지 태그 예시]");
                console.log(`<img src="${fullUrl}" alt="${attachment.file_name || '첨부파일'}">`);
                
                // img 요소 생성하여 로딩 테스트
                var testImg = new Image();
                testImg.onload = function() {
                    window.console.log(`[이미지 로드 성공] ${fullUrl}`);
                };
                testImg.onerror = function() {
                    window.console.error(`[이미지 로드 실패] ${fullUrl}`);
                    // 실패 시 원인 추가 로깅
                    window.console.error(`이미지 접근 실패 상세: ${fullUrl}`);
                    window.console.error(`- 현재 도메인: ${window.location.origin}`);
                    window.console.error(`- 코드 이상 없음`); 
                };
                testImg.src = fullUrl;
            }
        });
    } else {
        console.log("첨부파일 데이터가 없거나 비어 있습니다.");
    }
    
    // 실제 화면에 렌더링된 이미지 확인
    console.log("\n[화면에 렌더링된 이미지 확인]");
    var renderedImages = document.querySelectorAll('.attachment-img');
    console.log(`총 ${renderedImages.length}개의 이미지가 화면에 렌더링됨`);
    
    // 각 이미지 요소 정보 출력
    renderedImages.forEach(function(img, index) {
        console.log(`\n[렌더링된 이미지 ${index+1}]`);
        console.log("- src:", img.src);
        console.log("- data-file-path:", img.getAttribute('data-file-path'));
        console.log("- data-file-name:", img.getAttribute('data-file-name'));
        console.log("- data-full-url:", img.getAttribute('data-full-url'));
        
        // 이미지 로드 이벤트 추가
        img.addEventListener('load', function() {
            console.log(`[이미지 로드 성공] ${this.src}`);
        });
        
        img.addEventListener('error', function() {
            console.error(`[이미지 로드 실패] ${this.src}`);
            
            // 로드 실패 시 직접 URL 사용하여 재시도
            var fullUrl = img.getAttribute('data-full-url');
            if (fullUrl && fullUrl !== this.src) {
                console.log(`[이미지 재시도] ${fullUrl}`);
                this.src = fullUrl;
            }
        });
    });
    
    console.log("===== 첨부파일 이미지 디버깅 로그 종료 =====");
});

/**
 * 이미지 클릭 시 URL 정보를 콘솔에 출력
 */
function logImageInfo(imgElement) {
    console.log("===== 클릭한 이미지 정보 =====");
    console.log("- src:", imgElement.src);
    console.log("- 파일명:", imgElement.getAttribute('data-file-name'));
    console.log("- 파일경로:", imgElement.getAttribute('data-file-path'));
    console.log("- 전체URL:", imgElement.getAttribute('data-full-url') || "http://175.119.224.45:8080/uploads/" + imgElement.getAttribute('data-file-path'));
    console.log("============================");
}
