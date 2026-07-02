package ali.fathian.presentation.launch

import ali.fathian.domain.common.NetworkError
import ali.fathian.domain.common.Resource
import ali.fathian.domain.model.DomainLaunchModel
import ali.fathian.domain.use_cases.BookmarksUseCase
import ali.fathian.domain.use_cases.GetAllLaunchesUseCase
import ali.fathian.presentation.BaseTest
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchesViewModelTest : BaseTest() {

    private lateinit var viewModel: LaunchesViewModel

    private val getAllLaunchesUseCase = mock<GetAllLaunchesUseCase>()
    private val bookmarksUseCase = mock<BookmarksUseCase>().stub {
        onBlocking { getLocalLaunches() } doReturn flowOf(emptyList())
    }

    @Before
    fun setUp() {
        viewModel = LaunchesViewModel(getAllLaunchesUseCase, bookmarksUseCase, mockDispatcher.io())
    }

    @Test
    fun `fetchLaunches success updates uiState with launches`() = runTest {
        val expectedLaunches = getLaunchesList()
        getAllLaunchesUseCase.stub {
            onBlocking { invoke() } doReturn Resource.Success(expectedLaunches)
        }
        viewModel.fetchLaunches()
        viewModel.uiState.test {
            val launches = awaitItem()
            assertTrue(launches.errorMessage.isEmpty())
            assertEquals(1, launches.allLaunches.size)
            assertEquals(expectedLaunches[0].name, launches.allLaunches[0].name)
        }
    }

    @Test
    fun `fetchLaunches error updates uiState with error message`() = runTest {
        getAllLaunchesUseCase.stub {
            onBlocking { invoke() } doReturn Resource.Error(NetworkError.Timeout)
        }
        viewModel.fetchLaunches()
        viewModel.uiState.test {
            val launches = awaitItem()
            assertTrue(launches.errorMessage.isNotEmpty())
            assertEquals("Request timed out — try again later.", launches.errorMessage)
        }
    }

    private fun getLaunchesList(): List<DomainLaunchModel> {
        return listOf(
            DomainLaunchModel(
                name = "Falcon"
            )
        )
    }
}