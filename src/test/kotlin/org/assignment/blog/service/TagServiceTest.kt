package org.assignment.blog.service

import org.assignment.blog.model.BlogPost
import org.assignment.blog.model.Tag
import org.assignment.blog.repository.TagRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.Optional

@SpringBootTest
class TagServiceTest {
    @Autowired
    lateinit var tagService: TagService

    @MockitoBean
    lateinit var tagRepository: TagRepository

    companion object {
        const val FOOD = "food"
        const val TRAVEL = "travel"

        fun createTag(
            id: Long = 1L,
            name: String = TRAVEL,
        ) = Tag(id = id, name = name)

        fun createPost(
            id: Long = 1L,
            title: String = "Test Post",
            content: String = "Content",
            author: String = "Alice",
        ) = BlogPost(id = id, title = title, content = content, author = author)
    }

    // parseTagNames tests
    @Test
    fun `GIVEN comma-separated tags WHEN parseTagNames THEN returns list of lowercase tags`() {
        val result = tagService.parseTagNames("Kotlin, Spring, Java")

        Assertions.assertEquals(3, result.size)
        Assertions.assertEquals(listOf("kotlin", "spring", "java"), result)
    }

    @Test
    fun `GIVEN mixed case tags WHEN parseTagNames THEN normalizes to lowercase`() {
        val result = tagService.parseTagNames("KoTlIn, SPRING")

        Assertions.assertEquals(listOf("kotlin", "spring"), result)
    }

    @Test
    fun `GIVEN tags with extra whitespace WHEN parseTagNames THEN trims whitespace`() {
        val result = tagService.parseTagNames("  kotlin  ,   spring   ")

        Assertions.assertEquals(listOf("kotlin", "spring"), result)
    }

    @Test
    fun `GIVEN empty string WHEN parseTagNames THEN returns empty list`() {
        val result = tagService.parseTagNames("")

        Assertions.assertTrue(result.isEmpty())
    }

    @Test
    fun `GIVEN tags with empty entries WHEN parseTagNames THEN filters out empty strings`() {
        val result = tagService.parseTagNames("kotlin, , spring, ,")

        Assertions.assertEquals(listOf("kotlin", "spring"), result)
    }

    @Test
    fun `GIVEN multiple tags WHEN parseTagNames THEN returns tags in list`() {
        val result = tagService.parseTagNames("food, travel, food")

        Assertions.assertEquals(listOf("food", "travel", "food"), result)
        Assertions.assertEquals(3, result.size)
    }

    @Test
    fun `GIVEN duplicate tags with different cases WHEN parseTagNames THEN normalizes and returns duplicates`() {
        val result = tagService.parseTagNames("Food, FOOD, food")

        Assertions.assertEquals(listOf("food", "food", "food"), result)
        Assertions.assertEquals(3, result.size)
    }

    // findOrCreateTag tests
    @Test
    fun `GIVEN existing tag WHEN findOrCreateTag THEN returns existing tag`() {
        val existingTag = createTag(id = 1L, name = FOOD)
        whenever(tagRepository.findByName(FOOD)).thenReturn(Optional.of(existingTag))

        val result = tagService.findOrCreateTag(FOOD)

        Assertions.assertEquals(existingTag, result)
        verify(tagRepository).findByName(FOOD)
    }

    @Test
    fun `GIVEN non-existent tag WHEN findOrCreateTag THEN creates and returns new tag`() {
        val newTag = createTag(id = 2L, name = TRAVEL)
        whenever(tagRepository.findByName(TRAVEL)).thenReturn(Optional.empty())
        whenever(tagRepository.save(any<Tag>())).thenReturn(newTag)

        val result = tagService.findOrCreateTag(TRAVEL)

        Assertions.assertEquals(newTag, result)
        verify(tagRepository).findByName(TRAVEL)
        verify(tagRepository).save(any<Tag>())
    }

    // processAndAssociateTags tests
    @Test
    fun `GIVEN null tags string WHEN processAndAssociateTags THEN no tags added`() {
        val post = createPost()

        tagService.processAndAssociateTags(post, null)

        Assertions.assertTrue(post.tags.isEmpty())
    }

    @Test
    fun `GIVEN blank tags string WHEN processAndAssociateTags THEN no tags added`() {
        val post = createPost()

        tagService.processAndAssociateTags(post, "   ")

        Assertions.assertTrue(post.tags.isEmpty())
    }

    @Test
    fun `GIVEN valid tags string WHEN processAndAssociateTags THEN tags are associated with post`() {
        val post = createPost()
        val foodTag = createTag(id = 1L, name = FOOD)
        val travelTag = createTag(id = 2L, name = TRAVEL)

        whenever(tagRepository.findByName(FOOD)).thenReturn(Optional.of(foodTag))
        whenever(tagRepository.findByName(TRAVEL)).thenReturn(Optional.of(travelTag))

        tagService.processAndAssociateTags(post, "Food, Travel")

        Assertions.assertEquals(2, post.tags.size)
        Assertions.assertTrue(post.tags.contains(foodTag))
        Assertions.assertTrue(post.tags.contains(travelTag))
    }

    @Test
    fun `GIVEN duplicate tags WHEN processAndAssociateTags THEN only unique tags are added`() {
        val post = createPost()
        val foodTag = createTag(id = 1L, name = FOOD)

        whenever(tagRepository.findByName(FOOD)).thenReturn(Optional.of(foodTag))

        tagService.processAndAssociateTags(post, "food, FOOD, Food")

        Assertions.assertEquals(1, post.tags.size)
        Assertions.assertTrue(post.tags.contains(foodTag))
        verify(tagRepository, times(3)).findByName(FOOD)
    }

    // getPostsByTag tests
    @Test
    fun `GIVEN existing tag WHEN getPostsByTag THEN returns posts with that tag`() {
        val tag = createTag(name = FOOD)
        val post1 = createPost(id = 1L, title = "Post 1")
        val post2 = createPost(id = 2L, title = "Post 2")
        tag.posts.add(post1)
        tag.posts.add(post2)

        whenever(tagRepository.findByName(FOOD)).thenReturn(Optional.of(tag))

        val result = tagService.getPostsByTag(FOOD)

        Assertions.assertEquals(2, result.size)
        Assertions.assertTrue(result.contains(post1))
        Assertions.assertTrue(result.contains(post2))
    }

    @Test
    fun `GIVEN non-existent tag WHEN getPostsByTag THEN throws NoSuchElementException`() {
        whenever(tagRepository.findByName("nonexistent")).thenReturn(Optional.empty())

        val exception =
            Assertions.assertThrows(NoSuchElementException::class.java) {
                tagService.getPostsByTag("nonexistent")
            }

        Assertions.assertEquals("Tag not found: nonexistent", exception.message)
    }
}
