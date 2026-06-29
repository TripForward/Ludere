package com.draco.ludere.input

import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import com.draco.ludere.retroview.RetroView
import com.swordfish.libretrodroid.GLRetroView
import kotlin.math.abs

class ControllerInput {
    companion object {
        /**
         * Combination to open the menu
         */
        val KEYCOMBO_MENU = setOf(
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT
        )

        /**
         * Any of these keys will not be piped to the RetroView
         */
        val EXCLUDED_KEYS = setOf(
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_POWER
        )

        /**
         * Dead zone threshold for analog sticks to filter out drift
         */
        private const val ANALOG_DEAD_ZONE = 0.2f
    }
    /**
     * Set of keys currently being held by the user
     */
    private val keyLog = mutableSetOf<Int>()

    /**
     * The callback for when the user inputs the menu key-combination
     */
    var menuCallback: () -> Unit = {}

    /**
     *  Controller numbers are [1, inf), we need [0, inf)
     */
    private fun getPort(event: InputEvent): Int =
        ((event.device?.controllerNumber ?: 1) - 1).coerceAtLeast(0)

    /**
     * Check if we should be showing the user the menu
     */
    private fun checkMenuKeyCombo() {
        if (keyLog == KEYCOMBO_MENU)
            menuCallback()
    }

    /**
     * Apply dead zone filtering to analog stick values
     */
    private fun applyDeadZone(value: Float): Float {
        return if (abs(value) < ANALOG_DEAD_ZONE) 0f else value
    }

    fun processKeyEvent(keyCode: Int, event: KeyEvent, retroView: RetroView): Boolean? {
        /* Block these keys! */
        if (EXCLUDED_KEYS.contains(keyCode))
            return null

        /* We're not ready yet! */
        if (retroView.frameRendered.value == false)
            return true

        val port = getPort(event)
        retroView.view.sendKeyEvent(event.action, keyCode, port)

        /* Keep track of user input events */
        when (event.action) {
            KeyEvent.ACTION_DOWN -> keyLog.add(keyCode)
            KeyEvent.ACTION_UP -> keyLog.remove(keyCode)
        }

        checkMenuKeyCombo()

        return true
    }

    fun processMotionEvent(event: MotionEvent, retroView: RetroView): Boolean? {
        /* We're not ready yet! */
        if (retroView.frameRendered.value == false)
            return null

        val port = getPort(event)
        retroView.view.apply {
            sendMotionEvent(
                GLRetroView.MOTION_SOURCE_DPAD,
                event.getAxisValue(MotionEvent.AXIS_HAT_X),
                event.getAxisValue(MotionEvent.AXIS_HAT_Y),
                port
            )
            sendMotionEvent(
                GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                applyDeadZone(event.getAxisValue(MotionEvent.AXIS_X)),
                applyDeadZone(event.getAxisValue(MotionEvent.AXIS_Y)),
                port
            )
            sendMotionEvent(
                GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                applyDeadZone(event.getAxisValue(MotionEvent.AXIS_Z)),
                applyDeadZone(event.getAxisValue(MotionEvent.AXIS_RZ)),
                port
            )
        }

        return true
    }
}
