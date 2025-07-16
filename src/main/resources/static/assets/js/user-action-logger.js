/**
 * 사용자 액션 로깅 시스템
 * 로그인한 사용자의 클릭 액션을 자동으로 로깅합니다.
 */
document.addEventListener('DOMContentLoaded', function() {
    // 클릭 이벤트 전역 리스너
    document.addEventListener('click', function(e) {
        // 클릭한 요소 정보 수집
        const targetElement = e.target;
        const targetId = targetElement.id || '';
        const targetClass = targetElement.className || '';
        const targetTag = targetElement.tagName || '';
        const targetText = targetElement.innerText || targetElement.textContent || '';
        const targetHref = targetElement.href || '';

        // 클릭한 요소의 경로 파악 (부모 요소 포함)
        let path = '';
        let element = targetElement;
        let i = 0;
        while (element && element.tagName && i < 5) {
            path = element.tagName.toLowerCase() +
                (element.id ? '#' + element.id : '') +
                (element.className ? '.' + element.className.replace(/\s+/g, '.') : '') +
                (path ? ' > ' + path : '');
            element = element.parentElement;
            i++;
        }

        // 클릭 위치
        const x = e.clientX;
        const y = e.clientY;

        // 페이지 정보
        const url = window.location.href;
        const path2 = window.location.pathname;

        // 데이터 구성
        const actionData = {
            elementId: targetId,
            elementClass: targetClass,
            elementTag: targetTag,
            elementText: targetText.substring(0, 100), // 텍스트가 너무 길면 잘라냄
            elementHref: targetHref,
            elementPath: path,
            position: { x, y },
            pageUrl: url,
            pagePath: path2,
            timestamp: new Date().toISOString(),
            viewport: {
                width: window.innerWidth,
                height: window.innerHeight
            }
        };

        // 서버로 데이터 전송
        logUserAction('click', targetId || targetClass || targetTag, url, actionData);
    });

    // 페이지 로드 이벤트 캡처
    window.addEventListener('load', function() {
        const url = window.location.href;
        logUserAction('pageview', 'window', url, {
            path: window.location.pathname,
            referrer: document.referrer,
            timestamp: new Date().toISOString()
        });
    });

    // 로깅 함수
    function logUserAction(actionType, actionTarget, actionUrl, actionData) {
        // 로깅 제외 대상 체크 (너무 많은 로그 방지)
        if (shouldExcludeLogging(actionType, actionTarget, actionUrl)) {
            return;
        }

        const logData = {
            actionType: actionType,
            actionTarget: actionTarget,
            actionUrl: actionUrl,
            actionData: JSON.stringify(actionData),
            sessionId: getSessionId()
        };

        // 서버에 로그 데이터 전송
        fetch('/api/log/user-action', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCSRFToken() // Spring Security CSRF 토큰
            },
            body: JSON.stringify(logData),
            // 백그라운드로 처리 (사용자 경험에 영향 없음)
            credentials: 'same-origin'
        }).catch(error => {
            console.error('Error logging user action:', error);
        });
    }

    // 로깅 제외할 대상인지 확인
    function shouldExcludeLogging(actionType, actionTarget, actionUrl) {
        // 로그인 페이지는 제외
        if (window.location.pathname === '/login') {
            return true;
        }

        // 로깅 API 호출 자체는 제외
        if (actionUrl.includes('/api/log/')) {
            return true;
        }

        // 자주 발생하는 무의미한 클릭은 제외 (선택적)
        if (actionType === 'click' &&
            (actionTarget === 'BODY' || actionTarget === 'HTML')) {
            return true;
        }

        return false;
    }

    // CSRF 토큰 가져오기 (Spring Security용)
    function getCSRFToken() {
        return document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
    }

    // 세션 ID 관리
    function getSessionId() {
        let sessionId = sessionStorage.getItem('user_action_session_id');
        if (!sessionId) {
            sessionId = generateUUID();
            sessionStorage.setItem('user_action_session_id', sessionId);
        }
        return sessionId;
    }

    // UUID 생성 함수
    function generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
});