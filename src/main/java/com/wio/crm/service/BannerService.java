package com.wio.crm.service;

import com.wio.crm.mapper.BannerMapper;
import com.wio.crm.model.Banner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BannerService {
    
    private static final Logger logger = LoggerFactory.getLogger(BannerService.class);
    
    @Autowired
    private BannerMapper bannerMapper;
    
    public List<Banner> getActiveSidebarBanners() {
        logger.debug("사이드바 활성 배너 조회");
        return bannerMapper.findActiveBannersByPosition("sidebar");
    }
    
    public List<Banner> getAllBanners() {
        logger.debug("모든 배너 조회");
        return bannerMapper.findAllBanners();
    }
    
    public Banner getBannerById(Long id) {
        logger.debug("ID로 배너 조회: {}", id);
        return bannerMapper.findById(id);
    }
    
    public void saveBanner(Banner banner) {
        if (banner.getId() == null) {
            logger.info("새 배너 등록: {}", banner.getName());
            bannerMapper.insert(banner);
        } else {
            logger.info("배너 수정: ID={}, 이름={}", banner.getId(), banner.getName());
            bannerMapper.update(banner);
        }
    }
    
    public void deleteBanner(Long id) {
        logger.info("배너 삭제: ID={}", id);
        bannerMapper.deleteById(id);
    }
}