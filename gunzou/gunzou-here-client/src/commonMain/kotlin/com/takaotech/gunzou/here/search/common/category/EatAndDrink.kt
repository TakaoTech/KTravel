package com.takaotech.gunzou.here.search.common.category

/**
 * `100` Eat and Drink: The Eat and Drink category is a top level category for places where food or
 * beverages are prepared or served.
 */
object EatAndDrink : PlaceCategoryGroup("100", "Eat and Drink") {
    /**
     * `100-1000` Restaurant: An establishment that prepares and serves refreshments and prepared
     * meals.
     */
    object Restaurant : PlaceCategorySubgroup("100-1000", "Restaurant") {
        override val group: PlaceCategoryGroup get() = EatAndDrink

        /**
         * `100-1000-0000` Restaurant: An establishment that prepares and serves refreshments and
         * prepared meals. This is a base-level category that should be used for all places that do
         * not fit other categories defined for Restaurant (100-1000-xxxx).
         */
        val RESTAURANT: PlaceCategoryLeaf = leaf("100-1000-0000", "Restaurant")

        /**
         * `100-1000-0001` Casual Dining: A restaurant serving moderately-priced food in a casual
         * atmosphere that usually includes table service.
         */
        val CASUAL_DINING: PlaceCategoryLeaf = leaf("100-1000-0001", "Casual Dining")

        /**
         * `100-1000-0002` Fine Dining: A full-service restaurant that serves full-course meals in a
         * formal setting. These places usually have high quality décor, highly-trained chefs, wait
         * staff and visually appealing food. Prices are typically higher than other types of
         * restaurants.
         */
        val FINE_DINING: PlaceCategoryLeaf = leaf("100-1000-0002", "Fine Dining")

        /**
         * `100-1000-0003` Take Out and Delivery Only: A restaurant that offers take-out service,
         * delivery service, or both.
         */
        val TAKE_OUT_AND_DELIVERY_ONLY: PlaceCategoryLeaf =
            leaf("100-1000-0003", "Take Out and Delivery Only")

        /**
         * `100-1000-0004` Food Market-Stall: A restaurant that serves specialty foods at a food
         * court, marketplace or outdoor setting including hawker centers (common in Southeast
         * Asia).
         */
        val FOOD_MARKET_STALL: PlaceCategoryLeaf = leaf("100-1000-0004", "Food Market-Stall")

        /**
         * `100-1000-0005` Taqueria: A street vendor stand or small restaurant that serves
         * traditional Mexican food, such as tacos or burritos.
         */
        val TAQUERIA: PlaceCategoryLeaf = leaf("100-1000-0005", "Taqueria")

        /**
         * `100-1000-0006` Deli: A restaurant that sells ready-to-serve delicatessens including cold
         * cut meats, cheeses and salads. This type of restaurant can be found as a stand-alone
         * establishment or within a grocery store.
         */
        val DELI: PlaceCategoryLeaf = leaf("100-1000-0006", "Deli")

        /**
         * `100-1000-0007` Cafeteria: A restaurant that provides food service with little or no wait
         * staff. This type of establishment is common in schools, large office buildings, hospitals
         * and other public establishments.
         */
        val CAFETERIA: PlaceCategoryLeaf = leaf("100-1000-0007", "Cafeteria")

        /**
         * `100-1000-0008` Bistro: A restaurant that serves moderately priced meals in a
         * European-styled casual setting.
         */
        val BISTRO: PlaceCategoryLeaf = leaf("100-1000-0008", "Bistro")

        /**
         * `100-1000-0009` Fast Food: A restaurant offering food that is prepared and served
         * quickly.
         */
        val FAST_FOOD: PlaceCategoryLeaf = leaf("100-1000-0009", "Fast Food")

        /**
         * `100-1000-0050` Family Restaurant: A family-friendly restaurant chain location which
         * provides casual food. The menu includes a wide variety of Japanese, Western, and Chinese
         * dishes. Note: This category is a Japan-only category.
         */
        val FAMILY_RESTAURANT: PlaceCategoryLeaf = leaf("100-1000-0050", "Family Restaurant")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                RESTAURANT,
                CASUAL_DINING,
                FINE_DINING,
                TAKE_OUT_AND_DELIVERY_ONLY,
                FOOD_MARKET_STALL,
                TAQUERIA,
                DELI,
                CAFETERIA,
                BISTRO,
                FAST_FOOD,
                FAMILY_RESTAURANT,
            )
        }
    }

    /** `100-1100` Coffee-Tea. */
    object CoffeeTea : PlaceCategorySubgroup("100-1100", "Coffee-Tea") {
        override val group: PlaceCategoryGroup get() = EatAndDrink

        /**
         * `100-1100-0000` Coffee-Tea: An establishment that sells drinks, such as coffee and tea,
         * as well as refreshments. This is a base-level category that should be used for all places
         * that do not fit other categories defined for Coffee-Tea (100-1100-xxxx).
         */
        val COFFEE_TEA: PlaceCategoryLeaf = leaf("100-1100-0000", "Coffee-Tea")

        /**
         * `100-1100-0010` Coffee Shop: An establishment that primarily sells coffee, but may also
         * serve light foods, such as pastries or other snacks.
         */
        val COFFEE_SHOP: PlaceCategoryLeaf = leaf("100-1100-0010", "Coffee Shop")

        /**
         * `100-1100-0331` Tea House: An establishment that sells tea and other related products.
         */
        val TEA_HOUSE: PlaceCategoryLeaf = leaf("100-1100-0331", "Tea House")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                COFFEE_TEA,
                COFFEE_SHOP,
                TEA_HOUSE,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            Restaurant,
            CoffeeTea,
        )
    }
}
