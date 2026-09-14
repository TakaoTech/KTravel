package com.takaotech.gunzou.here.search.common.category

/**
 * `700` Business and Services: The Business and Services category is a top level category for
 * places that provide professional services to other businesses, such as printing, photocopying,
 * graphic design, marketing, advertising and other general business services.
 */
object BusinessAndServices : PlaceCategoryGroup("700", "Business and Services") {
    /**
     * `700-7000` Banking: Businesses that specialize in the maintenance, lending, exchange, or
     * issuance of money.
     */
    object Banking : PlaceCategorySubgroup("700-7000", "Banking") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7000-0107` Bank: A business that specializes in the maintenance, lending, exchange,
         * or issuance of money. Places in this category commonly include central banks, consumer
         * banks and credit unions.
         */
        val BANK: PlaceCategoryLeaf = leaf("700-7000-0107", "Bank")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                BANK,
            )
        }
    }

    /**
     * `700-7010` ATM: A computer terminal that allows bank customers to deposit, withdraw, or
     * transfer funds without the assistance of a bank teller.
     */
    object ATM : PlaceCategorySubgroup("700-7010", "ATM") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7010-0108` ATM: A computer terminal that provides the ability to deposit, withdraw,
         * or transfer funds without the assistance of a bank teller.
         */
        val ATM: PlaceCategoryLeaf = leaf("700-7010-0108", "ATM")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                ATM,
            )
        }
    }

    /** `700-7050` Money-Cash Services: Businesses that provide money related services. */
    object MoneyCashServices : PlaceCategorySubgroup("700-7050", "Money-Cash Services") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7050-0109` Money Transferring Service: A business that provides or receives
         * electronic money transfers.
         */
        val MONEY_TRANSFERRING_SERVICE: PlaceCategoryLeaf =
            leaf("700-7050-0109", "Money Transferring Service")

        /**
         * `700-7050-0110` Check Cashing Service-Currency Exchange: A business that provides and
         * charges for check cashing services or foreign currency exchange.
         */
        val CHECK_CASHING_SERVICE_CURRENCY_EXCHANGE: PlaceCategoryLeaf =
            leaf("700-7050-0110", "Check Cashing Service-Currency Exchange")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                MONEY_TRANSFERRING_SERVICE,
                CHECK_CASHING_SERVICE_CURRENCY_EXCHANGE,
            )
        }
    }

    /** `700-7100` Communication-Media: Businesses that provide communication services. */
    object CommunicationMedia : PlaceCategorySubgroup("700-7100", "Communication-Media") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7100-0000` Communication-Media: A business that provides communication services,
         * with the exception of telephone communications. This is a base-level category that should
         * be used for all places that do not fit other categories defined for Communication-Media
         * (700-7100-xxxx) categories.
         */
        val COMMUNICATION_MEDIA: PlaceCategoryLeaf = leaf("700-7100-0000", "Communication-Media")

        /**
         * `700-7100-0134` Telephone Service: A business that provides telephone communications
         * services.
         */
        val TELEPHONE_SERVICE: PlaceCategoryLeaf = leaf("700-7100-0134", "Telephone Service")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                COMMUNICATION_MEDIA,
                TELEPHONE_SERVICE,
            )
        }
    }

    /**
     * `700-7200` Commercial Services: Businesses that provide a service or product for use by other
     * businesses.
     */
    object CommercialServices : PlaceCategorySubgroup("700-7200", "Commercial Services") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7200-0000` Commercial Services: A business that provides a service or product for
         * use by other businesses. This is a base-level category that should be used for all places
         * that do not fit other categories defined for Commercial Services (700-7200-xxxx).
         */
        val COMMERCIAL_SERVICES: PlaceCategoryLeaf = leaf("700-7200-0000", "Commercial Services")

        /**
         * `700-7200-0252` Advertising-Marketing, PR and Market Research: A business that provides
         * advertising, marketing, public relations, and market research services to other
         * businesses and to the public as a whole.
         */
        val ADVERTISING_MARKETING_PR_AND_MARKET_RESEARCH: PlaceCategoryLeaf =
            leaf("700-7200-0252", "Advertising-Marketing, PR and Market Research")

        /**
         * `700-7200-0253` Catering and Other Food Services: A business that provides a single
         * event-based food service or other food services including house-to-house bakery products,
         * lunch wagons, and so on.
         */
        val CATERING_AND_OTHER_FOOD_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0253", "Catering and Other Food Services")

        /**
         * `700-7200-0254` Construction: A business that is engaged in the building of something,
         * typically a large structure.
         */
        val CONSTRUCTION: PlaceCategoryLeaf = leaf("700-7200-0254", "Construction")

        /**
         * `700-7200-0255` Customer Care-Service Center: A business that provides customer care
         * before, during, and after purchasing a product or service.
         */
        val CUSTOMER_CARE_SERVICE_CENTER: PlaceCategoryLeaf =
            leaf("700-7200-0255", "Customer Care-Service Center")

        /**
         * `700-7200-0256` Engineering and Scientific Services: A business that is engaged in
         * applying physical and chemical laws and principles of engineering and science in the
         * design, development, and utilization of machines, materials, instruments, structures,
         * processes, and systems.
         */
        val ENGINEERING_AND_SCIENTIFIC_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0256", "Engineering and Scientific Services")

        /**
         * `700-7200-0257` Farming: A business that provides support services for lands devoted to
         * the practice of producing and managing fibers, fuel, and food (for example, produce,
         * grains, fish farming or livestock).
         */
        val FARMING: PlaceCategoryLeaf = leaf("700-7200-0257", "Farming")

        /**
         * `700-7200-0258` Food Production: A business that is engaged in the production of food.
         */
        val FOOD_PRODUCTION: PlaceCategoryLeaf = leaf("700-7200-0258", "Food Production")

        /**
         * `700-7200-0259` Human Resources and Recruiting Services: A business that provides advice
         * and services to businesses and other organizations in the fields of human resources and
         * recruiting.
         */
        val HUMAN_RESOURCES_AND_RECRUITING_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0259", "Human Resources and Recruiting Services")

        /**
         * `700-7200-0260` Investigation Services: A business that provides private investigation
         * and detective services.
         */
        val INVESTIGATION_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0260", "Investigation Services")

        /**
         * `700-7200-0261` IT and Office Equipment Services: A business that provides information
         * technology services, as well as other office equipment. Services generally include
         * computer service, networking, software development, training, and other related services.
         */
        val IT_AND_OFFICE_EQUIPMENT_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0261", "IT and Office Equipment Services")

        /**
         * `700-7200-0262` Landscaping Services: A business that provides landscape and garden
         * project planning, construction and landscape management, and maintenance and gardening.
         * Services generally include planning, design, implementation, as well as care and
         * maintenance.
         */
        val LANDSCAPING_SERVICES: PlaceCategoryLeaf = leaf("700-7200-0262", "Landscaping Services")

        /**
         * `700-7200-0263` Locksmiths and Security Systems Services: A business that provides a
         * variety of services designed to prevent and detect intrusion or unauthorized access to a
         * building or area. Services generally include lock production, installation, repair, key
         * replacement, or security system design, implementation and monitoring.
         */
        val LOCKSMITHS_AND_SECURITY_SYSTEMS_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0263", "Locksmiths and Security Systems Services")

        /**
         * `700-7200-0264` Management and Consulting Services: A business that is engaged in
         * providing management or consulting services to other businesses.
         */
        val MANAGEMENT_AND_CONSULTING_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0264", "Management and Consulting Services")

        /**
         * `700-7200-0265` Manufacturing: A business that is engaged in making something into a
         * finished product using raw materials, especially on a large industrial scale.
         */
        val MANUFACTURING: PlaceCategoryLeaf = leaf("700-7200-0265", "Manufacturing")

        /**
         * `700-7200-0266` Mining, Quarrying and Other Extraction: A business that is engaged in the
         * extraction of valuable minerals or other geological substances (including liquids and
         * gases) from the earth. Also included are mining support services.
         */
        val MINING_QUARRYING_AND_OTHER_EXTRACTION: PlaceCategoryLeaf =
            leaf("700-7200-0266", "Mining, Quarrying and Other Extraction")

        /**
         * `700-7200-0267` Modeling Agencies: A business that provides representation for fashion
         * models, to work in the fashion industry.
         */
        val MODELING_AGENCIES: PlaceCategoryLeaf = leaf("700-7200-0267", "Modeling Agencies")

        /**
         * `700-7200-0268` Motorcycle Service and Maintenance: A business that provides a service
         * for the maintenance and repair of motorcycles.
         */
        val MOTORCYCLE_SERVICE_AND_MAINTENANCE: PlaceCategoryLeaf =
            leaf("700-7200-0268", "Motorcycle Service and Maintenance")

        /**
         * `700-7200-0269` Organizations and Societies: A business that is engaged in promoting
         * business, and non-business, organizations and the interests of their members.
         */
        val ORGANIZATIONS_AND_SOCIETIES: PlaceCategoryLeaf =
            leaf("700-7200-0269", "Organizations and Societies")

        /**
         * `700-7200-0270` Entertainment and Recreation: A business that provides services to
         * produce or organize live performances and shows in the performing arts, spectator sports,
         * or related industries (for example, actors and actresses, singers, dancers, musical
         * groups, artists, athletes, and freelance entertainers).
         */
        val ENTERTAINMENT_AND_RECREATION: PlaceCategoryLeaf =
            leaf("700-7200-0270", "Entertainment and Recreation")

        /**
         * `700-7200-0271` Finance and Insurance: A business that provides trust, fiduciary, and
         * custody services to others, public finance and establishments acting as agents (for
         * example, brokers) in selling annuities and insurance policies.
         */
        val FINANCE_AND_INSURANCE: PlaceCategoryLeaf =
            leaf("700-7200-0271", "Finance and Insurance")

        /**
         * `700-7200-0272` Healthcare and Healthcare Support Services: A business that provides
         * healthcare and healthcare support services not covered by other Hospital or Health Care
         * Facility categories (for example, dietician, speech therapist).
         */
        val HEALTHCARE_AND_HEALTHCARE_SUPPORT_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0272", "Healthcare and Healthcare Support Services")

        /**
         * `700-7200-0274` Rental and Leasing: A business that provides rental and leasing services
         * not covered by other categories (for example, boat rentals, medical equipment rental, and
         * so on).
         */
        val RENTAL_AND_LEASING: PlaceCategoryLeaf = leaf("700-7200-0274", "Rental and Leasing")

        /**
         * `700-7200-0275` Repair and Maintenance Services: A business that provides repair and
         * maintenance of commercial and industrial machinery/equipment in addition to goods not
         * covered by other categories. This excludes businesses that provide repair services for
         * commercial goods.
         */
        val REPAIR_AND_MAINTENANCE_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0275", "Repair and Maintenance Services")

        /**
         * `700-7200-0276` Printing and Publishing: A business that provides the printing and
         * publishing of products, such as books, journals, or software.
         */
        val PRINTING_AND_PUBLISHING: PlaceCategoryLeaf =
            leaf("700-7200-0276", "Printing and Publishing")

        /**
         * `700-7200-0277` Specialty Trade Contractors: A business or person limited and specialized
         * to a specific segment or trade for new contacts, additions, alterations, maintenance, or
         * repairs.
         */
        val SPECIALTY_TRADE_CONTRACTORS: PlaceCategoryLeaf =
            leaf("700-7200-0277", "Specialty Trade Contractors")

        /**
         * `700-7200-0278` Towing Service: A business that provides towing light or heavy motor
         * vehicles, including boats, both local and long distance. These establishments may provide
         * incidental services, such as storage and emergency road repair services.
         */
        val TOWING_SERVICE: PlaceCategoryLeaf = leaf("700-7200-0278", "Towing Service")

        /**
         * `700-7200-0279` Translation and Interpretation Services: A business that translates
         * written material and speech from one language to another, and could also provide sign
         * language services.
         */
        val TRANSLATION_AND_INTERPRETATION_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0279", "Translation and Interpretation Services")

        /**
         * `700-7200-0324` Apartment Rental-Flat Rental: A business that specializes in the
         * marketing, brokering, and media outlets for the rental of apartments and flats by the
         * public.
         */
        val APARTMENT_RENTAL_FLAT_RENTAL: PlaceCategoryLeaf =
            leaf("700-7200-0324", "Apartment Rental-Flat Rental")

        /**
         * `700-7200-0328` B2B Sales and Services: A business that sells products and services to
         * other businesses. These types of places generally include manufacturers and wholesalers.
         */
        val B2B_SALES_AND_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0328", "B2B Sales and Services")

        /**
         * `700-7200-0329` B2B Restaurant Services: A business that specializes in sales and
         * services to restaurants and the restaurant industry, such as wholesale kitchen appliance
         * stores and restaurant food suppliers.
         */
        val B2B_RESTAURANT_SERVICES: PlaceCategoryLeaf =
            leaf("700-7200-0329", "B2B Restaurant Services")

        /**
         * `700-7200-0330` Aviation: A business that repairs, maintains or sells airplanes, and
         * related parts and accessories.
         */
        val AVIATION: PlaceCategoryLeaf = leaf("700-7200-0330", "Aviation")

        /**
         * `700-7200-0342` Interior and Exterior Design: A business that provides interior and
         * exterior decoration services.
         */
        val INTERIOR_AND_EXTERIOR_DESIGN: PlaceCategoryLeaf =
            leaf("700-7200-0342", "Interior and Exterior Design")

        /**
         * `700-7200-0344` Property Management: A business that operates and manages real property.
         */
        val PROPERTY_MANAGEMENT: PlaceCategoryLeaf = leaf("700-7200-0344", "Property Management")

        /**
         * `700-7200-0345` Financial Investment Firm: A business that invests capital for the
         * purposes of appreciation, dividends or interest earnings.
         */
        val FINANCIAL_INVESTMENT_FIRM: PlaceCategoryLeaf =
            leaf("700-7200-0345", "Financial Investment Firm")

        /**
         * `700-7200-0351` Warehouse: A business facility that provides storage for goods of other
         * business.
         */
        val WAREHOUSE: PlaceCategoryLeaf = leaf("700-7200-0351", "Warehouse")

        /**
         * `700-7200-0352` Fulfillment and Distribution Center: A business facility that stores,
         * receives, processes, and delivers customer orders on behalf of another retailer.
         */
        val FULFILLMENT_AND_DISTRIBUTION_CENTER: PlaceCategoryLeaf =
            leaf("700-7200-0352", "Fulfillment and Distribution Center")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                COMMERCIAL_SERVICES,
                ADVERTISING_MARKETING_PR_AND_MARKET_RESEARCH,
                CATERING_AND_OTHER_FOOD_SERVICES,
                CONSTRUCTION,
                CUSTOMER_CARE_SERVICE_CENTER,
                ENGINEERING_AND_SCIENTIFIC_SERVICES,
                FARMING,
                FOOD_PRODUCTION,
                HUMAN_RESOURCES_AND_RECRUITING_SERVICES,
                INVESTIGATION_SERVICES,
                IT_AND_OFFICE_EQUIPMENT_SERVICES,
                LANDSCAPING_SERVICES,
                LOCKSMITHS_AND_SECURITY_SYSTEMS_SERVICES,
                MANAGEMENT_AND_CONSULTING_SERVICES,
                MANUFACTURING,
                MINING_QUARRYING_AND_OTHER_EXTRACTION,
                MODELING_AGENCIES,
                MOTORCYCLE_SERVICE_AND_MAINTENANCE,
                ORGANIZATIONS_AND_SOCIETIES,
                ENTERTAINMENT_AND_RECREATION,
                FINANCE_AND_INSURANCE,
                HEALTHCARE_AND_HEALTHCARE_SUPPORT_SERVICES,
                RENTAL_AND_LEASING,
                REPAIR_AND_MAINTENANCE_SERVICES,
                PRINTING_AND_PUBLISHING,
                SPECIALTY_TRADE_CONTRACTORS,
                TOWING_SERVICE,
                TRANSLATION_AND_INTERPRETATION_SERVICES,
                APARTMENT_RENTAL_FLAT_RENTAL,
                B2B_SALES_AND_SERVICES,
                B2B_RESTAURANT_SERVICES,
                AVIATION,
                INTERIOR_AND_EXTERIOR_DESIGN,
                PROPERTY_MANAGEMENT,
                FINANCIAL_INVESTMENT_FIRM,
                WAREHOUSE,
                FULFILLMENT_AND_DISTRIBUTION_CENTER,
            )
        }
    }

    /**
     * `700-7250` Business-Industry: Businesses that employ people in and around the city in which
     * it is located.
     */
    object BusinessIndustry : PlaceCategorySubgroup("700-7250", "Business-Industry") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7250-0136` Business Facility: A business establishment that employs people in and
         * around the city in which it is located.
         */
        val BUSINESS_FACILITY: PlaceCategoryLeaf = leaf("700-7250-0136", "Business Facility")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                BUSINESS_FACILITY,
            )
        }
    }

    /** `700-7300` Police-Fire-Emergency: Municipal emergency services. */
    object PoliceFireEmergency : PlaceCategorySubgroup("700-7300", "Police-Fire-Emergency") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7300-0110` Police Box: A phone booth, kiosk, or small office which houses police
         * officers for more localized service than full sized police stations.
         */
        val POLICE_BOX: PlaceCategoryLeaf = leaf("700-7300-0110", "Police Box")

        /** `700-7300-0111` Police Station: The office or headquarters of a local police force. */
        val POLICE_STATION: PlaceCategoryLeaf = leaf("700-7300-0111", "Police Station")

        /**
         * `700-7300-0112` Police Services-Security: An organization that provides security
         * services, such event security, campus security, mall security or campus police.
         */
        val POLICE_SERVICES_SECURITY: PlaceCategoryLeaf =
            leaf("700-7300-0112", "Police Services-Security")

        /**
         * `700-7300-0113` Fire Department: A local or municipal authority in charge of preventing
         * and fighting fires. Staff may include full-time employees or volunteers.
         */
        val FIRE_DEPARTMENT: PlaceCategoryLeaf = leaf("700-7300-0113", "Fire Department")

        /**
         * `700-7300-0280` Ambulance Services: A licensed service provider of ground or air
         * transportation for injured or sick people. Organizations that provide these services use
         * specially designed and equipped vehicles and equipment, as well as trained personnel who
         * are licensed or certified as required by law.
         */
        val AMBULANCE_SERVICES: PlaceCategoryLeaf = leaf("700-7300-0280", "Ambulance Services")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                POLICE_BOX,
                POLICE_STATION,
                POLICE_SERVICES_SECURITY,
                FIRE_DEPARTMENT,
                AMBULANCE_SERVICES,
            )
        }
    }

    /**
     * `700-7400` Consumer Services: An organization that provides consumer services for a variety
     * of products for used by the public.
     */
    object ConsumerServices : PlaceCategorySubgroup("700-7400", "Consumer Services") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7400-0000` Consumer Services: An organization that provides consumer services for a
         * variety of products used by the public. This is a base-level category that should be used
         * for all places that do not fit other categories defined for Consumer Services
         * (700-7400-xxxx).
         */
        val CONSUMER_SERVICES: PlaceCategoryLeaf = leaf("700-7400-0000", "Consumer Services")

        /**
         * `700-7400-0133` Travel Agent-Ticketing: A business that sells travel arrangements and
         * itineraries for travelers. Services may include air accommodations and ground
         * transportation. This category includes travel advisors, travel insurance brokers, travel
         * agents, and more.
         */
        val TRAVEL_AGENT_TICKETING: PlaceCategoryLeaf =
            leaf("700-7400-0133", "Travel Agent-Ticketing")

        /**
         * `700-7400-0137` Dry Cleaning and Laundry: A business that provides professional dry
         * cleaning or laundry services.
         */
        val DRY_CLEANING_AND_LAUNDRY: PlaceCategoryLeaf =
            leaf("700-7400-0137", "Dry Cleaning and Laundry")

        /**
         * `700-7400-0138` Attorney: A business that provides legal representation or counsel with
         * respect to legal matters.
         */
        val ATTORNEY: PlaceCategoryLeaf = leaf("700-7400-0138", "Attorney")

        /**
         * `700-7400-0140` Boating: A business that sells boating and water sport equipment. Some
         * establishments may also provide other services including repair, maintenance or rental.
         */
        val BOATING: PlaceCategoryLeaf = leaf("700-7400-0140", "Boating")

        /**
         * `700-7400-0141` Business Service: A retail or business establishment that provides
         * professional services to other businesses, such as printing, photocopying, graphic design
         * and advertising.
         */
        val BUSINESS_SERVICE: PlaceCategoryLeaf = leaf("700-7400-0141", "Business Service")

        /**
         * `700-7400-0142` Funeral Director: A business that provides burial or cremation services,
         * and also assists in funeral rites. Funeral directors might also be known as morticians or
         * undertakers.
         */
        val FUNERAL_DIRECTOR: PlaceCategoryLeaf = leaf("700-7400-0142", "Funeral Director")

        /**
         * `700-7400-0143` Mover: A business that provides services for transporting household or
         * office goods from one location to another.
         */
        val MOVER: PlaceCategoryLeaf = leaf("700-7400-0143", "Mover")

        /**
         * `700-7400-0144` Photography: A business that provides professional photography services,
         * the development of photos, or photographic supplies.
         */
        val PHOTOGRAPHY: PlaceCategoryLeaf = leaf("700-7400-0144", "Photography")

        /**
         * `700-7400-0145` Real Estate Services: A business or person licensed to buy, sell, or
         * broker real estate loans or services, and act as an intermediary on behalf of other
         * individuals or businesses during property transactions (e.g., real estate agent, broker,
         * realtor, buyer's agent, etc.).
         */
        val REAL_ESTATE_SERVICES: PlaceCategoryLeaf = leaf("700-7400-0145", "Real Estate Services")

        /**
         * `700-7400-0146` Repair Service: A business that provides repair service for various types
         * of consumer goods. Most repair service businesses specialize in specific types of repair,
         * such as electronics or shoes. This category excludes auto service.
         */
        val REPAIR_SERVICE: PlaceCategoryLeaf = leaf("700-7400-0146", "Repair Service")

        /**
         * `700-7400-0147` Social Service: A business that provides services to assist disadvantaged
         * people and advance human welfare. Social services includes both private and government
         * funded institutions.
         */
        val SOCIAL_SERVICE: PlaceCategoryLeaf = leaf("700-7400-0147", "Social Service")

        /**
         * `700-7400-0148` Storage: A business that provides long-term or short-term storage
         * services.
         */
        val STORAGE: PlaceCategoryLeaf = leaf("700-7400-0148", "Storage")

        /**
         * `700-7400-0149` Tailor and Alteration: A business that makes, mends, or alters garments.
         */
        val TAILOR_AND_ALTERATION: PlaceCategoryLeaf =
            leaf("700-7400-0149", "Tailor and Alteration")

        /**
         * `700-7400-0150` Tax Service: A business that provides tax preparation and filing
         * services.
         */
        val TAX_SERVICE: PlaceCategoryLeaf = leaf("700-7400-0150", "Tax Service")

        /**
         * `700-7400-0151` Utilities: A business that provides public utilities, such as water,
         * electric or other service. Places may be privately or government funded.
         */
        val UTILITIES: PlaceCategoryLeaf = leaf("700-7400-0151", "Utilities")

        /**
         * `700-7400-0152` Waste and Sanitary: A business that provides the transfer, disposal,
         * holding, cleaning, or treatment of solid waste material. Places may be privately or
         * government funded.
         */
        val WASTE_AND_SANITARY: PlaceCategoryLeaf = leaf("700-7400-0152", "Waste and Sanitary")

        /**
         * `700-7400-0281` Bicycle Service and Maintenance: A business that provides bicycle repair
         * and maintenance services.
         */
        val BICYCLE_SERVICE_AND_MAINTENANCE: PlaceCategoryLeaf =
            leaf("700-7400-0281", "Bicycle Service and Maintenance")

        /** `700-7400-0282` Bill Payment Service: A business that provides bill payment services. */
        val BILL_PAYMENT_SERVICE: PlaceCategoryLeaf = leaf("700-7400-0282", "Bill Payment Service")

        /**
         * `700-7400-0283` Body Piercing and Tattoos: A business that provides body piercing or
         * tattoo services.
         */
        val BODY_PIERCING_AND_TATTOOS: PlaceCategoryLeaf =
            leaf("700-7400-0283", "Body Piercing and Tattoos")

        /**
         * `700-7400-0284` Wedding Services and Bridal Studio: A business that sells or rents bridal
         * gowns or provides bridal services. Some businesses may sell wedding products or other
         * related services.
         */
        val WEDDING_SERVICES_AND_BRIDAL_STUDIO: PlaceCategoryLeaf =
            leaf("700-7400-0284", "Wedding Services and Bridal Studio")

        /**
         * `700-7400-0285` Internet Cafe: A business that provides public access to the Internet.
         * Some businesses may provide computers, or food and beverage service.
         */
        val INTERNET_CAFE: PlaceCategoryLeaf = leaf("700-7400-0285", "Internet Cafe")

        /**
         * `700-7400-0286` Kindergarten and Childcare: A business that provides day care of infants
         * or children. Some businesses may offer educational programs. Other childcare businesses
         * may include nanny, au pair, and other baby sitting services.
         */
        val KINDERGARTEN_AND_CHILDCARE: PlaceCategoryLeaf =
            leaf("700-7400-0286", "Kindergarten and Childcare")

        /**
         * `700-7400-0287` Maid Services: A business that provides cleaning services, including
         * live-in domestic help and periodic maid service.
         */
        val MAID_SERVICES: PlaceCategoryLeaf = leaf("700-7400-0287", "Maid Services")

        /**
         * `700-7400-0288` Marriage and Match Making Services: A business that provides dating or
         * relationship services for individuals seeking romantic partnerships or friendships.
         */
        val MARRIAGE_AND_MATCH_MAKING_SERVICES: PlaceCategoryLeaf =
            leaf("700-7400-0288", "Marriage and Match Making Services")

        /**
         * `700-7400-0289` Public Administration: A business that provides public administration
         * services not covered by other specialized categories, such as Police Service and Fire
         * Department.
         */
        val PUBLIC_ADMINISTRATION: PlaceCategoryLeaf =
            leaf("700-7400-0289", "Public Administration")

        /**
         * `700-7400-0292` Wellness Center and Services: A business that provides a variety of
         * services for the purpose of improving health, beauty and relaxation. Typical services
         * might include personal care treatments such as acupuncture, massages, or Chinese
         * medicine.
         */
        val WELLNESS_CENTER_AND_SERVICES: PlaceCategoryLeaf =
            leaf("700-7400-0292", "Wellness Center and Services")

        /**
         * `700-7400-0293` Pet Care: A business that provides specialized pet care services, such as
         * boarding, grooming, sitting, walking, and training. This category excludes veterinary
         * services.
         */
        val PET_CARE: PlaceCategoryLeaf = leaf("700-7400-0293", "Pet Care")

        /**
         * `700-7400-0327` Legal Services: A business that provides specialized legal services, such
         * as notary public, divorce assistance, mediation services, and paralegal services.
         */
        val LEGAL_SERVICES: PlaceCategoryLeaf = leaf("700-7400-0327", "Legal Services")

        /** `700-7400-0343` Tanning Salon: A business that provides cosmetic tanning services. */
        val TANNING_SALON: PlaceCategoryLeaf = leaf("700-7400-0343", "Tanning Salon")

        /**
         * `700-7400-0352` Recycling Center: A business that accepts, treats and processes waste
         * material for reuse.
         */
        val RECYCLING_CENTER: PlaceCategoryLeaf = leaf("700-7400-0352", "Recycling Center")

        /**
         * `700-7400-0365` Electrical: A repair service where a consumer can take electrical items
         * for repair.
         */
        val ELECTRICAL: PlaceCategoryLeaf = leaf("700-7400-0365", "Electrical")

        /**
         * `700-7400-0366` Plumbing: A repair service where a consumer can take plumbing items in
         * for repair, or book an appointment with a plumber to visit the premises.
         */
        val PLUMBING: PlaceCategoryLeaf = leaf("700-7400-0366", "Plumbing")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CONSUMER_SERVICES,
                TRAVEL_AGENT_TICKETING,
                DRY_CLEANING_AND_LAUNDRY,
                ATTORNEY,
                BOATING,
                BUSINESS_SERVICE,
                FUNERAL_DIRECTOR,
                MOVER,
                PHOTOGRAPHY,
                REAL_ESTATE_SERVICES,
                REPAIR_SERVICE,
                SOCIAL_SERVICE,
                STORAGE,
                TAILOR_AND_ALTERATION,
                TAX_SERVICE,
                UTILITIES,
                WASTE_AND_SANITARY,
                BICYCLE_SERVICE_AND_MAINTENANCE,
                BILL_PAYMENT_SERVICE,
                BODY_PIERCING_AND_TATTOOS,
                WEDDING_SERVICES_AND_BRIDAL_STUDIO,
                INTERNET_CAFE,
                KINDERGARTEN_AND_CHILDCARE,
                MAID_SERVICES,
                MARRIAGE_AND_MATCH_MAKING_SERVICES,
                PUBLIC_ADMINISTRATION,
                WELLNESS_CENTER_AND_SERVICES,
                PET_CARE,
                LEGAL_SERVICES,
                TANNING_SALON,
                RECYCLING_CENTER,
                ELECTRICAL,
                PLUMBING,
            )
        }
    }

    /**
     * `700-7450` Post Office: An office or station that receives, sorts, dispatches and delivers
     * mail to a specific area or region.
     */
    object PostOffice : PlaceCategorySubgroup("700-7450", "Post Office") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7450-0114` Post Office: An office or station that receives, sorts, dispatches and
         * delivers mail to a specific area or region. This includes local branch or national
         * offices. Some locations may also sell postage and provide other delivery-related
         * services.
         */
        val POST_OFFICE: PlaceCategoryLeaf = leaf("700-7450-0114", "Post Office")

        /**
         * `700-7450-0115` Postal Collection Box: A postal collection box (also commonly referred to
         * as a mailbox) is used for dropping off letters or small packages for outbound mail
         * processing. These are usually located curbside so motorists may drop off their mail.
         */
        val POSTAL_COLLECTION_BOX: PlaceCategoryLeaf =
            leaf("700-7450-0115", "Postal Collection Box")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                POST_OFFICE,
                POSTAL_COLLECTION_BOX,
            )
        }
    }

    /**
     * `700-7460` Tourist Information: Businesses that provide a variety of information for visiting
     * tourists, such as event schedules, lodging/accommodations, restaurants, attractions and more.
     */
    object TouristInformation : PlaceCategorySubgroup("700-7460", "Tourist Information") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7460-0115` Tourist Information: A business that provides a variety of information
         * for visiting tourists, such as event schedules, lodging/accommodations, restaurants,
         * attractions and more. Tourist information may also include chambers-of-commerce, as well
         * as convention and visitor bureaus.
         */
        val TOURIST_INFORMATION: PlaceCategoryLeaf = leaf("700-7460-0115", "Tourist Information")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                TOURIST_INFORMATION,
            )
        }
    }

    /** `700-7600` Fueling Station: Businesses that sell fuel for vehicles. */
    object FuelingStation : PlaceCategorySubgroup("700-7600", "Fueling Station") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7600-0000` Fueling Station: A business that sells fuel for vehicles. This is a
         * base-level category that should be used for all places that do not fit other categories
         * defined for Fueling Station (700-7600-xxxx).
         */
        val FUELING_STATION: PlaceCategoryLeaf = leaf("700-7600-0000", "Fueling Station")

        /**
         * `700-7600-0116` Petrol-Gasoline Station: A business that sells fuel, oil, and other
         * motoring supplies.
         */
        val PETROL_GASOLINE_STATION: PlaceCategoryLeaf =
            leaf("700-7600-0116", "Petrol-Gasoline Station")

        /**
         * `700-7600-0322` EV Charging Station for passenger cars: A charging station that provides
         * recharging services for electric vehicles.
         */
        val EV_CHARGING_STATION_FOR_PASSENGER_CARS: PlaceCategoryLeaf =
            leaf("700-7600-0322", "EV Charging Station for passenger cars")

        /**
         * `700-7600-0323` EV Charging Station for Trucks: A charging station that provides
         * recharging services for trucks and buses.
         */
        val EV_CHARGING_STATION_FOR_TRUCKS: PlaceCategoryLeaf =
            leaf("700-7600-0323", "EV Charging Station for Trucks")

        /**
         * `700-7600-0324` EV Charging Station for 2-wheelers and Light Vehicles: A charging station
         * that provides recharging services for two-wheelers (e.g. motorbike or moped) and light
         * electric vehicles.
         */
        val EV_CHARGING_STATION_FOR_2_WHEELERS_AND_LIGHT_VEHICLES: PlaceCategoryLeaf =
            leaf("700-7600-0324", "EV Charging Station for 2-wheelers and Light Vehicles")

        /**
         * `700-7600-0325` EV Battery Swap Station: A location in which a battery (for an electric
         * vehicle) can be exchanged for one that is fully charged.
         */
        val EV_BATTERY_SWAP_STATION: PlaceCategoryLeaf =
            leaf("700-7600-0325", "EV Battery Swap Station")

        /**
         * `700-7600-0444` Hydrogen Fuel Station: A fuel station which specifically provides
         * Hydrogen (H2) fuel for vehicles.
         */
        val HYDROGEN_FUEL_STATION: PlaceCategoryLeaf =
            leaf("700-7600-0444", "Hydrogen Fuel Station")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                FUELING_STATION,
                PETROL_GASOLINE_STATION,
                EV_CHARGING_STATION_FOR_PASSENGER_CARS,
                EV_CHARGING_STATION_FOR_TRUCKS,
                EV_CHARGING_STATION_FOR_2_WHEELERS_AND_LIGHT_VEHICLES,
                EV_BATTERY_SWAP_STATION,
                HYDROGEN_FUEL_STATION,
            )
        }
    }

    /** `700-7800` Car Dealer-Sales: Businesses that sell new automobiles and motorcycles. */
    object CarDealerSales : PlaceCategorySubgroup("700-7800", "Car Dealer-Sales") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7800-0118` Automobile Dealership-New Cars: A business that sells new automobiles.
         */
        val AUTOMOBILE_DEALERSHIP_NEW_CARS: PlaceCategoryLeaf =
            leaf("700-7800-0118", "Automobile Dealership-New Cars")

        /**
         * `700-7800-0119` Automobile Dealership-Used Cars: A business that sells previously-owned
         * automobiles.
         */
        val AUTOMOBILE_DEALERSHIP_USED_CARS: PlaceCategoryLeaf =
            leaf("700-7800-0119", "Automobile Dealership-Used Cars")

        /** `700-7800-0120` Motorcycle Dealership: A business that sells motorcycles. */
        val MOTORCYCLE_DEALERSHIP: PlaceCategoryLeaf =
            leaf("700-7800-0120", "Motorcycle Dealership")

        /**
         * `700-7800-0200` EV Dealership-New Vehicles: A business that sells new electric vehicles.
         */
        val EV_DEALERSHIP_NEW_VEHICLES: PlaceCategoryLeaf =
            leaf("700-7800-0200", "EV Dealership-New Vehicles")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                AUTOMOBILE_DEALERSHIP_NEW_CARS,
                AUTOMOBILE_DEALERSHIP_USED_CARS,
                MOTORCYCLE_DEALERSHIP,
                EV_DEALERSHIP_NEW_VEHICLES,
            )
        }
    }

    /** `700-7850` Car Repair-Service: Businesses that provide automotive repair services. */
    object CarRepairService : PlaceCategorySubgroup("700-7850", "Car Repair-Service") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7850-0000` Car Repair-Service: A business that provides automotive repair services.
         * This is a base-level category that should be used for all places that do not fit other
         * categories defined for Car Repair-Service (700-7850-xxxx).
         */
        val CAR_REPAIR_SERVICE: PlaceCategoryLeaf = leaf("700-7850-0000", "Car Repair-Service")

        /**
         * `700-7850-0121` Car Wash-Detailing: A business that provides automobile cleaning
         * services. Some businesses may provide exterior detailing, interior detailing, or both.
         */
        val CAR_WASH_DETAILING: PlaceCategoryLeaf = leaf("700-7850-0121", "Car Wash-Detailing")

        /**
         * `700-7850-0122` Car Repair: A business that provides automotive repair services, usually
         * as part of a major chain.
         */
        val CAR_REPAIR: PlaceCategoryLeaf = leaf("700-7850-0122", "Car Repair")

        /**
         * `700-7850-0123` Auto Parts: A business that sells parts and accessories for automobiles.
         * Auto dealerships that sell auto parts as a sub-service are not applicable.
         */
        val AUTO_PARTS: PlaceCategoryLeaf = leaf("700-7850-0123", "Auto Parts")

        /** `700-7850-0124` Emission Testing: A business that provides emission testing services. */
        val EMISSION_TESTING: PlaceCategoryLeaf = leaf("700-7850-0124", "Emission Testing")

        /**
         * `700-7850-0125` Tire Repair: A business that sells automobile tires and related services.
         */
        val TIRE_REPAIR: PlaceCategoryLeaf = leaf("700-7850-0125", "Tire Repair")

        /**
         * `700-7850-0126` Truck Repair: A business that provides repair services for trucks and
         * tractor trailers. Usually as part of a major chain.
         */
        val TRUCK_REPAIR: PlaceCategoryLeaf = leaf("700-7850-0126", "Truck Repair")

        /**
         * `700-7850-0127` Van Repair: A business that provides repair services for vans. Usually as
         * part of a major chain.
         */
        val VAN_REPAIR: PlaceCategoryLeaf = leaf("700-7850-0127", "Van Repair")

        /**
         * `700-7850-0128` Road Assistance: A business that provides roadside assistance to
         * motorists that have suffered mechanical failure. Some businesses also service bicyclists.
         */
        val ROAD_ASSISTANCE: PlaceCategoryLeaf = leaf("700-7850-0128", "Road Assistance")

        /**
         * `700-7850-0129` Automobile Club: A business that provides emergency road services,
         * travel-related services, and other membership services, such as travel insurance,
         * lodging, car repair, and car rental.
         */
        val AUTOMOBILE_CLUB: PlaceCategoryLeaf = leaf("700-7850-0129", "Automobile Club")

        /**
         * `700-7850-0200` EV Repair: A business that provides repair services for electric
         * vehicles.
         */
        val EV_REPAIR: PlaceCategoryLeaf = leaf("700-7850-0200", "EV Repair")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                CAR_REPAIR_SERVICE,
                CAR_WASH_DETAILING,
                CAR_REPAIR,
                AUTO_PARTS,
                EMISSION_TESTING,
                TIRE_REPAIR,
                TRUCK_REPAIR,
                VAN_REPAIR,
                ROAD_ASSISTANCE,
                AUTOMOBILE_CLUB,
                EV_REPAIR,
            )
        }
    }

    /** `700-7851` Car Rental: Businesses that rent or lease automobiles. */
    object CarRental : PlaceCategorySubgroup("700-7851", "Car Rental") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /** `700-7851-0117` Rental Car Agency: A business that rents or leases automobiles. */
        val RENTAL_CAR_AGENCY: PlaceCategoryLeaf = leaf("700-7851-0117", "Rental Car Agency")

        /**
         * `700-7851-0127` Carshare Location: A designated location where a person can pick up
         * and/or drop off a rental vehicle for temporary use. Carshare vehicles are rented for
         * shorter time periods and are often privately owned (which is different for car rental).
         */
        val CARSHARE_LOCATION: PlaceCategoryLeaf = leaf("700-7851-0127", "Carshare Location")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                RENTAL_CAR_AGENCY,
                CARSHARE_LOCATION,
            )
        }
    }

    /**
     * `700-7900` Truck-Semi Dealer-Services: Business that sell or service trucks and tractor
     * trailers.
     */
    object TruckSemiDealerServices :
        PlaceCategorySubgroup("700-7900", "Truck-Semi Dealer-Services") {
        override val group: PlaceCategoryGroup get() = BusinessAndServices

        /**
         * `700-7900-0000` Truck-Semi Dealer-Services: A business that sells or services trucks and
         * tractor trailers. This is a base-level category that should be used for all places that
         * do not fit other categories defined for Truck-Semi Dealers-Service (700-7900-xxxx).
         */
        val TRUCK_SEMI_DEALER_SERVICES: PlaceCategoryLeaf =
            leaf("700-7900-0000", "Truck-Semi Dealer-Services")

        /**
         * `700-7900-0130` Truck Dealership: A trucking dealership that sells new heavy
         * trucks/lorries.
         */
        val TRUCK_DEALERSHIP: PlaceCategoryLeaf = leaf("700-7900-0130", "Truck Dealership")

        /**
         * `700-7900-0131` Truck Parking: A designated area for parking heavy trucks/lorries. Some
         * areas may also provide other services, such as restaurants, restrooms, or warehouse
         * stores. The Place record should reflect the name of the associated facility.
         */
        val TRUCK_PARKING: PlaceCategoryLeaf = leaf("700-7900-0131", "Truck Parking")

        /**
         * `700-7900-0132` Truck Stop-Plaza: A business that provides fuel for heavy trucks. Some
         * areas may also provide other services, such as restaurants, service facilities, sleeping
         * and shower facilities.
         */
        val TRUCK_STOP_PLAZA: PlaceCategoryLeaf = leaf("700-7900-0132", "Truck Stop-Plaza")

        /**
         * `700-7900-0323` Truck Wash: A business that provides cleaning services for trucks, semis
         * and tractor trailers. Some businesses may offer external detailing, internal detailing or
         * both.
         */
        val TRUCK_WASH: PlaceCategoryLeaf = leaf("700-7900-0323", "Truck Wash")

        override val categories: List<PlaceCategoryLeaf> by lazy {
            listOf(
                TRUCK_SEMI_DEALER_SERVICES,
                TRUCK_DEALERSHIP,
                TRUCK_PARKING,
                TRUCK_STOP_PLAZA,
                TRUCK_WASH,
            )
        }
    }

    override val subgroups: List<PlaceCategorySubgroup> by lazy {
        listOf(
            Banking,
            ATM,
            MoneyCashServices,
            CommunicationMedia,
            CommercialServices,
            BusinessIndustry,
            PoliceFireEmergency,
            ConsumerServices,
            PostOffice,
            TouristInformation,
            FuelingStation,
            CarDealerSales,
            CarRepairService,
            CarRental,
            TruckSemiDealerServices,
        )
    }
}
