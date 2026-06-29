package com.draco.ludere.gamepad

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.InputDevice
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.draco.ludere.R
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.event.Event
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.abs

class GamePad(
    context: Context,
    padConfig: RadialGamePadConfig,
) {
    val pad = RadialGamePad(padConfig, 0f, context)
    private val appContext = context

    companion object {
        private const val TAG = "GamePad"
        
        /**
         * Dead zone threshold for analog sticks to filter out drift
         */
        private const val ANALOG_DEAD_ZONE = 0.2f
        
        /**
         * Should the user see the on-screen controls?
         */
        @Suppress("DEPRECATION")
        fun shouldShowGamePads(activity: Activity): Boolean {
            /* Config says we shouldn't use virtual controls */
            if (!activity.resources.getBoolean(R.bool.config_gamepad))
                return false

            // If the template/auto-generator forces the touch gamepad, show it always
            if (activity.resources.getBoolean(R.bool.config_force_touch_gamepad))
                return true

            /* Devices without a touchscreen don't need a GamePad */
            val hasTouchScreen = activity.packageManager?.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
            if (hasTouchScreen == null || hasTouchScreen == false)
                return false

            /* Fetch the current display that the game is running on */
            val currentDisplayId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                activity.display!!.displayId
            else {
                val wm = activity.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
                wm.defaultDisplay.displayId
            }

            /* Are we presenting this screen on a TV or display? */
            val dm = activity.getSystemService(Service.DISPLAY_SERVICE) as DisplayManager
            if (dm.getDisplay(currentDisplayId).flags and Display.FLAG_PRESENTATION == Display.FLAG_PRESENTATION)
                return false

            /* If a GamePad is connected, we definitely don't need touch controls */
            for (id in InputDevice.getDeviceIds()) {
                InputDevice.getDevice(id).apply {
                    if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD)
                        return false
                }
            }

            return true
        }
    }

    /**
     * Apply dead zone filtering to analog stick values
     */
    private fun applyDeadZone(value: Float): Float {
        return if (abs(value) < ANALOG_DEAD_ZONE) 0f else value
    }

    /**
     * Send inputs to the RetroView
     */
    private fun eventHandler(event: Event, retroView: GLRetroView) {
        Log.d(TAG, "EventHandler received: $event")
        
        when (event) {
            is Event.Button -> {
                Log.d(TAG, "Button Event - Action: ${event.action}, ID: ${event.id}")
                retroView.sendKeyEvent(event.action, event.id)
            }
            is Event.Direction -> {
                val filteredX = applyDeadZone(event.xAxis)
                val filteredY = applyDeadZone(event.yAxis)
                
                Log.d(TAG, "Direction Event - ID: ${event.id}, Raw: (${event.xAxis}, ${event.yAxis}), Filtered: ($filteredX, $filteredY)")
                
                when (event.id) {
                    GLRetroView.MOTION_SOURCE_DPAD -> {
                        Log.d(TAG, "Sending DPAD motion event")
                        retroView.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, filteredX, filteredY)
                    }
                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT -> {
                        Log.d(TAG, "Sending ANALOG_LEFT motion event")
                        retroView.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_LEFT, filteredX, filteredY)
                    }
                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT -> {
                        Log.d(TAG, "Sending ANALOG_RIGHT motion event")
                        retroView.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT, filteredX, filteredY)
                    }
                    else -> Log.w(TAG, "Unknown motion source ID: ${event.id}")
                }
            }
        }
    }

    /**
     * Register input events to the RetroView
     */
    fun subscribe(compositeDisposable: CompositeDisposable, retroView: GLRetroView) {
        Log.d(TAG, "Subscribing touch gamepad to RetroView")
        Toast.makeText(appContext, "GamePad Subscribe Called", Toast.LENGTH_SHORT).show()
        
        try {
            val eventStream = pad.events()
            Log.d(TAG, "Got event stream: $eventStream")
            
            val inputDisposable = eventStream.subscribe(
                { event ->
                    Log.d(TAG, "Pad event fired: $event")
                    Toast.makeText(appContext, "GamePad Event: $event", Toast.LENGTH_SHORT).show()
                    eventHandler(event, retroView)
                },
                { error ->
                    Log.e(TAG, "Error in pad events", error)
                    Toast.makeText(appContext, "GamePad Error: ${error.message}", Toast.LENGTH_SHORT).show()
                },
                {
                    Log.d(TAG, "Pad events completed")
                }
            )
            compositeDisposable.add(inputDisposable)
            Log.d(TAG, "Successfully subscribed to pad events")
        } catch (e: Exception) {
            Log.e(TAG, "Exception while subscribing", e)
            Toast.makeText(appContext, "Subscribe Exception: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
