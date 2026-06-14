package ali.fathian.presentation.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class UiModel(
    val image: String = "",
    val name: String = "",
    val date: String = "",
    val time: String = "",
    val details: String = "",
    val upcoming: Boolean = false,
    val success: Boolean = false,
    val statusText: String = "",
    val statusColor: Color = Color.Transparent,
    val expanded: Boolean = false,
    val bookmarked: Boolean = false,
    val id: String = ""
)

data class Launches(
    val allLaunches: ImmutableList<UiModel> = persistentListOf(),
    val upcomingLaunches: ImmutableList<UiModel> = persistentListOf(),
    val pastLaunches: ImmutableList<UiModel> = persistentListOf(),
    val errorMessage: String = "",
    val loading: Boolean = false
)

@Stable
sealed class Origin {
    object AllLaunches: Origin()
    object UpcomingLaunches: Origin()
    object PastLaunches: Origin()
    object BookmarkLaunches: Origin()
}



