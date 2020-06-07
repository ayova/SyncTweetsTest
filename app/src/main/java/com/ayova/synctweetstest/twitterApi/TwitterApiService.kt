package com.ayova.synctweetstest.twitterApi

import com.ayova.synctweetstest.models.ListOfStatuses
import com.ayova.synctweetstest.models.OAuthToken
import com.ayova.synctweetstest.models.SearchTweets
import retrofit2.Call
import retrofit2.http.*

interface TwitterApiService {
    @POST("oauth2/token")
    @FormUrlEncoded
    fun getAccessToken(@Header("Authorization")authorization: String, @Field("grant_type")grantType: String): Call<OAuthToken>

    @POST("1.1/statuses/filter.json")
    fun getStatusesFilter(@Header("Authorization")authorization: String, @Query("track")track: String): Call<ListOfStatuses>

    /**
     * @param q is the text to be matched in the tweets
     * @param count is the number of tweets to retrieve.
     *        Defaults to 15 if none provided. Max is 100.
     * @param result_type filters how tweets should be gathered
     *        Mixed, popular or recent tweets
     * @param geocode is used to restrict tweets from which geocode
     *        are displayed. I'm using it because otherwise, most
     *        recent tweets, usually don't provide geo location
     *        e.g. @Query("geocode")geocode: String = "40.4636688,-3.7492199,900km" - all of Spain
     */
    @GET("1.1/search/tweets.json")
    fun searchTweets(@Header("Authorization")auth: String, @Query("q")query: String,
                     @Query("result_type")result_type: String = "recent",
                     @Query("count")count: Int = 100): Call<SearchTweets>

    @GET("1.1/lists/statuses.json")
    fun listStatuses(@Header("Authorization")auth: String, @Query("list_id")listId: String = "1130185227375038465",
                     @Query("count")count: Int = 1): Call<ListOfStatuses>
}