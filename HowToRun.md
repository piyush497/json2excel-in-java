# How to Run

## Prerequisites
- Java 11 or higher installed and on PATH (`java -version`)
- Maven 3.6+ installed (`mvn -v`)

## Build the Project
```bash
mvn clean package
```
This produces the executable JAR at `target/json-to-excel-1.0.0.jar` (fat JAR with dependencies).

## Run with Sample JSON
- Windows PowerShell / Command Prompt:
```bash
java -jar target/json-to-excel-1.0.0.jar src/main/resources/sample.json output.xlsx
```
- macOS/Linux:
```bash
java -jar target/json-to-excel-1.0.0.jar src/main/resources/sample.json output.xlsx
```
This creates `output.xlsx` in the project root.

## Run with Your Own JSON
```bash
java -jar target/json-to-excel-1.0.0.jar <path/to/input.json> <path/to/output.xlsx>
```
Examples:
```bash
# Absolute paths
java -jar target/json-to-excel-1.0.0.jar C:/data/input.json C:/data/output.xlsx

# Relative paths
java -jar target/json-to-excel-1.0.0.jar data/input.json exports/out.xlsx
```

## Notes
- The application supports multi-level JSON (nested objects and arrays). Nested fields become columns using dot and index notation (e.g., `address.city`, `projects[0].name`).
- If you see a Log4j warning about missing logging implementation, it is safe to ignore. It does not affect conversion.
- Ensure the input JSON is valid. If in doubt, validate it with any JSON validator.

## Programmatic Use (Optional)
If using from code:
```java
JsonToExcelConverter converter = new JsonToExcelConverter();
converter.convertJsonToExcel("input.json", "output.xlsx");
converter.close();
```
