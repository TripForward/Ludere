*** Begin Patch
*** Update File: app/src/main/java/com/draco/ludere/gamepad/GamePad.kt
@@
         @Suppress("DEPRECATION")
         fun shouldShowGamePads(activity: Activity): Boolean {
-            /* Config says we shouldn't use virtual controls */
-            if (!activity.resources.getBoolean(R.bool.config_gamepad))
-                return false
+            /* Config says we shouldn't use virtual controls */
+            if (!activity.resources.getBoolean(R.bool.config_gamepad))
+                return false
+
+            // If the template/auto-generator forces the touch gamepad, show it always
+            if (activity.resources.getBoolean(R.bool.config_force_touch_gamepad))
+                return true
*** End Patch
