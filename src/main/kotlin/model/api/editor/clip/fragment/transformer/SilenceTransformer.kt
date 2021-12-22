package model.api.editor.clip.fragment.transformer

interface SilenceTransformer: FragmentTransformer {
    val silenceDurationUs: Long

    override val type: FragmentTransformer.Type get() = FragmentTransformer.Type.SILENCE
}