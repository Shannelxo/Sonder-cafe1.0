package com.sondercafe.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sondercafe.app.domain.model.Role
import java.time.Instant

@Entity(
    tableName = "users",
    // Unique index: the database itself rejects a second account with the same email.
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "password_salt") val passwordSalt: String,
    val role: Role,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)
