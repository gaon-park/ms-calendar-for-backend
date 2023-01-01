package com.maple.heroforbackend.exception

class AlreadyExistException(
    private val msg: String
) : InvalidRequestException(msg)
