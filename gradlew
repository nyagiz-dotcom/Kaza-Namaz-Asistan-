#!/usr/bin/env sh
DIR=$(dirname "$0")
APP_BASE_NAME=$(basename "$0")
exec "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
