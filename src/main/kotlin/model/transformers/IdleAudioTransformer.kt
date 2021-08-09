package model.transformers

class IdleAudioTransformer: AudioTransformer {
    override fun outputSize(input: ByteArray): Int {
        return input.size
    }

    override fun transform(input: ByteArray): ByteArray {
        return input
    }

    override fun transform(input: ByteArray, output: ByteArray) {
        input.copyInto(output)
    }

    override fun toJson(indent: String): String {

        return """
            |{
                "type": "IDLE"
            }""".trimIndent().prependIndent(indent).trimMargin()
    }
}