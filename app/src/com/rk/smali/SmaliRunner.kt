package com.rk.smali

import com.rk.extension.ExtensionContext
import java.io.File

/**
 * Smali Runner for XED Editor
 *
 * Runs .smali files using a shell-based pipeline:
 *
 *   Mode A — ASSEMBLE + RUN (requires Android device / smali.jar + dx + dalvikvm):
 *     1. smali assemble input.smali -o classes.dex
 *     2. dx --dex --output=out.dex classes.dex   (or d8)
 *     3. dalvikvm -cp out.dex com.ClassName
 *
 *   Mode B — DISASSEMBLE (baksmali, inspection only):
 *     1. baksmali disassemble input.apk -o out/
 *
 *   Mode C — LINT (syntax check only, no execution):
 *     - Tokenize + validate without assembling
 *
 * The runner registers itself with XED's runner system.
 * When the user taps Run on a .smali file, XED calls executeFile().
 */
class SmaliRunner(
    private val context: ExtensionContext,
    private val settings: SmaliSettings
) {

    companion object {
        const val RUNNER_ID   = "smali-runner"
        const val RUNNER_NAME = "Smali Runner"

        // Tool paths — user can override in Settings
        const val DEFAULT_SMALI_JAR   = "/data/local/tmp/smali.jar"
        const val DEFAULT_BAKSMALI_JAR = "/data/local/tmp/baksmali.jar"
        const val DEFAULT_DX_PATH     = "/data/local/tmp/dx"
        const val OUTPUT_DIR          = "/data/local/tmp/smali_out"
    }

    fun register() {
        // XED SDK: RunnerRegistry.register(id = RUNNER_ID, name = RUNNER_NAME, extensions = listOf("smali"), executor = ::executeFile)
        context.logInfo("SmaliPlugin: Runner '$RUNNER_NAME' registered")
    }

    fun unregister() {
        // RunnerRegistry.unregister(RUNNER_ID)
        context.logInfo("SmaliPlugin: Runner unregistered")
    }

    /**
     * Main execution entry — called by XED when user taps Run.
     * @param filePath  absolute path to the .smali file
     * @param outputCallback  streams output lines back to XED's console panel
     */
    fun executeFile(filePath: String, outputCallback: (String) -> Unit) {
        val mode = settings.getString("runner_mode", "lint")

        outputCallback("▶ SmaliPlugin Runner — mode: $mode")
        outputCallback("  file: $filePath")
        outputCallback("")

        when (mode) {
            "lint"       -> runLint(filePath, outputCallback)
            "assemble"   -> runAssemble(filePath, outputCallback)
            "disassemble" -> runDisassemble(filePath, outputCallback)
            else          -> outputCallback("✗ Unknown mode '$mode'. Check SmaliPlugin settings.")
        }
    }

    // ── LINT MODE ────────────────────────────────────────────────────────────
    private fun runLint(filePath: String, out: (String) -> Unit) {
        out("🔍 Linting $filePath ...")
        val file = File(filePath)
        if (!file.exists()) { out("✗ File not found."); return }

        val lines = file.readLines()
        var errors = 0
        var inMethod = false

        lines.forEachIndexed { i, raw ->
            val line = raw.trim()
            when {
                line.startsWith(".method")     -> inMethod = true
                line.startsWith(".end method") -> inMethod = false
                line.startsWith("invoke-") && inMethod -> {
                    if (!line.contains("{") || !line.contains("}")) {
                        out("  ⚠ Line ${i+1}: invoke missing register list — $line")
                        errors++
                    }
                }
                line.startsWith(".registers") -> {
                    val n = line.split("\\s+".toRegex()).getOrNull(1)?.toIntOrNull()
                    if (n == null || n < 1) {
                        out("  ⚠ Line ${i+1}: invalid .registers count")
                        errors++
                    }
                }
            }
        }

        if (errors == 0) out("✔ No issues found (${lines.size} lines)")
        else out("✗ $errors issue(s) found")
    }

    // ── ASSEMBLE MODE ────────────────────────────────────────────────────────
    private fun runAssemble(filePath: String, out: (String) -> Unit) {
        val smaliJar = settings.getString("smali_jar_path", DEFAULT_SMALI_JAR)
        val outDir   = settings.getString("output_dir", OUTPUT_DIR)

        out("🔨 Assembling with smali.jar ...")
        out("   smali.jar : $smaliJar")
        out("   output    : $outDir/classes.dex")
        out("")

        File(outDir).mkdirs()

        val assembleCmd = listOf(
            "java", "-jar", smaliJar,
            "assemble", filePath,
            "-o", "$outDir/classes.dex"
        )

        val result = runShell(assembleCmd, out)
        if (!result) { out("✗ Assembly failed."); return }

        out("✔ Assembly OK → $outDir/classes.dex")
        out("")

        // Optionally run with dalvikvm if on device
        val dalvikEnabled = settings.getBoolean("run_after_assemble", false)
        if (dalvikEnabled) {
            val className = extractClassName(filePath)
            if (className != null) {
                out("🚀 Running: dalvikvm -cp $outDir/classes.dex $className")
                runShell(
                    listOf("dalvikvm", "-cp", "$outDir/classes.dex", className),
                    out
                )
            } else {
                out("⚠ Could not detect main class name — skipping dalvikvm step.")
                out("  Set it manually in SmaliPlugin settings.")
            }
        }
    }

    // ── DISASSEMBLE MODE ─────────────────────────────────────────────────────
    private fun runDisassemble(filePath: String, out: (String) -> Unit) {
        val baksmaliJar = settings.getString("baksmali_jar_path", DEFAULT_BAKSMALI_JAR)
        val outDir      = settings.getString("output_dir", OUTPUT_DIR)

        out("📦 Disassembling with baksmali.jar ...")
        out("   baksmali : $baksmaliJar")
        out("   output   : $outDir/smali/")
        out("")

        File(outDir).mkdirs()

        val cmd = listOf(
            "java", "-jar", baksmaliJar,
            "disassemble", filePath,
            "-o", "$outDir/smali"
        )

        val result = runShell(cmd, out)
        if (result) out("✔ Disassembly complete → $outDir/smali/")
        else        out("✗ Disassembly failed.")
    }

    // ── Shell helper ─────────────────────────────────────────────────────────
    private fun runShell(cmd: List<String>, out: (String) -> Unit): Boolean {
        return try {
            val proc = ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start()

            proc.inputStream.bufferedReader().forEachLine { out("  $it") }
            val exit = proc.waitFor()

            if (exit != 0) out("  ⚠ Process exited with code $exit")
            exit == 0
        } catch (e: Exception) {
            out("  ✗ Shell error: ${e.message}")
            false
        }
    }

    // ── Extract .class name from .smali source ────────────────────────────────
    private fun extractClassName(filePath: String): String? {
        return File(filePath).useLines { lines ->
            lines
                .firstOrNull { it.trim().startsWith(".class") }
                ?.trim()
                ?.split("\\s+".toRegex())
                ?.last()
                ?.removePrefix("L")
                ?.removeSuffix(";")
                ?.replace("/", ".")
        }
    }
}
