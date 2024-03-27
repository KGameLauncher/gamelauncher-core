package de.dasbabypixel.gamelauncher.android.compat

import android.content.Context
import android.view.View
import de.dasbabypixel.gamelauncher.android.compat.render.gl.GLViewCompat

class ViewCompat {
    companion object {
        fun view(context: Context): View {
            // currently only redirect to gl. May add vulkan later
            return GLViewCompat.glView(context)
        }
    }
}