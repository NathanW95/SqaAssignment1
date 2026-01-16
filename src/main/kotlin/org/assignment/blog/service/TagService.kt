package org.assignment.blog.service

import org.assignment.blog.model.BlogPost
import org.assignment.blog.model.Tag
import org.assignment.blog.repository.TagRepository
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    fun parseTagNames(tagsString: String): List<String> =
        tagsString
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

    fun findOrCreateTag(tagName: String): Tag =
        tagRepository.findByName(tagName).orElseGet {
            tagRepository.save(Tag(name = tagName))
        }

    fun processAndAssociateTags(
        post: BlogPost,
        tagsString: String?,
    ) {
        if (tagsString.isNullOrBlank()) return

        val tagNames = parseTagNames(tagsString)
        tagNames.forEach { tagName ->
            val tag = findOrCreateTag(tagName)
            post.tags.add(tag)
        }
    }

    fun getPostsByTag(tagName: String): List<BlogPost> {
        val tag =
            tagRepository.findByName(tagName).orElseThrow {
                NoSuchElementException("Tag not found: $tagName")
            }
        return tag.posts.toList()
    }
}
