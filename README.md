# Microservices Project

A Maven-based Java project for building microservices applications.

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/app/
│   │   └── resources/
│   └── test/
│       ├── java/
│       │   └── com/example/app/
│       └── resources/
├── pom.xml
└── README.md
```

## Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher

## Building the Project

```bash
# Clean and install dependencies
mvn clean install

# Compile the project
mvn compile

# Run tests
mvn test
```

## Development

- Add Java source files to `src/main/java/com/example/app/`
- Add test files to `src/test/java/com/example/app/`
- Update dependencies in [pom.xml](pom.xml) as needed

## Project Information

- **GroupId**: com.example
- **ArtifactId**: microservices
- **Version**: 1.0-SNAPSHOT
- **Java Version**: 11
