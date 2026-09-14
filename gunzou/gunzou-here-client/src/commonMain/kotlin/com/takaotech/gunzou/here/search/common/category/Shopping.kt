package com.takaotech.gunzou.here.search.common.category

/**
 * `600` Shopping: The Shopping category is a top level category for places where consumer goods are
 * commonly sold, such as clothing stores, grocery stores, hardware stores and other types of
 * shopping centers.
 */
object Shopping : PlaceCategoryGroup("600", "Shopping") {
    /**
     * `600-6000` Convenience Store: An establishment that sells groceries, candy, toiletries, soft
     * drinks, tobacco products, newspapers and other products.
     */
    object ConvenienceStore : PlaceCategorySubgroup("600-6000", "Convenience Store") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6000-0061` Convenience Store: An establishment that sells groceries, candy,
         * toiletries, soft drinks, tobacco products, newspapers and other products. Some
         * convenience stores may be part of a petro/fuel station.
         */
        val CONVENIENCE_STORE: PlaceCategoryLeaf = leaf("600-6000-0061", "Convenience Store")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CONVENIENCE_STORE,
            )
        }
    }

    /**
     * `600-6100` Mall-Shopping Complex: A complex of businesses that are co-located and share
     * common services.
     */
    object MallShoppingComplex : PlaceCategorySubgroup("600-6100", "Mall-Shopping Complex") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6100-0062` Shopping Mall: A complex of businesses that are co-located and share
         * common services. Shopping malls generally include a variety of retail businesses, such as
         * clothing, electronics, jewelry, restaurants and more. Common examples of shopping malls
         * include covered shopping centers, strip malls, famous shopping streets, factory outlets
         * and open air shopping centers.
         */
        val SHOPPING_MALL: PlaceCategoryLeaf = leaf("600-6100-0062", "Shopping Mall")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                SHOPPING_MALL,
            )
        }
    }

    /**
     * `600-6200` Department Store: A business that sells a wide variety of merchandise that is
     * organized by product or service departments.
     */
    object DepartmentStore : PlaceCategorySubgroup("600-6200", "Department Store") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6200-0063` Department Store: A business that sells a wide variety of merchandise
         * that is organized by product or service departments. Departments generally include
         * clothing, garden, electronics, photo services, household goods, pets, toys and more.
         */
        val DEPARTMENT_STORE: PlaceCategoryLeaf = leaf("600-6200-0063", "Department Store")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                DEPARTMENT_STORE,
            )
        }
    }

    /**
     * `600-6300` Food and Drink: A business that sells specialty products of a particular type of
     * food or beverage.
     */
    object FoodAndDrink : PlaceCategorySubgroup("600-6300", "Food and Drink") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6300-0064` Food-Beverage Specialty Store: A business that sells specialty products
         * of a particular type of food or beverage, such as ice cream, meat, cheese, coffee or
         * other unprepared foods.
         */
        val FOOD_BEVERAGE_SPECIALTY_STORE: PlaceCategoryLeaf =
            leaf("600-6300-0064", "Food-Beverage Specialty Store")

        /**
         * `600-6300-0066` Grocery: A business that sells a large variety of food including fresh
         * produce, frozen foods, packaged goods, bakery items and meat products.
         */
        val GROCERY: PlaceCategoryLeaf = leaf("600-6300-0066", "Grocery")

        /**
         * `600-6300-0067` Specialty Food Store: A business that sells unique ethnic or quality
         * foods.
         */
        val SPECIALTY_FOOD_STORE: PlaceCategoryLeaf = leaf("600-6300-0067", "Specialty Food Store")

        /**
         * `600-6300-0068` Wine and Liquor: A business that sells packaged beer, wine and liquor
         * products.
         */
        val WINE_AND_LIQUOR: PlaceCategoryLeaf = leaf("600-6300-0068", "Wine and Liquor")

        /**
         * `600-6300-0244` Bakery and Baked Goods Store: A business that sells bread and other baked
         * goods. Common examples include cupcake shops, bagel shops and other types of bakeries.
         */
        val BAKERY_AND_BAKED_GOODS_STORE: PlaceCategoryLeaf =
            leaf("600-6300-0244", "Bakery and Baked Goods Store")

        /**
         * `600-6300-0245` Sweet Shop: A business that sells traditional sweets and popular snacks.
         * Common examples include candy stores, fudge shops and chocolatiers.
         */
        val SWEET_SHOP: PlaceCategoryLeaf = leaf("600-6300-0245", "Sweet Shop")

        /**
         * `600-6300-0246` Doughnut Shop: A shop that specializes in the production and sale of
         * doughnuts (also commonly spelled as donuts).
         */
        val DOUGHNUT_SHOP: PlaceCategoryLeaf = leaf("600-6300-0246", "Doughnut Shop")

        /**
         * `600-6300-0363` Butcher: A specialty store where fresh meats and associated goods are
         * available.
         */
        val BUTCHER: PlaceCategoryLeaf = leaf("600-6300-0363", "Butcher")

        /**
         * `600-6300-0364` Dairy Goods: A specialty store where dairy products and associated goods
         * are available.
         */
        val DAIRY_GOODS: PlaceCategoryLeaf = leaf("600-6300-0364", "Dairy Goods")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                FOOD_BEVERAGE_SPECIALTY_STORE,
                GROCERY,
                SPECIALTY_FOOD_STORE,
                WINE_AND_LIQUOR,
                BAKERY_AND_BAKED_GOODS_STORE,
                SWEET_SHOP,
                DOUGHNUT_SHOP,
                BUTCHER,
                DAIRY_GOODS,
            )
        }
    }

    /**
     * `600-6400` Drugstore or Pharmacy: A business that sells medications, toiletry items and other
     * retail cosmetics.
     */
    object DrugstoreOrPharmacy : PlaceCategorySubgroup("600-6400", "Drugstore or Pharmacy") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6400-0000` Drugstore or Pharmacy: A business that sells medications, toiletry items
         * and other retail cosmetics. Drugstores and Pharmacies generally sell both prescription
         * and non-prescription medications. This is a base-level category that should be used for
         * all places that do not fit other categories defined forPharmacy (600-6400-xxxx).
         */
        val DRUGSTORE_OR_PHARMACY: PlaceCategoryLeaf =
            leaf("600-6400-0000", "Drugstore or Pharmacy")

        /**
         * `600-6400-0069` Drugstore: A business that sells prescription and non-prescription
         * medications in addition to other consumer goods, such as cosmetics, snacks, beverages and
         * tobacco products.
         */
        val DRUGSTORE: PlaceCategoryLeaf = leaf("600-6400-0069", "Drugstore")

        /**
         * `600-6400-0070` Pharmacy: A business that prepares and dispenses pharmaceutical drugs,
         * medicines and other health remedies.
         */
        val PHARMACY: PlaceCategoryLeaf = leaf("600-6400-0070", "Pharmacy")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                DRUGSTORE_OR_PHARMACY,
                DRUGSTORE,
                PHARMACY,
            )
        }
    }

    /**
     * `600-6500` Electronics: A business that sells consumer electronics and electronic
     * entertainment equipment.
     */
    object Electronics : PlaceCategorySubgroup("600-6500", "Electronics") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6500-0072` Consumer Electronics Store: A business that sells consumer and
         * entertainment electronics such as cooking/washing appliances, televisions, and gaming
         * systems.
         */
        val CONSUMER_ELECTRONICS_STORE: PlaceCategoryLeaf =
            leaf("600-6500-0072", "Consumer Electronics Store")

        /** `600-6500-0073` Mobile Retailer: An authorized retailer of mobile phones and devices. */
        val MOBILE_RETAILER: PlaceCategoryLeaf = leaf("600-6500-0073", "Mobile Retailer")

        /**
         * `600-6500-0074` Mobile Service Center: An authorized service center for the servicing and
         * repair of mobile phones and devices.
         */
        val MOBILE_SERVICE_CENTER: PlaceCategoryLeaf =
            leaf("600-6500-0074", "Mobile Service Center")

        /**
         * `600-6500-0075` Computer and Software: A business that sells computer goods, such as
         * hardware, software and related accessories.
         */
        val COMPUTER_AND_SOFTWARE: PlaceCategoryLeaf =
            leaf("600-6500-0075", "Computer and Software")

        /**
         * `600-6500-0076` Entertainment Electronics: A business that sells consumer electronics and
         * entertainment equipment.
         */
        val ENTERTAINMENT_ELECTRONICS: PlaceCategoryLeaf =
            leaf("600-6500-0076", "Entertainment Electronics")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CONSUMER_ELECTRONICS_STORE,
                MOBILE_RETAILER,
                MOBILE_SERVICE_CENTER,
                COMPUTER_AND_SOFTWARE,
                ENTERTAINMENT_ELECTRONICS,
            )
        }
    }

    /**
     * `600-6600` Hardware, House and Garden: A business that sells crafts, gardening, remodeling,
     * or decorating items for the home.
     */
    object HardwareHouseAndGarden :
        PlaceCategorySubgroup("600-6600", "Hardware, House and Garden") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6600-0000` Hardware, House and Garden: A business that sells crafts, gardening,
         * remodeling, or decorating items for the home. This is a base-level category that should
         * be used for all places that do not fit other categories defined forHardware, House and
         * Garden (600-6600-xxxx).
         */
        val HARDWARE_HOUSE_AND_GARDEN: PlaceCategoryLeaf =
            leaf("600-6600-0000", "Hardware, House and Garden")

        /**
         * `600-6600-0077` Home Improvement: A business that sells a variety of building materials,
         * hardware, and home improvement products. These are larger in size than hardware stores,
         * and are usually major retail chains.
         */
        val HOME_IMPROVEMENT: PlaceCategoryLeaf = leaf("600-6600-0077", "Home Improvement")

        /**
         * `600-6600-0078` Home Specialty Store: A business that sells home furnishings and
         * accessories, but also specializes in custom furniture and other household items.
         */
        val HOME_SPECIALTY_STORE: PlaceCategoryLeaf = leaf("600-6600-0078", "Home Specialty Store")

        /** `600-6600-0079` Floor and Carpet: A business that sells flooring and carpeting. */
        val FLOOR_AND_CARPET: PlaceCategoryLeaf = leaf("600-6600-0079", "Floor and Carpet")

        /**
         * `600-6600-0080` Furniture Store: A business that sells furnishings and accessories for a
         * home.
         */
        val FURNITURE_STORE: PlaceCategoryLeaf = leaf("600-6600-0080", "Furniture Store")

        /**
         * `600-6600-0082` Garden Center: A business that sells a variety of lawn and garden
         * supplies, tools and other related items.
         */
        val GARDEN_CENTER: PlaceCategoryLeaf = leaf("600-6600-0082", "Garden Center")

        /**
         * `600-6600-0083` Glass and Window: A business that sells windows, glass products and
         * window treatments. These establishments may also provide window installation services.
         */
        val GLASS_AND_WINDOW: PlaceCategoryLeaf = leaf("600-6600-0083", "Glass and Window")

        /** `600-6600-0084` Lumber: A business that sells timber products. */
        val LUMBER: PlaceCategoryLeaf = leaf("600-6600-0084", "Lumber")

        /** `600-6600-0085` Major Appliance: A business that sells major household appliances. */
        val MAJOR_APPLIANCE: PlaceCategoryLeaf = leaf("600-6600-0085", "Major Appliance")

        /**
         * `600-6600-0310` Power Equipment Dealer: A business that sells power equipment, such as
         * lawnmowers, lawn tractors and generators. These establishments may also provide repair or
         * rental services, and may also sell replacement parts.
         */
        val POWER_EQUIPMENT_DEALER: PlaceCategoryLeaf =
            leaf("600-6600-0310", "Power Equipment Dealer")

        /** `600-6600-0319` Paint Store: A business that sells paint and other related products. */
        val PAINT_STORE: PlaceCategoryLeaf = leaf("600-6600-0319", "Paint Store")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                HARDWARE_HOUSE_AND_GARDEN,
                HOME_IMPROVEMENT,
                HOME_SPECIALTY_STORE,
                FLOOR_AND_CARPET,
                FURNITURE_STORE,
                GARDEN_CENTER,
                GLASS_AND_WINDOW,
                LUMBER,
                MAJOR_APPLIANCE,
                POWER_EQUIPMENT_DEALER,
                PAINT_STORE,
            )
        }
    }

    /** `600-6700` Bookstore: A business that sells books, magazines and other reading material. */
    object Bookstore : PlaceCategorySubgroup("600-6700", "Bookstore") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6700-0000` Other Bookshop: A business that sells books, magazines and other reading
         * material. This is a base-level category that should be used for all places that do not
         * fit other categories defined for Bookshop (600-6700-xxxx).
         */
        val OTHER_BOOKSHOP: PlaceCategoryLeaf = leaf("600-6700-0000", "Other Bookshop")

        /** `600-6700-0087` Bookstore: A retail business that primarily sells books. */
        val BOOKSTORE: PlaceCategoryLeaf = leaf("600-6700-0087", "Bookstore")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                OTHER_BOOKSHOP,
                BOOKSTORE,
            )
        }
    }

    /**
     * `600-6800` Clothing and Accessories: A business that sells apparel items, garments or fashion
     * accessories for men, women, and children.
     */
    object ClothingAndAccessories : PlaceCategorySubgroup("600-6800", "Clothing and Accessories") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6800-0000` Clothing and Accessories: A business that sells apparel items, garments
         * or fashion accessories for men, women, and children. This is a base-level category that
         * should be used for all places that do not fit other categories defined for Clothing and
         * Accessories (600-6800-xxxx).
         */
        val CLOTHING_AND_ACCESSORIES: PlaceCategoryLeaf =
            leaf("600-6800-0000", "Clothing and Accessories")

        /** `600-6800-0089` Men's Apparel: A business that sells men's wear and accessories. */
        val MEN_S_APPAREL: PlaceCategoryLeaf = leaf("600-6800-0089", "Men's Apparel")

        /** `600-6800-0090` Women's Apparel: A business that sells women's wear and accessories. */
        val WOMEN_S_APPAREL: PlaceCategoryLeaf = leaf("600-6800-0090", "Women's Apparel")

        /**
         * `600-6800-0091` Children's Apparel: A business that sells children's wear and
         * accessories.
         */
        val CHILDREN_S_APPAREL: PlaceCategoryLeaf = leaf("600-6800-0091", "Children's Apparel")

        /** `600-6800-0092` Shoes-Footwear: A business that sells shoes. */
        val SHOES_FOOTWEAR: PlaceCategoryLeaf = leaf("600-6800-0092", "Shoes-Footwear")

        /**
         * `600-6800-0093` Specialty Clothing Store: A business that sells and specializes in a
         * specific type of clothing.
         */
        val SPECIALTY_CLOTHING_STORE: PlaceCategoryLeaf =
            leaf("600-6800-0093", "Specialty Clothing Store")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CLOTHING_AND_ACCESSORIES,
                MEN_S_APPAREL,
                WOMEN_S_APPAREL,
                CHILDREN_S_APPAREL,
                SHOES_FOOTWEAR,
                SPECIALTY_CLOTHING_STORE,
            )
        }
    }

    /**
     * `600-6900` Consumer Goods: A business that sells a variety of products targeted to consumers.
     */
    object ConsumerGoods : PlaceCategorySubgroup("600-6900", "Consumer Goods") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6900-0000` Consumer Goods: A business that sells a variety of products targeted to
         * consumers. This is a base-level category that should be used for all places that do not
         * fit other categories defined for Consumer Goods (600-6900-xxxx).
         */
        val CONSUMER_GOODS: PlaceCategoryLeaf = leaf("600-6900-0000", "Consumer Goods")

        /**
         * `600-6900-0094` Sporting Goods Store: A business that sells individual and team sports
         * equipment and other related items.
         */
        val SPORTING_GOODS_STORE: PlaceCategoryLeaf = leaf("600-6900-0094", "Sporting Goods Store")

        /**
         * `600-6900-0095` Office Supply and Services Store: A business that sells office supplies.
         * These businesses may also provide select professional business services, such as
         * printing, photocopying, graphic design, shipping and advertising.
         */
        val OFFICE_SUPPLY_AND_SERVICES_STORE: PlaceCategoryLeaf =
            leaf("600-6900-0095", "Office Supply and Services Store")

        /**
         * `600-6900-0096` Specialty Store: A business that specializes in the sale of a specific
         * type of merchandise. For example, a candle store, t-shirts shop or other.
         */
        val SPECIALTY_STORE: PlaceCategoryLeaf = leaf("600-6900-0096", "Specialty Store")

        /** `600-6900-0097` Pet Supply: A business that sells goods and services for pets. */
        val PET_SUPPLY: PlaceCategoryLeaf = leaf("600-6900-0097", "Pet Supply")

        /**
         * `600-6900-0098` Wholesale Store: A business that sells consumer goods in bulk quantity.
         * These places may charge membership fees.
         */
        val WHOLESALE_STORE: PlaceCategoryLeaf = leaf("600-6900-0098", "Wholesale Store")

        /**
         * `600-6900-0099` General Merchandise: A business that sells general merchandise, but does
         * not fit into other retail categories.
         */
        val GENERAL_MERCHANDISE: PlaceCategoryLeaf = leaf("600-6900-0099", "General Merchandise")

        /**
         * `600-6900-0100` Discount Store: A business that sells merchandise below suggested retail
         * value.
         */
        val DISCOUNT_STORE: PlaceCategoryLeaf = leaf("600-6900-0100", "Discount Store")

        /**
         * `600-6900-0101` Flowers and Jewelry: A business that sells that buys and sells jewelry,
         * or that sells and arranges plants and cut flowers.
         */
        val FLOWERS_AND_JEWELRY: PlaceCategoryLeaf = leaf("600-6900-0101", "Flowers and Jewelry")

        /**
         * `600-6900-0102` Variety Store: A business that sells a large variety of general goods.
         */
        val VARIETY_STORE: PlaceCategoryLeaf = leaf("600-6900-0102", "Variety Store")

        /**
         * `600-6900-0103` Gift, Antique and Art: A business that sells antiques, art, gifts,
         * novelty items, souvenir items or greeting cards.
         */
        val GIFT_ANTIQUE_AND_ART: PlaceCategoryLeaf = leaf("600-6900-0103", "Gift, Antique and Art")

        /** `600-6900-0105` Record, CD and Video: A business that sells audio and video media. */
        val RECORD_CD_AND_VIDEO: PlaceCategoryLeaf = leaf("600-6900-0105", "Record, CD and Video")

        /**
         * `600-6900-0106` Video and Game Rental: A business that rents videotapes, DVDs, or
         * electronic games.
         */
        val VIDEO_AND_GAME_RENTAL: PlaceCategoryLeaf =
            leaf("600-6900-0106", "Video and Game Rental")

        /**
         * `600-6900-0107` Cigar and Tobacco Shop: A business that sells cigars, tobacco, pipes, and
         * other tobacco smoking accessories.
         */
        val CIGAR_AND_TOBACCO_SHOP: PlaceCategoryLeaf =
            leaf("600-6900-0107", "Cigar and Tobacco Shop")

        /**
         * `600-6900-0108` Vaping Store: A business that sells electronic cigarettes (also refered
         * to as vaporizers) and other vaping accessories.
         */
        val VAPING_STORE: PlaceCategoryLeaf = leaf("600-6900-0108", "Vaping Store")

        /**
         * `600-6900-0246` Bicycle and Bicycle Accessories Shop: A business that sell bicycles and
         * bicycle accessories.
         */
        val BICYCLE_AND_BICYCLE_ACCESSORIES_SHOP: PlaceCategoryLeaf =
            leaf("600-6900-0246", "Bicycle and Bicycle Accessories Shop")

        /**
         * `600-6900-0247` Market: A public marketplace where fresh fruits, vegetables, meat, and
         * other farm products are sold. Some markets also sell other types of merchandise.
         */
        val MARKET: PlaceCategoryLeaf = leaf("600-6900-0247", "Market")

        /** `600-6900-0248` Motorcycle Accessories: A business that sells motorcycle accessories. */
        val MOTORCYCLE_ACCESSORIES: PlaceCategoryLeaf =
            leaf("600-6900-0248", "Motorcycle Accessories")

        /**
         * `600-6900-0249` Non-Store Retailers: A business that sells merchandise without a physical
         * storefront. For example, mail catalogs, television sales, and internet-based sales sites
         * fall within this category.
         */
        val NON_STORE_RETAILERS: PlaceCategoryLeaf = leaf("600-6900-0249", "Non-Store Retailers")

        /**
         * `600-6900-0250` Pawnshop: A business that provides cash or secured loans in exchange for
         * personal property. For loans, the personal property is used as collateral. These places
         * generally offer items for sale to recoup the value of the loan for those that have
         * extended beyond the contractual period.
         */
        val PAWNSHOP: PlaceCategoryLeaf = leaf("600-6900-0250", "Pawnshop")

        /**
         * `600-6900-0251` Used-Second Hand Merchandise Stores: A business that sells previously
         * owned and used merchandise, such as clothing, books, furniture and toys. Inventory in
         * these stores varies widely.
         */
        val USED_SECOND_HAND_MERCHANDISE_STORES: PlaceCategoryLeaf =
            leaf("600-6900-0251", "Used-Second Hand Merchandise Stores")

        /**
         * `600-6900-0305` Adult Shop: A business that sells items made specifically for adult use,
         * often with sexual connotations.
         */
        val ADULT_SHOP: PlaceCategoryLeaf = leaf("600-6900-0305", "Adult Shop")

        /**
         * `600-6900-0307` Arts and Crafts Supplies: A business that sells merchandise for home made
         * arts and crafts projects.
         */
        val ARTS_AND_CRAFTS_SUPPLIES: PlaceCategoryLeaf =
            leaf("600-6900-0307", "Arts and Crafts Supplies")

        /** `600-6900-0355` Florist: A business that sells and arranges plants and cut flowers. */
        val FLORIST: PlaceCategoryLeaf = leaf("600-6900-0355", "Florist")

        /**
         * `600-6900-0356` Jeweler: A business that makes or sells jewels or jewelry. Some places
         * may also offer repair services.
         */
        val JEWELER: PlaceCategoryLeaf = leaf("600-6900-0356", "Jeweler")

        /** `600-6900-0358` Toy Store: A business that makes or sells toys. */
        val TOY_STORE: PlaceCategoryLeaf = leaf("600-6900-0358", "Toy Store")

        /**
         * `600-6900-0387` Golf Shop: A business that sells golf-related items, such as golf clubs,
         * golf bags, and other accessories.
         */
        val GOLF_SHOP: PlaceCategoryLeaf = leaf("600-6900-0387", "Golf Shop")

        /**
         * `600-6900-0388` Hunting-Fishing Shop: A business that sells hunting or fishing-related
         * items, such as boots, waders, rods, reels and other accessories.
         */
        val HUNTING_FISHING_SHOP: PlaceCategoryLeaf = leaf("600-6900-0388", "Hunting-Fishing Shop")

        /**
         * `600-6900-0389` Running-Walking Shop: A business that sells running or walking-related
         * items, such as shoes, clothing and other accessories.
         */
        val RUNNING_WALKING_SHOP: PlaceCategoryLeaf = leaf("600-6900-0389", "Running-Walking Shop")

        /**
         * `600-6900-0390` Skate Shop: A business that sells skate-related items, such as
         * skateboards, clothing, protective gear and other accessories.
         */
        val SKATE_SHOP: PlaceCategoryLeaf = leaf("600-6900-0390", "Skate Shop")

        /**
         * `600-6900-0391` Ski Shop: A business that sells skiing-related items, such as skis, ski
         * boots, clothing and other accessories.
         */
        val SKI_SHOP: PlaceCategoryLeaf = leaf("600-6900-0391", "Ski Shop")

        /**
         * `600-6900-0392` Snowboard Shop: A business that sells snowboarding-related items, such as
         * snowboards, protective gear, clothing and other accessories.
         */
        val SNOWBOARD_SHOP: PlaceCategoryLeaf = leaf("600-6900-0392", "Snowboard Shop")

        /**
         * `600-6900-0393` Surf Shop: A business that sells surf-related items, such as surf boards,
         * rash vests and other accessories.
         */
        val SURF_SHOP: PlaceCategoryLeaf = leaf("600-6900-0393", "Surf Shop")

        /**
         * `600-6900-0394` BMX Shop: A business that sells BMX-related items, such as BMX bicycles,
         * BMX parts, clothing and protective gear.
         */
        val BMX_SHOP: PlaceCategoryLeaf = leaf("600-6900-0394", "BMX Shop")

        /**
         * `600-6900-0395` Camping-Hiking Shop: A business that sells camping/hiking-related items,
         * such as tents, sleeping bags and other accessories.
         */
        val CAMPING_HIKING_SHOP: PlaceCategoryLeaf = leaf("600-6900-0395", "Camping-Hiking Shop")

        /**
         * `600-6900-0396` Canoe-Kayak Shop: A business that sells canoe/kayak-related items, such
         * as clothing, canoes, paddles and other accessories.
         */
        val CANOE_KAYAK_SHOP: PlaceCategoryLeaf = leaf("600-6900-0396", "Canoe-Kayak Shop")

        /**
         * `600-6900-0397` Cross Country Ski Shop: A business that sells equipment for cross-country
         * skiing.
         */
        val CROSS_COUNTRY_SKI_SHOP: PlaceCategoryLeaf =
            leaf("600-6900-0397", "Cross Country Ski Shop")

        /** `600-6900-0398` Tack Shop: A business that sells equipment for horseback riding. */
        val TACK_SHOP: PlaceCategoryLeaf = leaf("600-6900-0398", "Tack Shop")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CONSUMER_GOODS,
                SPORTING_GOODS_STORE,
                OFFICE_SUPPLY_AND_SERVICES_STORE,
                SPECIALTY_STORE,
                PET_SUPPLY,
                WHOLESALE_STORE,
                GENERAL_MERCHANDISE,
                DISCOUNT_STORE,
                FLOWERS_AND_JEWELRY,
                VARIETY_STORE,
                GIFT_ANTIQUE_AND_ART,
                RECORD_CD_AND_VIDEO,
                VIDEO_AND_GAME_RENTAL,
                CIGAR_AND_TOBACCO_SHOP,
                VAPING_STORE,
                BICYCLE_AND_BICYCLE_ACCESSORIES_SHOP,
                MARKET,
                MOTORCYCLE_ACCESSORIES,
                NON_STORE_RETAILERS,
                PAWNSHOP,
                USED_SECOND_HAND_MERCHANDISE_STORES,
                ADULT_SHOP,
                ARTS_AND_CRAFTS_SUPPLIES,
                FLORIST,
                JEWELER,
                TOY_STORE,
                GOLF_SHOP,
                HUNTING_FISHING_SHOP,
                RUNNING_WALKING_SHOP,
                SKATE_SHOP,
                SKI_SHOP,
                SNOWBOARD_SHOP,
                SURF_SHOP,
                BMX_SHOP,
                CAMPING_HIKING_SHOP,
                CANOE_KAYAK_SHOP,
                CROSS_COUNTRY_SKI_SHOP,
                TACK_SHOP,
            )
        }
    }

    /** `600-6950` Hair and Beauty. */
    object HairAndBeauty : PlaceCategorySubgroup("600-6950", "Hair and Beauty") {
        override val group: PlaceCategoryGroup get() = Shopping

        /**
         * `600-6950-0000` Hair and Beauty: A business that provides hair styling and personal
         * appearance services. Places in this category may also sell hair products and other
         * related cosmetic items. This is a base-level category that should be used for all places
         * that do not fit other categories defined for Hair and Beauty (600-6950-xxxx).
         */
        val HAIR_AND_BEAUTY: PlaceCategoryLeaf = leaf("600-6950-0000", "Hair and Beauty")

        /**
         * `600-6950-0399` Barber: A business that provides hair cutting services, especially for
         * men, and may offer shaves or beard trims.
         */
        val BARBER: PlaceCategoryLeaf = leaf("600-6950-0399", "Barber")

        /** `600-6950-0400` Nail Salon: A business that provides manicure and pedicure services. */
        val NAIL_SALON: PlaceCategoryLeaf = leaf("600-6950-0400", "Nail Salon")

        /**
         * `600-6950-0401` Hair Salon: A business that provides hair styling, personal appearance
         * services, and may also sell hair products and other related cosmetic items.
         */
        val HAIR_SALON: PlaceCategoryLeaf = leaf("600-6950-0401", "Hair Salon")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                HAIR_AND_BEAUTY,
                BARBER,
                NAIL_SALON,
                HAIR_SALON,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            ConvenienceStore,
            MallShoppingComplex,
            DepartmentStore,
            FoodAndDrink,
            DrugstoreOrPharmacy,
            Electronics,
            HardwareHouseAndGarden,
            Bookstore,
            ClothingAndAccessories,
            ConsumerGoods,
            HairAndBeauty,
        )
    }
}
