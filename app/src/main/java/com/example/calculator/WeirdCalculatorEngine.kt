package com.example.calculator

import kotlin.math.roundToInt

data class UnitDefinition(val key: String, val displayName: String, val factorToBase: Double)

data class ComparisonObject(
    val name: String,
    val category: String,
    val baseValue: Double, // in base unit of that category
    val description: String,
    val fact: String,
    val icon: String // Emoji representation
)

object WeirdCalculatorEngine {

    val categories = listOf(
        "Length",
        "Weight / Mass",
        "Time",
        "Speed",
        "Energy",
        "Digital Storage",
        "Money",
        "Astronomy",
        "Human Body",
        "Historical"
    )

    val unitsMap = mapOf(
        "Length" to listOf(
            UnitDefinition("mm", "Millimeter (mm)", 0.001),
            UnitDefinition("cm", "Centimeter (cm)", 0.01),
            UnitDefinition("m", "Meter (m)", 1.0),
            UnitDefinition("km", "Kilometer (km)", 1000.0),
            UnitDefinition("in", "Inch (in)", 0.0254),
            UnitDefinition("ft", "Foot (ft)", 0.3048),
            UnitDefinition("yd", "Yard (yd)", 0.9144),
            UnitDefinition("mi", "Mile (mi)", 1609.344),
            UnitDefinition("nmi", "Nautical Mile (nmi)", 1852.0),
            UnitDefinition("au", "Astronomical Unit (AU)", 1.495978707e11),
            UnitDefinition("ly", "Light Year (ly)", 9.4607304725808e15)
        ),
        "Weight / Mass" to listOf(
            UnitDefinition("mg", "Milligram (mg)", 1e-6),
            UnitDefinition("g", "Gram (g)", 0.001),
            UnitDefinition("kg", "Kilogram (kg)", 1.0),
            UnitDefinition("t", "Ton (t)", 1000.0),
            UnitDefinition("lb", "Pound (lb)", 0.45359237),
            UnitDefinition("oz", "Ounce (oz)", 0.028349523)
        ),
        "Time" to listOf(
            UnitDefinition("s", "Seconds (s)", 1.0),
            UnitDefinition("min", "Minutes (min)", 60.0),
            UnitDefinition("h", "Hours (h)", 3600.0),
            UnitDefinition("d", "Days (d)", 86400.0),
            UnitDefinition("w", "Weeks (w)", 604800.0),
            UnitDefinition("mo", "Months (mo)", 2.6297438e6),
            UnitDefinition("yr", "Years (yr)", 3.15576e7)
        ),
        "Speed" to listOf(
            UnitDefinition("m_s", "m/s", 1.0),
            UnitDefinition("km_h", "km/h", 0.277778),
            UnitDefinition("mph", "mph", 0.44704),
            UnitDefinition("knots", "knots", 0.514444)
        ),
        "Energy" to listOf(
            UnitDefinition("j", "Joules (J)", 1.0),
            UnitDefinition("kj", "Kilojoules (kJ)", 1000.0),
            UnitDefinition("cal", "Calories (Cal)", 4184.0),
            UnitDefinition("kwh", "Kilowatt-hours (kWh)", 3.6e6)
        ),
        "Digital Storage" to listOf(
            UnitDefinition("bit", "Bit (b)", 0.125),
            UnitDefinition("b", "Byte (B)", 1.0),
            UnitDefinition("kb", "Kilobyte (KB)", 1024.0),
            UnitDefinition("mb", "Megabyte (MB)", 1048576.0),
            UnitDefinition("gb", "Gigabyte (GB)", 1073741824.0),
            UnitDefinition("tb", "Terabyte (TB)", 1099511627776.0),
            UnitDefinition("pb", "Petabyte (PB)", 1125899906842624.0)
        ),
        "Money" to listOf(
            UnitDefinition("usd", "US Dollar ($)", 1.0),
            UnitDefinition("eur", "Euro (€)", 1.08),
            UnitDefinition("gbp", "British Pound (£)", 1.28),
            UnitDefinition("jpy", "Japanese Yen (¥)", 0.0064),
            UnitDefinition("cad", "Canadian Dollar ($)", 0.73),
            UnitDefinition("aud", "Australian Dollar ($)", 0.66)
        ),
        "Astronomy" to listOf(
            UnitDefinition("km", "Kilometers (km)", 1.0),
            UnitDefinition("au", "Astronomical Unit (AU)", 1.4959787e8),
            UnitDefinition("ly", "Light Year (ly)", 9.4607304725808e12),
            UnitDefinition("ld", "Lunar Distance (LD)", 384400.0)
        ),
        "Human Body" to listOf(
            UnitDefinition("steps", "Steps", 1.0),
            UnitDefinition("m", "Walking Distance (m)", 0.75),
            UnitDefinition("cal", "Energy Burned (kcal)", 0.04),
            UnitDefinition("min", "Walking Duration (min)", 0.0125)
        ),
        "Historical" to listOf(
            UnitDefinition("yr", "Years (yr)", 1.0),
            UnitDefinition("gen", "Human Generations", 25.0),
            UnitDefinition("oly", "Olympic Cycles", 4.0),
            UnitDefinition("pres", "US Presidential Terms", 4.0),
            UnitDefinition("orbit", "Earth Orbits", 1.0)
        )
    )

    val dbObjects = listOf(
        // Animals
        ComparisonObject("Domestic Cat", "Animals", 4.5, "Standard weight of a healthy domestic cat.", "Cats can rotate their ears 180 degrees and have 32 muscles in each ear!", "🐱"),
        ComparisonObject("Golden Retriever", "Animals", 30.0, "Average weight of a full-grown Golden Retriever.", "Golden Retrievers are famous for their soft mouths, able to carry raw eggs without breaking them.", "🦮"),
        ComparisonObject("African Elephant", "Animals", 5000.0, "Typical weight of an adult African savannah elephant.", "An elephant's temporal glands can secrete up to 10 liters of liquid a day during musth.", "🐘"),
        ComparisonObject("Blue Whale", "Animals", 150000.0, "Weight of the largest animal ever known to exist.", "A blue whale heartbeat can be heard from over two miles away under water!", "🐋"),
        ComparisonObject("Cheetah Peak speed", "Animals", 30.0, "The absolute land speed speed of a cheetah (30 m/s).", "A cheetah can accelerate from 0 to 60 miles per hour in just 3 seconds flat.", "🐆"),
        
        // Landmarks & Architecture
        ComparisonObject("Standard HB Pencil", "Landmarks", 0.19, "A standard yellow graphite pencil.", "A single pencil can draw a line about 35 miles long or write roughly 45,000 words.", "✏️"),
        ComparisonObject("Average Human", "Landmarks", 1.7, "Typical height of an adult human.", "Your bones are about four times stronger than concrete weight-for-weight.", "🧍"),
        ComparisonObject("Standard School Bus", "Landmarks", 12.0, "Approximate length of an American school bus.", "Most school buses are painted unique School Bus Yellow because it's highly visible.", "🚌"),
        ComparisonObject("American Football Field", "Landmarks", 110.0, "Length of a football field including both endzones (120 yards total).", "Field grass at high levels is often hybrid-woven into the ground to resist player impact.", "🏈"),
        ComparisonObject("Eiffel Tower", "Landmarks", 330.0, "The architectural height of Paris' iconic Eiffel Tower.", "During cold weather, the iron on the Eiffel Tower shrinks, making it 6 inches shorter.", "🗼"),
        ComparisonObject("Mount Everest", "Landmarks", 8848.0, "The height of Earth's tallest mountain above sea level.", "Due to tectonic plate movement, Mount Everest grows taller by about 4 millimeters each year.", "🏔️"),
        ComparisonObject("Earth Circumference", "Landmarks", 4.0075e7, "The equatorial circumference of mother Earth.", "If you could drive around the equator at 60 mph, it would take you about 17 days non-stop.", "🌍"),

        // Digital Things
        ComparisonObject("Standard Digital Photo", "Technology", 4.0e6, "Size of a high-resolution smartphone camera photo.", "More photos are taken in two minutes today than were taken in the entire 19th Century.", "📸"),
        ComparisonObject("Streaming Song", "Technology", 5.0e6, "The data storage consumed by a high-quality audio song file.", "The world's longest recorded song runs for over 1000 hours and was created using AI loops.", "🎵"),
        ComparisonObject("Standard PDF Book", "Technology", 1.0e7, "Medium size of an illustrated digital book.", "The Library of Congress is the largest library, containing over 38 million cataloged books.", "📚"),
        ComparisonObject("HD Movie Stream", "Technology", 1.5e9, "Normal file size of a standard 90-minute HD movie stream.", "Netflix streams consume about 3 GB of data per hour for Ultra HD videos.", "🎬"),
        ComparisonObject("4K Movie DL", "Technology", 1.5e10, "Average storage needed to download a movie in 4K resolution.", "A single raw uncompressed 4K video reel can weigh up to several Terabytes.", "🍿"),
        ComparisonObject("Modern AAA Video Game", "Technology", 7.0e10, "Size of a storage-hungry modern triple-A game.", "The entire source code of Doom (1993) fits on less than three floppy disks (3 MB).", "🎮"),

        // Energy Comparisons
        ComparisonObject("Smartphone Charge", "Energy", 40000.0, "The electrical energy needed to charge a current smartphone (approx 11 Wh).", "Charging your smartphone daily costs less than $1 per year in electricity bill costs.", "🔋"),
        ComparisonObject("Laptop Screen Hour", "Energy", 252000.0, "Electrical work of running a standard laptop for one hour.", "Laptops are roughly 80% more energy-efficient than desktop computers.", "💻"),
        ComparisonObject("LED Bulb Hour", "Energy", 36000.0, "Runtime energy for a standard 10 Watt energy LED bulb for one hour.", "LED bulbs use 75% less energy and last 25 times longer than incandescent bulbs.", "💡"),
        ComparisonObject("Average Home Daily Use", "Energy", 3.6e7, "Average electricity energy used in a household in one day (10 kWh).", "A single lightning bolt contains enough energy to power a household for up to 30 days.", "🏠"),

        // Purchasing power
        ComparisonObject("Slice of Pizza", "Money", 4.5, "Cost of a nice gourmet NY style slice.", "Over 3 billion pizzas are sold in the US alone each year.", "🍕"),
        ComparisonObject("Premium Cafe Latte", "Money", 5.5, "Price of a fancy barista latte coffee cup.", "The world's most expensive coffee is parsed from wild asian civet cat droppings.", "☕"),
        ComparisonObject("Premium Smartphone", "Money", 1000.0, "High-end smartphone cost.", "Modern smartphones have millions of times more computing power than NASA's Apollo 11 computer.", "📱"),
        ComparisonObject("Next-Gen Console", "Money", 500.0, "Popular console cost.", "The best-selling gaming console of all time remains the PlayStation 2 at 155 million units.", "🕹️")
    )

    fun getComparisons(category: String, baseUnitValue: Double): List<String> {
        val list = mutableListOf<String>()
        val value = baseUnitValue

        when (category) {
            "Length" -> {
                // Pencil (0.19m)
                val pencils = value / 0.19
                list.add("✏️ ~${formatCount(pencils)} Standard HB Pencils")
                
                // Human (1.7m)
                val humans = value / 1.7
                list.add("🧍 ~${formatCount(humans)} Average Humans")

                // School Bus (12m)
                val buses = value / 12.0
                if (buses >= 0.1) list.add("🚌 ~${formatCount(buses)} School Buses")

                // Football Field (110m)
                val fields = value / 110.0
                if (fields >= 0.05) list.add("🏈 ~${formatCount(fields)} Football Fields")

                // Eiffel Tower (330m)
                val eiffels = value / 330.0
                if (eiffels >= 0.01) list.add("🗼 ~${formatCount(eiffels)} Eiffel Towers")

                // Mount Everest (8848m)
                val everests = value / 8848.0
                if (everests >= 0.001) list.add("🏔️ ~${formatCount(everests)} Mount Everests")

                // Earth Circumference (4.0075e7m)
                val earths = value / 4.0075e7
                if (earths >= 0.0001) list.add("🌍 ~${formatCount(earths)} Earth Circumferences")
            }
            "Weight / Mass" -> {
                // Apples (0.2kg)
                val apples = value / 0.2
                list.add("🍎 ~${formatCount(apples)} Apples")

                // Cats (4.5kg)
                val cats = value / 4.5
                list.add("🐱 ~${formatCount(cats)} Domestic Cats")

                // Dogs (30kg)
                val dogs = value / 30.0
                if (dogs >= 0.1) list.add("🦮 ~${formatCount(dogs)} Golden Retrievers")

                // Cars (1500kg)
                val cars = value / 1500.0
                if (cars >= 0.05) list.add("🚗 ~${formatCount(cars)} Small Cars")

                // Elephants (5000kg)
                val elephants = value / 5000.0
                if (elephants >= 0.01) list.add("🐘 ~${formatCount(elephants)} African Elephants")

                // Blue Whales (150,000kg)
                val whales = value / 150000.0
                if (whales >= 0.001) list.add("🐋 ~${formatCount(whales)} Blue Whales")

                // Airplanes (200,000kg)
                val planes = value / 200000.0
                if (planes >= 0.001) list.add("✈️ ~${formatCount(planes)} Commercial Airplanes")
            }
            "Time" -> {
                // Heartbeats (80/min, i.e., 1.333/sec)
                val beats = value * (80.0 / 60.0)
                list.add("🫀 ~${formatCount(beats)} Human Heartbeats")

                // Breaths (16/min, i.e., 0.2667/sec)
                val breaths = value * (16.0 / 60.0)
                list.add("🪶 ~${formatCount(breaths)} Breaths Taken")

                // Blinks (15/min, i.e., 0.25/sec)
                val blinks = value * (15.0 / 60.0)
                list.add("👁️ ~${formatCount(blinks)} Eye Blinks")

                // School Lessons (45 min = 2700s)
                val lessons = value / 2700.0
                if (lessons >= 0.1) list.add("🏫 ~${formatCount(lessons)} School Lessons (45m)")

                // Movies Watched (2 hours = 7200s)
                val movies = value / 7200.0
                if (movies >= 0.05) list.add("🎬 ~${formatCount(movies)} Full Movies Watched")

                // Football Matches (90 min = 5400s)
                val matches = value / 5400.0
                if (matches >= 0.05) list.add("⚽ ~${formatCount(matches)} Football Matches")
            }
            "Speed" -> {
                // Walking human (1.4 m/s)
                val walk = value / 1.4
                list.add("🚶 ~${formatCount(walk)}x Walking Human speed")

                // Running athlete (8.0 m/s)
                val run = value / 8.0
                list.add("🏃 ~${formatCount(run)}x Running Athlete speed")

                // Cheetah (30.0 m/s)
                val cheetah = value / 30.0
                list.add("🐆 ~${formatCount(cheetah)}x Sprinting Cheetah speed")

                // Formula 1 (90.0 m/s)
                val f1 = value / 90.0
                list.add("🏎️ ~${formatCount(f1)}x Formula 1 Car speed")

                // Passenger jet (250 m/s)
                val jet = value / 250.0
                list.add("✈️ ~${formatCount(jet)}x Passenger Jet speed")

                // Sound (343 m/s)
                val sound = value / 343.0
                list.add("🔊 ~${formatCount(sound)}x Speed of Sound (Mach)")
            }
            "Energy" -> {
                // Phone charges (40,000 J)
                val phones = value / 40000.0
                list.add("🔋 ~${formatCount(phones)} full Smartphone charges")

                // Laptop Hours (252,000 J)
                val laptop = value / 252000.0
                list.add("💻 ~${formatCount(laptop)} Hours of Laptop usage")

                // LED Bulb Hours (36,000 J)
                val led = value / 36000.0
                list.add("💡 ~${formatCount(led)} Hours of LED Bulb light")

                // Food Calories (4184 J)
                val food = value / 4184.0
                list.add("🍎 ~${formatCount(food)} Food Calories (kcal)")

                // Pizza Slices (1,170,000 J)
                val pizza = value / 1170000.0
                list.add("🍕 ~${formatCount(pizza)} Slices of Pizza")

                // Home Daily Use (3.6e7 J)
                val home = value / 3.6e7
                if (home >= 0.01) list.add("🏠 ~${formatCount(home)} Days of Household electricity")
            }
            "Digital Storage" -> {
                // Photos (4 MB = 4.19e6 B)
                val photos = value / 4194304.0
                list.add("📸 ~${formatCount(photos)} Digital Photos (4MB)")

                // Songs (5 MB = 5.24e6 B)
                val songs = value / 5242880.0
                list.add("🎵 ~${formatCount(songs)} Streaming Songs (5MB)")

                // PDF Books (10 MB = 1.04e7 B)
                val books = value / 10485760.0
                if (books >= 0.1) list.add("📚 ~${formatCount(books)} PDF Books (10MB)")

                // HD Movies (1.5 GB = 1.61e9 B)
                val movies = value / 1610612736.0
                if (movies >= 0.01) list.add("🎬 ~${formatCount(movies)} HD Movie Streams")

                // 4K Movies (15 GB = 1.61e10 B)
                val ultra = value / 16106127360.0
                if (ultra >= 0.01) list.add("🍿 ~${formatCount(ultra)} 4K Movie Downloads")

                // Games (70 GB = 7.51e10 B)
                val games = value / 75161927680.0
                if (games >= 0.001) list.add("🎮 ~${formatCount(games)} AAA Video Games")
            }
            "Money" -> {
                // Coffee Cups ($5.50)
                val coffees = value / 5.50
                list.add("☕ ~${formatCount(coffees)} Premium Cafe Lattes")

                // Real NY Pizza Slice ($4.50)
                val pizza = value / 4.50
                list.add("🍕 ~${formatCount(pizza)} Slices of Pepperoni Pizza")

                // Movie Tickets ($12)
                val tickets = value / 12.0
                list.add("🎟️ ~${formatCount(tickets)} Cinema Tickets")

                // Games ($70)
                val games = value / 70.0
                list.add("🎮 ~${formatCount(games)} New Release Video Games")

                // Gaming Consoles ($500)
                val consoles = value / 500.0
                if (consoles >= 0.05) list.add("🕹️ ~${formatCount(consoles)} Next-Gen Video Game Consoles")

                // Smart Phone ($1000)
                val phones = value / 1000.0
                if (phones >= 0.01) list.add("📱 ~${formatCount(phones)} Flagship Smartphones")
            }
            "Astronomy" -> {
                // Distance to Moon (384,400 km)
                val moon = value / 384400.0
                list.add("🌙 ~${formatCount(moon)} Lunar Distances (Earth-to-Moon)")

                // Earth-Sun distance percentage (1.496e8 km)
                val sunPercentage = (value / 1.496e8) * 100.0
                list.add("☀️ ~${formatDecimal(sunPercentage, 4)}% of Earth-Sun Distance (1 AU)")

                // Light travel time
                val lightSec = value / 299792.0
                list.add("⚡ ~${formatDecimal(lightSec, 3)} seconds for light to travel this far")
            }
            "Human Body" -> {
                // Input is steps. Output walk distance, calories, walking time, sport equivalents.
                val distM = value * 0.75
                val kcal = value * 0.04
                val minutes = value * 0.0125

                list.add("🏃 Distance walked: ~${formatDecimal(distM / 1000.0, 2)} km (${formatCount(distM)} meters)")
                list.add("🔥 Energy expended: ~${formatDecimal(kcal, 1)} kcal")
                list.add("⏱️ Active walking time: ~${formatDecimal(minutes, 1)} minutes")
                list.add("🏊 Equivalent to ~${formatDecimal(kcal / 10.0, 1)} minutes of high-intensity swimming")
            }
            "Historical" -> {
                // Input is years.
                val gens = value / 25.0
                val oly = value / 4.0
                val pres = value / 4.0
                val orbits = value

                list.add("🌍 ~${formatDecimal(orbits, 1)} Earth orbits around the Sun")
                list.add("🧬 ~${formatDecimal(gens, 1)} Human Generations")
                list.add("🏅 ~${formatDecimal(oly, 1)} Olympic Games cycles")
                list.add("🇺🇸 ~${formatDecimal(pres, 1)} US Presidential terms")
            }
        }
        return list
    }

    fun getFunFactForCategory(category: String): String {
        return when (category) {
            "Length" -> listOf(
                "A child could crawl through the blood vessels of a blue whale.",
                "Sears Tower has 16,000 windows and 100 elevators.",
                "The length of your foot is equal to the distance from your wrist to your inner cubital elbow.",
                "Mount Everest shrinks and grows slightly depending on seasonal snow load in the Himalayas."
            ).random()
            "Weight / Mass" -> listOf(
                "A teaspoon of a neutron star would weigh about 6 billion tons on Earth.",
                "All the ants on Earth weigh roughly as much as all the humans on Earth.",
                "A blue whale's heart is the size of a small car and weighs over 400 pounds.",
                "Clouds look light, but an average cumulus cloud weighs about 1.1 million pounds."
            ).random()
            "Time" -> listOf(
                "Your brain processes about 70,000 thoughts on an average day.",
                "A single day on Venus is longer than a whole Venusian year.",
                "Every 2 minutes, we take more photos than the entire human race took in the 1800s.",
                "If the Earth's history were squeezed into 24 hours, humans would appear at 11:58 PM."
            ).random()
            "Speed" -> listOf(
                "Our solar system orbit travel speed in the Milky Way is 490,000 miles per hour.",
                "Snails can sleep for up to three years without drying out.",
                "The speed of light can wrap around the Earth 7.5 times in one single second.",
                "Tectonic plates shift at roughly the same speed that human fingernails grow."
            ).random()
            "Energy" -> listOf(
                "A single lightning strike contains enough heat to toast 100,000 slices of bread.",
                "The Sun produces more energy in one second than humanity has generated in all history.",
                "A mature oak tree can consume and transform up to 50 gallons of water-powered energy daily."
            ).random()
            "Digital Storage" -> listOf(
                "The human brain's memory storage capacity is estimated to be around 2.5 Petabytes.",
                "The entire internet in 1997 could fit onto a modern 2 TB hard drive disk.",
                "Over 500 hours of video are uploaded to YouTube every single minute worldwide."
            ).random()
            "Money" -> listOf(
                "If you spent $1 million every day since Jesus was born, you still wouldn't have spent as much as some tech billionaires own.",
                "A standard US dollar bill can be folded back and forth about 4,000 times before it tears.",
                "Money isn't actually paper—it's mostly a blend of 75% cotton and 25% linen.",
                "The first paper money was printed in China during the Song Dynasty around 1,000 years ago."
            ).random()
            "Astronomy" -> listOf(
                "Because of lack of atmospheric erosion, astronaut footprints on the Moon will remain intact for 100 million years.",
                "One day on Venus is longer than one Year on Venus.",
                "There are more trees on Earth than stars in the entire Milky Way galaxy.",
                "Space smells like a mix of hot metal, diesel fumes, and seared steak."
            ).random()
            "Human Body" -> listOf(
                "The human body has enough iron in it to forge a 3-inch nail.",
                "Your eyes blink around 20,000 times a day on average.",
                "You produce enough saliva in your lifetime to fill two standard swimming pools.",
                "Every minute, your body sheds around 30,000 to 40,000 dead skin cells."
            ).random()
            "Historical" -> listOf(
                "The Bronze Age collapse happened in under 50 years, wiping out almost all complex Mediterranean civilizations.",
                "Ancient Rome was 8 times more densely populated than modern New York City.",
                "In 1913, the US Post Office allowed parents to legally mail their babies via Parcel Post.",
                "Cleopatra lived closer to the construction of the Apple Store than to the construction of the Great Pyramid of Giza."
            ).random()
            else -> "Math makes the universe click!"
        }
    }

    fun convertValue(category: String, inputValue: Double, fromUnitKey: String, toUnitKey: String): ConversionResult {
        val list = unitsMap[category] ?: return ConversionResult("Error", emptyList(), "Category not found")
        val fromDef = list.find { it.key == fromUnitKey } ?: return ConversionResult("Error", emptyList(), "Source unit not found")
        val toDef = list.find { it.key == toUnitKey } ?: return ConversionResult("Error", emptyList(), "Destination unit not found")

        // 1. Calculate base unit value
        val baseValue = if (category == "Temperature") {
            when (fromUnitKey) {
                "c" -> inputValue
                "f" -> (inputValue - 32.0) * (5.0 / 9.0)
                "k" -> inputValue - 273.15
                else -> inputValue
            }
        } else {
            inputValue * fromDef.factorToBase
        }

        // 2. Convert from base unit value to destination unit
        val destValue = if (category == "Temperature") {
            when (toUnitKey) {
                "c" -> baseValue
                "f" -> (baseValue * (9.0 / 5.0)) + 32.0
                "k" -> baseValue + 273.15
                else -> baseValue
            }
        } else {
            baseValue / toDef.factorToBase
        }

        val formattedInput = formatDecimal(inputValue, 4)
        val formattedOutput = formatDecimal(destValue, 4)

        val resultString = "$formattedInput ${fromDef.displayName.substringBefore(" (")} = " +
                "$formattedOutput ${toDef.displayName.substringBefore(" (")}"

        // If Category is temperature, handle custom comparison, else general list
        val comparisons = if (category == "Temperature") {
            val celsius = baseValue
            listOf(
                "🔥 Celsius temperature: ${formatDecimal(celsius, 2)}°C",
                "🌡️ Equivalent to heat of: " + when {
                    celsius <= 0 -> "Freezing water point (0°C). Quick, grab a thick blanket!"
                    celsius in 1.0..10.0 -> "Typical refrigerator interior. Crisp!"
                    celsius in 11.0..25.0 -> "A pleasant spring afternoon in Paris."
                    celsius in 26.0..37.0 -> "Human core body temperature (~37°C)."
                    celsius in 38.0..50.0 -> "Sizzling sand in the Sahara Desert."
                    celsius in 51.0..100.0 -> "Hot tea brewing heat (Water boils at 100°C)."
                    else -> "Extremely hot! Earth core lava territory."
                }
            )
        } else {
            getComparisons(category, baseValue)
        }

        return ConversionResult(
            resultString = resultString,
            comparisons = comparisons,
            fact = getFunFactForCategory(category)
        )
    }

    private fun formatCount(v: Double): String {
        return when {
            v.isNaN() || v.isInfinite() -> "0"
            v >= 1e6 -> String.format("%,.1f Million", v / 1e6)
            v >= 1000 -> String.format("%,d", v.roundToInt())
            v >= 1.0 -> String.format("%.2f", v)
            v >= 0.001 -> String.format("%.4f", v)
            v > 0 -> String.format("%.6f", v)
            else -> "0"
        }
    }

    private fun formatDecimal(v: Double, decimals: Int): String {
        if (v.isNaN() || v.isInfinite()) return "0"
        return if (v % 1.0 == 0.0) {
            String.format("%,d", v.toLong())
        } else {
            val formatStr = "%.,${decimals}f"
            try {
                String.format(formatStr, v)
            } catch (e: Exception) {
                v.toString()
            }
        }
    }
}

data class ConversionResult(
    val resultString: String,
    val comparisons: List<String>,
    val fact: String
)
