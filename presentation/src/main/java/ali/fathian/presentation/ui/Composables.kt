package ali.fathian.presentation.ui

import ali.fathian.presentation.R
import ali.fathian.presentation.model.Launches
import ali.fathian.presentation.model.Origin
import ali.fathian.presentation.model.UiModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun LaunchList(
    launches: Launches,
    bookmarks: List<UiModel>?,
    onRetryClick: () -> Unit,
    onItemClick: (UiModel, Origin) -> Unit,
    onBookmarkClicked: (UiModel) -> Unit
) {
    var selectedBottomBarIndex by remember {
        mutableIntStateOf(0)
    }
    val bottomBarItems = remember { listOf("Launches", "Bookmarks") }
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomBarItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedBottomBarIndex == index,
                        onClick = {
                            if (index == 0) {
                                navController.navigate(route = Screens.LaunchListScreen.route)
                            } else {
                                navController.navigate(route = Screens.BookmarksScreen.route)
                            }
                            selectedBottomBarIndex = index
                        },
                        icon = {
                            if (index == 0) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = item)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = item
                                )
                            }
                        },
                        label = {
                            Text(text = item)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Screens.LaunchListScreen.route) {
            composable(route = Screens.LaunchListScreen.route) {
                Home(
                    paddingValues = paddingValues,
                    launches = launches,
                    onRetryClick = onRetryClick,
                    onItemClick = onItemClick,
                    onBookmarkClicked = onBookmarkClicked
                )
            }
            composable(route = Screens.BookmarksScreen.route) {
                Bookmarks(
                    paddingValues = paddingValues,
                    bookmarks = bookmarks,
                    onItemClick = onItemClick,
                    onBookmarkClicked = onBookmarkClicked
                )
            }
        }
    }
}

@Composable
fun Bookmarks(
    paddingValues: PaddingValues,
    bookmarks: List<UiModel>?,
    onItemClick: (UiModel, Origin) -> Unit,
    onBookmarkClicked: (UiModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(all = 8.dp)
    ) {
        items(bookmarks ?: emptyList(), key = { it.id }) { uiModel ->
            LaunchItem(
                uiModel = uiModel,
                onItemClick = onItemClick,
                onBookmarkClicked = onBookmarkClicked,
                origin = Origin.BookmarkLaunches
            )
        }
    }
}

@Composable
private fun Home(
    paddingValues: PaddingValues,
    launches: Launches,
    onRetryClick: () -> Unit,
    onItemClick: (UiModel, Origin) -> Unit,
    onBookmarkClicked: (UiModel) -> Unit
) {
    val tabItems = remember { listOf("All", "Upcoming", "Past") }
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (launches.loading) {
            Loading()
        } else if (launches.errorMessage.isNotEmpty()) {
            ErrorMessageUi(launches.errorMessage) {
                onRetryClick()
            }
        } else {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabItems.forEachIndexed { index, text ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(text)
                        }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> LaunchesUi(
                    launches = launches.allLaunches,
                    onItemClick = onItemClick,
                    onBookmarkClicked = onBookmarkClicked,
                    origin = Origin.AllLaunches
                )

                1 -> LaunchesUi(
                    launches = launches.upcomingLaunches,
                    onItemClick = onItemClick,
                    onBookmarkClicked = onBookmarkClicked,
                    origin = Origin.UpcomingLaunches
                )

                2 -> LaunchesUi(
                    launches = launches.pastLaunches,
                    onItemClick = onItemClick,
                    onBookmarkClicked = onBookmarkClicked,
                    origin = Origin.PastLaunches
                )
            }
        }
    }
}

@Composable
fun LaunchesUi(
    launches: List<UiModel>,
    onItemClick: (UiModel, Origin) -> Unit,
    onBookmarkClicked: (UiModel) -> Unit,
    origin: Origin
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(all = 8.dp)
    ) {
        items(launches, key = { it.id }) { uiModel ->
            LaunchItem(
                uiModel = uiModel,
                onItemClick = onItemClick,
                onBookmarkClicked = onBookmarkClicked,
                origin
            )
        }
    }
}

@Composable
fun ErrorMessageUi(message: String, onRetryClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetryClick) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
fun LaunchItem(
    uiModel: UiModel,
    onItemClick: (UiModel, Origin) -> Unit,
    onBookmarkClicked: (UiModel) -> Unit,
    origin: Origin
) {
    Column(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color.Red,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onItemClick(uiModel, origin)
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .width(64.dp)
                    .padding(start = 10.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uiModel.image)
                        .fallback(R.drawable.spacex_logo)
                        .error(R.drawable.spacex_logo)
                        .placeholder(R.drawable.spacex_logo)
                        .build(),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = uiModel.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    text = uiModel.date,
                    fontSize = 16.sp
                )
                Text(
                    text = uiModel.time,
                    fontSize = 16.sp
                )
                Text(
                    text = uiModel.statusText,
                    color = uiModel.statusColor,
                    fontSize = 16.sp
                )
            }
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .width(32.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    imageVector = if (uiModel.bookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Go to launch's details",
                    modifier = Modifier.clickable {
                        onBookmarkClicked(uiModel)
                    }
                )
            }
        }
        AnimatedVisibility(visible = uiModel.expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Details:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = uiModel.details, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun Loading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
fun ErrorMessagePreview() {
    ErrorMessageUi("Error") {

    }
}