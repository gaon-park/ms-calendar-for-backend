package com.maple.heroforbackend.exception

open class InvalidRequestException(
    private val msg: String
) : Exception(msg)
