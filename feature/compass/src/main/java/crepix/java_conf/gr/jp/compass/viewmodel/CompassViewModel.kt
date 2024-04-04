package crepix.java_conf.gr.jp.compass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import crepix.java_conf.gr.jp.repository.DirectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CompassViewModel @Inject constructor(private val directionRepository: DirectionRepository) : ViewModel() {
    private var plateVelocity: Float? = null
    private var needleVelocity = 0
    private var isNeedleFixed = false
    private var fixedDegree = 0f
    private val uiStateInternal = MutableStateFlow(UiState(0f, 0f, false))

    val uiState: StateFlow<UiState> = uiStateInternal

    companion object {
        private const val UI_REFRESH_RATE = 16L
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            directionRepository.degree.collect {
                val degree = if (it <= 0) (it + 3600) % 360 else it % 360
                if (plateVelocity == null) {
                    uiStateInternal.value = UiState(degree, degree, directionRepository.isError.value)
                    plateVelocity = 0f
                } else {
                    val velocity = (degree - uiStateInternal.value.planeDegree) / 10f
                    plateVelocity = if (velocity >= 18f) {
                        (velocity - 36f)
                    } else if (velocity <= -18f) {
                        (velocity + 36f)
                    } else {
                        velocity
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(UI_REFRESH_RATE)

                plateVelocity?.let {
                    val planeDegree = (uiStateInternal.value.planeDegree + it + 360) % 360
                    val needleDegree = if (isNeedleFixed) {
                        needleVelocity = 0
                        fixedDegree
                    } else if (
                        abs(planeDegree - uiStateInternal.value.needleDegree) <= 2f &&
                        needleVelocity <= 2 && needleVelocity >= -2
                    ) {
                        needleVelocity = 0
                        planeDegree
                    } else {
                        val diff = planeDegree - uiStateInternal.value.needleDegree
                        if ((diff <= 0 && diff >= -180) || diff >= 180) {
                            if (needleVelocity >= -15) needleVelocity -= 1
                            if (needleVelocity >= 0) needleVelocity -= 1
                        } else {
                            if (needleVelocity <= 15) needleVelocity += 1
                            if (needleVelocity <= 0) needleVelocity += 1
                        }
                        uiStateInternal.value.needleDegree + needleVelocity
                    } + 360
                    uiStateInternal.value = UiState(planeDegree, needleDegree % 360, directionRepository.isError.value)
                }
            }
        }
    }

    fun startMeasure() {
        viewModelScope.launch {
            directionRepository.start()
        }
    }

    fun stopMeasure() {
        viewModelScope.launch {
            directionRepository.stop()
        }
    }

    fun fixNeedlePosition(degree: Float) {
        isNeedleFixed = true
        fixedDegree = if (degree <= 0) (degree + 3600) % 360 else degree % 360
    }

    fun releaseNeedlePosition(velocity: Int) {
        isNeedleFixed = false
        needleVelocity = velocity
    }

    data class UiState(
        val planeDegree: Float,
        val needleDegree: Float,
        val isError: Boolean
    )
}
