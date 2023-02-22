package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TCubeApiKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Suppress("EmptyClassBlock")
@Repository
interface TCubeApiKeyRepository : JpaRepository<TCubeApiKey, String> {

}
