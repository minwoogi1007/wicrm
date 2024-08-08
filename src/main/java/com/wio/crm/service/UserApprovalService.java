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

@Service
public class UserApprovalService {

    @Autowired
    private UserApprovalMapper userApprovalMapper;

    @Autowired
    private Tcnt01EmpMapper tcnt01EmpMapper;

    public List<UserApproval> getPendingApprovals() {
        return userApprovalMapper.findByConfirmYn();
    }

    public UserApproval getUserApprovalById(String  id) {
        return userApprovalMapper.findById(id);
    }

    @Transactional
    public void approveUser(String  id, String companyCode) {
        UserApproval userApproval = getUserApprovalById(id);
        userApproval.setConfirmYn("Y");
        userApproval.setConfirmTime(LocalDateTime.now());
        userApprovalMapper.updateUserApproval(userApproval);

        Tcnt01Emp tcnt01Emp = new Tcnt01Emp();
        tcnt01Emp.setUserId(userApproval.getUserid());
        tcnt01Emp.setEmp_name(userApproval.getUsername());
        tcnt01Emp.setCustCode(companyCode);
        // Set other fields as necessary
        tcnt01EmpMapper.insertTcnt01Emp(tcnt01Emp);
    }
}