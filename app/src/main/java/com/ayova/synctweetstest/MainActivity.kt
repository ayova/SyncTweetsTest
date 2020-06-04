package com.ayova.synctweetstest

import android.database.Observable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Base64.DEFAULT
import android.util.Log
import com.ayova.synctweetstest.models.ListOfStatuses
import com.ayova.synctweetstest.models.OAuthToken
import com.ayova.synctweetstest.twitterApi.TwitterApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val TAG = "myapp"
    lateinit var retrievedToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TwitterApi.initServiceApi()
        getAccessToken()

    }

    /**
     * Function used to retrieve the access token for the app
     */
    private fun getAccessToken() {
        val concatKeys = "${TwitterApi.API_KEY}:${TwitterApi.API_SECRET_KEY}"
        val call = TwitterApi.service.getAccessToken("Basic ${Base64.encodeToString(concatKeys.toByteArray(), 1738)}", "client_credentials")
        call.enqueue(object : Callback<OAuthToken> {
            override fun onResponse(call: Call<OAuthToken>, response: Response<OAuthToken>) {
                val body = response.body()
                Log.v(TAG, response.toString())
                if (response.isSuccessful && body != null) {
                    retrievedToken = body.access_token // assign the access token to be saved later
                    Log.v(TAG, body.toString())
                    getStatusesFilter()
//                    Log.v(TAG, "Basic ${Base64.encodeToString(concantKeys.toByteArray(), 1738)}")
                } else {
                    Log.e(TAG, response.errorBody()!!.string())
                }
            }
            override fun onFailure(call: Call<OAuthToken>, t: Throwable) {
                Log.e(TAG, t.message!!)
            }
        })
    }

    /**
     * Function used to search for tweets based on search terms
     */
    private fun getStatusesFilter() {
        TwitterApi.initServiceStream()
        val concatKeys = "${TwitterApi.API_KEY}:${TwitterApi.API_SECRET_KEY}"
        val call = TwitterApi.service.getStatusesFilter("Bearer $retrievedToken", "car")
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
