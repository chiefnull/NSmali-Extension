package com.rk.smali

import com.rk.extension.ExtensionContext

/**
 * Registers Smali language support into XED:
 *  - File extension binding (.smali)
 *  - TextMate grammar for syntax highlighting
 *  - Autocomplete provider for opcodes, directives, registers, types
 */
class SmaliLanguage(private val context: ExtensionContext) {

    companion object {
        const val LANGUAGE_ID = "smali"
        val FILE_EXTENSIONS = listOf("smali")

        // ── Dalvik Opcodes ─────────────────────────────────────────────────
        val OPCODES = listOf(
            "nop",
            "move", "move/from16", "move/16",
            "move-wide", "move-wide/from16", "move-wide/16",
            "move-object", "move-object/from16", "move-object/16",
            "move-result", "move-result-wide", "move-result-object",
            "move-exception",
            "return-void", "return", "return-wide", "return-object",
            "const/4", "const/16", "const", "const/high16",
            "const-wide/16", "const-wide/32", "const-wide", "const-wide/high16",
            "const-string", "const-string/jumbo", "const-class",
            "monitor-enter", "monitor-exit",
            "check-cast", "instance-of", "array-length",
            "new-instance", "new-array", "filled-new-array", "filled-new-array/range",
            "fill-array-data",
            "throw", "goto", "goto/16", "goto/32",
            "packed-switch", "sparse-switch",
            "cmpl-float", "cmpg-float", "cmpl-double", "cmpg-double", "cmp-long",
            "if-eq", "if-ne", "if-lt", "if-ge", "if-gt", "if-le",
            "if-eqz", "if-nez", "if-ltz", "if-gez", "if-gtz", "if-lez",
            "aget", "aget-wide", "aget-object", "aget-boolean", "aget-byte",
            "aget-char", "aget-short",
            "aput", "aput-wide", "aput-object", "aput-boolean", "aput-byte",
            "aput-char", "aput-short",
            "iget", "iget-wide", "iget-object", "iget-boolean", "iget-byte",
            "iget-char", "iget-short",
            "iput", "iput-wide", "iput-object", "iput-boolean", "iput-byte",
            "iput-char", "iput-short",
            "sget", "sget-wide", "sget-object", "sget-boolean", "sget-byte",
            "sget-char", "sget-short",
            "sput", "sput-wide", "sput-object", "sput-boolean", "sput-byte",
            "sput-char", "sput-short",
            "invoke-virtual", "invoke-super", "invoke-direct",
            "invoke-static", "invoke-interface",
            "invoke-virtual/range", "invoke-super/range", "invoke-direct/range",
            "invoke-static/range", "invoke-interface/range",
            "neg-int", "not-int", "neg-long", "not-long",
            "neg-float", "neg-double",
            "int-to-long", "int-to-float", "int-to-double",
            "long-to-int", "long-to-float", "long-to-double",
            "float-to-int", "float-to-long", "float-to-double",
            "double-to-int", "double-to-long", "double-to-float",
            "int-to-byte", "int-to-char", "int-to-short",
            "add-int", "sub-int", "mul-int", "div-int", "rem-int",
            "and-int", "or-int", "xor-int", "shl-int", "shr-int", "ushr-int",
            "add-long", "sub-long", "mul-long", "div-long", "rem-long",
            "and-long", "or-long", "xor-long", "shl-long", "shr-long", "ushr-long",
            "add-float", "sub-float", "mul-float", "div-float", "rem-float",
            "add-double", "sub-double", "mul-double", "div-double", "rem-double",
            "add-int/2addr", "sub-int/2addr", "mul-int/2addr", "div-int/2addr",
            "rem-int/2addr", "and-int/2addr", "or-int/2addr", "xor-int/2addr",
            "shl-int/2addr", "shr-int/2addr", "ushr-int/2addr",
            "add-int/lit16", "rsub-int", "mul-int/lit16", "div-int/lit16",
            "rem-int/lit16", "and-int/lit16", "or-int/lit16", "xor-int/lit16",
            "add-int/lit8", "rsub-int/lit8", "mul-int/lit8", "div-int/lit8",
            "rem-int/lit8", "and-int/lit8", "or-int/lit8", "xor-int/lit8",
            "shl-int/lit8", "shr-int/lit8", "ushr-int/lit8"
        )

        // ── Smali Directives ───────────────────────────────────────────────
        val DIRECTIVES = listOf(
            ".class", ".super", ".source", ".implements",
            ".field", ".method", ".end method",
            ".registers", ".locals", ".prologue", ".epilogue",
            ".annotation", ".end annotation",
            ".param", ".line",
            ".restart local", ".end local",
            ".catch", ".catchall",
            ".packed-switch", ".end packed-switch",
            ".sparse-switch", ".end sparse-switch",
            ".array-data", ".end array-data",
            ".annotation system", ".annotation build", ".annotation runtime"
        )

        // ── Access Modifiers / Keywords ────────────────────────────────────
        val KEYWORDS = listOf(
            "public", "private", "protected", "static", "final",
            "abstract", "synthetic", "constructor", "interface",
            "enum", "annotation", "bridge", "varargs",
            "declared-synchronized", "system", "build", "runtime",
            "true", "false", "null"
        )

        // ── Common Android/Java Types ──────────────────────────────────────
        val COMMON_TYPES = listOf(
            "Ljava/lang/String;",
            "Ljava/lang/Object;",
            "Ljava/lang/StringBuilder;",
            "Ljava/lang/Integer;",
            "Ljava/lang/Boolean;",
            "Ljava/lang/System;",
            "Ljava/lang/Math;",
            "Ljava/lang/Thread;",
            "Ljava/lang/Runnable;",
            "Ljava/lang/Exception;",
            "Ljava/lang/RuntimeException;",
            "Ljava/lang/NullPointerException;",
            "Ljava/io/PrintStream;",
            "Ljava/io/File;",
            "Ljava/util/ArrayList;",
            "Ljava/util/HashMap;",
            "Ljava/util/List;",
            "Ljava/util/Map;",
            "Landroid/util/Log;",
            "Landroid/content/Context;",
            "Landroid/content/Intent;",
            "Landroid/app/Activity;",
            "Landroid/app/Application;",
            "Landroid/os/Bundle;",
            "Landroid/os/Handler;",
            "Landroid/widget/Toast;",
            "Landroid/widget/TextView;",
            "Landroid/view/View;"
        )

        // ── Register names ─────────────────────────────────────────────────
        val REGISTERS = (0..15).flatMap { listOf("v$it", "p$it") }

        // ── All completions merged ─────────────────────────────────────────
        val ALL_COMPLETIONS: List<CompletionItem> by lazy {
            OPCODES.map { CompletionItem(it, "opcode", "Dalvik opcode") } +
            DIRECTIVES.map { CompletionItem(it, "directive", "Smali directive") } +
            KEYWORDS.map { CompletionItem(it, "keyword", "Access modifier") } +
            COMMON_TYPES.map { CompletionItem(it, "type", "Java/Android type") } +
            REGISTERS.map { r ->
                val desc = when {
                    r == "p0" -> "this reference"
                    r.startsWith("p") -> "parameter register"
                    else -> "local register"
                }
                CompletionItem(r, "register", desc)
            }
        }
    }

    data class CompletionItem(
        val label: String,
        val kind: String,   // opcode | directive | keyword | type | register
        val detail: String
    )

    fun register() {
        // XED Extension SDK hooks for language registration
        // These are called via reflection by the host app.
        // When the SDK jar is present, replace with actual API calls:
        //
        //   LanguageRegistry.register(
        //       id        = LANGUAGE_ID,
        //       extensions = FILE_EXTENSIONS,
        //       grammar   = loadGrammar(),
        //       completionProvider = ::getCompletions
        //   )
        //
        context.logInfo("SmaliPlugin: Language '$LANGUAGE_ID' registered for ${FILE_EXTENSIONS.joinToString()}")
    }

    fun unregister() {
        // LanguageRegistry.unregister(LANGUAGE_ID)
        context.logInfo("SmaliPlugin: Language unregistered")
    }

    /**
     * Returns autocomplete items for the given partial word.
     * Called by the host editor on each keystroke.
     */
    fun getCompletions(prefix: String): List<CompletionItem> {
        if (prefix.length < 1) return emptyList()
        val lower = prefix.lowercase()
        return ALL_COMPLETIONS
            .filter { it.label.lowercase().startsWith(lower) }
            .sortedWith(compareBy(
                // directives and opcodes first, then the rest
                { if (it.kind == "opcode" || it.kind == "directive") 0 else 1 },
                { it.label }
            ))
            .take(12)
    }

    /**
     * Returns the TextMate-style grammar JSON for Smali.
     * Loaded from assets/smali.tmLanguage.json at runtime.
     */
    private fun loadGrammar(): String {
        return try {
            context.assets.open("smali.tmLanguage.json")
                .bufferedReader()
                .readText()
        } catch (e: Exception) {
            context.logError("SmaliPlugin: Failed to load grammar — ${e.message}")
            "{}"
        }
    }
}
