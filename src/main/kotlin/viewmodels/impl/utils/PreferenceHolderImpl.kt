package viewmodels.impl.utils

import viewmodels.api.utils.PreferenceHolder
import java.util.prefs.Preferences

class PreferenceHolderImpl: PreferenceHolder {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)

    override fun reset() {
        preferences.clear()
    }
}