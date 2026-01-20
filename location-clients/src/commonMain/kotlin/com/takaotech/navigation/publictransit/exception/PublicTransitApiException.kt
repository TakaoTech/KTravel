package com.takaotech.navigation.publictransit.exception

/**
 * Exception thrown when the Public Transit API returns an error.
 */
class PublicTransitApiException(
    val errorResponse: com.takaotech.navigation.publictransit.dto.response.ErrorResponse?,
    val statusCode: Int,
    message: String = errorResponse?.title ?: "Public Transit API error"
) : Exception(message)
