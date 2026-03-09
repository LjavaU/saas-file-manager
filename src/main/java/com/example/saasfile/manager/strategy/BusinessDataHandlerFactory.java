package com.example.saasfile.manager.strategy;

import com.example.saasfile.common.enums.SubCategoryEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 涓氬姟鏁版嵁澶勭悊宸ュ巶
 *
 * @author luhao
 * @since 2025/06/24 16:59:33
 */
@Slf4j
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
            log.error("涓嶆敮鎸佺殑涓氬姟绫诲瀷: {}-{}", SubCategoryEnum.fromCode(businessKey), businessKey);
            return Optional.empty();
            //throw new IllegalArgumentException("涓嶆敮鎸佺殑涓氬姟绫诲瀷: " + SubCategoryEnum.fromCode(businessKey) + "-" + businessKey);
        }
        return Optional.of(handler);
    }
}