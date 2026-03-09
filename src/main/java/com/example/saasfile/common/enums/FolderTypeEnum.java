package com.example.saasfile.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FolderTypeEnum {
    PERSONAL("private", "Personal"),
    TENANT("tenant", "Shared");

    private final String code;
    private final String description;
}
