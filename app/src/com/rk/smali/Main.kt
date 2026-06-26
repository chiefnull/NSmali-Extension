package com.rk.smali

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Keep
import com.rk.xededitor.plugin.ExtensionAPI
import com.rk.xededitor.plugin.ExtensionContext

@Keep
@Suppress("unused")
class Main(context: ExtensionContext) : ExtensionAPI(context) {

    override fun onExtensionLoaded() {
        // Register the two shell runners for .smali files
        registerRunners()
    }

    // ── Runner registration ───────────────────────────────────────────────────

    private fun registerRunners() {
        // Runner 1: Assemble  (.smali folder / .smali file  →  .dex)
        context.registerRunner(
            id          = "smali_assemble",
            name        = "Smali: Assemble → .dex",
            description = "Assembles a .smali file or folder into a .dex file using smali.jar",
            extensions  = listOf("smali"),
            scriptAsset = "runners/assemble.sh"
        )

        // Runner 2: Disassemble  (.apk / .dex  →  .smali files)
        context.registerRunner(
            id          = "smali_disassemble",
            name        = "Baksmali: Disassemble → smali",
            description = "Disassembles a .dex or .apk file into .smali files using baksmali.jar",
            extensions  = listOf("dex", "apk"),
            scriptAsset = "runners/disassemble.sh"
        )
    }

    // ── Lifecycle (unused but required) ───────────────────────────────────────

    override fun onInstalled()   {}
    override fun onUninstalled() {}
    override fun onUpdated()     {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity)   {}
    override fun onActivityPaused(activity: Activity)    {}
    override fun onActivityDestroyed(activity: Activity) {}
}
