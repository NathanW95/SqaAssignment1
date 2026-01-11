package org.assignment.blog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

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
    @ManyToMany
    @JoinTable(
        name = "blog_post_tags",
        joinColumns = [JoinColumn(name = "blog_post_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")],
    )
    var tags: MutableSet<Tag> = mutableSetOf(),
) {
    override fun toString(): String = "\"$title\" by $author (${createdAt.toLocalDate()})"
}
