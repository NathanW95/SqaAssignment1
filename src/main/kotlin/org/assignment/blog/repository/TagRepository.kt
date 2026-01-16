package org.assignment.blog.repository

import org.assignment.blog.model.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TagRepository : JpaRepository<Tag, Long> {
    fun findByName(name: String): Optional<Tag>
}
