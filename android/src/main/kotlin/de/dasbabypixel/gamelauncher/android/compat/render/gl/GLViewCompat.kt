package de.dasbabypixel.gamelauncher.android.compat.render.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.Log
import android.view.View
import de.dasbabypixel.gamelauncher.android.MyGLSurfaceView
import javax.microedition.khronos.egl.*

class GLViewCompat {
    companion object {
        fun glView(context: Context): View {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                return MyGLSurfaceView(context);
            }

            return GLSurfaceView1(context);
        }
    }
}

interface EGLConfigChooser {
    fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig
}

interface EGLContextFactory {
    fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext
    fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext)
}

class EGLHelper(val contextFactory: EGLContextFactory, val configChooser: EGLConfigChooser) {
    val egl = EGLContext.getEGL() as EGL10
    val eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
    val eglConfig: EGLConfig
    val eglContext: EGLContext

    init {
        val version = IntArray(2)
        if (!egl.eglInitialize(eglDisplay, version)) {
            throwEglException("eglInitialize failed")
        }
        eglConfig = configChooser.chooseConfig(egl, eglDisplay)
        eglContext = contextFactory.createContext(egl, eglDisplay, eglConfig)
        if (eglContext == EGL10.EGL_NO_CONTEXT) {
            throwEglException("createContext")
        }
    }

    fun throwEglException(function: String) {
        throwEglException(function, egl.eglGetError())
    }

    companion object {
        fun throwEglException(function: String, error: Int) {
            val message: String = formatEglError(function, error)
            Log.e(
                "EglHelper", "throwEglException tid=" + Thread.currentThread().id + " " + message
            )
            throw RuntimeException(message)
        }

        fun formatEglError(function: String, error: Int): String {
            return function + " failed: " + getErrorString(error)
        }

        fun getErrorString(error: Int): String {
            return when (error) {
                EGL10.EGL_SUCCESS -> "EGL_SUCCESS"
                EGL10.EGL_NOT_INITIALIZED -> "EGL_NOT_INITIALIZED"
                EGL10.EGL_BAD_ACCESS -> "EGL_BAD_ACCESS"
                EGL10.EGL_BAD_ALLOC -> "EGL_BAD_ALLOC"
                EGL10.EGL_BAD_ATTRIBUTE -> "EGL_BAD_ATTRIBUTE"
                EGL10.EGL_BAD_CONFIG -> "EGL_BAD_CONFIG"
                EGL10.EGL_BAD_CONTEXT -> "EGL_BAD_CONTEXT"
                EGL10.EGL_BAD_CURRENT_SURFACE -> "EGL_BAD_CURRENT_SURFACE"
                EGL10.EGL_BAD_DISPLAY -> "EGL_BAD_DISPLAY"
                EGL10.EGL_BAD_MATCH -> "EGL_BAD_MATCH"
                EGL10.EGL_BAD_NATIVE_PIXMAP -> "EGL_BAD_NATIVE_PIXMAP"
                EGL10.EGL_BAD_NATIVE_WINDOW -> "EGL_BAD_NATIVE_WINDOW"
                EGL10.EGL_BAD_PARAMETER -> "EGL_BAD_PARAMETER"
                EGL10.EGL_BAD_SURFACE -> "EGL_BAD_SURFACE"
                EGL11.EGL_CONTEXT_LOST -> "EGL_CONTEXT_LOST"
                else -> getHex(error)
            }
        }

        private fun getHex(value: Int): String {
            return "0x" + Integer.toHexString(value)
        }
    }

}