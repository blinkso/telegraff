package ua.blink.telegraff.util

import java.util.*

fun String.localized(
    locale: Locale? = null
): String {
    return runCatching {
        ResourceBundle.getBundle(
            "strings",
            locale ?: DEFAULT_LOCALE,
            UTF8Control()
        ).getString(this)
    }.getOrNull() ?: ""
}

/**
 * Should be extended every time we add new language to the system
 */
fun String.allLocalizedValues(): Set<String> {
    return setOf(
        ResourceBundle.getBundle(
            "strings",
            Locale(PORTUGUESE_LOCALE),
            UTF8Control()
        ).getString(this),
        ResourceBundle.getBundle(
            "strings",
            Locale(ENGLISH_LOCALE),
            UTF8Control()
        ).getString(this),
        ResourceBundle.getBundle(
            "strings",
            Locale(UKRAINIAN_LOCALE),
            UTF8Control()
        ).getString(this),
        ResourceBundle.getBundle(
            "strings",
            Locale(RUSSIAN_LOCALE),
            UTF8Control()
        ).getString(this)
    )
}

const val RUSSIAN_LOCALE = "ru"
const val UKRAINIAN_LOCALE = "uk"
const val ENGLISH_LOCALE = "en"
const val PORTUGUESE_LOCALE = "pt"

// https://stackoverflow.com/questions/42245519/datetimeformatter-not-work-with-llll-pattern-in-en-locale
val LOCALES_WITH_STANDALONE_CASE = setOf(RUSSIAN_LOCALE, UKRAINIAN_LOCALE)
val DEFAULT_LOCALE = Locale(ENGLISH_LOCALE)