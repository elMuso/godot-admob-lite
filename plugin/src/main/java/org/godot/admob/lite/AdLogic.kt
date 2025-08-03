package org.godot.admob.lite

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdLogic(val act: Activity, val container: FrameLayout) {
	// REQUIRED TO ADD THE APPLICATION ID TO THE MANIFEST https://developers.google.com/admob/android/privacy#add_the_application_id
	val loadedBanners = mutableMapOf<String, AdView>()
	val loadedInterstitials = mutableMapOf<String, InterstitialAd>()
	val loadedRewarded = mutableMapOf<String, RewardedAd>()

	fun initialize() {
		MobileAds.initialize(act)
	}

	//	Banner Logic

	fun loadBanner(id: String, size: Int, position: Int) {
		destroyBanner(id)

		val newBanner = AdView(act)
		if (BuildConfig.DEBUG) {
			newBanner.adUnitId = "ca-app-pub-3940256099942544/6300978111"
		} else {
			newBanner.adUnitId = id
		}

		// Set the AdSize based on the 'size' parameter
		if (size > 40) {
			newBanner.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(act, size))
		} else {
			when (size) {
				0 -> newBanner.setAdSize(AdSize.BANNER)
				1 -> newBanner.setAdSize(AdSize.LARGE_BANNER)
				2 -> newBanner.setAdSize(AdSize.MEDIUM_RECTANGLE)
				3 -> newBanner.setAdSize(AdSize.FULL_BANNER)
				4 -> newBanner.setAdSize(AdSize.LEADERBOARD)
			}
		}

		// Determine the gravity based on the 'position' parameter
		val gravity = when (position) {
			0 -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
			1 -> Gravity.CENTER
			2 -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
			else -> Gravity.TOP or Gravity.CENTER_HORIZONTAL // Default to top if an invalid position is provided
		}

		// Create a new FrameLayout.LayoutParams with the determined gravity
		val layoutParams = FrameLayout.LayoutParams(
			FrameLayout.LayoutParams.MATCH_PARENT, // Width
			FrameLayout.LayoutParams.WRAP_CONTENT, // Height
			gravity
		)

		// Add the new banner to the container with the specified layout parameters
		container.addView(newBanner, layoutParams)

		val adRequest = AdRequest.Builder().build()
		newBanner.loadAd(adRequest)
		loadedBanners[id] = newBanner
	}

	fun destroyBanner(id: String) {
		// Remove banner from view hierarchy.
		val parentView = loadedBanners[id]?.parent
		if (parentView is ViewGroup) {
			parentView.removeView(loadedBanners[id])
		}
		loadedBanners[id]?.destroy()
		loadedBanners.remove(id)
	}

	//	Interstitial Logic

	fun loadInterstitial(id: String, callback: (Boolean) -> Unit) {
		var adId = "ca-app-pub-3940256099942544/1033173712"
		if (!BuildConfig.DEBUG) {
			adId = id
		}
		InterstitialAd.load(
			act,
			adId,
			AdRequest.Builder().build(),
			object : InterstitialAdLoadCallback() {
				override fun onAdLoaded(ad: InterstitialAd) {
					callback(true)
					loadedInterstitials[id] = ad
				}

				override fun onAdFailedToLoad(adError: LoadAdError) {
					callback(false)
					loadedInterstitials.remove(id)
				}
			},
		)
	}

	fun releaseInterstitial(id: String) {
		loadedInterstitials.remove(id)
	}

	fun showInterstitial(id: String, callback: (Int) -> Unit) {
		if (loadedInterstitials[id] == null) {
			callback(0)
		}
		loadedInterstitials[id]?.fullScreenContentCallback = object : FullScreenContentCallback() {
			override fun onAdFailedToShowFullScreenContent(adError: AdError) {
				callback(0)
				loadedInterstitials.remove(id)
			}

			override fun onAdDismissedFullScreenContent() {
				callback(1)
				loadedInterstitials.remove(id)
			}

			override fun onAdShowedFullScreenContent() {
				callback(2)
			}

			override fun onAdImpression() {
				callback(3)
			}

			override fun onAdClicked() {
				callback(4)
			}
		}
		loadedInterstitials[id]?.show(act)
	}

	//	Rewarded Logic

	fun loadRewarded(id: String, callback: (Boolean) -> Unit) {
		var adId = "ca-app-pub-3940256099942544/5224354917"
		if (!BuildConfig.DEBUG) {
			adId = id
		}

		RewardedAd.load(
			act,
			adId,
			AdRequest.Builder().build(),
			object : RewardedAdLoadCallback() {
				override fun onAdLoaded(ad: RewardedAd) {
					callback(true)
					loadedRewarded[id] = ad
				}

				override fun onAdFailedToLoad(adError: LoadAdError) {
					callback(false)
					loadedRewarded.remove(id)
				}
			},
		)
	}

	fun releaseRewarded(id: String) {
		loadedRewarded.remove(id)
	}

	fun showRewarded(id: String, callback: (Int) -> Unit, rewardCallback: (Int, String) -> Unit) {
		if (loadedRewarded[id] == null) {
			callback(0)
		}
		loadedRewarded[id]?.fullScreenContentCallback = object : FullScreenContentCallback() {
			override fun onAdFailedToShowFullScreenContent(adError: AdError) {
				callback(0)
				loadedRewarded.remove(id)
			}

			override fun onAdDismissedFullScreenContent() {
				callback(1)
				loadedRewarded.remove(id)
			}

			override fun onAdShowedFullScreenContent() {
				callback(2)
			}

			override fun onAdImpression() {
				callback(3)
			}

			override fun onAdClicked() {
				callback(4)
			}
		}

		loadedRewarded[id]?.show(
			act,
		) { rewardItem ->
			rewardCallback(rewardItem.amount, rewardItem.type)
		}
	}
}