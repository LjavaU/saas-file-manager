package com.example.saasfile.support.web;

import lombok.Data;

@Data
public class SupResult<T> {

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;

    private int code;
    private String msg;
    private T data;

    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }

    public static <T> SupResult<T> success() {
        SupResult<T> result = new SupResult<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("success");
        return result;
    }

    public static <T> SupResult<T> success(T data) {
        SupResult<T> result = success();
        result.setData(data);
        return result;
    }

    public static <T> SupResult<T> error(String msg) {
        SupResult<T> result = new SupResult<>();
        result.setCode(ERROR_CODE);
        result.setMsg(msg);
        return result;
    }
}
