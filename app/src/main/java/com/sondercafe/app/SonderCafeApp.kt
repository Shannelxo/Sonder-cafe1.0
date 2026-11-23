package com.sondercafe.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.sondercafe.app.data.local.seed.DatabaseSeeder
import com.sondercafe.app.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SonderCafeApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    // WorkManager is set up here (instead of automatically) so Hilt can inject into workers.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            try {
                databaseSeeder.seed()
            } catch (e: Exception) {
                // The app still opens; the menu screen shows its empty state instead.
                Log.e(TAG, "Seeding the database failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "SonderCafeApp"
    }
}
