package com.example.saasfile.feign.entity.knowledge;

import lombok.Data;

import java.util.List;

@Data
public class FileDataSimple {
    
    private String status;
    
    private String file_name;
    
    private List<String> key_words;



}