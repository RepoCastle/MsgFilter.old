package cn.hjmao.msgfilter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContentProvider.PipeDataWriter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class MsgFilterProvider extends ContentProvider implements
		PipeDataWriter<Cursor> {
	private static final String TAG = "MsgFilterProvider";
	private static final String DATABASE_NAME = "msgfilter.db";
	private static final int DATABASE_VERSION = 2;
	private static HashMap<String, String> sRulesProjectionMap;

	private static final String[] READ_RULE_PROJECTION = new String[] {
			MsgFilter.Rules._ID,
			MsgFilter.Rules.COLUMN_NAME_TITLE,
			MsgFilter.Rules.COLUMN_NAME_PATTERN,
			MsgFilter.Rules.COLUMN_NAME_DSTNUM};
	private static final int READ_RULE_TITLE_INDEX = 1;
	private static final int READ_RULE_PATTERN_INDEX = 2;
	private static final int READ_RULE_DSTNUM_INDEX = 3;
	private static final int RULES = 1;
	private static final int RULE_ID = 2;
	private static final UriMatcher sUriMatcher;
	private DatabaseHelper mOpenHelper;
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + MsgFilter.Rules.TABLE_NAME + " ("
					+ MsgFilter.Rules._ID + " INTEGER PRIMARY KEY,"
					+ MsgFilter.Rules.COLUMN_NAME_TITLE + " TEXT,"
					+ MsgFilter.Rules.COLUMN_NAME_PATTERN + " TEXT,"
					+ MsgFilter.Rules.COLUMN_NAME_DSTNUM + " TEXT,"
					+ MsgFilter.Rules.COLUMN_NAME_CREATE_DATE + " INTEGER,"
					+ MsgFilter.Rules.COLUMN_NAME_MODIFICATION_DATE
					+ " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(MsgFilter.AUTHORITY, "rules", RULES);
		sUriMatcher.addURI(MsgFilter.AUTHORITY, "rules/#", RULE_ID);
		sRulesProjectionMap = new HashMap<String, String>();
		sRulesProjectionMap.put(MsgFilter.Rules._ID, MsgFilter.Rules._ID);
		sRulesProjectionMap.put(MsgFilter.Rules.COLUMN_NAME_TITLE, MsgFilter.Rules.COLUMN_NAME_TITLE);
		sRulesProjectionMap.put(MsgFilter.Rules.COLUMN_NAME_PATTERN, MsgFilter.Rules.COLUMN_NAME_PATTERN);
		sRulesProjectionMap.put(MsgFilter.Rules.COLUMN_NAME_DSTNUM, MsgFilter.Rules.COLUMN_NAME_DSTNUM);
		sRulesProjectionMap.put(MsgFilter.Rules.COLUMN_NAME_CREATE_DATE, MsgFilter.Rules.COLUMN_NAME_CREATE_DATE);
		sRulesProjectionMap.put(MsgFilter.Rules.COLUMN_NAME_MODIFICATION_DATE, MsgFilter.Rules.COLUMN_NAME_MODIFICATION_DATE);
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != RULES) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		Long now = Long.valueOf(System.currentTimeMillis());
		if (values.containsKey(MsgFilter.Rules.COLUMN_NAME_CREATE_DATE) == false) {
			values.put(MsgFilter.Rules.COLUMN_NAME_CREATE_DATE, now);
		}

		if (values.containsKey(MsgFilter.Rules.COLUMN_NAME_MODIFICATION_DATE) == false) {
			values.put(MsgFilter.Rules.COLUMN_NAME_MODIFICATION_DATE, now);
		}

		if (values.containsKey(MsgFilter.Rules.COLUMN_NAME_TITLE) == false) {
			Resources r = Resources.getSystem();
			values.put(MsgFilter.Rules.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
		}

		if (values.containsKey(MsgFilter.Rules.COLUMN_NAME_PATTERN) == false) {
			values.put(MsgFilter.Rules.COLUMN_NAME_PATTERN, "");
		}

		if (values.containsKey(MsgFilter.Rules.COLUMN_NAME_DSTNUM) == false) {
			values.put(MsgFilter.Rules.COLUMN_NAME_DSTNUM, "");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = db.insert(MsgFilter.Rules.TABLE_NAME, null, values);

		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(MsgFilter.Rules.CONTENT_ID_URI_BASE, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String finalWhere;

		int count;
		switch (sUriMatcher.match(uri)) {
		case RULES:
			count = db.delete(MsgFilter.Rules.TABLE_NAME, where, whereArgs);
			break;

		case RULE_ID:
			finalWhere = MsgFilter.Rules._ID + " = " + uri.getPathSegments().get(MsgFilter.Rules.RULE_ID_PATH_POSITION);
			if (where != null) {
				finalWhere = finalWhere + " AND " + where;
			}
			count = db.delete(MsgFilter.Rules.TABLE_NAME, finalWhere, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String finalWhere;
		switch (sUriMatcher.match(uri)) {
		case RULES:
			count = db.update(MsgFilter.Rules.TABLE_NAME, values, where, whereArgs);
			break;
		case RULE_ID:
			String ruleId = uri.getPathSegments().get(MsgFilter.Rules.RULE_ID_PATH_POSITION);
			finalWhere = MsgFilter.Rules._ID + " = " + ruleId;
			if (where != null) {
				finalWhere = finalWhere + " AND " + where;
			}
			count = db.update(MsgFilter.Rules.TABLE_NAME, values, finalWhere, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(MsgFilter.Rules.TABLE_NAME);
		switch (sUriMatcher.match(uri)) {
		case RULES:
			qb.setProjectionMap(sRulesProjectionMap);
			break;
		case RULE_ID:
			qb.setProjectionMap(sRulesProjectionMap);
			qb.appendWhere(MsgFilter.Rules._ID + "=" + uri.getPathSegments().get(MsgFilter.Rules.RULE_ID_PATH_POSITION));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = MsgFilter.Rules.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case RULES:
			return MsgFilter.Rules.CONTENT_TYPE;
		case RULE_ID:
			return MsgFilter.Rules.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

    static ClipDescription RULE_STREAM_TYPES = new ClipDescription(null,new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN });
	@Override
	public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
		switch (sUriMatcher.match(uri)) {
		case RULES:
			return null;
		case RULE_ID:
			return RULE_STREAM_TYPES.filterMimeTypes(mimeTypeFilter);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public AssetFileDescriptor openTypedAssetFile(Uri uri,
			String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
		String[] mimeTypes = getStreamTypes(uri, mimeTypeFilter);

		if (mimeTypes != null) {

			Cursor c = query(uri, READ_RULE_PROJECTION, null, null, null);

			if (c == null || !c.moveToFirst()) {
				if (c != null) {
					c.close();
				}
				throw new FileNotFoundException("Unable to query " + uri);
			}

			return new AssetFileDescriptor(openPipeHelper(uri, mimeTypes[0],
					opts, c, this), 0, AssetFileDescriptor.UNKNOWN_LENGTH);
		}
		return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
	}

	@Override
	public void writeDataToPipe(ParcelFileDescriptor output, Uri uri,
			String mimeType, Bundle opts, Cursor c) {
		FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
			pw.println(c.getString(READ_RULE_TITLE_INDEX));
			pw.println("");
			pw.println(c.getString(READ_RULE_PATTERN_INDEX));
			pw.println("");
			pw.println(c.getString(READ_RULE_DSTNUM_INDEX));
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "Ooops", e);
		} finally {
			c.close();
			if (pw != null) {
				pw.flush();
			}
			try {
				fout.close();
			} catch (IOException e) {
			}
		}
	}
}
