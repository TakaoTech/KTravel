package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.takaotech.ktravel.core.KTravelPlatform
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.Routes
import com.takaotech.ktravel.ui.common.MAP_STYLE_URI
import com.takaotech.ktravel.ui.theme.KTravelTheme
import com.takaotech.navigation.common.GeoJsonConverter
import com.takaotech.navigation.common.PolylineEncoderDecoder
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.check
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

/** Inset kept between the route's bounding box and the map edges when framing it. */
private val ROUTE_FIT_PADDING = 48.dp

/** Zoom applied when framing a single route step, close enough to read the manoeuvre. */
private const val STEP_FOCUS_ZOOM = 16.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningTransportRoutePreviewPage(
    routes: Routes,
    selectedRouteIndex: Int,
    onRouteChange: (Int) -> Unit,
    onRouteConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedRoute by remember(selectedRouteIndex) {
        derivedStateOf {
            routes.routes[selectedRouteIndex]
        }
    }

    val cameraState = rememberCameraState()
    val scope = rememberCoroutineScope()
    var focusedStep by remember { mutableStateOf<PolylineEncoderDecoder.LatLngZ?>(null) }

    // Switching route would otherwise leave the marker on a path that is no longer drawn.
    LaunchedEffect(selectedRouteIndex) {
        focusedStep = null
    }

    val onStepClick: (PolylineEncoderDecoder.LatLngZ) -> Unit = { step ->
        focusedStep = step
        scope.launch {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(longitude = step.lng, latitude = step.lat),
                    zoom = STEP_FOCUS_ZOOM,
                ),
            )
        }
    }

    if (
        currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
        )
    ) {
        PlanningTransportRouteDesktop(
            onRouteConfirm = onRouteConfirm,
            modifier = modifier,
            routes = routes,
            selectedRouteIndex = selectedRouteIndex,
            onRouteChange = onRouteChange,
            selectedRoute = selectedRoute,
            onStepClick = onStepClick,
            cameraState = cameraState,
            focusedStep = focusedStep,
        )
    } else {
        PlanningTransportPreviewMobile(
            modifier = modifier,
            routes = routes,
            selectedRouteIndex = selectedRouteIndex,
            onRouteChange = onRouteChange,
            onRouteConfirm = onRouteConfirm,
            selectedRoute = selectedRoute,
            onStepClick = onStepClick,
            cameraState = cameraState,
            focusedStep = focusedStep,
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
    cameraState: CameraState,
    focusedStep: PolylineEncoderDecoder.LatLngZ?,
) {
    Scaffold(
        topBar = {
            RoutePreviewTopBar(onRouteConfirm = onRouteConfirm)
        },
    ) {
        Row(modifier = modifier.padding(it)) {
            RouteStepsPreview(
                modifier = Modifier.weight(1f),
                routes = routes,
                selectedRouteIndex = selectedRouteIndex,
                onRouteChange = onRouteChange,
                onStepClick = onStepClick,
            )

            RoutePreviewMap(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                enable = true,
                sections = selectedRoute.sections,
                cameraState = cameraState,
                focusedStep = focusedStep,
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
    cameraState: CameraState,
    focusedStep: PolylineEncoderDecoder.LatLngZ?,
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            RouteStepsPreview(
                modifier = Modifier.fillMaxSize(),
                routes = routes,
                selectedRouteIndex = selectedRouteIndex,
                onRouteChange = onRouteChange,
                onStepClick = onStepClick,
            )
        },
        topBar = {
            RoutePreviewTopBar(onRouteConfirm = onRouteConfirm)
        },
    ) {
        var mapEnable by remember {
            mutableStateOf(true)
        }

        LaunchedEffect(bottomSheetScaffoldState.bottomSheetState) {
            snapshotFlow { bottomSheetScaffoldState.bottomSheetState }
                .collect {
                    mapEnable = bottomSheetScaffoldState.bottomSheetState.hasExpandedState
                }
        }

        RoutePreviewMap(
            modifier = Modifier
                .fillMaxSize(),
            enable = mapEnable,
            sections = selectedRoute.sections,
            cameraState = cameraState,
            focusedStep = focusedStep,
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
                onClick = onRouteConfirm,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.check),
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun RouteStepsPreview(
    routes: Routes,
    selectedRouteIndex: Int,
    modifier: Modifier = Modifier,
    onRouteChange: (Int) -> Unit,
    onStepClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        if (routes.routes.size > 1) {
            stickyHeader {
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedRouteIndex,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    routes.routes.forEachIndexed { index, _ ->
                        Tab(
                            selected = index == selectedRouteIndex,
                            onClick = { onRouteChange(index) },
                            text = { Text("Route ${index + 1}") },
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

/**
 * Route map: the selected route drawn as a single line, framed on the whole path.
 *
 * [enable] turns the gestures off rather than the map: the caller passes `false` while the bottom
 * sheet is expanded over it, and a map that still panned under the sheet would swallow the drag.
 *
 * [cameraState] is hoisted because the step list, which lives outside this map, drives the camera
 * when a step is clicked; [focusedStep] is the coordinate that click resolved to, marked on the map.
 */
@Composable
fun RoutePreviewMap(
    enable: Boolean,
    sections: List<RouteSection>,
    cameraState: CameraState,
    focusedStep: PolylineEncoderDecoder.LatLngZ?,
    modifier: Modifier = Modifier,
) {
    val path by remember(sections) {
        derivedStateOf {
            sections.mapNotNull { it.polyline }
                .let { GeoJsonConverter.mergePolylinesToGeoJson(it) }
        }
    }

    LaunchedEffect(path) {
        routeBoundingBox(path)?.let { bbox ->
            cameraState.animateTo(bbox, padding = PaddingValues(ROUTE_FIT_PADDING))
        }
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(MAP_STYLE_URI),
        cameraState = cameraState,
        options = MapOptions(
            gestureOptions = if (enable) GestureOptions.Standard else GestureOptions.AllDisabled,
        ),
    ) {
        val pathLine = rememberGeoJsonSource(data = GeoJsonData.JsonString(path))
        LineLayer("path", source = pathLine)

        focusedStep?.let { step ->
            val markerSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(
                    Point(Position(longitude = step.lng, latitude = step.lat)),
                ),
            )
            CircleLayer(
                id = "step-marker",
                source = markerSource,
                radius = const(8.dp),
                color = const(Color.Red),
                strokeColor = const(Color.White),
                strokeWidth = const(2.dp),
            )
        }
    }
}

/**
 * Bounding box of the route, or `null` when the GeoJSON cannot be read.
 *
 * Only the shape [GeoJsonConverter.mergePolylinesToGeoJson] produces is handled — a `Feature` whose
 * geometry is a `LineString` — because that is the only producer feeding this screen.
 */
private fun routeBoundingBox(geoJson: String): BoundingBox? = runCatching {
    val root = Json.parseToJsonElement(geoJson).jsonObject
    if (root["type"]?.jsonPrimitive?.contentOrNull != "Feature") return@runCatching null
    val coordinates: JsonArray = root["geometry"]?.jsonObject?.get("coordinates")?.jsonArray
        ?: return@runCatching null

    // GeoJSON positions are [longitude, latitude] per RFC 7946. An empty line has no box: min()
    // throws and runCatching turns that into null, same as a payload that cannot be read at all.
    val positions = coordinates.map { it.jsonArray }
    val longitudes = positions.map { it[0].jsonPrimitive.double }
    val latitudes = positions.map { it[1].jsonPrimitive.double }

    BoundingBox(
        west = longitudes.min(),
        south = latitudes.min(),
        east = longitudes.max(),
        north = latitudes.max(),
    )
}.getOrNull()

@Composable
fun RouteStepSection(
    section: RouteSection,
    onActionClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Duration ${section.summary.durationSeconds}")

        for (action in section.actions) {
            RouteStep(
                action = action,
                onActionClick = {
                    val polyline = section.polyline
                    val offset = action.offset

                    if (polyline != null && offset != null) {
                        runCatching {
                            val coord =
                                PolylineEncoderDecoder.getCoordinateAtOffset(polyline, offset)
                            onActionClick(coord)
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun RouteStep(action: RouteAction, onActionClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
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
            .then(if (onActionClick != null) Modifier.clickable(onClick = onActionClick) else Modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.action.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (!action.instruction.isNullOrBlank()) {
                    Text(
                        text = action.instruction,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (!action.direction.isNullOrBlank()) {
                    Text(
                        text = action.direction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = action.durationSeconds.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@PreviewScreenSizes
@Composable
private fun PlanningRoutePreviewPagePreview(@PreviewParameter(RoutesPreviewParameterProvider::class) routes: Routes) =
    KTravelTheme {
        KTravelPlatform {
            PlanningTransportPreviewMobile(
                selectedRouteIndex = 0,
                onRouteChange = {},
                onRouteConfirm = {},
                routes = routes,
                modifier = Modifier.fillMaxSize(),
                selectedRoute = routes.routes.first(),
                onStepClick = {},
                cameraState = rememberCameraState(),
                focusedStep = null,
            )
        }
    }

@PreviewScreenSizes
@Composable
private fun RouteStepPreview() = KTravelTheme {
    Scaffold {
        RouteStep(
            modifier = Modifier.padding(it),
            action = RouteAction(
                action = "turn",
                durationSeconds = 45.seconds,
                distanceMeters = 320 * Length.meters,
                instruction = "Turn right onto Via Roma",
                direction = "right",
                severity = "normal",
            ),
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
                                distanceMeters = 15000,
                            ),
                            actions = buildList {
                                add(
                                    RouteAction(
                                        action = "depart",
                                        durationSeconds = 60.seconds,
                                        distanceMeters = 500 * Length.meters,
                                        instruction = "Head north on Via del Corso",
                                        direction = "north",
                                        severity = "normal",
                                    ),
                                )
                                repeat(10) {
                                    add(
                                        RouteAction(
                                            action = "turn",
                                            durationSeconds = 45.seconds,
                                            distanceMeters = 320 * Length.meters,
                                            instruction = "Turn right onto Via Roma",
                                            direction = "right",
                                            severity = "normal",
                                        ),
                                    )
                                }
                                add(
                                    RouteAction(
                                        action = "arrive",
                                        durationSeconds = 0.seconds,
                                        distanceMeters = 0 * Length.meters,
                                        instruction = "Arrive at destination",
                                        severity = "normal",
                                    ),
                                )
                            }.toPersistentList(),
                        ),
                    ).toPersistentList(),
                ),
                Route(
                    sections = listOf(
                        RouteSection(
                            summary = RouteSummary(
                                durationSeconds = 2400.seconds,
                                distanceMeters = 18000,
                            ),
                            actions = listOf(
                                RouteAction(
                                    action = "depart",
                                    durationSeconds = 90.seconds,
                                    distanceMeters = 800 * Length.meters,
                                    instruction = "Head south on Via Appia",
                                    direction = "south",
                                    severity = "normal",
                                ),
                                RouteAction(
                                    action = "arrive",
                                    durationSeconds = 0.seconds,
                                    distanceMeters = 0 * Length.meters,
                                    instruction = "Arrive at destination",
                                    severity = "normal",
                                ),
                            ).toPersistentList(),
                        ),
                    ).toPersistentList(),
                ),
            ).toPersistentList(),
        ),
    )
}
