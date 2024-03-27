package de.dasbabypixel.gamelauncher.android.apilevel

import android.view.Window
import androidx.annotation.RequiresApi

@RequiresApi(30)
class Api30 {
    companion object {
        fun setDecorFitsSystemWindows(window: Window, decorFitsSystemWindows: Boolean) {
            window.setDecorFitsSystemWindows(decorFitsSystemWindows)
        }
    }
}