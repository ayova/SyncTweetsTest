package com.ayova.synctweetstest

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.ayova.synctweetstest.models.*
import com.ayova.synctweetstest.twitterApi.TwitterApi
import com.ayova.synctweetstest.TwitterAuth
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val TAG = "myapp"
    val PREFERENCES_FILE = "com.ayova.synctweetstest.prefs"
    val BEARER_TOKEN = "bearer_token"
    lateinit var prefs: SharedPreferences
    var bearerToken: String = ""
    lateinit var allStatuses: ArrayList<ListOfStatusesItem>
    var tweetsList: ArrayList<Status>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, TweetsInMapActivity::class.java))

//        prefs = this.getSharedPreferences(PREFERENCES_FILE, 0)
//        bearerToken = prefs.getString(BEARER_TOKEN, "").toString()
//
//        TwitterApi.initServiceApi()
//
//        getStatusesFilter("q")
//
//        main_btn_gotomap.setOnClickListener {
//            if (bearerToken.isEmpty()){
//                getBearerToken(main_et_search.text.toString())
//            } else {
//                searchTweets(main_et_search.text.toString())
//            }
//        }
    }

    /**
     * Function used to generate an access token for the app
     */
    private fun getBearerToken(query: String?) {
        val concatKeys = "${TwitterApi.API_CONSUMER_KEY}:${TwitterApi.API_SECRET_CONSUMER_KEY}"

        val call = TwitterApi.service.getAccessToken(
            "Basic ${Base64.encodeToString(concatKeys.toByteArray(), 1738)}",
            "client_credentials"
        )

        call.enqueue(object : Callback<OAuthToken> {
            override fun onResponse(call: Call<OAuthToken>, response: Response<OAuthToken>) {
                val body = response.body()
                Log.v(TAG, response.toString())
                if (response.isSuccessful && body != null) {
                    // assign the access token to be saved
                    bearerToken = body.access_token
                    // save retrievedToken to shared prefs
                    val editor = prefs.edit()
                    editor.putString(BEARER_TOKEN, bearerToken)
                    editor.apply()
                    /* Once the token has been fetched, search for the tweets.
                       If other functions could take place, it'd be better to
                       abstract this from here. */
                    if (!query.isNullOrEmpty()) {
                        searchTweets(query)
                    }
                } else { Log.e(TAG, response.errorBody()!!.string()) }
            }
            override fun onFailure(call: Call<OAuthToken>, t: Throwable) { Log.e(TAG, t.message!!) }
        })
    }

    /**
     * Function for fetching tweets from the search API endpoint
     * Used while getStatusesFilter isn't working
     */
    private fun searchTweets(query: String) {
        TwitterApi.initServiceApi()
        if (bearerToken.isEmpty()) { // if access token not available, generate one
            getBearerToken(query)
        } else {
            val call = TwitterApi.service.searchTweets("Bearer $bearerToken", query)
            call.enqueue(object : Callback<SearchTweets>{
                override fun onResponse(call: Call<SearchTweets>, response: Response<SearchTweets>) {
                    val body =  response.body()
                    if (response.isSuccessful && body != null) {
                        // here i only append those tweets that do have a geo location
                        body.statuses.forEach { status ->
                            if (status.geo?.coordinates != null) {
                                Log.i(TAG, "\n${status.coordinates.toString()}\n${status.geo.coordinates[0]} ${status.geo.coordinates[1]}\n")
                                tweetsList?.add(status)
                            }
                        }
                        TweetsWithGeo.tweets?.addAll(tweetsList as ArrayList<Status>)
                        Log.i(TAG, TweetsWithGeo.tweets.toString())
                        goToMap()
                    } else { Log.e(TAG, response.errorBody()!!.toString()) }
                }
                override fun onFailure(call: Call<SearchTweets>, t: Throwable) { Log.e(TAG, t.message!!) }
            })
        }
    }

    /**
     * Navigate to the map activity if there are tweets to be displayed
     * otherwise, display warning through a Toast message
     */
    private fun goToMap(){
        if (!tweetsList.isNullOrEmpty()) { // check there are some tweets to pass through the bundle
            startActivity(Intent(this, TweetsInMapActivity::class.java))
        } else {
            Toast.makeText(this, "No tweets found matching the given search terms...", Toast.LENGTH_SHORT).show()
        }
    }



    /**
     * Function used to search for tweets based on search terms
     * This function would help implement realtime updates to the map pins,
     * as well as it'd allow for connection to the stream API endpoint.
     */
    private fun getStatusesFilter(track: String) {
        TwitterApi.initServiceStream()
        val call = TwitterApi.service.getStatusesFilter(TwitterAuth().setAuthorizationHeader(track), track)
        call.enqueue(object : Callback<ListOfStatuses> {
            override fun onResponse(call: Call<ListOfStatuses>, response: Response<ListOfStatuses>) {
                val statuses = response.body()
//                Log.v(TAG, response.toString())
                if (response.isSuccessful && statuses != null) {
                    allStatuses.addAll(statuses)
                    Log.v(TAG, statuses.toString())
                } else {
                    Log.e(TAG, response.errorBody()!!.string())
                }
            }
            override fun onFailure(call: Call<ListOfStatuses>, t: Throwable) {
                Log.e(TAG, t.message!!)
            }
        })

        // uncomment following line to print the request headers
        Log.v(TAG, call.request().headers().toString())

    }

    /**
     * Function to get tweet's by list_id...
     * This a one-time connection, not stream
     *
     * Function used for testing with API at the
     * beginning, i.e. initial setup
     */
    private fun getTweetsByList(){
        TwitterApi.initServiceApi()
        val call = TwitterApi.service.listStatuses("Bearer $bearerToken")
        call.enqueue(object : Callback<ListOfStatuses> {
            override fun onResponse(call: Call<ListOfStatuses>, response: Response<ListOfStatuses>) {
                val body = response.body()
                Log.v(TAG, response.toString())
                if (response.isSuccessful && body != null) {
                    //code here
                    Log.v(TAG, body.toString())
                } else {
                    Log.e(TAG, response.errorBody()!!.string())
                }
            }
            override fun onFailure(call: Call<ListOfStatuses>, t: Throwable) {
                Log.e(TAG, t.message!!)
            }
        })
    }
}
