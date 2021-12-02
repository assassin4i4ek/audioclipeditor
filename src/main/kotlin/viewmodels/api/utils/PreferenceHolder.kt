package viewmodels.api.utils

import java.util.prefs.Preferences

interface PreferenceHolder {
    val preferences: Preferences

    fun reset()
}