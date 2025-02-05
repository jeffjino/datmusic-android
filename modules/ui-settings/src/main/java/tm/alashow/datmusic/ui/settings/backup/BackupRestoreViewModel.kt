/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.settings.backup

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.util.CreateFileContract
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.base.util.toUiMessage
import tm.alashow.datmusic.data.backup.DatmusicBackupToFile
import tm.alashow.datmusic.data.backup.DatmusicRestoreFromFile
import tm.alashow.datmusic.ui.settings.R
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Success
import tm.alashow.i18n.UiMessage

val BACKUP_FILE_PARAMS = CreateFileContract.Params(suggestedName = "datmusic-backup", fileExtension = "json", fileMimeType = "application/json")

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val backupToFile: DatmusicBackupToFile,
    private val restoreFromFile: DatmusicRestoreFromFile,
    private val snackbarManager: SnackbarManager,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val isBackingUp = MutableStateFlow(false)
    private val isRestoring = MutableStateFlow(false)

    val state = combine(isBackingUp, isRestoring, ::BackupRestoreViewState)
        .stateInDefault(viewModelScope, BackupRestoreViewState.Empty)

    init {
        viewModelScope.launch {
            restoreFromFile.warnings.collect { snackbarManager.addMessage(it.toUiMessage()) }
        }
    }

    fun backupTo(file: Uri) = viewModelScope.launch {
        analytics.event("settings.db.backup", mapOf("uri" to file))
        backupToFile(file).collect {
            isBackingUp.value = it.isLoading
            when (it) {
                is Fail -> snackbarManager.addMessage(it.error.toUiMessage())
                is Success -> snackbarManager.addMessage(UiMessage.Resource(R.string.settings_database_backup_complete))
                else -> Unit
            }
        }
    }

    fun restoreFrom(file: Uri) = viewModelScope.launch {
        analytics.event("settings.db.restore", mapOf("uri" to file))
        restoreFromFile(file).collect {
            isRestoring.value = it.isLoading
            when (it) {
                is Fail -> snackbarManager.addMessage(it.error.toUiMessage())
                is Success -> snackbarManager.addMessage(UiMessage.Resource(R.string.settings_database_restore_complete))
                else -> Unit
            }
        }
    }
}
