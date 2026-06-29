package com.draco.ludere.gamepad

import android.app.Activity
import android.view.View
import com.draco.ludere.gamepad.GamePadConfig

/**
 * GamePad class for handling virtual gamepad interactions
 */
object GamePad {
    /**
     * Determine if we should show the virtual game pads
     */
    @Suppress("DEPRECATION")
    fun shouldShowGamePads(activity: Activity): Boolean {
        /* Config says we shouldn't use virtual controls */
        if (!activity.resources.getBoolean(R.bool.config_gamepad))
            return false

        // If the template/auto-generator forces the touch gamepad, show it always
        if (activity.resources.getBoolean(R.bool.config_force_touch_gamepad))
            return true

        return true
    }
}
