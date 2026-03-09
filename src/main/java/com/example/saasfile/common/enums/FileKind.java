package com.example.saasfile.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum FileKind {
    FILE("file"),
    FOLDER("folder");

    private final String value;

}