package com.ayova.synctweetstest.models

data class Entities(
    val hashtags: List<Any>,
    val symbols: List<Any>,
    val urls: List<Url>,
    val user_mentions: List<Any>
)