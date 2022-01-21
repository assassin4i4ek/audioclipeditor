package specs.api.immutable

interface ProcessingSpecs {
    val fetchClipsOnAppStart: Boolean
    val closeAppOnProcessingFinish: Boolean
}