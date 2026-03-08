package com.amaya.mytracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrackerViewModel : ViewModel() {
    // In a professional app, this would be injected via Hilt
    private val repository = TrackerRepository()

    private val _lista = MutableStateFlow<List<TrackItem>>(emptyList())
    val lista: StateFlow<List<TrackItem>> = _lista

    private val _searchResults = MutableStateFlow<List<MangaData>>(emptyList())
    val searchResults: StateFlow<List<MangaData>> = _searchResults

    init {
        viewModelScope.launch {
            // Collecting the Flow from the repository and updating our UI state
            repository.getTrackItems().collectLatest { items ->
                _lista.value = items
            }
        }
    }

    fun searchMangaApi(query: String) {
        if (query.length < 3) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            // The ViewModel doesn't care how the search happens, it just asks the repo
            _searchResults.value = repository.searchManga(query)
        }
    }

    fun addItemFromApi(manga: MangaData) {
        repository.addTrackItem(manga)
        _searchResults.value = emptyList()
    }

    fun updateItem(id: String, updates: Map<String, Any>) {
        repository.updateTrackItem(id, updates)
    }

    fun deleteItem(id: String) {
        repository.deleteTrackItem(id)
    }
}