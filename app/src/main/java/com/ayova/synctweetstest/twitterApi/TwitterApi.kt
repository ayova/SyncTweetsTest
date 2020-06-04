package com.ayova.synctweetstest.twitterApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TwitterApi {
    val API_URL = "https://api.twitter.com/"
    val API_KEY = "ce8PFaUCpCKnMaWLtAxtM2xlu"
    val API_SECRET_KEY = "0hT7Ayq7atdty7UDcVnlA6KJWTizNAkfLmvhTomN411dJ0WZ6g"
    val API_ACCESS_TOKEN = "1168178904424751112-BNTBZPWkyslyHq9Lc9tS2gAKaBhgSu"
    val API_ACCESS_TOKEN_SECRET = "F9QJqQmLytxas8AONFkVtMBVWcmvXma4VuSvGCA3XZoCz"
    lateinit var service: TwitterApiService

    fun initService() {
        val retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(TwitterApiService::class.java)
    }
}