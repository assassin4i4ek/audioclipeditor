package specs.impl.accounting

import specs.api.mutable.MutableAccountingServiceSpecs
import specs.impl.utils.BasePreferenceSpecsImpl
import specs.impl.utils.PreferenceSavableProperty
import java.io.File
import java.util.prefs.Preferences

class PreferenceAccountingServiceSpecs: BasePreferenceSpecsImpl(), MutableAccountingServiceSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)
    override val properties: MutableList<PreferenceSavableProperty<*, *, *>> = mutableListOf()

    override var excelFile: File by savableProperty(
        File(System.getProperty("user.dir")).resolve("Clips").resolve("Processed Clips.xlsx"),
        ::excelFile
    )
}