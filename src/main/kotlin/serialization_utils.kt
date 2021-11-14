import kotlinx.serialization.Serializable

@Serializable
data class SerializedAudioClip(
    val srcFilepath: String,
    val destFilepath: String,
    val fragments: List<SerializedAudioFragment>
)

@Serializable
data class SerializedAudioFragment(
    val lowerImmutableAreaStartUs: Long,
    val mutableAreaStartUs: Long,
    val mutableAreaEndUs: Long,
    val upperImmutableAreaEndUs: Long,
    val transformer: SerializedSilenceTransformer
)

@Serializable
data class SerializedSilenceTransformer(
    val type: String,
    val durationUs: Long
)