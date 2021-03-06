package me.ruslanys.telegraff.core.filter

import me.ruslanys.telegraff.core.event.TelegramUpdateEvent
import me.ruslanys.telegraff.core.util.TelegramFilterOrderUtil

class DefaultTelegramFiltersFactory(filters: List<TelegramFilter>) : TelegramFiltersFactory, TelegramFilterProcessor {

    private val filters: List<TelegramFilter> = filters.sortedBy {
        TelegramFilterOrderUtil.getOrder(it::class.java)
    }

    override fun getFilters(): List<TelegramFilter> {
        return filters
    }

    override fun process(event: TelegramUpdateEvent) {
        val chain = DefaultTelegramFilterChain(filters)
        if (event.update.message != null) { // only new messages are supported
            chain.doFilter(event.update.message)
        } else if (event.update.callbackQuery?.message != null) {
            // Populating callback query for further filtering
            event.update.callbackQuery.message.callbackQuery = event.update.callbackQuery
            // Currently we use callback data just to send it as a message text back and than handle it
            // Same as reply markup keyboard query
            event.update.callbackQuery.message.text = event.update.callbackQuery.data
            chain.doFilter(event.update.callbackQuery.message)
        }
    }

}