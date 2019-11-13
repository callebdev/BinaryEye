package de.markusfisch.android.binaryeye.fragment

import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import de.markusfisch.android.binaryeye.R
import de.markusfisch.android.binaryeye.app.prefs

class PreferencesFragment : androidx.fragment.app.Fragment() {
	private lateinit var useHistorySwitch: SwitchCompat
	private lateinit var openWithUrlInput: EditText

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		state: Bundle?
	): View? {
		activity?.setTitle(R.string.preferences)

		val view = inflater.inflate(
			R.layout.fragment_preferences,
			container,
			false
		)

		useHistorySwitch = view.findViewById(R.id.use_history)
		if (prefs.useHistory) {
			useHistorySwitch.toggle()
		}

		openWithUrlInput = view.findViewById(R.id.open_with_url)
		openWithUrlInput.setText(prefs.openWithUrl)

		return view
	}

	override fun onPause() {
		super.onPause()
		prefs.useHistory = useHistorySwitch.isChecked
		prefs.openWithUrl = openWithUrlInput.text.toString()
	}
}
