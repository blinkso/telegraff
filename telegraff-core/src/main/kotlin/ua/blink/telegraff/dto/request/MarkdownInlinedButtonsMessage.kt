package ua.blink.telegraff.dto.request

import ua.blink.telegraff.dto.request.keyboard.TelegramInlineUrlReplyKeyboard
import ua.blink.telegraff.dto.request.keyboard.TelegramMarkupInlinedReplyKeyboard
import ua.blink.telegraff.dto.request.keyboard.TelegramRemoveReplyKeyboard

class MarkdownInlinedButtonsMessage(
    text: String,
    vararg inlines: TelegramInlineUrlReplyKeyboard,
    chatId: Long = 0,
    linkPreviewOptions: TelegramLinkPreviewOptions? = null
) : TelegramMessageSendRequest(
    chatId = chatId,
    text = text,
    parseMode = TelegramParseMode.MARKDOWN,
    replyMarkup = if (inlines.isNotEmpty()) TelegramMarkupInlinedReplyKeyboard(
        inlines = inlines.toList()
    ) else TelegramRemoveReplyKeyboard(),
    linkPreviewOptions = linkPreviewOptions
)