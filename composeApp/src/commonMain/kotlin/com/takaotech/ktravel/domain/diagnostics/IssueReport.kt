package com.takaotech.ktravel.domain.diagnostics

import com.takaotech.ktravel.core.telemetry.TelemetryConsent

/**
 * A bug report, ready to be handed to GitHub.
 *
 * @property title The issue title.
 * @property body The issue body, in Markdown, already within [MAX_BODY_CHARS].
 */
data class IssueReport(val title: String, val body: String)

/**
 * How much of the body survives.
 *
 * A GitHub issue is opened by putting the body in the query string, and a URL that grows past about
 * eight kilobytes is refused by the browser or the server before anyone reads it. Six thousand
 * characters leaves room for the percent-encoding, which can triple a newline.
 */
const val MAX_BODY_CHARS: Int = 6_000

/**
 * Builds the report the "report a problem" action opens GitHub with.
 *
 * Pure, and that is the point: what a report says about the user's installation is worth a test, and
 * a test should not need a device to read it.
 *
 * The log is quoted in the body only when the user did **not** consent to sending diagnostics. With
 * consent the maintainer already has the session, and quoting it again would be sending the same
 * thing twice through a channel the user did not pick.
 */
// TODO Review this class
object IssueReportBuilder {

    /**
     * The report for a failure the user is about to describe.
     *
     * @param appVersion The version of the application.
     * @param platform Where it is running, as Platform-Tools names it.
     * @param consent What the user answered about sending diagnostics.
     * @param installationId The id of this installation, quoted so a maintainer can line the report
     *   up with what the telemetry console shows.
     * @param logTail The end of the local log, newest last. Cut from the front when it does not fit.
     */
    fun build(
        appVersion: String,
        platform: String,
        consent: TelemetryConsent,
        installationId: String,
        logTail: String,
    ): IssueReport {
        val header = buildString {
            appendLine("### What happened")
            appendLine()
            appendLine("<!-- Describe what you were doing, and what happened instead. -->")
            appendLine()
            appendLine("### Environment")
            appendLine()
            appendLine("- KTravel: $appVersion")
            appendLine("- Platform: $platform")
            appendLine("- Installation: $installationId")
            appendLine("- Diagnostics sent to the developer: ${consent.reportLabel()}")
            appendLine()
            appendLine("### Log")
            appendLine()
        }

        val body = if (consent == TelemetryConsent.Granted) {
            header + "The log of this installation was sent with the crash reports. " +
                "The full local log is in the file saved next to this report; attach it if it helps."
        } else {
            header + logBlock(logTail, budget = MAX_BODY_CHARS - header.length)
        }

        return IssueReport(
            title = "[$platform] ",
            body = body.take(MAX_BODY_CHARS),
        )
    }

    /**
     * The tail of the log in a fenced block, keeping the newest lines when it does not all fit.
     *
     * The end is what matters: whatever went wrong happened last.
     */
    private fun logBlock(logTail: String, budget: Int): String {
        val fence = "```\n"
        val closing = "\n```\n"
        val note = "\nThe full log has been saved to a file. Attach it to this issue.\n"
        val room = (budget - fence.length - closing.length - note.length).coerceAtLeast(0)
        val kept = if (logTail.length <= room) logTail else logTail.takeLast(room)

        return fence + kept + closing + note
    }

    private fun TelemetryConsent.reportLabel(): String = when (this) {
        TelemetryConsent.Granted -> "yes"
        TelemetryConsent.Denied -> "no"
        TelemetryConsent.Unknown -> "not answered"
    }
}
