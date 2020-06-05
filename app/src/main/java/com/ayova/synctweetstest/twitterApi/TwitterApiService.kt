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

    @GET("1.1/lists/statuses.json")
    fun getTweets(@Header("Authorization")auth: String, @Query("list_id")listId: String = "1130185227375038465", @Query("count")count: Int = 1): Call<ListOfStatuses>
}