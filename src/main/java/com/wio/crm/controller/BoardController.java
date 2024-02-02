package com.wio.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {
    @GetMapping("/board")
    public String getContentFragment(Model model) {
       // model.addAttribute("content", "list"); // Content changes to list.html
        System.out.println("리스트 !!!");
        return "board/board";
    }
}
