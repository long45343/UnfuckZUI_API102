package xyz.long4543.unfuckzzui.core

import android.util.Log
import io.github.libxposed.api.XposedModule

object Logger {
    private const val TAG = "UnfuckZUI102"
    private var moduleRef: XposedModule? = null

    fun init(module: XposedModule) {
        moduleRef = module
    }

    fun d(msg: String) {
        Log.d(TAG, msg)
        moduleRef?.log(Log.DEBUG, TAG, msg)
    }

    fun i(msg: String) {
        Log.i(TAG, msg)
        moduleRef?.log(Log.INFO, TAG, msg)
    }

    fun w(msg: String, throwable: Throwable? = null) {
        Log.w(TAG, msg, throwable)
        if (throwable != null) {
            moduleRef?.log(Log.WARN, TAG, msg, throwable)
        } else {
            moduleRef?.log(Log.WARN, TAG, msg)
        }
    }

    fun e(msg: String, throwable: Throwable? = null) {
        Log.e(TAG, msg, throwable)
        if (throwable != null) {
            moduleRef?.log(Log.ERROR, TAG, msg, throwable)
        } else {
            moduleRef?.log(Log.ERROR, TAG, msg)
        }
    }
}
