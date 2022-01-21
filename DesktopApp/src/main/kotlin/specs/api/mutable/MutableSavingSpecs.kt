package specs.api.mutable

import specs.api.immutable.SavingSpecs
import java.io.File

interface MutableSavingSpecs: SavingSpecs, MutableSpecs {
    override var defaultPreprocessedClipSavingDir: File
    override var defaultTransformedClipSavingDir: File
    override var defaultClipMetadataSavingDir: File
}