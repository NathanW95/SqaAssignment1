package org.assignment.blog.model

import org.junit.jupiter.api.Assertions
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
        Assertions.assertNull(tag.id)
    }

    @Test
    fun `GIVEN new tag WHEN created THEN posts set is empty`() {
        val tag = createTag()
        Assertions.assertTrue(tag.posts.isEmpty())
    }

    // Entity behavior tests
    @Test
    fun `GIVEN tag WHEN toString called THEN returns name`() {
        val tag = createTag()
        val result = tag.toString()

        Assertions.assertEquals(TRAVEL, result)
    }

    // Relationship management tests
    @Test
    fun `GIVEN tag and post WHEN post added to tag THEN posts contains post`() {
        val tag = createTag()
        val post = createPost()

        tag.posts.add(post)

        Assertions.assertTrue(tag.posts.contains(post))
        Assertions.assertEquals(1, tag.posts.size)
    }

    @Test
    fun `GIVEN tag with post WHEN post removed THEN posts is empty`() {
        val tag = createTag()
        val post = createPost()
        tag.posts.add(post)

        tag.posts.remove(post)

        Assertions.assertTrue(tag.posts.isEmpty())
    }
}
