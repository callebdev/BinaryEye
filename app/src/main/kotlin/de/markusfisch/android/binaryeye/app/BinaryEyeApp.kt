package de.markusfisch.android.binaryeye.app

import android.app.Application
import de.markusfisch.android.binaryeye.preference.Preferences
import de.markusfisch.android.binaryeye.repository.DatabaseRepository

val db = DatabaseRepository()
val prefs = Preferences()

class BinaryEyeApp : Application() {
	override fun onCreate() {
		super.onCreate()
		db.open(this)
		prefs.init(this)
	}
}
