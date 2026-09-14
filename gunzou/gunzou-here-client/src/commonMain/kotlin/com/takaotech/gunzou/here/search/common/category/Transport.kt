package com.takaotech.gunzou.here.search.common.category

/**
 * `400` Transport: The Transport category is a top level category for places commonly associated
 * with pedestrian and cargo transport facilities, including airports, rail yards and seaports.
 */
object Transport : PlaceCategoryGroup("400", "Transport") {
    /**
     * `400-4000` Airport: A designated area that serves various aspects of aviation related sports,
     * including gliders, recreational aircraft and model airplanes.
     */
    object Airport : PlaceCategorySubgroup("400-4000", "Airport") {
        override val group: PlaceCategoryGroup get() = Transport

        /**
         * `400-4000-4580` Public Sports Airport: A designated area that serves various aspects of
         * aviation related sports, including gliders, recreational aircraft and model airplanes.
         */
        val PUBLIC_SPORTS_AIRPORT: PlaceCategoryLeaf =
            leaf("400-4000-4580", "Public Sports Airport")

        /**
         * `400-4000-4581` Airport: A designated area for the landing and takeoff of aircraft, as
         * well as the loading and unloading of cargo and passengers.
         */
        val AIRPORT: PlaceCategoryLeaf = leaf("400-4000-4581", "Airport")

        /**
         * `400-4000-4582` Airport Terminal: A facility where passengers are allowed to board and
         * disembark from aircraft. Airport terminals are generally comprised of various
         * establishments, such as retail stores and restaurants. These places share common services
         * such as parking or utilities.
         */
        val AIRPORT_TERMINAL: PlaceCategoryLeaf = leaf("400-4000-4582", "Airport Terminal")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                PUBLIC_SPORTS_AIRPORT,
                AIRPORT,
                AIRPORT_TERMINAL,
            )
        }
    }

    /**
     * `400-4100` Public Transport: A facility for travelers who are travelling between stops on
     * public transport.
     */
    object PublicTransport : PlaceCategorySubgroup("400-4100", "Public Transport") {
        override val group: PlaceCategoryGroup get() = Transport

        /**
         * `400-4100-0035` Train Station: A hub for travelers who are travelling between stops along
         * a railway network.
         */
        val TRAIN_STATION: PlaceCategoryLeaf = leaf("400-4100-0035", "Train Station")

        /**
         * `400-4100-0036` Bus Station: A facility that serves as a hub for inter-city bus service.
         */
        val BUS_STATION: PlaceCategoryLeaf = leaf("400-4100-0036", "Bus Station")

        /**
         * `400-4100-0037` Underground Train-Subway: A facility that provides access to an
         * underground rail transit system.
         */
        val UNDERGROUND_TRAIN_SUBWAY: PlaceCategoryLeaf =
            leaf("400-4100-0037", "Underground Train-Subway")

        /**
         * `400-4100-0038` Commuter Rail Station: A facility that provides access to regional or
         * intra-city rail transportation service.
         */
        val COMMUTER_RAIL_STATION: PlaceCategoryLeaf =
            leaf("400-4100-0038", "Commuter Rail Station")

        /**
         * `400-4100-0039` Commuter Train: A facility that provides access to above-ground rail
         * transit systems.
         */
        val COMMUTER_TRAIN: PlaceCategoryLeaf = leaf("400-4100-0039", "Commuter Train")

        /** `400-4100-0040` Public Transit Access: An entrance or exit to public transit service. */
        val PUBLIC_TRANSIT_ACCESS: PlaceCategoryLeaf =
            leaf("400-4100-0040", "Public Transit Access")

        /**
         * `400-4100-0041` Transportation Service: An establishment that provides charter air
         * travel, limousine or taxi services.
         */
        val TRANSPORTATION_SERVICE: PlaceCategoryLeaf =
            leaf("400-4100-0041", "Transportation Service")

        /**
         * `400-4100-0042` Bus Stop: Location offering access to intercity and urban public transit
         * lines, including trolley buses.
         */
        val BUS_STOP: PlaceCategoryLeaf = leaf("400-4100-0042", "Bus Stop")

        /**
         * `400-4100-0043` Local Transit: A facility that provides access to regional, intra-city or
         * local transportation through the use of buses, subways and elevated trains. These places
         * are generally run by local governments or commercial services.
         */
        val LOCAL_TRANSIT: PlaceCategoryLeaf = leaf("400-4100-0043", "Local Transit")

        /**
         * `400-4100-0044` Ferry Terminal: A terminal that provides ferry services for transporting
         * passengers and automotive vehicles by water.
         */
        val FERRY_TERMINAL: PlaceCategoryLeaf = leaf("400-4100-0044", "Ferry Terminal")

        /** `400-4100-0045` Boat Ferry: A terminal that provides access to boat ferry services. */
        val BOAT_FERRY: PlaceCategoryLeaf = leaf("400-4100-0045", "Boat Ferry")

        /** `400-4100-0046` Rail Ferry: A terminal that provides access to rail ferry services. */
        val RAIL_FERRY: PlaceCategoryLeaf = leaf("400-4100-0046", "Rail Ferry")

        /**
         * `400-4100-0047` Taxi Stand: A designated queuing, loading and unloading area for taxis.
         * Places in this category are usually in city centers and buildings with a high volume of
         * pedestrians.
         */
        val TAXI_STAND: PlaceCategoryLeaf = leaf("400-4100-0047", "Taxi Stand")

        /**
         * `400-4100-0051` Rideshare Pickup: A site or designated area for the pickup of passengers
         * by a rideshare driver. Common rideshare companies include Lyft, Uber, Didi, and Ola.
         */
        val RIDESHARE_PICKUP: PlaceCategoryLeaf = leaf("400-4100-0051", "Rideshare Pickup")

        /**
         * `400-4100-0225` Highway Entrance: A public transport feature that represents the entrance
         * along a motorway, preceding an entrance ramp.
         */
        val HIGHWAY_ENTRANCE: PlaceCategoryLeaf = leaf("400-4100-0225", "Highway Entrance")

        /** `400-4100-0226` Highway Exit: A marked ramp or spur providing egress from a motorway. */
        val HIGHWAY_EXIT: PlaceCategoryLeaf = leaf("400-4100-0226", "Highway Exit")

        /**
         * `400-4100-0326` Tollbooth: A structure along a motorway or thoroughfare where a fee is
         * paid in exchange for roadway usage. Tollbooths include areas where an automobile must
         * physically stop to pay a fee.
         */
        val TOLLBOOTH: PlaceCategoryLeaf = leaf("400-4100-0326", "Tollbooth")

        /**
         * `400-4100-0337` Lightrail: This includes rail trolleys and streetcar lines, AKA Tram in
         * Europe.
         */
        val LIGHTRAIL: PlaceCategoryLeaf = leaf("400-4100-0337", "Lightrail")

        /**
         * `400-4100-0338` Water Transit: Boat systems that provide transit service within an urban
         * area. It is used in Ped routing only and doesn't support vehicle routing.
         */
        val WATER_TRANSIT: PlaceCategoryLeaf = leaf("400-4100-0338", "Water Transit")

        /**
         * `400-4100-0339` Monorail: Rail systems that operate on a single beam. Monorail operation
         * is distinct from other trasnsportation types or traffic.
         */
        val MONORAIL: PlaceCategoryLeaf = leaf("400-4100-0339", "Monorail")

        /**
         * `400-4100-0340` Aerial Tramway: Passenger vehicles suspended from a system of aerial
         * cables and propelled by separate cables attached to the vehicle suspension system. The
         * cable system is powered by engines or motors at a central location not on board the
         * vehicle (for example, teleferic).
         */
        val AERIAL_TRAMWAY: PlaceCategoryLeaf = leaf("400-4100-0340", "Aerial Tramway")

        /**
         * `400-4100-0341` Bus Rapid Transit: These bus systems come in a variety of forms, from
         * dedicated bus ways that have their own right-of-way, to bus services that utilize HOV
         * lanes and dedicated freeway lanes, to limited-stop buses on pre-existing routes.
         */
        val BUS_RAPID_TRANSIT: PlaceCategoryLeaf = leaf("400-4100-0341", "Bus Rapid Transit")

        /**
         * `400-4100-0342` Inclined Rail: Special tramway type of vehicles operating up and down
         * slopes on rails via a cable mechanism so that passenger seats remain horizontal while the
         * undercarriage (truck) is angled parallel to the slope. It is often referred to as
         * funicular.
         */
        val INCLINED_RAIL: PlaceCategoryLeaf = leaf("400-4100-0342", "Inclined Rail")

        /**
         * `400-4100-0347` Bicycle Sharing Location: A designated area that provides self-service
         * docking stations for bicycles.
         */
        val BICYCLE_SHARING_LOCATION: PlaceCategoryLeaf =
            leaf("400-4100-0347", "Bicycle Sharing Location")

        /** `400-4100-0348` Bicycle Parking: A designated area for parking bicycles. */
        val BICYCLE_PARKING: PlaceCategoryLeaf = leaf("400-4100-0348", "Bicycle Parking")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                TRAIN_STATION,
                BUS_STATION,
                UNDERGROUND_TRAIN_SUBWAY,
                COMMUTER_RAIL_STATION,
                COMMUTER_TRAIN,
                PUBLIC_TRANSIT_ACCESS,
                TRANSPORTATION_SERVICE,
                BUS_STOP,
                LOCAL_TRANSIT,
                FERRY_TERMINAL,
                BOAT_FERRY,
                RAIL_FERRY,
                TAXI_STAND,
                RIDESHARE_PICKUP,
                HIGHWAY_ENTRANCE,
                HIGHWAY_EXIT,
                TOLLBOOTH,
                LIGHTRAIL,
                WATER_TRANSIT,
                MONORAIL,
                AERIAL_TRAMWAY,
                BUS_RAPID_TRANSIT,
                INCLINED_RAIL,
                BICYCLE_SHARING_LOCATION,
                BICYCLE_PARKING,
            )
        }
    }

    /**
     * `400-4200` Cargo Transportation: A facility that handles some aspect of the transportation of
     * cargo freight.
     */
    object CargoTransportation : PlaceCategorySubgroup("400-4200", "Cargo Transportation") {
        override val group: PlaceCategoryGroup get() = Transport

        /**
         * `400-4200-0048` Weigh Station: A facility adjacent to a roadway that provides weight
         * services to ensure vehicles are compliant with relevant laws and regulations (weight,
         * logs, and so on).
         */
        val WEIGH_STATION: PlaceCategoryLeaf = leaf("400-4200-0048", "Weigh Station")

        /**
         * `400-4200-0049` Cargo Center: A facility where cargo is transferred between different
         * modes of transportation. For example, ship-to-rail, ship-to-truck, air-to-truck and other
         * modes of transportation.
         */
        val CARGO_CENTER: PlaceCategoryLeaf = leaf("400-4200-0049", "Cargo Center")

        /** `400-4200-0050` Rail Yard: A hub for freight trains where cargo is transferred. */
        val RAIL_YARD: PlaceCategoryLeaf = leaf("400-4200-0050", "Rail Yard")

        /**
         * `400-4200-0051` Seaport-Harbour: A port where cargo ships dock to transfer cargo/freight.
         */
        val SEAPORT_HARBOUR: PlaceCategoryLeaf = leaf("400-4200-0051", "Seaport-Harbour")

        /**
         * `400-4200-0052` Airport Cargo: An airport facility where cargo/freight is transferred.
         */
        val AIRPORT_CARGO: PlaceCategoryLeaf = leaf("400-4200-0052", "Airport Cargo")

        /**
         * `400-4200-0240` Couriers: A facility that provides air, land, or combined mode courier
         * and express delivery services. This includes local messenger/delivery services, but
         * excludes government managed package and parcel delivery services.
         */
        val COURIERS: PlaceCategoryLeaf = leaf("400-4200-0240", "Couriers")

        /**
         * `400-4200-0241` Cargo Transportation: A facility that provides logistical support
         * services, but does not fit into any other Transport (400-4200-xxxx) categories. For
         * example, freight forwarders, marine shipping agents and customs brokers.
         */
        val CARGO_TRANSPORTATION: PlaceCategoryLeaf = leaf("400-4200-0241", "Cargo Transportation")

        /**
         * `400-4200-0311` Delivery Entrance: A designated building or parking lot entrance that is
         * specifically designated for deliveries.
         */
        val DELIVERY_ENTRANCE: PlaceCategoryLeaf = leaf("400-4200-0311", "Delivery Entrance")

        /**
         * `400-4200-0312` Loading Dock: A designated area where deliverers can load or unload
         * cargo/freight.
         */
        val LOADING_DOCK: PlaceCategoryLeaf = leaf("400-4200-0312", "Loading Dock")

        /**
         * `400-4200-0313` Loading Zone: A designated area where delivery vehicles may park for a
         * limited time to load or unload cargo/freight.
         */
        val LOADING_ZONE: PlaceCategoryLeaf = leaf("400-4200-0313", "Loading Zone")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                WEIGH_STATION,
                CARGO_CENTER,
                RAIL_YARD,
                SEAPORT_HARBOUR,
                AIRPORT_CARGO,
                COURIERS,
                CARGO_TRANSPORTATION,
                DELIVERY_ENTRANCE,
                LOADING_DOCK,
                LOADING_ZONE,
            )
        }
    }

    /**
     * `400-4300` Rest Area: An establishment along a motorway (controlled access road) that
     * provides restrooms and parking.
     */
    object RestArea : PlaceCategorySubgroup("400-4300", "Rest Area") {
        override val group: PlaceCategoryGroup get() = Transport

        /**
         * `400-4300-0000` Rest Area: An establishment along a motorway (controlled access road)
         * that provides restrooms and parking. This is a base-level category that should be used
         * for all places that do not fit other categories defined for Rest Area (400-4300-xxxx).
         */
        val REST_AREA: PlaceCategoryLeaf = leaf("400-4300-0000", "Rest Area")

        /**
         * `400-4300-0199` Complete Rest Area: A rest area that provides restroom facilities,
         * parking, snacks, an open space, and is usually along a major highway.
         */
        val COMPLETE_REST_AREA: PlaceCategoryLeaf = leaf("400-4300-0199", "Complete Rest Area")

        /**
         * `400-4300-0200` Parking and Restroom Only Rest Area: An area that provides both parking
         * and restrooms.
         */
        val PARKING_AND_RESTROOM_ONLY_REST_AREA: PlaceCategoryLeaf =
            leaf("400-4300-0200", "Parking and Restroom Only Rest Area")

        /**
         * `400-4300-0201` Parking Only Rest Area: An area that provides only parking. This type of
         * Place does not provide any facilities such as restaurant, petrol station or restrooms.
         */
        val PARKING_ONLY_REST_AREA: PlaceCategoryLeaf =
            leaf("400-4300-0201", "Parking Only Rest Area")

        /**
         * `400-4300-0202` Motorway Service Rest Area: An area located adjacent to a motorway where
         * motorists can refuel, rest and buy refreshments.
         */
        val MOTORWAY_SERVICE_REST_AREA: PlaceCategoryLeaf =
            leaf("400-4300-0202", "Motorway Service Rest Area")

        /**
         * `400-4300-0251` Roadside Station: A roadside facility in Japan that consists of
         * restaurants, shops and restrooms. It is certified by Japan's MLIT (Ministry of Land,
         * Infrastructure, Transport and Tourism) and is located along roads and highways. Note:
         * This category is a Japan-only category.
         */
        val ROADSIDE_STATION: PlaceCategoryLeaf = leaf("400-4300-0251", "Roadside Station")

        /**
         * `400-4300-0308` Scenic Overlook Rest Area: An area located along a roadway that provides
         * a place for drivers to observe the scenery. Scenic overlooks generally have regional
         * importance.
         */
        val SCENIC_OVERLOOK_REST_AREA: PlaceCategoryLeaf =
            leaf("400-4300-0308", "Scenic Overlook Rest Area")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                REST_AREA,
                COMPLETE_REST_AREA,
                PARKING_AND_RESTROOM_ONLY_REST_AREA,
                PARKING_ONLY_REST_AREA,
                MOTORWAY_SERVICE_REST_AREA,
                ROADSIDE_STATION,
                SCENIC_OVERLOOK_REST_AREA,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            Airport,
            PublicTransport,
            CargoTransportation,
            RestArea,
        )
    }
}
