package ali.fathian.data.repository

import ali.fathian.data.BaseTest
import ali.fathian.data.local.LaunchDao
import ali.fathian.data.remote.api.ApiService
import ali.fathian.data.remote.dto.Launch
import ali.fathian.data.remote.dto.mapper.toDomainLaunchModel
import ali.fathian.domain.common.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import org.mockito.kotlin.*
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultLaunchRepositoryTest : BaseTest() {

    private val apiService = mock<ApiService>()
    private val launchDao = mock<LaunchDao>()

    @Test
    fun `getAllLaunches returns success resource when response is successful`() = runTest {
        // Arrange (preparing the scenario)
        val launchResponse = getSuccessResponse()
        val apiResponse = Response.success(launchResponse)
        apiService.stub {
            onBlocking { getAllLaunches() } doReturn apiResponse
        }
        // Act
        val result = DefaultLaunchRepository(apiService, launchDao).getAllLaunches()

        // Assert
        Assert.assertTrue(result is Resource.Success)
        Assert.assertEquals(
            launchResponse.map { it.toDomainLaunchModel() },
            (result as Resource.Success).data
        )
        Mockito.verify(apiService, times(1)).getAllLaunches()
    }

    @Test
    fun `getAllLaunches returns success resource with empty list when response is successful but data is null`() =
        runTest {
            // Arrange (preparing the scenario)
            val apiResponse = Response.success(null as List<Launch>?)

            apiService.stub {
                onBlocking { getAllLaunches() } doReturn apiResponse
            }
            // Act
            val result = DefaultLaunchRepository(apiService, launchDao).getAllLaunches()

            // Assert
            Assert.assertTrue(result is Resource.Success)
            Assert.assertTrue((result as Resource.Success).data?.isEmpty() == true)

            Mockito.verify(apiService, times(1)).getAllLaunches()
        }

    @Test
    fun `getAllLaunches returns error resource when response is unsuccessful`() = runTest {
        // Arrange (preparing the scenario)
        val errorMessage = "Check your internet connection"

        apiService.stub {
            onBlocking { getAllLaunches() } doThrow MockitoException(errorMessage)
        }
        // Act
        val result = DefaultLaunchRepository(apiService, launchDao).getAllLaunches()

        // Assert
        Assert.assertTrue(result is Resource.Error)
        Assert.assertEquals("Unexpected error occurred: $errorMessage", (result as Resource.Error).message)
        Mockito.verify(apiService, times(1)).getAllLaunches()
    }

    private fun getSuccessResponse(): List<Launch> {
        return listOf(
            Launch(
                fairings = null,
                links = null,
                staticFireDateUtc = null,
                staticFireDateUnix = null,
                tdb = null,
                net = null,
                window = null,
                rocket = null,
                success = null,
                details = null,
                crew = null,
                ships = null,
                capsules = null,
                payloads = null,
                launchpad = null,
                autoUpdate = null,
                flightNumber = null,
                name = null,
                dateUtc = null,
                dateUnix = null,
                dateLocal = null,
                datePrecision = null,
                upcoming = null,
                cores = null,
                id = "123456789"
            )
        )
    }
}