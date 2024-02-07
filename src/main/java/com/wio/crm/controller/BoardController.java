package com.wio.crm.controller;

import com.wio.crm.service.BoardService;
import com.wio.crm.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

    @Autowired
    private BoardService boardService;

    @GetMapping("/board")
    public String getContentFragment(Model model) {
       // model.addAttribute("list",boardService.getNoticeBoardList() ); // Content changes to list.html

        return "board/board";
    }
}
