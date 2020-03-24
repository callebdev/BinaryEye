package de.markusfisch.android.binaryeye.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import de.markusfisch.android.binaryeye.R
import de.markusfisch.android.binaryeye.app.prefs
import de.markusfisch.android.binaryeye.app.setWindowInsetListener
import de.markusfisch.android.binaryeye.view.setPadding

class PreferencesFragment : Fragment() {
	private lateinit var openImmediatelySwitch: SwitchCompat
	private lateinit var useHistorySwitch: SwitchCompat
	private lateinit var showMetaDataSwitch: SwitchCompat
	private lateinit var showHexDumpSwitch: SwitchCompat
	private lateinit var ignoreConsecutiveDuplicatesSwitch: SwitchCompat
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

		openImmediatelySwitch = view.findViewById(R.id.open_immediately)
		if (prefs.openImmediately) {
			openImmediatelySwitch.toggle()
		}

		useHistorySwitch = view.findViewById(R.id.use_history)
		if (prefs.useHistory) {
			useHistorySwitch.toggle()
		}

		ignoreConsecutiveDuplicatesSwitch = view.findViewById(
			R.id.ignore_consecutive_duplicates
		)
		if (prefs.ignoreConsecutiveDuplicates) {
			ignoreConsecutiveDuplicatesSwitch.toggle()
		}

		showMetaDataSwitch = view.findViewById(R.id.show_meta_data)
		if (prefs.showMetaData) {
			showMetaDataSwitch.toggle()
		}

		showHexDumpSwitch = view.findViewById(R.id.show_hex_dump)
		if (prefs.showHexDump) {
			showHexDumpSwitch.toggle()
		}

		openWithUrlInput = view.findViewById(R.id.open_with_url)
		openWithUrlInput.setText(prefs.openWithUrl)

		setWindowInsetListener { insets ->
			(view.findViewById(R.id.scroll_view) as View).setPadding(insets)
		}

		return view
	}

	override fun onPause() {
		super.onPause()
		prefs.openImmediately = openImmediatelySwitch.isChecked
		prefs.useHistory = useHistorySwitch.isChecked
		prefs.ignoreConsecutiveDuplicates = ignoreConsecutiveDuplicatesSwitch.isChecked
		prefs.showMetaData = showMetaDataSwitch.isChecked
		prefs.showHexDump = showHexDumpSwitch.isChecked
		prefs.openWithUrl = openWithUrlInput.text.toString()
	}
}
