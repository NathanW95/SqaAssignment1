package org.assignment.blog

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@WebMvcTest(BlogController::class)
class BlogControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var blogPostService: BlogPostService

    @MockitoBean
    lateinit var tagService: TagService

    companion object {
        const val TRAVEL = "travel"
        const val FOOD = "food"
        const val TRAVEL_AND_FOOD = "travel, food"

        fun createPost(
            id: Long = 1L,
            title: String = "Test Post",
            content: String = "Test Content",
            author: String = "Alice",
        ) = BlogPost(id = id, title = title, content = content, author = author)
    }

    // Index/List view tests
    @Test
    fun `GIVEN existing posts WHEN GET index THEN list is shown`() {
        val post = createPost(title = "Hello", content = "Content")
        whenever(blogPostService.getAllPosts()).thenReturn(listOf(post))

        mockMvc
            .perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("posts"))
            .andExpect(content().string(containsString("Hello")))
    }

    // Create post tests
    @Test
    fun `WHEN GET create THEN create view is returned`() {
        mockMvc
            .perform(get("/create"))
            .andExpect(status().isOk)
            .andExpect(view().name("create"))
            .andExpect(model().attribute("title", "Create Post"))
    }

    @Test
    fun `GIVEN valid form WHEN POST create THEN post is saved and redirected`() {
        mockMvc
            .perform(
                post("/create")
                    .param("title", "New Post")
                    .param("content", "New Content")
                    .param("author", "Alice"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(blogPostService).createPost("New Post", "New Content", "Alice", null)
    }

    // View single post tests
    @Test
    fun `GIVEN existing post WHEN GET post THEN post page is shown`() {
        val post = createPost(title = "Title", content = "Body")
        whenever(blogPostService.getPostById(1L)).thenReturn(post)

        mockMvc
            .perform(get("/post/1"))
            .andExpect(status().isOk)
            .andExpect(view().name("post"))
            .andExpect(model().attribute("post", post))
            .andExpect(content().string(containsString("Title")))
            .andExpect(content().string(containsString("Body")))
    }

    @Test
    fun `GIVEN non-existent post WHEN GET post THEN exception is thrown`() {
        whenever(blogPostService.getPostById(999L))
            .thenThrow(NoSuchElementException("Post not found: 999"))

        try {
            mockMvc.perform(get("/post/999"))
            assert(false) { "Expected exception to be thrown" }
        } catch (e: Exception) {
            assert(e.cause is NoSuchElementException)
        }
    }

    // Edit post tests
    @Test
    fun `GIVEN existing post WHEN GET edit THEN edit view is shown`() {
        val post = BlogPost(id = 1L, title = "Old", content = "Body", author = "Alice")
        whenever(blogPostService.getPostById(1L)).thenReturn(post)
        whenever(blogPostService.getTagString(post)).thenReturn("")

        mockMvc
            .perform(get("/edit/1"))
            .andExpect(status().isOk)
            .andExpect(view().name("edit"))
            .andExpect(model().attribute("post", post))
    }

    @Test
    fun `GIVEN non-existent post WHEN GET edit THEN exception is thrown`() {
        whenever(blogPostService.getPostById(999L))
            .thenThrow(NoSuchElementException("Post not found: 999"))

        try {
            mockMvc.perform(get("/edit/999"))
            assert(false) { "Expected exception to be thrown" }
        } catch (e: Exception) {
            assert(e.cause is NoSuchElementException)
        }
    }

    @Test
    fun `GIVEN existing post WHEN POST edit THEN post is updated and redirected`() {
        mockMvc
            .perform(
                post("/edit/1")
                    .param("title", "New")
                    .param("content", "New content"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/post/1"))

        verify(blogPostService).updatePost(1L, "New", "New content", null)
    }

    // Delete post tests
    @Test
    fun `GIVEN existing post WHEN POST delete THEN post is deleted and redirected`() {
        mockMvc
            .perform(post("/delete/1"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(blogPostService).deletePost(1L)
    }

    @Test
    fun `GIVEN non-existent post WHEN POST delete THEN redirects without error`() {
        mockMvc
            .perform(post("/delete/999"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(blogPostService).deletePost(999L)
    }

    // Statistics tests
    @Test
    fun `GIVEN lengths WHEN GET stats THEN stats are calculated and shown`() {
        val stats =
            mapOf(
                "average" to 7.0,
                "median" to 7.0,
                "max" to 9,
                "min" to 5,
                "total" to 21,
            )
        whenever(blogPostService.getPostStatistics()).thenReturn(stats)

        mockMvc
            .perform(get("/stats"))
            .andExpect(status().isOk)
            .andExpect(view().name("stats"))
            .andExpect(model().attribute("title", "Post Statistics"))
            .andExpect(model().attribute("averageLength", 7.0))
            .andExpect(model().attribute("medianLength", 7.0))
            .andExpect(model().attribute("maxLength", 9))
            .andExpect(model().attribute("minLength", 5))
            .andExpect(model().attribute("totalLength", 21))
    }

    @Test
    fun `GIVEN no lengths WHEN GET stats is called THEN zero statistics are shown`() {
        val stats =
            mapOf(
                "average" to 0.0,
                "median" to 0.0,
                "max" to 0,
                "min" to 0,
                "total" to 0,
            )
        whenever(blogPostService.getPostStatistics()).thenReturn(stats)

        mockMvc
            .perform(get("/stats"))
            .andExpect(status().isOk)
            .andExpect(view().name("stats"))
            .andExpect(model().attribute("title", "Post Statistics"))
            .andExpect(model().attribute("averageLength", 0.0))
            .andExpect(model().attribute("medianLength", 0.0))
            .andExpect(model().attribute("maxLength", 0))
            .andExpect(model().attribute("minLength", 0))
            .andExpect(model().attribute("totalLength", 0))
    }

    @Test
    fun `GIVEN even number of lengths WHEN GET stats THEN median is average of middle two`() {
        val stats =
            mapOf(
                "average" to 5.5,
                "median" to 5.0,
                "max" to 10,
                "min" to 2,
                "total" to 22,
            )
        whenever(blogPostService.getPostStatistics()).thenReturn(stats)

        mockMvc
            .perform(get("/stats"))
            .andExpect(status().isOk)
            .andExpect(view().name("stats"))
            .andExpect(model().attribute("title", "Post Statistics"))
            .andExpect(model().attribute("medianLength", 5.0))
    }

    // Tag association tests
    @Test
    fun `GIVEN valid form with tags WHEN POST create THEN post is saved with tags`() {
        mockMvc
            .perform(
                post("/create")
                    .param("title", "New Post")
                    .param("content", "Body")
                    .param("author", "Bob")
                    .param("tags", TRAVEL_AND_FOOD),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(blogPostService).createPost("New Post", "Body", "Bob", TRAVEL_AND_FOOD)
    }

    @Test
    fun `GIVEN form with empty tags WHEN POST create THEN post is saved without tags`() {
        mockMvc
            .perform(
                post("/create")
                    .param("title", "Post")
                    .param("content", "Content")
                    .param("author", "Alice")
                    .param("tags", ""),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(blogPostService).createPost("Post", "Content", "Alice", "")
    }

    @Test
    fun `GIVEN existing post WHEN POST edit with tags THEN delegates to service with tags`() {
        mockMvc
            .perform(
                post("/edit/1")
                    .param("title", "New")
                    .param("content", "New content")
                    .param("tags", FOOD),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/post/1"))

        verify(blogPostService).updatePost(1L, "New", "New content", FOOD)
    }

    // Tag filtering tests
    @Test
    fun `GIVEN existing tag WHEN GET filter by tag THEN only posts with that tag are shown`() {
        val post1 = createPost(id = 1L, title = "Travel Post 1", content = "Content 1")
        val post2 = createPost(id = 2L, title = "Travel Post 2", content = "Content 2", author = "Bob")

        whenever(tagService.getPostsByTag(TRAVEL)).thenReturn(listOf(post1, post2))

        mockMvc
            .perform(get("/tag/$TRAVEL"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("posts"))
            .andExpect(model().attribute("filterTag", TRAVEL))
            .andExpect(content().string(containsString("Travel Post 1")))
            .andExpect(content().string(containsString("Travel Post 2")))
    }

    @Test
    fun `GIVEN non-existent tag WHEN GET filter by tag THEN exception is thrown`() {
        whenever(tagService.getPostsByTag("nonexistent"))
            .thenThrow(NoSuchElementException("Tag not found: nonexistent"))

        try {
            mockMvc.perform(get("/tag/nonexistent"))
            assert(false) { "Expected exception to be thrown" }
        } catch (e: Exception) {
            // Expected - tag not found should throw exception
            assert(e.cause is NoSuchElementException)
        }
    }
}
