package model.api.editor.clip.fragment.transformer

interface SilenceTransformer: FragmentTransformer {
    var silenceDurationUs: Long

    override val type: FragmentTransformer.Type get() = FragmentTransformer.Type.SILENCE
}