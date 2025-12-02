package org.assignment.blog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@SpringBootApplication
class SqaAssignment1BlogApplication

fun main(args: Array<String>) {
    runApplication<SqaAssignment1BlogApplication>(*args)
}

// Controller kept in the same file to mirror the single app.py file in the Flask project
@Controller
class BlogController(
    private val repository: BlogPostRepository,
) {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("title", "Blog Posts")
        model.addAttribute("posts", repository.findAll())
        return "index"
    }

    @GetMapping("/create")
    fun createPostPage(model: Model): String {
        model.addAttribute("title", "Create Post")
        return "create"
    }

    @PostMapping("/create")
    fun createPostAction(
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam author: String,
    ): String {
        val post = BlogPost(title = title, content = content, author = author)
        repository.save(post)
        return "redirect:/"
    }

    @GetMapping("/post/{postId}")
    fun post(@PathVariable("postId") postId: Long, model: Model): String {
        val post = repository.findById(postId).orElseThrow()
        model.addAttribute("title", post.title)
        model.addAttribute("post", post)
        return "post"
    }

    @GetMapping("/edit/{postId}")
    fun editPage(@PathVariable("postId") postId: Long, model: Model): String {
        val post = repository.findById(postId).orElseThrow()
        model.addAttribute("title", "Edit Post")
        model.addAttribute("post", post)
        return "edit"
    }

    @PostMapping("/edit/{postId}")
    fun editAction(
        @PathVariable("postId") postId: Long,
        @RequestParam title: String,
        @RequestParam content: String,
    ): String {
        val post = repository.findById(postId).orElseThrow()
        post.title = title
        post.content = content
        repository.save(post)
        return "redirect:/post/$postId"
    }

    @PostMapping("/delete/{postId}")
    fun deleteAction(@PathVariable("postId") postId: Long): String {
        if (repository.existsById(postId)) {
            repository.deleteById(postId)
        }
        return "redirect:/"
    }

    @GetMapping("/stats")
    fun stats(model: Model): String {
        val lengths = repository.getPostLengths()
        if (lengths.isEmpty()) {
            model.addAttribute("title", "Post Statistics")
            model.addAttribute("averageLength", 0.0)
            model.addAttribute("medianLength", 0.0)
            model.addAttribute("maxLength", 0)
            model.addAttribute("minLength", 0)
            model.addAttribute("totalLength", 0)
            return "stats"
        }

        val sorted = lengths.sorted()
        val average = lengths.average()
        val median = if (sorted.size % 2 == 1) {
            sorted[sorted.size / 2].toDouble()
        } else {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
        }
        val max = sorted.last()
        val min = sorted.first()
        val total = lengths.sum()

        model.addAttribute("title", "Post Statistics")
        model.addAttribute("averageLength", average)
        model.addAttribute("medianLength", median)
        model.addAttribute("maxLength", max)
        model.addAttribute("minLength", min)
        model.addAttribute("totalLength", total)
        return "stats"
    }
}
