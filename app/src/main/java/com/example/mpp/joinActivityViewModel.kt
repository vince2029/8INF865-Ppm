package com.example.mpp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpp.data.API
import com.example.mpp.data.models.activity.ActivityModel
import com.example.mpp.data.models.dog.DogModel
import com.example.mpp.data.models.participations.ParticipantModel
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val activityId: String) : ViewModel() {

    var activityState by mutableStateOf<ActivityModel?>(null)
    var requestSent by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var invalidDogMessage by mutableStateOf<String?>(null)
    var hasLeft by mutableStateOf(false)
    var participants by mutableStateOf<List<ParticipantModel>>(emptyList())
    var creatorUserDog by mutableStateOf<UserDog?>(null)
    var acceptedUserDogs by mutableStateOf<List<UserDog>>(emptyList())
    var currentUserDog by mutableStateOf<UserDog?>(null)
    var canJoin by mutableStateOf(false)


    init { load() }

    fun load() {
        viewModelScope.launch {
            val act = API.getActivity(activityId)
            if (act == null) {
                errorMessage = "Impossible de charger l'activité"
                return@launch
            }
            activityState = act

            val parts = API.getParticipants(activityId) ?: emptyList()
            participants = parts

            val userId = API.currentUserId ?: return@launch
            val currentDog = API.getDog(userId)

            if (currentDog != null) {
                currentUserDog = UserDog("currentUser", currentDog)
            }


            val creatorDog = API.getDog(act.creatorId)
            if (creatorDog != null) {
                creatorUserDog = UserDog(act.creatorPseudo, creatorDog)
            }

            val dogs = parts.mapNotNull { p ->
                val dog = API.getDog(p.participantId)
                dog?.let { UserDog(p.participantPseudo, it) }
            }
            acceptedUserDogs = dogs

            canJoin = dogValid(currentUserDog?.dog ?: return@launch , activityState?:return@launch)
            if(!canJoin){
                invalidDogMessage = "Votre chien ne respecte pas les critères de l'activité"
            }
        }
    }


    fun join() = viewModelScope.launch {
        val act = activityState ?: return@launch
        val dog = currentUserDog?.dog ?: return@launch

        if (!dogValid(dog, act)) {
            errorMessage = "Votre chien ne respecte pas les critères de l'activité"
            return@launch
        }

        val response = API.joinActivity(activityId)
        if (response.isSuccessful) {
            requestSent = true
            hasLeft = false
            activityState = API.getActivity(activityId)
        } else {
            errorMessage = response.errorBody()?.string()
        }
    }


    fun leave() = viewModelScope.launch {
        val success = API.leaveActivity(activityId)
        if (success) {
            hasLeft = true
            requestSent = false
            errorMessage = null
        } else errorMessage = "Impossible d'annuler votre participation"
    }

    fun cancelRequest(requestId: String) = viewModelScope.launch {
        val success = API.cancelParticipationRequest(requestId)
        if (success) {
            hasLeft = true
            requestSent = false
            activityState = API.getActivity(activityId)
        } else errorMessage = "Impossible d'annuler votre demande"
    }

    fun deleteActivity(onSuccess: () -> Unit) = viewModelScope.launch {
        val success = API.deleteActivity(activityId)
        if (success) onSuccess()
        else errorMessage = "Impossible de supprimer l'activité"
    }

    fun dogValid(dog: DogModel, activity: ActivityModel): Boolean {

        if (dog.energyLevel < activity.minEnergyLevel) return false
        if (dog.energyLevel > activity.maxEnergyLevel) return false

        if (!activity.allowShyDogs && dog.isShy) return false

        val sizeOrder = listOf("PETIT", "MOYEN", "GRAND")

        val dogIndex = sizeOrder.indexOf(dog.size.uppercase())
        val minIndex = sizeOrder.indexOf(activity.minDogSize.uppercase())
        val maxIndex = sizeOrder.indexOf(activity.maxDogSize.uppercase())

        if (dogIndex == -1 || minIndex == -1 || maxIndex == -1) return false
        if (dogIndex !in minIndex..maxIndex) return false

        return true
    }
}
