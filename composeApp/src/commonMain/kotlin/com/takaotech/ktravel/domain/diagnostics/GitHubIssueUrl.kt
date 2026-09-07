package com.takaotech.ktravel.domain.diagnostics

import io.ktor.http.encodeURLParameter

/** Where the issues of this application live. */
const val KTRAVEL_ISSUES_URL: String = "https://github.com/TakaoTech/KTravel/issues/new"

/** The label every report opened from the application carries. */
private const val ISSUE_LABEL = "bug"

/**
 * The GitHub URL that opens a new issue with [report] already filled in.
 *
 * GitHub has no way to attach a file through a URL, which is why the action saves the log next to
 * opening this: the body says so, and the user attaches it in the browser.
 *
 * @param report The title and body to prefill.
 */
fun gitHubIssueUrl(report: IssueReport): String = buildString {
    append(KTRAVEL_ISSUES_URL)
    append("?labels=")
    append(ISSUE_LABEL)
    append("&title=")
    append(report.title.encodeURLParameter())
    append("&body=")
    append(report.body.encodeURLParameter())
}
