package com.example.shared.data.repositorys


import com.example.shared.data.local.AppSetting
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.SelectedApartmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Merkt sich die zuletzt gewählte Wohnung über App-Neustarts hinweg. */
class SqlDelightSelectedApartmentRepository(
    private val db: MietCheckDatabase
) : SelectedApartmentRepository {

    private val _selectedId = MutableStateFlow(
        db.appSettingQueries.selectSetting(KEY).executeAsOneOrNull()?.let { ApartmentId(it) }
    )
    override val selectedId: StateFlow<ApartmentId?> = _selectedId.asStateFlow()

    override fun select(id: ApartmentId?) {
        _selectedId.value = id
        if (id == null) db.appSettingQueries.deleteSetting(KEY)
        else db.appSettingQueries.upsertSetting(AppSetting(KEY, id.value))
    }

    private companion object { const val KEY = "selected_apartment_id" }
}