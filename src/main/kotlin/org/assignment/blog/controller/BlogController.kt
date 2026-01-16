package org.assignment.blog.controller

import org.assignment.blog.service.BlogPostService
import org.assignment.blog.service.TagService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class BlogController(
    private val blogPostService: BlogPostService,
    private val tagService: TagService,
) {
    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("title", "Blog Posts")
        model.addAttribute("posts", blogPostService.getAllPosts())
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
        @RequestParam(required = false) tags: String?,
    ): String {
        blogPostService.createPost(title, content, author, tags)
        return "redirect:/"
    }

    @GetMapping("/post/{postId}")
    fun post(
        @PathVariable("postId") postId: Long,
        model: Model,
    ): String {
        val post = blogPostService.getPostById(postId)
        model.addAttribute("title", post.title)
        model.addAttribute("post", post)
        return "post"
    }

    @GetMapping("/edit/{postId}")
    fun editPage(
        @PathVariable("postId") postId: Long,
        model: Model,
    ): String {
        val post = blogPostService.getPostById(postId)
        val tagString = blogPostService.getTagString(post)
        model.addAttribute("title", "Edit Post")
        model.addAttribute("post", post)
        model.addAttribute("tagString", tagString)
        return "edit"
    }

    @PostMapping("/edit/{postId}")
    fun editAction(
        @PathVariable("postId") postId: Long,
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam(required = false) tags: String?,
    ): String {
        blogPostService.updatePost(postId, title, content, tags)
        return "redirect:/post/$postId"
    }

    @PostMapping("/delete/{postId}")
    fun deleteAction(
        @PathVariable("postId") postId: Long,
    ): String {
        blogPostService.deletePost(postId)
        return "redirect:/"
    }

    @GetMapping("/tag/{tagName}")
    fun filterByTag(
        @PathVariable tagName: String,
        model: Model,
    ): String {
        val posts = tagService.getPostsByTag(tagName)
        model.addAttribute("title", "Posts tagged with: $tagName")
        model.addAttribute("posts", posts)
        model.addAttribute("filterTag", tagName)
        return "index"
    }

    @GetMapping("/stats")
    fun stats(model: Model): String {
        val statistics = blogPostService.getPostStatistics()
        val tagStatistics = tagService.getTagStatistics()
        model.addAttribute("title", "Post Statistics")
        model.addAttribute("averageLength", String.format("%.2f", statistics["average"]).toDouble())
        model.addAttribute("medianLength", String.format("%.2f", statistics["median"]).toDouble())
        model.addAttribute("maxLength", statistics["max"])
        model.addAttribute("minLength", statistics["min"])
        model.addAttribute("totalLength", statistics["total"])
        model.addAttribute("tagStatistics", tagStatistics)
        return "stats"
    }
}
