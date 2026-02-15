package com.takaotech.ktravel.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.compose.AsyncImage
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.place_delete
import ktravel.composeapp.generated.resources.place_delete_permanent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun PlaceItem(
    //TODO
    name: String,
    hour: String? = null,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    image: String? = null,
    actions: @Composable () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null,
    onPermanentDeleteClick: () -> Unit
) {
    val showImage by remember(expanded, image) {
        derivedStateOf {
            expanded && image != null
        }
    }

    val imageBytes = remember(image) {
        image?.let { Base64.decode(it) }
    }

    ConstraintLayout(modifier = modifier) {
        val (imageRef, nameRef, hourRef, actionsRef) = createRefs()

        //TODO Remove Hour

        Text(
            modifier = Modifier.constrainAs(hourRef) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            text = hour.orEmpty(),
        )

        if (showImage) {
            AsyncImage(
                modifier = Modifier.constrainAs(imageRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(hourRef.end, margin = 16.dp)
                    end.linkTo(parent.end)
                },
                model = imageBytes,
                contentDescription = null,
            )
        }

        val textModifier = if (showImage) {
            Modifier
                .constrainAs(nameRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(imageRef.start)
                    end.linkTo(parent.end)

                    width = Dimension.percent(0.8f)
                    horizontalBias = 0.0f
                }
        } else {
            Modifier.constrainAs(nameRef) {
                top.linkTo(hourRef.top)
                start.linkTo(hourRef.end, margin = 16.dp)
//                end.linkTo(parent.end)
            }
        }

        Text(
            modifier = textModifier
                //TODO Adapt light-dark
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(topEnd = 8.dp)
                ),
            text = name
        )

        Row(
            modifier = Modifier.constrainAs(actionsRef) {
                start.linkTo(nameRef.end, margin = 16.dp)
                top.linkTo(nameRef.top)
                bottom.linkTo(nameRef.bottom)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
            horizontalArrangement = Arrangement.End
        ) {
            // TODO Modificare layout per avere la compressione delle azioni
            var expanded by remember { mutableStateOf(false) }
            IconButton(
                onClick = {
                    if (onDeleteClick == null) {
                        onPermanentDeleteClick()
                    } else {
                        expanded = true
                    }
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = null
                )
            }

            actions()


            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DeleteMode.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(option.text),
                                color = if (option == DeleteMode.PERMANENT) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    Color.Unspecified
                                }
                            )
                        },
                        onClick = {
                            when (option) {
                                DeleteMode.GENERAL -> onDeleteClick?.invoke()
                                DeleteMode.PERMANENT -> {
                                    expanded = false
                                    onPermanentDeleteClick()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class DeleteMode(val text: StringResource) {
    GENERAL(Res.string.place_delete),
    PERMANENT(Res.string.place_delete_permanent),
}

@Preview(showBackground = true)
@Composable
private fun PlaceItemPreview() {
    PlaceItem(
        expanded = true,

        hour = "10:00",
        name = "Test",
        image = null,
        onDeleteClick = {},
        onPermanentDeleteClick = {}
    )
}