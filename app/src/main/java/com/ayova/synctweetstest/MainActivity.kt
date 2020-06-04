package com.ayova.synctweetstest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Base64.DEFAULT
import android.util.Log
import com.ayova.synctweetstest.models.OAuthToken
import com.ayova.synctweetstest.twitterApi.TwitterApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val TAG = "myapp"
    lateinit var retrievedToken: OAuthToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TwitterApi.initService()
        getAccessToken()

    }

    private fun getAccessToken() {
        val concantKeys = "${TwitterApi.API_KEY}:${TwitterApi.API_SECRET_KEY}"
        val call = TwitterApi.service.getAccessToken("Basic ${Base64.encodeToString(concantKeys.toByteArray(), 1738)}", "client_credentials")
        call.enqueue(object : Callback<OAuthToken> {
            override fun onResponse(call: Call<OAuthToken>, response: Response<OAuthToken>) {
                val body = response.body()
                Log.v(TAG, response.toString())
                if (response.isSuccessful && body != null) {
                    retrievedToken = OAuthToken(body.token_type, body.access_token) // assign the access token to be saved later
//                    Log.v(TAG, body.toString())
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
}
