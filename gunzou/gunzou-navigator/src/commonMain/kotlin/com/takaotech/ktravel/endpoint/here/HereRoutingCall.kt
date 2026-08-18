package com.takaotech.ktravel.endpoint.here

import com.takaotech.navigator.api.here.HereRoutingRequest
import com.takaotech.navigator.api.here.HereTransportMode

/**
 * One call to the road profile: the vehicle, and everything else.
 *
 * The two halves arrive on different parts of the request — the mode is the last segment of the
 * path, the rest is the body — and this is what puts them back together before the endpoint sees
 * them. Carrying the mode in a wrapper rather than in [HereRoutingRequest] is what keeps the body a
 * faithful copy of the contract: the path already names the vehicle, and a field repeating it would
 * be a second answer to a question that has one.
 *
 * @property transportMode What the traveller is using, read from the path.
 * @property request What was asked for, read from the body.
 */
data class HereRoutingCall(val transportMode: HereTransportMode, val request: HereRoutingRequest)
