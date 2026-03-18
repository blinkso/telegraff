package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TelegramLinkPreviewOptions(
    @get:JsonProperty("is_disabled")
    val isDisabled: Boolean? = null,

    @get:JsonProperty("url")
    val url: String? = null,

    @get:JsonProperty("prefer_small_media")
    val preferSmallMedia: Boolean? = null,

    @get:JsonProperty("prefer_large_media")
    val preferLargeMedia: Boolean? = null,

    @get:JsonProperty("show_above_text")
    val showAboveText: Boolean? = null,
)
