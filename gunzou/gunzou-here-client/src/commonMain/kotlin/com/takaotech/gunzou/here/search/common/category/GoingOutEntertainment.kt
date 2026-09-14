package com.takaotech.gunzou.here.search.common.category

/**
 * `200` Going Out-Entertainment: The Going Out-Entertainment category is a top level category for
 * places commonly associated with entertainment, such as bars, cinemas, theatres, casinos and
 * nightclubs.
 */
object GoingOutEntertainment : PlaceCategoryGroup("200", "Going Out-Entertainment") {
    /**
     * `200-2000` Nightlife-Entertainment: An establishment that provides evening entertainment and
     * usually serves alcoholic beverages.
     */
    object NightlifeEntertainment : PlaceCategorySubgroup("200-2000", "Nightlife-Entertainment") {
        override val group: PlaceCategoryGroup get() = GoingOutEntertainment

        /**
         * `200-2000-0000` Nightlife-Entertainment: An establishment that provides evening
         * entertainment and usually serves alcoholic beverages. This is a base-level category that
         * should be used for all places that do not fit other categories defined
         * forNightlife-Entertainment (200-2000-xxxx).
         */
        val NIGHTLIFE_ENTERTAINMENT: PlaceCategoryLeaf =
            leaf("200-2000-0000", "Nightlife-Entertainment")

        /**
         * `200-2000-0011` Bar or Pub: An establishment that sells alcoholic beverages, such as a
         * public house, sports bar, wine bar or grill. Some places may also serve food or light
         * meals.
         */
        val BAR_OR_PUB: PlaceCategoryLeaf = leaf("200-2000-0011", "Bar or Pub")

        /**
         * `200-2000-0012` Night Club: An establishment that provides evening entertainment, such as
         * a dance club or lounge. Most places are generally open late.
         */
        val NIGHT_CLUB: PlaceCategoryLeaf = leaf("200-2000-0012", "Night Club")

        /**
         * `200-2000-0013` Dancing: An establishment that provides dancing facilities, such as a
         * dance hall, dance studio or other locations containing dance floors.
         */
        val DANCING: PlaceCategoryLeaf = leaf("200-2000-0013", "Dancing")

        /** `200-2000-0014` Karaoke: An establishment that provides karaoke entertainment. */
        val KARAOKE: PlaceCategoryLeaf = leaf("200-2000-0014", "Karaoke")

        /**
         * `200-2000-0015` Live Entertainment-Music: An establishment that provides live
         * entertainment acts, such as a comedy club, piano bar or music hall.
         */
        val LIVE_ENTERTAINMENT_MUSIC: PlaceCategoryLeaf =
            leaf("200-2000-0015", "Live Entertainment-Music")

        /**
         * `200-2000-0016` Billiards-Pool Hall: An establishment that provides billiards or pool
         * tables.
         */
        val BILLIARDS_POOL_HALL: PlaceCategoryLeaf = leaf("200-2000-0016", "Billiards-Pool Hall")

        /**
         * `200-2000-0017` Video Arcade-Game Room: An establishment that provides video games,
         * pinball machines and other games.
         */
        val VIDEO_ARCADE_GAME_ROOM: PlaceCategoryLeaf =
            leaf("200-2000-0017", "Video Arcade-Game Room")

        /**
         * `200-2000-0018` Jazz Club: An establishment that offers live jazz music in a club
         * setting.
         */
        val JAZZ_CLUB: PlaceCategoryLeaf = leaf("200-2000-0018", "Jazz Club")

        /**
         * `200-2000-0019` Beer Garden: An outdoor area (in most cases) that serves primarily beer,
         * but also provides food and entertainment. They may be attached adjacent to pubs and are
         * usually seasonal.
         */
        val BEER_GARDEN: PlaceCategoryLeaf = leaf("200-2000-0019", "Beer Garden")

        /**
         * `200-2000-0306` Adult Entertainment: An establishment that provides live adult
         * entertainment or other types of adult entertainment.
         */
        val ADULT_ENTERTAINMENT: PlaceCategoryLeaf = leaf("200-2000-0306", "Adult Entertainment")

        /** `200-2000-0368` Cocktail Lounge: An establishment where cocktails are served. */
        val COCKTAIL_LOUNGE: PlaceCategoryLeaf = leaf("200-2000-0368", "Cocktail Lounge")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                NIGHTLIFE_ENTERTAINMENT,
                BAR_OR_PUB,
                NIGHT_CLUB,
                DANCING,
                KARAOKE,
                LIVE_ENTERTAINMENT_MUSIC,
                BILLIARDS_POOL_HALL,
                VIDEO_ARCADE_GAME_ROOM,
                JAZZ_CLUB,
                BEER_GARDEN,
                ADULT_ENTERTAINMENT,
                COCKTAIL_LOUNGE,
            )
        }
    }

    /** `200-2100` Cinema: An establishment that shows movies through screen projection. */
    object Cinema : PlaceCategorySubgroup("200-2100", "Cinema") {
        override val group: PlaceCategoryGroup get() = GoingOutEntertainment

        /**
         * `200-2100-0019` Cinema: An establishment that shows movies through screen projection,
         * such as a movie theatre, movie theatre complex or outdoor movie theatre.
         */
        val CINEMA: PlaceCategoryLeaf = leaf("200-2100-0019", "Cinema")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CINEMA,
            )
        }
    }

    /**
     * `200-2200` Theatre, Music and Culture: An establishment where various types of performing
     * arts are presented.
     */
    object TheatreMusicAndCulture :
        PlaceCategorySubgroup("200-2200", "Theatre, Music and Culture") {
        override val group: PlaceCategoryGroup get() = GoingOutEntertainment

        /**
         * `200-2200-0000` Theatre, Music and Culture: An establishment where various types of
         * performing arts are presented. This is a base-level category that should be used for all
         * places that do not fit other categories defined for Theatre, Music and Culture
         * (200-2200-xxxx).
         */
        val THEATRE_MUSIC_AND_CULTURE: PlaceCategoryLeaf =
            leaf("200-2200-0000", "Theatre, Music and Culture")

        /**
         * `200-2200-0020` Performing Arts: An establishment where various types of performing arts
         * are presented.
         */
        val PERFORMING_ARTS: PlaceCategoryLeaf = leaf("200-2200-0020", "Performing Arts")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                THEATRE_MUSIC_AND_CULTURE,
                PERFORMING_ARTS,
            )
        }
    }

    /**
     * `200-2300` Gambling-Lottery-Betting: An establishment that provides gambling entertainment.
     */
    object GamblingLotteryBetting : PlaceCategorySubgroup("200-2300", "Gambling-Lottery-Betting") {
        override val group: PlaceCategoryGroup get() = GoingOutEntertainment

        /**
         * `200-2300-0000` Gambling-Lottery-Betting: An establishment that provides gambling
         * entertainment. This is a base-level category that should be used for all places that do
         * not fit other categories defined for Gambling-Lottery-Betting (200-2300-xxxx).
         */
        val GAMBLING_LOTTERY_BETTING: PlaceCategoryLeaf =
            leaf("200-2300-0000", "Gambling-Lottery-Betting")

        /**
         * `200-2300-0021` Casino: An establishment that provides a variety of legal gaming
         * services, such as a casino, riverboat casino or card room.
         */
        val CASINO: PlaceCategoryLeaf = leaf("200-2300-0021", "Casino")

        /** `200-2300-0022` Lottery Booth: An establishment where lottery tickets are purchased. */
        val LOTTERY_BOOTH: PlaceCategoryLeaf = leaf("200-2300-0022", "Lottery Booth")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                GAMBLING_LOTTERY_BETTING,
                CASINO,
                LOTTERY_BOOTH,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            NightlifeEntertainment,
            Cinema,
            TheatreMusicAndCulture,
            GamblingLotteryBetting,
        )
    }
}
