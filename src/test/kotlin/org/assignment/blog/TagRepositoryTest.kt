package org.assignment.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest
class TagRepositoryTest {
    @Autowired
    lateinit var tagRepository: TagRepository

    // Basic persistence tests
    @Test
    fun `GIVEN new tag WHEN saved THEN can be retrieved by id`() {
        val tag = Tag(name = "Travel")

        val saved = tagRepository.save(tag)
        val retrieved = tagRepository.findById(saved.id!!).get()

        assertNotNull(saved.id)
        assertEquals("Travel", retrieved.name)
    }

    @Test
    fun `GIVEN duplicate tag name WHEN saved THEN throws exception`() {
        tagRepository.save(Tag(name = "Travel"))

        assertThrows(DataIntegrityViolationException::class.java) {
            tagRepository.save(Tag(name = "Travel"))
            tagRepository.flush()
        }
    }

    // Custom query method tests
    @Test
    fun `GIVEN tag WHEN findByName called THEN returns correct tag`() {
        val tag = tagRepository.save(Tag(name = "Food"))

        val found = tagRepository.findByName("Food")

        assertTrue(found.isPresent)
        assertEquals(tag.id, found.get().id)
    }

    @Test
    fun `GIVEN no tag WHEN findByName called THEN returns empty`() {
        val found = tagRepository.findByName("NonExistent")

        assertFalse(found.isPresent)
    }

    // Validation tests
    @Test
    fun `GIVEN tag with blank name WHEN saved THEN validation fails`() {
        val tag = Tag(name = "")

        assertThrows(Exception::class.java) {
            tagRepository.saveAndFlush(tag)
        }
    }

    @Test
    fun `GIVEN tag with name exceeding max length WHEN saved THEN validation fails`() {
        val longName = "a".repeat(26)
        val tag = Tag(name = longName)

        assertThrows(Exception::class.java) {
            tagRepository.saveAndFlush(tag)
        }
    }
}
