package com.sondercafe.app.domain.recommendation

import com.sondercafe.app.domain.model.MenuItem
import com.sondercafe.app.domain.model.Recommendation
import com.sondercafe.app.domain.model.ServingTemperature
import com.sondercafe.app.domain.model.WeatherSnapshot
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Ranks available menu items for a customer by combining four signals, each scaled to 0..1:
 *
 * - personal: how often this customer has ordered the item, relative to their most-ordered item
 * - popularity: how often all customers order it, relative to the most popular item
 * - weather: how well the item suits the current weather (hot drinks when cold or wet, cold drinks when warm)
 * - rating: the item's average rating (items with no ratings get a neutral score)
 *
 * The weighted sum gives the score. The signal that contributed most becomes the reason shown
 * to the customer. Everything here is plain Kotlin, so it can be tested without Android.
 */
class RecommendationEngine @Inject constructor() {

    fun recommend(
        items: List<MenuItem>,
        personalCounts: Map<Long, Int>,
        popularityCounts: Map<Long, Int>,
        averageRatings: Map<Long, Double>,
        weather: WeatherSnapshot?,
        limit: Int = DEFAULT_LIMIT
    ): List<Recommendation> {
        val maxPersonal = personalCounts.values.maxOrNull() ?: 0
        val maxPopularity = popularityCounts.values.maxOrNull() ?: 0

        return items
            .filter { it.isAvailable }
            .map { item ->
                val personalCount = personalCounts[item.id] ?: 0
                val popularityCount = popularityCounts[item.id] ?: 0
                val rating = averageRatings[item.id]

                val signals = listOf(
                    Signal(
                        weighted = WEIGHT_PERSONAL * ratio(personalCount, maxPersonal),
                        reason = personalReason(personalCount)
                    ),
                    Signal(
                        weighted = WEIGHT_POPULARITY * ratio(popularityCount, maxPopularity),
                        reason = if (popularityCount > 0) "Popular with other customers" else null
                    ),
                    weatherSignal(item, weather),
                    Signal(
                        weighted = WEIGHT_RATING * ratingScore(rating),
                        reason = rating?.takeIf { it >= GOOD_RATING }?.let {
                            String.format(Locale.UK, "Rated %.1f out of 5", it)
                        }
                    )
                )

                Recommendation(
                    menuItem = item,
                    score = signals.sumOf { it.weighted },
                    reason = signals
                        .filter { it.reason != null }
                        .maxByOrNull { it.weighted }
                        ?.reason
                        ?: FALLBACK_REASON
                )
            }
            .sortedWith(compareByDescending<Recommendation> { it.score }.thenBy { it.menuItem.name })
            .take(limit)
    }

    internal fun weatherFit(temperature: ServingTemperature, weather: WeatherSnapshot): Double {
        val celsius = weather.temperatureCelsius
        return when (temperature) {
            ServingTemperature.HOT -> when {
                weather.isWet || celsius <= COOL_CELSIUS -> 1.0
                celsius >= WARM_CELSIUS -> 0.1
                else -> 0.5
            }
            ServingTemperature.COLD -> when {
                celsius >= WARM_CELSIUS && !weather.isWet -> 1.0
                celsius >= WARM_CELSIUS -> 0.6
                celsius <= COOL_CELSIUS -> 0.0
                else -> 0.4
            }
            ServingTemperature.NEUTRAL -> 0.5
        }
    }

    private fun weatherSignal(item: MenuItem, weather: WeatherSnapshot?): Signal {
        // No weather data (offline, API down): this signal contributes nothing and the other three decide.
        if (weather == null) return Signal(weighted = 0.0, reason = null)

        val fit = weatherFit(item.servingTemperature, weather)
        val degrees = weather.temperatureCelsius.roundToInt()
        val reason = when {
            fit < STRONG_WEATHER_FIT -> null
            item.servingTemperature == ServingTemperature.HOT -> "Something warm for a $degrees°C ${weather.conditions} day"
            else -> "Cool down: it's $degrees°C outside"
        }
        return Signal(weighted = WEIGHT_WEATHER * fit, reason = reason)
    }

    private fun personalReason(count: Int): String? = when {
        count >= FAVOURITE_COUNT -> "One of your favourites"
        count > 0 -> "You've ordered this before"
        else -> null
    }

    private fun ratio(value: Int, max: Int): Double = if (max == 0) 0.0 else value.toDouble() / max

    // Maps a 1..5 rating onto 0..1. Unrated items sit in the middle so they aren't punished for being new.
    private fun ratingScore(rating: Double?): Double = rating?.let { (it - 1.0) / 4.0 } ?: 0.5

    private data class Signal(val weighted: Double, val reason: String?)

    companion object {
        const val DEFAULT_LIMIT = 5

        const val WEIGHT_PERSONAL = 0.40
        const val WEIGHT_POPULARITY = 0.25
        const val WEIGHT_WEATHER = 0.20
        const val WEIGHT_RATING = 0.15

        const val COOL_CELSIUS = 12.0
        const val WARM_CELSIUS = 20.0

        private const val STRONG_WEATHER_FIT = 0.8
        private const val GOOD_RATING = 4.0
        private const val FAVOURITE_COUNT = 3
        private const val FALLBACK_REASON = "Worth a try"
    }
}
