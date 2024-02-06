package com.wio.crm.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wio.crm.model.Board;
import com.wio.crm.mapper.BoardMapper;
import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardMapper boardMapper;


    //사내 공지 게시판 조회
    public List<Board> getNoticeBoardList () {
    return boardMapper.noticeBoardList();
}

}
