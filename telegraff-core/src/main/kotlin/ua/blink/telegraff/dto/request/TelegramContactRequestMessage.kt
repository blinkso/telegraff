package ua.blink.telegraff.dto.request

import ua.blink.telegraff.dto.request.keyboard.TelegramMarkupContactReplyKeyboard

open class TelegramContactRequestMessage(
    chatId: Long = 0,

    text: String,

    buttonText: String,

    cancelButtonText: String? = null,

    linkPreviewOptions: TelegramLinkPreviewOptions? = null
) : TelegramMessageSendRequest(
    chatId = chatId,
    text = text,
    parseMode = TelegramParseMode.MARKDOWN,
    replyMarkup = TelegramMarkupContactReplyKeyboard(text = buttonText, cancelButtonText = cancelButtonText),
    linkPreviewOptions = linkPreviewOptions
)