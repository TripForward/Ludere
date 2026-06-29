package com.draco.ludere.gamepad

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.draco.ludere.R
import com.swordfish.radialgamepad.library.config.*
import com.swordfish.radialgamepad.library.haptics.HapticConfig

class GamePadConfig(
    context: Context,
    private val resources: Resources
) {
    companion object {
        val BUTTON_START = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_START,
            label = "+"
        )

        val BUTTON_SELECT = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_SELECT,
            label = "-"
        )

        val BUTTON_L1 = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_L1,
            label = "L"
        )

        val BUTTON_R1 = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_R1,
            label = "R"
        )

        val BUTTON_A = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_A,
            label = "A"
        )

        val BUTTON_B = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_B,
            label = "B"
        )

        val BUTTON_X = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_X,
            label = "X"
        )

        val BUTTON_Y = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_Y,
            label = "Y"
        )

        // C-buttons mapping for on-screen layout (use available keycodes)
        val BUTTON_C_UP = ButtonConfig(id = KeyEvent.KEYCODE_BUTTON_THUMBR, label = "CU")
        val BUTTON_C_DOWN = ButtonConfig(id = KeyEvent.KEYCODE_BUTTON_THUMBL, label = "CD")
        val BUTTON_C_LEFT = ButtonConfig(id = KeyEvent.KEYCODE_BUTTON_L2, label = "CL")
        val BUTTON_C_RIGHT = ButtonConfig(id = KeyEvent.KEYCODE_BUTTON_R2, label = "CR")

        val LEFT_DPAD = PrimaryDialConfig.Cross(CrossConfig(0))
        val LEFT_ANALOG = PrimaryDialConfig.Stick(0)
    }

    private val radialGamePadTheme = RadialGamePadTheme(
        primaryDialBackground = Color.TRANSPARENT,
        textColor = ContextCompat.getColor(context, R.color.gamepad_icon_color),
        normalColor = ContextCompat.getColor(context, R.color.gamepad_button_color),
        pressedColor = ContextCompat.getColor(context, R.color.gamepad_pressed_color)
    )

    val left = RadialGamePadConfig(
        haptic = if (resources.getBoolean(R.bool.config_gamepad_haptic)) HapticConfig.PRESS else HapticConfig.OFF,
        theme = radialGamePadTheme,
        sockets = 12,
        primaryDial = if (resources.getBoolean(R.bool.config_left_analog)) LEFT_ANALOG else LEFT_DPAD,
        secondaryDials = listOfNotNull(
            SecondaryDialConfig.SingleButton(4, 1, BUTTON_L1).takeIf { resources.getBoolean(R.bool.config_gamepad_l1) },
            SecondaryDialConfig.SingleButton(10, 1, BUTTON_SELECT).takeIf { resources.getBoolean(R.bool.config_gamepad_select) },
        )
    )

    val right = RadialGamePadConfig(
        haptic = if (resources.getBoolean(R.bool.config_gamepad_haptic)) HapticConfig.PRESS else HapticConfig.OFF,
        theme = radialGamePadTheme,
        sockets = 12,
        primaryDial = PrimaryDialConfig.PrimaryButtons(
            dials = listOfNotNull(
                BUTTON_A.takeIf { resources.getBoolean(R.bool.config_gamepad_a) },
                BUTTON_X.takeIf { resources.getBoolean(R.bool.config_gamepad_x) },
                BUTTON_Y.takeIf { resources.getBoolean(R.bool.config_gamepad_y) },
                BUTTON_B.takeIf { resources.getBoolean(R.bool.config_gamepad_b) }
            )
        ),
        secondaryDials = listOfNotNull(
            // R1 / shoulders
            SecondaryDialConfig.SingleButton(2, 1, BUTTON_R1).takeIf { resources.getBoolean(R.bool.config_gamepad_r1) },
            // Map C-buttons to visible secondary sockets (tweak socket numbers to adjust on-screen position)
            SecondaryDialConfig.SingleButton(3, 1, BUTTON_C_UP).takeIf { resources.getBoolean(R.bool.config_gamepad_c_up) },
            SecondaryDialConfig.SingleButton(5, 1, BUTTON_C_DOWN).takeIf { resources.getBoolean(R.bool.config_gamepad_c_down) },
            SecondaryDialConfig.SingleButton(7, 1, BUTTON_C_LEFT).takeIf { resources.getBoolean(R.bool.config_gamepad_c_left) },
            SecondaryDialConfig.SingleButton(9, 1, BUTTON_C_RIGHT).takeIf { resources.getBoolean(R.bool.config_gamepad_c_right) },
            // Start button
            SecondaryDialConfig.SingleButton(8, 1, BUTTON_START).takeIf { resources.getBoolean(R.bool.config_gamepad_start) },
        )
    )
}
