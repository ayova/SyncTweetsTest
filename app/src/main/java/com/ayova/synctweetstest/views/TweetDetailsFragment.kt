package com.ayova.synctweetstest.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

import com.ayova.synctweetstest.R
import com.ayova.synctweetstest.TweetsInMapActivity
import com.ayova.synctweetstest.models.Status
import com.ayova.synctweetstest.models.TweetsWithGeo
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_tweet_details.*

private const val COMING_TWEET_ID = "coming_tweet_id"

class TweetDetailsFragment : Fragment() {
    val TAG = "myapp"
    private var tweetId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tweetId = it.getString(COMING_TWEET_ID)
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
            val tweet = TweetsWithGeo.tweets.filter { it.id_str == tweetId }

            // Getting 0th element because there can only ever be one tweet with the given id_str (id)
            setTweetDetails(tweet[0])
        } else { // if no id provided
            Log.e(TAG, "id error")
            Toast.makeText(activity!!,"Sorry, can't open that tweet :(", Toast.LENGTH_SHORT).show()
            activity!!.supportFragmentManager.popBackStack()
        }
    }

    @SuppressLint("SetTextI18n") // to avoid text setting lint
    private fun setTweetDetails (tweetToShow: Status) {
        detailsfragment_text.text = tweetToShow.text

        // set user photo
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

    override fun onStop() {
        super.onStop()
        // set search elements visible again
        activity!!.findViewById<TextInputEditText>(R.id.tweets_in_map_et_search)
            .visibility = LinearLayout.VISIBLE
        activity!!.findViewById<TextInputLayout>(R.id.tweets_in_map_et_search_outline)
            .visibility = LinearLayout.VISIBLE
        activity!!.findViewById<MaterialButton>(R.id.tweets_in_map_btn_refresh)
            .visibility = LinearLayout.VISIBLE
    }
}
