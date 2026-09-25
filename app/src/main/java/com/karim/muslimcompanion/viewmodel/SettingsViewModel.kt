package com.karim.muslimcompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karim.muslimcompanion.BuildConfig
import com.karim.muslimcompanion.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val isAzanSoundEnabled: StateFlow<Boolean> = preferencesManager.isAzanSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val calculationMethod: StateFlow<Int> = preferencesManager.calculationMethod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val appVersion: String = BuildConfig.VERSION_NAME

    fun setAzanSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAzanSoundEnabled(enabled)
        }
    }

    fun setCalculationMethod(method: Int) {
        viewModelScope.launch {
            preferencesManager.setCalculationMethod(method)
        }
    }
}
