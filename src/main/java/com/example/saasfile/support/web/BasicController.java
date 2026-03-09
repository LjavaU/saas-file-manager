package com.example.saasfile.support.web;

public class BasicController {

    protected <T> SupResult<T> data(T data) {
        return SupResult.success(data);
    }
}
