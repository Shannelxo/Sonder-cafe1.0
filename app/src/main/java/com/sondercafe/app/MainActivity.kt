package com.sondercafe.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sondercafe.app.core.notifications.AndroidOrderNotifier
import com.sondercafe.app.ui.navigation.SonderCafeNavHost
import com.sondercafe.app.ui.theme.SonderCafeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Order to open because a notification was tapped. Cleared once the nav host has shown it.
    private var pendingOrderId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Only on a fresh start: after rotation the intent is the same, and the order was
        // already opened.
        if (savedInstanceState == null) {
            pendingOrderId = orderIdFrom(intent)
        }

        setContent {
            SonderCafeTheme {
                SonderCafeNavHost(
                    pendingOrderId = pendingOrderId,
                    onPendingOrderHandled = { pendingOrderId = null }
                )
            }
        }
    }

    // launchMode="singleTop": tapping a notification while the app is open arrives here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        orderIdFrom(intent)?.let { pendingOrderId = it }
    }

    private fun orderIdFrom(intent: Intent?): Long? =
        intent?.getLongExtra(AndroidOrderNotifier.EXTRA_ORDER_ID, -1L)?.takeIf { it > 0 }
}
