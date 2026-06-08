package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.KTravelPlatform
import com.takaotech.ktravel.core.LocalPlatform
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.Routes
import com.takaotech.navigation.common.GeoJsonConverter
import com.takaotech.navigation.common.PolylineEncoderDecoder
import com.takaotech.os_map.RouteMap
import io.github.kdroidfilter.platformtools.Platform
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
    modifier: Modifier = Modifier,
    platform: Platform = LocalPlatform.current,
) {
    val selectedRoute by remember(selectedRouteIndex) {
        derivedStateOf {
            routes.routes[selectedRouteIndex]
        }
    }


    val onStepClick: (PolylineEncoderDecoder.LatLngZ) -> Unit = {

    }


    if (platform == Platform.JVM) {
        PlanningTransportRouteDesktop(
            onRouteConfirm = onRouteConfirm,
            modifier = modifier,
            routes = routes,
            selectedRouteIndex = selectedRouteIndex,
            onRouteChange = onRouteChange,
            selectedRoute = selectedRoute,
            onStepClick = onStepClick
        )
    } else {
        PlanningTransportPreviewMobile(
            modifier = modifier,
            routes = routes,
            selectedRouteIndex = selectedRouteIndex,
            onRouteChange = onRouteChange,
            onRouteConfirm = onRouteConfirm,
            selectedRoute = selectedRoute,
            onStepClick = onStepClick
        )
    }
}

@Composable
private fun PlanningTransportRouteDesktop(
    onRouteConfirm: () -> Unit,
    modifier: Modifier,
    routes: Routes,
    selectedRouteIndex: Int,
    onRouteChange: (Int) -> Unit,
    selectedRoute: Route,
    onStepClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
) {
    Scaffold(
        topBar = {
            RoutePreviewTopBar(onRouteConfirm = onRouteConfirm)
        }
    ) {
        Row(modifier = modifier.padding(it)) {
            RouteStepsPreview(
                modifier = Modifier.weight(1f),
                routes = routes,
                selectedRouteIndex = selectedRouteIndex,
                onRouteChange = onRouteChange,
                onStepClick = onStepClick
            )

            RoutePreviewMap(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                enable = true,
                sections = selectedRoute.sections,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlanningTransportPreviewMobile(
    modifier: Modifier,
    routes: Routes,
    selectedRouteIndex: Int,
    onRouteChange: (Int) -> Unit,
    onRouteConfirm: () -> Unit,
    selectedRoute: Route,
    onStepClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
) {
    val sheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = sheetState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            RouteStepsPreview(
                modifier = Modifier.fillMaxSize(),
                routes = routes,
                selectedRouteIndex = selectedRouteIndex,
                onRouteChange = onRouteChange,
                onStepClick = onStepClick
            )
        },
        topBar = {
            RoutePreviewTopBar(onRouteConfirm = onRouteConfirm)
        }
    ) {
        val mapEnable by remember(sheetState.bottomSheetState.hasExpandedState) {
            derivedStateOf {
                !sheetState.bottomSheetState.hasExpandedState
            }
        }

        RoutePreviewMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            enable = mapEnable,
            sections = selectedRoute.sections,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutePreviewTopBar(onRouteConfirm: () -> Unit) {
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

@Composable
private fun RouteStepsPreview(
    routes: Routes,
    selectedRouteIndex: Int,
    modifier: Modifier = Modifier,
    onRouteChange: (Int) -> Unit,
    onStepClick: (PolylineEncoderDecoder.LatLngZ) -> Unit
) {
    LazyColumn(
        modifier = modifier
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
                RouteStepSection(section = section, onActionClick = onStepClick)
            }
        }
    }
}


@Composable
fun RoutePreviewMap(
    enable: Boolean,
    sections: List<RouteSection>,
    modifier: Modifier = Modifier,
) {
    val path by remember(sections) {
        derivedStateOf {
            sections.mapNotNull { it.polyline }
                .let { GeoJsonConverter.mergePolylinesToGeoJson(it) }
        }
    }

    RouteMap(
        modifier = modifier,
        enable = enable,
        geoJsonPath = path,
    )
}

@Composable
fun RouteStepSection(
    section: RouteSection,
    onActionClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Duration ${section.summary.durationSeconds}")

        for (action in section.actions) {
            val resolvedClick: (() -> Unit)? = remember(section.polyline, action.offset) {
                val polyline = section.polyline
                val offset = action.offset
                if (polyline != null && offset != null) {
                    {
                        runCatching {
                            val coord =
                                PolylineEncoderDecoder.getCoordinateAtOffset(polyline, offset)
                            onActionClick(coord)
                        }
                    }
                } else null
            }

            RouteStep(
                action = action,
                onActionClick = resolvedClick
            )
        }
    }
}

@Composable
fun RouteStep(
    action: RouteAction,
    onActionClick: (() -> Unit)? = null,
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

    Column(
        modifier = modifier
            .then(if (onActionClick != null) Modifier.clickable(onClick = onActionClick) else Modifier)
    ) {
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
private fun PlanningRoutePreviewPagePreview(
    @PreviewParameter(RoutesPreviewParameterProvider::class) routes: Routes
) {
    KTravelPlatform {
        PlanningTransportPreviewMobile(
            selectedRouteIndex = 0,
            onRouteChange = {},
            onRouteConfirm = {},
            routes = routes,
            modifier = Modifier.fillMaxSize(),
            selectedRoute = routes.routes.first(),
            onStepClick = {}
        )
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

class RoutesPreviewParameterProvider : PreviewParameterProvider<Routes> {
    override val values = sequenceOf(
        Routes(
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
