package de.dasbabypixel.gamelauncher.android.compat

import android.app.Activity
import android.os.Build
import de.dasbabypixel.gamelauncher.android.apilevel.Api11

fun Activity.hideActionBar() {
    ActivityCompat.hideActionBar(this)
}

class ActivityCompat {
    companion object {
        fun hideActionBar(activity: Activity) {
            if (Build.VERSION.SDK_INT >= 11) {
                Api11.hideActionBar(activity)
            }
        }
    }
}