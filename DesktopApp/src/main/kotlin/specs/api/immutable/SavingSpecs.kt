package specs.api.immutable

import java.io.File

interface SavingSpecs {
    val defaultPreprocessedClipSavingDir: File
    val defaultTransformedClipSavingDir: File
    val defaultClipMetadataSavingDir: File
}