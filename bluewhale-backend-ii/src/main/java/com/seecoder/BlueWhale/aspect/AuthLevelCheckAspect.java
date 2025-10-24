package com.seecoder.BlueWhale.aspect;

import com.seecoder.BlueWhale.enums.RoleEnum;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.util.SecurityUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthLevelCheckAspect {

    @Autowired
    SecurityUtil securityUtil;

    @Before("@annotation(authLevelCheck)")
    public void check(JoinPoint joinPoint, AuthLevelCheck authLevelCheck) throws Throwable {
        RoleEnum[] roleGroup;
        if (authLevelCheck.roleGroup().length == 0) {
            roleGroup = new RoleEnum[]{authLevelCheck.role()};
        } else {
            roleGroup = authLevelCheck.roleGroup();
        }
        RoleEnum userRole = securityUtil.getCurrentUser().getRole();
        for (RoleEnum role : roleGroup) {
            if (role == userRole) {
                return;
            }
        }
        throw BlueWhaleException.authLevelMismatch();
    }
}
