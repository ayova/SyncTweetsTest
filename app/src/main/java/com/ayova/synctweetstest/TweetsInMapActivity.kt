package com.ayova.synctweetstest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ayova.synctweetstest.models.Status
import com.ayova.synctweetstest.models.TweetsWithGeo
import com.ayova.synctweetstest.views.TweetDetailsFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private const val COMING_TWEET_ID = "coming_tweet_id"

class TweetsInMapActivity : AppCompatActivity(), OnMapReadyCallback {

    val TAG = "myapp"
    private lateinit var mMap: GoogleMap
    var tweetsList: ArrayList<Status> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets_in_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // check there are tweets to show in the map
        if (TweetsWithGeo.tweets.isNullOrEmpty()){
            Log.e(TAG, "no tweets passed to map ${TweetsWithGeo.tweets.toString()}" )
        } else {
            tweetsList.addAll(TweetsWithGeo.tweets!!)
            Log.v(TAG, tweetsList[0].toString())
        }

        // map to set markers on
        mMap = googleMap

        // setting markers in map base on geo location of each tweet
        for (tweet in tweetsList) {
            val pos = LatLng(tweet.geo!!.coordinates[0],tweet.geo!!.coordinates[1])
            mMap.addMarker(MarkerOptions().position(pos).title(tweet.text.trim()).draggable(false))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos)) // will zoom into the each marker added, stopping in the last one
            mMap.setOnInfoWindowClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.map, TweetDetailsFragment.newInstance(it.id)) // to show the details in the same view
                    .addToBackStack("map") // to be able and go back to the map
                    .commit()
            }
        }
    }
}
