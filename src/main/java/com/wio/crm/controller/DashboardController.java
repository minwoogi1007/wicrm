package com.wio.crm.controller;
import com.wio.crm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData(Principal principal) {
        // SecurityContext에서 인증 객체를 가져옵니다.
        String username = principal.getName();
        dashboardService.printUserDetails();
        Map<String, Object> data = dashboardService.getDashboardData(username);
        
        // 데이터 로깅 추가
        //System.out.println("======== 대시보드 데이터 전송 시작 ========");
        if (data != null) {
            //System.out.println("전체 데이터 키: " + data.keySet());
            
            // 주요 데이터 항목 상세 로깅
            if (data.containsKey("card-data-1")) {
                //System.out.println("card-data-1 상세:");
                Object cardData1Obj = data.get("card-data-1");
                //System.out.println("  타입: " + (cardData1Obj != null ? cardData1Obj.getClass().getName() : "null"));
                
                if (cardData1Obj instanceof com.wio.crm.model.DashboardData) {
                    com.wio.crm.model.DashboardData cardData1 = (com.wio.crm.model.DashboardData) cardData1Obj;
                    //System.out.println("  count_Miss: " + cardData1.getCount_Miss());
                    //System.out.println("  count_Com: " + cardData1.getCount_Com());
                    //System.out.println("  count_sum: " + cardData1.getCount_sum());
                    //System.out.println("  processing_rate: " + cardData1.getProcessing_rate());
                    //System.out.println("  todayMiss: " + cardData1.getTodayMiss());
                    //System.out.println("  todayCom: " + cardData1.getTodayCom());
                    //System.out.println("  todayEme: " + cardData1.getTodayEme());
                } else {
                    //System.out.println("  내용: " + cardData1Obj);
                }
            }
            
            if (data.containsKey("card-data-2")) {
                //System.out.println("card-data-2 상세:");
                Object cardData2Obj = data.get("card-data-2");
                //System.out.println("  타입: " + (cardData2Obj != null ? cardData2Obj.getClass().getName() : "null"));
                
                if (cardData2Obj instanceof com.wio.crm.model.DashboardData) {
                    com.wio.crm.model.DashboardData cardData2 = (com.wio.crm.model.DashboardData) cardData2Obj;
                    //System.out.println("  dailyPoint: " + cardData2.getDailyPoint());
                    //System.out.println("  dailyPointN: " + cardData2.getDailyPointN());
                    //System.out.println("  processing_rate: " + cardData2.getProcessing_rate());
                } else {
                    //System.out.println("  내용: " + cardData2Obj);
                }
            }
            
            if (data.containsKey("card-data-3")) {
                //System.out.println("card-data-3 상세:");
                Object cardData3Obj = data.get("card-data-3");
                //System.out.println("  타입: " + (cardData3Obj != null ? cardData3Obj.getClass().getName() : "null"));
                
                if (cardData3Obj instanceof com.wio.crm.model.DashboardData) {
                    com.wio.crm.model.DashboardData cardData3 = (com.wio.crm.model.DashboardData) cardData3Obj;
                    //System.out.println("  todayMiss: " + cardData3.getTodayMiss());
                    //System.out.println("  todayCom: " + cardData3.getTodayCom());
                    //System.out.println("  todayEme: " + cardData3.getTodayEme());
                    //System.out.println("  yesterdayMiss: " + cardData3.getYesterdayMiss());
                    //System.out.println("  yesterdayCom: " + cardData3.getYesterdayCom());
                    //System.out.println("  yesterdayEme: " + cardData3.getYesterdayEme());
                } else {
                    //System.out.println("  내용: " + cardData3Obj);
                }
            }
            
            // 추가 데이터도 확인
            for (String key : data.keySet()) {
                if (key.contains("yesterday") || key.contains("weekly") || key.contains("monthly")) {
                    //System.out.println(key + " 상세:");
                    Object timeDataObj = data.get(key);
                    //System.out.println("  타입: " + (timeDataObj != null ? timeDataObj.getClass().getName() : "null"));
                    //System.out.println("  내용: " + timeDataObj);
                }
            }
        } else {
            //System.out.println("getDashboardData 반환 데이터가 null입니다.");
        }
        //System.out.println("======== 대시보드 데이터 전송 완료 ========");
        
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-callCount-data")
    public ResponseEntity<Map<String, Object>> getDashboardConCount(Principal principal) {
        String username = principal.getName();
        
        // 로깅 추가
        //System.out.println("======== 콜 카운트 데이터 요청 시작 ========");
        //System.out.println("사용자: " + username);
        
        Map<String, Object> data = dashboardService.getDashboardCallCount(username);
        
        // 응답 데이터 로깅
        //System.out.println("콜 카운트 응답 데이터:");
        //System.out.println("데이터 키: " + data.keySet());
        
        if (data.containsKey("dashCallCount-data")) {
            Object callCountData = data.get("dashCallCount-data");
            //System.out.println("dashCallCount-data 타입: " + (callCountData != null ? callCountData.getClass().getName() : "null"));
            
            if (callCountData instanceof List) {
                List<?> callCountList = (List<?>) callCountData;
                //System.out.println("dashCallCount-data 항목 수: " + callCountList.size());
                
                if (!callCountList.isEmpty()) {
                    Object firstItem = callCountList.get(0);
                    //System.out.println("첫 번째 항목 타입: " + (firstItem != null ? firstItem.getClass().getName() : "null"));
                    //System.out.println("첫 번째 항목 toString: " + firstItem);
                    
                    if (firstItem instanceof com.wio.crm.model.DashboardData) {
                        com.wio.crm.model.DashboardData dashboardData = (com.wio.crm.model.DashboardData) firstItem;
                        //System.out.println("gubn: " + dashboardData.getGubn());
                        //System.out.println("hour_09: " + dashboardData.getHour_09());
                        //System.out.println("hour_10: " + dashboardData.getHour_10());
                        //System.out.println("callSum: " + dashboardData.getCallSum());
                    }
                }
            }
        }
        
        //System.out.println("======== 콜 카운트 데이터 응답 완료 ========");
        
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/dashboard-personCount-data")
    public ResponseEntity<Map<String, Object>> getDashboardPersonCount(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDashboardPersonCount(username);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/dashboard-month-data")
    public ResponseEntity<Map<String, Object>> getDashboardMonth(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDashboardMonth(username);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/empl/dashboard-employee")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public  ResponseEntity<Map<String, Object>> getEmployeeList() {


        Map<String, Object> data = dashboardService.getEmployeeList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/empl/dashboard-custom")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public  ResponseEntity<Map<String, Object>> getCustomList() {


        Map<String, Object> data = dashboardService.getCustomList();
        return ResponseEntity.ok(data);
    }


    @GetMapping("/api/dashboard-daily-data")
    public ResponseEntity<Map<String, Object>> getDailyAve(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDailyAve(username);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-weekly-data")
    public ResponseEntity<Map<String, Object>> getWeeklySum(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getWeeklySum(username);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-monthly-data")
    public ResponseEntity<Map<String, Object>> getMonthlySum(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getMonthlySum(username);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-point-data")
    public ResponseEntity<Map<String, Object>> getDashBoardPoint(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDashBoardPoint(username);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/api/dashboard-count-data")
    public ResponseEntity<Map<String, Object>> getDashBoardCount(Principal principal) {

        String username = principal.getName();
        Map<String, Object> data = dashboardService.getDashBoardCount(username);
        return ResponseEntity.ok(data);
    }
}