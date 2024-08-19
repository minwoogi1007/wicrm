package com.wio.crm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;


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

    private boolean isAuthorizedUser(Authentication authentication) {
        return authentication != null &&
                authentication.getName().equals("MINWOOGI");
    }
    @GetMapping("/board")
    public String board(Model model, @RequestParam(name = "category", required = false) String category, Authentication authentication) {
        List<Board> posts;


        if (authentication == null) {
            logger.info("접근불가");
            return "redirect:/sign-in";
        }

        boolean canCreatePost =  isAuthorizedUser(authentication);

        // 'G' 카테고리인 경우 권한 체크 없이 진행
        if ("G".equals(category)) {
            System.out.println("G.equals(category)========");
            posts = boardService.findPostsByCategory(category);
            model.addAttribute("list", posts);
            model.addAttribute("category", category);  // 추가된 부분
            model.addAttribute("canCreatePost", canCreatePost);

            return "board/board";
        }

        // 현재 로그인한 사용자의 CustomUserDetails 얻기
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 사용자 정의 정보 사용
            //내부직원
            Temp01 tempUser = userDetails.getTempUserInfo();
            //외부직원
            Tcnt01Emp tcntUser = userDetails.getTcntUserInfo();

            System.out.println("tempUser========"+tempUser);
            System.out.println("tcntUser========"+tcntUser);

            if(tempUser!=null){     //내부직원
                canCreatePost = false;
            }else{                  //외부직원
                if(tcntUser.getCustCode().equals(category)){
                    canCreatePost = true;
                }
            }
            posts = boardService.findPostsByCategory(category);


            model.addAttribute("list", posts);
            model.addAttribute("category", category);  // 추가된 부분
            model.addAttribute("canCreatePost", canCreatePost);
        }
        return "board/board";
    }

    // 글쓰기 폼 페이지로 이동
    @GetMapping("/board/create")
    public String createForm(@RequestParam(name = "category", required = false) String category, Model model, Authentication authentication) {
        model.addAttribute("category", category);
        return "board/createBoard";
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
    public String readPost(@RequestParam("id") String id,
                           @RequestParam(name = "category", required = false) String category,
                           Model model) {
        Board post;

        if ("G".equals(category)) {
            // 공지사항인 경우
            post = boardService.selectPostByIdWithoutCustCode(id);
        } else {
            // 일반 게시글인 경우
            post = boardService.selectPostById(id);
        }

        List<Board> comment;
        if ("G".equals(category)) {
            comment = boardService.selectCommentWithoutCustCode(id);
        } else {
            comment = boardService.selectComment(id);
        }

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

    @PostMapping("/board/update")
    public ResponseEntity<String> updateBoard(@RequestParam(value = "files", required = false) MultipartFile[] files,
                                              @RequestParam Map<String, String> params,
                                              @RequestParam("deletedFiles") String deletedFilesJson,
                                              Authentication authentication) {
        try {
            // 1. 데이터 무결성 확인 및 보안 검증
            Board existingPost = boardService.selectPostById(params.get("UNO"));
            if (existingPost == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
            }
            // 2. 권한 검증
            if (!existingPost.getID().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this post");
            }

            String uploadDir = env.getProperty("file.upload-dir");
            Path uploadPath = Paths.get(uploadDir);

            System.out.println("deletedFilesJson========"+deletedFilesJson);
            // 삭제된 파일 처리
            List<String> deletedFiles = new ObjectMapper().readValue(deletedFilesJson, new TypeReference<List<String>>() {});
            List<String> remainingFiles = new ArrayList<>();

            if (existingPost.getATT_FILE() != null && !existingPost.getATT_FILE().isEmpty()) {

                remainingFiles = new ArrayList<>(Arrays.asList(existingPost.getATT_FILE().split(";")));
                System.out.println("remainingFiles========"+remainingFiles);
                remainingFiles.removeAll(deletedFiles);
                System.out.println("remainingFiles========"+remainingFiles);
            }

            for (String deletedFile : deletedFiles) {
                Path filePath = uploadPath.resolve(deletedFile);
                Files.deleteIfExists(filePath);
            }

            StringBuilder fileNames = new StringBuilder(String.join(";", remainingFiles));

            if (files != null) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        if (fileNames.length() > 0 && !fileNames.toString().endsWith(";")) {
                            fileNames.append(";");
                        }
                        fileNames.append(fileName);
                    }
                }
            }

            existingPost.setATT_FILE(fileNames.toString());
            existingPost.setSUBJECT(params.get("SUBJECT"));
            existingPost.setCONTENT(params.get("CONTENT"));
            // 5. 게시글 업데이트
            boardService.updatePost(existingPost);

            return ResponseEntity.ok("/board/readBoard?id=" + existingPost.getUNO());
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating the post");
        }
    }

}
