package crepix.java_conf.gr.jp.repository

import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedOrientationProviderClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * [DirectionRepository]のデフォルト実装
 */
class DefaultDirectionRepository @Inject constructor(
    private val client: FusedOrientationProviderClient
) : DirectionRepository {
    private val degreeInternal = MutableStateFlow(0f)
    private val isErrorInternal = MutableStateFlow(false)

    // Create an FOP listener
    private val listener: DeviceOrientationListener = DeviceOrientationListener { orientation: DeviceOrientation ->
        // Use the orientation object returned by the FOP, e.g.
        degreeInternal.value = orientation.headingDegrees
        isErrorInternal.value = orientation.headingErrorDegrees >= 60
    }

    override val degree: StateFlow<Float> = degreeInternal

    override val isError: StateFlow<Boolean> = isErrorInternal

    override suspend fun start() {
        // Create an FOP request
        val request = DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build()

        // Create (or re-use) an Executor or Looper, e.g.
        val executor = Executors.newSingleThreadExecutor()

        // Register the request and listener
        client.requestOrientationUpdates(request, executor, listener)
    }

    override suspend fun stop() {
        // Unregister the listener
        client.removeOrientationUpdates(listener)
    }
}
