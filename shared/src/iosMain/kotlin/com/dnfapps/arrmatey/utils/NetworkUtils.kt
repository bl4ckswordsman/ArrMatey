package com.dnfapps.arrmatey.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFArrayGetCount
import platform.CoreFoundation.CFArrayGetValueAtIndex
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFStringRef
import platform.Foundation.NSString
import platform.SystemConfiguration.CNCopyCurrentNetworkInfo
import platform.SystemConfiguration.CNCopySupportedInterfaces
import platform.SystemConfiguration.kCNNetworkInfoKeySSID

class IOSNetworkUtils : NetworkUtils {

    override fun getCurrentWifiSsid(): String? {
        if (!isConnectedToWifi()) return null

        // CFArrayRef of interface names
        val interfaces = CNCopySupportedInterfaces() ?: return null

        @OptIn(ExperimentalForeignApi::class)
        val count = CFArrayGetCount(interfaces)

        @OptIn(ExperimentalForeignApi::class)
        for (i in 0 until count) {
            // CFArrayGetValueAtIndex returns COpaquePointer?
            val interfacePtr = CFArrayGetValueAtIndex(interfaces, i) ?: continue

            // Interpret as CFStringRef
            val interfaceName = interfacePtr as CFStringRef

            val networkInfo = CNCopyCurrentNetworkInfo(interfaceName) ?: continue

            // Dictionary lookup: key is CFStringRef
            val ssidValue = CFDictionaryGetValue(networkInfo, kCNNetworkInfoKeySSID) ?: continue

            // Value is an NSString under the hood
            val ssidNSString = ssidValue as NSString
            return ssidNSString.toString()
        }

        return null
    }

    override fun isConnectedToWifi(): Boolean {
        return getCurrentWifiSsid() != null
    }
}

private var networkUtilsInstance: NetworkUtils? = null

fun initializeNetworkUtils() {
    networkUtilsInstance = IOSNetworkUtils()
}

actual fun getNetworkUtils(): NetworkUtils {
    return networkUtilsInstance ?: IOSNetworkUtils().also { networkUtilsInstance = it }
}

