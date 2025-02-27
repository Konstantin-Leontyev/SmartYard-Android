package com.sesameware.smartyard_oem.ui.main.burger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sesameware.data.DataModule
import com.sesameware.domain.interactors.ExtInteractor
import com.sesameware.domain.interactors.GeoInteractor
import com.sesameware.domain.interactors.IssueInteractor
import com.sesameware.domain.interactors.SipInteractor
import com.sesameware.domain.model.request.CreateIssuesRequest
import com.sesameware.domain.model.request.ExtRequest
import com.sesameware.smartyard_oem.ui.main.BaseIssueViewModel
import com.sesameware.smartyard_oem.Event
import com.sesameware.smartyard_oem.R

class BurgerViewModel(
    private val sipInteractor: SipInteractor,
    geoInteractor: GeoInteractor,
    issueInteractor: IssueInteractor,
    private val extInteractor: ExtInteractor
) : BaseIssueViewModel(geoInteractor, issueInteractor) {

    private val _dialNumber = MutableLiveData<String>()
    val dialNumber: LiveData<String>
        get() = _dialNumber

    var chosenSupportOption = MutableLiveData<SupportOption>()

    private val _burgerList = MutableLiveData<List<BurgerModel>>()
    val burgerList: LiveData<List<BurgerModel>>
      get() = _burgerList

    private val _navigateToFragment = MutableLiveData<Event<Int>>()
    val navigateToFragment: LiveData<Event<Int>>
        get() = _navigateToFragment

    data class WebExtension(var basePath: String, var code: String)
    private val _navigateToWebView = MutableLiveData<Event<WebExtension>>()
    val navigateToWebView: LiveData<Event<WebExtension>>
        get() = _navigateToWebView

    init {
        getBurgerMenu()
    }

    fun getHelpMe() {
        _dialNumber.value = DataModule.providerConfig.supportPhone ?: ""
    }

    fun createIssue() {
        val summary = "Авто: Звонок с приложения"
        val description = "Выполнить звонок клиенту по запросу с приложения"
        val x10011 = "-3"
        val x12440 = "Приложение"
        super.createIssue(
            summary,
            description,
            null,
            CreateIssuesRequest.CustomFields(x10011 = x10011, x12440 = x12440),
            CreateIssuesRequest.TypeAction.ACTION1
        )
    }

    private fun getBurgerMenu() {
        viewModelScope.withProgress({false}) {
            val list = mutableListOf(
                BurgerModel(
                    orderId = 200,
                    iconId = R.drawable.address_settings_burger,
                    titleId = R.string.burger_address_settings_title,
                    onClick = {
                        _navigateToFragment.value = Event(R.id.action_burgerFragment_to_settingsFragment)
                    }
                ),
                BurgerModel(
                    orderId = 300,
                    iconId = R.drawable.common_settings_burger,
                    titleId = R.string.burger_common_settings_title,
                    onClick = {
                        _navigateToFragment.value = Event(R.id.action_burgerFragment_to_basicSettingsFragment)
                    }
                ),
            )
            if (DataModule.providerConfig.hasCityCams) {
                list.add(
                    BurgerModel(
                        orderId = 100,
                        iconId = R.drawable.city_camera_burger,
                        titleId = R.string.burger_city_cameras_title,
                        onClick = {
                            _navigateToFragment.value =
                                Event(R.id.action_burgerFragment_to_cityCamerasFragment)
                        }
                    )
                )
            }

            try {
                //загружаем расширения с использованием API методов
                extInteractor.list()?.let { extList ->
                    extList.data.forEach { item ->
                        if (item.extId != null && item.order != null && item.caption != null) {
                            list.add(
                                BurgerModel(
                                    orderId = item.order!!,
                                    iconUrl = item.icon,
                                    title = item.caption!!,
                                    onClick = {
                                        viewModelScope.withProgress({false}) {
                                            extInteractor.ext(ExtRequest(item.extId!!))?.let {
                                                if (it.data.basePath != null && it.data.code != null) {
                                                    _navigateToWebView.value = Event(WebExtension(it.data.basePath!!, it.data.code!!))
                                                }
                                            }
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {

            }

            list.sortWith(compareBy {
                it.orderId
            })
            _burgerList.value = list
        }
    }

    enum class SupportOption {
        CALL_TO_SUPPORT_BY_PHONE, ORDER_CALLBACK
    }
}
