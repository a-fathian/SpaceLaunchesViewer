package ali.fathian.domain.common

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val error: NetworkError) : Resource<Nothing>()
}
