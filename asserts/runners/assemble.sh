#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Smali Assembler Runner for Xed-Editor
# Assembles a .smali file or a folder of .smali files into a classes.dex
#
# Environment variables provided by Xed-Editor:
#   $FILE      – absolute path to the currently open file
#   $DIR       – directory containing the file
#   $HOME      – user home directory (Termux/Ubuntu sandbox)
# ─────────────────────────────────────────────────────────────────────────────

set -e

# ── Config ────────────────────────────────────────────────────────────────────
SMALI_JAR="$HOME/smali-tools/smali.jar"
SMALI_VERSION="3.0.9"
SMALI_URL="https://github.com/google/smali/releases/download/${SMALI_VERSION}/smali-${SMALI_VERSION}.jar"

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

# ── Auto-download smali.jar if missing ───────────────────────────────────────
if [ ! -f "$SMALI_JAR" ]; then
    warn "smali.jar not found. Downloading v${SMALI_VERSION}..."
    mkdir -p "$(dirname "$SMALI_JAR")"

    if command -v curl &>/dev/null; then
        curl -L "$SMALI_URL" -o "$SMALI_JAR" --progress-bar \
            || error "Download failed. Check your internet connection."
    elif command -v wget &>/dev/null; then
        wget -q --show-progress "$SMALI_URL" -O "$SMALI_JAR" \
            || error "Download failed. Check your internet connection."
    else
        error "Neither curl nor wget found.\n  Install one: pkg install curl"
    fi

    success "Downloaded smali.jar → $SMALI_JAR"
fi

# ── Resolve input: single file or entire directory? ───────────────────────────
if [ -z "$FILE" ]; then
    error "\$FILE is not set. Make sure Xed-Editor passed the current file path."
fi

# If user opened a single .smali file, use its parent directory so all sibling
# .smali files are assembled together (mirrors typical smali/baksmali usage).
INPUT_DIR="$DIR"

if [ ! -d "$INPUT_DIR" ]; then
    error "Input directory not found: $INPUT_DIR"
fi

# ── Output path ───────────────────────────────────────────────────────────────
OUTPUT_DEX="${INPUT_DIR}/classes.dex"

# ── Banner ────────────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  Smali Assembler                                 ${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
info "Input  : $INPUT_DIR"
info "Output : $OUTPUT_DEX"
echo ""

# ── Count .smali files ────────────────────────────────────────────────────────
SMALI_COUNT=$(find "$INPUT_DIR" -name "*.smali" | wc -l | tr -d ' ')
if [ "$SMALI_COUNT" -eq 0 ]; then
    error "No .smali files found in: $INPUT_DIR"
fi
info "Found $SMALI_COUNT .smali file(s)"

# ── Assemble ──────────────────────────────────────────────────────────────────
echo ""
info "Assembling..."
START_TIME=$(date +%s%N)

java -jar "$SMALI_JAR" assemble \
    --output "$OUTPUT_DEX" \
    "$INPUT_DIR" \
    2>&1 || error "Assembly failed. See errors above."

END_TIME=$(date +%s%N)
ELAPSED=$(( (END_TIME - START_TIME) / 1000000 ))

echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
success "Assembly complete in ${ELAPSED}ms"
success "Output: $OUTPUT_DEX"
info "File size: $(du -sh "$OUTPUT_DEX" | cut -f1)"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo ""
