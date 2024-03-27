package de.dasbabypixel.gamelauncher.android.apilevel

import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi

@RequiresApi(16)
class Api16 {
    companion object {
        fun setDecorFitsSystemWindows(window: Window, decorFitsSystemWindows: Boolean) {
            val decorFitsFlags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            val decorView = window.decorView
            val sysUiVis = decorView.systemUiVisibility
            decorView.systemUiVisibility = if (decorFitsSystemWindows
            ) sysUiVis and decorFitsFlags.inv()
            else sysUiVis or decorFitsFlags
        }
    }
}