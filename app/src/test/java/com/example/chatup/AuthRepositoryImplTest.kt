package com.example.chatup

import com.example.chatup.data.repository.AuthRepositoryImpl
import com.example.chatup.fakes.FakeAuthDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var fakeDataSource: FakeAuthDataSource
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        fakeDataSource = FakeAuthDataSource()
        repository = AuthRepositoryImpl(fakeDataSource)
    }

    @Test
    fun `register returns success when data source succeeds`() = runTest {
        fakeDataSource.registerShouldSucceed = true

        val result = repository.register("test@example.com", "password123")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `register returns failure when data source fails`() = runTest {
        fakeDataSource.registerShouldSucceed = false

        val result = repository.register("test@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Registration failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login returns success when data source succeeds`() = runTest {
        fakeDataSource.loginShouldSucceed = true

        val result = repository.login("test@example.com", "password123")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `login returns failure when data source fails`() = runTest {
        fakeDataSource.loginShouldSucceed = false

        val result = repository.login("test@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCurrentUserId delegates to data source`() {
        fakeDataSource.stubbedUserId = "uid_123"

        assertEquals("uid_123", repository.getCurrentUserId())
    }

    @Test
    fun `getCurrentUserEmail delegates to data source`() {
        fakeDataSource.stubbedUserEmail = "user@example.com"

        assertEquals("user@example.com", repository.getCurrentUserEmail())
    }
}
