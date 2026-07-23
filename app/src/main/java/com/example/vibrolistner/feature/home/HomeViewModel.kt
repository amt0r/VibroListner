package com.example.vibrolistner.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibrolistner.core.data.KeywordRepository
import com.example.vibrolistner.core.database.KeywordEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val keywords: List<KeywordEntity> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val keywordRepository: KeywordRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = keywordRepository.getAllKeywords()
        .map { HomeUiState(keywords = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun deleteKeyword(keyword: KeywordEntity) {
        viewModelScope.launch {
            keywordRepository.deleteKeyword(keyword)
        }
    }
}
