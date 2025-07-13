package dev.ristoflink.kotlin_spring.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JwtService(@Value("\${jwt.secret}") private val secret: String,) {
    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))
    private val accessTokenValidityMs = 15L * 60L * 1000L
    val refreshTokenValidityMs = 30L * 24 * 60 * 60 * 1000L

    private fun generateToken(userId: String, type: String, expiry: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String): String {
        return generateToken(userId, "access", accessTokenValidityMs)
    }

    fun generateRefreshToken(userId: String): String {
        return generateToken(userId, "refresh", refreshTokenValidityMs)
    }

    private fun validateToken(token: String, expectedType: String): Boolean {
        return try {
            val claims = parseAllClaims(token)
            val tokenType = claims["type"] as? String
            tokenType == expectedType
        } catch (e: Exception) {
            when (e) {
                is ExpiredJwtException -> logger.info("Validation failed: Token expired.")
                is SecurityException -> logger.warn("Validation failed: Invalid signature.")
                is MalformedJwtException -> logger.warn("Validation failed: Malformed token.")
                else -> logger.error("An unexpected error occurred during token validation.", e)
            }
            false
        }
    }


    fun validateAccessToken(token: String): Boolean {
        return validateToken(token, "access")
    }

    fun validateRefreshToken(token: String): Boolean {
       return validateToken(token, "refresh")
    }

    fun getUserIdFromToken(token: String): String {
        // Let exceptions propagate to be handled by the controller/security filter
        return parseAllClaims(token).subject
    }

    private fun parseAllClaims(token: String): Claims {
        val rawToken = token.removePrefix("Bearer ")
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(rawToken)
            .payload
    }
}