package cn.hjmao.msgfilter.utils;

import java.util.HashMap;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import cn.hjmao.msgfilter.MsgFilter;

public class RuleManager {
	private static Uri RULE_URI = MsgFilter.Rules.CONTENT_URI;
	private static Uri SMSINBOX_URI = Uri.parse("content://sms/inbox");
	private static final String[] PROJECTION = new String[] {
		MsgFilter.Rules._ID,
		MsgFilter.Rules.COLUMN_NAME_PATTERN,
		MsgFilter.Rules.COLUMN_NAME_DSTNUM };
	private static HashMap<String, String> rules = new HashMap<String, String>();
	private static ContentResolver contentResolver;
	private static boolean needReload = true;
	
	public static void setContentResolver(ContentResolver contentResolver) {
		RuleManager.contentResolver = contentResolver;
	}
	
	public static String match(String sender) {
		String matched = null;
		if (RuleManager.needReload && RuleManager.contentResolver != null) {
			reloadRules(RuleManager.contentResolver);
			RuleManager.needReload = false;
		}
		if (RuleManager.rules != null) {
			for (String pattern: RuleManager.rules.keySet()) {
				if (sender.matches(pattern)) {
					matched = RuleManager.rules.get(pattern);
					break;
				}
			}
		}
		return matched;
	}
	
	public static String match(String sender, String pattern) {
		String matched = null;
		
		if (RuleManager.needReload && RuleManager.contentResolver != null) {
			reloadRules(RuleManager.contentResolver);
			RuleManager.needReload = false;
		}
		
		if (sender.matches(pattern)) {
			matched = RuleManager.rules.get(pattern);
		}
		return matched;
	}

	private static void reloadRules(ContentResolver contentResolver) {
		if (contentResolver == null) return;
		RuleManager.rules.clear();

		Cursor cursor = RuleManager.contentResolver.query(RuleManager.RULE_URI, RuleManager.PROJECTION, null, null, MsgFilter.Rules.DEFAULT_SORT_ORDER);
		int patternIndex = cursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_PATTERN);
		int dstnumIndex = cursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_DSTNUM);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String pattern = cursor.getString(patternIndex);
			String dstnum = cursor.getString(dstnumIndex);
			RuleManager.rules.put(pattern, dstnum);
		}
		cursor.close();
	}
	
	//FIXME:
	public void expRules() {
	}

	//FIXME:
	public void impRules() {
		
	}

	public static int applyRule(String pattern) {
		int count = 0;
		String[] SMS_PROJECTION = new String[] { "_id", "thread_id", "address", "body"};
		Cursor cursor = RuleManager.contentResolver.query(SMSINBOX_URI, SMS_PROJECTION, null, null, null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String sender = cursor.getString(cursor.getColumnIndex("address"));
			String dstNum = RuleManager.match(sender, pattern);
			if (dstNum != null) {
				long smsID = cursor.getLong(cursor.getColumnIndex("_id"));
				
				String newSender = dstNum;
				String body = cursor.getString(cursor.getColumnIndex("body"));
				if (!SMSModifier.isSmsBodyModifiedByMsgFilter(body)) {
					body = SMSModifier.smsBodyPrefix(sender) + body;
				}
				long threadID = cursor.getLong(cursor.getColumnIndex("thread_id"));
				SMSModifier.smsDelete(contentResolver, threadID, smsID);
				SMSModifier.smsInsert(contentResolver, newSender, body);
				count++;
			}
		}
		cursor.close();
		
		return count;
	}
	
	public static void setNeedReload(boolean needReload) {
		RuleManager.needReload = needReload;
	}
}
