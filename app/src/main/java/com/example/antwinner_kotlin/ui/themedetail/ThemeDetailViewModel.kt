package com.example.antwinner_kotlin.ui.themedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antwinner_kotlin.data.model.ThemeData
import com.example.antwinner_kotlin.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThemeDetailViewModel : ViewModel() {

    private val _themeDetails = MutableLiveData<List<ThemeData>>()
    val themeDetails: LiveData<List<ThemeData>> = _themeDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _updateTime = MutableLiveData<String>()
    val updateTime: LiveData<String> = _updateTime

    init {
        fetchThemeDetails()
    }

    fun fetchThemeDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.apiService.getThemeAverageFluctuation()
                if (response.isSuccessful) {
                    _themeDetails.value = response.body() ?: emptyList()
                    _updateTime.value = getCurrentTimestamp()
                } else {
                    Timber.e("API Error: ${response.code()} - ${response.message()}")
                    _errorMessage.value = "데이터를 불러오는데 실패했습니다. (코드: ${response.code()})"
                }
            } catch (e: Exception) {
                Timber.e(e, "API Exception")
                _errorMessage.value = "네트워크 오류가 발생했습니다: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }
    
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm 기준", Locale.getDefault())
        return sdf.format(Date())
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 