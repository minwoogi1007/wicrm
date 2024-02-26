package com.wio.crm.service;

import com.wio.crm.mapper.Tcnt01EmpMapper;
import com.wio.crm.mapper.Temp01Mapper;
import com.wio.crm.mapper.TipdwMapper;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Temp01;
import com.wio.crm.model.Tipdw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TipdwMapper tipdwMapper;
    private final Tcnt01EmpMapper tcnt01EmpMapper;
    private final Temp01Mapper temp01Mapper;
    private final MenuService menuService;
    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    public CustomUserDetailsService(TipdwMapper tipdwMapper,
                                    Tcnt01EmpMapper tcnt01EmpMapper,
                                    Temp01Mapper temp01Mapper,
                                    MenuService menuService) {
        this.tipdwMapper = tipdwMapper;
        this.tcnt01EmpMapper = tcnt01EmpMapper;
        this.temp01Mapper = temp01Mapper;
        this.menuService = menuService;
    }

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        logger.info("Attempting to load user: {}", userid);
        Tipdw user = findUserByUserId(userid);

        List<SimpleGrantedAuthority> authorities = getUserAuthorities(user);
        logger.info("Authorities: {}", authorities);

        return new User(user.getUserid(), user.getPw(), authorities);
    }


    private Tipdw findUserByUserId(String userid) {
        Tipdw user = tipdwMapper.findByUserId(userid);
        if (user == null) {
            logger.info("아이디 없음");
            throw new UsernameNotFoundException("User not found");
        }
        if ("N".equals(user.getConfirmYn())) {
            logger.info("승인대기");
            throw new UsernameNotFoundException("Approval pending");
        }
        return user;
    }

    private List<SimpleGrantedAuthority> getUserAuthorities(Tipdw user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // 데이터베이스에서 사용자의 추가 정보 조회
        Tcnt01Emp tcntEmp;
        Temp01 tmp01;
        if ("0".equals(user.getGubn())) {

            authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
            tmp01 = temp01Mapper.findByUserId(user.getUsername());

            // Further logic if needed
        } else if ("1".equals(user.getGubn())) {

            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            tcntEmp = tcnt01EmpMapper.findByUserId(user.getUsername());
            // Further logic if needed
        }


        return authorities;
    }


}
