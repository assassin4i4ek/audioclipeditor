package views.states.impl.editor.pcm.transform

import views.states.api.editor.pcm.transform.TransformParams

class TransformParamsImpl(
    override val xOffsetDeltaCoef: Float = 50f,
    override val zoomDeltaCoef: Float = 2f
): TransformParams