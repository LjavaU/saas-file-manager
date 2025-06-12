package com.supcon.tptrecommend.dto.fileobject;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class Folder {

    private Long id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return Objects.equals(name, folder.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
