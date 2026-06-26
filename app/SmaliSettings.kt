package com.rk.smali

import com.rk.extension.ExtensionContext

/**
 * Persistent settings for SmaliPlugin.
 * Backed by ExtensionContext.settings (key-value storage scoped to this extension).
 *
 * Keys:
 *   runner_mode          → "lint" | "assemble" | "disassemble"   (default: lint)
 *   smali_jar_path       → path to smali.jar                      (default: /data/local/tmp/smali.jar)
 *   baksmali_jar_path    → path to baksmali.jar                   (default: /data/local/tmp/baksmali.jar)
 *   output_dir           → where .dex / disasm output goes        (default: /data/local/tmp/smali_out)
 *   autocomplete_enabled → enable/disable autocomplete            (default: true)
 *   run_after_assemble   → run with dalvikvm after assembly        (default: false)
 *   dalvikvm_class       → override class for dalvikvm            (default: "")
 */
class SmaliSettings(private val context: ExtensionContext) {

    fun setDefaults() {
        if (getString("runner_mode", null) == null) {
            setString("runner_mode", "lint")
            setString("smali_jar_path", SmaliRunner.DEFAULT_SMALI_JAR)
            setString("baksmali_jar_path", SmaliRunner.DEFAULT_BAKSMALI_JAR)
            setString("output_dir", SmaliRunner.OUTPUT_DIR)
            setBoolean("autocomplete_enabled", true)
            setBoolean("run_after_assemble", false)
            setString("dalvikvm_class", "")
            context.logInfo("SmaliPlugin: Default settings applied")
        }
    }

    fun getString(key: String, default: String?): String? =
        runCatching { context.settings.getString(key) }.getOrDefault(default)

    fun setString(key: String, value: String) =
        runCatching { context.settings.putString(key, value) }

    fun getBoolean(key: String, default: Boolean): Boolean =
        runCatching { context.settings.getBoolean(key) }.getOrDefault(default)

    fun setBoolean(key: String, value: Boolean) =
        runCatching { context.settings.putBoolean(key, value) }
}
