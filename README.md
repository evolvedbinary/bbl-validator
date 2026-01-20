# Broadband label file Validator Service
This project is a solution for validating Broadband label files against defined Schemas. Developed by [Evolved Binary](https://www.evolvedbinary.com/) ,to be used by the FCC https://www.fcc.gov/broadbandlabels, the system consists of a HTTP REST API and a user-friendly Web Application.

## Overview
The Broadband label file Validator Service allows public users to validate Broadband label files against various variants and versions of Schema files. The project is built using Java 21 and is open-source under the Apache 2.0 License.

The core validation logic is constructed around the Open Source [CSV Validator](https://github.com/digital-preservation/csv-validator) library originally built for The National Archives.

## Features

HTTP REST API: A robust API that exposes validation services to the internet.

Web Application: A simple HTML5 wrapper around the API allowing users to upload files or provide URLs for validation.

Multiple Input Methods: Support for validating CSVs via direct file upload or external URL retrieval.

## Endpoints
Check the API documentation for more information https://app.swaggerhub.com/apis/evolvedbinary/bbl-validator-web-api/1.0.0#/schema/validateCsv. 

## Prerequisites
- Java 21
- Maven 3.6+

## Running the Application
Using Maven

```bash
git clone https://github.com/evolvedbinary/bbl-validator.git
cd bbl-validator
mvn clean compile
mvn mn:run
```

Using Docker

```bash
git clone https://github.com/evolvedbinary/bbl-validator.git
cd bbl-validator
mvn clean install
docker run -p 8080:8080 -v /path/to/schemas:/app/schemas:ro evolvedbinary/bbl-validator:1.0.0-SNAPSHOT
```

note: use the absolute path to the schemas directory when running the docker container.
 
## Contact and Support
Evolved Binary Limited 

Email: admin@evolvedbinary.com.

Web: https://www.evolvedbinary.com.

Phone: +44 (0)2032 397236 / +1 917 267-8787.