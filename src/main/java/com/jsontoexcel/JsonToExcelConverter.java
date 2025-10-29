package com.jsontoexcel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Converts multi-level JSON to Excel format.
 * Handles nested objects and arrays by flattening them into a tabular format.
 */
public class JsonToExcelConverter {
    
    private final ObjectMapper objectMapper;
    private final Workbook workbook;
    private Sheet currentSheet;
    private int currentRowNum;
    private Row headerRow;
    private Map<String, Integer> columnMap;
    
    public JsonToExcelConverter() {
        this.objectMapper = new ObjectMapper();
        this.workbook = new XSSFWorkbook();
        this.columnMap = new LinkedHashMap<>();
    }
    
    /**
     * Converts a JSON file to Excel
     * @param jsonFilePath Path to the input JSON file
     * @param excelFilePath Path to the output Excel file
     * @throws IOException If file operations fail
     */
    public void convertJsonToExcel(String jsonFilePath, String excelFilePath) throws IOException {
        JsonNode rootNode = objectMapper.readTree(new FileInputStream(jsonFilePath));
        convertJsonNodeToExcel(rootNode, excelFilePath);
    }
    
    /**
     * Converts a JSON string to Excel
     * @param jsonString The JSON string to convert
     * @param excelFilePath Path to the output Excel file
     * @throws IOException If file operations fail
     */
    public void convertJsonStringToExcel(String jsonString, String excelFilePath) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonString);
        convertJsonNodeToExcel(rootNode, excelFilePath);
    }
    
    private void convertJsonNodeToExcel(JsonNode rootNode, String excelFilePath) throws IOException {
        currentSheet = workbook.createSheet("Data");
        currentRowNum = 0;
        columnMap.clear();
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        if (rootNode.isArray()) {
            // Handle array of objects
            processJsonArray(rootNode, headerStyle, dataStyle);
        } else if (rootNode.isObject()) {
            // Handle single object or array with one row
            List<Map<String, Object>> flattenedData = new ArrayList<>();
            Map<String, Object> flattenedMap = new LinkedHashMap<>();
            flattenJsonObject(rootNode, "", flattenedMap);
            flattenedData.add(flattenedMap);
            processFlattenedData(flattenedData, headerStyle, dataStyle);
        } else {
            // Handle primitive value
            createHeaderRow(Collections.singletonList("Value"), headerStyle);
            Row dataRow = currentSheet.createRow(currentRowNum++);
            Cell cell = dataRow.createCell(0);
            cell.setCellValue(rootNode.asText());
            cell.setCellStyle(dataStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < columnMap.size(); i++) {
            currentSheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
            workbook.write(outputStream);
        }
        
        System.out.println("Successfully converted JSON to Excel: " + excelFilePath);
    }
    
    private void processJsonArray(JsonNode arrayNode, CellStyle headerStyle, CellStyle dataStyle) {
        List<Map<String, Object>> flattenedData = new ArrayList<>();
        Set<String> allKeys = new LinkedHashSet<>();
        
        // First pass: flatten all objects and collect all keys
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                Map<String, Object> flattenedMap = new LinkedHashMap<>();
                flattenJsonObject(item, "", flattenedMap);
                flattenedData.add(flattenedMap);
                allKeys.addAll(flattenedMap.keySet());
            } else if (item.isArray()) {
                // Handle nested arrays
                Map<String, Object> flattenedMap = new LinkedHashMap<>();
                flattenJsonArray(item, "", flattenedMap, 0);
                flattenedData.add(flattenedMap);
                allKeys.addAll(flattenedMap.keySet());
            } else {
                // Primitive value in array
                Map<String, Object> flattenedMap = new LinkedHashMap<>();
                flattenedMap.put("Value", getJsonValue(item));
                flattenedData.add(flattenedMap);
                allKeys.add("Value");
            }
        }
        
        // Ensure all rows have all keys
        for (Map<String, Object> row : flattenedData) {
            for (String key : allKeys) {
                row.putIfAbsent(key, "");
            }
        }
        
        processFlattenedData(flattenedData, headerStyle, dataStyle);
    }
    
    private void processFlattenedData(List<Map<String, Object>> flattenedData, 
                                     CellStyle headerStyle, CellStyle dataStyle) {
        if (flattenedData.isEmpty()) {
            return;
        }
        
        // Create header row
        List<String> headers = new ArrayList<>(flattenedData.get(0).keySet());
        createHeaderRow(headers, headerStyle);
        
        // Create data rows
        for (Map<String, Object> rowData : flattenedData) {
            Row dataRow = currentSheet.createRow(currentRowNum++);
            int colNum = 0;
            for (String header : headers) {
                Cell cell = dataRow.createCell(colNum++);
                Object value = rowData.get(header);
                setCellValue(cell, value);
                cell.setCellStyle(dataStyle);
            }
        }
    }
    
    private void createHeaderRow(List<String> headers, CellStyle headerStyle) {
        headerRow = currentSheet.createRow(currentRowNum++);
        int colNum = 0;
        for (String header : headers) {
            columnMap.put(header, colNum);
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);
        }
    }
    
    private void flattenJsonObject(JsonNode node, String prefix, Map<String, Object> result) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey();
            JsonNode value = field.getValue();
            
            if (value.isObject()) {
                flattenJsonObject(value, key, result);
            } else if (value.isArray()) {
                flattenJsonArray(value, key, result, 0);
            } else {
                result.put(key, getJsonValue(value));
            }
        }
    }
    
    private void flattenJsonArray(JsonNode arrayNode, String prefix, Map<String, Object> result, int index) {
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode item = arrayNode.get(i);
            String key = prefix + "[" + i + "]";
            
            if (item.isObject()) {
                flattenJsonObject(item, key, result);
            } else if (item.isArray()) {
                flattenJsonArray(item, key, result, 0);
            } else {
                result.put(key, getJsonValue(item));
            }
        }
    }
    
    private Object getJsonValue(JsonNode node) {
        if (node.isNull()) {
            return "";
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isDouble() || node.isFloat()) {
            return node.asDouble();
        } else {
            return node.asText();
        }
    }
    
    private void setCellValue(Cell cell, Object value) {
        if (value == null || value.equals("")) {
            cell.setCellValue("");
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    public void close() throws IOException {
        workbook.close();
    }
    
    /**
     * Main method for command-line usage
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar json-to-excel.jar <input.json> <output.xlsx>");
            System.out.println("Example: java -jar json-to-excel.jar data.json output.xlsx");
            return;
        }
        
        String jsonFilePath = args[0];
        String excelFilePath = args[1];
        
        JsonToExcelConverter converter = new JsonToExcelConverter();
        try {
            converter.convertJsonToExcel(jsonFilePath, excelFilePath);
        } catch (IOException e) {
            System.err.println("Error converting JSON to Excel: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                converter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

