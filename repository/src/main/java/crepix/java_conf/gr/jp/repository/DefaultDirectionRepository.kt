package crepix.java_conf.gr.jp.repository

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.math.floor

/**
 * [DirectionRepository]のデフォルト実装
 */
class DefaultDirectionRepository @Inject constructor(
    private val manager: SensorManager
) : DirectionRepository {
    private var accelerometerReading: FloatArray? = null
    private var magnetometerReading: FloatArray? = null
    private val rotationMatrix = FloatArray(16)
    private val inclinationMatrix = FloatArray(16)
    private val remappedMatrix = FloatArray(16)
    private val orientationAngles = FloatArray(3)
    private val listener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            when (p0?.sensor?.type) {
                Sensor.TYPE_ACCELEROMETER -> accelerometerReading = p0.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading = p0.values.clone()
            }
            accelerometerReading?.let { accelerometer ->
                magnetometerReading?.let {  magnetometer ->
                    SensorManager.getRotationMatrix(
                        rotationMatrix,
                        inclinationMatrix,
                        accelerometer,
                        magnetometer
                    )
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedMatrix
                    )
                    SensorManager.getOrientation(remappedMatrix, orientationAngles)
                    degreeInternal.value = 720 - Math.toDegrees(orientationAngles[0].toDouble()).toFloat() - Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            // nothing to do
        }
    }

    private val degreeInternal = MutableStateFlow(0f)

    override val degree: StateFlow<Float> = degreeInternal

    override suspend fun start() {
        manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            manager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            manager.registerListener(
                listener,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override suspend fun stop() {
        manager.unregisterListener(listener)
        accelerometerReading = null
        magnetometerReading = null
    }

}
