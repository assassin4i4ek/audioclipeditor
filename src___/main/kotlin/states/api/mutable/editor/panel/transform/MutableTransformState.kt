package states.api.mutable.editor.panel.transform

import states.api.immutable.editor.panel.transform.TransformState

interface MutableTransformState: TransformState {
    override var xAbsoluteOffsetPx: Float
    override var zoom: Float
}