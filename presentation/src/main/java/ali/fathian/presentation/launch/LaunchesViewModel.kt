package ali.fathian.presentation.launch

import ali.fathian.domain.common.Resource
import ali.fathian.domain.use_cases.BookmarksUseCase
import ali.fathian.domain.use_cases.GetAllLaunchesUseCase
import ali.fathian.presentation.common.DispatcherIO
import ali.fathian.presentation.model.Launches
import ali.fathian.presentation.model.Origin
import ali.fathian.presentation.model.UiModel
import ali.fathian.presentation.model.mapper.toDomainModel
import ali.fathian.presentation.model.mapper.toUiModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchesViewModel @Inject constructor(
    private val launchUseCase: GetAllLaunchesUseCase,
    private val bookmarksUseCase: BookmarksUseCase,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(Launches())
    val uiState = _uiState.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<UiModel>?>(null)
    val bookmarks = _bookmarks.asStateFlow()

    init {
        fetchBookmarks()
        uiState.combine(bookmarks) { allLaunches, bookmarks ->
            syncWithDatabase(allLaunches.allLaunches, bookmarks ?: emptyList())
        }.shareIn(viewModelScope, started = SharingStarted.Eagerly)
    }

    private fun fetchBookmarks() {
        viewModelScope.launch(dispatcher) {
            bookmarksUseCase.getLocalLaunches().collect {
                _bookmarks.emit(it.map { item -> item.toUiModel() })
            }
        }
    }

    fun fetchLaunches() {
        viewModelScope.launch(dispatcher) {
            _uiState.emit(uiState.value.copy(loading = true))
            val launches = launchUseCase()
            if (launches is Resource.Success) {
                launches.data?.let {
                    _uiState.emit(
                        Launches(
                            allLaunches = it.map { item -> item.toUiModel() }.toImmutableList(),
                            upcomingLaunches = it.map { item -> item.toUiModel() }
                                .filter { item -> item.upcoming }.toImmutableList(),
                            pastLaunches = it.map { item -> item.toUiModel() }
                                .filter { item -> !item.upcoming }.toImmutableList(),
                            errorMessage = "",
                            loading = false
                        )
                    )
                }
            } else {
                _uiState.emit(
                    uiState.value.copy(errorMessage = launches.message ?: "Unknown Error", loading = false)
                )
            }
        }
    }

    private fun syncWithDatabase(allLaunches: List<UiModel>, bookmarks: List<UiModel>) {
        viewModelScope.launch(dispatcher) {
            val list = mutableListOf<UiModel>()
            allLaunches.forEach { uiModel ->
                if (bookmarks.any { it.id == uiModel.id }) {
                    list.add(uiModel.copy(bookmarked = true))
                } else {
                    list.add(uiModel.copy(bookmarked = false))
                }
            }
            _uiState.emit(
                uiState.value.copy(
                    allLaunches = list.toImmutableList(),
                    upcomingLaunches = list.filter { it.upcoming }.toImmutableList(),
                    pastLaunches = list.filter { !it.upcoming }.toImmutableList(),
                )
            )
        }
    }

    fun onItemClick(uiModel: UiModel, origin: Origin) {
        // Determine the list to be manipulated based on the origin
        val currentList = when (origin) {
            Origin.AllLaunches -> _uiState.value.allLaunches
            Origin.BookmarkLaunches -> _bookmarks.value ?: emptyList()
            Origin.PastLaunches -> _uiState.value.pastLaunches
            Origin.UpcomingLaunches -> _uiState.value.upcomingLaunches
        }

        // Find the items we are going to collapse/expand
        val previousExpandedItem = currentList.find { it.expanded }
        val currentItemIndex = currentList.indexOf(uiModel)

        // If we didn't find the current item, there's nothing we can do
        if (currentItemIndex == -1) return

        // Create a new list with updated items
        val updatedList = currentList.mapIndexed { index, item ->
            when {
                // Collapse the previous expanded item if it exists
                previousExpandedItem == item -> item.copy(expanded = false)
                // Expand the current item
                index == currentItemIndex -> item.copy(expanded =
                !item.expanded) // Toggle the current item
                // Leave all other items unmodified
                else -> item
            }
        }.toImmutableList()

        // Update the state with the new list
        when (origin) {
            Origin.AllLaunches -> _uiState.value =
                _uiState.value.copy(allLaunches = updatedList)
            Origin.BookmarkLaunches -> _bookmarks.value = updatedList
            Origin.PastLaunches -> _uiState.value =
                _uiState.value.copy(pastLaunches = updatedList)
            Origin.UpcomingLaunches -> _uiState.value =
                _uiState.value.copy(upcomingLaunches = updatedList)
        }
    }

    fun onBookmarkClicked(uiModel: UiModel) {
        viewModelScope.launch(dispatcher) {
            if (uiModel.bookmarked) {
                bookmarksUseCase.deleteLaunch(uiModel.toDomainModel())
            } else {
                bookmarksUseCase.insertLaunch(uiModel.toDomainModel().copy(bookmarked = true))
            }
        }
    }
}

inline fun <T> MutableList<T>.replaceIf(toReplace: T, predicate: (T) -> Boolean): Boolean {
    var replaced = false
    for (i in 0 until size) {
        val currentValue = get(i)
        if (predicate(currentValue)) {
            removeAt(i)
            add(i, toReplace)
            replaced = true
            break
        }
    }
    return replaced
}