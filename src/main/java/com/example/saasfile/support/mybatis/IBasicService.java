package com.example.saasfile.support.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.saasfile.support.web.SupRequestBody;

import java.util.Map;

public interface IBasicService<T> extends IService<T> {

    IPage<T> pageAutoQuery(QueryWrapper<T> queryWrapper, SupRequestBody<Map<String, String>> body);
}
