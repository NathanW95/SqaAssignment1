package org.assignment.blog

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TagRepository : JpaRepository<Tag, Long> {
    fun findByName(name: String): Optional<Tag>
}
