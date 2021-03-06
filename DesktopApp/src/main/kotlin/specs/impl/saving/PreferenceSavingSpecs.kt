package specs.impl.saving

import specs.api.mutable.MutableSavingSpecs
import specs.impl.utils.BaseStatefulPreferenceSpecsImpl
import specs.impl.utils.PreferenceSavableProperty
import java.io.File
import java.util.prefs.Preferences

class PreferenceSavingSpecs: BaseStatefulPreferenceSpecsImpl(), MutableSavingSpecs {
    override val preferences: Preferences = Preferences.userNodeForPackage(this.javaClass)
    override val properties: MutableList<PreferenceSavableProperty<*, *, *>> = mutableListOf()

    override var defaultPreprocessedClipSavingDir: File by savableProperty(
        File(System.getProperty("user.dir")).resolve("Clips").resolve("Preprocessed Clips"),
        ::defaultPreprocessedClipSavingDir
    )

    override var defaultTransformedClipSavingDir: File by savableProperty(
        File(System.getProperty("user.dir")).resolve("Clips").resolve("Transformed Clips"),
        ::defaultTransformedClipSavingDir
    )

    override var defaultClipMetadataSavingDir: File by savableProperty(
        File(System.getProperty("user.dir")).resolve("Clips").resolve("Transformed Clips Metadata"),
        ::defaultClipMetadataSavingDir
    )
}