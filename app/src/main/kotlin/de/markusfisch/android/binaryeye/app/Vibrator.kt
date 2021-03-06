package de.markusfisch.android.binaryeye.app

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import de.markusfisch.android.binaryeye.app.prefs

private const val MILLISECONDS = 100L

fun Vibrator.vibrate() {
	if (!prefs.vibrate) {
		return
	}
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
		@Suppress("DEPRECATION")
		this.vibrate(MILLISECONDS)
	} else {
		this.vibrate(
			VibrationEffect.createOneShot(
				MILLISECONDS,
				VibrationEffect.DEFAULT_AMPLITUDE
			)
		)
	}
}
