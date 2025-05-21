package com.wio.crm.service;


import com.wio.crm.config.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.wio.crm.model.Board;
import com.wio.crm.mapper.BoardMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public interface BoardService {


    List<Board> findPostsByCategory(String category);
    List<Board> findAllPosts(); // 모든 게시글 조회
    Board selectPostById(String id,String category); // 글 읽기 기능 추가
    List<Board> selectComment(String id,String category);
    void insertPost(Board board); // 글쓰기 기능 추가
    Board saveComment(Board board);

    void updatePost(Board board); // 새로 추가된 메서드

    Board getLatestNotice();//메인 공지 모달창

   // Board selectPostByIdWithoutCustCode(String id);
    List<Board> selectCommentWithoutCustCode(String id);
}
