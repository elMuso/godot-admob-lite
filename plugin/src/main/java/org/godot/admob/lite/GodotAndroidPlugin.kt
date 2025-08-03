// TODO: Update to match your plugin's package name.
package org.godot.admob.lite

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.collection.ArraySet
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

@Suppress("unused")
class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {
	override fun getPluginName() = "GodotAdmobLite"
	private var canShowAds: Boolean = false
	private lateinit var adContainer: FrameLayout
	private lateinit var adLogic: AdLogic
	private lateinit var umpLogic: UMPLogic

	override fun onMainCreate(activity: Activity?): View {
		val aActivity = super.getActivity()!!
		adContainer = FrameLayout(aActivity)
		adLogic = AdLogic(activity!!, adContainer)
		umpLogic = UMPLogic(activity)
		val layoutParams = ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
		)

		// Set the LayoutParams on the FrameLayout
		adContainer.layoutParams = layoutParams
		return adContainer
	}

	override fun getPluginSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = ArraySet()
		signals.add(SignalInfo("on_initialization_complete", Boolean::class.javaObjectType))
		signals.add(SignalInfo("on_interstitial_loaded", String::class.javaObjectType, Boolean::class.javaObjectType))
		signals.add(SignalInfo("on_interstitial_shown", String::class.javaObjectType, Int::class.javaObjectType))
		signals.add(SignalInfo("on_rewarded_loaded", String::class.javaObjectType, Boolean::class.javaObjectType))
		signals.add(SignalInfo("on_reward_shown", String::class.javaObjectType, Int::class.javaObjectType))
		signals.add(SignalInfo("on_reward_received", String::class.javaObjectType, Int::class.javaObjectType))
		return signals
	}

	// GENERAL UMP METHODS
	@UsedByGodot
	fun enableDebugMode(deviceId: String, mode: Int) {
		umpLogic.enableDebugMode(deviceId, mode)
	}

	@UsedByGodot
	fun showUmpForm() {
		umpLogic.gatherAndExecute(true) { canShow ->
			canShowAds = canShow
			emitSignal("on_initialization_complete", canShowAds)
			if (canShow) {
				adLogic.initialize()
			}
		}
	}

	@UsedByGodot
	fun showPrivacyForm() {
		umpLogic.forcePrivacyForm(activity!!) {}
	}

	@UsedByGodot
	fun debugResetForm() {
		umpLogic.debugReset()
	}

	// GENERAL AD METHODS

	@UsedByGodot
	fun initialize() { //Checks Google's ump and also initializes ads if successful
		runOnUiThread {
			umpLogic.gatherAndExecute(false) { canShow ->
				canShowAds = canShow
				emitSignal("on_initialization_complete", canShowAds)
				if (canShow) {
					adLogic.initialize()
				}
			}
		}
	}

	@UsedByGodot
	fun loadAndShowBanner(id: String, size: Int, position: Int) {
		if (!canShowAds) return

		runOnUiThread {
			adLogic.loadBanner(id, size, position)
		}
	}

	@UsedByGodot
	fun destroyBanner(id: String) {
		if (!canShowAds) return

		runOnUiThread {
			adLogic.destroyBanner(id)
		}
	}

	@UsedByGodot
	fun loadInterstitial(id: String) {
		if (!canShowAds) {
			emitSignal("on_interstitial_loaded", id, false)
		}

		runOnUiThread {
			adLogic.loadInterstitial(id) { r ->
				emitSignal("on_interstitial_loaded", id, r)
			}
		}
	}

	@UsedByGodot
	fun showInterstitial(id: String) {
		if (!canShowAds) {
			emitSignal("on_interstitial_shown", id, 0)
		}

		runOnUiThread {
			adLogic.showInterstitial(id) { status ->
				emitSignal("on_interstitial_shown", id, status)
			}
		}
	}

	@UsedByGodot
	fun releaseInterstitial(id: String) {
		runOnUiThread {
			adLogic.releaseInterstitial(id)
		}
	}

	@UsedByGodot
	fun loadRewarded(id: String) {
		if (!canShowAds) {
			emitSignal("on_rewarded_loaded", id, false)
		}

		runOnUiThread {
			adLogic.loadRewarded(id) { r ->
				emitSignal("on_rewarded_loaded", id, r)
			}
		}
	}

	@UsedByGodot
	fun showRewarded(id: String) {
		if (!canShowAds) {
			emitSignal("on_reward_shown", id, 0)
		}

		runOnUiThread {
			adLogic.showRewarded(id, { status -> emitSignal("on_reward_shown", id, status) }, { amount, type -> emitSignal("on_reward_received", id, amount, type) })
		}
	}

	@UsedByGodot
	fun releaseRewarded(id: String) {
		runOnUiThread {
			adLogic.releaseRewarded(id)
		}
	}

}
