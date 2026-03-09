package com.example.saasfile.support.web;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IDList<T> {

    private List<T> ids = new ArrayList<>();
}
