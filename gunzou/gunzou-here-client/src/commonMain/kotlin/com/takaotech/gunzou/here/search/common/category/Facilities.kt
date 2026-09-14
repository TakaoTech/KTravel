package com.takaotech.gunzou.here.search.common.category

/**
 * `800` Facilities: The Facilities category is a top level category for places associated with
 * specialized facilities, such as sports venues, government buildings, health care centers and
 * other types of facilities.
 */
object Facilities : PlaceCategoryGroup("800", "Facilities") {
    /**
     * `800-8000` Hospital or Health Care Facility: Facilities that include dental offices,
     * hospitals, nursing homes and other health care-related services.
     */
    object HospitalOrHealthCareFacility :
        PlaceCategorySubgroup("800-8000", "Hospital or Health Care Facility") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8000-0000` Hospital or Health Care Facility: An institution or facility that
         * provides medical or surgical treatment for the sick or injured. Places range from small
         * clinics and doctor's offices to urgent care centers and large hospitals containing
         * elaborate emergency rooms and trauma centers. This is a base-level category that should
         * be used for all places that do not fit other categories defined for Hospital or Health
         * Care Facility (800-8000-xxxx).
         */
        val HOSPITAL_OR_HEALTH_CARE_FACILITY: PlaceCategoryLeaf =
            leaf("800-8000-0000", "Hospital or Health Care Facility")

        /**
         * `800-8000-0154` Dentist-Dental Office: A health care facility that provides dental
         * services. Services generally include cleanings, exams, cosmetic dentistry, dentures,
         * braces, and other related services.
         */
        val DENTIST_DENTAL_OFFICE: PlaceCategoryLeaf =
            leaf("800-8000-0154", "Dentist-Dental Office")

        /**
         * `800-8000-0155` Family-General Practice Physicians: A health care facility office that
         * provides medical services to individual persons or families. Services generally include
         * medical advice, treatment of specific or acute illnesses, preventive care and other
         * related medical services.
         */
        val FAMILY_GENERAL_PRACTICE_PHYSICIANS: PlaceCategoryLeaf =
            leaf("800-8000-0155", "Family-General Practice Physicians")

        /**
         * `800-8000-0156` Psychiatric Institute: A health care facility that provides care and
         * treatment of patients affected with acute or chronic mental illness.
         */
        val PSYCHIATRIC_INSTITUTE: PlaceCategoryLeaf =
            leaf("800-8000-0156", "Psychiatric Institute")

        /**
         * `800-8000-0157` Nursing Home: A health care facility that provides full-time care and
         * medical treatment for the elderly, chronically ill, or other individuals who are unable
         * to take care of themselves.
         */
        val NURSING_HOME: PlaceCategoryLeaf = leaf("800-8000-0157", "Nursing Home")

        /**
         * `800-8000-0158` Medical Services-Clinics: A health care facility that provides general
         * medical services that are not directly associated or connected with a medical hospital.
         * Some places may specialize in particular conditions or areas of medicine.
         */
        val MEDICAL_SERVICES_CLINICS: PlaceCategoryLeaf =
            leaf("800-8000-0158", "Medical Services-Clinics")

        /**
         * `800-8000-0159` Hospital: An institution or facility that provides medical or surgical
         * treatment for the sick or injured. Some places may include elaborate emergency rooms and
         * trauma centers.
         */
        val HOSPITAL: PlaceCategoryLeaf = leaf("800-8000-0159", "Hospital")

        /**
         * `800-8000-0160` Urgent Care Center: A type of walk-in clinic focused on the delivery of
         * urgent ambulatory care in a dedicated medical facility outside of a traditional emergency
         * room (ER) located within a hospital. Urgent care centers primarily treat injuries or
         * illnesses requiring immediate care but not serious enough to require an ER visit.
         */
        val URGENT_CARE_CENTER: PlaceCategoryLeaf = leaf("800-8000-0160", "Urgent Care Center")

        /**
         * `800-8000-0161` Optical: A health care facility that sells eyeglasses, lenses, and other
         * optical instruments, and employs an optician.
         */
        val OPTICAL: PlaceCategoryLeaf = leaf("800-8000-0161", "Optical")

        /**
         * `800-8000-0162` Veterinarian: A health care facility that practices veterinary medicine
         * or surgery. Services generally include the prevention, cure, or alleviation of disease
         * and injury to animals, especially domestic animals. This category includes all named
         * veterinary locations, animal hospitals, and emergency veterinary services.
         */
        val VETERINARIAN: PlaceCategoryLeaf = leaf("800-8000-0162", "Veterinarian")

        /**
         * `800-8000-0325` Hospital Emergency Room: A health care facility that provides emergency
         * medical treatment for people. Places in this category may be part of a larger hospital
         * facility or a stand-alone facility.
         */
        val HOSPITAL_EMERGENCY_ROOM: PlaceCategoryLeaf =
            leaf("800-8000-0325", "Hospital Emergency Room")

        /**
         * `800-8000-0340` Therapist: A health care facility that offers a specific type of therapy
         * service, such as a psychologist, psychoanalyst, psychotherapist, psychiatrist or
         * counselor.
         */
        val THERAPIST: PlaceCategoryLeaf = leaf("800-8000-0340", "Therapist")

        /**
         * `800-8000-0341` Chiropractor: A health care facility that provides treatment for bodily
         * disorders through the manipulation of the spine and other body parts.
         */
        val CHIROPRACTOR: PlaceCategoryLeaf = leaf("800-8000-0341", "Chiropractor")

        /**
         * `800-8000-0367` Blood Bank: A medical service where blood is collected from donors,
         * typed, separated into components, stored, and prepared for transfusion into recipients.
         */
        val BLOOD_BANK: PlaceCategoryLeaf = leaf("800-8000-0367", "Blood Bank")

        /**
         * `800-8000-0400` COVID-19 Testing Site: A medical facility that provides screening and/or
         * testing for the detection of the virus which causes COVID-19.
         */
        val COVID_19_TESTING_SITE: PlaceCategoryLeaf =
            leaf("800-8000-0400", "COVID-19 Testing Site")

        /**
         * `800-8000-0401` Vaccination Site: A medical facility that provides vaccination for
         * immunity.
         */
        val VACCINATION_SITE: PlaceCategoryLeaf = leaf("800-8000-0401", "Vaccination Site")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                HOSPITAL_OR_HEALTH_CARE_FACILITY,
                DENTIST_DENTAL_OFFICE,
                FAMILY_GENERAL_PRACTICE_PHYSICIANS,
                PSYCHIATRIC_INSTITUTE,
                NURSING_HOME,
                MEDICAL_SERVICES_CLINICS,
                HOSPITAL,
                URGENT_CARE_CENTER,
                OPTICAL,
                VETERINARIAN,
                HOSPITAL_EMERGENCY_ROOM,
                THERAPIST,
                CHIROPRACTOR,
                BLOOD_BANK,
                COVID_19_TESTING_SITE,
                VACCINATION_SITE,
            )
        }
    }

    /** `800-8100` Government or Community Facility. */
    object GovernmentOrCommunityFacility :
        PlaceCategorySubgroup("800-8100", "Government or Community Facility") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8100-0000` Government or Community Facility: A Place where government employees work
         * and government services are provided. This is a base-level category that should be used
         * for all places that do not fit other categories defined for Government or Community
         * Facility (800-8100-xxxx).
         */
        val GOVERNMENT_OR_COMMUNITY_FACILITY: PlaceCategoryLeaf =
            leaf("800-8100-0000", "Government or Community Facility")

        /**
         * `800-8100-0163` City Hall: A government building that houses the seat of an incorporated
         * government for a town, borough, city, county, or other municipality.
         */
        val CITY_HALL: PlaceCategoryLeaf = leaf("800-8100-0163", "City Hall")

        /**
         * `800-8100-0164` Embassy: A government building that houses diplomatic representatives of
         * foreign countries.
         */
        val EMBASSY: PlaceCategoryLeaf = leaf("800-8100-0164", "Embassy")

        /**
         * `800-8100-0165` Military Base: A government facility that facilitates military training
         * and operations, and shelters military equipment and personnel. Military bases are
         * directly owned and operated by the military. For example, the Army, Navy, Air Force,
         * Marine, Coast Guard.
         */
        val MILITARY_BASE: PlaceCategoryLeaf = leaf("800-8100-0165", "Military Base")

        /**
         * `800-8100-0168` County Council: A government building where an elected administrative
         * body governs the respective county.
         */
        val COUNTY_COUNCIL: PlaceCategoryLeaf = leaf("800-8100-0168", "County Council")

        /**
         * `800-8100-0169` Civic-Community Center: A government building that provides space to the
         * public for community events, special events, seasonal programs, organized classes, as
         * well as a general space for rent.
         */
        val CIVIC_COMMUNITY_CENTER: PlaceCategoryLeaf =
            leaf("800-8100-0169", "Civic-Community Center")

        /**
         * `800-8100-0170` Courthouse: A government building that serves as the home for the local
         * court of law.
         */
        val COURTHOUSE: PlaceCategoryLeaf = leaf("800-8100-0170", "Courthouse")

        /**
         * `800-8100-0171` Government Office: A government building that often includes federal
         * offices, municipality offices, state offices, provincial offices, ministries, and other
         * governmental departments. Examples include the Department of Finance or the Ministry of
         * Agriculture and Water.
         */
        val GOVERNMENT_OFFICE: PlaceCategoryLeaf = leaf("800-8100-0171", "Government Office")

        /**
         * `800-8100-0172` Border Crossing: A controlled junction where travellers or goods are
         * inspected upon entry or exit of an area or region. Border crossings are generally located
         * between two countries.
         */
        val BORDER_CROSSING: PlaceCategoryLeaf = leaf("800-8100-0172", "Border Crossing")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                GOVERNMENT_OR_COMMUNITY_FACILITY,
                CITY_HALL,
                EMBASSY,
                MILITARY_BASE,
                COUNTY_COUNCIL,
                CIVIC_COMMUNITY_CENTER,
                COURTHOUSE,
                GOVERNMENT_OFFICE,
                BORDER_CROSSING,
            )
        }
    }

    /** `800-8200` Continuing Education. */
    object ContinuingEducation : PlaceCategorySubgroup("800-8200", "Continuing Education") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8200-0000` Education Facility: An institution that provides basic/elementary or
         * secondary education. This is a base-level category that should be used for all places
         * that do not fit other categories defined for Education Facility (800-8200-xxxx).
         */
        val EDUCATION_FACILITY: PlaceCategoryLeaf = leaf("800-8200-0000", "Education Facility")

        /**
         * `800-8200-0173` Higher Education: An institution that provides post-secondary, tertiary
         * or third level education. Higher education includes educational institutions that serve
         * students beyond secondary education, such as a college, university, technical institution
         * or trade school.
         */
        val HIGHER_EDUCATION: PlaceCategoryLeaf = leaf("800-8200-0173", "Higher Education")

        /**
         * `800-8200-0295` Training and Development: An institution or business that provides
         * education and training, such as development courses, second languages, presentation
         * skills, technical skills and other related education services.
         */
        val TRAINING_AND_DEVELOPMENT: PlaceCategoryLeaf =
            leaf("800-8200-0295", "Training and Development")

        /**
         * `800-8200-0360` Coaching Institute: A training center where individuals are "coached" on
         * various topics. For example, preparation for competitive exams, preparation for
         * government jobs or preparation for admission into reputed universities.
         */
        val COACHING_INSTITUTE: PlaceCategoryLeaf = leaf("800-8200-0360", "Coaching Institute")

        /**
         * `800-8200-0361` Fine Arts: A training center where individuals learn an aspect of fine
         * arts, such as painting, photography, tap dance, ballet and more.
         */
        val FINE_ARTS: PlaceCategoryLeaf = leaf("800-8200-0361", "Fine Arts")

        /**
         * `800-8200-0362` Language Studies: A training center where individuals learn a particular
         * language, such as German, Spanish, English and others.
         */
        val LANGUAGE_STUDIES: PlaceCategoryLeaf = leaf("800-8200-0362", "Language Studies")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                EDUCATION_FACILITY,
                HIGHER_EDUCATION,
                TRAINING_AND_DEVELOPMENT,
                COACHING_INSTITUTE,
                FINE_ARTS,
                LANGUAGE_STUDIES,
            )
        }
    }

    /** `800-8250` School: Facilities that provide basic education. */
    object School : PlaceCategorySubgroup("800-8250", "School") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8250-0000` School: This is a base-level category that can be used for places that
         * may not fit appropriately within other categories within School (800-8250-xxxx).
         */
        val SCHOOL: PlaceCategoryLeaf = leaf("800-8250-0000", "School")

        /**
         * `800-8250-0287` Primary School: An institution that provides basic elementary education
         * to children.
         */
        val PRIMARY_SCHOOL: PlaceCategoryLeaf = leaf("800-8250-0287", "Primary School")

        /**
         * `800-8250-0288` Secondary School: An institution that provides secondary education to
         * teenagers.
         */
        val SECONDARY_SCHOOL: PlaceCategoryLeaf = leaf("800-8250-0288", "Secondary School")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                SCHOOL,
                PRIMARY_SCHOOL,
                SECONDARY_SCHOOL,
            )
        }
    }

    /**
     * `800-8300` Library: Facilities that offer books, periodicals, audio, video and other material
     * for public use.
     */
    object Library : PlaceCategorySubgroup("800-8300", "Library") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8300-0000` Other Library: A facility that offers books, periodicals, audio, video
         * and other material for public use (but not sale). Material can be used on-premise or
         * borrowed for an extended period. This is a base-level category that should be used for
         * all places that do not fit other categories defined for Library (800-8300-xxxx).
         */
        val OTHER_LIBRARY: PlaceCategoryLeaf = leaf("800-8300-0000", "Other Library")

        /**
         * `800-8300-0175` Library: A place in which literary, musical, artistic, or reference
         * materials are kept for use and circulation, but are not for sale.
         */
        val LIBRARY: PlaceCategoryLeaf = leaf("800-8300-0175", "Library")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                OTHER_LIBRARY,
                LIBRARY,
            )
        }
    }

    /**
     * `800-8400` Event Spaces: An area or facility used for the hosting of fairs and conventions.
     */
    object EventSpaces : PlaceCategorySubgroup("800-8400", "Event Spaces") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8400-0000` Event Spaces: A facility that provides space for large events such as
         * trade shows, trade fairs, or conventions. This is a base-level category that should be
         * used for all places that do not fit other categories defined for Fair-Convention Facility
         * (800-8400-xxxx).
         */
        val EVENT_SPACES: PlaceCategoryLeaf = leaf("800-8400-0000", "Event Spaces")

        /**
         * `800-8400-0139` Banquet Hall: An establishment that offers short-term rental of a room or
         * building for the purposes of hosting a party, banquet, reception or other social event.
         * Banquet halls are frequently co-located in other establishments, such as clubs, hotels
         * and restaurants.
         */
        val BANQUET_HALL: PlaceCategoryLeaf = leaf("800-8400-0139", "Banquet Hall")

        /**
         * `800-8400-0176` Convention-Exhibition Center: A facility that provides space for large
         * events such as trade shows, trade fairs, or conventions.
         */
        val CONVENTION_EXHIBITION_CENTER: PlaceCategoryLeaf =
            leaf("800-8400-0176", "Convention-Exhibition Center")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                EVENT_SPACES,
                BANQUET_HALL,
                CONVENTION_EXHIBITION_CENTER,
            )
        }
    }

    /** `800-8500` Parking: Area or building used for parking cars. */
    object Parking : PlaceCategorySubgroup("800-8500", "Parking") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8500-0000` Parking: An area used to park motor vehicles. This is a base-level
         * category that should be used for all places that do not fit other categories defined for
         * Parking Facility (800-8500-xxxx).
         */
        val PARKING: PlaceCategoryLeaf = leaf("800-8500-0000", "Parking")

        /**
         * `800-8500-0177` Parking Garage-Parking House: A designated parking area that is covered
         * or enclosed.
         */
        val PARKING_GARAGE_PARKING_HOUSE: PlaceCategoryLeaf =
            leaf("800-8500-0177", "Parking Garage-Parking House")

        /**
         * `800-8500-0178` Parking Lot: A designated parking area that is not covered or enclosed.
         */
        val PARKING_LOT: PlaceCategoryLeaf = leaf("800-8500-0178", "Parking Lot")

        /**
         * `800-8500-0179` Park and Ride: A designated parking area where commuters can park their
         * vehicles to join a carpool, bus or other form of transportation. Fees generally do not
         * apply.
         */
        val PARK_AND_RIDE: PlaceCategoryLeaf = leaf("800-8500-0179", "Park and Ride")

        /**
         * `800-8500-0200` Motorcycle, Moped and Scooter Parking: A designated parking area that is
         * specifically for motorcycles, mopeds or scooters.
         */
        val MOTORCYCLE_MOPED_AND_SCOOTER_PARKING: PlaceCategoryLeaf =
            leaf("800-8500-0200", "Motorcycle, Moped and Scooter Parking")

        /**
         * `800-8500-0315` Cellphone Parking Lot: A designated parking area for individuals who need
         * to make mobile phone calls without posing a risk to other motorists on the road.
         */
        val CELLPHONE_PARKING_LOT: PlaceCategoryLeaf =
            leaf("800-8500-0315", "Cellphone Parking Lot")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                PARKING,
                PARKING_GARAGE_PARKING_HOUSE,
                PARKING_LOT,
                PARK_AND_RIDE,
                MOTORCYCLE_MOPED_AND_SCOOTER_PARKING,
                CELLPHONE_PARKING_LOT,
            )
        }
    }

    /**
     * `800-8600` Sports Facility-Venue: A facility used for individual and team sports including
     * recreational sports.
     */
    object SportsFacilityVenue : PlaceCategorySubgroup("800-8600", "Sports Facility-Venue") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8600-0000` Sports Facility-Venue: A facility where sporting events are held. This is
         * a base-level category that should be used for all places that do not fit other categories
         * defined for Sports Facility-Venue (800-8600-xxxx).
         */
        val SPORTS_FACILITY_VENUE: PlaceCategoryLeaf =
            leaf("800-8600-0000", "Sports Facility-Venue")

        /**
         * `800-8600-0180` Sports Complex-Stadium: A facility that provides multi-use indoor or
         * outdoor arenas where sporting events are played and viewed.
         */
        val SPORTS_COMPLEX_STADIUM: PlaceCategoryLeaf =
            leaf("800-8600-0180", "Sports Complex-Stadium")

        /**
         * `800-8600-0181` Ice Skating Rink: A facility that provides a surface where all types of
         * ice skating are played.
         */
        val ICE_SKATING_RINK: PlaceCategoryLeaf = leaf("800-8600-0181", "Ice Skating Rink")

        /**
         * `800-8600-0182` Swimming Pool: A facility that provides a tank or basin filled with water
         * for swimming.
         */
        val SWIMMING_POOL: PlaceCategoryLeaf = leaf("800-8600-0182", "Swimming Pool")

        /**
         * `800-8600-0183` Tennis Court: A facility that provides a court upon which tennis is
         * played.
         */
        val TENNIS_COURT: PlaceCategoryLeaf = leaf("800-8600-0183", "Tennis Court")

        /**
         * `800-8600-0184` Bowling Center: An indoor facility containing alleys or lanes designed
         * for bowling.
         */
        val BOWLING_CENTER: PlaceCategoryLeaf = leaf("800-8600-0184", "Bowling Center")

        /**
         * `800-8600-0185` Indoor Ski: A facility that provides an indoor area designed for ski
         * activities.
         */
        val INDOOR_SKI: PlaceCategoryLeaf = leaf("800-8600-0185", "Indoor Ski")

        /**
         * `800-8600-0186` Hockey: A facility that provides a rink used to play hockey and has
         * markings on the playing surface to determine it as such.
         */
        val HOCKEY: PlaceCategoryLeaf = leaf("800-8600-0186", "Hockey")

        /**
         * `800-8600-0187` Racquetball Court: A facility that provides a court designed for playing
         * racquetball.
         */
        val RACQUETBALL_COURT: PlaceCategoryLeaf = leaf("800-8600-0187", "Racquetball Court")

        /**
         * `800-8600-0188` Shooting Range: A facility that provides an enclosed firing range with
         * targets for firearm practice.
         */
        val SHOOTING_RANGE: PlaceCategoryLeaf = leaf("800-8600-0188", "Shooting Range")

        /** `800-8600-0189` Soccer Club: A facility that provides a field used to play soccer. */
        val SOCCER_CLUB: PlaceCategoryLeaf = leaf("800-8600-0189", "Soccer Club")

        /**
         * `800-8600-0190` Squash Court: A facility that provides an indoor court used to play
         * squash.
         */
        val SQUASH_COURT: PlaceCategoryLeaf = leaf("800-8600-0190", "Squash Court")

        /**
         * `800-8600-0191` Fitness-Health Club: A facility that offers classes, instruction and
         * equipment for exercising and physical conditioning. Membership fees are generally
         * required.
         */
        val FITNESS_HEALTH_CLUB: PlaceCategoryLeaf = leaf("800-8600-0191", "Fitness-Health Club")

        /**
         * `800-8600-0192` Indoor Sports: A facility that provides an area designed to play a
         * variety of indoor sports, such as basketball, volleyball, cricket, gymnastics, table
         * tennis, handball and other sports.
         */
        val INDOOR_SPORTS: PlaceCategoryLeaf = leaf("800-8600-0192", "Indoor Sports")

        /**
         * `800-8600-0193` Golf Course: An area ground or course upon which golf is played. Course
         * usually contain 9 or 18 holes. These places may be public or private.
         */
        val GOLF_COURSE: PlaceCategoryLeaf = leaf("800-8600-0193", "Golf Course")

        /**
         * `800-8600-0194` Golf Practice Range: A area that intended for practicing long golf shots
         * with balls provided by the facility. Fees generally apply.
         */
        val GOLF_PRACTICE_RANGE: PlaceCategoryLeaf = leaf("800-8600-0194", "Golf Practice Range")

        /**
         * `800-8600-0195` Race Track: A facility that provides a dirt, grass or paved track
         * designed for racing animals (horses or greyhounds), vehicles, or athletes. Race tracks
         * often feature grandstands or concourses.
         */
        val RACE_TRACK: PlaceCategoryLeaf = leaf("800-8600-0195", "Race Track")

        /**
         * `800-8600-0196` Sporting Instruction and Camps: A facility that provides instruction and
         * training for a particular sport. This also includes camps that provide recreational
         * activities or sports.
         */
        val SPORTING_INSTRUCTION_AND_CAMPS: PlaceCategoryLeaf =
            leaf("800-8600-0196", "Sporting Instruction and Camps")

        /**
         * `800-8600-0197` Sports Activities: A facility or designated area that where recreational
         * or amateur sports are played.
         */
        val SPORTS_ACTIVITIES: PlaceCategoryLeaf = leaf("800-8600-0197", "Sports Activities")

        /** `800-8600-0199` Basketball: A facility designed specifically for playing basketball. */
        val BASKETBALL: PlaceCategoryLeaf = leaf("800-8600-0199", "Basketball")

        /** `800-8600-0200` Badminton: A facility designed specifically for playing badminton. */
        val BADMINTON: PlaceCategoryLeaf = leaf("800-8600-0200", "Badminton")

        /** `800-8600-0314` Rugby: A facility designed specifically for playing rugby. */
        val RUGBY: PlaceCategoryLeaf = leaf("800-8600-0314", "Rugby")

        /**
         * `800-8600-0316` Diving Center: A facility that provides classes, instruction and
         * equipment for learning or practicing scuba diving. Often these facilities are located
         * near to natural bodies of water where diving occurs, or they may contain indoor pools
         * designed for training.
         */
        val DIVING_CENTER: PlaceCategoryLeaf = leaf("800-8600-0316", "Diving Center")

        /**
         * `800-8600-0376` Bike Park: A facility that provides trails maintained for mountain bike
         * riding.
         */
        val BIKE_PARK: PlaceCategoryLeaf = leaf("800-8600-0376", "Bike Park")

        /** `800-8600-0377` BMX Track: A facility designed specifically for racing a BMX bicycle. */
        val BMX_TRACK: PlaceCategoryLeaf = leaf("800-8600-0377", "BMX Track")

        /**
         * `800-8600-0381` Running Track: Common features of running tracks are: an artificial
         * surface (usually rubberized), which is usually built to a specific length and lane width.
         */
        val RUNNING_TRACK: PlaceCategoryLeaf = leaf("800-8600-0381", "Running Track")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                SPORTS_FACILITY_VENUE,
                SPORTS_COMPLEX_STADIUM,
                ICE_SKATING_RINK,
                SWIMMING_POOL,
                TENNIS_COURT,
                BOWLING_CENTER,
                INDOOR_SKI,
                HOCKEY,
                RACQUETBALL_COURT,
                SHOOTING_RANGE,
                SOCCER_CLUB,
                SQUASH_COURT,
                FITNESS_HEALTH_CLUB,
                INDOOR_SPORTS,
                GOLF_COURSE,
                GOLF_PRACTICE_RANGE,
                RACE_TRACK,
                SPORTING_INSTRUCTION_AND_CAMPS,
                SPORTS_ACTIVITIES,
                BASKETBALL,
                BADMINTON,
                RUGBY,
                DIVING_CENTER,
                BIKE_PARK,
                BMX_TRACK,
                RUNNING_TRACK,
            )
        }
    }

    /**
     * `800-8700` Facilities: Facilities with miscellaneous uses such as Clubhouses, Offices, and
     * Registration Offices. Named Facilities by HERE, renamed here so that it does not shadow its
     * group.
     */
    object OtherFacilities : PlaceCategorySubgroup("800-8700", "Facilities") {
        override val group: PlaceCategoryGroup get() = Facilities

        /**
         * `800-8700-0000` Facilities: A structure designed or built to serve a specific function,
         * activity, or service. This is a base-level category that should be used for all places
         * that do not fit other categories defined for Facilities (800-8700-xxxx).
         */
        val FACILITIES: PlaceCategoryLeaf = leaf("800-8700-0000", "Facilities")

        /**
         * `800-8700-0166` Cemetery: A designated area containing graves, tombs, or funeral urns.
         * This category includes churchyards, burial grounds and graveyards.
         */
        val CEMETERY: PlaceCategoryLeaf = leaf("800-8700-0166", "Cemetery")

        /** `800-8700-0167` Crematorium: A facility where human remains are incinerated. */
        val CREMATORIUM: PlaceCategoryLeaf = leaf("800-8700-0167", "Crematorium")

        /**
         * `800-8700-0198` Public Restroom-Toilets: A room or public building that contains
         * lavatories and washing facilities.
         */
        val PUBLIC_RESTROOM_TOILETS: PlaceCategoryLeaf =
            leaf("800-8700-0198", "Public Restroom-Toilets")

        /**
         * `800-8700-0296` Clubhouse: A facility that provides social or recreational activities.
         * Clubhouses are often found at apartment complexes, golf courses and other social clubs.
         */
        val CLUBHOUSE: PlaceCategoryLeaf = leaf("800-8700-0296", "Clubhouse")

        /**
         * `800-8700-0298` Registration Office: A facility engaged in various types of
         * registrations, such as vehicles, houses and companies.
         */
        val REGISTRATION_OFFICE: PlaceCategoryLeaf = leaf("800-8700-0298", "Registration Office")

        /**
         * `800-8700-0333` Student Housing: A facility typically used for housing students, and is
         * most common for university/college students. These may be located on or off campus, and
         * are commonly referred to as dorms (dormitories).
         */
        val STUDENT_HOUSING: PlaceCategoryLeaf = leaf("800-8700-0333", "Student Housing")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                FACILITIES,
                CEMETERY,
                CREMATORIUM,
                PUBLIC_RESTROOM_TOILETS,
                CLUBHOUSE,
                REGISTRATION_OFFICE,
                STUDENT_HOUSING,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            HospitalOrHealthCareFacility,
            GovernmentOrCommunityFacility,
            ContinuingEducation,
            School,
            Library,
            EventSpaces,
            Parking,
            SportsFacilityVenue,
            OtherFacilities,
        )
    }
}
