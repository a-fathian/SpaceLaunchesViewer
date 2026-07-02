package ali.fathian.domain.common

sealed class NetworkError {
    data class Http(val code: Int) : NetworkError()
    object NoConnection : NetworkError()
    object Timeout : NetworkError()
    data class Unknown(val throwable: Throwable) : NetworkError()
}
