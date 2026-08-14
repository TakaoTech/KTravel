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
 * traveller drives against a scheduled service, a walking route you ask for against the walking legs
 * of a journey you are told about. A common enum would spell each of them once and lose the
 * distinction; a mode that only ever travels attached to its profile cannot.
 */
data class RoutingMode(val id: String)

/**
 * How a profile lets its modes be chosen, which is not the same question for every API.
 *
 * The difference is upstream and real: HERE's road API takes a required `transportMode` with exactly
 * one value, while its transit API takes an optional set of modes that restricts which vehicles the
 * answer may use. Naming it here is what lets a selector render both without knowing which provider
 * it is drawing.
 */
enum class ModeSelection {
    /** Exactly one mode, always. */
    SINGLE,

    /** Any number of them, where none means no restriction rather than nothing. */
    FILTER,
}

/**
 * What a profile can do, as the app needs to know it.
 *
 * @property modes What may be asked for, in this profile's own vocabulary.
 * @property modesSupportingShortest The subset that can be optimized for distance rather than time.
 *   Upstream refuses the option on the others, so it is not offered rather than sent and rejected.
 * @property maxAlternatives How many routes may be asked for at once.
 * @property requiresApiKey Whether a route needs the traveller's own provider key.
 */
data class RoutingProfileInfo(
    val id: RoutingProfileId,
    val displayName: String,
    val modes: List<RoutingMode>,
    val modeSelection: ModeSelection,
    val modesSupportingShortest: Set<RoutingMode> = emptySet(),
    val supportsTolls: Boolean = false,
    val maxAlternatives: Int = 1,
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
