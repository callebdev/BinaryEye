package de.markusfisch.android.binaryeye.app

import android.net.Uri

private val nonPrintable = "[\\x00-\\x08\\x0e-\\x1f]".toRegex()

fun hasNonPrintableCharacters(s: String) = nonPrintable.containsMatchIn(s)

fun parseAndNormalizeUri(input: String): Uri = Uri.parse(input).let {
	it.normalizeScheme()
}
