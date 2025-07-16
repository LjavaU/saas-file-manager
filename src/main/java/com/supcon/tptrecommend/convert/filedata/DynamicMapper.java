package com.supcon.tptrecommend.convert.filedata;

/**
 * 通用 Mapper 接口，所有动态选择的 Mapper 都应实现此接口
 *
 * @param <S> Source a.k.a. 源类型
 * @param <T> Target a.k.a. 目标类型
 */
public interface DynamicMapper<S, T> {

    /**
     * 将源对象映射到目标对象
     *
     * @param source 源对象
     * @return 目标对象
     * @author luhao
     * @since 2025/07/15 17:22:22
     */
    T map(S source);


    /**
     * 用于在工厂中唯一标识
     * 这里用SubCategoryEnum的code作为标识
     * @return {@link Integer } 返回SubCategoryEnum中的code
     * @author luhao
     * @since 2025/07/15 17:22:00
     */
    default Integer getIdentifier() {
        return null;
    }
}