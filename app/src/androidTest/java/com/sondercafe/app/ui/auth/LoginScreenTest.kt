package com.sondercafe.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sondercafe.app.ui.theme.SonderCafeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Drives the stateless LoginContent directly, so these tests don't need Hilt or a database.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        state: LoginUiState = LoginUiState(),
        onEmailChange: (String) -> Unit = {},
        onSubmit: () -> Unit = {},
        onCreateAccount: () -> Unit = {}
    ) {
        composeRule.setContent {
            SonderCafeTheme {
                LoginContent(
                    state = state,
                    onEmailChange = onEmailChange,
                    onPasswordChange = {},
                    onSubmit = onSubmit,
                    onCreateAccount = onCreateAccount
                )
            }
        }
    }

    @Test
    fun showsFieldErrorsFromState() {
        setContent(state = LoginUiState(emailError = "Enter a valid email address"))

        composeRule.onNodeWithText("Enter a valid email address").assertIsDisplayed()
    }

    @Test
    fun showsErrorBannerFromState() {
        setContent(state = LoginUiState(errorMessage = "Email or password is incorrect"))

        composeRule.onNodeWithText("Email or password is incorrect").assertIsDisplayed()
    }

    @Test
    fun typingInEmailFieldReportsTheText() {
        var email by mutableStateOf("")
        composeRule.setContent {
            SonderCafeTheme {
                LoginContent(
                    state = LoginUiState(email = email),
                    onEmailChange = { email = it },
                    onPasswordChange = {},
                    onSubmit = {},
                    onCreateAccount = {}
                )
            }
        }

        composeRule.onNodeWithText("Email").performTextInput("sam@example.com")

        assertEquals("sam@example.com", email)
    }

    @Test
    fun loginButtonSubmits() {
        var submitted = false
        setContent(onSubmit = { submitted = true })

        composeRule.onNodeWithText("Log in").performClick()

        assertTrue(submitted)
    }

    @Test
    fun buttonIsDisabledWhileLoading() {
        setContent(state = LoginUiState(isLoading = true))

        composeRule.onNodeWithText("Logging in…").assertIsNotEnabled()
    }

    @Test
    fun passwordVisibilityToggleIsLabelled() {
        setContent()

        composeRule.onNodeWithContentDescription("Show password").performClick()
        composeRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }

    @Test
    fun createAccountLinkNavigates() {
        var clicked = false
        setContent(onCreateAccount = { clicked = true })

        composeRule.onNodeWithText("New here? Create an account").performClick()

        assertTrue(clicked)
    }
}
