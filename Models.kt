package com.medspace.app.data

data class Profile(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String,
    val status: String
) {
    val isAdmin: Boolean get() = role == "super_admin"
}

fun statusLabel(status: String): String = when (status) {
    "pending" -> "قيد المراجعة"
    "approved" -> "مقبول"
    "rejected" -> "مرفوض"
    "suspended" -> "موقوف"
    else -> status
}

sealed interface AuthState {
    object Loading : AuthState
    object SignedOut : AuthState
    data class Active(val profile: Profile) : AuthState
    data class Blocked(val profile: Profile) : AuthState
    data class Failure(val message: String) : AuthState
}
