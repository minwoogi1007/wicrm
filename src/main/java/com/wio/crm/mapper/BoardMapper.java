package com.wio.crm.mapper;

import com.wio.crm.model.Board;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<Board> findPostsByCategory(@Param("category") String category);
    List<Board> findAllPosts();
    List<Board> noticeBoardList();

    Board selectPostById(String id,String category);
    List<Board> selectComment(@Param("category") String category, @Param("id") Integer id);

    void insertComment(Board board);


    void insertPost(Board board);


    int  getNextUno(@Param("catGroup") String catGroup);


    int getReplyCount(@Param("catGroup") String catGroup, @Param("gno") String gno, @Param("replyDepth") String replyDepth);

    void updatePost(Board board); // 새로 추가된 메서드


    // 공지사항 댓글 조회(고객코드 없이)
    List<Board> selectCommentWithoutCustCode(@Param("id") Integer id);
    
    // 최신 공지사항 조회
    Board getLatestNotice();

}
