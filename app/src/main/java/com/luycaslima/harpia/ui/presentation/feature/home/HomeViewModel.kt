package com.luycaslima.harpia.ui.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.luycaslima.harpia.data.local.ReconstructionProject
import com.luycaslima.harpia.data.repository.ProjectStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val projects : List<ReconstructionProject>) : HomeUiState
    data class Error(val message : String) : HomeUiState
}

class HomeViewModel(val repository: ProjectStorageRepository): ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState : StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects(){
        viewModelScope.launch {
            if (_uiState.value !is HomeUiState.Success){
                _uiState.value = HomeUiState.Loading
            }

            try {
                val projects = repository.getAllProjects()
                _uiState.value = HomeUiState.Success(projects)
            }catch (e : Exception){
                _uiState.value = HomeUiState.Error(
                    e.localizedMessage ?: "Failed to load saved Projects"
                )
            }
        }
    }

    fun deleteProject( projectID : String) {
        if (projectID.isBlank() ) return
        viewModelScope.launch {
            try{
                repository.deleteProject(projectID)

                _uiState.update {currentState ->
                    if(currentState is HomeUiState.Success){
                        currentState.copy(
                            projects = currentState.projects.filterNot { it.id == projectID }
                        )
                    }else {
                        currentState
                    }
                }
            }catch ( e : Exception) {
                _uiState.value = HomeUiState.Error(
                    e.localizedMessage ?: "Failed to delete project"
                )
            }
        }

    }

    //What is this?
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                HomeViewModel(ProjectStorageRepository(app.applicationContext))
            }
        }
    }
}