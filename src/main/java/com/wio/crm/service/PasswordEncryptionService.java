package com.wio.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.wio.crm.mapper.TipdwMapper;
import com.wio.crm.model.Tipdw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class PasswordEncryptionService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryptionService.class);

    @Autowired
    private TipdwMapper tipdwMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void encryptExistingPasswords() {
        logger.info("encryptExistingPasswords start ");
        try {
            List<Tipdw> users = tipdwMapper.findAll();
            logger.info("encryptExistingPasswords start users");

            for (Tipdw user : users) {
                logger.info("encryptExistingPasswords start Tipdw user : users 1");
                String encryptedPassword = passwordEncoder.encode(user.getPw());
                logger.info("encryptExistingPasswords start Tipdw user : encryptedPassword");
                user.setPw(encryptedPassword);
                logger.info("encryptExistingPasswords start Tipdw user : user.setPw(encryptedPassword)");
                tipdwMapper.save(user);
                logger.info("encryptExistingPasswords start Tipdw user : save(user)");
            }
        } catch (Exception e) {
            logger.error("Error occurred during password encryption", e);
        }
    }

    public void encryptPassword(String userId) {
        logger.info("encryptPassword start, userId=" + userId);
        try {
            Tipdw user = tipdwMapper.findById(userId);
            if (user != null) {
                String encryptedPassword = passwordEncoder.encode(user.getPw());
                user.setPw(encryptedPassword);
                tipdwMapper.save(user);
                logger.info("encryptPassword end, userId=" + userId);
            } else {
                logger.warn("User not found with userId=" + userId);
            }
        } catch (Exception e) {
            logger.error("Error occurred during password encryption for userId=" + userId, e);
        }
    }
}