// 이미지 로드 테스트 함수
function testImageLoad(src, label) {
    var testImg = new Image();
    testImg.onload = function() {
        window.console.log(`[${label}] 이미지 로드 성공:`, src);
    };
    testImg.onerror = function() {
        window.console.error(`[${label}] 이미지 로드 실패:`, src);
        
        // 추가 로컬 경로 시도
        if (src.includes('/uploads/')) {
            var fallbackSrc = '/assets/media/svg/files/blank-image.svg';
            window.console.warn(`[${label}] 대체 이미지 사용:`, fallbackSrc);
            
            // 해당 이미지 요소 찾기 시도
            var imgElements = document.querySelectorAll('img[src="' + src + '"]');
            if (imgElements.length > 0) {
                imgElements.forEach(function(img) {
                    img.src = fallbackSrc;
                    window.console.log(`[${label}] 이미지 요소 자동 교체 완료`);
                });
            }
        }
    };
    testImg.src = src;
}

// 이미지 원본 다운로드 함수
window.downloadOriginalImage = function(filePath, fileName) {
    // 원래 이미지 URL
    var originalUrl = 'http://175.119.224.45:8080/uploads/' + filePath;
    
    window.console.log('원본 이미지 다운로드 시도:', originalUrl);
    
    // 새 탭에서 열기
    var link = document.createElement('a');
    link.href = originalUrl;
    link.download = fileName || 'image.jpg';
    link.target = '_blank';
    link.click();
};

/**
 * 상담 문의 상세 페이지 디버깅 스크립트
 */
window.console.log("디버깅 스크립트가 로드되었습니다.");

// 디버깅 도구
console.log("이미지 디버깅 도구 로드됨");

// 이미지 경로 디버깅용 함수
window.fixImageUrls = function() {
    console.log("이미지 경로 디버깅 실행");
    
    // 서버 정보 확인
    var serverInfo = {
        host: window.location.host,
        origin: window.location.origin,
        pathname: window.location.pathname,
        protocol: window.location.protocol
    };
    console.log("현재 서버 정보:", serverInfo);
    
    // 로컬, 개발, 운영 환경 확인
    var isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
    console.log("로컬 환경 여부:", isLocal);
    
    // 모든 이미지 요소 확인
    var images = document.querySelectorAll('img');
    console.log(`이미지 확인 (총 ${images.length}개)`);
    
    // 각 이미지 순회
    images.forEach(function(img, index) {
        console.log(`[이미지 ${index+1}]`);
        console.log("- src:", img.src);
        console.log("- data-file-path:", img.getAttribute('data-file-path'));
        console.log("- data-file-id:", img.getAttribute('data-file-id'));
        
        // 이미지 경로 분석
        if (img.src) {
            var isExternal = img.src.includes('http://175.119.224.45:8080');
            var isRelative = img.src.startsWith('/');
            var isBlankImage = img.src.includes('blank-image.svg');
            
            console.log("- 외부 URL 여부:", isExternal);
            console.log("- 상대 경로 여부:", isRelative);
            console.log("- 대체 이미지 여부:", isBlankImage);
        }
        
        // 이미지 크기 분석
        console.log("- 너비:", img.width, "px");
        console.log("- 높이:", img.height, "px");
        console.log("- 자연 너비:", img.naturalWidth, "px");
        console.log("- 자연 높이:", img.naturalHeight, "px");
        
        // 이미지 로드 상태 확인
        console.log("- 로드 완료 여부:", img.complete);
        
        // 이미지가 제대로 표시되지 않으면 분석
        if (img.complete && img.naturalWidth === 0) {
            console.warn(`[이미지 ${index+1}] 로드는 되었으나 표시되지 않음 (깨진 이미지)`);
            
            // 이미지 경로에 문제가 있는지 분석
            if (img.getAttribute('data-file-path')) {
                var filePath = img.getAttribute('data-file-path');
                console.log("- 시도할 경로:", [
                    `/uploads/${filePath}`,
                    `http://175.119.224.45:8080/uploads/${filePath}`,
                    `/api/image-proxy?path=${encodeURIComponent(filePath)}`,
                    `/api/image-base64?path=${encodeURIComponent(filePath)}`
                ]);
            }
        }
    });
    
    // CORS 오류 확인
    console.log("CORS 오류 확인중...");
    if (window.performance && window.performance.getEntries) {
        var resources = window.performance.getEntries();
        var failedResources = resources.filter(function(resource) {
            return resource.name.includes('uploads') && resource.name.includes('175.119.224.45:8080');
        });
        
        console.log(`외부 리소스 요청 (${failedResources.length}개):`);
        failedResources.forEach(function(resource, index) {
            console.log(`[리소스 ${index+1}]`);
            console.log("- URL:", resource.name);
            console.log("- 상태:", resource.responseStatus || '알 수 없음');
            console.log("- 로드 시간:", resource.duration, "ms");
        });
    }
};

// 페이지 로드 후 디버깅 실행
window.addEventListener('load', function() {
    console.log("페이지 로드 완료 - 디버깅 시작");
    
    // 2초 후 이미지 디버깅 실행 (페이지 완전 로드 후)
    setTimeout(function() {
        window.fixImageUrls();
    }, 2000);
});

// 접근 경로 디버깅 함수
function logImageAccess(attachment, key) {
    try {
        // 다양한 방법으로 속성 접근 시도
        var dotAccess = attachment.file_path;
        var bracketStringAccess = attachment['file_path'];
        var bracketVarAccess = attachment[key];
        
        window.console.log("속성 접근 디버깅:");
        window.console.log('- attachment 타입:', typeof attachment);
        window.console.log('- attachment.file_path:', dotAccess);
        window.console.log('- attachment[\'file_path\']:', bracketStringAccess);
        window.console.log('- attachment[key]:', bracketVarAccess);
        
        // 속성 목록 확인
        window.console.log('- attachment 속성 목록:');
        for (var prop in attachment) {
            window.console.log(`  ${prop}: ${attachment[prop]}`);
        }
        
        return {
            dotAccess: dotAccess,
            bracketStringAccess: bracketStringAccess,
            bracketVarAccess: bracketVarAccess
        };
    } catch (e) {
        window.console.error("속성 접근 중 오류:", e);
        return null;
    }
}