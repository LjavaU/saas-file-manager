package com.example.saasfile.support.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupRequestBody<T> {

    private T data;

    public static <T> SupRequestBody<T> data(T data) {
        return new SupRequestBody<>(data);
    }
}
