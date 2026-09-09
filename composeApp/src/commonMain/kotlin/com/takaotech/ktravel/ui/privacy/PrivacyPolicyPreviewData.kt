package com.takaotech.ktravel.ui.privacy

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicySection
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyNode
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** The two states the document can be drawn in: still loading, and read. */
internal class PrivacyPolicyContentPreviewParams : PreviewParameterProvider<PrivacyPolicyUiState> {

    override val values = sequenceOf(
        PrivacyPolicyUiState(nodes = persistentListOf()) {},
        PrivacyPolicyUiState(nodes = previewPolicyNodes) {},
    )
}

/** The index shut, and open on the section being read. */
internal class PrivacyPolicyIndexPreviewParams : PreviewParameterProvider<Boolean> {

    override val values = sequenceOf(false, true)
}

/**
 * A policy with both shapes the index has to draw: top level sections, and what is nested under one.
 */
internal val previewPolicyNodes: ImmutableList<PrivacyPolicyNode> = persistentListOf(
    node(
        path = "collection",
        depth = 0,
        id = "collection",
        title = "1. Information We Collect",
        body = "KTravel is a trip planner that runs on your device. There is no account to create.",
    ),
    node(
        path = "collection.provided",
        depth = 1,
        id = "provided",
        title = "1.1 Information You Provide Directly",
        body = "What you write while planning is stored in a **local database on this device**.",
    ),
    node(
        path = "collection.automatic",
        depth = 1,
        id = "automatic",
        title = "1.2 Information Collected Automatically",
        body = "**Nothing is collected automatically until you answer the diagnostics question**, " +
            "and nothing is collected while your answer is *no*.",
    ),
    node(
        path = "sdks",
        depth = 0,
        id = "sdks",
        title = "3. Third-Party SDKs and Services",
        body = "Four services are involved in running KTravel, and each one is named here.",
    ),
    node(
        path = "rights",
        depth = 0,
        id = "rights",
        title = "9. Your Rights",
        body = "Because the data is on your device, most of these rights are things you can " +
            "exercise yourself.",
    ),
)

private fun node(path: String, depth: Int, id: String, title: String, body: String) = PrivacyPolicyNode(
    path = path,
    depth = depth,
    section = PrivacyPolicySection(id = id, title = title, body = body),
)
