package cn.hjmao.msgfilter;

import java.util.HashMap;

public class RuleManager {
//	private static Uri RULE_URI = MsgFilter.Rules.CONTENT_URI;
//	private static final String[] PROJECTION = new String[] {
//		MsgFilter.Rules._ID,
//		MsgFilter.Rules.COLUMN_NAME_PATTERN,
//		MsgFilter.Rules.COLUMN_NAME_DSTNUM };
	private HashMap<String, String> rules = new HashMap<String, String>();
//	private Uri mUri;
//	private Cursor mCursor;
	
	public RuleManager() {
//		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		String pattern = "^10658139.*";
		String dstnum = "1065813900000000";
		rules.put(pattern, dstnum);
	}

	public void addRule(String pattern, String dstnum) {
		rules.put(pattern, dstnum);
	}
	
	public String match(String sender) {
		String matched = null;
		
		for (String pattern: rules.keySet()) {
			if (sender.matches(pattern)) {
				matched = rules.get(pattern);
			}
		}
		return matched;
	}
}
