package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.core.ui.KFieldState
import com.takaotech.ktravel.di.LocalAppGraph
import com.takaotech.ktravel.ui.planning.trip.TravelDateRangePicker
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.travel_creation_button
import ktravel.composeapp.generated.resources.travel_creation_name_label
import ktravel.composeapp.generated.resources.travel_creation_name_placeholder
import ktravel.composeapp.generated.resources.travel_creation_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Serializable
object TravelCreationPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelCreationPage(
    onBackClick: () -> Unit,
    onNavigateToPlanning: (id: String) -> Unit
) {
    val appGraph = LocalAppGraph.current
    val viewModel = remember { appGraph.travelCreationViewModel }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdTravelId) {
        uiState.createdTravelId?.let { id ->
            onNavigateToPlanning(id)
        }
    }

    TravelCreationPage(
        travelName = uiState.travelName,
        startDateMillis = uiState.startDateMillis,
        endDateMillis = uiState.endDateMillis,
        error = uiState.error,
        onNameChange = { viewModel.onNameChange(it) },
        onPlanDateRangeChanged = { start, end ->
            viewModel.onDateRangeChange(start, end)
        },
        onBackClick = onBackClick,
        onConfirmClick = { viewModel.createTravelPlan() },
        onDismissError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TravelCreationPage(
    travelName: KFieldState,
    startDateMillis: Long?,
    endDateMillis: Long?,
    error: String?,
    onNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onDismissError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            onDismissError()
        }
    }

    var showDateRangePicker by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDateMillis ?: 0L,
        initialSelectedEndDateMillis = endDateMillis ?: 0L
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.travel_creation_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    // TODO Change icon to add with background circle
                    Icon(
                        painter = painterResource(Res.drawable.add),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.travel_creation_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero image
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(180.dp)
//                    .clip(RoundedCornerShape(16.dp))
//                    .background(
//                        Brush.verticalGradient(
//                            colors = listOf(Color(0xFF546E7A), Color(0xFF263238))
//                        )
//                    )
//            ) {
//                Column(
//                    modifier = Modifier
//                        .align(Alignment.BottomStart)
//                        .padding(16.dp)
//                ) {
//                    Text(
//                        text = stringResource(Res.string.travel_creation_hero_subtitle),
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Medium,
//                        letterSpacing = 1.sp
//                    )
//                    Text(
//                        text = stringResource(Res.string.travel_creation_hero_title),
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }

            // Nome del viaggio
            // TODO Replace with PlanningHeader?
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.travel_creation_name_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = travelName.value,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.travel_creation_name_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.flight),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = travelName.validationState.isError,
                    supportingText = {
                        travelName.validationState.kErrorStringResource()?.let {
                            Text(text = it)
                        }
                    },
                    singleLine = true,
                )
            }

            // Periodo del viaggio
            TravelDateRangePicker(
                showDateRangePicker = showDateRangePicker,
                dateRangePickerState = dateRangePickerState,
                onShowDateRangePicker = { showDateRangePicker = it },
                onPlanDateRangeChanged = { start, end -> onPlanDateRangeChanged(start, end) }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@PreviewScreenSizes
@Composable
private fun TravelCreationPagePreview() {
    TravelCreationPage(
        travelName = KFieldState(value = TextFieldValue("Viaggio a Tokyo")),
        startDateMillis = (Clock.System.now() - 1.days).toEpochMilliseconds(),
        endDateMillis = Clock.System.now().toEpochMilliseconds(),
        error = null,
        onNameChange = {},
        onBackClick = {},
        onPlanDateRangeChanged = { start, end -> },
        onConfirmClick = {},
        onDismissError = {}
    )
}
