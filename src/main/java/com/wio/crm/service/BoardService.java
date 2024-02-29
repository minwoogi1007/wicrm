package com.wio.crm.service;


import com.wio.crm.config.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.wio.crm.model.Board;
import com.wio.crm.mapper.BoardMapper;
import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardMapper boardMapper;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String custCode = null;
    //사내 공지 게시판 조회
    public List<Board> getNoticeBoardList () {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        custCode = userDetails.getCustCode();
        if(custCode == null) custCode = "gongi";
    return boardMapper.noticeBoardList(custCode);
}

}
