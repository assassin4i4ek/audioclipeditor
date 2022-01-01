package viewmodels.api.utils

interface PcmPathStepRecommender {
    fun getRecommendedStep(amplifier: Float, zoom: Float): Int
}