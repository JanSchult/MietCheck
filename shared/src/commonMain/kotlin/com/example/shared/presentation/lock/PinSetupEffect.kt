package com.example.shared.presentation.lock

sealed interface PinSetupEffect { data object Done : PinSetupEffect }
