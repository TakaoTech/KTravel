package com.takaotech.ktravel.domain.model

/**
 * Avviso di incoerenza temporale sull'itinerario di un giorno.
 *
 * È una *segnalazione*, non un errore: la lista [StepDomain] è l'unica titolare della coerenza
 * degli orari (vincolo V2) e la coerenza va mostrata all'utente, non imposta.
 */
data class ScheduleWarning(
    val stepId: String,
    val previousStepId: String,
    val reason: Reason
) {
    enum class Reason {
        /** L'orario dello step precede quello di uno step schedulato che lo precede nella lista. */
        OUT_OF_ORDER
    }
}

/**
 * Valuta (senza imporre) la coerenza temporale dell'itinerario di un giorno (V2).
 *
 * Non lancia mai: restituisce gli avvisi da mostrare in UI. Considera solo gli [StepDomain.Place]
 * con orario assegnato; uno step è incoerente se il suo orario precede quello dell'ultimo step
 * schedulato che lo precede nella lista. Gli step di trasporto e quelli senza orario sono ignorati.
 */
fun List<StepDomain>.scheduleInconsistencies(): List<ScheduleWarning> {
    val warnings = mutableListOf<ScheduleWarning>()
    var previousScheduled: StepDomain.Place? = null

    for (step in this) {
        if (step !is StepDomain.Place) continue
        val time = step.schedule?.time ?: continue

        val previous = previousScheduled
        if (previous != null && previous.schedule != null && time < previous.schedule.time) {
            warnings += ScheduleWarning(
                stepId = step.id,
                previousStepId = previous.id,
                reason = ScheduleWarning.Reason.OUT_OF_ORDER
            )
        }
        previousScheduled = step
    }

    return warnings
}
