package xyz.long4543.unfuckzzui

import android.app.Application
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ZuiApp : Application(), XposedServiceHelper.OnServiceListener {

    companion object {
        private val _serviceFlow = MutableStateFlow<XposedService?>(null)
        val serviceFlow: StateFlow<XposedService?> = _serviceFlow.asStateFlow()

        val currentService: XposedService?
            get() = _serviceFlow.value
    }

    override fun onCreate() {
        super.onCreate()
        try {
            XposedServiceHelper.registerListener(this)
        } catch (_: Throwable) {
        }
    }

    override fun onServiceBind(service: XposedService) {
        _serviceFlow.value = service
    }

    override fun onServiceDied(service: XposedService) {
        _serviceFlow.value = null
    }
}
