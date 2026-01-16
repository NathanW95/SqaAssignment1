package org.assignment.blog.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "tag")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @NotBlank(message = "Tag name cannot be blank")
    @Size(min = 1, max = 25, message = "Tag name must be between 1 and 50 characters")
    @Column(nullable = false, unique = true, length = 25)
    var name: String = "",
    @ManyToMany(mappedBy = "tags")
    var posts: MutableSet<BlogPost> = mutableSetOf(),
) {
    override fun toString(): String = name
}
