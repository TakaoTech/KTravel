package com.takaotech.ktravel.ui.planning.common

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
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
        Icon(
            painter = painterResource(Res.drawable.add),
            contentDescription = null
        )
        Text("Add place")
    }
}
