package com.sondercafe.app.data.local

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room stores timestamps as epoch milliseconds. Enums don't need converters here; Room stores
 * them as their name, which keeps the database readable.
 */
class Converters {

    @TypeConverter
    fun instantToMillis(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun millisToInstant(millis: Long?): Instant? = millis?.let(Instant::ofEpochMilli)
}
