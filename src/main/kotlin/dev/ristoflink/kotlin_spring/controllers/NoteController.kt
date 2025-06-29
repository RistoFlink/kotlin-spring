package dev.ristoflink.kotlin_spring.controllers

import dev.ristoflink.kotlin_spring.controllers.NoteController.NoteResponse
import dev.ristoflink.kotlin_spring.database.model.Note
import dev.ristoflink.kotlin_spring.database.repository.NoteRepository
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val repository: NoteRepository
) {
    data class NoteRequest(
        val id: String?,
        val title: String,
        val content: String,
        val color: Long,
        val ownerId: ObjectId,
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant,
    )

    @PostMapping
    fun save(body: NoteRequest): NoteResponse {
        val note = repository.save(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = body.ownerId,
            )
        )
        return note.toResponse()
    }

    @GetMapping
    fun findByOwnerId(@RequestParam(required = true) ownerId: String): List<NoteResponse> {
        return repository.findByOwnerId(ObjectId(ownerId)).map {
                it.toResponse()
        };
    }
}

private fun Note.toResponse(): NoteController.NoteResponse {
        return NoteResponse(
                id = id!!.toHexString(),
                title = title,
                content = content,
                color = color,
                createdAt = createdAt,
        )
}