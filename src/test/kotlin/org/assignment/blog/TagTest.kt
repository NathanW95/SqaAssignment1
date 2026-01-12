package org.assignment.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TagTest {
    // Entity initialization tests
    @Test
    fun `GIVEN new tag WHEN created THEN id is null by default`() {
        val tag = Tag(name = "Travel")
        assertNull(tag.id)
    }

    @Test
    fun `GIVEN new tag WHEN created THEN posts set is empty`() {
        val tag = Tag(name = "Travel")
        assertTrue(tag.posts.isEmpty())
    }

    // Entity behavior tests
    @Test
    fun `GIVEN tag WHEN toString called THEN returns name`() {
        val tag = Tag(name = "Travel")
        val result = tag.toString()

        assertEquals("Travel", result)
    }

    // Relationship management tests
    @Test
    fun `GIVEN tag and post WHEN post added to tag THEN posts contains post`() {
        val tag = Tag(name = "Travel")
        val post = BlogPost(title = "Test Post", content = "Content", author = "Alice")

        tag.posts.add(post)

        assertTrue(tag.posts.contains(post))
        assertEquals(1, tag.posts.size)
    }

    @Test
    fun `GIVEN tag with post WHEN post removed THEN posts is empty`() {
        val tag = Tag(name = "Travel")
        val post = BlogPost(title = "Test Post", content = "Content", author = "Alice")
        tag.posts.add(post)

        tag.posts.remove(post)

        assertTrue(tag.posts.isEmpty())
    }
}
