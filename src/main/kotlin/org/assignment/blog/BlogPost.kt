package org.assignment.blog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Table(name = "blog_post")
class BlogPost(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(length = 100, nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(length = 100, nullable = false)
    var author: String = "",
) {
    override fun toString(): String = "\"$title\" by $author (${createdAt.toLocalDate()})"
}

interface BlogPostRepository : JpaRepository<BlogPost, Long> {

    // Equivalent of BlogPost.get_post_lengths() using JPQL over the BlogPost entity
    @Query("SELECT LENGTH(b.title) + LENGTH(b.content) FROM BlogPost b")
    fun getPostLengths(): List<Int>
}
