package com.rk.smali

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Keep
import com.rk.extension.ExtensionAPI
import com.rk.extension.ExtensionContext

/**
 * Smali Plugin for XED Editor
 * Entry class — must match manifest.json "mainClass"
 *
 * Features:
 *  - Smali syntax highlighting via TextMate grammar
 *  - Autocomplete for opcodes, directives, registers, types
 *  - Shell-based Smali runner (baksmali + smali toolchain)
 *  - Settings: toggle autocomplete, choose runner mode
 */
@Keep
@Suppress("unused")
class Main(context: ExtensionContext) : ExtensionAPI(context) {

    private lateinit var smaliLanguage: SmaliLanguage
    private lateinit var smaliRunner: SmaliRunner
    private lateinit var smaliSettings: SmaliSettings

    override fun onExtensionLoaded() {
        context.logInfo("SmaliPlugin: Loading...")

        smaliSettings = SmaliSettings(context)
        smaliLanguage = SmaliLanguage(context)
        smaliRunner = SmaliRunner(context, smaliSettings)

        // Register .smali file association → syntax highlighting
        smaliLanguage.register()

        // Register runner for .smali files
        smaliRunner.register()

        context.logInfo("SmaliPlugin: Loaded OK — syntax + autocomplete + runner active")
    }

    override fun onInstalled() {
        context.logInfo("SmaliPlugin: Installed for the first time")
        smaliSettings.setDefaults()
    }

    override fun onUpdated() {
        context.logInfo("SmaliPlugin: Updated to v1.0.0")
    }

    override fun onUninstalled() {
        context.logInfo("SmaliPlugin: Uninstalling, cleaning up...")
        smaliLanguage.unregister()
        smaliRunner.unregister()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
