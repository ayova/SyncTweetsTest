package com.ayova.synctweetstest

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.ayova.synctweetstest.models.OAuthToken
import com.ayova.synctweetstest.models.SearchTweets
import com.ayova.synctweetstest.models.Status
import com.ayova.synctweetstest.models.TweetsWithGeo
import com.ayova.synctweetstest.twitterApi.TwitterApi
import com.ayova.synctweetstest.views.TweetDetailsFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_tweets_in_map.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

private const val COMING_TWEET_ID = "coming_tweet_id"

class TweetsInMapActivity : AppCompatActivity(), OnMapReadyCallback {

    val TAG = "myapp"
    private lateinit var mMap: GoogleMap
    val PREFERENCES_FILE = "com.ayova.synctweetstest.prefs"
    val BEARER_TOKEN = "bearer_token"
    lateinit var prefs: SharedPreferences
    var bearerToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets_in_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        TwitterApi.initServiceApi()
        prefs = this.getSharedPreferences(PREFERENCES_FILE, 0)
        bearerToken = prefs.getString(BEARER_TOKEN, "").toString()
        if (bearerToken.isNullOrEmpty()){
            getBearerToken()
        }

        tweets_in_map_btn_refresh.setOnClickListener {
            val searchTerm = tweets_in_map_et_search.text.toString()
            updateMap(searchTerm)
        }
    }


    /**
     * Function responsible for updating the markers on the map
     * and the setting the infoWindowClick listener
     */
    private fun updateMap(query: String) {
        searchTweets(query) { tweetsList ->
            Log.i(TAG, "updating map with so-called response")
            mMap.clear()
            Log.d(TAG, tweetsList.toString())
//            Log.d(TAG, TweetsWithGeo.tweets.toString())
            // check there are tweets to show in the map
            if (tweetsList.isNullOrEmpty()){
                Toast.makeText(this,"No tweets matching search term found!", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "no results" )
            } else {
                // setting markers in map base on geo location of each tweet
                Log.e(TAG, "TWG has: ${tweetsList.size}")
                tweetsList.forEach { tweet ->
                    val pos = LatLng(tweet.geo!!.coordinates[0],tweet.geo.coordinates[1])
                    val marker = mMap.addMarker(MarkerOptions().position(pos).title(tweet.text.trim()).draggable(false))
                    marker.tag = tweet.id_str
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos)) // will zoom into the each marker added, stopping in the last one
                    mMap.setOnInfoWindowClickListener {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.map, TweetDetailsFragment.newInstance(it.tag.toString())) // to show the details in the same view
                            .addToBackStack("map") // to be able and go back to the map
                            .commit()

                        toggleSearchElements() // hide search elements
                    }
                }
            }
        }

        /*   Before implementing callback on searchTweets   */
//        Log.v(TAG, "updateMap()")
//        mMap.clear()
//
//        // check there are tweets to show in the map
//        if (TweetsWithGeo.tweets.isNullOrEmpty()){
//            Toast.makeText(this,"No tweets matching search term found!", Toast.LENGTH_SHORT).show()
//            Log.e(TAG, "no results" )
//        } else {
//            // setting markers in map base on geo location of each tweet
//            Log.e(TAG, "TWG has: ${TweetsWithGeo.tweets.size}")
//            TweetsWithGeo.tweets.forEach { tweet ->
//                val pos = LatLng(tweet.geo!!.coordinates[0],tweet.geo.coordinates[1])
//                val marker = mMap.addMarker(MarkerOptions().position(pos).title(tweet.text.trim()).draggable(false))
//                marker.tag = tweet.id_str
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos)) // will zoom into the each marker added, stopping in the last one
//                mMap.setOnInfoWindowClickListener {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.map, TweetDetailsFragment.newInstance(it.tag.toString())) // to show the details in the same view
//                        .addToBackStack("map") // to be able and go back to the map
//                        .commit()
//
//                    toggleSearchElements() // hide search elements
//                }
//            }
//        }
    }

    /**
     * Function for fetching tweets from the search API endpoint
     * Used while getStatusesFilter isn't working
     */
    private fun searchTweets(query: String, onTweetsRetrieved: (ArrayList<Status>)->Unit) {
        Log.v(TAG, "searchTweets()")
        TwitterApi.initServiceApi()
        val call = TwitterApi.service.searchTweets("Bearer $bearerToken", query)
        call.enqueue(object : Callback<SearchTweets> {
            override fun onResponse(call: Call<SearchTweets>, response: Response<SearchTweets>) {
                val body =  response.body()
                if (response.isSuccessful && body != null) {
                    TweetsWithGeo.tweets.clear()
                    TweetsWithGeo.tweets = arrayListOf()
                    // here i only append those tweets that do have a geo location
                    body.statuses.forEach { status ->
                        if (status.geo?.coordinates != null) {
//                                Log.i(TAG, "\n${status.coordinates.toString()}\n${status.geo.coordinates[0]} ${status.geo.coordinates[1]}\n")
                            TweetsWithGeo.tweets.add(status)
                        }
                    }
                } else { Log.e(TAG, response.errorBody()!!.toString()) }
            }
            override fun onFailure(call: Call<SearchTweets>, t: Throwable) { Log.e(TAG, t.message!!) }
        })
        val tweets = TweetsWithGeo.tweets
        Log.i(TAG, "got the response (tweets)")
        onTweetsRetrieved(tweets)
    }

    /**
     * Function used to generate an access token for the app
     */
    private fun getBearerToken() {
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
                } else { Log.e(TAG, response.errorBody()!!.string()) }
            }
            override fun onFailure(call: Call<OAuthToken>, t: Throwable) { Log.e(TAG, t.message!!) }
        })
    }

    private fun toggleSearchElements(){
        tweets_in_map_et_search.visibility = LinearLayout.GONE
        tweets_in_map_btn_refresh.visibility = LinearLayout.GONE
        tweets_in_map_et_search_outline.visibility = LinearLayout.GONE
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // map to set markers on
        mMap = googleMap
    }

}
