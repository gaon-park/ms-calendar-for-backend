package com.maple.heroforbackend.exception

data class ErrorMessage(
    var exception: String,
    var status: Int? = null,
    var message: String? = null,
)
