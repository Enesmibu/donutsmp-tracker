#!/bin/sh

# Gradle wrapper bootstrap script
# Eğer sistem Gradle yoksa, Gradle'ı indirir

GRADLE_VERSION="8.11.1"
GRADLE_DIR="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
GRADLE_ZIP="$HOME/.gradle/wrapper/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_HOME="$GRADLE_DIR/gradle-${GRADLE_VERSION}"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

if command -v gradle > /dev/null 2>&1; then
    exec gradle "$@"
elif [ -f "$GRADLE_BIN" ]; then
    exec "$GRADLE_BIN" "$@"
else
    echo "Gradle bulunamadı, indiriliyor (${GRADLE_VERSION})..."
    mkdir -p "$GRADLE_DIR"
    if command -v curl > /dev/null 2>&1; then
        curl -fsSL -o "$GRADLE_ZIP" "$GRADLE_URL"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$GRADLE_ZIP" "$GRADLE_URL"
    else
        echo "HATA: curl veya wget bulunamadı. Lütfen Gradle'ı manuel olarak kurun: https://gradle.org/install/"
        exit 1
    fi
    unzip -q "$GRADLE_ZIP" -d "$GRADLE_DIR"
    exec "$GRADLE_BIN" "$@"
fi
