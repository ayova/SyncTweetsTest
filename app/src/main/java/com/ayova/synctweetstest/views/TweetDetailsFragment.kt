package com.ayova.synctweetstest.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.ayova.synctweetstest.R
import com.ayova.synctweetstest.models.Status
import com.ayova.synctweetstest.models.TweetsWithGeo
import com.google.android.material.shape.RoundedCornerTreatment
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.fragment_tweet_details.*
import kotlin.math.round

private const val COMING_TWEET_ID = "coming_tweet_id"

class TweetDetailsFragment : Fragment() {
    val TAG = "myapp"
    private var tweetId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tweetId = it.getString(COMING_TWEET_ID)
            Log.i(TAG, tweetId.toString())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tweet_details, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(tweetId: String) =
            TweetDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(COMING_TWEET_ID, tweetId)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (tweetId != null) {
            val tweet = TweetsWithGeo.tweets[tweetId!!.toInt()]
            Log.e(TAG, "id in:${tweetId}, all tweets: ${TweetsWithGeo.tweets}")
            setTweetDetails(tweet)
        } else {
            Log.e(TAG, "id error")
            Toast.makeText(activity!!,"Sorry, can't open that tweet :(", Toast.LENGTH_SHORT).show()
            activity!!.supportFragmentManager.popBackStack()
        }

    }

    private fun setTweetDetails (tweetToShow: Status) {
        detailsfragment_text.text = tweetToShow.text
        Picasso.get()
            .load(tweetToShow.user.profile_image_url_https)
            .resize(70,70)
            .centerCrop()
            .into(detailsfragment_user_photo)
        detailsfragment_user.text = "@${tweetToShow.user.screen_name} (${tweetToShow.user.name})"
        detailsfragment_id.text = "Tweet with id: ${tweetToShow.id_str}"
        detailsfragment_rts.text = "${tweetToShow.retweet_count} retweets, and ${tweetToShow.favorite_count} favorites"
        detailsfragment_coords.text = "Latitude: ${tweetToShow.geo!!.coordinates[0]}, longitude: ${tweetToShow.geo!!.coordinates[1]}"
        detailsfragment_lang.text = "Language: ${tweetToShow.lang}"
    }
}
