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

    void updatePost(Board board); // 새로 추가된 메서드

    @Select("SELECT * FROM BOARD_CALL WHERE CAT_GROUP = 'G' AND UNO = (SELECT MAX(UNO) FROM BOARD_CALL WHERE CAT_GROUP = 'G')")
    Board getLatestNotice();

    @Select("SELECT\n" +
            "            gno,\n" +
            "            uno,\n" +
            "            REPLY_DEPTH,\n" +
            "            CAT_GROUP,\n" +
            "            SUBJECT,\n" +
            "            EMPNM,\n" +
            "            IN_DATE,\n" +
            "            ID,\n" +
            "            CONTENT,\n" +
            "            ATT_FILE\n" +
            "        FROM BOARD_CALL\n" +
            "        WHERE CAT_GROUP = 'G'\n" +
            "          AND uno=#{id}")
    Board selectPostByIdWithoutCustCode(String id);

    @Select("SELECT\n" +
            "            gno,\n" +
            "            uno,\n" +
            "            REPLY_DEPTH,\n" +
            "            CAT_GROUP,\n" +
            "            SUBJECT,\n" +
            "            EMPNM,\n" +
            "            TO_DATE(IN_DATE,'YYYY-MM-DD HH24:MI:SS')  IN_DATE,\n" +
            "            ID,\n" +
            "            CONTENT,\n" +
            "            ATT_FILE\n" +
            "        FROM BOARD_CALL\n" +
            "        WHERE CAT_GROUP = 'G'\n" +
            "          AND gno=(select gno from board_call where uno= #{id} and CAT_GROUP ='G')\n" +
            "          AND SUBJECT IS NULL\n" +
            "        ORDER BY IN_DATE")
    List<Board> selectCommentWithoutCustCode(String id);

}
