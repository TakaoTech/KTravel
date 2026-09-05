package com.takaotech.ktravel.domain.routing

/**
 * Which profile of which provider computes a route.
 *
 * A pair rather than one name because the two halves answer different questions — who computes it,
 * and which of their APIs does — and because they are the two the navigator itself is keyed on.
 */
data class RoutingProfileId(val provider: String, val profile: String)

/**
 * A way of travelling, named in the vocabulary of the profile it belongs to.
 *
 * A plain identifier and not an enum, because the set is not knowable here: it comes from the
 * catalog. More to the point, it must not be shared between profiles. HERE's road API and its public
 * transport API both have a `BUS` and a `PEDESTRIAN`, and they are different things — a coach the
 * traveller drives against a scheduled service, a walking route you ask for against the walking steps
 * of a journey you are told about. A common enum would spell each of them once and lose the
 * distinction; a mode that only ever travels attached to its profile cannot.
 */
data class RoutingMode(val id: String)

/**
 * What a profile lets a traveller ask for, in the shape of the API family that answers it.
 *
 * A sealed type rather than a flat list of fields, and it mirrors
 * [com.takaotech.gunzou.api.catalog.SupportedModes] on the wire for the same reason: the two
 * families do not take the same parameters. A road profile has a single required vehicle and things
 * to keep off the route; a transit profile has a set of acceptable vehicles and a tolerance for
 * transfers. Flattening both into one record forces every profile to carry the other's fields
 * emptied out, and a reader cannot tell an option that is switched off from one that does not apply.
 *
 * Which variant a profile has is also what decides which options control the screen draws, so a
 * family added here does not compile until it is given one.
 *
 * @property modes What may be asked for, in this profile's own vocabulary.
 * @property maxAlternatives How many routes may be asked for at once.
 */
sealed interface RoutingOptionsSpec {

    val modes: List<RoutingMode>
    val maxAlternatives: Int

    /** A profile that takes no mode at all, and therefore has nothing to configure. */
    data object None : RoutingOptionsSpec {
        override val modes: List<RoutingMode> get() = emptyList()
        override val maxAlternatives: Int get() = 1
    }

    /**
     * A route on roads, travelled by exactly one vehicle.
     *
     * @property modesSupportingShortest The subset that can be optimized for distance rather than
     *   time. Upstream refuses the option on the others, so it is not offered rather than sent and
     *   rejected.
     * @property supportsTolls Whether the profile understands being asked to keep off toll roads.
     */
    data class RoutingSingleMode(
        override val modes: List<RoutingMode>,
        override val maxAlternatives: Int,
        val modesSupportingShortest: Set<RoutingMode> = emptySet(),
        val supportsTolls: Boolean = false,
    ) : RoutingOptionsSpec

    /**
     * A journey on scheduled services, restricted by which vehicles it may use.
     *
     * The modes are a filter and not a choice, and an empty one admits all of them.
     */
    data class TransitFilter(override val modes: List<RoutingMode>, override val maxAlternatives: Int) :
        RoutingOptionsSpec
}

/**
 * What a profile can do, as the app needs to know it.
 *
 * @property options Everything that depends on the family of API behind the profile.
 * @property supportsArriveBy Whether it can plan backwards from an arrival time. Published rather
 *   than assumed: a navigator refuses [RouteTimeChoice.ArriveBy] on a profile that cannot honour it,
 *   so the choice is not offered rather than being sent and rejected.
 * @property requiresApiKey Whether a route needs the traveller's own provider key.
 */
data class RoutingProfileInfo(
    val id: RoutingProfileId,
    val displayName: String,
    val options: RoutingOptionsSpec,
    val supportsArriveBy: Boolean = false,
    val requiresApiKey: Boolean = false,
)

/**
 * Why a profile the app knows about cannot be used right now.
 *
 * The distinction is the point of the whole catalog: a profile missing because this deployment does
 * not serve it, one missing because the trip has no API key, and one missing because nothing
 * answered are three different problems with three different remedies, and collapsing them into
 * "unavailable" leaves the traveller with nothing to act on.
 */
sealed interface ProfileAvailability {

    /** Usable now. */
    data object Available : ProfileAvailability

    /** This version of the app can express it, but the chosen navigator does not mount it. */
    data object NotServed : ProfileAvailability

    /** It needs the traveller's provider key, and this trip has none configured. */
    data object MissingApiKey : ProfileAvailability

    /** Nothing is known, because the navigator did not answer. */
    data class NavigatorUnreachable(val reason: String) : ProfileAvailability
}

/** A profile, and whether it can be picked. */
data class RoutingProfileOption(val profile: RoutingProfileInfo, val availability: ProfileAvailability) {
    val isSelectable: Boolean get() = availability == ProfileAvailability.Available
}

/**
 * Everything the app knows about one navigator at one moment.
 *
 * [options] always lists every profile this build can express, including the ones that cannot be
 * picked: a selector that silently omits them tells the traveller that HERE transit does not exist,
 * when the truth is that this server does not serve it.
 *
 * @property navigatorVersion What answered, or null when nothing did — which is also the reachability
 *   indicator, because loading the catalog is itself the proof that the navigator is there.
 * @property latencyMillis How long that took, when it worked.
 */
data class RoutingCatalog(
    val options: List<RoutingProfileOption>,
    val navigatorVersion: String? = null,
    val latencyMillis: Long? = null,
) {
    val isReachable: Boolean get() = navigatorVersion != null

    /** The profiles that can actually be picked, in the order they should be offered. */
    val selectable: List<RoutingProfileOption> get() = options.filter { it.isSelectable }
}
