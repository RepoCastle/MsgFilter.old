package cn.hjmao.msgfilter;

import cn.hjmao.msgfilter.utils.RuleManager;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class RuleEditor extends Activity {
	private static final String TAG = "RuleEditor";
	private static final String[] PROJECTION = new String[] {
			MsgFilter.Rules._ID,
			MsgFilter.Rules.COLUMN_NAME_TITLE,
			MsgFilter.Rules.COLUMN_NAME_PATTERN,
			MsgFilter.Rules.COLUMN_NAME_DSTNUM};
	private static final String ORIGINAL_CONTENT = "origContent";
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;

	private int mState;
	private Uri mUri;
	private Cursor mCursor;
	private EditText mTitleText;
	private EditText mPatternText;
	private EditText mDstNumText;
	private String mOriginalContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final String action = intent.getAction();

		if (Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			mUri = intent.getData();
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
			mUri = getContentResolver().insert(intent.getData(), null);
			if (mUri == null) {
				Log.e(TAG, "Failed to insert new rule into " + getIntent().getData());
				finish();
				return;
			}
			setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		setContentView(R.layout.rule_editor);
		mTitleText = (EditText) findViewById(R.id.title);
		mPatternText = (EditText) findViewById(R.id.pattern);
		mDstNumText = (EditText) findViewById(R.id.dstnum);

		if (savedInstanceState != null) {
			mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCursor != null) {
			mCursor.requery();
			mCursor.moveToFirst();
			String title = "";
			if (mState == STATE_EDIT) {
				int colTitleIndex = mCursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_TITLE);
				title = mCursor.getString(colTitleIndex);
				Resources res = getResources();
				String name = String.format(res.getString(R.string.rule_edit), title);
				setTitle(name);
			} else if (mState == STATE_INSERT) {
				setTitle(getText(R.string.rule_create));
			}
			mTitleText.setTextKeepState(title);
			
			int colDstNumIndex = mCursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_DSTNUM);
			String dstNum = mCursor.getString(colDstNumIndex);
			mDstNumText.setTextKeepState(dstNum);
			
			int colPatternIndex = mCursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_PATTERN);
			String pattern = mCursor.getString(colPatternIndex);
			mPatternText.setTextKeepState(pattern);
			
			if (mOriginalContent == null) {
				mOriginalContent = dstNum;
			}
		} else {
			setTitle(getText(R.string.error_title));
			mTitleText.setText(getText(R.string.error_title));
			mPatternText.setText(getText(R.string.error_pattern));
			mDstNumText.setText(getText(R.string.error_message));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(ORIGINAL_CONTENT, mOriginalContent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCursor != null) {
			String title = mTitleText.getText().toString();
			String pattern = mPatternText.getText().toString();
			String dstnum = mDstNumText.getText().toString();
			int titleLen = title.length();
			int patternLen = pattern.length();
			int dstnumLen = dstnum.length();
			
			if (isFinishing() && (patternLen == 0) && (dstnumLen == 0) && (titleLen == 0)) {
				setResult(RESULT_CANCELED);
				deleteRule();
			} else {
				updateRule(title, pattern, dstnum);
				mState = STATE_EDIT;
			}
		}
	}

	private final void updateRule(String title, String pattern, String dstnum) {
		ContentValues values = new ContentValues();
		values.put(MsgFilter.Rules.COLUMN_NAME_TITLE, title);
		values.put(MsgFilter.Rules.COLUMN_NAME_PATTERN, pattern);
		values.put(MsgFilter.Rules.COLUMN_NAME_DSTNUM, dstnum);
		values.put(MsgFilter.Rules.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
		getContentResolver().update(mUri, values, null, null);
		RuleManager.setNeedReload(true);
	}

	private final void deleteRule() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
			mTitleText.setText("");
			mPatternText.setText("");
			mDstNumText.setText("");
			RuleManager.setNeedReload(true);
		}
	}
	
	public void onClickOk(View v) {
		finish();
		if (mCursor != null) {
			mCursor.close();
		}
	}
}
