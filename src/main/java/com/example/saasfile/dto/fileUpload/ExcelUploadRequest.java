package com.example.saasfile.dto.fileUpload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
public class ExcelUploadRequest {

    @NotBlank(message = "fileName cannot be blank")
    private String fileName;

    @NotEmpty(message = "content cannot be empty")
    private Map<String, List<List<String>>> content;
}
