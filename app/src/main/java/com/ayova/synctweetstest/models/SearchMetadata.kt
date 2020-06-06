package com.ayova.synctweetstest.models

data class SearchMetadata(
    val completed_in: Double,
    val count: Int,
    val max_id: Long,
    val max_id_str: String,
    val next_results: String,
    val query: String,
    val refresh_url: String,
    val since_id: Int,
    val since_id_str: String
)