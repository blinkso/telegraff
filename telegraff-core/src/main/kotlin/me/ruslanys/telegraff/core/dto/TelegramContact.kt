package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import me.ruslanys.telegraff.core.util.EMPTY

@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramContact(
    @get:JsonProperty("user_id")
    val userId: Long,

    @get:JsonProperty("first_name")
    val firstName: String,

    @get:JsonProperty("last_name")
    val lastName: String?,

    @get:JsonProperty("phone_number")
    val phoneNumber: String?
) {
    constructor() : this(
        0L,
        String.EMPTY,
        null,
        null
    )

    constructor(phoneNumber: String) : this(
        0L,
        phoneNumber,
        null,
        null
    )

    fun getUserFullName(): String = firstName.plus(
        if (lastName?.isNotEmpty() == true) {
            " $lastName"
        } else {
            String.EMPTY
        }
    )
}