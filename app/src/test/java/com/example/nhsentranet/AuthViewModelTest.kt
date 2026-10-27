package com.example.nhsentranet

import com.example.nhsentranet.data.repository.NewtonRepository
import com.example.nhsentranet.network.ApiService
import com.example.nhsentranet.network.UserSessionDto
import com.example.nhsentranet.ui.viewmodel.AuthViewModel
import com.example.nhsentranet.ui.viewmodel.ScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.mock

class FakeNewtonRepository : NewtonRepository(mock(ApiService::class.java)) {
    var loginResult: Result<UserSessionDto> = Result.failure(Exception("Not set"))

    override suspend fun login(username: String, pass: String): Result<UserSessionDto> {
        return loginResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeNewtonRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeNewtonRepository()
        viewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_whenValidInput_triggersLoadingAndSuccess() = runTest {
        val dummySession = UserSessionDto(3, "john.johnson", "John", "Student", "dummy-jwt", emptyList())
        fakeRepository.loginResult = Result.success(dummySession)

        viewModel.username = "john.johnson"
        viewModel.password = "Student@123"
        viewModel.login()

        assertTrue(viewModel.authState.value is ScreenState.Success)
        assertEquals(dummySession, viewModel.session.value)
    }

    @Test
    fun login_withInvalidInput_throwsValidationFailure() = runTest {
        viewModel.username = ""
        viewModel.password = "123" // Too short
        viewModel.login()

        assertTrue(viewModel.authState.value is ScreenState.Failure)
        val failureState = viewModel.authState.value as ScreenState.Failure
        assertTrue(failureState.error.contains("password must be at least 8 characters"))
    }
}
