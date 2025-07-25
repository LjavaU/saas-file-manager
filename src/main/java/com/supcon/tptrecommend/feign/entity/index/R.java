package com.supcon.tptrecommend.feign.entity.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 封装响应结果
 *
 * @param <T>
 * @author ricky
 * @date 2019-06-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private boolean success;
    private T data;
    private String msg;



}