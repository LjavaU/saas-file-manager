package com.supcon.tptrecommend.manager.impl;

import com.alibaba.excel.EasyExcel;
import com.supcon.tptrecommend.common.ExcelDataListener;
import com.supcon.tptrecommend.common.utils.FileEncodingDetector;
import com.supcon.tptrecommend.common.utils.MarkdownConverter;
import com.supcon.tptrecommend.manager.FileParseManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileParseManageImpl implements FileParseManager {


    /**
     * 将文件解析为 Markdown
     *
     * @param file       文件
     * @param onlyHeader Only 标头
     * @return {@link String }
     * @throws Exception 异常
     * @author luhao
     * @since 2025/06/18 20:08:25
     */
    @Override
    public String parseFileToMarkdown(File file, Boolean onlyHeader) throws Exception {
        // 获取文件编码
        Charset charset = FileEncodingDetector.detectCharset(file);
        return doParseAndConvert(file, charset, onlyHeader);
    }

    /**
     * 读取 CSV
     *
     * @param file       文件
     * @param charset    字符集
     * @param onlyHeader 是否只含表头
     * @return {@link List }<{@link List }<{@link String }>>
     * @throws IOException io异常
     * @author luhao
     * @since 2025/06/18 20:11:17
     */
    private List<List<String>> readCsv(File file, Charset charset, Boolean onlyHeader) throws IOException {
        try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), charset);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .setTrim(true)
                 .build())) { // 去除值两边的空格

            // 获取头部标题
            List<String> header = csvParser.getHeaderNames();
            if (onlyHeader) {
                List<List<String>> records = new ArrayList<>();
                records.add(header);
                return records;
            }

            // TODO:  限制记录数 10条；且如果全部读取可能会有内存溢出
            List<List<String>> records = csvParser.stream()
                .map(CSVRecord::toList).limit(10)
                .collect(Collectors.toList());

            // 将标题添加为第一行
            records.add(0, header);
            return records;
        }
    }

    private String doParseAndConvert(File file, Charset charset, Boolean onlyHeader) throws Exception {
        List<List<String>> data;
        String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(".csv")) {
            data = readCsv(file, charset, onlyHeader);
        } else if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
            data = readExcel(file, onlyHeader);
        } else {
            return null;
        }
        return MarkdownConverter.generateMarkdownTable(data);
    }


    /**
     * 读 Excel
     *
     * @param file       文件
     * @param onlyHeader 是否只含表头
     * @return {@link List }<{@link List }<{@link String }>>
     * @author luhao
     * @since 2025/06/18 20:10:54
     */
    private List<List<String>> readExcel(File file, Boolean onlyHeader) {
        ExcelDataListener listener = new ExcelDataListener();
        EasyExcel.read(file, listener).sheet().doRead();
        if (onlyHeader) {
            return Collections.singletonList(listener.getHeader());
        }
        return listener.getData();
    }

}

