package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.exception.UserNotConfirmedException;
import com.wio.crm.mapper.Tcnt01EmpMapper;
import com.wio.crm.mapper.Temp01Mapper;
import com.wio.crm.mapper.TipdwMapper;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Temp01;
import com.wio.crm.model.Tipdw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final TipdwMapper tipdwMapper;
    private final Tcnt01EmpMapper tcnt01EmpMapper;
    private final Temp01Mapper temp01Mapper;

    public CustomUserDetailsService(TipdwMapper tipdwMapper, Tcnt01EmpMapper tcnt01EmpMapper, Temp01Mapper temp01Mapper) {
        this.tipdwMapper = tipdwMapper;
        this.tcnt01EmpMapper = tcnt01EmpMapper;
        this.temp01Mapper = temp01Mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        try {
            Tipdw user = tipdwMapper.findByUserId(userid);

            if (user == null) {
                throw new UsernameNotFoundException("User not found with userid: " + userid);
            }
            if ("N".equals(user.getConfirmYn())) {
                throw new UserNotConfirmedException("User with userid: " + userid + " is not confirmed yet.");
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            String custCode = null;
            Temp01 tempUser = null; // 내부직원 정보를 담을 객체
            Tcnt01Emp tcntUser = null; // 거래처 직원 정보를 담을 객체

            if ("0".equals(user.getGubn())) { // 내부 직원
                tempUser = temp01Mapper.findByUserId(userid);
                if (tempUser != null) {
                    if (tempUser == null || "0".equals(tempUser.getWork_gubn())) {
                        throw new AccountExpiredException("User with userid: " + userid + " is not currently active.");
                    }
                    authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
                }
            } else if ("1".equals(user.getGubn())) { // 거래처 직원
                tcntUser = tcnt01EmpMapper.findByUserId(userid);
                if (tcntUser == null) {
                    throw new AccountExpiredException("User with userid: " + userid + " is not currently active.");
                }
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                custCode = tcntUser.getCust_grade();
            }

            return new CustomUserDetails(userid, user.getPw(), authorities, custCode, tempUser, tcntUser);
        } catch (UserNotConfirmedException e) {
            logger.info(e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error authenticating user: {}", userid, e);
            throw new InternalAuthenticationServiceException("Error authenticating user: " + userid, e);
        }
    }

}
