package com.example.saasfile.support.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.saasfile.support.web.SupRequestBody;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

public class BasicServiceImpl<M extends IBaseMapper<T>, T> extends ServiceImpl<M, T> implements IBasicService<T> {

    @Override
    public IPage<T> pageAutoQuery(QueryWrapper<T> queryWrapper, SupRequestBody<Map<String, String>> body) {
        Map<String, String> params = body == null ? null : body.getData();
        long current = parseLong(params, "current", parseLong(params, "pageNum", 1L));
        long size = parseLong(params, "size", parseLong(params, "pageSize", 10L));
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!StringUtils.hasText(value) || isPageKey(key) || "sortField".equals(key) || "sortOrder".equals(key)) {
                    continue;
                }
                queryWrapper.eq(toSnakeCase(key), value);
            }
            String sortField = params.get("sortField");
            String sortOrder = params.get("sortOrder");
            if (StringUtils.hasText(sortField)) {
                boolean asc = !"descend".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder);
                queryWrapper.orderBy(true, asc, toSnakeCase(sortField));
            }
        }
        return page(new Page<>(current, size), queryWrapper);
    }

    private boolean isPageKey(String key) {
        return "current".equals(key) || "pageNum".equals(key) || "size".equals(key) || "pageSize".equals(key);
    }

    private long parseLong(Map<String, String> params, String key, long defaultValue) {
        if (params == null || !StringUtils.hasText(params.get(key))) {
            return defaultValue;
        }
        try {
            return Long.parseLong(params.get(key));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String toSnakeCase(String key) {
        StringBuilder builder = new StringBuilder();
        for (char ch : key.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                builder.append('_').append(Character.toLowerCase(ch));
            } else {
                builder.append(ch);
            }
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }
}
