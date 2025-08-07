package org.godot.admob.lite;

import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.HashMap;
import java.util.Map;

public class AdLogic {
    private final Activity activity;
    private final FrameLayout container;

    // Maps to store loaded ads by their IDs
    private final Map<String, AdView> loadedBanners = new HashMap<>();
    private final Map<String, InterstitialAd> loadedInterstitials = new HashMap<>();
    private final Map<String, RewardedAd> loadedRewarded = new HashMap<>();

    // Interface for callbacks
    public interface AdLoadCallback {
        void onResult(boolean success);
    }

    public interface InterstitialShowCallback {
        void onResult(int event);
    }

    public interface RewardedShowCallback {
        void onResult(int event);
    }

    public interface RewardedRewardCallback {
        void onReward(int amount, String type);
    }

    public AdLogic(Activity act, FrameLayout container) {
        this.activity = act;
        this.container = container;
    }

    public void initialize() {
        MobileAds.initialize(activity);
    }

    // Banner Logic

    public void loadBanner(String id, int size, int position) {
        destroyBanner(id); // Destroy any existing banner with the same ID

        AdView newBanner = new AdView(activity);
        if (BuildConfig.DEBUG) { 
            newBanner.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // Test Ad Unit ID
        } else {
            newBanner.setAdUnitId(id);
        }

        // Set the AdSize based on the 'size' parameter
        if (size > 40) {
            newBanner.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, size));
        } else {
            switch (size) {
                case 0:
                    newBanner.setAdSize(AdSize.BANNER);
                    break;
                case 1:
                    newBanner.setAdSize(AdSize.LARGE_BANNER);
                    break;
                case 2:
                    newBanner.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    break;
                case 3:
                    newBanner.setAdSize(AdSize.FULL_BANNER);
                    break;
                case 4:
                    newBanner.setAdSize(AdSize.LEADERBOARD);
                    break;
            }
        }

        // Determine the gravity based on the 'position' parameter
        int gravity = switch (position) {
            case 0 -> Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            case 1 -> Gravity.CENTER;
            case 2 -> Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            default ->
                    Gravity.TOP | Gravity.CENTER_HORIZONTAL; // Default to top if an invalid position is provided
        };

        // Create a new FrameLayout.LayoutParams with the determined gravity
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, // Width
                FrameLayout.LayoutParams.WRAP_CONTENT, // Height
                gravity
        );

        // Add the new banner to the container with the specified layout parameters
        container.addView(newBanner, layoutParams);

        AdRequest adRequest = new AdRequest.Builder().build();
        newBanner.loadAd(adRequest);
        loadedBanners.put(id, newBanner);
    }

    public void destroyBanner(String id) {
        AdView banner = loadedBanners.get(id);
        if (banner != null) {
            // Remove banner from view hierarchy.
            ViewGroup parentView = (ViewGroup) banner.getParent();
            if (parentView != null) {
                parentView.removeView(banner);
            }
            banner.destroy();
            loadedBanners.remove(id);
        }
    }

    // Interstitial Logic

    public void loadInterstitial(String id, final AdLoadCallback callback) {
        String adId = "ca-app-pub-3940256099942544/1033173712"; // Test Ad Unit ID
        if (!BuildConfig.DEBUG) { // Assuming BuildConfig.DEBUG is false for release
            adId = id;
        }

        InterstitialAd.load(
                activity,
                adId,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        callback.onResult(true);
                        loadedInterstitials.put(id, ad);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        callback.onResult(false);
                        loadedInterstitials.remove(id);
                    }
                }
        );
    }

    public void releaseInterstitial(String id) {
        loadedInterstitials.remove(id);
    }

    public void showInterstitial(String id, final InterstitialShowCallback callback) {
        InterstitialAd interstitialAd = loadedInterstitials.get(id);
        if (interstitialAd == null) {
            callback.onResult(0); // Ad not loaded
            return;
        }

        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                callback.onResult(0); // Failed to show
                loadedInterstitials.remove(id);
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                callback.onResult(1); // Dismissed
                loadedInterstitials.remove(id);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                callback.onResult(2); // Showed
            }

            @Override
            public void onAdImpression() {
                callback.onResult(3); // Impression
            }

            @Override
            public void onAdClicked() {
                callback.onResult(4); // Clicked
            }
        });
        interstitialAd.show(activity);
    }

    // Rewarded Logic

    public void loadRewarded(String id, final AdLoadCallback callback) {
        String adId = "ca-app-pub-3940256099942544/5224354917"; // Test Ad Unit ID
        if (!BuildConfig.DEBUG) { // Assuming BuildConfig.DEBUG is false for release
            adId = id;
        }

        RewardedAd.load(
                activity,
                adId,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        callback.onResult(true);
                        loadedRewarded.put(id, ad);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        callback.onResult(false);
                        loadedRewarded.remove(id);
                    }
                }
        );
    }

    public void releaseRewarded(String id) {
        loadedRewarded.remove(id);
    }

    public void showRewarded(String id, final RewardedShowCallback callback, final RewardedRewardCallback rewardCallback) {
        RewardedAd rewardedAd = loadedRewarded.get(id);
        if (rewardedAd == null) {
            callback.onResult(0); // Ad not loaded
            return;
        }

        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                callback.onResult(0); // Failed to show
                loadedRewarded.remove(id);
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                callback.onResult(1); // Dismissed
                loadedRewarded.remove(id);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                callback.onResult(2); // Showed
            }

            @Override
            public void onAdImpression() {
                callback.onResult(3); // Impression
            }

            @Override
            public void onAdClicked() {
                callback.onResult(4); // Clicked
            }
        });

        rewardedAd.show(
                activity, rewardItem -> rewardCallback.onReward(rewardItem.getAmount(), rewardItem.getType()));
    }
}