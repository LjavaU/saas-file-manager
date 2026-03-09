package com.example.saasfile.convert.filedata;

/**
 * 閫氱敤 Mapper 鎺ュ彛锛屾墍鏈夊姩鎬侀€夋嫨鐨?Mapper 閮藉簲瀹炵幇姝ゆ帴鍙?
 *
 * @param <S> Source a.k.a. 婧愮被鍨?
 * @param <T> Target a.k.a. 鐩爣绫诲瀷
 */
public interface DynamicMapper<S, T> {

    /**
     * 灏嗘簮瀵硅薄鏄犲皠鍒扮洰鏍囧璞?
     *
     * @param source 婧愬璞?
     * @return 鐩爣瀵硅薄
     * @author luhao
     * @since 2025/07/15 17:22:22
     */
    T map(S source);


    /**
     * 鐢ㄤ簬鍦ㄥ伐鍘備腑鍞竴鏍囪瘑
     * 杩欓噷鐢⊿ubCategoryEnum鐨刢ode浣滀负鏍囪瘑
     * @return {@link Integer } 杩斿洖SubCategoryEnum涓殑code
     * @author luhao
     * @since 2025/07/15 17:22:00
     */
    default Integer getIdentifier() {
        return null;
    }
}