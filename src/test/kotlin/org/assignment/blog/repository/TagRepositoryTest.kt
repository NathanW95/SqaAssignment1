package org.assignment.blog.repository

import org.assignment.blog.model.Tag
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest
class TagRepositoryTest {
    @Autowired
    lateinit var tagRepository: TagRepository

    companion object {
        const val FOOD = "food"
        const val TRAVEL = "travel"

        fun createTag(name: String = TRAVEL) = Tag(name = name)
    }

    // Basic persistence tests
    @Test
    fun `GIVEN new tag WHEN saved THEN can be retrieved by id`() {
        val tag = createTag(name = TRAVEL)

        val saved = tagRepository.save(tag)
        val retrieved = tagRepository.findById(saved.id!!).get()

        Assertions.assertNotNull(saved.id)
        Assertions.assertEquals(TRAVEL, retrieved.name)
    }

    @Test
    fun `GIVEN duplicate tag name WHEN saved THEN throws exception`() {
        tagRepository.save(createTag(name = TRAVEL))

        Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            tagRepository.save(createTag(name = TRAVEL))
            tagRepository.flush()
        }
    }

    // Custom query method tests
    @Test
    fun `GIVEN tag WHEN findByName called THEN returns correct tag`() {
        val tag = tagRepository.save(createTag(name = FOOD))

        val found = tagRepository.findByName(FOOD)

        Assertions.assertTrue(found.isPresent)
        Assertions.assertEquals(tag.id, found.get().id)
    }

    @Test
    fun `GIVEN no tag WHEN findByName called THEN returns empty`() {
        val found = tagRepository.findByName("NonExistent")

        Assertions.assertFalse(found.isPresent)
    }

    // Validation tests
    @Test
    fun `GIVEN tag with blank name WHEN saved THEN validation fails`() {
        val tag = createTag(name = "")

        Assertions.assertThrows(Exception::class.java) {
            tagRepository.saveAndFlush(tag)
        }
    }

    @Test
    fun `GIVEN tag with name exceeding max length WHEN saved THEN validation fails`() {
        val longName = "a".repeat(26)
        val tag = createTag(name = longName)

        Assertions.assertThrows(Exception::class.java) {
            tagRepository.saveAndFlush(tag)
        }
    }
}
