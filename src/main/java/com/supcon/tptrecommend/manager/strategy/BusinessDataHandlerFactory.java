package com.supcon.tptrecommend.manager.strategy;

import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务数据处理工厂
 *
 * @author luhao
 * @since 2025/06/24 16:59:33
 */
@Component
@RequiredArgsConstructor
public class BusinessDataHandlerFactory {

    private final List<BusinessDataHandler> handlers;

    private static Map<Integer, BusinessDataHandler> handlerMap;

    @PostConstruct
    public void init() {
        handlerMap = handlers.stream()
            .collect(Collectors.toMap(BusinessDataHandler::getBusinessKey, Function.identity()));
    }

    public Optional<BusinessDataHandler> getHandler(Integer businessKey) {
        if (Objects.isNull(businessKey)) {
            return Optional.empty();
        }
        BusinessDataHandler handler = handlerMap.get(businessKey);
        if (handler == null) {
            throw new IllegalArgumentException("不支持的业务类型: " + SubCategoryEnum.fromCode(businessKey) + "-" + businessKey);
        }
        return Optional.of(handler);
    }
}