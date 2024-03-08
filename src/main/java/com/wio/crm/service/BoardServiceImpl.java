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
}
