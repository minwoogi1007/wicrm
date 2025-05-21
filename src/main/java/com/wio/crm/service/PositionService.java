package com.wio.crm.service;

import com.wio.crm.mapper.Temp01Mapper;
import com.wio.crm.model.Temp01;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PositionService {

    @Autowired
    private Temp01Mapper temp01Mapper;

    /**
     * 현재 인증된 사용자의 포지션이 지정된 값 이상인지 확인합니다.
     * 
     * @param minPosition 최소 포지션 값
     * @return 사용자의 포지션이 minPosition 이상이면 true, 그렇지 않으면 false
     */
    public boolean hasPositionGreaterThanOrEqual(int minPosition) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        String userId = authentication.getName();
        Temp01 employee = temp01Mapper.findByUserId(userId);
        
        if (employee == null) {
            return false;
        }
        
        try {
            int position = Integer.parseInt(employee.getPosition());
            return position >= minPosition;
        } catch (NumberFormatException e) {
            return false;  // position이 숫자 형식이 아닌 경우
        }
    }
} 