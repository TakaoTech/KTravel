package com.takaotech.ktravel.ui.shared.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The arrow that leaves a screen.
 *
 * Every screen that has one draws the same icon with the same description, and the only thing that
 * ever differed between the copies was the test tag — which is why it takes a [modifier] and the
 * caller puts its own tag on it.
 *
 * @param onClick Called when the user asks to leave.
 * @param modifier The modifier applied to the button, and where the caller's test tag goes.
 */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            painter = painterResource(Res.drawable.arrow_back),
            contentDescription = stringResource(Res.string.planning_detail_cd_back),
        )
    }
}
