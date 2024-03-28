package com.wio.crm.controller;

import com.wio.crm.model.Board;
import com.wio.crm.service.BoardService;
import com.wio.crm.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class BoardController {

    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }
    @GetMapping("/board")
    public String board(Model model, @RequestParam(name = "category", required = false) String category) {
        List<Board> posts;
        if (category != null) {
            posts = boardService.findPostsByCategory(category);
        } else {
            posts = boardService.findAllPosts();
        }
        model.addAttribute("list", posts);
        return "board/board";
    }


}
