package com.takaotech.ktravel.ui.planning.common

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.edit
import org.jetbrains.compose.resources.painterResource

@Composable
fun AddPlaceButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        // TODO Change icon with add
        Icon(
            painter = painterResource(Res.drawable.edit),
            contentDescription = null
        )
        Text("Add place")
    }
}
