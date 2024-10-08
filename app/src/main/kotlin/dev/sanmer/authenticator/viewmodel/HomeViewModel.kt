package dev.sanmer.authenticator.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sanmer.authenticator.ktx.combineToLatest
import dev.sanmer.authenticator.model.auth.Auth
import dev.sanmer.authenticator.repository.DbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dbRepository: DbRepository
) : ViewModel() {
    private val authsFlow = MutableStateFlow(emptyList<Auth>())
    val auths get() = authsFlow.asStateFlow()

    var isSearch by mutableStateOf(false)
        private set
    private val queryFlow = MutableStateFlow("")

    init {
        Timber.d("HomeViewModel init")
        dataObserver()
        clearTrash()
    }

    private fun dataObserver() {
        viewModelScope.launch {
            dbRepository.getAuthAllAsFlow(enable = true)
                .combineToLatest(queryFlow) { source, key ->
                    authsFlow.update {
                        source.filter {
                            if (key.isNotBlank()) {
                                it.name.contains(key, ignoreCase = true)
                                        || it.issuer.contains(key, ignoreCase = true)
                            } else {
                                true
                            }
                        }.sortedBy {
                            it.issuer.lowercase()
                        }
                    }
                }
        }
    }

    private fun clearTrash() {
        viewModelScope.launch {
            val secrets = dbRepository.getTrashAll(dead = true).map { it.secret }
            dbRepository.deleteAuth(secrets)
            dbRepository.deleteTrash(secrets)
        }
    }

    fun search(key: String) {
        queryFlow.update { key }
    }

    fun openSearch() {
        isSearch = true
    }

    fun closeSearch() {
        isSearch = false
        queryFlow.value = ""
    }

    fun updateAuth(auth: Auth) {
        viewModelScope.launch {
            dbRepository.updateAuth(auth)
        }
    }

    fun recycleAuth(auth: Auth) {
        viewModelScope.launch {
            dbRepository.insertTrash(auth.secret)
        }
    }
}