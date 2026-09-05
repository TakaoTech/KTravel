package com.takaotech.ktravel.ui.plan.day.stepspane.component

import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.takaotech.ktravel.ui.plan.day.stepspane.StepsPaneTestTags
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.inbox_customize
import ktravel.composeapp.generated.resources.planning_detail_cd_close_backlog
import ktravel.composeapp.generated.resources.planning_detail_cd_open_backlog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Toolbar toggle of the backlog pane: filled while the pane is showing, tonal while it is not, so
 * the button carries the state the way the design does. The description follows the state too,
 * which is what announces it to a screen reader.
 */
@Composable
internal fun BacklogToggle(open: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(
        if (open) {
            Res.string.planning_detail_cd_close_backlog
        } else {
            Res.string.planning_detail_cd_open_backlog
        },
    )
    val content: @Composable () -> Unit = {
        Icon(
            painter = painterResource(Res.drawable.inbox_customize),
            contentDescription = description,
        )
    }
    val buttonModifier = modifier.testTag(StepsPaneTestTags.OPEN_BACKLOG_BUTTON)

    if (open) {
        FilledIconButton(modifier = buttonModifier, onClick = onClick, content = content)
    } else {
        FilledTonalIconButton(modifier = buttonModifier, onClick = onClick, content = content)
    }
}
