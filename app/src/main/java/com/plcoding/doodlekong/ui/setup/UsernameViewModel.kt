package com.plcoding.doodlekong.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.doodlekong.repository.SetupRepository
import com.plcoding.doodlekong.util.Constants.MAX_USER_NAME_LENGTH
import com.plcoding.doodlekong.util.Constants.MIN_USER_NAME_LENGTH
import com.plcoding.doodlekong.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel @Inject constructor(
    private val repository: SetupRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    sealed class SetupEvent {
        object InputEmptyError : SetupEvent()
        object InputTooShortEvent : SetupEvent()
        object InputTooLongEvent : SetupEvent()

        data class NavigateToSelectedRoomEvent(val username: String) : SetupEvent()
    }

    private val _setupEvent = MutableSharedFlow<SetupEvent>()
    val setupEvent: SharedFlow<SetupEvent> = _setupEvent

    fun validateUserNameAndNavigateToSelectedRoom(username: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            val trimmedUserName = username.trim()
            when {
                trimmedUserName.isEmpty() -> _setupEvent.emit(SetupEvent.InputEmptyError)
                trimmedUserName.length < MIN_USER_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooShortEvent)
                trimmedUserName.length > MAX_USER_NAME_LENGTH -> _setupEvent.emit(SetupEvent.InputTooLongEvent)
                else -> _setupEvent.emit(SetupEvent.NavigateToSelectedRoomEvent(username))
            }
        }
    }

}