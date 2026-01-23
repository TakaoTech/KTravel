package com.takaotech.ktravel.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.compose.AsyncImage
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun PlaceItem(
    //TODO
    hour: String,
    name: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    image: String? = null,
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
        val (imageRef, nameRef, hourRef) = createRefs()

        Text(
            modifier = Modifier.constrainAs(hourRef) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            text = hour,
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
                end.linkTo(parent.end)
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
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceItemPreview() {
    PlaceItem(
        expanded = true,

        hour = "10:00",
        name = "Test",
        image = null
    )
}