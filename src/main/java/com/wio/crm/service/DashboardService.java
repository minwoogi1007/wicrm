package com.wio.crm.service;    /**
 * com.wio.crm.service
 * Created by IntelliJ IDEA
 * User: miro
 * Project: crm
 * Date: 2024-01-22
 * Time: 23:16
 */

import com.wio.crm.config.CustomUserDetails;
import com.wio.crm.mapper.DashboardMapper;
import com.wio.crm.mapper.Tcnt01EmpMapper;
import com.wio.crm.mapper.Temp01Mapper;
import com.wio.crm.model.DashboardData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DashboardService {

    // 로깅을 위한 로거
    private final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private final DashboardMapper dashboardMapper;
    private final Tcnt01EmpMapper tcnt01EmpMapper;
    private final Temp01Mapper temp01Mapper;
    @Autowired
    public DashboardService(Tcnt01EmpMapper tcnt01EmpMapper, Temp01Mapper temp01Mapper, DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
        this.tcnt01EmpMapper = tcnt01EmpMapper;
        this.temp01Mapper = temp01Mapper;
    }
    
    /**
     * 코드를 기반으로 TCNT01 테이블에서 직접 cust_grade 값을 조회합니다.
     * @param custCode 고객 코드
     * @return 고객 등급 (예: 'A', 'B' 등) 또는 null
     */
    private String getCustomerGradeDirectly(String custCode) {
        if (custCode == null || custCode.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 직접 TCNT01 테이블에서 cust_grade 조회
            Map<String, Object> params = new HashMap<>();
            params.put("custCode", custCode);
            return dashboardMapper.getCustomerGrade(params);
        } catch (Exception e) {
            logger.error("Error retrieving customer grade for custCode {}: {}", custCode, e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getTcntEmp() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 사용자의 CustomUserDetails 객체에서 custCode 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String tempUserGrade ="";
        String custGrade = "";

        Map<String, Object> data = new HashMap<>();
        //System.out.println("userDetails.getTcntUserInfo()===="+userDetails.getTcntUserInfo());
        
        // 현재 로그인한 유저 권한 확인
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        String loginUserAuthority = (String) session.getAttribute("loginUserAuthority");
        
        // 권한이 1인 경우 무조건 마일리지 데이터 표시
        if ("1".equals(loginUserAuthority)) {
            data.put("dataForA", true);
            //System.out.println("권한 1 사용자에게 마일리지 데이터 표시");
        }
        
        if(userDetails.getTempUserInfo()!= null){
            tempUserGrade = userDetails.getTempUserInfo().getPosition();
            //System.out.println("내부 직원");
            data.put("dataForA", true);
        }else{
             custGrade = userDetails.getTcntUserInfo().getCust_grade();
            //System.out.println("외부 직원");
        }

        // 거래처 직원 정보 접근
        if ("A".equals(custGrade)) {
            data.put("dataForA", true);
        }        // 내부 직원 정보 접근
        else if ("B".equals(custGrade)) {
            // 여기에 B 등급 사용자를 위한 데이터 준비 로직 추가
            data.put("dataForB", "B 등급 사용자에 대한 데이터");
        }
        //System.out.println("datadatadatadata===="+data);
        return data;
    }
    public Map<String, Object> getDashboardData(String username) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 사용자의 CustomUserDetails 객체에서 custCode 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Map<String, Object> data = new HashMap<>();
        // 데이터베이스 조회
        String custCode=getCurrentUserCustCode();

        DashboardData card1Data = dashboardMapper.findDataForCard1(custCode);
        DashboardData card2Data = dashboardMapper.findDataForCard2(custCode);
        
        // 포인트 리스트 조회 및 로깅
        List<DashboardData> pointList = dashboardMapper.findPointList(custCode);
        
        // 포인트 리스트 로깅
        if (pointList != null && !pointList.isEmpty()) {
            logger.info("findPointList 조회 결과 - 항목 수: {}", pointList.size());
            for (int i = 0; i < pointList.size(); i++) {
                DashboardData item = pointList.get(i);
                logger.info("포인트 항목 [{}] - CS_TYPE: '{}', dailyPoint: {}, dailyPointN: {}", 
                    i, item.getCs_type(), item.getDailyPoint(), item.getDailyPointN());
            }
        } else {
            logger.warn("findPointList 조회 결과 없음 또는 빈 리스트");
        }
        
        DashboardData dashConSum = dashboardMapper.dashConSum(custCode);

        // 로그 강화: 필드 상태를 정밀하게 확인하기 위한 디버깅 로그 추가
        logger.info("[원본 데이터 확인] card1Data 원래 상태: count_Com=[{}](타입:{}), todayCom=[{}](타입:{}), todayMiss=[{}](타입:{}), todayEme=[{}](타입:{}), processing_rate=[{}](타입:{})",
                card1Data.getCount_Com(), 
                card1Data.getCount_Com() != null ? card1Data.getCount_Com().getClass().getName() : "null",
                card1Data.getTodayCom(), 
                card1Data.getTodayCom() != null ? card1Data.getTodayCom().getClass().getName() : "null",
                card1Data.getTodayMiss(), 
                card1Data.getTodayMiss() != null ? card1Data.getTodayMiss().getClass().getName() : "null",
                card1Data.getTodayEme(), 
                card1Data.getTodayEme() != null ? card1Data.getTodayEme().getClass().getName() : "null",
                card1Data.getProcessing_rate(),
                card1Data.getProcessing_rate() != null ? card1Data.getProcessing_rate().getClass().getName() : "null");
        
        // 첫 번째 단계: todayCom <-> count_Com 동기화
        if (card1Data.getCount_Com() == null && card1Data.getTodayCom() != null) {
            card1Data.setCount_Com(card1Data.getTodayCom());
            logger.info("[동기화] 1단계: todayCom({})로부터 count_Com 설정", card1Data.getTodayCom());
        } 
        else if (card1Data.getTodayCom() == null && card1Data.getCount_Com() != null) {
            card1Data.setTodayCom(card1Data.getCount_Com());
            logger.info("[동기화] 1단계: count_Com({})으로부터 todayCom 설정", card1Data.getCount_Com());
        }
        
        // 두 번째 단계: count_Com이 여전히 null이면 로깅만 함
        if (card1Data.getCount_Com() == null || "0".equals(card1Data.getCount_Com())) {
            logger.info("[확인] count_Com이 null 또는 0, 실제 쿼리 결과값 사용");
        }
        
        // 세 번째 단계: processing_rate 로깅만 수행
        if (card1Data.getProcessing_rate() == null || "0".equals(card1Data.getProcessing_rate()) || "0.0".equals(card1Data.getProcessing_rate())) {
            logger.info("[확인] processing_rate가 SQL에서 null 또는 0으로 반환됨");
        } else {
            logger.info("[확인] SQL에서 계산된 processing_rate 값 사용: {}", card1Data.getProcessing_rate());
        }
        
        // 설정 이후 count_sum 계산 (필요한 경우)
        if (card1Data.getCount_sum() == null || "0".equals(card1Data.getCount_sum())) {
            try {
                if (card1Data.getTodayMiss() != null && card1Data.getCount_Com() != null) {
                    int miss = Integer.parseInt(card1Data.getTodayMiss());
                    int complete = Integer.parseInt(card1Data.getCount_Com());
                    int sum = miss + complete;
                    card1Data.setCount_sum(String.valueOf(sum));
                    logger.info("[계산] count_sum을 {}로 계산하여 설정", sum);
                }
            } catch (Exception e) {
                logger.error("[오류] count_sum 계산 중 예외 발생: {}", e.getMessage());
            }
        }

        // 추가: 어제, 일주일, 이번달 데이터 조회
        DashboardData yesterdayCard1Data = dashboardMapper.findYesterdayDataForCard1(custCode);
        DashboardData weeklyCard1Data = dashboardMapper.findWeeklyDataForCard1(custCode);
        DashboardData monthlyCard1Data = dashboardMapper.findMonthlyDataForCard1(custCode);
        
        DashboardData yesterdayCard2Data = dashboardMapper.findYesterdayDataForCard2(custCode);
        DashboardData weeklyCard2Data = dashboardMapper.findWeeklyDataForCard2(custCode);
        DashboardData monthlyCard2Data = dashboardMapper.findMonthlyDataForCard2(custCode);
        
        // 어제, 일주일, 이번달 데이터도 동일한 문제가 있을 수 있으므로 유사한 수정 적용
        processCard1Data(yesterdayCard1Data, "Yesterday");
        processCard1Data(weeklyCard1Data, "Weekly");
        processCard1Data(monthlyCard1Data, "Monthly");
        
        // 최종 상태 확인
        logger.info("[최종 확인] 카드1 데이터(수정 후): {count_Com={}, todayCom={}, todayMiss={}, todayEme={}, processing_rate={}, count_sum={}}",
                card1Data.getCount_Com(), card1Data.getTodayCom(), card1Data.getTodayMiss(), 
                card1Data.getTodayEme(), card1Data.getProcessing_rate(), card1Data.getCount_sum());
        
        data.put("card-data-1", card1Data);
        data.put("card-data-2", card2Data);
        data.put("card-data-3", dashConSum);
        data.put("pointlist-data", pointList);

        // 추가: 어제, 일주일, 이번달 데이터
        data.put("yesterday-card-data-1", yesterdayCard1Data);
        data.put("weekly-card-data-1", weeklyCard1Data);
        data.put("monthly-card-data-1", monthlyCard1Data);
        
        data.put("yesterday-card-data-2", yesterdayCard2Data);
        data.put("weekly-card-data-2", weeklyCard2Data);
        data.put("monthly-card-data-2", monthlyCard2Data);
        
        return data;
    }
    
    /**
     * Card1Data 객체에서 count_Com과 processing_rate 필드가 null 또는 0인 경우 처리
     * @param card1Data 처리할 카드 데이터 객체
     * @param prefix 로그 메시지용 접두사
     */
    private void processCard1Data(DashboardData card1Data, String prefix) {
        if (card1Data == null) {
            logger.warn("{} card1Data is null", prefix);
            return;
        }
        
        // 로그 출력으로 상태 확인
        logger.debug("{} card1Data 상태: count_Com=[{}], todayCom=[{}], processing_rate=[{}]",
            prefix, card1Data.getCount_Com(), card1Data.getTodayCom(), card1Data.getProcessing_rate());
            
        // count_Com이 null이면 todayCom의 값을 사용
        if (card1Data.getCount_Com() == null) {
            if (card1Data.getTodayCom() != null) {
                card1Data.setCount_Com(card1Data.getTodayCom());
                logger.debug("{} card1Data.count_Com을 todayCom 값으로 설정: {}", prefix, card1Data.getCount_Com());
            }
        }
        
        // processing_rate 로깅만 수행
        if (card1Data.getProcessing_rate() == null || "0".equals(card1Data.getProcessing_rate())) {
            logger.debug("{} processing_rate가 SQL에서 null 또는 0으로 반환됨", prefix);
        } else {
            logger.debug("{} SQL에서 반환된 processing_rate 값 사용: {}", prefix, card1Data.getProcessing_rate());
        }
    }
    //로그인 유저별 코드 (거래처 는 업체 코드)
    private String getCurrentUserCustCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ""; // CustomUserDetails가 아니면 빈 문자열 반환
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 거래처 직원 정보 접근
        if (userDetails.getTcntUserInfo() != null) {
            // custCode가 null인지 확인
            String custCode = userDetails.getTcntUserInfo().getCustCode();
            if (custCode == null || custCode.isEmpty()) {
                //System.out.println("WARNING: getTcntUserInfo().getCustCode() is null or empty.");
                return "P000000011"; // 기본값 설정 - 실제 가능한 코드로 변경 필요
            }
            return custCode;
        }

        // 내부 직원 정보 접근
        if (userDetails.getTempUserInfo() != null) {
            // 내부 직원에 대한 처리가 필요한 경우 여기에 로직 추가
            //System.out.println("Accessing Temp01 UserInfo");
            // 예: return userDetails.getTempUserInfo().getSomeOtherInfo();
        }

        return "";
    }
    public Map<String, Object> getDashboardCallCount(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        
        logger.info("getDashboardCallCount 호출됨 - username: {}, custCode: {}", username, custCode);
        
        try {
            List<DashboardData> callCountList = dashboardMapper.getDashboardCallCount(custCode);
            
            logger.info("SQL 쿼리 실행 후 callCountList size: {}", callCountList != null ? callCountList.size() : "null");
            
            if (callCountList != null && !callCountList.isEmpty()) {
                DashboardData firstItem = callCountList.get(0);
                logger.info("첫 번째 항목 필드 확인: gubn=[{}], prc_Date=[{}], hour_09=[{}], hour_10=[{}], hour_11=[{}], hour_12=[{}], callSum=[{}]",
                    firstItem.getGubn(), 
                    firstItem.getPrc_Date(),
                    firstItem.getHour_09(), 
                    firstItem.getHour_10(),
                    firstItem.getHour_11(),
                    firstItem.getHour_12(),
                    firstItem.getCallSum());
                
                // 각 항목 데이터 확인 로깅 추가
                for (int i = 0; i < callCountList.size(); i++) {
                    DashboardData item = callCountList.get(i);
                    logger.info("항목 #{}: gubn=[{}], prc_Date=[{}], hour_09=[{}], hour_10=[{}], hour_11=[{}], hour_12=[{}], callSum=[{}]", 
                        i+1, item.getGubn(), item.getPrc_Date(), item.getHour_09(), item.getHour_10(), item.getHour_11(), item.getHour_12(), item.getCallSum());
                }
            } else {
                logger.warn("callCountList가 비어있거나 null입니다. SQL 쿼리 결과가 없습니다.");
            }
            
            // 결과를 data 맵에 추가
            data.put("dashCallCount-data", callCountList);
            logger.info("data 맵에 callCountList 추가 완료. data 크기: {}", data.size());
        } catch (Exception e) {
            logger.error("getDashboardCallCount 처리 중 오류 발생", e);
            data.put("error", e.getMessage());
        }

        //System.out.println("data===="+data);
        return data;
    }

    public Map<String, Object> getDashboardPersonCount(String username) {
        String custCode = getCurrentUserCustCode();
        Map<String, Object> data = new HashMap<>();
        
        // 데이터 키 변경: dashStatCount-data -> dashPersonCount-data
        data.put("dashPersonCount-data", dashboardMapper.getDashboardPersonCount(custCode));
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
    public Map<String, Object> getCustomList() {
        checkUserRole();
        Map<String, Object> data = new HashMap<>();

        data.put("customList", dashboardMapper.getCustomList());
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
    
    /**
     * 사용자 ID를 기반으로 N_TCNT01_EMP 테이블에서 직접 authority 값을 조회합니다.
     * @param userId 사용자 ID
     * @return 권한 정보 또는 null
     */
    private String getUserAuthorityDirectly(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 직접 N_TCNT01_EMP 테이블에서 authority 조회
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            return dashboardMapper.getUserAuthority(params);
        } catch (Exception e) {
            logger.error("Error retrieving user authority for userId {}: {}", userId, e.getMessage());
            return null;
        }
    }
    /**
     * 사용자가 특정 권한을 가지고 있는지 확인하는 헬퍼 메서드
     * @param authentication 인증 객체
     * @param authority 확인할 권한
     * @return 사용자가 해당 권한을 가진 경우 true, 그렇지 않은 경우 false
     */
    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    public void checkUserRole() {
        // 현재 인증된 사용자의 Authentication 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            //System.out.println("사용자가 로그인하지 않았습니다.");
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
            //System.out.println("사용자는 직원(EMPLOYEE)입니다.");
        } else if (isUser) {
            //System.out.println("사용자는 거래처(USER)입니다.");
        } else {
            //System.out.println("사용자는 알려진 권한이 없습니다.");
        }
    }

    public void printUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // 사용자의 기본 인증 정보 출력
            //System.out.println("Username: " + authentication.getName());
            //System.out.println("Credentials: " + authentication.getCredentials());
            //System.out.println("Authorities: " + authentication.getAuthorities());

            // UserDetails 객체에서 추가적인 사용자 정보를 조회
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                //System.out.println("Username: " + userDetails.getUsername());
                //System.out.println("Password: " + userDetails.getPassword());
                //System.out.println("Authorities: " + userDetails.getAuthorities());
            }

            // 세션 ID와 세션에 저장된 모든 속성 출력
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false); // false: 기존 세션이 있을 경우에만 가져옴
            if (session != null) {
                //System.out.println("Session ID: " + session.getId());
                java.util.Enumeration<String> attributeNames = session.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String attributeName = attributeNames.nextElement();
                    //System.out.println("Session attribute Name: " + attributeName + ", Value: " + session.getAttribute(attributeName));
                }
            }
        }
    }
    @Transactional
    public Map<String, Object> getDashBoardPoint(String username) {

        Map<String, Object> data = new HashMap<>();
        DashboardData point = null;
        List<DashboardData> pointList = null;

        // 사용자 정보 및 거래처 코드 조회
        String custCode = getCurrentUserCustCode();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 로그 추가 - 사용자 유형 확인
        if (userDetails.getTcntUserInfo() != null) {
            logger.debug("Vendor employee user - accessing point data");
        } else if (userDetails.getTempUserInfo() != null) {
            logger.debug("Temp employee user - accessing point data");
        }

        try {
            if (custCode != null) {
                point = dashboardMapper.getPoint(custCode);
                pointList = dashboardMapper.getPointList(custCode);
            } else {
                point = dashboardMapper.getPoint(null);
                pointList = dashboardMapper.getPointList(null);
            }

            // 리스트 내용 로깅 및 처리
            if (pointList != null && !pointList.isEmpty()) {
                for (int i = 0; i < pointList.size(); i++) {
                    DashboardData item = pointList.get(i);
                    if (item == null) {
                        logger.warn("Invalid point data found at index {}", i);
                        // null 항목 대신 새 객체로 대체
                        item = new DashboardData();
                        item.setSUM_POINT(0);
                        item.setPOINT_DATE("N/A");
                    }
                }
            } else {
                logger.info("No point data found for user {}", username);
            }
        } catch (Exception e) {
            logger.error("포인트 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
        }

        data.put("point", point);
        data.put("pointList", pointList);

        return data;
    }
    /**
     * 대시보드 카운트 데이터 조회 - 사용자 권한과 등급에 따라 분기
     */
    @Transactional
    public Map<String, Object> getDashBoardCount(String username) {

        Map<String, Object> data = new HashMap<>();
        DashboardData point = null;
        List<DashboardData> pointList = null;

        try {
            // 사용자 정보 및 거래처 코드 조회
            String custCode = getCurrentUserCustCode();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            logger.debug("현재 사용자: {}, custCode: {}", username, custCode);

            // 거래처 직원인 경우 (외부 사용자)
            if (userDetails.getTcntUserInfo() != null) {
                // 사용자 정보 상세 로그
                logger.debug("Count query - user details: {}", userDetails.getTcntUserInfo());
                
                // 직접 DB에서 정보 조회
                String userId = userDetails.getTcntUserInfo().getUserId();
                String authority = getUserAuthorityDirectly(userId);
                String grade = getCustomerGradeDirectly(custCode);
                
                // 사용자 권한 및 등급 확인
                boolean isAdmin = hasAuthority(authentication, "ROLE_ADMIN");
                logger.debug("Count data query - User authority from DB: [{}], grade from DB: [{}]", authority, grade);
                
                if (grade != null && "A".equals(grade)) {
                    // A 등급 사용자의 경우 처리
                    logger.debug("Count data for Grade A user: {}", custCode);
                    // A 등급에 필요한 데이터 처리
                } else if (grade != null && "B".equals(grade)) {
                    logger.debug("Count data for Grade B user: {}", custCode);
                    point = dashboardMapper.getCount(custCode);
                    pointList = dashboardMapper.getCountSum(custCode);
                } else {
                    // 기본 처리 (등급이 null이거나 기타 값인 경우)
                    logger.debug("Count data for default case (grade=[{}]): {}", grade, custCode);
                    point = dashboardMapper.getCount(custCode);
                    pointList = dashboardMapper.getCountSum(custCode);
                }
            } 
            // 내부 직원인 경우 (내부 사용자)
            else if (userDetails.getTempUserInfo() != null) {
                logger.debug("Query count data for internal employee");
                point = dashboardMapper.getCount(custCode);
                pointList = dashboardMapper.getCountSum(custCode);
            }

            // 확인 후 데이터가 null이거나 빈 리스트인 경우 처리
            if (pointList == null) {
                logger.warn("Count: pointList is null, creating empty list");
                pointList = new ArrayList<>();
            }

            // 포인트 정보가 null인 경우 새로운 객체로 처리
            List<DashboardData> processedPointList = new ArrayList<>();

            // 리스트 내용 로깅 및 처리
            logger.info("Count: PointList size: {}", pointList.size());
            if (!pointList.isEmpty()) {
                for (int i = 0; i < pointList.size(); i++) {
                    DashboardData item = pointList.get(i);
                    if (item == null) {
                        logger.warn("Count: Item at index {} is null", i);
                        // null 항목은 새로운 객체로 대체
                        item = new DashboardData();
                        item.setSUM_POINT(0);
                        item.setPOINT_DATE("N/A");
                    } else {
                        logger.info("Count: Item {}: SUM_POINT={}, POINT_DATE={}", 
                            i, item.getSUM_POINT(), item.getPOINT_DATE());
                    }
                    // 객체 추가
                    processedPointList.add(item);
                }
            }
            
            data.put("point", point);
            data.put("pointList", processedPointList); // 처리된 리스트로 교체
        } catch (Exception e) {
            logger.error("카운트 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            data.put("point", null);
            data.put("pointList", new ArrayList<>());
        }
        
        return data;
    }
}
