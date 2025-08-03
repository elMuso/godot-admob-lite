package org.godot.admob.lite

import android.app.Activity
import androidx.preference.PreferenceManager
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.android.ump.UserMessagingPlatform.loadAndShowConsentFormIfRequired

class UMPLogic(// This is available on start but not updated
	private val activity: Activity
) {
	private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)

	private var debugSettings: ConsentDebugSettings.Builder = ConsentDebugSettings.Builder(activity)

	fun enableDebugMode(deviceId: String, mode: Int) {
		debugSettings.addTestDeviceHashedId(deviceId)
		if (mode == 0) {
			debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_DISABLED)
		} else if (mode == 1) {
			debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
		} else if (mode == 2) {
			debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE)
		} else if (mode == 3) {
			debugSettings.setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_OTHER)
		}
	}

	fun gatherAndExecute(force: Boolean, canShowAds: (Boolean) -> Unit) {
		val params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings.build()).build()
		consentInformation.requestConsentInfoUpdate(
			activity,
			params,
			{

				// Called when consent information is successfully updated.
				// [START_EXCLUDE silent]
				if (needsUMP(activity) || force) {
					loadAndShowConsentFormIfRequired(activity) { error ->
						needsPrivacy(activity, canShowAds, error != null && consentInformation.canRequestAds())
					}
				} else {
					needsPrivacy(activity, canShowAds, consentInformation.canRequestAds())
				}
				// [END_EXCLUDE]
			},
			{ requestConsentError ->
				needsPrivacy(activity, canShowAds, true)
			},
		)
	}

	private fun needsPrivacy(activity: Activity, canShowAds: (Boolean) -> Unit, result: Boolean) {

		if (consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
			UserMessagingPlatform.showPrivacyOptionsForm(activity) { r ->
				canShowAds(result)

			}
		} else {
			canShowAds(result)
		}
	}

	fun forcePrivacyForm(activity: Activity, callback: () -> Unit) {
		UserMessagingPlatform.showPrivacyOptionsForm(activity) {}
	}

	// Nice hack to see if we are inside the EU or not
	fun needsUMP(act: Activity): Boolean {
		val prefs = PreferenceManager.getDefaultSharedPreferences(act)
		val gdpr = prefs.getInt("IABTCF_gdprApplies", 1)
		return gdpr == 1
	}

	fun debugReset() {
		consentInformation.reset()
	}
}