# Broadband label file Validator Service
This project provides a solution for validating Broadband Label files against defined Schemas. Developed by [Evolved Binary](https://www.evolvedbinary.com/) for use with the [FCC Broadband Labels](https://www.fcc.gov/broadbandlabels) initiative, the system consists of a HTTP REST API and a user-friendly Web Application.

The core validation logic is built upon the Open Source [CSV Validator](https://github.com/digital-preservation/csv-validator) library, originally developed for The National Archives.

## Features

* **HTTP REST API**: A robust API that exposes validation services to the internet.
* **Web Application**: A simple HTML5 interface allowing users to upload files or provide URLs for validation.
* **Multiple Input Methods**: Support for validating broadband label files via direct file upload or external URL retrieval.
* **Schema Management**: dynamically lists and utilizes validation schemas stored in the configured directory.

## Endpoints
Check the API documentation for more information https://app.swaggerhub.com/apis/evolvedbinary/bbl-validator-web-api/1.0.0#/schema/validateCsv.

## Built With

* [Java 21](https://openjdk.org/projects/jdk/21/) - Programming Language
* [Micronaut 4](https://micronaut.io/) - The JVM-based framework used
* [Apache Velocity](https://velocity.apache.org/) - Template engine for the Web Views
* [Maven](https://maven.apache.org/) - Dependency Management
* [Docker](https://www.docker.com/) - Containerization

## Prerequisites
* Java 21
* Maven 3.6+

## Getting Started
Using Maven

```bash
git clone https://github.com/evolvedbinary/bbl-validator.git
cd bbl-validator
mvn mn:run
```

Build the JAR

```bash
mvn clean package
```

Using Docker

```bash
git clone https://github.com/evolvedbinary/bbl-validator.git
cd bbl-validator
mvn clean install
docker run -p 8080:8080 -v /path/to/schemas:/app/schemas:ro evolvedbinary/bbl-validator:1.0.0-SNAPSHOT
```

note: use the absolute path to the schemas directory when running the docker container.

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

Email: admin@evolvedbinary.com.

Web: https://www.evolvedbinary.com.

Phone: +44 (0)2032 397236 / +1 917 267-8787.