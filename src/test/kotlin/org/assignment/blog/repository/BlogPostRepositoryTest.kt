package org.assignment.blog.repository

import org.assignment.blog.model.BlogPost
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class BlogPostRepositoryTest(
    @Autowired val repository: BlogPostRepository,
) {
    companion object {
        fun createPost(
            title: String = "Test Post",
            content: String = "Test Content",
            author: String = "Alice",
        ) = BlogPost(title = title, content = content, author = author)
    }

    @Test
    fun `GIVEN a new post WHEN saved THEN it can be loaded by id`() {
        val post = createPost(title = "Hello", content = "World")

        val saved = repository.save(post)
        val found = repository.findById(saved.id!!).orElseThrow()

        Assertions.assertEquals("Hello", found.title)
        Assertions.assertEquals("World", found.content)
        Assertions.assertEquals("Alice", found.author)
        Assertions.assertNotNull(found.createdAt)
    }

    @Test
    fun `GIVEN posts WHEN getPostLengths called THEN returns title plus content lengths`() {
        repository.save(createPost(title = "Hi", content = "There", author = "A")) // 2 + 5 = 7
        repository.save(createPost(title = "Hey", content = "Yo", author = "B")) // 3 + 2 = 5

        val lengths = repository.getPostLengths().sorted()

        Assertions.assertEquals(listOf(5, 7), lengths)
    }
}
