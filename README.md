# <img src="assets/smali-icon.png" width="32"/> Smali Support — Xed-Editor Plugin

> Smali/Baksmali support for [Xed-Editor](https://github.com/Xed-Editor/Xed-Editor) on Android.

---

## Features

- 🎨 **Syntax highlighting** for `.smali` files
- ⚙️ **Runner 1 — Assemble**: converts `.smali` → `classes.dex`
- 🔍 **Runner 2 — Disassemble**: converts `.apk` or `.dex` → `.smali` files
- 📦 Auto-downloads `smali.jar` & `baksmali.jar` on first use

---

## Installation

1. Download the latest `SmaliSupport.zip` from [Releases](../../releases)
2. Transfer it to your Android device
3. Open **Xed-Editor** → Extensions → **Install from storage**
4. Select `SmaliSupport.zip`
5. Restart Xed-Editor

---

## First Time Setup

Open the **Xed terminal** and run:

```bash
pkg install openjdk-17 curl
```

`smali.jar` and `baksmali.jar` will be downloaded automatically to `~/smali-tools/` the first time you run a runner.

---

## Usage

### Assemble (.smali → .dex)

1. Open any `.smali` file in Xed-Editor
2. Tap **▶ Run**
3. Select **"Smali: Assemble → .dex"**
4. `classes.dex` is saved in the same folder

### Disassemble (.apk / .dex → smali)

1. Open a `.apk` or `.dex` file in Xed-Editor
2. Tap **▶ Run**
3. Select **"Baksmali: Disassemble → smali"**
4. A folder `<filename>-smali/` is created with all `.smali` files

---

## Project Structure

```
smali-plugin/
├── manifest.json                  ← Plugin metadata
├── assets/
│   ├── smali-icon.png             ← Plugin icon
│   ├── smali.tmLanguage.json      ← Syntax highlighting grammar
│   └── runners/
│       ├── assemble.sh            ← Smali assembler runner
│       └── disassemble.sh         ← Baksmali disassembler runner
└── src/
    └── com/rk/smali/
        └── Main.kt                ← Extension entry point
```

---

## Building from Source

### Requirements

- Android Studio
- JDK 17+
- Git

### Steps

```bash
git clone https://github.com/chiefnull/xed-smali-plugin
cd xed-smali-plugin
./compileDebug
# output: output/SmaliSupport.zip
```

---

## Tools Used

| Tool | Version | Source |
|------|---------|--------|
| smali | 3.0.9 | [google/smali](https://github.com/google/smali) |
| baksmali | 3.0.9 | [google/smali](https://github.com/google/smali) |

---

## License

MIT License — see [LICENSE](LICENSE)

---

## Author

**null**
GitHub: [@chiefnull](https://github.com/chiefnull)
