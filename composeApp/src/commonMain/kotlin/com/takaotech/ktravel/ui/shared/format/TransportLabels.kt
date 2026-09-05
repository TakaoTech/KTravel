package com.takaotech.ktravel.ui.shared.format

import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.plan.transport.TransportFailureReason
import com.takaotech.ktravel.presentation.plan.transport.options.WalkingPace
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.directions_bus
import ktravel.composeapp.generated.resources.directions_car
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.planning_transport_avoid_car_shuttle_train
import ktravel.composeapp.generated.resources.planning_transport_avoid_controlled_access_highway
import ktravel.composeapp.generated.resources.planning_transport_avoid_dirt_road
import ktravel.composeapp.generated.resources.planning_transport_avoid_ferry
import ktravel.composeapp.generated.resources.planning_transport_avoid_toll_road
import ktravel.composeapp.generated.resources.planning_transport_avoid_tunnel
import ktravel.composeapp.generated.resources.planning_transport_error_invalid_request
import ktravel.composeapp.generated.resources.planning_transport_error_no_route
import ktravel.composeapp.generated.resources.planning_transport_error_not_authenticated
import ktravel.composeapp.generated.resources.planning_transport_error_provider_credentials
import ktravel.composeapp.generated.resources.planning_transport_error_provider_unavailable
import ktravel.composeapp.generated.resources.planning_transport_error_rate_limited
import ktravel.composeapp.generated.resources.planning_transport_error_unexpected
import ktravel.composeapp.generated.resources.planning_transport_error_unreachable
import ktravel.composeapp.generated.resources.planning_transport_mode_aerial
import ktravel.composeapp.generated.resources.planning_transport_mode_bicycle
import ktravel.composeapp.generated.resources.planning_transport_mode_bus
import ktravel.composeapp.generated.resources.planning_transport_mode_bus_rapid
import ktravel.composeapp.generated.resources.planning_transport_mode_car
import ktravel.composeapp.generated.resources.planning_transport_mode_city_train
import ktravel.composeapp.generated.resources.planning_transport_mode_ferry
import ktravel.composeapp.generated.resources.planning_transport_mode_flight
import ktravel.composeapp.generated.resources.planning_transport_mode_high_speed_train
import ktravel.composeapp.generated.resources.planning_transport_mode_inclined
import ktravel.composeapp.generated.resources.planning_transport_mode_inter_regional_train
import ktravel.composeapp.generated.resources.planning_transport_mode_intercity_train
import ktravel.composeapp.generated.resources.planning_transport_mode_light_rail
import ktravel.composeapp.generated.resources.planning_transport_mode_monorail
import ktravel.composeapp.generated.resources.planning_transport_mode_pedestrian
import ktravel.composeapp.generated.resources.planning_transport_mode_private_bus
import ktravel.composeapp.generated.resources.planning_transport_mode_regional_train
import ktravel.composeapp.generated.resources.planning_transport_mode_scooter
import ktravel.composeapp.generated.resources.planning_transport_mode_subway
import ktravel.composeapp.generated.resources.planning_transport_mode_taxi
import ktravel.composeapp.generated.resources.planning_transport_mode_truck
import ktravel.composeapp.generated.resources.planning_transport_profile_here_routing
import ktravel.composeapp.generated.resources.planning_transport_profile_here_routing_desc
import ktravel.composeapp.generated.resources.planning_transport_profile_here_transit
import ktravel.composeapp.generated.resources.planning_transport_profile_here_transit_desc
import ktravel.composeapp.generated.resources.planning_transport_unavailable_missing_key
import ktravel.composeapp.generated.resources.planning_transport_unavailable_not_served
import ktravel.composeapp.generated.resources.planning_transport_unavailable_unreachable
import ktravel.composeapp.generated.resources.planning_transport_walking_pace_fast
import ktravel.composeapp.generated.resources.planning_transport_walking_pace_normal
import ktravel.composeapp.generated.resources.planning_transport_walking_pace_slow
import ktravel.composeapp.generated.resources.train
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** The localized name of a profile, or null when this build has never heard of it. */
fun RoutingProfileId.labelOrNull(): StringResource? = when (this) {
    HERE_ROUTING -> Res.string.planning_transport_profile_here_routing
    HERE_TRANSIT -> Res.string.planning_transport_profile_here_transit
    else -> null
}

/** What the profile is good for, in one line, as the selector shows under its name. */
fun RoutingProfileId.descriptionOrNull(): StringResource? = when (this) {
    HERE_ROUTING -> Res.string.planning_transport_profile_here_routing_desc
    HERE_TRANSIT -> Res.string.planning_transport_profile_here_transit_desc
    else -> null
}

/**
 * The localized name of a mode.
 *
 * Deliberately one lookup covering both vocabularies rather than one per profile, and it is not the
 * merging the contract forbids: this maps an identifier to a word, it does not decide which modes a
 * profile has. `BUS` reads the same in both because the Italian for a coach and for a service bus
 * is the same word — but which of the two the traveller is choosing is still settled by the profile
 * the chip belongs to, never by this table.
 */
@Suppress("CyclomaticComplexMethod")
fun RoutingMode.labelOrNull(): StringResource? = when (id) {
    "CAR" -> Res.string.planning_transport_mode_car
    "TRUCK" -> Res.string.planning_transport_mode_truck
    "TAXI" -> Res.string.planning_transport_mode_taxi
    "BUS" -> Res.string.planning_transport_mode_bus
    "PRIVATE_BUS" -> Res.string.planning_transport_mode_private_bus
    "PEDESTRIAN" -> Res.string.planning_transport_mode_pedestrian
    "BICYCLE" -> Res.string.planning_transport_mode_bicycle
    "SCOOTER" -> Res.string.planning_transport_mode_scooter
    "HIGH_SPEED_TRAIN" -> Res.string.planning_transport_mode_high_speed_train
    "INTERCITY_TRAIN" -> Res.string.planning_transport_mode_intercity_train
    "INTER_REGIONAL_TRAIN" -> Res.string.planning_transport_mode_inter_regional_train
    "REGIONAL_TRAIN" -> Res.string.planning_transport_mode_regional_train
    "CITY_TRAIN" -> Res.string.planning_transport_mode_city_train
    "SUBWAY" -> Res.string.planning_transport_mode_subway
    "LIGHT_RAIL" -> Res.string.planning_transport_mode_light_rail
    "MONORAIL" -> Res.string.planning_transport_mode_monorail
    "BUS_RAPID" -> Res.string.planning_transport_mode_bus_rapid
    "FERRY" -> Res.string.planning_transport_mode_ferry
    "INCLINED" -> Res.string.planning_transport_mode_inclined
    "AERIAL" -> Res.string.planning_transport_mode_aerial
    "FLIGHT" -> Res.string.planning_transport_mode_flight
    else -> null
}

/**
 * An icon for a mode, where the project has one.
 *
 * Null is a normal answer and not a gap to work around: the chip carries its label either way, and
 * a mode without an icon reads as a word rather than as a wrong picture.
 */
fun RoutingMode.iconOrNull(): DrawableResource? = when (id) {
    "CAR", "TAXI" -> Res.drawable.directions_car

    "BUS", "PRIVATE_BUS", "BUS_RAPID" -> Res.drawable.directions_bus

    "HIGH_SPEED_TRAIN", "INTERCITY_TRAIN", "INTER_REGIONAL_TRAIN", "REGIONAL_TRAIN",
    "CITY_TRAIN", "SUBWAY", "LIGHT_RAIL", "MONORAIL",
    -> Res.drawable.train

    "FLIGHT" -> Res.drawable.flight

    else -> null
}

/**
 * The localized name of something a route can be asked to avoid.
 *
 * Exhaustive rather than nullable, unlike the mode lookup above: this set is the app's own and does
 * not arrive from a catalog, so a feature added without a label does not compile.
 */
fun RouteFeature.label(): StringResource = when (this) {
    RouteFeature.TOLL_ROAD -> Res.string.planning_transport_avoid_toll_road
    RouteFeature.CONTROLLED_ACCESS_HIGHWAY -> Res.string.planning_transport_avoid_controlled_access_highway
    RouteFeature.FERRY -> Res.string.planning_transport_avoid_ferry
    RouteFeature.TUNNEL -> Res.string.planning_transport_avoid_tunnel
    RouteFeature.DIRT_ROAD -> Res.string.planning_transport_avoid_dirt_road
    RouteFeature.CAR_SHUTTLE_TRAIN -> Res.string.planning_transport_avoid_car_shuttle_train
}

/** How fast the traveller walks, as the three paces the screen offers. */
fun WalkingPace.label(): StringResource = when (this) {
    WalkingPace.SLOW -> Res.string.planning_transport_walking_pace_slow
    WalkingPace.NORMAL -> Res.string.planning_transport_walking_pace_normal
    WalkingPace.FAST -> Res.string.planning_transport_walking_pace_fast
}

/** Why a profile cannot be picked, said where the traveller can act on it. */
fun ProfileAvailability.reasonOrNull(): StringResource? = when (this) {
    ProfileAvailability.Available -> null
    ProfileAvailability.NotServed -> Res.string.planning_transport_unavailable_not_served
    ProfileAvailability.MissingApiKey -> Res.string.planning_transport_unavailable_missing_key
    is ProfileAvailability.NavigatorUnreachable -> Res.string.planning_transport_unavailable_unreachable
}

/** What went wrong, in terms of what to do about it. */
fun TransportFailureReason.label(): StringResource = when (this) {
    TransportFailureReason.NAVIGATOR_UNREACHABLE -> Res.string.planning_transport_error_unreachable
    TransportFailureReason.NOT_AUTHENTICATED -> Res.string.planning_transport_error_not_authenticated
    TransportFailureReason.PROVIDER_CREDENTIALS -> Res.string.planning_transport_error_provider_credentials
    TransportFailureReason.RATE_LIMITED -> Res.string.planning_transport_error_rate_limited
    TransportFailureReason.PROVIDER_UNAVAILABLE -> Res.string.planning_transport_error_provider_unavailable
    TransportFailureReason.NO_ROUTE_FOUND -> Res.string.planning_transport_error_no_route
    TransportFailureReason.INVALID_REQUEST -> Res.string.planning_transport_error_invalid_request
    TransportFailureReason.UNEXPECTED -> Res.string.planning_transport_error_unexpected
}

private val HERE_ROUTING = RoutingProfileId(provider = "here", profile = "routing")
private val HERE_TRANSIT = RoutingProfileId(provider = "here", profile = "transit")
