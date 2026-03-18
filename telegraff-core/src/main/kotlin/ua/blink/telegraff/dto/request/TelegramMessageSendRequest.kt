package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.telegraff.dto.request.keyboard.TelegramRemoveReplyKeyboard
import ua.blink.telegraff.dto.request.keyboard.TelegramReplyKeyboard

open class TelegramMessageSendRequest(
    chatId: Long,

    @get:JsonProperty("text")
    val text: String,

    @get:JsonProperty("parse_mode")
    val parseMode: TelegramParseMode,

    replyMarkup: TelegramReplyKeyboard = TelegramRemoveReplyKeyboard(),

    disableNotification: Boolean = false,

    @get:JsonProperty("link_preview_options")
    val linkPreviewOptions: TelegramLinkPreviewOptions? = null
) : TelegramSendRequest(chatId, replyMarkup, disableNotification) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TelegramMessageSendRequest) return false
        if (!super.equals(other)) return false

        if (text != other.text) return false
        if (parseMode != other.parseMode) return false
        if (linkPreviewOptions != other.linkPreviewOptions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + parseMode.hashCode()
        result = 31 * result + (linkPreviewOptions?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "TelegramMessageSendRequest(text='$text', parseMode=$parseMode, linkPreviewOptions=$linkPreviewOptions)"
    }
}