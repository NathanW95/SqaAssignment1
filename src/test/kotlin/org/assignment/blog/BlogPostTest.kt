package org.assignment.blog

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BlogPostTest {

    @Test
    fun `GIVEN a blog post with all fields WHEN toString is called THEN return the expected string`() {
        val post = BlogPost(
            title = "Test Post",
            content = "This is a test post.",
            author = "Test Author",
            createdAt = LocalDateTime.of(2025, 1, 1,0,0),
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

        val post = BlogPost(
            title = "Title",
            content = "Content",
            author = "Author"
        )

        val after = LocalDateTime.now()

        val createdAt = post.createdAt
        assert(createdAt.isAfter(before) && createdAt.isBefore(after))
    }


}