# Broadband Label file Validator Service
This project provides a solution for validating Broadband Label files against defined Schemas. Developed for [Comcast](https://www.comcast.com) by [Evolved Binary](https://www.evolvedbinary.com) for use with the [FCC Broadband Labels](https://www.fcc.gov/broadbandlabels) initiative, the system consists of a HTTP REST API and a user-friendly Web Application.

The core validation logic is built upon the Open Source [CSV Validator](https://github.com/digital-preservation/csv-validator) library, originally developed for [The National Archives](https://www.nationalarchives.gov.uk/).

## Features

* **HTTP REST API**: A robust API that exposes validation services to the internet.
* **Web Application**: A simple HTML5 interface allowing users to upload files or provide URLs for validation.
* **Multiple Input Methods**: Support for validating Broadband Label files via direct file upload or external URL retrieval.
* **Schema Management**: dynamically lists and utilizes validation schemas stored in the configured directory.

## Endpoints
Check the API documentation for more information https://app.swaggerhub.com/apis/evolvedbinary/bbl-validator-web-api/1.0.0#/schema/validateCsv.

## Built With

* [Java 21](https://openjdk.org/projects/jdk/21/) - Programming Language
* [Micronaut 4](https://micronaut.io/) - A Web Application framework
* [Apache Velocity](https://velocity.apache.org/) - Provides templating for the Web Pages
* [Maven](https://maven.apache.org/) - Build System
* [Docker](https://www.docker.com/) - Containerization


# Getting Started

## Prerequisites
* Java 21
* Maven 3.6+

## Running the Application

### Option 1: Running in Development Mode

Clone the repository and run the application directly using Maven:

```bash
git clone https://github.com/evolvedbinary/bbl-validator.git
cd bbl-validator
mvn mn:run
```

The application will start on http://localhost:8080.

### Option 2: Building and Running the JAR

#### Building the JAR

Build an executable JAR file with all dependencies included:

```bash
git clone https://github.com/evolvedbinary/bbl-validator.git
cd bbl-validator
mvn clean package
```

This will create an executable JAR file at `target/bbl-validator-1.0.0-SNAPSHOT.jar`.

#### Running the JAR

Once built, you can run the application using:

```bash
# Change the server port
java -Dmicronaut.server.port=9090 -jar target/bbl-validator-1.0.0-SNAPSHOT.jar

# Specify a schema directory
java -Dschema.directory=/path/to/schema -jar target/bbl-validator-1.0.0-SNAPSHOT.jar
```

### Option 3: Using Docker

#### Pulling and Running a Pre-built Image

Pull the latest image from Docker Hub and run it:

```bash
docker pull evolvedbinary/bbl-validator:1.0.0-SNAPSHOT
docker run -p 8080:8080 -v /path/to/schemas:/app/schemas:ro evolvedbinary/bbl-validator:1.0.0-SNAPSHOT
```

**Note:** Replace `/path/to/schemas` with the absolute path to your schemas directory on the host machine.

#### Building the Docker Image (Optional)

If you want to build the Docker image locally:

```bash
git clone https://github.com/evolvedbinary/bbl-validator.git
cd bbl-validator
mvn clean install
```

This will:
1. Build the application JAR
2. Create a Docker image tagged as `evolvedbinary/bbl-validator:1.0.0-SNAPSHOT`

Then run the locally built image:

```bash
docker run -p 8080:8080 -v /path/to/schemas:/app/schemas:ro evolvedbinary/bbl-validator:1.0.0-SNAPSHOT
```

## Usage

### Web Interface
Navigate to http://localhost:8080/views/validate in your browser. You can:

1. Select a Schema ID from the dropdown.

2. Choose to validate via URL or Content.

3. View the validation status and any error messages in the results table.

### HTTP API
You can validate files programmatically using the API. Full documentation is available via:

Online: [SwaggerHub API Docs](https://app.swaggerhub.com/apis/evolvedbinary/bbl-validator-web-api/1.0.0#/schema/validateCsv)

Local Spec: The OpenAPI YAML file is available at `/static/openapi.yaml` on the running server.

## Configuration
The application uses application.yml for configuration.

* Server Port: Defaults to `8080`.
* Schema Directory: Defaults to `schemas` relative to the working directory.
 
## Contact and Support
Evolved Binary Limited 

Email: tech@evolvedbinary.com

Web: https://www.evolvedbinary.com