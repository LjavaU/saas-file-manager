package com.example.saasfile.support.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JsonMessageDO<T> {

    private String type;
    private T data;

    public static <T> JsonMessageDO<T> data(String type, T data) {
        return new JsonMessageDO<>(type, data);
    }
}
