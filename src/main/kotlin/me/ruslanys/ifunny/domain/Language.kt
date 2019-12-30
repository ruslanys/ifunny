package me.ruslanys.ifunny.domain

enum class Language(val code: String) {

    GERMAN("de"),
    SPANISH("es"),
    ITALIAN("it"),
    PORTUGUESE("pt"),
    RUSSIAN("ru");

    companion object {

        fun findByCode(code: String): Language = values().firstOrNull {
            it.code == code
        } ?: throw IllegalArgumentException("Unsupported language code $code")

    }

}
