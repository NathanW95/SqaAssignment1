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

    @MockitoBean
    lateinit var tagRepository: TagRepository

    // Index/List view tests
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
                    .param("content", "Body")
                    .param("author", "Bob"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).save(
            argThat { title == "New Post" && content == "Body" && author == "Bob" },
        )
    }

    // View single post tests
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

    // Edit post tests
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

    // Delete post tests
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
    fun `GIVEN non-existent post WHEN POST delete THEN redirects without error`() {
        whenever(repository.existsById(999L)).thenReturn(false)

        mockMvc
            .perform(post("/delete/999"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).existsById(999L)
        verify(repository, org.mockito.kotlin.never()).deleteById(999L)
    }

    // Statistics tests
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

    // Tag association tests
    @Test
    fun `GIVEN valid form with tags WHEN POST create THEN post is saved with tags`() {
        val tag1 = Tag(id = 1L, name = "kotlin")
        val tag2 = Tag(id = 2L, name = "spring")

        whenever(tagRepository.findByName("kotlin")).thenReturn(Optional.of(tag1))
        whenever(tagRepository.findByName("spring")).thenReturn(Optional.of(tag2))

        mockMvc
            .perform(
                post("/create")
                    .param("title", "New Post")
                    .param("content", "Body")
                    .param("author", "Bob")
                    .param("tags", "Kotlin, Spring"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).save(
            argThat {
                title == "New Post" &&
                    content == "Body" &&
                    author == "Bob" &&
                    tags.size == 2 &&
                    tags.any { it.name == "kotlin" } &&
                    tags.any { it.name == "spring" }
            },
        )
    }

    @Test
    fun `GIVEN form with new tags WHEN POST create THEN new tags are created`() {
        val newTag = Tag(id = 1L, name = "travel")
        whenever(tagRepository.findByName("travel")).thenReturn(Optional.empty())
        whenever(tagRepository.save(argThat { name == "travel" })).thenReturn(newTag)

        mockMvc
            .perform(
                post("/create")
                    .param("title", "Post")
                    .param("content", "Content")
                    .param("author", "Alice")
                    .param("tags", "Travel"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).save(
            argThat {
                tags.size == 1 &&
                    tags.first().name == "travel"
            },
        )
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

        verify(repository).save(
            argThat { tags.isEmpty() },
        )
    }

    @Test
    fun `GIVEN existing post WHEN POST edit with tags THEN tags are updated`() {
        val post = BlogPost(id = 1L, title = "Old", content = "Old content", author = "Alice")
        val tag = Tag(id = 1L, name = "food")

        whenever(repository.findById(1L)).thenReturn(Optional.of(post))
        whenever(tagRepository.findByName("food")).thenReturn(Optional.of(tag))

        mockMvc
            .perform(
                post("/edit/1")
                    .param("title", "New")
                    .param("content", "New content")
                    .param("tags", "Food"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/post/1"))

        verify(repository).save(
            argThat {
                id == 1L &&
                    title == "New" &&
                    content == "New content" &&
                    tags.size == 1 &&
                    tags.first().name == "food"
            },
        )
    }

    @Test
    fun `GIVEN existing post WHEN POST edit with new tags THEN new tags are created`() {
        val post = BlogPost(id = 1L, title = "Old", content = "Old content", author = "Alice")
        val newTag = Tag(id = 2L, name = "travel")

        whenever(repository.findById(1L)).thenReturn(Optional.of(post))
        whenever(tagRepository.findByName("travel")).thenReturn(Optional.empty())
        whenever(tagRepository.save(argThat { name == "travel" })).thenReturn(newTag)

        mockMvc
            .perform(
                post("/edit/1")
                    .param("title", "Updated Title")
                    .param("content", "Updated content")
                    .param("tags", "Travel"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/post/1"))

        verify(repository).save(
            argThat {
                id == 1L &&
                    tags.size == 1 &&
                    tags.first().name == "travel"
            },
        )
    }

    // Tag normalization tests
    @Test
    fun `GIVEN mixed case tags WHEN POST create THEN tags are normalized to lowercase`() {
        val kotlinTag = Tag(id = 1L, name = "kotlin")
        whenever(tagRepository.findByName("kotlin")).thenReturn(Optional.of(kotlinTag))

        mockMvc
            .perform(
                post("/create")
                    .param("title", "Post")
                    .param("content", "Content")
                    .param("author", "Alice")
                    .param("tags", "KoTlIn"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).save(
            argThat {
                tags.size == 1 &&
                    tags.first().name == "kotlin"
            },
        )
    }

    @Test
    fun `GIVEN duplicate tags with different cases WHEN POST create THEN only one tag is stored`() {
        val foodTag = Tag(id = 1L, name = "food")
        whenever(tagRepository.findByName("food")).thenReturn(Optional.of(foodTag))

        mockMvc
            .perform(
                post("/create")
                    .param("title", "Post")
                    .param("content", "Content")
                    .param("author", "Alice")
                    .param("tags", "Food, FOOD, food"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(repository).save(
            argThat {
                tags.size == 1 &&
                    tags.first().name == "food"
            },
        )
    }

    @Test
    fun `GIVEN uppercase tag WHEN POST create THEN tag is stored as lowercase`() {
        val travelTag = Tag(id = 1L, name = "travel")
        whenever(tagRepository.findByName("travel")).thenReturn(Optional.empty())
        whenever(tagRepository.save(argThat { name == "travel" })).thenReturn(travelTag)

        mockMvc
            .perform(
                post("/create")
                    .param("title", "Post")
                    .param("content", "Content")
                    .param("author", "Alice")
                    .param("tags", "TRAVEL"),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/"))

        verify(tagRepository).findByName("travel")
        verify(tagRepository).save(argThat { name == "travel" })
    }
}
