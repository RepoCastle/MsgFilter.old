package cn.hjmao.msgfilter;

import android.net.Uri;
import android.provider.BaseColumns;

public class MsgFilter {
    public static final String AUTHORITY = "cn.hjmao.MsgFilter";
    private MsgFilter() {
    }
    public static final class Rules implements BaseColumns {
    	private Rules() {}
    	public static final String TABLE_NAME = "rules";
        private static final String SCHEME = "content://";
        private static final String PATH_RULES = "/rules";
        private static final String PATH_RULE_ID = "/rules/";
        public static final int RULE_ID_PATH_POSITION = 1;
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_RULES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RULE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RULE_ID + "/#");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.msgfilter.rule";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.msgfilter.rule";
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        public static final String COLUMN_NAME_PATTERN = "pattern";
        public static final String COLUMN_NAME_DSTNUM = "dstnumber";
        public static final String COLUMN_NAME_CREATE_DATE = "created";
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    }
}
