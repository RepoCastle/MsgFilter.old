package cn.hjmao.msgfilter;

import java.util.HashMap;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class RuleManager {
	private Uri RULE_URI = MsgFilter.Rules.CONTENT_URI;
	private static final String[] PROJECTION = new String[] {
		MsgFilter.Rules._ID,
		MsgFilter.Rules.COLUMN_NAME_PATTERN,
		MsgFilter.Rules.COLUMN_NAME_DSTNUM };
	private HashMap<String, String> rules = new HashMap<String, String>();
	private ContentResolver contentResolver;
	private boolean needReload = true;
	
	public RuleManager() {
	}
	
	public void setContentResolver(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}
	
	public String match(String sender) {
		String matched = null;
		if (needReload && this.contentResolver != null) {
			Cursor cursor = this.contentResolver.query(RULE_URI, PROJECTION, null, null, MsgFilter.Rules.DEFAULT_SORT_ORDER);
			int patternIndex = cursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_PATTERN);
			int dstnumIndex = cursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_DSTNUM);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				String pattern = cursor.getString(patternIndex);
				String dstnum = cursor.getString(dstnumIndex);
				rules.put(pattern, dstnum);
			}
			cursor.close();

		}
		if (rules != null) {
			for (String pattern: rules.keySet()) {
				if (sender.matches(pattern)) {
					matched = rules.get(pattern);
					break;
				}
			}
		}
		return matched;
	}

	//FIXME:
	public void expRules() {
		
	}

	//FIXME:
	public void impRules() {
		
	}

	public static int applyRule(String pattern, String dstNum) {
		int count = 0;
		//FIXME:
		return count;
	}
}
