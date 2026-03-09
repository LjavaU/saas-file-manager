package com.example.saasfile.common.utils;

import com.example.saasfile.support.exception.SupException;
import com.example.saasfile.support.user.LoginInfoUserDTO;
import com.example.saasfile.support.user.LoginInfoUtil;

public class LoginUserUtils {

    
    public static LoginInfoUserDTO getLoginUserInfo() {
        try {
            return LoginInfoUtil.getCurrentUser();
        } catch (SupException e) {
            LoginInfoUserDTO user = new LoginInfoUserDTO();
            user.setId(88888L);
            user.setUsername("defaultUser");
            return user;
        }
    }
}
