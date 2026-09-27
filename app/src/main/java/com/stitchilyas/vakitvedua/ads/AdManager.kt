package com.stitchilyas.vakitvedua.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // User's Rewarded Ad Unit ID
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-4494482100556023/5846461225"

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        MobileAds.initialize(context) {
            isInitialized = true
            preloadRewardedAd(context)
        }
    }

    fun preloadRewardedAd(context: Context) {
        if (isLoading || rewardedAd != null) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "RewardedAd successfully preloaded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.w(TAG, "RewardedAd failed to preload: ${error.message} (code: ${error.code})")
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit = {},
        onAdFailed: (String) -> Unit
    ) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    preloadRewardedAd(activity.applicationContext)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    preloadRewardedAd(activity.applicationContext)
                    onAdFailed("Reklam gösterilemedi: ${error.message}")
                }
            }

            currentAd.show(activity) { rewardItem ->
                onRewardEarned()
            }
        } else {
            // Load on the fly and show
            isLoading = true
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity,
                REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        isLoading = false
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                rewardedAd = null
                                preloadRewardedAd(activity.applicationContext)
                                onAdClosed()
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                rewardedAd = null
                                preloadRewardedAd(activity.applicationContext)
                                onAdFailed("Reklam gösterilemedi: ${error.message}")
                            }
                        }

                        ad.show(activity) { _ ->
                            onRewardEarned()
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoading = false
                        onAdFailed("Reklam yüklenemedi: ${error.message}")
                    }
                }
            )
        }
    }
}
