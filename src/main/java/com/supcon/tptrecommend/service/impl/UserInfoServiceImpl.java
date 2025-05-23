package com.supcon.tptrecommend.service.impl;

import com.supcon.system.base.entity.basic.impl.BasicServiceImpl;
import com.supcon.tptrecommend.convert.userinfo.UserInfoConvert;
import com.supcon.tptrecommend.dto.userinfo.UserInfoCreateReq;
import com.supcon.tptrecommend.dto.userinfo.UserInfoResp;
import com.supcon.tptrecommend.dto.userinfo.UserInfoUpdateReq;
import com.supcon.tptrecommend.entity.UserInfo;
import com.supcon.tptrecommend.mapper.UserInfoMapper;
import com.supcon.tptrecommend.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl extends BasicServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    private final UserInfoMapper userInfoMapper;

    @Override
    public UserInfoResp getObj(Long id) {
        UserInfo userInfo = userInfoMapper.selectById(id);
        return UserInfoConvert.INSTANCE.convert(userInfo);
    }

    @Override
    public Long saveObj(UserInfoCreateReq userInfoCreateReq) {
        UserInfo userInfo = UserInfoConvert.INSTANCE.convert(userInfoCreateReq);
        userInfoMapper.insert(userInfo);
        return userInfo.getId();
    }

    @Override
    public boolean updateObj(UserInfoUpdateReq userInfoUpdateReq) {
        UserInfo userInfo = UserInfoConvert.INSTANCE.convert(userInfoUpdateReq);
        return userInfoMapper.updateById(userInfo) > 0;
    }

    @Override
    public boolean removeObjs(List<Long> ids) {
        return userInfoMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * sass平台同步用户信息
     *
     * @param data 数据
     * @author luhao
     * @date 2025/05/22 11:30:28
     */
    @Override
    public void sync(List<UserInfoCreateReq> data) {
        List<UserInfo> userInfoList = UserInfoConvert.INSTANCE.convertList(data);
        saveBatch(userInfoList);
    }
}
