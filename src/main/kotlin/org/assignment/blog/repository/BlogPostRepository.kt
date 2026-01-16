package org.assignment.blog.repository

import org.assignment.blog.model.BlogPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BlogPostRepository : JpaRepository<BlogPost, Long> {
    @Query("SELECT LENGTH(b.title) + LENGTH(b.content) FROM BlogPost b")
    fun getPostLengths(): List<Int>
}
