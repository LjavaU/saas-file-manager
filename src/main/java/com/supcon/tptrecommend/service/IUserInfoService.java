package com.supcon.tptrecommend.service;

import com.supcon.system.base.entity.basic.IBasicService;
import com.supcon.tptrecommend.dto.userinfo.UserInfoCreateReq;
import com.supcon.tptrecommend.dto.userinfo.UserInfoResp;
import com.supcon.tptrecommend.dto.userinfo.UserInfoUpdateReq;
import com.supcon.tptrecommend.entity.UserInfo;

import java.util.List;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
public interface IUserInfoService extends IBasicService<UserInfo> {

    /**
     * 获取
     *
     * @param id 对象ID
     * @return 对象响应实体
     * @author luhao
     * @date 2025-05-22
     */
    UserInfoResp getObj(Long id);

    /**
     * 保存
     *
     * @param userInfoCreateReq 对象创建实体
     * @return 对象ID
     * @author luhao
     * @date 2025-05-22
     */
    Long saveObj(UserInfoCreateReq userInfoCreateReq);

    /**
     * 更新
     *
     * @param userInfoUpdateReq 对象更新实体
     * @return 更新是否成功
     * @author luhao
     * @date 2025-05-22
     */
    boolean updateObj(UserInfoUpdateReq userInfoUpdateReq);


    /**
     * 批量删除
     *
     * @param ids 对象ID集合
     * @return 批量删除时是否成功
     * @author luhao
     * @date 2025-05-22
     */
    boolean removeObjs(List<Long> ids);

    /**
     * sass平台同步用户信息
     *
     * @param data 数据
     * @author luhao
     * @date 2025/05/22 11:30:06
     */
    void sync(List<UserInfoCreateReq> data);
}
