package com.sondercafe.app.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sondercafe.app.ui.components.MessageBanner

@Composable
fun AccountScreen(
    onOpenAdminConsole: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AccountContent(
        state = state,
        onOpenAdminConsole = onOpenAdminConsole,
        onLogout = viewModel::logout,
        onRequestDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountContent(
    state: AccountUiState,
    onOpenAdminConsole: () -> Unit,
    onLogout: () -> Unit,
    onRequestDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Account") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            state.user?.let { user ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(user.displayName, style = MaterialTheme.typography.titleLarge)
                        Text(user.email, style = MaterialTheme.typography.bodyMedium)
                        if (user.isAdmin) {
                            Text("Café staff", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (user.isAdmin) {
                    Button(onClick = onOpenAdminConsole, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("Open admin console")
                    }
                }
            }

            state.errorMessage?.let { MessageBanner(message = it) }

            OutlinedButton(
                onClick = onLogout,
                enabled = !state.isWorking,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Log out")
            }

            HorizontalDivider()

            Text("Your data", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Your account, cart, orders and feedback are stored only on this phone. " +
                    "Deleting your account removes all of them and can't be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onRequestDelete,
                enabled = !state.isWorking,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete my account")
            }
        }
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete your account?") },
            text = { Text("This permanently deletes your account, cart, order history and feedback.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) { Text("Cancel") }
            }
        )
    }
}
