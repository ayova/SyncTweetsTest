package com.ayova.synctweetstest.twitterApi

import com.ayova.synctweetstest.models.OAuthToken
import retrofit2.Call
import retrofit2.http.*

interface TwitterApiService {
    @POST("oauth2/token")
    @FormUrlEncoded
    fun getAccessToken(@Header("Authorization")authorization: String, @Field("grant_type")grantType: String): Call <OAuthToken>

//    @GET("1.1/search/tweets.json")
//    fun searchTweeet(@Header("Authorization") authorization :String ,@Query("q") query : String):
}