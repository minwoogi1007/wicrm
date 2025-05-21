package com.wio.crm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.model.Board;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Temp01;
import com.wio.crm.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
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
        logger.info("에러 페이지 요청");
        return "error"; // Thymeleaf 템플릿 이름
    }

    private boolean isAuthorizedUser(Authentication authentication) {
        return authentication != null &&
                authentication.getName().equals("MINWOOGI");
    }
    @GetMapping("/board")
    public String board(Model model, @RequestParam("category") String category, Authentication authentication) {
        List<Board> posts;

        //System.out.println("Board method called with category: " + category);

        boolean canCreatePost = isAuthorizedUser(authentication);

        // 현재 로그인한 사용자의 CustomUserDetails 얻기
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // 사용자의 custCode를 가져와서 category로 설정 (G 카테고리는 제외)
            if (!"G".equals(category) && userDetails.getTcntUserInfo() != null) {
                String userCustCode = userDetails.getTcntUserInfo().getCustCode();
                if (userCustCode != null && !userCustCode.isEmpty()) {
                    // 사용자 custCode를 사용하여 category 업데이트
                    category = userCustCode;
                    //System.out.println("Updated category to user's custCode: " + category);
                }
            }

            // 'G' 카테고리인 경우 권한 체크 없이 진행
            if ("G".equals(category)) {
                //System.out.println("G.equals(category)========");
                posts = boardService.findPostsByCategory(category);
                
                // 데이터 검증 및 로그
                validateAndLogPosts(posts, category);
                
                model.addAttribute("list", posts);
                model.addAttribute("category", category);
                model.addAttribute("canCreatePost", canCreatePost);
                return "board/board";
            }

            // 사용자 정의 정보 사용
            // 내부직원
            Temp01 tempUser = userDetails.getTempUserInfo();
            // 외부직원 
            Tcnt01Emp tcntUser = userDetails.getTcntUserInfo();

            //System.out.println("tempUser========" + tempUser);
            //System.out.println("tcntUser========" + tcntUser);

            if (tempUser != null) {     // 내부직원
                canCreatePost = false;
            } else {                  // 외부직원
                if (tcntUser.getCustCode().equals(category)) {
                    canCreatePost = true;
                }
            }
            
            posts = boardService.findPostsByCategory(category);
            
            // 데이터 검증 및 로그
            validateAndLogPosts(posts, category);

            model.addAttribute("list", posts);
            model.addAttribute("category", category);
            //System.out.println("Board method - Category: " + category);
            model.addAttribute("canCreatePost", canCreatePost);
        }
        return "board/board";
    }
    
    // 게시물 데이터 검증 및 로그 출력
    private void validateAndLogPosts(List<Board> posts, String category) {
        //System.out.println("===== 게시물 데이터 검증 (" + category + ") =====");
        //System.out.println("총 게시물 수: " + (posts != null ? posts.size() : 0));
        
        if (posts != null && !posts.isEmpty()) {
            for (Board post : posts) {
                // 게시물 데이터 유효성 확인 (로그만 출력)
                //System.out.println("게시물: GNO=" + post.getGNO() 
                //    + ", 제목=" + post.getSUBJECT()
                //    + ", 작성자=" + post.getID() 
                //    + ", 날짜=" + post.getIN_DATE() 
                //    + ", 댓글수=" + post.getRE_COUNT()
                //    + ", 조회수=" + post.getHIT());
                
                // 날짜 형식 확인 (기본값 설정 제거, 로그만 남김)
                if (post.getIN_DATE() == null || post.getIN_DATE().trim().isEmpty()) {
                    //System.out.println("경고: GNO=" + post.getGNO() + " 게시물의 날짜가 비어있습니다.");
                }
                
                // 댓글 수 확인 (기본값 설정 제거, 로그만 남김)
                if (post.getRE_COUNT() == null || post.getRE_COUNT().trim().isEmpty()) {
                    //System.out.println("경고: GNO=" + post.getGNO() + " 게시물의 댓글 수가 비어있습니다.");
                }
                
                // 조회수 확인 (기본값 설정 제거, 로그만 남김)
                if (post.getHIT() == null || post.getHIT().trim().isEmpty()) {
                    //System.out.println("경고: GNO=" + post.getGNO() + " 게시물의 조회수가 비어있습니다.");
                }
            }
        } else {
            //System.out.println("게시물이 없거나 null입니다.");
        }
        //System.out.println("===== 검증 완료 =====");
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
            
            // 디렉토리 존재 확인 추가
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
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

                // 요청한 카테고리 매개변수 사용 (form에서 전달된 값)
                custCode = params.get("category");
                
                // 내부직원인 경우
                if (tempUser != null) {
                    id = tempUser.getUserId(); // 적절한 필드로 변경해야 함
                    empName = tempUser.getEmp_Name(); // Temp01 클래스의 올바른 메서드 이름으로 수정
                } 
                // 외부직원인 경우
                else if (tcntUser != null) {
                    id = tcntUser.getUserId();
                    empName = tcntUser.getEmp_name();
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("사용자 정보를 찾을 수 없습니다");
                }
            }

            // 현재 날짜와 시간을 특정 형식으로 설정
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());

            // Save post data
            Board board = new Board();
            board.setSUBJECT(params.get("title"));
            board.setCONTENT(params.get("content"));
            board.setATT_FILE(fileNames.toString());
            board.setCAT_GROUP(custCode);
            board.setID(id);
            board.setEMPNM(empName);
            board.setIN_DATE(formattedDate);
            
            //System.out.println("저장할 게시물 정보: " + board.getSUBJECT() + ", " + board.getCAT_GROUP() + ", " + board.getID() + ", " + board.getIN_DATE());
            
            boardService.insertPost(board);

            return ResponseEntity.ok("Saved successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + ex.getMessage());
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
                           @RequestParam("category") String category,
                           HttpServletRequest request,
                           Model model) {
        Board post;

        System.out.println("Full request URL: " + request.getRequestURL() + "?" + request.getQueryString());
        System.out.println("id: " + id);
        System.out.println("category: " + category);
        System.out.println("All request parameters:");
        request.getParameterMap().forEach((key, value) ->
                System.out.println(key + ": " + String.join(", ", value)));

            // 일반 게시글인 경우
            post = boardService.selectPostById(id,category);


        List<Board> comment;
        if ("G".equals(category)) {
            comment = boardService.selectCommentWithoutCustCode(id);
        } else {
            comment = boardService.selectComment(id,category);
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
            @RequestParam(value = "REPLY_DEPTH", required = false) String replyDepth) {

        // 각 파라미터가 잘 넘어오는지 확인
        System.out.println("=== 댓글 등록 요청 파라미터 ===");
        System.out.println("GNO: " + gno);
        System.out.println("UNO: " + uno);
        System.out.println("CAT_GROUP: " + catGroup);
        System.out.println("CONTENT: " + content);
        System.out.println("REPLY_DEPTH: " + replyDepth);
        System.out.println("=============================");
        
        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = "";
        String empName = "";
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            // 내부직원
            Temp01 tempUser = userDetails.getTempUserInfo();
            // 외부직원
            Tcnt01Emp tcntUser = userDetails.getTcntUserInfo();
            
            if (tempUser != null) {
                userId = tempUser.getUserId();
                empName = tempUser.getEmp_Name();
            } else if (tcntUser != null) {
                userId = tcntUser.getUserId();
                empName = tcntUser.getEmp_name();
            }
        } else {
            userId = authentication.getName();
        }
        
        // 현재 날짜와 시간을 특정 형식으로 설정
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date());
        
        // Board 객체 생성 및 설정
        Board board = new Board();
        board.setGNO(gno);
        board.setUNO(uno);
        board.setCAT_GROUP(catGroup);
        board.setCONTENT(content);
        board.setREPLY_DEPTH(replyDepth); // null이어도 서비스 레이어에서 처리
        board.setIN_DATE(formattedDate);
        board.setID(userId);
        board.setEMPNM(empName);
        
        System.out.println("=== 댓글 저장 데이터 확인 ===");
        System.out.println("ID: " + userId);
        System.out.println("EMPNM: " + empName);
        System.out.println("IN_DATE: " + formattedDate);
        System.out.println("===========================");

        // 댓글 저장 로직
        try {
            Board savedComment = boardService.saveComment(board);
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            System.err.println("댓글 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    @PostMapping("/board/update")
    public ResponseEntity<String> updateBoard(@RequestParam(value = "files", required = false) MultipartFile[] files,
                                              @RequestParam Map<String, String> params,
                                              @RequestParam("deletedFiles") String deletedFilesJson,
                                              Authentication authentication) {
        try {
            // 1. 데이터 무결성 확인 및 보안 검증

            Board existingPost = boardService.selectPostById(params.get("UNO"),params.get("CAT_GROUP"));


            if (existingPost == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
            }
            // 2. 권한 검증
            if (!existingPost.getID().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this post");
            }

            String uploadDir = env.getProperty("file.upload-dir");
            Path uploadPath = Paths.get(uploadDir);

            // 삭제된 파일 처리
            List<String> deletedFiles = new ObjectMapper().readValue(deletedFilesJson, new TypeReference<List<String>>() {});
            List<String> remainingFiles = new ArrayList<>();

            if (existingPost.getATT_FILE() != null && !existingPost.getATT_FILE().isEmpty()) {

                remainingFiles = new ArrayList<>(Arrays.asList(existingPost.getATT_FILE().split(";")));
                remainingFiles.removeAll(deletedFiles);
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

            // 현재 날짜와 시간을 특정 형식으로 설정
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());

            existingPost.setATT_FILE(fileNames.toString());
            existingPost.setSUBJECT(params.get("SUBJECT"));
            existingPost.setCONTENT(params.get("CONTENT"));
            existingPost.setCAT_GROUP(params.get("CAT_GROUP"));
            existingPost.setIN_DATE(formattedDate); // 날짜 갱신
            
            // 5. 게시글 업데이트
            boardService.updatePost(existingPost);


            return ResponseEntity.ok("/board/readBoard?id=" + existingPost.getUNO()+"&category=" + existingPost.getCAT_GROUP());
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while updating the post");
        }
    }

}
