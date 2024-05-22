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

    Board selectPostById(String id,String custCode);
    List<Board> selectComment(String custCode,String id);

    void insertComment(Board board);


    void insertPost(Board board);


    int  getNextUno(@Param("catGroup") String catGroup);


    int getReplyCount(@Param("catGroup") String catGroup, @Param("gno") String gno, @Param("replyDepth") String replyDepth);

}
