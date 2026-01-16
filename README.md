# Blog Application with Tagging System

A Spring Boot blog application implementing a tagging and categorization system with comprehensive testing, automated quality checks, and security best practices.

---

## Quick Start

### Prerequisites
- JDK 17+
- Git

### Run the Application

```bash
git clone <repository-url>
cd SqaAssignment1Blog
./gradlew bootRun
```

Access at: **http://localhost:8080**

### Run Tests

```bash
# All tests (79 tests)
./gradlew test

# View test report
open build/reports/tests/test/index.html


# Mutation tests (100% coverage)
./gradlew checkMutationCoverage

# View mutation report
open build/reports/pitest/index.html

# Check code formatting
./gradlew spotlessCheck

# Auto-fix formatting issues
./gradlew spotlessApply
```

### Pre-commit Hook

Formatting is checked automatically before each commit:

```bash
# .git/hooks/pre-commit
./gradlew spotlessCheck
```

---

## Tech Stack

### Core Framework
- **Kotlin** - Null safety prevents runtime errors, concise syntax
- **Spring Boot** - Industry-standard framework with auto-configuration
- **H2 Database** - In-memory DB for fast development/testing, easy production switch

### View Layer
- **Thymeleaf** - Server-side template engine with automatic XSS protection through auto-escaping

### Testing
- **JUnit 5** - Standard testing framework with modern features and Spring Boot integration
- **Mockito** - Mocking framework for unit testing, isolates units from dependencies
- **PITest** - Mutation testing framework that measures test quality, not just execution

### Code Quality
- **Spotless** - Automated code formatting with zero manual effort, enforces consistent style
- **ktlint** - Kotlin linter integrated with Spotless for coding conventions

### Why This Stack?

**Team Experience:** Familiar with Spring Boot, Gradle, JUnit from previous projects.

**Quality Focus:** Built-in testing tools (JUnit, Mockito), mutation testing (PITest), automated formatting (Spotless).

**Security:** Thymeleaf auto-escaping prevents XSS, JPA parameterized queries prevent SQL injection.

**Developer Productivity:** Fast feedback loop with H2, automated formatting, compile-time null safety.

---

## Project Structure

```
src/main/kotlin/org/assignment/blog/
├── controller/          # HTTP request handling
│   └── BlogController.kt
├── service/            # Business logic
│   ├── BlogPostService.kt
│   └── TagService.kt
├── repository/         # Data access
│   ├── BlogPostRepository.kt
│   └── TagRepository.kt
└── model/             # JPA entities
    ├── BlogPost.kt
    └── Tag.kt
```

### Architecture

**Layered design** following Spring Boot best practices:
- **Controller** handles HTTP, delegates to service
- **Service** contains business logic (tag normalization, post creation)
- **Repository** abstracts database operations
- **Model** defines entities and relationships

**Benefits:**
- Testable - service layer independent of HTTP
- Maintainable - clear separation of concerns
- Follows SOLID principles (Single Responsibility)

**Example:** Controller reduced from 161 lines to 107 lines (33% reduction) after extracting service layer.

---

## Features

### Tagging System

Users can:
- Create posts with comma-separated tags: `food, travel, lifestyle`
- Click tags to filter posts
- View tag statistics sorted by popularity
- Edit tags with automatic normalization

**Tag Normalization:**
Input: `TRAVEL, Travel,  travel ` → Output: `travel` (single tag)

Handles:
- Mixed case → lowercase
- Extra whitespace → trimmed
- Duplicates → removed
- Empty strings → filtered

### Database Design

**Many-to-many relationship:**
```
BlogPost ←→ blog_post_tags ←→ Tag
```

**Cascade behavior:**
- Saving a post with new tags automatically persists tags
- Deleting a post removes join table entries, keeps tags
- Tags have unique constraint at database level

---

## Testing Strategy

### Approach

**BDD Naming:** Self-documenting tests following `GIVEN [context] WHEN [action] THEN [outcome]` pattern.

**TDD:** Incremental development with tests written alongside implementation (or first when no dependencies).

**Test-First Examples:**
- `TagService.parseTagNames()` - pure logic, no dependencies
- `BlogPostService.getPostStatistics()` - clear requirements, mockable

**Implementation-First Examples:**
- JPA cascade behavior - needs entity configuration
- Repository queries - needs database schema

### Coverage

**79 tests** organized by layer:
- **32 unit tests** - Service layer with mocked dependencies
- **28 integration tests** - Controller layer with MockMvc
- **15 repository tests** - JPA relationships with real H2 database
- **4 model tests** - Entity initialization

**What's tested:**
- Business logic in isolation
- HTTP request/response handling
- Database persistence and relationships
- Edge cases: null input, duplicates, whitespace
- Error handling: invalid IDs, constraint violations

### Mutation Testing

**100% mutation coverage** (41/41 mutants killed)

#### Why Mutation Testing?

Line coverage measures which code is executed, but not whether tests actually verify behavior. Mutation testing introduces bugs (mutants) into the code and checks if tests catch them.

**Benefits:**
- **Finds weak tests** - Tests that execute code but don't verify behavior
- **Proves test quality** - 100% line coverage doesn't mean tests are effective
- **Increases confidence** - Ensures tests will catch real bugs during refactoring
- **Prevents false security** - Line coverage can be misleading without mutation testing

#### Real Bug Found

Initial `updatePost` test only verified `save()` was called, not that the post was modified. PITest removed `setTitle()` and `setContent()` calls - test still passed.

**Before:**
```kotlin
@Test
fun `GIVEN existing post WHEN updatePost THEN updates and returns post`() {
    val result = blogPostService.updatePost(1L, "New Title", "New Content", null)
    verify(repository).save(any<BlogPost>())  // WEAK
}
```

**After:**
```kotlin
@Test
fun `GIVEN existing post WHEN updatePost THEN updates and returns post`() {
    val result = blogPostService.updatePost(1L, "New Title", "New Content", null)
    assertEquals("New Title", existingPost.title)  // Verify state change
    assertEquals("New Content", existingPost.content)
    verify(repository).save(existingPost)
}
```

This demonstrates mutation testing's value - it found a gap that line coverage missed.

---

## Security

### XSS Prevention

**Thymeleaf auto-escaping** - most critical security feature.

Using `th:text` automatically escapes HTML/JavaScript:
```html
<!-- User input: <script>alert('XSS')</script> -->
<!-- Rendered as: &lt;script&gt;alert('XSS')&lt;/script&gt; -->
<span th:text="${post.title}">Title</span>
```

Browser displays text, doesn't execute script. Secure by default.

### SQL Injection Prevention

**JPA parameterized queries** - automatic with Spring Data JPA.

```kotlin
fun findByName(name: String): Optional<Tag>
```

Uses prepared statements, not string concatenation. User input treated as data, not SQL code.

### Input Validation

**Bean Validation** at application level:
```kotlin
@Entity
class BlogPost(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title too long")
    var title: String
)
```

**Database constraints** at persistence level:
```kotlin
@Entity
class Tag(
    @Column(unique = true)
    var name: String
)
```

Defense in depth - multiple validation layers.

---

## Code Quality

### Automated Formatting

**Spotless** enforces consistent style with zero manual effort.

**Pre-commit hook** formats code before commit:
```bash
# .git/hooks/pre-commit
./gradlew spotlessApply
```

**CI check** fails build if formatting is incorrect:
```bash
./gradlew spotlessCheck
```

**Benefits:**
- No style debates in code reviews
- Consistent codebase
- Automatic enforcement

**Tradeoff:** Initial setup time, but saves time long-term.

### Mutation Testing

**PITest** measures test quality, not just execution.

**Custom Gradle task** enforces 70% threshold:
```kotlin
tasks.register("checkMutationCoverage") {
    doLast {
        val mutationScore = // parse XML report
        if (mutationScore < 70) {
            throw GradleException("Mutation coverage below 70%")
        }
    }
}
```

**Benefits:**
- Finds weak tests that line coverage misses
- Proves tests verify behavior
- Increases confidence in refactoring

**Tradeoff:** Slower than unit tests (~30 seconds vs 5 seconds), but worth it for quality assurance.

---

## CI/CD Pipeline

### GitHub Actions Workflow

Runs on every commit:

1. **Checkout code**
2. **Setup Java 17**
3. **Run tests** (79 tests)
4. **Check formatting** (Spotless)
5. **Run mutation tests** (PITest)
6. **Upload artifacts** (test reports, mutation reports)

**Build fails if:**
- Any test fails
- Formatting is incorrect
- Mutation coverage < 70% (if threshold & shouldFailBuild Boolean is true)

**Benefits:**
- Catches issues before merge
- Ensures every commit maintains quality
- No manual intervention required

---

## Design Decisions

### Benefits

**Layered Architecture:**
- ✅ Testable without HTTP layer
- ✅ Clear separation of concerns
- ✅ Easy to understand and maintain

**H2 In-Memory Database:**
- ✅ Fast test execution (milliseconds)
- ✅ No external dependencies
- ✅ Easy to switch to PostgreSQL/MySQL

**Kotlin:**
- ✅ Null safety prevents NullPointerExceptions
- ✅ Concise syntax, less boilerplate
- ✅ 100% Java interoperable

**Automated Quality Checks:**
- ✅ Spotless enforces formatting
- ✅ PITest ensures test quality
- ✅ CI/CD catches issues early

### Tradeoffs

**Mutation Testing:**
- ⚠️ Slower than unit tests
- ✅ But finds real bugs in tests

**Automated Formatting:**
- ⚠️ Initial setup time
- ✅ But saves time in code reviews

**Service Layer Extraction:**
- ⚠️ More files to maintain
- ✅ But much more testable

---

## Key Highlights

**Industry Standards:**
- SOLID principles (Single Responsibility)
- Design patterns (Repository, MVC, Dependency Injection)
- Layered architecture
- Automated testing and quality checks

**Test Quality:**
- 79 tests covering unit, integration, edge cases
- 100% mutation coverage
- BDD naming for self-documenting tests
- TDD where applicable (pure logic)

**Security:**
- XSS prevention (Thymeleaf auto-escaping)
- SQL injection prevention (JPA parameterized queries)
- Input validation (Bean Validation + database constraints)

**Automation:**
- CI/CD pipeline runs on every commit
- Automated formatting (Spotless)
- Automated test quality checks (PITest)
- Pre-commit hooks

---

## Example: Tag Normalization

**Pure business logic** - ideal for test-first development.

**Tests written first:**
```kotlin
@Test
fun `GIVEN mixed case tags WHEN parseTagNames THEN normalizes to lowercase`() {
    val result = tagService.parseTagNames("KoTlIn, SPRING")
    assertEquals(listOf("kotlin", "spring"), result)
}

@Test
fun `GIVEN tags with whitespace WHEN parseTagNames THEN trims whitespace`() {
    val result = tagService.parseTagNames("  kotlin  ,   spring   ")
    assertEquals(listOf("kotlin", "spring"), result)
}

@Test
fun `GIVEN duplicate tags WHEN parseTagNames THEN returns unique tags`() {
    val result = tagService.parseTagNames("food, FOOD, Food")
    assertEquals(listOf("food"), result)
}
```

**Implementation:**
```kotlin
fun parseTagNames(tagString: String?): List<String> {
    if (tagString.isNullOrBlank()) return emptyList()
    return tagString.split(",")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .distinct()
}
```

Functional, immutable, handles all edge cases.

---


