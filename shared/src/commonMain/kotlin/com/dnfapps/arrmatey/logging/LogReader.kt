package com.dnfapps.arrmatey.logging

expect object LogReader {
    fun readLogs(): String
    fun clearLogs()
    fun getLogFilePath(): String
    fun shareLogsFile(): String  // Returns file path for sharing
}