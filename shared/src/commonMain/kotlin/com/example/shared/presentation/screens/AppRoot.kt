package com.example.shared.presentation.screens


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.lock.LockState
import com.example.shared.presentation.navigation.AppNavigation
import org.koin.compose.koinInject

@Composable
fun AppRoot(manager: AppLockManager = koinInject()) {
    val state by manager.state.collectAsState()
    when (state) {
        LockState.NEEDS_ONBOARDING -> OnboardingScreen()
        LockState.LOCKED -> LockScreen()
        LockState.UNLOCKED -> AppNavigation()
    }
}