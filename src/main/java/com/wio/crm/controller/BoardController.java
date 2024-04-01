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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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

    // 글쓰기 폼 페이지로 이동
    @GetMapping("/create")
    public String createForm() {
        return "board/createBoard"; // Thymeleaf 템플릿 이름
    }

    // 글쓰기 처리
    @PostMapping("/create")
    public String createPost(Board board, @RequestParam("image") MultipartFile image) {
        // 이미지 처리 로직은 생략. Board 객체에 이미지 정보를 설정하는 부분 필요
        boardService.insertPost(board);
        return "redirect:/Board"; // 글 목록 페이지로 리디렉션
    }

    // 글 읽기
    @GetMapping("/post/{id}")
    public String readPost(@PathVariable("id") int id, Model model) {
        Board post = boardService.selectPostById(id);
        model.addAttribute("post", post);
        return "board/readBoard"; // Thymeleaf 템플릿 이름
    }
}
