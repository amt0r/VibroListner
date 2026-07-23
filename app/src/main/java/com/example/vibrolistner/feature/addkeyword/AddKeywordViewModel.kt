package com.example.vibrolistner.feature.addkeyword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibrolistner.core.data.KeywordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddKeywordUiState(
    val keywordText: String = "",
    val isSaved: Boolean = false,
)

@HiltViewModel
class AddKeywordViewModel @Inject constructor(
    private val keywordRepository: KeywordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddKeywordUiState())
    val uiState: StateFlow<AddKeywordUiState> = _uiState.asStateFlow()

    fun onKeywordTextChanged(text: String) {
        _uiState.update { it.copy(keywordText = text) }
    }

    fun saveKeyword() {
        val keyword = _uiState.value.keywordText.trim()
        if (keyword.isBlank()) return

        viewModelScope.launch {
            keywordRepository.insertKeyword(keyword)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
