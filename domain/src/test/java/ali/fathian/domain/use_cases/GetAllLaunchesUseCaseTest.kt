package ali.fathian.domain.use_cases

import ali.fathian.domain.BaseTest
import ali.fathian.domain.common.NetworkError
import ali.fathian.domain.common.Resource
import ali.fathian.domain.model.DomainLaunchModel
import ali.fathian.domain.repository.LaunchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times

@OptIn(ExperimentalCoroutinesApi::class)
class GetAllLaunchesUseCaseTest : BaseTest() {

    private val launchRepository: LaunchRepository = mock()

    @Test
    fun `invoke returns error resource when repository fetch fails`() = runTest {
        // Arrange
        val useCase = GetAllLaunchesUseCase(launchRepository)
        val networkError = NetworkError.NoConnection
        launchRepository.stub {
            onBlocking { getAllLaunches() } doReturn Resource.Error(networkError)
        }

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Resource.Error)
        assertEquals(networkError, (result as Resource.Error).error)
        verify(launchRepository).getAllLaunches()
    }

    @Test
    fun `invoke returns success resource when repository fetch is successful`() = runTest {
        // Arrange (preparing the scenario)
        val useCase = GetAllLaunchesUseCase(launchRepository)
        val mockLaunchesList = getSuccessResult()
        launchRepository.stub {
            onBlocking { getAllLaunches() } doReturn Resource.Success(mockLaunchesList)
        }

        // Act
        val result = useCase()

        // Assert
        assertTrue(result is Resource.Success)
        assertEquals(mockLaunchesList, (result as Resource.Success).data)
        verify(launchRepository, times(1)).getAllLaunches()
    }

    private fun getSuccessResult(): List<DomainLaunchModel> {
        return listOf(
            DomainLaunchModel(name = "Starlink")
        )
    }
}