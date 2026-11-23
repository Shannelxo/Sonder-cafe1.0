package com.sondercafe.app.ui.auth

import com.sondercafe.app.testutil.FakeAuthRepository
import com.sondercafe.app.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        authRepository.addAccount(email = "sam@example.com", password = "coffee123")
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `empty email shows a field error and does not call the repository`() {
        viewModel.onPasswordChange("coffee123")
        viewModel.submit()

        assertEquals("Enter your email address", viewModel.uiState.value.emailError)
        assertEquals(0, authRepository.loginCalls)
    }

    @Test
    fun `empty password shows a field error`() {
        viewModel.onEmailChange("sam@example.com")
        viewModel.submit()

        assertEquals("Enter your password", viewModel.uiState.value.passwordError)
        assertEquals(0, authRepository.loginCalls)
    }

    @Test
    fun `wrong password shows a generic error and stays logged out`() {
        viewModel.onEmailChange("sam@example.com")
        viewModel.onPasswordChange("wrongpass1")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertEquals("Email or password is incorrect", state.errorMessage)
        assertFalse(state.isLoggedIn)
        assertFalse(state.isLoading)
    }

    @Test
    fun `correct details log the user in and clear the password`() {
        viewModel.onEmailChange("sam@example.com")
        viewModel.onPasswordChange("coffee123")
        viewModel.submit()

        val state = viewModel.uiState.value
        assertTrue(state.isLoggedIn)
        assertNull(state.errorMessage)
        assertEquals("", state.password)
    }

    @Test
    fun `email is trimmed and lower-cased before logging in`() {
        viewModel.onEmailChange("  Sam@Example.COM ")
        viewModel.onPasswordChange("coffee123")
        viewModel.submit()

        assertTrue(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `editing a field clears the previous error`() {
        viewModel.onEmailChange("sam@example.com")
        viewModel.onPasswordChange("wrongpass1")
        viewModel.submit()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.onPasswordChange("coffee123")

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `lockout message from the repository is shown as is`() {
        authRepository.loginError = "Too many failed attempts. Try again in 30 seconds."
        viewModel.onEmailChange("sam@example.com")
        viewModel.onPasswordChange("coffee123")
        viewModel.submit()

        assertEquals("Too many failed attempts. Try again in 30 seconds.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `input longer than the limit is cut off`() {
        viewModel.onPasswordChange("a".repeat(100))

        assertEquals(64, viewModel.uiState.value.password.length)
    }
}
