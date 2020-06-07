package com.ayova.synctweetstest.models


data class Status(
    val contributors: Any?,
    val coordinates: Any?,
    val entities: Entities,
    val favorite_count: Int,
    val favorited: Boolean,
    val geo: StatusGeo?,
    val id: Long,
    val id_str: String,
    val in_reply_to_screen_name: Any?,
    val in_reply_to_status_id: Any?,
    val in_reply_to_status_id_str: Any?,
    val in_reply_to_user_id: Any?,
    val in_reply_to_user_id_str: Any?,
    val is_quote_status: Boolean,
    val lang: String,
    val metadata: Metadata,
    val place: Any?,
    val possibly_sensitive: Boolean,
    val quoted_status: QuotedStatus,
    val quoted_status_id: Long,
    val quoted_status_id_str: String,
    val retweet_count: Int,
    val retweeted: Boolean,
    val source: String,
    val text: String,
    val truncated: Boolean,
    val user: User
)