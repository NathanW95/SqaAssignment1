package org.assignment.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TagTest {
    companion object {
        const val TRAVEL = "travel"

        fun createTag(name: String = TRAVEL) = Tag(name = name)

        fun createPost(
            title: String = "Test Post",
            content: String = "Content",
            author: String = "Alice",
        ) = BlogPost(title = title, content = content, author = author)
    }

    // Entity initialization tests
    @Test
    fun `GIVEN new tag WHEN created THEN id is null by default`() {
        val tag = createTag()
        assertNull(tag.id)
    }

    @Test
    fun `GIVEN new tag WHEN created THEN posts set is empty`() {
        val tag = createTag()
        assertTrue(tag.posts.isEmpty())
    }

    // Entity behavior tests
    @Test
    fun `GIVEN tag WHEN toString called THEN returns name`() {
        val tag = createTag()
        val result = tag.toString()

        assertEquals(TRAVEL, result)
    }

    // Relationship management tests
    @Test
    fun `GIVEN tag and post WHEN post added to tag THEN posts contains post`() {
        val tag = createTag()
        val post = createPost()

        tag.posts.add(post)

        assertTrue(tag.posts.contains(post))
        assertEquals(1, tag.posts.size)
    }

    @Test
    fun `GIVEN tag with post WHEN post removed THEN posts is empty`() {
        val tag = createTag()
        val post = createPost()
        tag.posts.add(post)

        tag.posts.remove(post)

        assertTrue(tag.posts.isEmpty())
    }
}
