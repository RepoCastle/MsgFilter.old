package cn.hjmao.msgfilter;

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
	private static final String TAG = "NoteEditor";
	private static final String[] PROJECTION = new String[] {
			MsgFilter.Rules._ID,
			MsgFilter.Rules.COLUMN_NAME_PATTERN,
			MsgFilter.Rules.COLUMN_NAME_DSTNUM };
	private static final String ORIGINAL_CONTENT = "origContent";
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;

	private int mState;
	private Uri mUri;
	private Cursor mCursor;
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
			String pattern = "";
			if (mState == STATE_EDIT) {
				int colPatternIndex = mCursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_PATTERN);
				pattern = mCursor.getString(colPatternIndex);
				Resources res = getResources();
				String title = String.format(res.getString(R.string.rule_edit), pattern);
				setTitle(title);
			} else if (mState == STATE_INSERT) {
				setTitle(getText(R.string.rule_create));
			}
			mPatternText.setTextKeepState(pattern);
			
			int colDstNumIndex = mCursor.getColumnIndex(MsgFilter.Rules.COLUMN_NAME_DSTNUM);
			String dstNum = mCursor.getString(colDstNumIndex);
			mDstNumText.setTextKeepState(dstNum);
			if (mOriginalContent == null) {
				mOriginalContent = dstNum;
			}
		} else {
			setTitle(getText(R.string.error_pattern));
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
			String pattern = mPatternText.getText().toString();
			String dstnum = mDstNumText.getText().toString();
			int patternLen = pattern.length();
			int dstnumLen = dstnum.length();
			
			if (isFinishing() && (patternLen == 0) && (dstnumLen == 0)) {
				setResult(RESULT_CANCELED);
				deleteRule();
			} else {
				updateRule(pattern, dstnum);
				mState = STATE_EDIT;
			}
		}
	}

	private final void updateRule(String pattern, String dstnum) {
		ContentValues values = new ContentValues();
		values.put(MsgFilter.Rules.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
		values.put(MsgFilter.Rules.COLUMN_NAME_PATTERN, pattern);
		values.put(MsgFilter.Rules.COLUMN_NAME_DSTNUM, dstnum);
		getContentResolver().update(mUri, values, null, null);
	}
	private final void deleteRule() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
			mPatternText.setText("");
			mDstNumText.setText("");
		}
	}
	
    public void onClickOk(View v) {
    	
        finish();
    }
}
