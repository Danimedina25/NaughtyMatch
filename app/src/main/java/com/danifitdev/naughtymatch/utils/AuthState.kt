package com.danifitdev.naughtymatch.utils

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Failure(val error: Throwable) : AuthState()
}