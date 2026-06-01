package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.Routes
import com.takaotech.navigation.common.GeoJsonConverter
import com.takaotech.os_map.RouteMap
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.collections.immutable.toPersistentList
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.check
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningTransportRoutePreviewPage(
    routes: Routes,
    selectedRouteIndex: Int,
    onRouteChange: (Int) -> Unit,
    onRouteConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedRoute by remember(selectedRouteIndex) {
        derivedStateOf {
            routes.routes[selectedRouteIndex]
        }
    }

    val sheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = sheetState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (routes.routes.size > 1) {
                    stickyHeader {
                        PrimaryScrollableTabRow(
                            selectedTabIndex = selectedRouteIndex,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            routes.routes.forEachIndexed { index, _ ->
                                Tab(
                                    selected = index == selectedRouteIndex,
                                    onClick = { onRouteChange(index) },
                                    text = { Text("Route ${index + 1}") }
                                )
                            }
                        }
                    }
                }

                val selectedRoute = routes.routes.getOrNull(selectedRouteIndex)
                selectedRoute?.sections?.forEach { section ->
                    item {
                        RouteStepSection(section = section)
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Route Preview") },
                actions = {
                    IconButton(
                        modifier = Modifier,
                        onClick = onRouteConfirm
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.check),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        RoutePreviewMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            sections = selectedRoute.sections
        )
    }
}


@Composable
fun RoutePreviewMap(
    modifier: Modifier = Modifier,
    sections: List<RouteSection>
) {
    val path by remember(sections) {
        derivedStateOf {
            sections.mapNotNull { it.polyline }
                .let { GeoJsonConverter.mergePolylinesToGeoJson(it) }
        }
    }

    RouteMap(
        modifier = modifier,
        geoJsonPath = path
    )
}

@Composable
fun RouteStepSection(
    section: RouteSection,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Duration ${section.summary.durationSeconds}")

        for (action in section.actions) {
            RouteStep(
                action = action
            )
        }
    }
}

@Composable
fun RouteStep(
    action: RouteAction,
    modifier: Modifier = Modifier,
) {
    val distanceM = (action.distanceMeters `in` Length.meters).roundToInt()
    val distanceText = if (distanceM >= 1000) {
        val km = distanceM / 1000.0
        val kmInt = km.toInt()
        val kmDec = ((km - kmInt) * 10).roundToInt()
        "$kmInt.$kmDec km"
    } else {
        "$distanceM m"
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.action.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!action.instruction.isNullOrBlank()) {
                    Text(
                        text = action.instruction,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (!action.direction.isNullOrBlank()) {
                    Text(
                        text = action.direction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = action.durationSeconds.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@PreviewScreenSizes
@Composable
private fun PlanningRoutePreviewPagePreview() {
    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            PlanningTransportRoutePreviewPage(
                selectedRouteIndex = 0,
                onRouteChange = {},
                onRouteConfirm = {},
                routes = Routes(
                    routes = listOf(
                        Route(
                            sections = listOf(
                                RouteSection(
                                    summary = RouteSummary(
                                        durationSeconds = 1800.seconds,
                                        distanceMeters = 15000
                                    ),
                                    actions = buildList {

                                        add(
                                            RouteAction(
                                                action = "depart",
                                                durationSeconds = 60.seconds,
                                                distanceMeters = 500 * Length.meters,
                                                instruction = "Head north on Via del Corso",
                                                direction = "north",
                                                severity = "normal"
                                            )
                                        )
                                        repeat(10) {
                                            add(
                                                RouteAction(
                                                    action = "turn",
                                                    durationSeconds = 45.seconds,
                                                    distanceMeters = 320 * Length.meters,
                                                    instruction = "Turn right onto Via Roma",
                                                    direction = "right",
                                                    severity = "normal"
                                                )
                                            )
                                        }
                                        add(
                                            RouteAction(
                                                action = "arrive",
                                                durationSeconds = 0.seconds,
                                                distanceMeters = 0 * Length.meters,
                                                instruction = "Arrive at destination",
                                                severity = "normal"
                                            )
                                        )
                                    }.toPersistentList()
                                )
                            ).toPersistentList()
                        ),
                        Route(
                            sections = listOf(
                                RouteSection(
                                    summary = RouteSummary(
                                        durationSeconds = 2400.seconds,
                                        distanceMeters = 18000
                                    ),
                                    actions = listOf(
                                        RouteAction(
                                            action = "depart",
                                            durationSeconds = 90.seconds,
                                            distanceMeters = 800 * Length.meters,
                                            instruction = "Head south on Via Appia",
                                            direction = "south",
                                            severity = "normal"
                                        ),
                                        RouteAction(
                                            action = "arrive",
                                            durationSeconds = 0.seconds,
                                            distanceMeters = 0 * Length.meters,
                                            instruction = "Arrive at destination",
                                            severity = "normal"
                                        )
                                    ).toPersistentList()
                                )
                            ).toPersistentList()
                        )
                    ).toPersistentList()
                )
            )
        }
    }
}

@PreviewScreenSizes
@Composable
private fun RouteStepPreview() {
    Scaffold {
        RouteStep(
            modifier = Modifier.padding(it),
            action = RouteAction(
                action = "turn",
                durationSeconds = 45.seconds,
                distanceMeters = 320 * Length.meters,
                instruction = "Turn right onto Via Roma",
                direction = "right",
                severity = "normal"
            )
        )
    }
}
