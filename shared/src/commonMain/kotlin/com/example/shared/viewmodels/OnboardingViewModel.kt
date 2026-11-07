package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.lock.AppLockManager
import kotlinx.coroutines.launch

class OnboardingViewModel(private val manager: AppLockManager) : ViewModel() {
    fun finish() {
        viewModelScope.launch { manager.finishOnboarding() }
    }
}