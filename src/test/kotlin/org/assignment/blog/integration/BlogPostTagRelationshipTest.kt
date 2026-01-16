package org.assignment.blog.integration

import org.assignment.blog.model.BlogPost
import org.assignment.blog.model.Tag
import org.assignment.blog.repository.BlogPostRepository
import org.assignment.blog.repository.TagRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

@DataJpaTest
class BlogPostTagRelationshipTest {
    @Autowired
    lateinit var blogPostRepository: BlogPostRepository

    @Autowired
    lateinit var tagRepository: TagRepository

    companion object {
        const val FOOD = "food"
        const val TRAVEL = "travel"
        const val LIFESTYLE = "lifestyle"
        const val TECH = "tech"

        fun createPost(
            title: String = "Test Post",
            content: String = "Content",
            author: String = "Alice",
        ) = BlogPost(title = title, content = content, author = author)

        fun createTag(name: String = TRAVEL) = Tag(name = name)
    }

    // Simple edge cases - entities without relationships
    @Test
    fun `GIVEN post without tags WHEN post deleted THEN succeeds`() {
        val post = createPost()
        val savedPost = blogPostRepository.save(post)

        savedPost.id?.let { blogPostRepository.deleteById(it) }
        blogPostRepository.flush()

        Assertions.assertFalse(blogPostRepository.findById(savedPost.id!!).isPresent)
    }

    @Test
    fun `GIVEN tag without posts WHEN tag deleted THEN succeeds`() {
        val tag = tagRepository.save(createTag(name = LIFESTYLE))

        tag.id?.let { tagRepository.deleteById(it) }
        tagRepository.flush()

        Assertions.assertFalse(tagRepository.findById(tag.id!!).isPresent)
    }

    // Basic relationship operations
    @Test
    fun `GIVEN post with tags WHEN post deleted THEN tags remain in database`() {
        val tag1 = tagRepository.save(createTag(name = FOOD))
        val tag2 = tagRepository.save(createTag(name = TRAVEL))

        val post = createPost()
        post.tags.add(tag1)
        post.tags.add(tag2)
        val savedPost = blogPostRepository.save(post)

        savedPost.id?.let { blogPostRepository.deleteById(it) }
        blogPostRepository.flush()

        Assertions.assertTrue(tagRepository.findById(tag1.id!!).isPresent)
        Assertions.assertTrue(tagRepository.findById(tag2.id!!).isPresent)
    }

    @Test
    fun `GIVEN post with tags WHEN post deleted THEN join table entries removed`() {
        val tag = tagRepository.save(createTag(name = TECH))

        val post = createPost()
        post.tags.add(tag)
        val savedPost = blogPostRepository.save(post)

        savedPost.id?.let { blogPostRepository.deleteById(it) }
        blogPostRepository.flush()

        val retrievedTag = tagRepository.findById(tag.id!!).get()
        Assertions.assertFalse(retrievedTag.posts.any { it.id == savedPost.id })
    }

    // Modifying relationships
    @Test
    fun `GIVEN post with multiple tags WHEN one tag removed THEN other tags remain`() {
        val tag1 = tagRepository.save(createTag(name = FOOD))
        val tag2 = tagRepository.save(createTag(name = LIFESTYLE))

        val post = createPost()
        post.tags.add(tag1)
        post.tags.add(tag2)
        val savedPost = blogPostRepository.save(post)

        post.tags.remove(tag1)
        blogPostRepository.save(post)
        blogPostRepository.flush()

        val retrievedPost = blogPostRepository.findById(savedPost.id!!).get()
        Assertions.assertEquals(1, retrievedPost.tags.size)
        Assertions.assertTrue(retrievedPost.tags.any { it.id == tag2.id })
        Assertions.assertFalse(retrievedPost.tags.any { it.id == tag1.id })
    }

    // Cascade behavior tests
    @Test
    fun `GIVEN post with unsaved tags WHEN post saved THEN tags are also saved`() {
        // UNSAVED tags (not calling tagRepository.save)
        val tag1 = createTag(name = FOOD)
        val tag2 = createTag(name = TRAVEL)

        val post = createPost()
        post.tags.add(tag1)
        post.tags.add(tag2)

        blogPostRepository.save(post)
        blogPostRepository.flush()

        Assertions.assertTrue(tagRepository.findById(tag1.id!!).isPresent)
        Assertions.assertTrue(tagRepository.findById(tag2.id!!).isPresent)
    }

    // Complex workflow - proper tag deletion
    @Test
    fun `GIVEN tag removed from all posts WHEN tag deleted THEN succeeds`() {
        val tag = tagRepository.save(createTag(name = TECH))

        val post = createPost()
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
        Assertions.assertFalse(tagRepository.findById(tag.id!!).isPresent)
        // Verify post still exists with no tags
        val retrievedPost = blogPostRepository.findById(savedPost.id!!).get()
        Assertions.assertTrue(retrievedPost.tags.isEmpty())
    }
}
