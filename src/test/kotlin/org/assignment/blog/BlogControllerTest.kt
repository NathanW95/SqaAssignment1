package org.assignment.blog

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
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
import java.util.Optional

@WebMvcTest(BlogController::class)
class BlogControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var repository: BlogPostRepository

    @Test
    fun `GIVEN existing posts WHEN GET index THEN list is shown`() {
        val post =
            BlogPost(
                id = 1L,
                title = "Hello",
                content = "Content",
                author = "Alice",
            )
        whenever(repository.findAll()).thenReturn(listOf(post))

        mockMvc
            .perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("posts"))
            .andExpect(content().string(containsString("Hello")))
    }

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
                    .param("content", "Body")
                    .param("author", "Bob"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).save(
            argThat { title == "New Post" && content == "Body" && author == "Bob" },
        )
    }

    @Test
    fun `GIVEN existing post WHEN GET post THEN post page is shown`() {
        val post =
            BlogPost(
                id = 1L,
                title = "Title",
                content = "Body",
                author = "Alice",
            )
        whenever(repository.findById(1L)).thenReturn(Optional.of(post))

        mockMvc
            .perform(get("/post/1"))
            .andExpect(status().isOk)
            .andExpect(view().name("post"))
            .andExpect(model().attribute("post", post))
            .andExpect(content().string(containsString("Title")))
            .andExpect(content().string(containsString("Body")))
    }

    @Test
    fun `GIVEN existing post WHEN GET edit THEN edit view is shown`() {
        val post = BlogPost(id = 1L, title = "Old", content = "Body", author = "Alice")
        whenever(repository.findById(1L)).thenReturn(Optional.of(post))

        mockMvc
            .perform(get("/edit/1"))
            .andExpect(status().isOk)
            .andExpect(view().name("edit"))
            .andExpect(model().attribute("post", post))
    }

    @Test
    fun `GIVEN existing post WHEN POST edit THEN post is updated and redirected`() {
        val post = BlogPost(id = 1L, title = "Old", content = "Old content", author = "Alice")
        whenever(repository.findById(1L)).thenReturn(Optional.of(post))

        mockMvc
            .perform(
                post("/edit/1")
                    .param("title", "New")
                    .param("content", "New content"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/post/1"))

        verify(repository).save(
            argThat { id == 1L && title == "New" && content == "New content" },
        )
    }

    @Test
    fun `GIVEN existing post WHEN POST delete THEN post is deleted and redirected`() {
        whenever(repository.existsById(1L)).thenReturn(true)

        mockMvc
            .perform(post("/delete/1"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).deleteById(1L)
    }

    @Test
    fun `GIVEN lengths WHEN GET stats THEN stats are calculated and shown`() {
        whenever(repository.getPostLengths()).thenReturn(listOf(5, 7, 9))

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
        whenever(repository.getPostLengths()).thenReturn(emptyList())

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
        whenever(repository.getPostLengths()).thenReturn(listOf(10, 2, 6, 4))

        mockMvc
            .perform(get("/stats"))
            .andExpect(status().isOk)
            .andExpect(view().name("stats"))
            .andExpect(model().attribute("title", "Post Statistics"))
            .andExpect(model().attribute("medianLength", 5.0))
    }
}
