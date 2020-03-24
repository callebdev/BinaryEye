package de.markusfisch.android.binaryeye.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import de.markusfisch.android.binaryeye.app.hasNonPrintableCharacters
import de.markusfisch.android.binaryeye.app.prefs
import de.markusfisch.android.binaryeye.repository.Scan

class Database {
	private lateinit var db: SQLiteDatabase

	fun open(context: Context) {
		db = OpenHelper(context).writableDatabase
	}

	fun getScans(): Cursor? = db.rawQuery(
		"""SELECT
			$SCANS_ID,
			$SCANS_DATETIME,
			$SCANS_CONTENT,
			$SCANS_FORMAT
			FROM $SCANS
			ORDER BY $SCANS_DATETIME DESC
		""", null
	)

	fun getScan(id: Long): Cursor? = db.rawQuery(
		"""SELECT
			$SCANS_DATETIME,
			$SCANS_CONTENT,
			$SCANS_RAW,
			$SCANS_FORMAT,
			$SCANS_ERROR_CORRECTION_LEVEL,
			$SCANS_ISSUE_NUMBER,
			$SCANS_ORIENTATION,
			$SCANS_OTHER_META_DATA,
			$SCANS_PDF417_EXTRA_METADATA,
			$SCANS_POSSIBLE_COUNTRY,
			$SCANS_SUGGESTED_PRICE,
			$SCANS_UPC_EAN_EXTENSION
			FROM $SCANS
			WHERE $SCANS_ID = ?
		""", arrayOf("$id")
	)

	fun hasBinaryData(): Cursor? = db.rawQuery(
		"""SELECT
			1
			FROM $SCANS
			WHERE $SCANS_RAW IS NOT NULL
			LIMIT 1
		""", null
	)

	fun insertScan(scan: Scan): Long {
		val cv = ContentValues()
		cv.put(SCANS_DATETIME, scan.timestamp)
		val isRaw = hasNonPrintableCharacters(scan.content)
		if (isRaw) {
			cv.put(SCANS_CONTENT, "")
			cv.put(SCANS_RAW, scan.raw ?: scan.content.toByteArray())
		} else {
			cv.put(SCANS_CONTENT, scan.content)
		}
		cv.put(SCANS_FORMAT, scan.format)
		scan.errorCorrectionLevel?.let {
			cv.put(SCANS_ERROR_CORRECTION_LEVEL, it)
		}
		scan.issueNumber?.let { cv.put(SCANS_ISSUE_NUMBER, it) }
		scan.orientation?.let { cv.put(SCANS_ORIENTATION, it) }
		scan.otherMetaData?.let { cv.put(SCANS_OTHER_META_DATA, it) }
		scan.pdf417ExtraMetaData?.let { cv.put(SCANS_PDF417_EXTRA_METADATA, it) }
		scan.possibleCountry?.let { cv.put(SCANS_POSSIBLE_COUNTRY, it) }
		scan.suggestedPrice?.let { cv.put(SCANS_SUGGESTED_PRICE, it) }
		scan.upcEanExtension?.let { cv.put(SCANS_UPC_EAN_EXTENSION, it) }
		if (prefs.ignoreConsecutiveDuplicates) {
			val id = getLastScan(
				cv.get(SCANS_CONTENT) as String,
				if (isRaw) cv.get(SCANS_RAW) as ByteArray else null,
				scan.format
			)
			if (id > 0L) {
				return id
			}
		}
		return db.insert(SCANS, null, cv)
	}

	private fun getLastScan(
		content: String,
		raw: ByteArray?,
		format: String
	): Long {
		return db.rawQuery(
			"""SELECT
				$SCANS_ID,
				$SCANS_CONTENT,
				$SCANS_RAW,
				$SCANS_FORMAT
				FROM $SCANS
				ORDER BY $SCANS_ID DESC
				LIMIT 1
			""", null
		)?.use {
			if (it.count > 0 &&
				it.moveToFirst() &&
				it.getString(it.getColumnIndex(SCANS_CONTENT)) == content &&
				(raw == null || it.getBlob(it.getColumnIndex(SCANS_RAW))
					?.contentEquals(raw) == true) &&
				it.getString(it.getColumnIndex(SCANS_FORMAT)) == format
			) {
				it.getLong(it.getColumnIndex(SCANS_ID))
			} else {
				0L
			}
		} ?: 0L
	}

	fun removeScan(id: Long) {
		db.delete(SCANS, "$SCANS_ID = ?", arrayOf("$id"))
	}

	fun removeScans() {
		db.delete(SCANS, null, null)
	}

	private class OpenHelper(context: Context) :
		SQLiteOpenHelper(context, "history.db", null, 3) {
		override fun onCreate(db: SQLiteDatabase) {
			createScans(db)
		}

		override fun onUpgrade(
			db: SQLiteDatabase,
			oldVersion: Int,
			newVersion: Int
		) {
			if (oldVersion < 2) {
				addRawColumn(db)
			}
			if (oldVersion < 3) {
				addMetaDataColumns(db)
			}
		}
	}

	companion object {
		const val SCANS = "scans"
		const val SCANS_ID = "_id"
		const val SCANS_DATETIME = "_datetime"
		const val SCANS_CONTENT = "content"
		const val SCANS_RAW = "raw"
		const val SCANS_FORMAT = "format"
		const val SCANS_ERROR_CORRECTION_LEVEL = "error_correction_level"
		const val SCANS_ISSUE_NUMBER = "issue_number"
		const val SCANS_ORIENTATION = "orientation"
		const val SCANS_OTHER_META_DATA = "other_meta_data"
		const val SCANS_PDF417_EXTRA_METADATA = "pdf417_extra_metadata"
		const val SCANS_POSSIBLE_COUNTRY = "possible_country"
		const val SCANS_SUGGESTED_PRICE = "suggested_price"
		const val SCANS_UPC_EAN_EXTENSION = "upc_ean_extension"

		private fun createScans(db: SQLiteDatabase) {
			db.execSQL("DROP TABLE IF EXISTS $SCANS")
			db.execSQL(
				"""CREATE TABLE $SCANS (
					$SCANS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
					$SCANS_DATETIME DATETIME NOT NULL,
					$SCANS_CONTENT TEXT NOT NULL,
					$SCANS_RAW BLOB,
					$SCANS_FORMAT TEXT NOT NULL,
					$SCANS_ERROR_CORRECTION_LEVEL TEXT,
					$SCANS_ISSUE_NUMBER INT,
					$SCANS_ORIENTATION INT,
					$SCANS_OTHER_META_DATA TEXT,
					$SCANS_PDF417_EXTRA_METADATA TEXT,
					$SCANS_POSSIBLE_COUNTRY TEXT,
					$SCANS_SUGGESTED_PRICE TEXT,
					$SCANS_UPC_EAN_EXTENSION TEXT
				)"""
			)
		}

		private fun addRawColumn(db: SQLiteDatabase) {
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_RAW BLOB")
		}

		private fun addMetaDataColumns(db: SQLiteDatabase) {
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_ERROR_CORRECTION_LEVEL TEXT")
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_ISSUE_NUMBER INT")
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_ORIENTATION INT")
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_OTHER_META_DATA TEXT")
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_PDF417_EXTRA_METADATA TEXT")
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_POSSIBLE_COUNTRY TEXT")
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_SUGGESTED_PRICE TEXT")
			db.execSQL("ALTER TABLE $SCANS ADD COLUMN $SCANS_UPC_EAN_EXTENSION TEXT")
		}
	}
}
