package org.assignment.blog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.Optional

@SpringBootTest
class BlogPostServiceTest {
    @Autowired
    lateinit var blogPostService: BlogPostService

    @MockitoBean
    lateinit var repository: BlogPostRepository

    @MockitoBean
    lateinit var tagService: TagService

    companion object {
        const val FOOD = "food"
        const val TRAVEL = "travel"

        fun createPost(
            id: Long = 1L,
            title: String = "Test Post",
            content: String = "Test Content",
            author: String = "Alice",
        ) = BlogPost(id = id, title = title, content = content, author = author)

        fun createTag(
            id: Long = 1L,
            name: String = TRAVEL,
        ) = Tag(id = id, name = name)
    }

    // getAllPosts tests
    @Test
    fun `GIVEN posts exist WHEN getAllPosts THEN returns all posts`() {
        val posts =
            listOf(
                createPost(id = 1L, title = "Post 1"),
                createPost(id = 2L, title = "Post 2"),
            )
        whenever(repository.findAll()).thenReturn(posts)

        val result = blogPostService.getAllPosts()

        assertEquals(2, result.size)
        assertEquals(posts, result)
    }

    @Test
    fun `GIVEN no posts WHEN getAllPosts THEN returns empty list`() {
        whenever(repository.findAll()).thenReturn(emptyList())

        val result = blogPostService.getAllPosts()

        assertTrue(result.isEmpty())
    }

    // getPostById tests
    @Test
    fun `GIVEN existing post WHEN getPostById THEN returns post`() {
        val post = createPost(id = 1L)
        whenever(repository.findById(1L)).thenReturn(Optional.of(post))

        val result = blogPostService.getPostById(1L)

        assertEquals(post, result)
    }

    @Test
    fun `GIVEN non-existent post WHEN getPostById THEN throws NoSuchElementException`() {
        whenever(repository.findById(999L)).thenReturn(Optional.empty())

        val exception =
            assertThrows(NoSuchElementException::class.java) {
                blogPostService.getPostById(999L)
            }

        assertEquals("Post not found: 999", exception.message)
    }

    // createPost tests
    @Test
    fun `GIVEN valid post data without tags WHEN createPost THEN saves and returns post`() {
        val savedPost = createPost(id = 1L, title = "New Post")
        whenever(repository.save(any<BlogPost>())).thenReturn(savedPost)

        val result = blogPostService.createPost("New Post", "Content", "Alice", null)

        assertEquals(savedPost, result)
        verify(repository).save(any<BlogPost>())
    }

    @Test
    fun `GIVEN valid post data with tags WHEN createPost THEN processes tags and saves post`() {
        val savedPost = createPost(id = 1L, title = "New Post")
        whenever(repository.save(any<BlogPost>())).thenReturn(savedPost)

        val result = blogPostService.createPost("New Post", "Content", "Alice", "food, travel")

        assertEquals(savedPost, result)
        verify(tagService).processAndAssociateTags(any<BlogPost>(), any())
        verify(repository).save(any<BlogPost>())
    }

    // updatePost tests
    @Test
    fun `GIVEN existing post WHEN updatePost THEN updates and returns post`() {
        val existingPost = createPost(id = 1L, title = "Old Title")
        val updatedPost = createPost(id = 1L, title = "New Title", content = "New Content")
        whenever(repository.findById(1L)).thenReturn(Optional.of(existingPost))
        whenever(repository.save(any<BlogPost>())).thenReturn(updatedPost)

        val result = blogPostService.updatePost(1L, "New Title", "New Content", null)

        assertEquals("New Title", result.title)
        assertEquals("New Content", result.content)
        verify(repository).save(any<BlogPost>())
    }

    @Test
    fun `GIVEN existing post with tags WHEN updatePost THEN clears old tags and adds new ones`() {
        val existingPost = createPost(id = 1L)
        val oldTag = createTag(name = FOOD)
        existingPost.tags.add(oldTag)

        whenever(repository.findById(1L)).thenReturn(Optional.of(existingPost))
        whenever(repository.save(any<BlogPost>())).thenReturn(existingPost)

        blogPostService.updatePost(1L, "Title", "Content", "travel")

        assertTrue(existingPost.tags.isEmpty() || existingPost.tags.none { it.name == FOOD })
        verify(tagService).processAndAssociateTags(any<BlogPost>(), any())
        verify(repository).save(any<BlogPost>())
    }

    @Test
    fun `GIVEN non-existent post WHEN updatePost THEN throws NoSuchElementException`() {
        whenever(repository.findById(999L)).thenReturn(Optional.empty())

        val exception =
            assertThrows(NoSuchElementException::class.java) {
                blogPostService.updatePost(999L, "Title", "Content", null)
            }

        assertEquals("Post not found: 999", exception.message)
    }

    // deletePost tests
    @Test
    fun `GIVEN existing post WHEN deletePost THEN deletes post`() {
        whenever(repository.existsById(1L)).thenReturn(true)

        blogPostService.deletePost(1L)

        verify(repository).existsById(1L)
        verify(repository).deleteById(1L)
    }

    @Test
    fun `GIVEN non-existent post WHEN deletePost THEN does not attempt deletion`() {
        whenever(repository.existsById(999L)).thenReturn(false)

        blogPostService.deletePost(999L)

        verify(repository).existsById(999L)
        verify(repository, org.mockito.kotlin.never()).deleteById(999L)
    }

    // getPostStatistics tests
    @Test
    fun `GIVEN posts with lengths WHEN getPostStatistics THEN returns correct statistics`() {
        whenever(repository.getPostLengths()).thenReturn(listOf(5, 7, 9))

        val result = blogPostService.getPostStatistics()

        assertEquals(7.0, result["average"])
        assertEquals(7.0, result["median"])
        assertEquals(9, result["max"])
        assertEquals(5, result["min"])
        assertEquals(21, result["total"])
    }

    @Test
    fun `GIVEN no posts WHEN getPostStatistics THEN returns zero statistics`() {
        whenever(repository.getPostLengths()).thenReturn(emptyList())

        val result = blogPostService.getPostStatistics()

        assertEquals(0.0, result["average"])
        assertEquals(0.0, result["median"])
        assertEquals(0, result["max"])
        assertEquals(0, result["min"])
        assertEquals(0, result["total"])
    }

    @Test
    fun `GIVEN even number of lengths WHEN getPostStatistics THEN calculates correct median`() {
        whenever(repository.getPostLengths()).thenReturn(listOf(10, 2, 6, 4))

        val result = blogPostService.getPostStatistics()

        assertEquals(5.0, result["median"]) // (4 + 6) / 2
    }

    @Test
    fun `GIVEN odd number of lengths WHEN getPostStatistics THEN calculates correct median`() {
        whenever(repository.getPostLengths()).thenReturn(listOf(1, 3, 5))

        val result = blogPostService.getPostStatistics()

        assertEquals(3.0, result["median"]) // Middle value
    }

    @Test
    fun `GIVEN single post WHEN getPostStatistics THEN returns correct statistics`() {
        whenever(repository.getPostLengths()).thenReturn(listOf(10))

        val result = blogPostService.getPostStatistics()

        assertEquals(10.0, result["average"])
        assertEquals(10.0, result["median"])
        assertEquals(10, result["max"])
        assertEquals(10, result["min"])
        assertEquals(10, result["total"])
    }

    // getTagString tests
    @Test
    fun `GIVEN post with tags WHEN getTagString THEN returns comma-separated tag names`() {
        val post = createPost()
        val tag1 = createTag(id = 1L, name = FOOD)
        val tag2 = createTag(id = 2L, name = TRAVEL)
        post.tags.add(tag1)
        post.tags.add(tag2)

        val result = blogPostService.getTagString(post)

        assertTrue(result.contains(FOOD))
        assertTrue(result.contains(TRAVEL))
        assertTrue(result.contains(", "))
    }

    @Test
    fun `GIVEN post without tags WHEN getTagString THEN returns empty string`() {
        val post = createPost()

        val result = blogPostService.getTagString(post)

        assertEquals("", result)
    }
}
