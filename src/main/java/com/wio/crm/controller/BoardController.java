package com.wio.crm.controller;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.model.Board;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Temp01;
import com.wio.crm.service.BoardService;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
public class BoardController {

    private final BoardService boardService;
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @Autowired
    private Environment env;
    @GetMapping("/error")
    public String error() {
        return "error"; // Thymeleaf 템플릿 이름
    }

    @GetMapping("/board")
    public String board(Model model, @RequestParam(name = "category", required = false) String category) {
        List<Board> posts;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.info("접근불가");
            return "redirect:/sign-in";
        }


        // 현재 로그인한 사용자의 CustomUserDetails 얻기
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 사용자 정의 정보 사용
            //내부직원
            Temp01 tempUser = userDetails.getTempUserInfo();
            //외부직원
            Tcnt01Emp tcntUser = userDetails.getTcntUserInfo();

            if(tcntUser!= null){
                if(tcntUser.getCustCode().equals(category)){
                    posts = boardService.findPostsByCategory(category);
                }else{
                    return "redirect:/error";

                }
            }else{
                posts = boardService.findPostsByCategory(category);
            }

            model.addAttribute("list", posts);
        }
        return "board/board";
    }

    // 글쓰기 폼 페이지로 이동
    @GetMapping("/board/create")
    public String createForm() {
        return "board/createBoard"; // Thymeleaf 템플릿 이름
    }

    // 글쓰기 처리
    @PostMapping("/board/create/saveBoard")
    public ResponseEntity<String> saveBoard(@RequestParam(value = "files", required = false) MultipartFile[] files, @RequestParam Map<String, String> params) {
        try {

            String uploadDir = env.getProperty("file.upload-dir");
            Path uploadPath = Paths.get(uploadDir);
            StringBuilder fileNames = new StringBuilder();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    fileNames.append(fileName).append(";");
                }
            }


            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String id ="";
            String custCode="";
            String empName="";
            // 현재 로그인한 사용자의 CustomUserDetails 얻기
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

                // 사용자 정의 정보 사용
                //내부직원
                Temp01 tempUser = userDetails.getTempUserInfo();
                //외부직원
                Tcnt01Emp tcntUser = userDetails.getTcntUserInfo();

                //일단 업체 담당자만 등록
                id = tcntUser.getUserId();
                custCode  = tcntUser.getCustCode();
                empName=tcntUser.getEmp_name();

            }

            // Save other details to database

            // Save post data
            Board board = new Board();
            board.setSUBJECT(params.get("title"));
            board.setCONTENT(params.get("content"));
            board.setATT_FILE(fileNames.toString());
            board.setCAT_GROUP(custCode);
            board.setID(id);
            board.setEMPNM(empName);
            boardService.insertPost(board);

            return ResponseEntity.ok("Saved successfully");
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while saving the file");
        }

    }
    @PostMapping("/board/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = env.getProperty("file.upload-dir");
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + fileName; // 업로드된 파일의 URL

            return ResponseEntity.ok(fileUrl);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while uploading the image");
        }
    }
    // 글 읽기
    @GetMapping("/board/readBoard")
    public String readPost(@RequestParam("id") String id, Model model) {

        Board post = boardService.selectPostById(id);
        List<Board> comment = boardService.selectComment(id);

        model.addAttribute("post", post);

        if (post.getATT_FILE() != null && !post.getATT_FILE().trim().isEmpty()) {
            String[] files = post.getATT_FILE().trim().split(";");
            model.addAttribute("files", files);
        } else {
            model.addAttribute("files", new String[0]);
        }

        model.addAttribute("list", comment);
        return "board/readBoard";// Thymeleaf 템플릿 이름
    }


    @PostMapping("/board/readBoard/comments")
    @ResponseBody
    public ResponseEntity<Board> saveComment(
            @RequestParam("GNO") String gno,
            @RequestParam("UNO") String uno,
            @RequestParam("CAT_GROUP") String catGroup,
            @RequestParam("CONTENT") String content,
            @RequestParam("REPLY_DEPTH") String replyDepth) {

        // 각 파라미터가 잘 넘어오는지 확인
        System.out.println("GNO============" + gno);
        System.out.println("UNO============" + uno);
        System.out.println("CAT_GROUP============" + catGroup);
        System.out.println("CONTENT============" + content);
        System.out.println("REPLY_DEPTH============" + replyDepth);
// 현재 날짜와 시간을 특정 형식으로 설정
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date());
        // Board 객체 생성 및 설정
        Board board = new Board();
        board.setGNO(gno);
        board.setUNO(uno);
        board.setCAT_GROUP(catGroup);
        board.setCONTENT(content);
        board.setREPLY_DEPTH(replyDepth);
        board.setIN_DATE(formattedDate);

        // 댓글 저장 로직
        Board savedComment = boardService.saveComment(board);
        board.setID(savedComment.getID());

        return ResponseEntity.ok(savedComment);
    }

}
