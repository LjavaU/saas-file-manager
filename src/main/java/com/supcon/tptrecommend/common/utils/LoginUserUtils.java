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
        try {
            return LoginInfoUtil.getCurrentUser();
        } catch (SupException e) {
            // 用于本地调试用
            LoginInfoUserDTO user = new LoginInfoUserDTO();
            user.setId(88888L);
            user.setUsername("defaultUser");
            return user;
        }
    }
}
