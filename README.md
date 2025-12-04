# SqaAssignment1Blog

Spring Boot + Kotlin project for the Software Quality Assurance assignment.

This project will replicate and extend the functionality of the provided Python blog template, using Spring Boot and Kotlin instead.

## Tech stack & rationale

- **Language**: Kotlin (on the JVM)
- **Framework**: Spring Boot
- **Build tool**: Gradle (using the Gradle wrapper in this repo)
- **View layer**: Thymeleaf
- **Database**: H2 (in-memory database via Spring Data JPA)

I chose this stack because I already have experience with Spring Boot and Java, 
and I have recently moved to a new team at Booking that uses Kotlin and Spring. 
This project is a way to further my knowledge and skills with this tech stack, 
so that I can bring value back to my team.

## Running the Spring Boot Application

### 1. Prerequisites

- Java 17 (or compatible JDK)
- Gradle wrapper (included in this project)

### 2. Build and Run the Application

From the project root:

```bash
./gradlew bootRun
```

On Windows (PowerShell or Command Prompt):

```bash
gradlew.bat bootRun
```

### 3. Open the Application in Your Browser

After the app starts, open:

```text
http://localhost:8080
```

This is the Spring Boot + Kotlin version of the original Flask blog template.

### 4. Stopping the Application

Press `Ctrl + C` in the terminal where `bootRun` is running.

## Running tests

All tests are run via the Gradle wrapper, so this works the same in IntelliJ, VS Code, or a plain terminal.

From the project root:

```bash
./gradlew test
```

On Windows:

```bash
gradlew.bat test
```

This will compile and run all JUnit tests.

### Viewing test results

- **In the terminal**: Gradle prints a summary of passed/failed tests. For more detail you can run:

  ```bash
  ./gradlew test --info
  ```

- **HTML test report**: Gradle also generates a detailed report at:

  ```text
  build/reports/tests/test/index.html
  ```

  Open this file in a browser to see per-test-class and per-test-case results.
