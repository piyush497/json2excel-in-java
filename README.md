# JSON to Excel Converter

A Java application that converts multi-level JSON files to Excel format, handling nested objects and arrays by flattening them into a tabular structure.

## Features

- **Multi-level JSON Support**: Handles nested objects and arrays of any depth
- **Automatic Flattening**: Converts nested structures into flat columns using dot notation (e.g., `address.city`)
- **Array Handling**: Arrays are indexed in column names (e.g., `skills[0]`, `skills[1]`)
- **Formatted Output**: Creates Excel files with styled headers and borders
- **Auto-sized Columns**: Automatically adjusts column widths for better readability

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Project Structure

```
java/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── jsontoexcel/
        │           └── JsonToExcelConverter.java
        └── resources/
            └── sample.json
```

## Building the Project

```bash
mvn clean package
```

This will create a JAR file in the `target/` directory.

## Usage

### Command Line

```bash
java -jar target/json-to-excel-1.0.0.jar <input.json> <output.xlsx>
```

Example:
```bash
java -jar target/json-to-excel-1.0.0.jar src/main/resources/sample.json output.xlsx
```

### Programmatic Usage

```java
import com.jsontoexcel.JsonToExcelConverter;

// Convert from JSON file
JsonToExcelConverter converter = new JsonToExcelConverter();
converter.convertJsonToExcel("input.json", "output.xlsx");
converter.close();

// Convert from JSON string
String jsonString = "{\"name\":\"John\",\"age\":30}";
converter.convertJsonStringToExcel(jsonString, "output.xlsx");
converter.close();
```

## How It Works

1. **JSON Parsing**: Uses Jackson to parse JSON into a tree structure
2. **Flattening**: Recursively flattens nested objects and arrays:
   - Objects: `user.address.city` becomes a column
   - Arrays: `skills[0]`, `skills[1]` become separate columns
   - Nested arrays: `projects[0].name`, `projects[0].status` become columns
3. **Excel Creation**: Uses Apache POI to create styled Excel worksheets with:
   - Bold headers with gray background
   - Borders around all cells
   - Auto-sized columns
4. **Data Mapping**: Ensures all rows have consistent columns, filling missing values with empty strings

## Sample JSON Structure

The included `sample.json` demonstrates:
- Top-level objects
- Nested objects (address)
- Arrays of primitives (skills)
- Arrays of objects (employees, projects)

## Output Format

The Excel output will have columns like:
- `company`
- `employees[0].id`
- `employees[0].name`
- `employees[0].address.street`
- `employees[0].address.city`
- `employees[0].skills[0]`
- `employees[0].projects[0].name`
- `departments[0].name`
- etc.

## Dependencies

- **Apache POI 5.2.5**: Excel file creation and manipulation
- **Jackson 2.15.2**: JSON parsing and processing

## License

This project is provided as-is for educational and development purposes.

