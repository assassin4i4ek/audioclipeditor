package specs.api.mutable

import specs.api.immutable.ProcessingSpecs

interface MutableProcessingSpecs: ProcessingSpecs, MutableSpecs {
    override var fetchClipsOnAppStart: Boolean
    override var closeAppOnProcessingFinish: Boolean
}