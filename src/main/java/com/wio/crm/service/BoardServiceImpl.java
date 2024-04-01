package com.wio.crm.service;

import com.wio.crm.mapper.BoardMapper;
import com.wio.crm.model.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardServiceImpl implements BoardService {
    private final BoardMapper boardMapper;

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
    public Board selectPostById(int id) { // 반환 타입 추가
        return boardMapper.selectPostById(id);
    }

    @Override
    public void insertPost(Board board) { // 글쓰기 기능 구현
        boardMapper.insertPost(board); // 가정: BoardMapper에 insertPost 메소드가 정의되어 있음
    }
}
