package com.meharenterprises.originconnect.data.model
data class Country(val name: String, val flag: String, val dialCode: String, val code: String)

object CountryList {
    val all = listOf(
        Country("Pakistan", "🇵🇰", "+92", "PK"),
        Country("United States", "🇺🇸", "+1", "US"),
        Country("United Kingdom", "🇬🇧", "+44", "GB"),
        Country("India", "🇮🇳", "+91", "IN"),
        Country("Saudi Arabia", "🇸🇦", "+966", "SA"),
        Country("UAE", "🇦🇪", "+971", "AE"),
        Country("Canada", "🇨🇦", "+1", "CA"),
        Country("Australia", "🇦🇺", "+61", "AU"),
        Country("Germany", "🇩🇪", "+49", "DE"),
        Country("France", "🇫🇷", "+33", "FR"),
        Country("Turkey", "🇹🇷", "+90", "TR"),
        Country("Bangladesh", "🇧🇩", "+880", "BD"),
        Country("Afghanistan", "🇦🇫", "+93", "AF"),
        Country("Malaysia", "🇲🇾", "+60", "MY"),
        Country("Indonesia", "🇮🇩", "+62", "ID"),
        Country("Nigeria", "🇳🇬", "+234", "NG"),
        Country("Egypt", "🇪🇬", "+20", "EG"),
        Country("Iran", "🇮🇷", "+98", "IR"),
        Country("Iraq", "🇮🇶", "+964", "IQ"),
        Country("Jordan", "🇯🇴", "+962", "JO"),
        Country("Kuwait", "🇰🇼", "+965", "KW"),
        Country("Qatar", "🇶🇦", "+974", "QA"),
        Country("Bahrain", "🇧🇭", "+973", "BH"),
        Country("Oman", "🇴🇲", "+968", "OM"),
        Country("China", "🇨🇳", "+86", "CN"),
        Country("Japan", "🇯🇵", "+81", "JP"),
        Country("South Korea", "🇰🇷", "+82", "KR"),
        Country("Russia", "🇷🇺", "+7", "RU"),
        Country("Brazil", "🇧🇷", "+55", "BR"),
        Country("Mexico", "🇲🇽", "+52", "MX")
    ).sortedBy { it.name }
}
