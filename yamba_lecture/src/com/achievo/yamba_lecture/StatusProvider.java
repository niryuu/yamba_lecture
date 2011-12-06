package com.achievo.yamba_lecture;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class StatusProvider extends ContentProvider {
	private static final String TAG = StatusProvider.class.getSimpleName();

	public static final Uri CONTENT_URI = Uri
			.parse("content://com.achievo.yamba_lecture.statusprovider");
	public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.achievo.yamba_lecture.status";
	public static final String MULTIPLE_RECORDS_MIME_TYPE = "vnd.android.cursor.dir/vnd.achievo.yamba_lecture.status";

	StatusData statusData;

	@Override
	public String getType(Uri uri) {
		return this.getId(uri) < 0 ? MULTIPLE_RECORDS_MIME_TYPE
				: SINGLE_RECORD_MIME_TYPE;
	}

	@Override
	public boolean onCreate() {
		statusData = new StatusData(getContext());
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
		try {
			long id = db.insertOrThrow(StatusData.TABLE, null, values);
			if (id == -1) {
				throw new RuntimeException(
						String.format(
								"%s: Failed to insert [%s] to [%s] for unknown reasons.",
								TAG, values, uri));
			} else {
				Uri newUri = ContentUris.withAppendedId(uri, id);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
		} finally {
			db.close();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		long id = this.getId(uri);
		int count;
		SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
		try {
			if (id < 0) {
				count = db.update(StatusData.TABLE, values, selection,
						selectionArgs);
			} else {
				count = db.update(StatusData.TABLE, values, StatusData.C_ID
						+ "=" + id, null);
			}
		} finally {
			db.close();
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = this.getId(uri);
		int count;
		SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
		try {
			if (id < 0) {
				count = db.delete(StatusData.TABLE, selection, selectionArgs);
			} else {
				count = db.delete(StatusData.TABLE, StatusData.C_ID + "=" + id,
						null);
			}
		} finally {
			db.close();
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = this.getId(uri);
		SQLiteDatabase db = statusData.dbHelper.getReadableDatabase();
		Log.d(TAG, "querying");

		Cursor c;

		if (id < 0) {
			c = db.query(StatusData.TABLE, projection, selection,
					selectionArgs, null, null, sortOrder);
		} else {
			c = db.query(StatusData.TABLE, projection, StatusData.C_ID + "="
					+ id, null, null, null, null);
		}

		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	private long getId(Uri uri) {
		String lastPathSegment = uri.getLastPathSegment();
		if (lastPathSegment != null) {
			try {
				return Long.parseLong(lastPathSegment);
			} catch (NumberFormatException e) {
			}
		}
		return -1;
	}

}
