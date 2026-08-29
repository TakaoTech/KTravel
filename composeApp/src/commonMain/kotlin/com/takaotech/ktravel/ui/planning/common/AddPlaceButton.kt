package com.takaotech.ktravel.ui.planning.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.planning_detail_add_place
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPlaceButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(ButtonDefaults.IconSize),
            painter = painterResource(Res.drawable.add),
            contentDescription = null,
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(Res.string.planning_detail_add_place))
    }
}
