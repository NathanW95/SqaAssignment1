package org.assignment.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class BlogPostTagRelationshipTest {
    @Autowired
    lateinit var blogPostRepository: BlogPostRepository

    @Autowired
    lateinit var tagRepository: TagRepository

    @Test
    fun `GIVEN post without tags WHEN post deleted THEN succeeds`() {
        val post = BlogPost(title = "Test", content = "Content", author = "Alice")
        val savedPost = blogPostRepository.save(post)

        savedPost.id?.let { blogPostRepository.deleteById(it) }
        blogPostRepository.flush()

        assertFalse(blogPostRepository.findById(savedPost.id!!).isPresent)
    }

    @Test
    fun `GIVEN tag without posts WHEN tag deleted THEN succeeds`() {
        val tag = tagRepository.save(Tag(name = "Orphan"))

        tag.id?.let { tagRepository.deleteById(it) }
        tagRepository.flush()

        assertFalse(tagRepository.findById(tag.id!!).isPresent)
    }

    @Test
    fun `GIVEN post with tags WHEN post deleted THEN tags remain in database`() {
        val tag1 = tagRepository.save(Tag(name = "Kotlin"))
        val tag2 = tagRepository.save(Tag(name = "Spring"))

        val post = BlogPost(title = "Test", content = "Content", author = "Alice")
        post.tags.add(tag1)
        post.tags.add(tag2)
        val savedPost = blogPostRepository.save(post)

        savedPost.id?.let { blogPostRepository.deleteById(it) }
        blogPostRepository.flush()

        assertTrue(tagRepository.findById(tag1.id!!).isPresent)
        assertTrue(tagRepository.findById(tag2.id!!).isPresent)
    }

    @Test
    fun `GIVEN post with tags WHEN post deleted THEN join table entries removed`() {
        val tag = tagRepository.save(Tag(name = "Java"))

        val post = BlogPost(title = "Test", content = "Content", author = "Alice")
        post.tags.add(tag)
        val savedPost = blogPostRepository.save(post)

        savedPost.id?.let { blogPostRepository.deleteById(it) }
        blogPostRepository.flush()

        val retrievedTag = tagRepository.findById(tag.id!!).get()
        assertFalse(retrievedTag.posts.any { it.id == savedPost.id })
    }

    @Test
    fun `GIVEN post with multiple tags WHEN one tag removed THEN other tags remain`() {
        val tag1 = tagRepository.save(Tag(name = "Backend"))
        val tag2 = tagRepository.save(Tag(name = "Frontend"))

        val post = BlogPost(title = "Test", content = "Content", author = "Alice")
        post.tags.add(tag1)
        post.tags.add(tag2)
        val savedPost = blogPostRepository.save(post)

        post.tags.remove(tag1)
        blogPostRepository.save(post)
        blogPostRepository.flush()

        val retrievedPost = blogPostRepository.findById(savedPost.id!!).get()
        assertEquals(1, retrievedPost.tags.size)
        assertTrue(retrievedPost.tags.any { it.id == tag2.id })
        assertFalse(retrievedPost.tags.any { it.id == tag1.id })
    }

    @Test
    fun `GIVEN post with unsaved tags WHEN post saved THEN tags are also saved`() {
        // UNSAVED tags (not calling tagRepository.save)
        val tag1 = Tag(name = "NewTag1")
        val tag2 = Tag(name = "NewTag2")

        val post = BlogPost(title = "Test", content = "Content", author = "Alice")
        post.tags.add(tag1)
        post.tags.add(tag2)

        val savedPost = blogPostRepository.save(post)
        blogPostRepository.flush()

        assertTrue(tagRepository.findById(tag1.id!!).isPresent)
        assertTrue(tagRepository.findById(tag2.id!!).isPresent)
    }

    @Test
    fun `GIVEN tag removed from all posts WHEN tag deleted THEN succeeds`() {
        val tag = tagRepository.save(Tag(name = "Python"))

        val post = BlogPost(title = "Test", content = "Content", author = "Alice")
        post.tags.add(tag)
        val savedPost = blogPostRepository.save(post)

        // User removes tag from post first (realistic workflow)
        val postToUpdate = blogPostRepository.findById(savedPost.id!!).get()
        postToUpdate.tags.remove(tag)
        blogPostRepository.save(postToUpdate)
        blogPostRepository.flush()

        // Now tag can be safely deleted since no posts reference it
        tag.id?.let { tagRepository.deleteById(it) }
        tagRepository.flush()

        // Verify tag is deleted
        assertFalse(tagRepository.findById(tag.id!!).isPresent)
        // Verify post still exists with no tags
        val retrievedPost = blogPostRepository.findById(savedPost.id!!).get()
        assertTrue(retrievedPost.tags.isEmpty())
    }

    // TODO: if adding Optional tag management feature... Add controller test to verify tags cannot be deleted when associated with posts
    // Controller would check: if (tag.posts.isNotEmpty()) throw IllegalStateException(...)
}
