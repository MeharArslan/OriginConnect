package com.meharenterprises.originconnect.ui.updates
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor() : ViewModel() {
    private val _hasUpdates = MutableStateFlow(false)
    val hasUpdates: StateFlow<Boolean> = _hasUpdates.asStateFlow()
}
