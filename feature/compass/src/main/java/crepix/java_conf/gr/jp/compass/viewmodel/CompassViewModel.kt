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
    private val uiStateInternal = MutableStateFlow(UiState(0f, 0f))

    val uiState: StateFlow<UiState> = uiStateInternal

    companion object {
        private const val UI_REFRESH_RATE = 20L
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            directionRepository.degree.collect {
                // TODO degreeに適用する
                if (plateVelocity == null) {
                    uiStateInternal.value = UiState(it, it)
                    plateVelocity = 0f
                } else {
                    val velocity = (it - uiStateInternal.value.planeDegree) / 10f
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
                    val planeDegree = uiStateInternal.value.planeDegree + it
                    val needleDegree =
                        if (isNeedleFixed) {
                            needleVelocity = 0
                            uiStateInternal.value.needleDegree
                        } else if (abs(planeDegree - uiStateInternal.value.needleDegree) <= 3f && needleVelocity <= 3) {
                            needleVelocity = 0
                            planeDegree
                        } else {
                            if (planeDegree < uiStateInternal.value.needleDegree) {
                                if (needleVelocity >= -15) needleVelocity -= 3
                                if (needleVelocity >= 0) needleVelocity -= 1
                            } else {
                                if (needleVelocity <= 15) needleVelocity += 3
                                if (needleVelocity <= 0) needleVelocity += 1
                            }
                            uiStateInternal.value.needleDegree + needleVelocity
                        }
                    uiStateInternal.value = UiState(planeDegree, needleDegree)
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
    }

    fun releaseNeedlePosition() {
        isNeedleFixed = false
    }

    data class UiState(
        val planeDegree: Float,
        val needleDegree: Float
    )
}
