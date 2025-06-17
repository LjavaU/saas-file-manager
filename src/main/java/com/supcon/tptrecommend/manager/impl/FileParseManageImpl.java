package com.supcon.tptrecommend.manager.impl;

import com.alibaba.excel.EasyExcel;
import com.supcon.tptrecommend.common.ExcelDataListener;
import com.supcon.tptrecommend.common.utils.FileEncodingDetector;
import com.supcon.tptrecommend.common.utils.MarkdownConverter;
import com.supcon.tptrecommend.manager.FileParseManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class FileParseManageImpl implements FileParseManager {
    /**
     * 将文件解析为 Markdown
     *
     * @param file 文件
     * @return {@link String } 返回Markdown字符串
     * @throws Exception 异常
     * @author luhao
     * @since 2025/06/09 16:07:05
     */
    @Override
    public String parseFileToMarkdown(MultipartFile file, Boolean onlyHeader) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.error("原始文件为空");
            return null;
        }

        log.info("开始解析文件: {}", originalFilename);
        Charset charset = FileEncodingDetector.detectCharset(file);
        return doParseAndConvert(file.getInputStream(), originalFilename, charset, onlyHeader);
    }

    private String doParseAndConvert(InputStream inputStream, String originalFilename, Charset charset, Boolean onlyHeader) throws Exception {
        List<List<String>> data;
        try (InputStream in = inputStream) {
            if (originalFilename.toLowerCase().endsWith(".csv")) {
                data = parseCsv(in, charset, onlyHeader);
            } else if (originalFilename.toLowerCase().endsWith(".xlsx") || originalFilename.toLowerCase().endsWith(".xls")) {
                data = parseExcel(in, onlyHeader);
            } else {
                return null;
            }
        }
        return MarkdownConverter.generateMarkdownTable(data);
    }

    /**
     * 将字节流解析为 Markdown
     *
     * @param bytes            字节
     * @param originalFilename 原始文件名
     * @param onlyHeader       Only 标头
     * @return {@link String } 返回Markdown字符串
     * @throws Exception 异常
     * @author luhao
     * @since 2025/06/09 16:24:16
     */
    @Override
    public String parseBytesToMarkdown(byte[] bytes, String originalFilename, Boolean onlyHeader) throws Exception {
        //  获取文件编码
        Charset charset = FileEncodingDetector.detectCharset(bytes);
        return doParseAndConvert(new ByteArrayInputStream(bytes), originalFilename, charset, onlyHeader);
    }


    /**
     * Parses a CSV file using Apache Commons CSV in a streaming fashion.
     */
    private List<List<String>> parseCsv(InputStream inputStream, Charset charset, Boolean onlyHeader) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                 .setHeader() // Treat the first line as header
                 .setSkipHeaderRecord(true) // Do not skip the header from records
                 .build())) {

            // 获取头部标题
            List<String> header = csvParser.getHeaderNames();
            if (onlyHeader) {
                List<List<String>> records = new ArrayList<>();
                records.add(header);
                return records;
            }
            // 获取记录并将其转换为 List<List<String>>
            List<List<String>> records = StreamSupport.stream(csvParser.spliterator(), false)
                .map(csvRecord -> StreamSupport.stream(csvRecord.spliterator(), false)
                    .collect(Collectors.toList()))
                .collect(Collectors.toList());

            // TODO:  限制记录数 10条
            records = records.stream().limit(10).collect(Collectors.toList());
            // 将标题添加为第一行
            records.add(0, header);
            return records;
        }
    }

    /**
     * Parses an Excel file (.xls or .xlsx) using EasyExcel's listener model for high performance.
     */
    private List<List<String>> parseExcel(InputStream inputStream, Boolean onlyHeader) {
        ExcelDataListener listener = new ExcelDataListener();
        // Use .sheet() to read the first sheet. .doRead() will trigger the parsing.
        // We use .read(Map.class) to handle arbitrary columns without a predefined POJO.
        EasyExcel.read(inputStream, listener).sheet().doRead();
        if (onlyHeader) {
            return Collections.singletonList(listener.getHeader());
        }
        // TODO: 限制记录数 10条
        return listener.getData().stream().limit(10).collect(Collectors.toList());
    }
}

