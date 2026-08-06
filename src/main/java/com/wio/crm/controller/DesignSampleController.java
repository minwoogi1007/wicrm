package com.wio.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/design-samples")
public class DesignSampleController {

    @GetMapping("/admin")
    public String adminSamples() {
        return "design-samples/admin";
    }

    @GetMapping("/admin/executive")
    public String executiveDashboard() {
        return "design-samples/executive";
    }

    @GetMapping("/admin/crm")
    public String crmWorkspace() {
        return "design-samples/crm";
    }

    @GetMapping("/admin/commerce")
    public String commerceControl() {
        return "design-samples/commerce";
    }

    @GetMapping("/admin/ai")
    public String aiWorkspace() {
        return "design-samples/ai";
    }

    @GetMapping("/admin/minimal")
    public String minimalTable() {
        return "design-samples/minimal";
    }

    @GetMapping("/admin/mobile")
    public String mobileFirst() {
        return "design-samples/mobile";
    }

    @GetMapping("/admin/bento")
    public String bentoGrid() {
        return "design-samples/bento";
    }

    @GetMapping("/admin/brutalist")
    public String brutalistOps() {
        return "design-samples/brutalist";
    }

    @GetMapping("/admin/glass")
    public String glassAi() {
        return "design-samples/glass";
    }
}
