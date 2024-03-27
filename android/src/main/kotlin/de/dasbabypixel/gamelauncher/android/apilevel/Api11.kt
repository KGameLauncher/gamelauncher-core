package de.dasbabypixel.gamelauncher.android.apilevel

import android.app.Activity
import androidx.annotation.RequiresApi

@RequiresApi(11)
class Api11 {
    companion object {
        fun hideActionBar(activity: Activity) {
            activity.actionBar?.hide()
        }
    }
}