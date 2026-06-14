package ali.fathian.data.repository

import ali.fathian.data.local.LaunchDao
import ali.fathian.data.remote.api.ApiService
import ali.fathian.data.remote.dto.mapper.toDomainLaunchModel
import ali.fathian.data.remote.dto.mapper.toDomainModel
import ali.fathian.data.remote.dto.mapper.toLaunchEntity
import ali.fathian.domain.common.Resource
import ali.fathian.domain.model.DomainLaunchModel
import ali.fathian.domain.repository.LaunchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultLaunchRepository @Inject constructor(
    private val apiService: ApiService,
    private val launchDao: LaunchDao
) : LaunchRepository {

    override suspend fun getAllLaunches(): Resource<List<DomainLaunchModel>> {
        return try {
            val response = apiService.getAllLaunches()
            if (response.isSuccessful) {
                val launches = response.body()?.map { it.toDomainLaunchModel() }.orEmpty()
                Resource.Success(launches)
            } else {
                // Map HTTP error codes to meaningful messages
                val message = when (response.code()) {
                    400 -> "Bad Request — something went wrong in the request."
                    401 -> "Unauthorized — please check your credentials."
                    403 -> "Forbidden — access denied."
                    404 -> "Launch data not found."
                    500 -> "Server Error — please try again later."
                    else -> response.message().ifEmpty { "Unexpected response (${response.code()})" }
                }
                Resource.Error(message)
            }
        } catch (e: retrofit2.HttpException) {
            val message = when (e.code()) {
                404 -> "Launch data not found."
                500 -> "Server unreachable — please try again later."
                else -> "HTTP error (${e.code()}) — ${e.message()}"
            }
            Resource.Error(message)
        } catch (e: java.net.UnknownHostException) {
            Resource.Error("No internet connection — please check your network.")
        } catch (e: java.net.SocketTimeoutException) {
            Resource.Error("Request timed out — try again later.")
        } catch (e: com.google.gson.JsonParseException) {
            Resource.Error("Failed to parse launch data — data format may be invalid.")
        } catch (e: Exception) {
            Resource.Error("Unexpected error occurred: ${e.localizedMessage ?: "Unknown"}")
        }
    }

    override suspend fun insertLaunch(launchModel: DomainLaunchModel) {
        launchDao.insertLaunch(launchModel.toLaunchEntity())
    }

    override suspend fun deleteLaunch(launchModel: DomainLaunchModel) {
        launchDao.deleteLaunch(launchModel.toLaunchEntity())
    }

    override fun getLocalLaunches(): Flow<List<DomainLaunchModel>> {
        return launchDao.getAllLaunches().map { it.map { item -> item.toDomainModel() } }
    }
}

