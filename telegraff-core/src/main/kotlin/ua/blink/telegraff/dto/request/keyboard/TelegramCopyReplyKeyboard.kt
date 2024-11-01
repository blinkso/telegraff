package ua.blink.telegraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class TelegramCopyReplyKeyboard(
    @get:JsonProperty("text")
    val text: String,
) : TelegramReplyKeyboard() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TelegramInlineUrlReplyKeyboard

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}