package viewmodels.api.utils

interface ClipUnitConverter {
    fun toUs(absPx: Float): Long
    fun toAbsPx(us: Long): Float

    fun toAbsSize(winPx: Float): Float
    fun toAbsOffset(winPx: Float): Float

    fun toWinSize(absPx: Float): Float
    fun toWinOffset(absPx: Float): Float
}