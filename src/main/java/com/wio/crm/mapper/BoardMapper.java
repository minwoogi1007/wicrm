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

    @Select("SELECT NVL(MAX(UNO), 0) + 1 FROM BOARD_CALL WHERE CAT_GROUP = #{catGroup}")
    int  getNextUno(String catGroup);

    @Select("SELECT COUNT(REPLY_DEPTH) FROM BOARD_CALL WHERE CAT_GROUP = #{catGroup} AND GNO = #{gno} AND REPLY_DEPTH LIKE #{replyDepth} || '%'")
    int getReplyCount(@Param("catGroup") String catGroup, @Param("gno") String gno, @Param("replyDepth") String replyDepth);

}
