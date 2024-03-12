package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.Tcnt01EmpMapper;
import com.wio.crm.mapper.Temp01Mapper;
import com.wio.crm.mapper.TipdwMapper;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Temp01;
import com.wio.crm.model.Tipdw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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

    public CustomUserDetailsService(TipdwMapper tipdwMapper, Tcnt01EmpMapper tcnt01EmpMapper,
                                    Temp01Mapper temp01Mapper, MenuService menuService) {
        this.tipdwMapper = tipdwMapper;
        this.tcnt01EmpMapper = tcnt01EmpMapper;
        this.temp01Mapper = temp01Mapper;
        this.menuService = menuService;
    }

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        try {
            logger.info("Attempting to load user: {}", userid);
            Tipdw user = findUserByUserId(userid);

            if (user == null) {
                throw new UsernameNotFoundException("User not found with userid: " + userid);
            }

            List<SimpleGrantedAuthority> authorities = getUserAuthorities(user);
            logger.info("User: {} has authorities: {}", user.getUserid(), authorities);

            // cust_code 값을 처리하는 부분 추가
            String custCode = null; // custCode 초기화
            if ("1".equals(user.getGubn())) {
                Tcnt01Emp tcnt01Emp = tcnt01EmpMapper.findByUserId(userid);
                if (tcnt01Emp != null) {
                    custCode = tcnt01Emp.getCustCode(); // custCode 값 할당
                }
            }
            logger.info("user.getGubn: {}",user.getGubn());
            logger.info("custCode: {}",custCode);

            return new CustomUserDetails(user.getUserid(), user.getPw(), authorities, custCode);
        } catch (Exception e) {
            logger.error("An error occurred while trying to authenticate the user: {}", userid, e);
            throw new InternalAuthenticationServiceException("An error occurred while authenticating the user: " + userid, e);
        }
    }

    private Tipdw findUserByUserId(String userid) {
        Tipdw user = tipdwMapper.findByUserId(userid);
        if (user == null) {
            logAndThrowUsernameNotFoundException("User not found for userid: " + userid);
        }
        if ("N".equals(user.getConfirmYn())) {
            logAndThrowUsernameNotFoundException("Approval pending for userid: " + userid);
        }
        return user;
    }

    private void logAndThrowUsernameNotFoundException(String message) {
        logger.info(message);
        throw new UsernameNotFoundException(message);
    }

    private List<SimpleGrantedAuthority> getUserAuthorities(Tipdw user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        switch (user.getGubn()) {
            case "0": // EMPLOYEE
              //  Temp01 temp01 = temp01Mapper.findByUserId(user.getUserid());
              //  if (temp01 != null) {
                    System.out.println("이건 직원용 ROLE_EMPLOYEE");
                    authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
                    // 여기에서 temp01 정보를 기반으로 추가적인 권한을 부여하거나, 처리할 수 있습니다.
                    logger.info("Employee info retrieved for user: {}", user.getUserid());
             //   } else {
              //      logger.warn("No additional employee info found for user: {}", user.getUserid());
             //   }
                break;
            case "1": // USER
              //  Tcnt01Emp tcnt01Emp = tcnt01EmpMapper.findByUserId(user.getUserid());
              //  if (tcnt01Emp != null) {
                    System.out.println("이건 거래처용 ROLE_USER");
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    // 여기에서 tcnt01Emp 정보를 기반으로 추가적인 권한을 부여하거나, 처리할 수 있습니다.
                    logger.info("User info retrieved for user: {}", user.getUserid());
              // } else {
              //      logger.warn("No additional user info found for user: {}", user.getUserid());
              //  }
                break;
            default:
                logger.warn("Unknown user type: {}", user.getGubn());
                break;
        }
        return authorities;
    }
}
