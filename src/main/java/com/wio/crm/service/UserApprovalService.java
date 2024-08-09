package com.wio.crm.service;

import com.wio.crm.mapper.UserApprovalMapper;
import com.wio.crm.mapper.Tcnt01EmpMapper;
import com.wio.crm.model.UserApproval;
import com.wio.crm.model.Tcnt01Emp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserApprovalService {

    @Autowired
    private UserApprovalMapper userApprovalMapper;

    @Autowired
    private Tcnt01EmpMapper tcnt01EmpMapper;

    public List<UserApproval> getAllApprovals() {
        return userApprovalMapper.findAllApprovals();
    }
    /**
     * 사용자 목록을 가져옵니다.
     */
    public List<UserApproval> getPendingApprovals(String status) {
        return userApprovalMapper.findByConfirmYn(status);
    }
    /**
     * 사용자 승인 취소.
     */
    public void cancelApproval(String userId) {
        // 여기에 승인 취소 로직 추가 (예: 상태를 'N'으로 변경)
        userApprovalMapper.updateApprovalStatus(userId);
        // N_TCNT01_EMP 테이블에서 해당 사용자 정보 삭제
        userApprovalMapper.deleteEmpTempData(userId);
    }
    /**
     * 특정 사용자의 승인 요청 상세 정보를 가져옵니다.
     */

    public UserApproval getUserApprovalById(String  id) {
        return userApprovalMapper.findById(id);
    }

    /**
     * 업체 목록을 가져옵니다.
     */
    public List<Map<String, String>> getCompanyList() {
        return userApprovalMapper.getCompanyList();
    }
    @Transactional
    public void approveUser(String  userId, String companyCode) {
        // Update N_TIPDW table: set CONFIRM_YN to 'Y' where USERID matches
        userApprovalMapper.updateConfirmYn(userId);

        // Retrieve data from N_TCNT01_EMP_TEMP for the given userId
        Map<String, Object> empTempData = userApprovalMapper.getEmpTempData(userId);

        // Generate a new EMPNO
        String newEmpNo = userApprovalMapper.generateNewEmpNo();

        // Prepare the data for insertion into N_TCNT01_EMP
        empTempData.put("companyCode", companyCode); // Add the companyCode to the data
        empTempData.put("EMPNO", newEmpNo); // Add the generated EMPNO
System.out.println("empTempData=========="+empTempData);
        // Insert the data into N_TCNT01_EMP
        userApprovalMapper.insertEmpData(empTempData);
    }

}