package com.rk.smali

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Keep

@Keep
@Suppress("unused")
class Main(context: ExtensionContext) : ExtensionAPI(context) {

    override fun onExtensionLoaded() {}

    override fun onInstalled() {}

    override fun onUninstalled() {}

    override fun onUpdated() {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
