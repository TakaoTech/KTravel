package com.takaotech.navigation.routing.dto.response.toll

import com.takaotech.navigation.routing.dto.response.Location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Refers to the physical location where the toll is collected. This can include various structures
 * such as toll booths, transponder readers, or number-plate cameras.
 * It's important to note that certain toll collection methods, such as vignettes, do not have
 * specific toll collection locations associated with them, and therefore this element will not be
 * present at all.
 *
 * @property name A descriptive name of the location.
 * @property location The coordinates of the payment location.
 */
@Serializable
data class TollCollectionLocation(
    @SerialName("name") val name: String? = null,
    @SerialName("location") val location: Location
)
