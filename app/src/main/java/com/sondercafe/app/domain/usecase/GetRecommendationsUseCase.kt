package com.sondercafe.app.domain.usecase

import com.sondercafe.app.core.result.AppResult
import com.sondercafe.app.core.result.getOrNull
import com.sondercafe.app.domain.model.Recommendation
import com.sondercafe.app.domain.model.WeatherSnapshot
import com.sondercafe.app.domain.recommendation.RecommendationEngine
import com.sondercafe.app.domain.repository.FeedbackRepository
import com.sondercafe.app.domain.repository.MenuRepository
import com.sondercafe.app.domain.repository.OrderRepository
import com.sondercafe.app.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class RecommendationsResult(
    val recommendations: List<Recommendation>,
    /** Null when weather couldn't be fetched, so the screen can say recommendations ignore the weather. */
    val weather: WeatherSnapshot?
)

/** Gathers everything the recommendation engine needs and runs it. */
class GetRecommendationsUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val orderRepository: OrderRepository,
    private val feedbackRepository: FeedbackRepository,
    private val weatherRepository: WeatherRepository,
    private val engine: RecommendationEngine
) {

    suspend operator fun invoke(userId: Long): AppResult<RecommendationsResult> = try {
        coroutineScope {
            // Independent lookups run in parallel; the network call is usually the slowest.
            val weather = async { weatherRepository.getCurrentWeather().getOrNull() }
            val items = async { menuRepository.getAvailableItems() }
            val personal = async { orderRepository.getItemCountsForUser(userId) }
            val popular = async { orderRepository.getItemCountsForAllUsers() }
            val ratings = async { feedbackRepository.getAverageRatings() }

            val currentWeather = weather.await()
            val recommendations = engine.recommend(
                items = items.await(),
                personalCounts = personal.await(),
                popularityCounts = popular.await(),
                averageRatings = ratings.await(),
                weather = currentWeather
            )
            AppResult.Success(RecommendationsResult(recommendations, currentWeather))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error("Couldn't load recommendations right now", e)
    }
}
