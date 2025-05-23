package com.supcon.tptrecommend.dto.userinfo;

import com.supcon.tptrecommend.entity.UserInfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "用户信息表-数据模型", description = "用户信息表")
public class UserInfoDTO extends UserInfo {

    private static final long serialVersionUID = 1L;


    /**
     * <p>Title:获取entity对象</p>
     * <p>Description:</p>
     * @author luhao
     * @date 2025-05-22
     * @return entity对象
     */
    public UserInfo toUserInfo() {
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(this, userInfo);
        return userInfo;
    }
}