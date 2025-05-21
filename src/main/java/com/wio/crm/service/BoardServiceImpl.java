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
    private final BoardMapper boardMapper;

    @Autowired
    public BoardServiceImpl(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    @Override
    public Board getLatestNotice() {
        return boardMapper.getLatestNotice();
    }


    @Override
    public List<Board> selectCommentWithoutCustCode(String id) {
        return boardMapper.selectCommentWithoutCustCode(Integer.parseInt(id));
    }
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

    @Override
    public List<Board> findPostsByCategory(String category) {
        return boardMapper.findPostsByCategory(category);
    }

    @Override
    public List<Board> findAllPosts() {
        return boardMapper.findAllPosts();
    }

    @Override
    public Board selectPostById(String id,String category) { // 반환 타입 추가

        return boardMapper.selectPostById(id,category);
    }

    @Override
    public List<Board> selectComment(String id,String category) {

        return boardMapper.selectComment(category, Integer.valueOf(id));
    }

    @Override
    public void insertPost(Board board) {

        boardMapper.insertPost(board);
    }

    @Override
    public Board saveComment(Board board) {
        // 댓글의 UNO, GNO, REPLY_DEPTH 값을 설정하는 로직

        // CAT_GROUP이 null인 경우 처리
        if (board.getCAT_GROUP() == null || board.getCAT_GROUP().trim().isEmpty()) {
            System.err.println("Warning: CAT_GROUP is null or empty. Setting user's CUST_CODE.");
            
            // 현재 로그인한 사용자의 CUST_CODE 가져오기
            String custCode = getCurrentCustcode();
            if (custCode != null && !custCode.isEmpty()) {
                board.setCAT_GROUP(custCode);
                //System.out.println("CAT_GROUP set from user's CUST_CODE: " + custCode);
            } else {
                // 로그인한 사용자의 CUST_CODE를 가져올 수 없는 경우 예외 발생
                throw new IllegalStateException("Cannot set CAT_GROUP: User's CUST_CODE is not available");
            }
        }

        int r_uno = boardMapper.getNextUno(board.getCAT_GROUP());

        board.setUNO(Integer.toString(r_uno));

        // REPLY_DEPTH가 null인 경우 기본값 'A' 설정
        if (board.getREPLY_DEPTH() == null || board.getREPLY_DEPTH().trim().isEmpty()) {
            board.setREPLY_DEPTH("A");
        }

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
        String userId = getCurrentUserId(); // 현재 사용자 ID 조회
        if("G".equals(board.getCAT_GROUP())){
            board.setID("MINWOOGI");
        }else{
            board.setID(userId);
        }


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
