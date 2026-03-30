package com.example.mpp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpp.data.API
import com.example.mpp.data.models.ApiDetailResponse
import com.example.mpp.data.models.notification.NotificationModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = API.getNotifications()
                if (result != null) {
                    _notifications.value = result
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun decideParticipation(notificationId: String, requestId: String, decision: String) {
        viewModelScope.launch {
            try {
                val response = API.decideParticipationWithResponse(requestId, decision)
                handleApiResponse(response, notificationId)
            } catch (e: Exception) {
                _snackbarMessage.emit("Erreur réseau")
            }
        }
    }

    fun leaveActivity(notificationId: String, activityId: String) {
        viewModelScope.launch {
            try {
                val response = API.leaveActivityWithResponse(activityId)
                handleApiResponse(response, notificationId)
            } catch (e: Exception) {
                _snackbarMessage.emit("Erreur réseau")
            }
        }
    }

    fun cancelParticipationRequest(notificationId: String, requestId: String) {
        viewModelScope.launch {
            try {
                val response = API.cancelParticipationRequestWithResponse(requestId)
                handleApiResponse(response, notificationId)
            } catch (e: Exception) {
                _snackbarMessage.emit("Erreur réseau")
            }
        }
    }

    fun dismissNotification(notificationId: String) {
        viewModelScope.launch {
            val current = _notifications.value
            val index = current.indexOfFirst { it.id == notificationId }
            if (index < 0) return@launch

            val dismissedNotification = current[index]
            _notifications.value = current.filter { it.id != notificationId }

            try {
                val response = API.markNotificationAsReadWithResponse(notificationId)
                if (!response.isSuccessful) {
                    _notifications.value = _notifications.value.toMutableList().also {
                        it.add(index, dismissedNotification)
                    }
                    _snackbarMessage.emit(extractErrorMessage(response))
                }
            } catch (e: Exception) {
                _notifications.value = _notifications.value.toMutableList().also {
                    it.add(index, dismissedNotification)
                }
                _snackbarMessage.emit("Erreur réseau")
            }
        }
    }

    private suspend fun handleApiResponse(response: Response<*>, notificationId: String) {
        if (response.isSuccessful) {
            val detail = try {
                val bodyString = response.body()?.toString()
                // On essaie de parser le succès aussi si l'API renvoie {detail: ...} en 200
                Gson().fromJson(bodyString, ApiDetailResponse::class.java).detail
            } catch (e: Exception) {
                "Action réussie"
            }
            _snackbarMessage.emit(detail)
            _notifications.value = _notifications.value.filter { it.id != notificationId }
        } else {
            _snackbarMessage.emit(extractErrorMessage(response))
        }
    }

    private fun extractErrorMessage(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val apiError = Gson().fromJson(errorBody, ApiDetailResponse::class.java)
            apiError.detail
        } catch (e: Exception) {
            when (response.code()) {
                in 400..499 -> "Erreur client (${response.code()})"
                in 500..599 -> "Erreur serveur (${response.code()})"
                else -> "Erreur inconnue"
            }
        }
    }
}
