package de.markusfisch.android.binaryeye.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.zxing.BarcodeFormat
import de.markusfisch.android.binaryeye.R
import de.markusfisch.android.binaryeye.app.colorSystemAndToolBars
import de.markusfisch.android.binaryeye.app.initSystemBars
import de.markusfisch.android.binaryeye.app.setFragment
import de.markusfisch.android.binaryeye.app.setupInsets
import de.markusfisch.android.binaryeye.fragment.DecodeFragment
import de.markusfisch.android.binaryeye.fragment.EncodeFragment
import de.markusfisch.android.binaryeye.fragment.HistoryFragment
import de.markusfisch.android.binaryeye.fragment.PreferencesFragment
import de.markusfisch.android.binaryeye.repository.Scan

class MainActivity : AppCompatActivity() {
	override fun onSupportNavigateUp(): Boolean {
		val fm = supportFragmentManager
		if (fm != null && fm.backStackEntryCount > 0) {
			fm.popBackStack()
		} else {
			finish()
		}
		return true
	}

	override fun onCreate(state: Bundle?) {
		super.onCreate(state)
		setContentView(R.layout.activity_main)

		initSystemBars(this)
		val toolbar = findViewById(R.id.toolbar) as Toolbar
		setupInsets(findViewById(android.R.id.content), toolbar)
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		supportFragmentManager.addOnBackStackChangedListener {
			colorSystemAndToolBars(this@MainActivity)
		}

		if (state == null) {
			setFragment(
				supportFragmentManager, when {
					intent?.hasExtra(PREFERENCES) == true ->
						PreferencesFragment()
					intent?.hasExtra(HISTORY) == true ->
						HistoryFragment()
					intent?.hasExtra(ENCODE) == true ->
						EncodeFragment.newInstance(
							intent.getStringExtra(ENCODE) ?: ""
						)
					intent?.hasExtra(DECODED) == true ->
						DecodeFragment.newInstance(
							intent.getParcelableExtra(DECODED)
						)
					else -> PreferencesFragment()
				}
			)
		}
	}

	companion object {
		private const val PREFERENCES = "preferences"
		private const val HISTORY = "history"
		private const val ENCODE = "encode"
		private const val DECODED = "decoded"

		fun getPreferencesIntent(context: Context): Intent {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(PREFERENCES, true)
			return intent
		}

		fun getHistoryIntent(context: Context): Intent {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(HISTORY, true)
			return intent
		}

		fun getEncodeIntent(
			context: Context,
			text: String? = "",
			isExternal: Boolean = false
		): Intent {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(ENCODE, text)
			if (isExternal) {
				intent.addFlags(
					Intent.FLAG_ACTIVITY_NO_HISTORY or
							Intent.FLAG_ACTIVITY_CLEAR_TASK or
							Intent.FLAG_ACTIVITY_NEW_TASK
				)
			}
			return intent
		}

		fun getDecodeIntent(context: Context, scan: Scan): Intent {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra(DECODED, scan)
			return intent
		}
	}
}
