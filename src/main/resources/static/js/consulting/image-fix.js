/**
 * 상담 문의 상세 페이지 - 첨부파일 이미지 처리 로직
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log("첨부파일 이미지 처리 모듈 초기화");
    
    // 이미지 요소 확인
    var attachmentImages = document.querySelectorAll('.attachment-img');
    console.log(`첨부파일 이미지 개수: ${attachmentImages.length}개`);
    
    // 이미지 로드 상태 확인
    attachmentImages.forEach(function(img, index) {
        console.log(`이미지 #${index+1} 확인: ${img.src}`);
        
        // 이미지 로드 이벤트 리스너
        img.addEventListener('load', function() {
            console.log(`이미지 #${index+1} 로드 성공`);
            hideLoader(this);
        });
        
        // 이미지 오류 이벤트 리스너
        img.addEventListener('error', function() {
            console.error(`이미지 #${index+1} 로드 실패`);
            handleImageError(this);
        });
    });
    
    // 모달 초기화
    initImageModal();
});

/**
 * 이미지 로드 오류 처리 - 재시도하지 않고 오류 표시
 */
function handleImageError(imgElement) {
    if (!imgElement) return;
    
    console.error('이미지 로드 실패:', imgElement.src);
    
    // 이미지 부모 요소 찾기
    var container = imgElement.parentElement;
    if (!container) return;
    
    // 이미지 숨기기
    imgElement.style.display = 'none';
    
    // 오류 메시지 표시
    var errorMsg = document.createElement('div');
    errorMsg.className = 'alert alert-danger text-center p-2 mt-2';
    errorMsg.innerHTML = '<p>이미지를 불러올 수 없습니다</p>';
    container.appendChild(errorMsg);
    
    // 로딩 스피너 숨기기
    var fileId = imgElement.getAttribute('data-file-id');
    if (fileId) {
        var loadingEl = document.getElementById('loading-' + fileId);
        if (loadingEl) loadingEl.style.display = 'none';
    }
}

/**
 * 로딩 표시 숨기기
 */
function hideLoader(imgElement) {
    if (!imgElement) return;
    
    // 이미지 ID에서 로딩 요소 ID 생성
    var fileId = imgElement.getAttribute('data-file-id');
    if (!fileId) return;
    
    var loadingElement = document.getElementById('loading-' + fileId);
    if (loadingElement) {
        loadingElement.style.display = 'none';
    }
}

/**
 * 이미지 모달 초기화
 */
function initImageModal() {
    // 모달이 닫힐 때 이미지 초기화
    var imageModal = document.getElementById('imagePreviewModal');
    if (imageModal) {
        imageModal.addEventListener('hidden.bs.modal', function() {
            var previewImg = document.getElementById('previewImageSrc');
            if (previewImg) {
                previewImg.src = '/assets/media/svg/files/blank-image.svg';
            }
        });
    }
}

/**
 * 첨부파일 ID로 이미지 표시 - 프록시 사용
 */
function showAttachmentImage(attachmentId) {
    console.log('첨부파일 ID로 이미지 보기:', attachmentId);
    
    // 전역 변수에서 첨부파일 찾기
    if (window.attachmentsData && window.attachmentsData.length > 0) {
        var foundAttachment = null;
        
        // ID로 첨부파일 찾기
        for (var i = 0; i < window.attachmentsData.length; i++) {
            var id = window.attachmentsData[i]['attachment_id'];
            if (id == attachmentId) {
                foundAttachment = window.attachmentsData[i];
                console.log('첨부파일 찾음:', foundAttachment);
                break;
            }
        }
        
        if (foundAttachment) {
            // 이미지 URL 가져오기
            var filePath = foundAttachment['file_path'];
            var fileName = foundAttachment['file_name'] || '첨부파일';
            
            // 프록시 URL 사용
            var timestamp = new Date().getTime();
            var imageUrl = '/api/image-proxy?path=' + encodeURIComponent(filePath) + '&t=' + timestamp;
            var downloadUrl = '/api/download?path=' + encodeURIComponent(filePath);
            
            console.log('이미지 프록시 URL 구성:', imageUrl);
            
            // 모달 이미지 설정
            var previewImg = document.getElementById('previewImageSrc');
            previewImg.src = imageUrl;
            
            // 이미지 로드 오류 처리
            previewImg.onerror = function() {
                console.error('모달 이미지 로드 실패');
                previewImg.src = '/assets/media/svg/files/blank-image.svg';
                document.getElementById('previewImageTitle').textContent = '이미지를 불러올 수 없습니다';
            };
            
            // 제목 및 다운로드 링크 설정
            document.getElementById('previewImageTitle').textContent = fileName;
            var downloadLink = document.getElementById('previewImageDownload');
            downloadLink.href = downloadUrl;
            downloadLink.setAttribute('download', fileName);
            
            // 원본 이미지 링크 업데이트
            var originalBtn = document.getElementById('originalImageBtn');
            if (originalBtn) {
                originalBtn.href = imageUrl;
            }
            
            // 모달 표시
            var imageModal = new bootstrap.Modal(document.getElementById('imagePreviewModal'));
            imageModal.show();
            return;
        }
    }
    
    // 첨부파일을 찾지 못한 경우
    console.error('첨부파일 ID를 찾을 수 없습니다:', attachmentId);
    alert('첨부파일 정보를 찾을 수 없습니다.');
}

// 전역 함수로 등록
window.showAttachmentImage = showAttachmentImage;
window.hideLoader = hideLoader;
window.imageLoadError = handleImageError;