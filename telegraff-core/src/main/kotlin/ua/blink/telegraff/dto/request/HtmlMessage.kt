package ua.blink.telegraff.dto.request

import ua.blink.telegraff.dto.request.keyboard.TelegramMarkupReplyKeyboard
import ua.blink.telegraff.dto.request.keyboard.TelegramRemoveReplyKeyboard

class HtmlMessage(
    text: String,
    vararg replies: String,
    chatId: Long = 0,
    linkPreviewOptions: TelegramLinkPreviewOptions? = null
) : TelegramMessageSendRequest(
    chatId = chatId,
    text = text,
    parseMode = TelegramParseMode.HTML,
    replyMarkup = if (replies.isNotEmpty()) TelegramMarkupReplyKeyboard(
        answers = replies.asList()
    ) else TelegramRemoveReplyKeyboard(),
    linkPreviewOptions = linkPreviewOptions
)