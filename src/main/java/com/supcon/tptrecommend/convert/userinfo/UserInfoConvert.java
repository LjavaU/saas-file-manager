package com.supcon.tptrecommend.convert.userinfo;

import com.supcon.tptrecommend.dto.userinfo.UserInfoCreateReq;
import com.supcon.tptrecommend.dto.userinfo.UserInfoResp;
import com.supcon.tptrecommend.dto.userinfo.UserInfoUpdateReq;
import com.supcon.tptrecommend.entity.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p>
 * 用户信息表转换器
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Mapper
public interface UserInfoConvert {

    UserInfoConvert INSTANCE = Mappers.getMapper(UserInfoConvert.class);

    UserInfo convert(UserInfoCreateReq userInfoCreateReq);

    UserInfo convert(UserInfoUpdateReq userInfoUpdateReq);

    UserInfoResp convert(UserInfo userInfo);

    List<UserInfo> convertList(List<UserInfoCreateReq> userInfoCreateReq);
}