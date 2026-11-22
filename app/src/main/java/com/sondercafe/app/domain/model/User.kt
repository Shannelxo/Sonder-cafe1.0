package com.sondercafe.app.domain.model

enum class Role { CUSTOMER, ADMIN }

/** A signed-in user. Password data never leaves the data layer, so it isn't part of this model. */
data class User(
    val id: Long,
    val email: String,
    val displayName: String,
    val role: Role
) {
    val isAdmin: Boolean get() = role == Role.ADMIN
}
