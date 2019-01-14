package com.angcyo.replugindemo

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.RePluginCallbacks
import com.qihoo360.replugin.RePluginConfig
import com.qihoo360.replugin.RePluginEventCallbacks

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/01/14
 */
class App : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        val config = RePluginConfig()
        config.callbacks = RePluginCallbacks(this)
        config.eventCallbacks = RePluginEventCallbacks(this)
        config.verifySign = false

        //插件类不存在时读取宿主
        config.isUseHostClassIfNotFound = true

        RePlugin.App.attachBaseContext(this, config)
    }

    override fun onCreate() {
        super.onCreate()

        RePlugin.App.onCreate()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        /* Not need to be called if your application's minSdkVersion > = 14 */
        RePlugin.App.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        /* Not need to be called if your application's minSdkVersion > = 14 */
        RePlugin.App.onTrimMemory(level)
    }

    override fun onConfigurationChanged(config: Configuration) {
        super.onConfigurationChanged(config)

        /* Not need to be called if your application's minSdkVersion > = 14 */
        RePlugin.App.onConfigurationChanged(config)
    }
}