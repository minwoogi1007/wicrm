package com.wio.crm.model;

import java.util.HashMap;

/**
 * MyBatis 쿼리 결과를 모두 String 타입으로 처리하는 맵
 * 특히 Oracle NUMBER 타입과 Java String 필드 간의 매핑 문제를 해결하기 위한 클래스
 */
public class ResultMap extends HashMap<String, Object> {
    
    /**
     * 맵에서 키에 해당하는 값을 String으로 변환하여 반환합니다.
     * 값이 null인 경우 null 반환
     * 
     * @param key 조회할 키
     * @return String으로 변환된 값 또는 null
     */
    public String getString(String key) {
        Object value = get(key);
        return value != null ? String.valueOf(value) : null;
    }
    
    /**
     * 맵에서 키에 해당하는 값을 String으로 변환하여 반환합니다.
     * 값이 null인 경우 기본값 반환
     * 
     * @param key 조회할 키
     * @param defaultValue 값이 null일 때 반환할 기본값
     * @return String으로 변환된 값 또는 기본값
     */
    public String getString(String key, String defaultValue) {
        Object value = get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }
    
    /**
     * 맵에서 키에 해당하는 값을 정수로 변환하여 반환합니다.
     * 변환할 수 없거나 null인 경우 0 반환
     * 
     * @param key 조회할 키
     * @return 정수 값 또는 0
     */
    public int getInt(String key) {
        Object value = get(key);
        if (value == null) {
            return 0;
        }
        
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 맵에서 키에 해당하는 값을 정수로 변환하여 반환합니다.
     * 변환할 수 없거나 null인 경우 기본값 반환
     * 
     * @param key 조회할 키
     * @param defaultValue 변환할 수 없거나 null일 때 반환할 기본값
     * @return 정수 값 또는 기본값
     */
    public int getInt(String key, int defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 맵에서 키에 해당하는 값을 Double로 변환하여 반환합니다.
     * 변환할 수 없거나 null인 경우 0.0 반환
     * 
     * @param key 조회할 키
     * @return Double 값 또는 0.0
     */
    public double getDouble(String key) {
        Object value = get(key);
        if (value == null) {
            return 0.0;
        }
        
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * 맵에서 키에 해당하는 값을 Long으로 변환하여 반환합니다.
     * 변환할 수 없거나 null인 경우 0L 반환
     * 
     * @param key 조회할 키
     * @return Long 값 또는 0L
     */
    public long getLong(String key) {
        Object value = get(key);
        if (value == null) {
            return 0L;
        }
        
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
} 