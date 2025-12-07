package com.example.antwinner_kotlin.utils

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class AppUpdateManager(private val activity: Activity) {
    
    private val appUpdateManager: com.google.android.play.core.appupdate.AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val updateRequestCode = 1001
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null
    
    companion object {
        private const val TAG = "AppUpdateManager"
    }
    
    /**
     * 앱 업데이트 가능 여부를 확인하고 업데이트를 시작합니다.
     */
    fun checkForUpdate() {
        Log.d(TAG, "업데이트 확인 시작")
        
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            Log.d(TAG, "업데이트 정보 조회 성공")
            Log.d(TAG, "Update availability: ${appUpdateInfo.updateAvailability()}")
            Log.d(TAG, "Available version code: ${appUpdateInfo.availableVersionCode()}")
            
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    Log.d(TAG, "업데이트 사용 가능")
                    
                    // 즉시 업데이트가 가능한지 확인
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        Log.d(TAG, "즉시 업데이트 가능")
                        showUpdateDialog(appUpdateInfo, AppUpdateType.IMMEDIATE)
                    } 
                    // 유연한 업데이트가 가능한지 확인
                    else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        Log.d(TAG, "유연한 업데이트 가능")
                        showUpdateDialog(appUpdateInfo, AppUpdateType.FLEXIBLE)
                    }
                }
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    Log.d(TAG, "업데이트 없음 - 최신 버전 사용 중")
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    Log.d(TAG, "업데이트 진행 중")
                    // 이미 업데이트가 진행 중인 경우 처리
                }
                else -> {
                    Log.d(TAG, "알 수 없는 업데이트 상태: ${appUpdateInfo.updateAvailability()}")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "업데이트 정보 조회 실패: ${exception.message}", exception)
        }
    }
    
    /**
     * 업데이트 다이얼로그를 표시합니다.
     */
    private fun showUpdateDialog(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        val updateTypeName = if (updateType == AppUpdateType.IMMEDIATE) "즉시" else "유연한"
        Log.d(TAG, "$updateTypeName 업데이트 다이얼로그 표시")
        
        val dialog = AlertDialog.Builder(activity)
            .setTitle("앱 업데이트")
            .setMessage("새로운 버전의 앱이 있습니다.\n더 나은 서비스를 위해 업데이트를 권장합니다.")
            .setPositiveButton("업데이트") { _, _ ->
                startUpdate(appUpdateInfo, updateType)
            }
            .setNegativeButton("나중에") { dialog, _ ->
                Log.d(TAG, "사용자가 업데이트를 연기함")
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            
        dialog.show()
    }
    
    /**
     * 업데이트를 시작합니다.
     */
    private fun startUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        try {
            Log.d(TAG, "업데이트 시작 - 타입: ${if (updateType == AppUpdateType.IMMEDIATE) "즉시" else "유연한"}")
            
            val updateOptions = AppUpdateOptions.newBuilder(updateType).build()
            
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                updateOptions,
                updateRequestCode
            )
            
            // 유연한 업데이트의 경우 설치 상태 리스너 등록
            if (updateType == AppUpdateType.FLEXIBLE) {
                registerInstallStateListener()
            }
            
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "업데이트 시작 실패: ${e.message}", e)
        }
    }
    
    /**
     * 유연한 업데이트를 위한 설치 상태 리스너를 등록합니다.
     */
    private fun registerInstallStateListener() {
        installStateUpdatedListener = InstallStateUpdatedListener { state ->
            Log.d(TAG, "설치 상태 업데이트: ${state.installStatus()}")
            
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    Log.d(TAG, "업데이트 다운로드 완료")
                    showInstallDialog()
                }
                InstallStatus.DOWNLOADING -> {
                    Log.d(TAG, "업데이트 다운로드 중...")
                    // 진행률 표시 가능
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    Log.d(TAG, "다운로드 진행률: $bytesDownloaded / $totalBytesToDownload")
                }
                InstallStatus.FAILED -> {
                    Log.e(TAG, "업데이트 설치 실패")
                    installStateUpdatedListener?.let { listener ->
                        appUpdateManager.unregisterListener(listener)
                    }
                }
                InstallStatus.INSTALLED -> {
                    Log.d(TAG, "업데이트 설치 완료")
                    installStateUpdatedListener?.let { listener ->
                        appUpdateManager.unregisterListener(listener)
                    }
                }
                else -> {
                    Log.d(TAG, "기타 설치 상태: ${state.installStatus()}")
                }
            }
        }
        
        installStateUpdatedListener?.let { listener ->
            appUpdateManager.registerListener(listener)
        }
    }
    
    /**
     * 다운로드 완료 후 설치 확인 다이얼로그를 표시합니다.
     */
    private fun showInstallDialog() {
        val dialog = AlertDialog.Builder(activity)
            .setTitle("업데이트 준비 완료")
            .setMessage("업데이트가 다운로드되었습니다.\n지금 설치하시겠습니까?")
            .setPositiveButton("지금 설치") { _, _ ->
                Log.d(TAG, "사용자가 즉시 설치 선택")
                appUpdateManager.completeUpdate()
            }
            .setNegativeButton("나중에 설치") { dialog, _ ->
                Log.d(TAG, "사용자가 나중에 설치 선택")
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            
        dialog.show()
    }
    
    /**
     * 앱이 재시작될 때 진행 중인 업데이트가 있는지 확인합니다.
     */
    fun checkForInProgressUpdate() {
        Log.d(TAG, "진행 중인 업데이트 확인")
        
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Log.d(TAG, "진행 중인 즉시 업데이트 발견")
                // 즉시 업데이트가 진행 중인 경우 재시작
                try {
                    val updateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        updateOptions,
                        updateRequestCode
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "진행 중인 업데이트 재시작 실패: ${e.message}", e)
                }
            }
        }
    }
    
    /**
     * 리소스 정리
     */
    fun cleanup() {
        Log.d(TAG, "AppUpdateManager 정리")
        // 리스너 해제
        installStateUpdatedListener?.let { listener ->
            appUpdateManager.unregisterListener(listener)
        }
        installStateUpdatedListener = null
    }
}
