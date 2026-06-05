package com.example.mandalunch.data.remote

import android.annotation.SuppressLint
import com.example.mandalunch.domain.model.Coordinates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * FusedLocationProviderClient를 코루틴으로 래핑한 Data 레이어 데이터소스.
 *
 * 위치 권한이 부여된 상태에서 호출되어야 한다. 호출 측(ViewModel/Screen)에서
 * Compose [androidx.activity.compose.rememberLauncherForActivityResult]로 권한을
 * 확보한 뒤 호출하므로 `@SuppressLint("MissingPermission")`을 사용한다.
 */
class FusedLocationDataSource @Inject constructor(
    private val client: FusedLocationProviderClient
) {

    /**
     * 현재 위치를 1회 조회해 [Coordinates]로 반환한다.
     *
     * @throws SecurityException 권한 미부여 상태에서 호출 시 (시스템이 발생)
     * @throws IllegalStateException 위치를 가져올 수 없을 때 (GPS off 등)
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrent(): Coordinates = suspendCancellableCoroutine { cont ->
        val task = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        task.addOnSuccessListener { location ->
            if (location != null) {
                cont.resume(Coordinates(location.latitude, location.longitude))
            } else {
                cont.resumeWithException(
                    IllegalStateException("Location unavailable. Check GPS settings.")
                )
            }
        }
        task.addOnFailureListener { e ->
            cont.resumeWithException(e)
        }
        task.addOnCanceledListener {
            if (cont.isActive) {
                cont.resumeWithException(IllegalStateException("Location request was canceled."))
            }
        }
    }
}
