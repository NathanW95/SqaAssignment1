package org.assignment.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class BlogPostRepositoryTest(
    @Autowired val repository: BlogPostRepository,
) {
    @Test
    fun `GIVEN a new post WHEN saved THEN it can be loaded by id`() {
        val post =
            BlogPost(
                title = "Hello",
                content = "World",
                author = "Alice",
            )

        val saved = repository.save(post)
        val found = repository.findById(saved.id!!).orElseThrow()

        assertEquals("Hello", found.title)
        assertEquals("World", found.content)
        assertEquals("Alice", found.author)
        assertNotNull(found.createdAt)
    }

    @Test
    fun `GIVEN posts WHEN getPostLengths called THEN returns title plus content lengths`() {
        repository.save(BlogPost(title = "Hi", content = "There", author = "A")) // 2 + 5 = 7
        repository.save(BlogPost(title = "Hey", content = "Yo", author = "B")) // 3 + 2 = 5

        val lengths = repository.getPostLengths().sorted()

        assertEquals(listOf(5, 7), lengths)
    }
}
