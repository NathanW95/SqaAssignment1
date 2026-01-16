package org.assignment.blog.service

import org.assignment.blog.model.BlogPost
import org.assignment.blog.repository.BlogPostRepository
import org.springframework.stereotype.Service

@Service
class BlogPostService(
    private val repository: BlogPostRepository,
    private val tagService: TagService,
) {
    fun getAllPosts(): List<BlogPost> = repository.findAll()

    fun getPostById(postId: Long): BlogPost =
        repository.findById(postId).orElseThrow {
            NoSuchElementException("Post not found: $postId")
        }

    fun createPost(
        title: String,
        content: String,
        author: String,
        tags: String?,
    ): BlogPost {
        val post = BlogPost(title = title, content = content, author = author)
        tagService.processAndAssociateTags(post, tags)
        return repository.save(post)
    }

    fun updatePost(
        postId: Long,
        title: String,
        content: String,
        tags: String?,
    ): BlogPost {
        val post = getPostById(postId)
        post.title = title
        post.content = content
        post.tags.clear()
        tagService.processAndAssociateTags(post, tags)
        return repository.save(post)
    }

    fun deletePost(postId: Long) {
        if (repository.existsById(postId)) {
            repository.deleteById(postId)
        }
    }

    fun getPostStatistics(): Map<String, Any> {
        val lengths = repository.getPostLengths()

        if (lengths.isEmpty()) {
            return mapOf(
                "average" to 0.0,
                "median" to 0.0,
                "max" to 0,
                "min" to 0,
                "total" to 0,
            )
        }

        val sorted = lengths.sorted()
        val average = lengths.average()
        val median =
            if (sorted.size % 2 == 1) {
                sorted[sorted.size / 2].toDouble()
            } else {
                (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
            }

        return mapOf(
            "average" to average,
            "median" to median,
            "max" to sorted.last(),
            "min" to sorted.first(),
            "total" to lengths.sum(),
        )
    }

    fun getTagString(post: BlogPost): String = post.tags.joinToString(", ") { it.name }
}
