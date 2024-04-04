package crepix.java_conf.gr.jp.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * 方位を取得するRepository
 */
interface DirectionRepository {
    /**
     * 方位(度)
     */
    val degree: StateFlow<Float>

    /**
     * 誤差があるかどうか
     */
    val isError: StateFlow<Boolean>

    /**
     * 方位の監視を開始する
     */
    suspend fun start()

    /**
     * 方位の監視を終了する
     */
    suspend fun stop()
}
