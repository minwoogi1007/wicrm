package com.wio.crm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 핸들러
 * 
 * 모든 컨트롤러에서 발생하는 예외를 중앙에서 처리합니다.
 * 
 * @author WICRM Team
 * @since 2025-12-17
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * API 요청인지 확인
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        
        return uri.startsWith("/api/") 
            || (accept != null && accept.contains("application/json"))
            || (contentType != null && contentType.contains("application/json"));
    }

    /**
     * 404 에러 처리 - 페이지를 찾을 수 없음
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("404 Not Found - URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
        
        if (isApiRequest(request)) {
            return createApiErrorResponse(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", request);
        }
        
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("errorMessage", "요청하신 페이지를 찾을 수 없습니다.");
        mav.addObject("requestUri", request.getRequestURI());
        return mav;
    }

    /**
     * IllegalArgumentException 처리 - 잘못된 인자
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("잘못된 요청 - URI: {}, 메시지: {}", request.getRequestURI(), ex.getMessage());
        
        return createApiErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        logger.error("런타임 예외 발생 - URI: {}", request.getRequestURI(), ex);
        
        if (isApiRequest(request)) {
            return createApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다.", request);
        }
        
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", "서버 처리 중 오류가 발생했습니다.");
        return mav;
    }

    /**
     * 일반 Exception 처리 (최종 폴백)
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        logger.error("예외 발생 - URI: {}, 타입: {}", request.getRequestURI(), ex.getClass().getSimpleName(), ex);
        
        if (isApiRequest(request)) {
            return createApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", request);
        }
        
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return mav;
    }

    /**
     * API 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createApiErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}

