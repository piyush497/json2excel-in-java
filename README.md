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

## Docker

Build image:

```bash
docker build -t json2excel:latest .
```

Run with mounted data directory (Windows PowerShell example):

```powershell
docker run --rm -v ${PWD}/data:/data json2excel:latest /data/input.json /data/output.xlsx
```

Notes:
- The container's entrypoint is `java -jar /app/json-to-excel.jar`, so you only pass the two file paths as args.
- Ensure `data` exists and contains `input.json`.

## Kubernetes

This app is best run as a Job (batch) that reads a JSON file and writes an Excel file.

1) Build and load the image to a registry or your cluster:

```bash
# Option A: Kind/minikube local load (example: minikube)
minikube image build -t json2excel:latest .

# Option B: Push to a registry
docker build -t <registry>/json2excel:latest .
docker push <registry>/json2excel:latest
# then update k8s/job.yaml image field
```

2) Apply manifests:

```bash
kubectl apply -f k8s/job.yaml
```

3) Get logs and retrieve the output:

```bash
kubectl logs job/json2excel-job
kubectl get pods -l job-name=json2excel-job
POD=$(kubectl get pods -l job-name=json2excel-job -o jsonpath='{.items[0].metadata.name}')
kubectl cp $POD:/data/output.xlsx ./output.xlsx
```

Customize:
- Edit `k8s/job.yaml` ConfigMap to change `input.json`.
- Replace the `emptyDir` volume with a `PersistentVolumeClaim` if you want the output to persist independent of the pod lifecycle.

