/*
 * Copyright (C) 2023-2024 the risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rising.settings.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.SwitchPreferenceCompat
import com.android.internal.logging.nano.MetricsProto
import com.android.internal.util.android.Utils
import com.android.settings.preferences.CustomSeekBarPreference
import com.android.settings.R
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settings.utils.SystemRestartUtils
import com.android.settingslib.search.SearchIndexable

@SearchIndexable
class Notifications : OptimizedSettingsFragment(), Preference.OnPreferenceChangeListener {

    companion object {
        const val TAG = "Notifications"
        
        private const val COMPACT_HUN_KEY = "persist.sys.compact_hun.enabled"
        private const val FLASHLIGHT_CATEGORY = "flashlight_category"
        private const val FLASHLIGHT_CALL_PREF = "flashlight_on_call"
        private const val FLASHLIGHT_DND_PREF = "flashlight_on_call_ignore_dnd"
        private const val FLASHLIGHT_RATE_PREF = "flashlight_on_call_rate"

        /**
         * For search
         */
        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER = object : BaseSearchIndexProvider(R.xml.rising_settings_notification) {
            override fun getNonIndexableKeys(context: Context): List<String> {
                val keys = super.getNonIndexableKeys(context).toMutableList()
                return keys
            }
        }
    }
    
    private var mCompactHUNPref: Preference? = null
    private var mFlashOnCall: ListPreference? = null
    private var mFlashOnCallIgnoreDND: SwitchPreferenceCompat? = null
    private var mFlashOnCallRate: CustomSeekBarPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.rising_settings_notification)

        val prefScreen: PreferenceScreen = preferenceScreen
        val mContext: Context = requireActivity().applicationContext
        val resolver: ContentResolver = mContext.contentResolver

        mCompactHUNPref = findPreference(COMPACT_HUN_KEY)
        mCompactHUNPref?.setOnPreferenceChangeListener(this)

    if (!Utils.deviceHasFlashlight(mContext)) {
        val flashlightCategory = prefScreen.findPreference<PreferenceCategory>(FLASHLIGHT_CATEGORY)
        if (flashlightCategory != null) {
            prefScreen.removePreference(flashlightCategory)
        }
    } else {
        mFlashOnCall = prefScreen.findPreference<ListPreference>(FLASHLIGHT_CALL_PREF)
        mFlashOnCall?.setOnPreferenceChangeListener(this)

        mFlashOnCallIgnoreDND = prefScreen.findPreference<SwitchPreferenceCompat>(FLASHLIGHT_DND_PREF)
        val value = Settings.System.getInt(resolver, Settings.System.FLASHLIGHT_ON_CALL, 0)

        mFlashOnCallRate = prefScreen.findPreference<CustomSeekBarPreference>(FLASHLIGHT_RATE_PREF)

        mFlashOnCallIgnoreDND?.isEnabled = value > 1
        mFlashOnCallRate?.isEnabled = value > 0
    }
}
    
    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return when (preference) {
        mCompactHUNPref -> {
            val context = getSafeContext()
            context?.let { SystemRestartUtils.showSystemUIRestartDialog(it) }
            true
        }
        mFlashOnCall -> {
            val value = (newValue as String).toInt()
            mFlashOnCallIgnoreDND?.isEnabled = value > 1
            mFlashOnCallRate?.isEnabled = value > 0
            true
        }
        else -> false
    }
}

    override fun getMetricsCategory(): Int {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN
    }
}
