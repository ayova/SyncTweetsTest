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

        prefs = this.getSharedPreferences(PREFERENCES_FILE, 0)
        bearerToken = prefs.getString(BEARER_TOKEN, "").toString()

        TwitterApi.initServiceApi()

        setAuthorizationHeader()
        getStatusesFilter("q")

        main_btn_gotomap.setOnClickListener {
            if (bearerToken.isEmpty()){
                getBearerToken(main_et_search.text.toString())
            } else {
                searchTweets(main_et_search.text.toString())
            }
        }
    }

    /**
     * Function used to generate an access token for the app
     */
    private fun getBearerToken(query: String) {
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
                    searchTweets(query)
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
     * Function for creating the Authorization header
     * Includes calls to generate oauth_nonce and oauth_signature
     */
    private fun setAuthorizationHeader(): String {
        val oauth_consumer_key = TwitterApi.API_CONSUMER_KEY
        val oauth_nonce = generateOauthNonce()
        val oauth_signature_method = "HMAC-SHA1"
        val oauth_timestamp = System.currentTimeMillis().toString()
        val oauth_token = TwitterApi.API_ACCESS_TOKEN
        val oauth_version = "1.0"
        val track = "q"
        val oauth_signature = generateOauthSignature(
            oauth_consumer_key, oauth_nonce, oauth_signature_method,
            oauth_timestamp, oauth_token, oauth_version, track)

        Log.v(TAG, "oauth_nonce --> $oauth_nonce")
        Log.v(TAG, "oauth_signature --> $oauth_signature")

        var authorization = "Oauth "
        // I'm using sortedMapOf so it is sorted automatically (as twitter API asks)
        val map = sortedMapOf(
            "oauth_consumer_key" to oauth_consumer_key,
            "oauth_nonce" to oauth_nonce.trim(),
            "oauth_signature" to oauth_signature,
            "oauth_signature_method" to oauth_signature_method,
            "oauth_timestamp" to oauth_timestamp,
            "oauth_token" to oauth_token ,
            "oauth_version" to oauth_version
        )
        // url encode each key and value then add it to the authorization string
        for ((key,value) in map) {
            val percentedKey = URLEncoder.encode(key,"utf-8")
            val percentedValue = URLEncoder.encode(value,"utf-8")
//            Log.v(TAG, "K: $percentedKey, V: $percentedValue")
            authorization += if (key == "oauth_version") {
                "$percentedKey=\"$percentedValue\""
            } else {
                "$percentedKey=\"$percentedValue\","
            }
        }

        Log.d(TAG, "authorization -------> $authorization")

        return authorization
    }

    /**
     * Generate oauth_nonce by generating 32 bytes of
     * random data, then Base64 encoding it
     * @return the Base64 encoded string
     */
    private fun generateOauthNonce(): String{
        val random = Random()
        val byteArr = ByteArray(32)
        random.nextBytes(byteArr)

        return Base64.encodeToString(byteArr, Base64.DEFAULT)
    }

    /**
     * Generate oauth_signature
     */
    private fun generateOauthSignature(
        oauthConsumerKey: String,
        oauthNonce: String,
        oauthSignatureMethod: String,
        oauthTimestamp: String,
        oauthToken: String,
        oauthVersion: String,
        track: String
    ): String{

        var paramString = "" // string with the keys and values url encoded

        // I'm using sortedMapOf so it is sorted automatically (as twitter API asks)
        val map = sortedMapOf<String, String>(
            "oauth_consumer_key" to oauthConsumerKey,
            "oauth_nonce" to oauthNonce,
            "oauth_signature_method" to oauthSignatureMethod,
            "oauth_timestamp" to oauthTimestamp,
            "oauth_token" to oauthToken,
            "oauth_version" to oauthVersion,
            "track" to track
        )
        // url encode each key and value then add it to the parameters string
        for ((key,value) in map) {
            // Log.v(TAG, "K: $key, V: $value")
            paramString += if (key == "track") {
                "${URLEncoder.encode(key, "utf-8")}=${URLEncoder.encode(value,"utf-8")}"
            } else {
                "${URLEncoder.encode(key, "utf-8")}=${URLEncoder.encode(value,"utf-8")}&"
            }
        }
//        Log.v(TAG, paramString)

        val httpMethod = "POST"
        val url = URLEncoder.encode("${TwitterApi.API_STREAM_URL}1.1/statuses/filter.json?track=$track", "utf-8")
        val percentedParamString = URLEncoder.encode(paramString, "utf-8")

        /* Creating the signature base string */
        var signatureBaseString = "$httpMethod&$url&$percentedParamString"
//        Log.v(TAG, "signaturebase --> $signatureBaseString")

        val percentedConsumerSecret = URLEncoder.encode(TwitterApi.API_SECRET_CONSUMER_KEY, "utf-8")
        val percentedAccessToken = URLEncoder.encode(TwitterApi.API_ACCESS_TOKEN_SECRET, "utf-8")
        val signingKey = "$percentedConsumerSecret&$percentedAccessToken"
//        Log.v(TAG, "signingkey --> $signingKey")

        return Base64.encodeToString(HmacSha1Signature.calculateRFC2104HMAC(signatureBaseString, signingKey).toByteArray(), Base64.DEFAULT)

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
        val call = TwitterApi.service.getStatusesFilter(setAuthorizationHeader(), track)
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
