package com.ayova.synctweetstest.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ayova.synctweetstest.R
import com.ayova.synctweetstest.models.TweetsWithGeo
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

        val id = tweetId?.removePrefix("m")
        val tweetToShow = TweetsWithGeo.tweets[id!!.toInt()]

        detailsfragment_text.text = tweetToShow.text
        detailsfragment_user.text = "${tweetToShow.user.screen_name} (${tweetToShow.user.name})"
        detailsfragment_rts.text = tweetToShow.retweet_count.toString()
    }
}