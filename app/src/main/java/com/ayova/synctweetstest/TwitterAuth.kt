package com.ayova.synctweetstest

import android.util.Base64
import android.util.Log
import com.ayova.synctweetstest.twitterApi.TwitterApi
import java.net.URLEncoder
import java.util.*

class TwitterAuth {
    private val TAG = "myapp"

    /**
     * Function for creating the Authorization header
     * Includes calls to generate oauth_nonce and oauth_signature
     */
    fun setAuthorizationHeader(): String {
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
}