package com.supcon.tptrecommend.manager.strategy;

import com.supcon.tptrecommend.convert.filedata.DynamicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MapperFactory {


    private final List<DynamicMapper<?, ?>> mappers;

    private final Map<Integer, DynamicMapper<?, ?>> mapperCache = new ConcurrentHashMap<>();

    /**
     * 注入所有mapper的实例
     * @author luhao
     * @since 2025/07/16 09:26:43
     */
    @PostConstruct
    public void init() {
        if (mappers != null) {
            for (DynamicMapper<?, ?> mapper : mappers) {
                mapperCache.put(mapper.getIdentifier(), mapper);
            }
        }
    }

    /**
     * 根据源类型和目标类型动态获取 Mapper
     *
     * @param identifier 标识符
     * @return {@link Optional }<{@link DynamicMapper }<{@link S }, {@link T }>>
     * @author luhao
     * @since 2025/07/16 09:33:18
     */
    @SuppressWarnings("unchecked")
    public <S, T> DynamicMapper<S, T> getMapper(Integer identifier) {
        return (DynamicMapper<S, T>) mapperCache.get(identifier);
    }
}