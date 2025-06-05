package com.supcon.tptrecommend.common.utils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.jetbrains.annotations.Nullable;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FileConvertUtil {

    public String convertToMarkdown(MultipartFile file,List<List<String>> headers) {
        if (file.isEmpty()) {
            return null;
        }
        byte[] fileBytes;
        String originalFilename = file.getOriginalFilename();
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("无法读取上传的文件{}", originalFilename, e);
            return null;
        }
        return doConvert(fileBytes, originalFilename,headers);
    }

    @Nullable
    private String doConvert(byte[] fileBytes, String originalFilename,List<List<String>> headers) {
        // 获取文件的编码
        String charset = detectCharset(fileBytes);
        List<List<String>> data = null;
        if (originalFilename != null && (originalFilename.endsWith(".csv"))) {
            try {
                data = parseCsv(new ByteArrayInputStream(fileBytes), Charset.forName(charset),headers);
            } catch (Throwable e) {
                log.error("{}解析失败: ", originalFilename, e);
            }
        } else if (originalFilename != null && (originalFilename.endsWith(".xls") || originalFilename.endsWith(".xlsx"))) {
            try {
                data = parseExcel(new ByteArrayInputStream(fileBytes));
            } catch (Throwable e) {
                log.error("{}解析失败: ", originalFilename, e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported file type. Please upload CSV, XLS, or XLSX.");
        }

        if (CollectionUtil.isEmpty(data)) {
            return null;
        }

        return generateMarkdownTable(data);
    }


    public String convertToMarkdown(InputStream inputStream, String originalFilename,List<List<String>> headers) {

        byte[] fileBytes;
        try {
            fileBytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("无法读取上传的文件:{}", originalFilename, e);
            return null;
        }
        return doConvert(fileBytes, originalFilename,headers);

    }

    /**
     * 获取文件编码
     *
     * @param bytes 字节
     * @return {@link String }
     * @author luhao
     * @date 2025/06/03 17:00:34
     */
    public static String detectCharset(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        return detector.getDetectedCharset(); // 如 UTF-8、GBK、windows-1252
    }

    // Updated parseCsv to accept Charset
    private List<List<String>> parseCsv(InputStream inputStream, Charset charset,List<List<String>> headers) throws Throwable {
        List<List<String>> data = new ArrayList<>();
        // Configure CSVFormat to correctly handle headers
        // If you want the header to be the first row in your 'data' list:
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader() // Treat the first line as header
            .setSkipHeaderRecord(true) // Do not skip the header from records
            .build();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            boolean headerAdded = false;
            List<String> headerNames = null;

            if (csvFormat.getSkipHeaderRecord()) { // If header is skipped by parser, add it manually first
                headerNames = csvParser.getHeaderNames();
                if (!headerNames.isEmpty()) {
                    data.add(new ArrayList<>(headerNames));
                    headerAdded = true;
                    headers.add(headerNames);
                }

            }


            for (CSVRecord csvRecord : csvParser) {
                // 只取10行
                if (csvRecord.getRecordNumber() > 10) {
                    break;
                }
                if (!headerAdded && csvRecord.getRecordNumber() == 1 && csvFormat.getHeader() != null && csvFormat.getHeader().length > 0) {
                    // This is the header row because setSkipHeaderRecord(false)
                    List<String> headerFromFile = new ArrayList<>();
                    for (String headerVal : csvRecord) {
                        headerFromFile.add(headerVal != null ? headerVal : "");
                    }
                    data.add(headerFromFile);
                    headerNames = headerFromFile; // Store for consistent column count
                    headerAdded = true;
                    continue; // Move to next record
                }

                List<String> row = new ArrayList<>();
                // Ensure all columns are added, even if some are empty at the end of a row
                int colCount = (headerNames != null && !headerNames.isEmpty()) ? headerNames.size() : csvRecord.size();

                for (int i = 0; i < colCount; i++) {
                    if (i < csvRecord.size()) {
                        row.add(csvRecord.get(i) != null ? csvRecord.get(i) : "");
                    } else {
                        row.add(""); // Add empty string for missing columns to match header
                    }
                }
                data.add(row);
            }
        }
        return data;
    }

    private List<List<String>> parseExcel(InputStream inputStream) throws Throwable {
        List<List<String>> data = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            // Using default Locale for DataFormatter, usually fine for Chinese characters.
            // If specific formatting of numbers/dates (not char encoding) is an issue, you might specify:
            // DataFormatter dataFormatter = new DataFormatter(java.util.Locale.SIMPLIFIED_CHINESE);
            DataFormatter dataFormatter = new DataFormatter();

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                int lastColumn = row.getLastCellNum();
                for (int cn = 0; cn < lastColumn; cn++) {
                    Cell cell = row.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell == null) {
                        rowData.add("");
                    } else {
                        rowData.add(dataFormatter.formatCellValue(cell));
                    }
                }
                // Only add non-empty rows or rows that are not entirely blank
                // This logic can be adjusted based on whether you want to keep completely blank rows
                if (!rowData.stream().allMatch(String::isEmpty)) {
                    data.add(rowData);
                }
            }
        }
        return data;
    }

    public String generateMarkdownTable(List<List<String>> data) {
        StringBuilder markdown = new StringBuilder();
        List<String> header = data.get(0);
        // Determine the maximum number of columns from all rows, in case some rows are shorter/longer
        // Or, more simply, assume the header dictates the number of columns.
        int numColumns = header.size();
        if (numColumns == 0 && data.size() > 1) { // Edge case: header is empty but data exists (unlikely with proper CSV/Excel)
            numColumns = data.stream().mapToInt(List::size).max().orElse(0);
        }
        if (numColumns == 0) return ""; // No columns to create a table

        // Header row
        markdown.append("| ");
        for (int i = 0; i < numColumns; i++) {
            String h = (i < header.size()) ? header.get(i) : "";
            markdown.append(sanitizeMarkdown(h)).append(" | ");
        }
        markdown.setLength(markdown.length() - 1);
        markdown.append("\n");

        // Separator row
        markdown.append("|");
        for (int i = 0; i < numColumns; i++) {
            markdown.append(" --- |");
        }
        markdown.append("\n");

        // Data rows (starting from index 1)
        for (int i = 1; i < data.size(); i++) {
            List<String> row = data.get(i);
            markdown.append("| ");
            for (int j = 0; j < numColumns; j++) {
                String cellValue = (j < row.size()) ? sanitizeMarkdown(row.get(j)) : "";
                markdown.append(cellValue).append(" | ");
            }
            markdown.setLength(markdown.length() - 1);
            markdown.append("\n");
        }

        return markdown.toString();
    }

    private String sanitizeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("|", "\\|")
            .replace("\r\n", " ") // Replace newlines in cells with space
            .replace("\n", " ")   // Replace newlines in cells with space
            .replace("\r", " ");  // Replace newlines in cells with space
    }
}
