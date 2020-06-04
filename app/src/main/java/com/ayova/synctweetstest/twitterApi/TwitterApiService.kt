package com.ayova.synctweetstest.twitterApi

import com.ayova.synctweetstest.models.ListOfStatuses
import com.ayova.synctweetstest.models.OAuthToken
import retrofit2.Call
import retrofit2.http.*

interface TwitterApiService {
    @POST("oauth2/token")
    @FormUrlEncoded
    fun getAccessToken(@Header("Authorization")authorization: String, @Field("grant_type")grantType: String): Call<OAuthToken>

    @POST("1.1/statuses/filter.json")
    fun getStatusesFilter(@Header("Authorization")authorization: String, @Query("track")track: String): Call<ListOfStatuses>
}