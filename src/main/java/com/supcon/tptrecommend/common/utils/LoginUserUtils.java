package com.supcon.tptrecommend.common.utils;

import com.supcon.systemcommon.exception.SupException;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.systemmanagerapi.utils.LoginInfoUtil;

public class LoginUserUtils {

    /**
     * 获取登录用户信息
     *
     * @return {@link LoginInfoUserDTO }
     * @author luhao
     * @date 2025/05/22 16:29:27
     */
    public static LoginInfoUserDTO getLoginUserInfo() {
        LoginInfoUserDTO user = new LoginInfoUserDTO();
        try {
            return LoginInfoUtil.getCurrentUser();
        } catch (SupException e) {
            user.setUsername("defaultUser");
            return user;
        }
    }
}
