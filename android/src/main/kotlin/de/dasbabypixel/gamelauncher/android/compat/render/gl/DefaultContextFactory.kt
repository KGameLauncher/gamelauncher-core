package de.dasbabypixel.gamelauncher.android.compat.render.gl

import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class DefaultContextFactory : EGLContextFactory {
    override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext {
        return egl.eglCreateContext(
            display, config, EGL10.EGL_NO_CONTEXT, null
        )
    }

    override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) {
        if (!egl.eglDestroyContext(display, context)) {
            Log.e("DefaultContextFactory", "display:$display context: $context")
            Log.e("DefaultContextFactory", "tid=" + Thread.currentThread().id)
            EGLHelper.throwEglException("eglDestroyContex", egl.eglGetError())
        }
    }
}