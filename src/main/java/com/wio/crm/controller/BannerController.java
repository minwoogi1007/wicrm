package com.wio.crm.controller;

import com.wio.crm.model.Banner;
import com.wio.crm.service.BannerService;
import com.wio.crm.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

@Controller
@RequestMapping("/admin/banners")
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")  // 내부 직원만 접근 가능
public class BannerController {

    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private PositionService positionService;
    
    @Value("${file.upload-dir}")
    private String fileUploadDir;
    
    // 모든 요청 처리 전에 POSITION 확인
    @ModelAttribute
    public void checkPosition() {
        if (!positionService.hasPositionGreaterThanOrEqual(90)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "배너 관리 권한이 없습니다. Position 90 이상의 직원만 접근 가능합니다.");
        }
    }

    // 배너 목록 페이지
    @GetMapping
    public String listBanners(Model model) {
        model.addAttribute("banners", bannerService.getAllBanners());
        return "admin/banners/list";
    }

    // 배너 추가 폼
    @GetMapping("/create")
    public String createBannerForm(Model model) {
        model.addAttribute("banner", new Banner());
        return "admin/banners/form";
    }

    // 배너 수정 폼
    @GetMapping("/edit/{id}")
    public String editBannerForm(@PathVariable Long id, Model model) {
        Banner banner = bannerService.getBannerById(id);
        model.addAttribute("banner", banner);
        return "admin/banners/form";
    }

    // 배너 저장 처리
    @PostMapping("/save")
    public String saveBanner(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam String linkUrl,
            @RequestParam String position,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "0") int displayOrder,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            RedirectAttributes redirectAttributes) {

        Banner banner = id != null ? bannerService.getBannerById(id) : new Banner();
        banner.setName(name);
        banner.setLinkUrl(linkUrl);
        banner.setPosition(position);
        banner.setActive(active);
        banner.setDisplayOrder(displayOrder);
        banner.setStartDate(startDate);
        banner.setEndDate(endDate);

        // 이미지 처리
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploadDir = fileUploadDir + "/banners";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);

                banner.setImageUrl("/" + fileUploadDir + "/banners/" + uniqueFileName);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "이미지 업로드 중 오류 발생: " + e.getMessage());
                return "redirect:/admin/banners";
            }
        }

        bannerService.saveBanner(banner);
        redirectAttributes.addFlashAttribute("success", "배너가 성공적으로 저장되었습니다.");
        return "redirect:/admin/banners";
    }

    // 배너 삭제
    @GetMapping("/delete/{id}")
    public String deleteBanner(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bannerService.deleteBanner(id);
        redirectAttributes.addFlashAttribute("success", "배너가 성공적으로 삭제되었습니다.");
        return "redirect:/admin/banners";
    }
}