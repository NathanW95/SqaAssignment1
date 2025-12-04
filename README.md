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

## Code formatting and style

This project uses **Spotless** with **ktlint** to enforce consistent Kotlin formatting.

### Check formatting

To verify that all code follows the style rules (fails if issues are found):

```bash
./gradlew spotlessCheck
```

On Windows:

```bash
gradlew.bat spotlessCheck
```

### Auto-fix formatting

To automatically fix formatting issues (whitespace, indentation, trailing newlines):

```bash
./gradlew spotlessApply
```

On Windows:

```bash
gradlew.bat spotlessApply
```

This will reformat your Kotlin source files and Gradle build scripts to match the project style.

## Continuous Integration

This project uses **GitHub Actions** for automated testing and code quality checks.

### CI Pipeline

Every push and pull request to the `master` branch automatically triggers:

1. **Code formatting check** (`spotlessCheck`) - Ensures all code follows Kotlin style conventions
2. **Test suite** (`test`) - Runs all JUnit tests with detailed output
3. **Test reporting** - Publishes test results in an easy-to-read format in the GitHub Actions UI

### Viewing CI Results

- Go to the **Actions** tab in the GitHub repository
- Click on any workflow run to see detailed results
- The **"Publish test report"** step creates a visual test results UI:
  - Click on the workflow run summary to see a "Test Results" section
  - Shows all tests with ✅ pass/❌ fail status
  - Displays error messages and stack traces for failures
  - No need to download artifacts - results are viewable directly in the browser
- The pipeline fails fast: if formatting is incorrect, tests are skipped

### Running CI Checks Locally

Before pushing, you can run the same checks locally:

```bash
./gradlew spotlessCheck test
```

This ensures your code will pass CI before you push.
