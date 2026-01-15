package org.assignment.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BlogPostTest {
    companion object {
        fun createPost(
            title: String = "Test Post",
            content: String = "Test Content",
            author: String = "Alice",
            createdAt: LocalDateTime = LocalDateTime.now(),
        ) = BlogPost(title = title, content = content, author = author, createdAt = createdAt)
    }

    @Test
    fun `GIVEN a blog post with all fields WHEN toString is called THEN return the expected string`() {
        val post =
            createPost(
                title = "Test Post",
                content = "This is a test post.",
                author = "Test Author",
                createdAt = LocalDateTime.of(2025, 1, 1, 0, 0),
            )
        assertEquals("\"Test Post\" by Test Author (2025-01-01)", post.toString())
    }

    @Test
    fun `GIVEN new post WHEN constructed THEN id is null by default`() {
        val post = BlogPost()
        assertNull(post.id)
    }

    @Test
    fun `GIVEN new post WHEN constructed without createdAt THEN createdAt is initialised`() {
        val before = LocalDateTime.now()
        val post = createPost()
        val after = LocalDateTime.now()

        val createdAt = post.createdAt
        assert(createdAt.isAfter(before) && createdAt.isBefore(after))
    }
}
