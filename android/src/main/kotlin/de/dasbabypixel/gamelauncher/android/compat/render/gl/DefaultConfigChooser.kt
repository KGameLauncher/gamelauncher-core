package de.dasbabypixel.gamelauncher.android.compat.render.gl

import android.opengl.EGL14
import android.opengl.EGLExt
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

class DefaultConfigChooser(withDepthBuffer: Boolean) :
    ComponentSizeChooser(8, 8, 8, 0, if (withDepthBuffer) 16 else 0, 0) {
}

class ComponentSizeChooser constructor(
    redSize: Int, greenSize: Int, blueSize: Int,
    alphaSize: Int, depthSize: Int, stencilSize: Int
) : BaseConfigChooser(
    intArrayOf(
        EGL10.EGL_RED_SIZE, redSize,
        EGL10.EGL_GREEN_SIZE, greenSize,
        EGL10.EGL_BLUE_SIZE, blueSize,
        EGL10.EGL_ALPHA_SIZE, alphaSize,
        EGL10.EGL_DEPTH_SIZE, depthSize,
        EGL10.EGL_STENCIL_SIZE, stencilSize,
        EGL10.EGL_NONE
    ), 0
) {

    private var mValue: IntArray

    // Subclasses can adjust these values:
    protected var mRedSize: Int = 0
    protected var mGreenSize: Int = 0
    protected var mBlueSize: Int = 0
    protected var mAlphaSize: Int = 0
    protected var mDepthSize: Int = 0
    protected var mStencilSize: Int = 0

    init {
        mValue = IntArray(1)
        mRedSize = redSize
        mGreenSize = greenSize
        mBlueSize = blueSize
        mAlphaSize = alphaSize
        mDepthSize = depthSize
        mStencilSize = stencilSize
    }

    override fun chooseConfig(
        egl: EGL10, display: EGLDisplay,
        configs: Array<EGLConfig?>
    ): EGLConfig? {
        for (config in configs) {
            val d = findConfigAttrib(
                egl, display, config,
                EGL10.EGL_DEPTH_SIZE, 0
            )
            val s = findConfigAttrib(
                egl, display, config,
                EGL10.EGL_STENCIL_SIZE, 0
            )
            if ((d >= mDepthSize) && (s >= mStencilSize)) {
                val r = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_RED_SIZE, 0
                )
                val g = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_GREEN_SIZE, 0
                )
                val b = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_BLUE_SIZE, 0
                )
                val a = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_ALPHA_SIZE, 0
                )
                if ((r == mRedSize) && (g == mGreenSize)
                    && (b == mBlueSize) && (a == mAlphaSize)
                ) {
                    return config
                }
            }
        }
        return null
    }

    private fun findConfigAttrib(
        egl: EGL10, display: EGLDisplay,
        config: EGLConfig, attribute: Int, defaultValue: Int
    ): Int {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0]
        }
        return defaultValue
    }
}


abstract class BaseConfigChooser(configSpec: IntArray, val eglContextVersion: Int) :
    EGLConfigChooser {
    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        val num_config = IntArray(1)
        require(
            egl.eglChooseConfig(
                display, mConfigSpec, null, 0,
                num_config
            )
        ) { "eglChooseConfig failed" }

        val numConfigs = num_config[0]

        require(numConfigs > 0) { "No configs match configSpec" }

        val configs = arrayOfNulls<EGLConfig>(numConfigs)
        require(
            egl.eglChooseConfig(
                display, mConfigSpec, configs, numConfigs,
                num_config
            )
        ) { "eglChooseConfig#2 failed" }
        val config = chooseConfig(egl, display, configs) ?: throw IllegalArgumentException("No config chosen")
        return config
    }

    abstract fun chooseConfig(
        egl: EGL10, display: EGLDisplay,
        configs: Array<EGLConfig?>
    ): EGLConfig?

    private var mConfigSpec: IntArray

    init {
        mConfigSpec = filterConfigSpec(configSpec)
    }

    private fun filterConfigSpec(configSpec: IntArray): IntArray {
        if (eglContextVersion != 2 && eglContextVersion != 3) {
            return configSpec
        }
        val len = configSpec.size
        val newConfigSpec = IntArray(len + 2)
        System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
        newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE
        if (eglContextVersion == 2) {
            newConfigSpec[len] = EGL14.EGL_OPENGL_ES2_BIT /* EGL_OPENGL_ES2_BIT */
        } else {
            newConfigSpec[len] = EGLExt.EGL_OPENGL_ES3_BIT_KHR /* EGL_OPENGL_ES3_BIT_KHR */
        }
        newConfigSpec[len + 1] = EGL10.EGL_NONE
        return newConfigSpec
    }
}
