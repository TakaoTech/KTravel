package com.takaotech.ktravel.data.archive

import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.data.entity.PlaceEntity
import com.takaotech.ktravel.data.entity.RouteEntity
import com.takaotech.ktravel.data.entity.RouteSectionEntity
import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.entity.TravelDayEntity
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.entity.VisitScheduleEntity
import com.takaotech.ktravel.domain.model.AttachmentReference
import kotlinx.datetime.LocalDate

/**
 * Piano di riferimento per i test di archivio: due giorni, uno step con inventario e note che
 * referenziano i file, uno step di trasporto con rotta non vuota (per verificare che i rami della
 * sealed class sopravvivano al round-trip).
 */
internal object ArchiveTestFixtures {

    const val TRAVEL_ID = "travel-1"
    const val PHOTO_PATH = "$TRAVEL_ID/step-1/photo.jpg"
    const val DOC_PATH = "$TRAVEL_ID/step-1/guide.pdf"

    val PHOTO_BYTES = ByteArray(2048) { (it % 97).toByte() }
    val DOC_BYTES = "a travel guide".encodeToByteArray()

    fun plan(): TravelPlanEntity = TravelPlanEntity(
        id = TRAVEL_ID,
        name = "Tokyo",
        periodStart = LocalDate(2026, 4, 1),
        periodEnd = LocalDate(2026, 4, 10),
        days = listOf(
            TravelDayEntity(
                id = "day-1",
                date = LocalDate(2026, 4, 1),
                steps = listOf(placeStep(), transportStep()),
                places = listOf(PlaceEntity("place-day-1", "Ueno", 35.71, 139.77)),
            ),
            TravelDayEntity(
                id = "day-2",
                date = LocalDate(2026, 4, 2),
                steps = emptyList(),
                places = emptyList(),
            ),
        ),
        places = listOf(PlaceEntity("place-backlog", "Odaiba", 35.62, 139.77)),
    )

    private fun placeStep() = StepEntity.Place(
        id = "step-1",
        name = "Senso-ji",
        lat = 35.71,
        lng = 139.79,
        schedule = VisitScheduleEntity(
            dateEpochDays = null,
            startTimeHour = 9,
            startTimeMinute = 30,
            endTimeHour = 11,
            endTimeMinute = 0,
        ),
        note = buildString {
            appendLine("# Senso-ji")
            appendLine(AttachmentReference.imageMarkdown(PHOTO_PATH, altText = "Tempio"))
            appendLine(AttachmentReference.fileMarkdown(DOC_PATH, label = "guide.pdf"))
            appendLine("External [link](https://example.com).")
        },
        attachments = listOf(
            AttachmentEntity(
                id = "att-1",
                relativePath = PHOTO_PATH,
                originalName = "photo.jpg",
                mimeType = "image/jpeg",
                sizeBytes = PHOTO_BYTES.size.toLong(),
            ),
            AttachmentEntity(
                id = "att-2",
                relativePath = DOC_PATH,
                originalName = "guide.pdf",
                mimeType = "application/pdf",
                sizeBytes = DOC_BYTES.size.toLong(),
            ),
        ),
    )

    /** Variante senza inventario, per i casi in cui gli allegati non sono il soggetto del test. */
    fun TravelPlanEntity.withoutAttachments(): TravelPlanEntity = mapPlaceSteps { step ->
        step.copy(attachments = emptyList())
    }

    /** Variante con un solo allegato, il cui path relativo è [relativePath]. */
    fun TravelPlanEntity.withAttachmentPath(relativePath: String): TravelPlanEntity =
        mapPlaceSteps { step ->
            step.copy(
                attachments = step.attachments.take(1).map { it.copy(relativePath = relativePath) },
            )
        }

    private fun TravelPlanEntity.mapPlaceSteps(transform: (StepEntity.Place) -> StepEntity.Place): TravelPlanEntity =
        copy(
            days = days.map { day ->
                day.copy(
                    steps = day.steps.map { step ->
                        if (step is StepEntity.Place) transform(step) else step
                    },
                )
            },
        )

    private fun transportStep() = StepEntity.Transport(
        id = "step-2",
        transportType = "TRAIN",
        route = RouteEntity(
            sections = listOf(
                RouteSectionEntity(
                    durationSeconds = 1800,
                    distanceMeters = 12_500.0,
                    polyline = "abc123",
                    transportMode = "train",
                ),
            ),
        ),
    )
}
