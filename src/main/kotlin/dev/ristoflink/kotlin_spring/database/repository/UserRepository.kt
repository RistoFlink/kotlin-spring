package dev.ristoflink.kotlin_spring.database.repository

import dev.ristoflink.kotlin_spring.database.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository: MongoRepository<User, ObjectId> {
    fun findByEmail(email: String): User?
}