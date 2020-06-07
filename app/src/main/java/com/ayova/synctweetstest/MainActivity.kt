package com.ayova.synctweetstest

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.ayova.synctweetstest.models.*
import com.ayova.synctweetstest.twitterApi.TwitterApi
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val TAG = "myapp"
    val PREFERENCES_FILE = "com.ayova.synctweetstest.prefs"
    val BEARER_TOKEN = "bearer_token"
    lateinit var prefs: SharedPreferences
    var retrievedToken: String = ""
    lateinit var allStatuses: ArrayList<ListOfStatusesItem>
    var tweetsList: ArrayList<Status>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = this.getSharedPreferences(PREFERENCES_FILE, 0)
        retrievedToken = prefs.getString(BEARER_TOKEN, "").toString()

        TwitterApi.initServiceApi()
        if (retrievedToken.isEmpty()){
            getAccessToken("ferrari")
        } else {
            searchTweets("instagram.")
        }

        main_btn_gotomap.setOnClickListener {
            if (tweetsList != null) { // check there are some tweets to pass through the bundle
                startActivity(Intent(this, TweetsInMapActivity::class.java))
            }
        }
    }

    /**
     * Function used to generate an access token for the app
     */
    private fun getAccessToken(query: String) {
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
                    retrievedToken = body.access_token
                    // save retrievedToken to shared prefs
                    val editor = prefs.edit()
                    editor.putString(BEARER_TOKEN, retrievedToken)
                    editor.apply()
                    /* Once the token has been fetched, search for the tweets.
                       If other functions could take place, it'd be better to
                       abstract this from here. */
                    searchTweets(query)
                } else { Log.e(TAG, response.errorBody()!!.string()) }
            }
            override fun onFailure(call: Call<OAuthToken>, t: Throwable) { Log.e(TAG, t.message!!) }
        })
    }

    /**
     * Function for fetching tweets from the search API endpoint
     */
    private fun searchTweets(query: String) {
        if (retrievedToken.isEmpty()) { // if access token not available, generate one
            getAccessToken(query)
        } else {
            val call = TwitterApi.service.searchTweets("Bearer $retrievedToken", query)
            call.enqueue(object : Callback<SearchTweets>{
                override fun onResponse(call: Call<SearchTweets>, response: Response<SearchTweets>) {
                    val body =  response.body()
                    if (response.isSuccessful && body != null) {
                        // here i only append those tweets that do have a geo location
                        body.statuses.forEach { status ->
                            if (status.geo?.coordinates != null) {
                                Log.i(TAG, "\n${status.coordinates.toString()}\n${status.geo.coordinates[0]} ${status.geo.coordinates[1]}\n")
                                tweetsList?.add(status)

                                // add tweets to global object to ease access throughout the app
                                TweetsWithGeo.tweets?.clear()
                                TweetsWithGeo.tweets?.addAll(tweetsList!!)
                            }
                        }
                    } else { Log.e(TAG, response.errorBody()!!.toString()) }
                }
                override fun onFailure(call: Call<SearchTweets>, t: Throwable) { Log.e(TAG, t.message!!) }
            })
        }
    }

    /**
     * Function used to search for tweets based on search terms
     * This function would help implement realtime updates to the map pins,
     * as well as it'd allow for connection to the stream API endpoint.
     *
     * Will try implementing it again later!
     */
    private fun getStatusesFilter(track: String) {
        TwitterApi.initServiceStream()

        val oauth_consumer_key = TwitterApi.API_CONSUMER_KEY
        val oauth_nonce = "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"
        val oauth_signature_method = "HMAC-SHA1"
        val oauth_timestamp = Date()
        val oauth_token = retrievedToken
        val paramString = "track=$track&oauth_consumer_key=$oauth_consumer_key&oauth_nonce=$oauth_nonce&oauth_signature_method=$oauth_signature_method&oauth_timestamp=$oauth_timestamp&oauth_token=$oauth_token&oauth_version=1.0"
        val httpMethod = "POST"
        val url = "${TwitterApi.API_STREAM_URL}1.1/statuses/filter.json?track=$track"
        val signatureBaseString = "$httpMethod&${URLEncoder.encode(url,"utf-8")}&${URLEncoder.encode(paramString,"utf-8")}"
//        val oauth_signature =

        val call = TwitterApi.service.getStatusesFilter("Bearer $retrievedToken", track)
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
        val call = TwitterApi.service.listStatuses("Bearer $retrievedToken")
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
