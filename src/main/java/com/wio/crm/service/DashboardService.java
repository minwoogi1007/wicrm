package com.wio.crm.service;

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.DashboardMapper;
import com.wio.crm.model.DashboardData;
import com.wio.crm.model.Tcnt01Emp;
import com.wio.crm.model.Temp01;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    @Autowired
    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }
    public Map<String, Object> getDashboardData(String username) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 사용자의 CustomUserDetails 객체에서 custCode 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Temp01 tempUserInfo = userDetails.getTempUserInfo(); // 내부직원 정보 접근
        Tcnt01Emp tcntUserInfo = userDetails.getTcntUserInfo(); // 거래처 직원 정보 접근
        String custCode = null;

        if (tcntUserInfo != null && tcntUserInfo.getCustCode() != null) {
            custCode = tcntUserInfo.getCustCode();
            System.out.println("CustCode from Tcnt01Emp: " + custCode);
        } else if (tempUserInfo != null) {
            // tempUserInfo 사용 시 관련 로직
            // 예: custCode = tempUserInfo.getSomeOtherInfo();
            custCode = "";
            System.out.println("Accessing Temp01 UserInfo");
        }
        Map<String, Object> data = new HashMap<>();
        // 데이터베이스 조회
        DashboardData card1Data = dashboardMapper.findDataForCard1(custCode);
        DashboardData card2Data = dashboardMapper.findDataForCard2(custCode);
        List<DashboardData> pointList = dashboardMapper.findPointList(custCode); //
        DashboardData dashConSum = dashboardMapper.dashConSum(custCode);

        data.put("card-data-1", card1Data);
        data.put("card-data-2", card2Data);
        data.put("card-data-3", dashConSum);
        data.put("pointlist-data", pointList);

        return data;
    }
    //로그인 유저별 코드 (거래처 는 업체 코드)
    private String getCurrentUserCustCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getCustCode();
        }
        return "";
    }
    public Map<String, Object> getDashboardCallCount(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dashStatCount-data", dashboardMapper.getDashboardCallCount(custCode));
        return data;
    }

    public Map<String, Object> getDashboardPersonCount(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dashStatCount-data", dashboardMapper.getDashboardPersonCount(custCode));
        return data;
    }

    public Map<String, Object> getDashboardMonth(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dashMonth-data", dashboardMapper.getDashboardMonth(custCode));
        return data;
    }
    public Map<String, Object> getEmployeeList() {
        checkUserRole();
        Map<String, Object> data = new HashMap<>();
        data.put("employeeList", dashboardMapper.getEmployeeList());
        return data;
    }

    public Map<String, Object> getDailyAve(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dailyAve", dashboardMapper.getDailyAve(custCode));
        return data;
    }
    public Map<String, Object> getWeeklySum(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dailyAve", dashboardMapper.getWeeklySum(custCode));
        return data;
    }
    public Map<String, Object> getMonthlySum(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        data.put("dailyAve", dashboardMapper.getMonthlySum(custCode));
        return data;
    }
    public void checkUserRole() {
        // 현재 인증된 사용자의 Authentication 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            System.out.println("사용자가 로그인하지 않았습니다.");
            return;
        }

        // 사용자의 권한 정보를 추출하고, ROLE_EMPLOYEE 또는 ROLE_USER 인지 확인
        boolean isEmployee = false;
        boolean isUser = false;

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_EMPLOYEE")) {
                isEmployee = true;
            } else if (authority.getAuthority().equals("ROLE_USER")) {
                isUser = true;
            }
        }

        // 권한에 따라 다른 로직 수행
        if (isEmployee) {
            System.out.println("사용자는 직원(EMPLOYEE)입니다.");
        } else if (isUser) {
            System.out.println("사용자는 거래처(USER)입니다.");
        } else {
            System.out.println("사용자는 알려진 권한이 없습니다.");
        }
    }

    public void printUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // 사용자의 기본 인증 정보 출력
            System.out.println("Username: " + authentication.getName());
            System.out.println("Credentials: " + authentication.getCredentials());
            System.out.println("Authorities: " + authentication.getAuthorities());

            // UserDetails 객체에서 추가적인 사용자 정보를 조회
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                System.out.println("Username: " + userDetails.getUsername());
                System.out.println("Password: " + userDetails.getPassword());
                System.out.println("Authorities: " + userDetails.getAuthorities());
            }

            // 세션 ID와 세션에 저장된 모든 속성 출력
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false); // false: 기존 세션이 있을 경우에만 가져옴
            if (session != null) {
                System.out.println("Session ID: " + session.getId());
                java.util.Enumeration<String> attributeNames = session.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String attributeName = attributeNames.nextElement();
                    System.out.println("Session attribute Name: " + attributeName + ", Value: " + session.getAttribute(attributeName));
                }
            }
        }
    }
}
