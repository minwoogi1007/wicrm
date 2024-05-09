package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.BoardMapper;
import com.wio.crm.model.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardServiceImpl implements BoardService {
    private final BoardMapper boardMapper;

    private String getCurrentCustcode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ""; // Early return for null or incorrect type
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Using a ternary operator to simplify code
        return userDetails.getTcntUserInfo() != null ? userDetails.getTcntUserInfo().getCustCode() : "";
    }
    @Autowired
    public BoardServiceImpl(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    @Override
    public List<Board> findPostsByCategory(String category) {
        return boardMapper.findPostsByCategory(category);
    }

    @Override
    public List<Board> findAllPosts() {
        return boardMapper.findAllPosts();
    }

    @Override
    public Board selectPostById(String id) { // 반환 타입 추가
        String custCode = getCurrentCustcode(); // 고객 코드 조회
        return boardMapper.selectPostById(id,custCode);
    }

    @Override
    public void insertPost(Board board) { // 글쓰기 기능 구현
        boardMapper.insertPost(board); // 가정: BoardMapper에 insertPost 메소드가 정의되어 있음
    }
}
