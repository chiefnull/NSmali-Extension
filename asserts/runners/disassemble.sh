#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Baksmali Disassembler Runner for Xed-Editor
# Disassembles a .dex or .apk file into readable .smali files
#
# Environment variables provided by Xed-Editor:
#   $FILE      – absolute path to the currently open file
#   $DIR       – directory containing the file
#   $HOME      – user home directory (Termux/Ubuntu sandbox)
# ─────────────────────────────────────────────────────────────────────────────

set -e

# ── Config ────────────────────────────────────────────────────────────────────
BAKSMALI_JAR="$HOME/smali-tools/baksmali.jar"
BAKSMALI_VERSION="3.0.9"
BAKSMALI_URL="https://github.com/google/smali/releases/download/${BAKSMALI_VERSION}/baksmali-${BAKSMALI_VERSION}.jar"

# ── Colors ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

info()    { echo -e "${CYAN}[INFO]${RESET}  $*"; }
success() { echo -e "${GREEN}[OK]${RESET}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${RESET}  $*"; }
error()   { echo -e "${RED}[ERR]${RESET}   $*"; exit 1; }

# ── Check Java ────────────────────────────────────────────────────────────────
if ! command -v java &>/dev/null; then
    error "Java not found. Install it via the Xed terminal:\n  pkg install openjdk-17"
fi

JAVA_VER=$(java -version 2>&1 | head -1)
info "Java: $JAVA_VER"

# ── Auto-download baksmali.jar if missing ─────────────────────────────────────
if [ ! -f "$BAKSMALI_JAR" ]; then
    warn "baksmali.jar not found. Downloading v${BAKSMALI_VERSION}..."
    mkdir -p "$(dirname "$BAKSMALI_JAR")"

    if command -v curl &>/dev/null; then
        curl -L "$BAKSMALI_URL" -o "$BAKSMALI_JAR" --progress-bar \
            || error "Download failed. Check your internet connection."
    elif command -v wget &>/dev/null; then
        wget -q --show-progress "$BAKSMALI_URL" -O "$BAKSMALI_JAR" \
            || error "Download failed. Check your internet connection."
    else
        error "Neither curl nor wget found.\n  Install one: pkg install curl"
    fi

    success "Downloaded baksmali.jar → $BAKSMALI_JAR"
fi

# ── Validate input file ───────────────────────────────────────────────────────
if [ -z "$FILE" ]; then
    error "\$FILE is not set. Make sure Xed-Editor passed the current file path."
fi

if [ ! -f "$FILE" ]; then
    error "File not found: $FILE"
fi

EXT="${FILE##*.}"
BASENAME="$(basename "$FILE" ."$EXT")"

# ── For APK: extract classes.dex first ───────────────────────────────────────
TARGET_DEX="$FILE"

if [ "$EXT" = "apk" ]; then
    info "APK detected — extracting classes.dex..."

    if ! command -v unzip &>/dev/null; then
        error "unzip not found. Install it:\n  pkg install unzip"
    fi

    EXTRACT_DIR="$(mktemp -d)"
    unzip -q "$FILE" "classes*.dex" -d "$EXTRACT_DIR" \
        || error "Failed to extract .dex from APK. Is the file a valid APK?"

    DEX_FILES=($(ls "$EXTRACT_DIR"/classes*.dex 2>/dev/null))
    if [ ${#DEX_FILES[@]} -eq 0 ]; then
        error "No classes.dex found inside the APK."
    fi

    info "Found ${#DEX_FILES[@]} dex file(s) inside APK"
    TARGET_DEX="${DEX_FILES[0]}"

    # If multiple dex files, use first and warn
    if [ ${#DEX_FILES[@]} -gt 1 ]; then
        warn "Multiple dex files found. Disassembling: $(basename "$TARGET_DEX")"
        warn "For multidex APKs, run baksmali separately on each classes*.dex"
    fi
fi

# ── Output directory ──────────────────────────────────────────────────────────
OUTPUT_DIR="${DIR}/${BASENAME}-smali"

# ── Banner ────────────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  Baksmali Disassembler                          ${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
info "Input  : $FILE"
info "Dex    : $TARGET_DEX"
info "Output : $OUTPUT_DIR"
echo ""

# ── Clean previous output if exists ──────────────────────────────────────────
if [ -d "$OUTPUT_DIR" ]; then
    warn "Output directory already exists. Overwriting..."
    rm -rf "$OUTPUT_DIR"
fi
mkdir -p "$OUTPUT_DIR"

# ── Disassemble ───────────────────────────────────────────────────────────────
info "Disassembling..."
START_TIME=$(date +%s%N)

java -jar "$BAKSMALI_JAR" disassemble \
    --output "$OUTPUT_DIR" \
    "$TARGET_DEX" \
    2>&1 || error "Disassembly failed. See errors above."

END_TIME=$(date +%s%N)
ELAPSED=$(( (END_TIME - START_TIME) / 1000000 ))

# ── Summary ───────────────────────────────────────────────────────────────────
SMALI_COUNT=$(find "$OUTPUT_DIR" -name "*.smali" | wc -l | tr -d ' ')

echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
success "Disassembly complete in ${ELAPSED}ms"
success "Generated $SMALI_COUNT .smali file(s)"
success "Output folder: $OUTPUT_DIR"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
info "Open the folder in Xed file browser to browse .smali files"
echo ""

# ── Cleanup temp dir (APK mode) ───────────────────────────────────────────────
if [ -n "$EXTRACT_DIR" ] && [ -d "$EXTRACT_DIR" ]; then
    rm -rf "$EXTRACT_DIR"
fi
