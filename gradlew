#!/bin/bash
export ANDROID_HOME=/data/data/com.termux/files/usr/share/android-sdk
export JAVA_HOME=/data/data/com.termux/files/usr
exec java -jar gradle/wrapper/gradle-wrapper.jar "$@"
