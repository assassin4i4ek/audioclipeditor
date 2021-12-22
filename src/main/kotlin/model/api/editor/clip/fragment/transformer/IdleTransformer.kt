package model.api.editor.clip.fragment.transformer

interface IdleTransformer: FragmentTransformer {
    override val type: FragmentTransformer.Type get() = FragmentTransformer.Type.IDLE
}
