package com.wio.crm.config;

import com.wio.crm.service.BannerService;
import com.wio.crm.service.PositionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class BannerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(BannerInterceptor.class);

    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private PositionService positionService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            // 모든 뷰에 사이드바 배너 추가
            modelAndView.addObject("sidebarBanners", bannerService.getActiveSidebarBanners());
            
            // POSITION 값이 90 이상인 내부 직원인지 확인하여 모델에 추가
            boolean canManageBanners = positionService.hasPositionGreaterThanOrEqual(90);
            modelAndView.addObject("canManageBanners", canManageBanners);
            
            logger.debug("사이드바 배너를 모델에 추가: {}, 배너 관리 권한: {}", request.getRequestURI(), canManageBanners);
        }
    }
}