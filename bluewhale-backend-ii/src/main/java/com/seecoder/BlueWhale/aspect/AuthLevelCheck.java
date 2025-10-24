package com.seecoder.BlueWhale.aspect;

import com.seecoder.BlueWhale.enums.RoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注需要权限检查的方法，仅当用户为对应的角色时会继续执行，否则抛出BlueWhaleException.authLevelMismatch
 * <br />
 * 例子: @AuthLevelCheck(level = RoleEnum.MANAGER)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthLevelCheck {
    RoleEnum role() default RoleEnum.CUSTOMER;
    RoleEnum[] roleGroup() default {};
}
