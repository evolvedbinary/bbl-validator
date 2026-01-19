## Requirements
- Java 21
- Maven 3.6+

## Running the Application

### Using Maven:
```bash
mvn clean compile
```

```bash
mvn mn:run
```

## Using Docker:

```bash
mvn clean install
```
Use the absolute path to the schemas directory when running the docker container.

```bash
docker run -p 8080:8080 -v /path/to/schemas:/app/schemas:ro evolvedbinary/bbl-validator:1.0.0-SNAPSHOT
```


