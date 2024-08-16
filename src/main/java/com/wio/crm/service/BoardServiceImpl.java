package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.BoardMapper;
import com.wio.crm.model.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BoardServiceImpl implements BoardService {
    @Autowired
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
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ""; // Early return for null or incorrect type
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Using a ternary operator to simplify code
        return userDetails.getTcntUserInfo() != null ? userDetails.getTcntUserInfo().getUserId() : "";
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
    public List<Board> selectComment(String id) {
        String custCode = getCurrentCustcode(); // 고객 코드 조회

        return boardMapper.selectComment(custCode,id);
    }

    @Override
    public void insertPost(Board board) {

        boardMapper.insertPost(board);
    }

    @Override
    public Board saveComment(Board board) {
        // 댓글의 UNO, GNO, REPLY_DEPTH 값을 설정하는 로직


        int r_uno = boardMapper.getNextUno(board.getCAT_GROUP());

        board.setUNO(Integer.toString(r_uno));


        int c_reply = boardMapper.getReplyCount(board.getCAT_GROUP(), board.getGNO(), board.getREPLY_DEPTH());
        board.setID(getCurrentUserId());

        String r_reply = getReplyDepth(c_reply);




        board.setREPLY_DEPTH(board.getREPLY_DEPTH() + r_reply);

        // 현재 날짜와 시간을 설정

        boardMapper.insertComment(board);
        return board;
    }

    @Override
    public void updatePost(Board board) {
        String custCode = getCurrentCustcode(); // 고객 코드 조회
        String userId = getCurrentUserId(); // 현재 사용자 ID 조회

        board.setCAT_GROUP(custCode);
        board.setID(userId);

        // 기존 게시글 정보 조회


        boardMapper.updatePost(board);
    }


    private String getReplyDepth(int c_reply) {
        switch (c_reply) {
            case 1: return "A";
            case 2: return "B";
            case 3: return "C";
            case 4: return "D";
            case 5: return "E";
            case 6: return "F";
            case 7: return "G";
            case 8: return "H";
            case 9: return "I";
            case 10: return "J";
            case 11: return "K";
            case 12: return "L";
            case 13: return "M";
            case 14: return "N";
            case 15: return "0";
            case 16: return "P";
            case 17: return "Q";
            case 18: return "R";
            case 19: return "S";
            case 20: return "T";
            case 21: return "U";
            case 22: return "V";
            default: return "";
        }
    }
}
