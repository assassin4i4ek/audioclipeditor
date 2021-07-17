package model.transformers

interface AudioTransformer {
    fun outputSize(input: ByteArray): Int
    fun transform(input: ByteArray): ByteArray
    fun transform(input: ByteArray, output: ByteArray)
}