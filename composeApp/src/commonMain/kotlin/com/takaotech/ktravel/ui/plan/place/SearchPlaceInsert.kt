package com.takaotech.ktravel.ui.plan.place

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.ktravel.ui.theme.KTravelTheme

@Composable
fun SearchPlaceInsert(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onPlaceSelected: (name: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            label = { Text("Cerca luogo") },
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
        )

        // TODO: Implementare lista risultati ricerca
    }
}

//region Previews
@Composable
@Preview(showBackground = true)
private fun SearchPlaceInsertPreview() {
    KTravelTheme {
        SearchPlaceInsert(
            searchQuery = TextFieldValue(),
            onSearchQueryChange = { },
            onPlaceSelected = { _, _, _ -> },
        )
    }
}
//endregion Previews
