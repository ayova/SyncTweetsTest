package com.ayova.synctweetstest.models

data class SearchTweets(
    val search_metadata: SearchMetadata,
    val statuses: List<Status>
)