package cn.hjmao.msgfilter;

import cn.hjmao.msgfilter.R;
import cn.hjmao.msgfilter.utils.RuleManager;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RuleList extends ListActivity {
	private static final String TAG = "NotesList";
	private static final String[] PROJECTION = new String[] {
			MsgFilter.Rules._ID,
			MsgFilter.Rules.COLUMN_NAME_TITLE,
			MsgFilter.Rules.COLUMN_NAME_PATTERN,
	};
	private static final int COLUMN_INDEX_TITLE = 1;
	private static final int COLUMN_INDEX_PATTERN = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RuleManager.setContentResolver(getContentResolver());
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(MsgFilter.Rules.CONTENT_URI);
		}
		getListView().setOnCreateContextMenuListener(this);
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, MsgFilter.Rules.DEFAULT_SORT_ORDER);
		String[] dataColumns = { MsgFilter.Rules.COLUMN_NAME_TITLE };
		int[] viewIDs = { android.R.id.text1 };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.rule_list, cursor, dataColumns, viewIDs);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_options_menu, menu);
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, RuleList.class), null, intent, 0, null);

		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean haveItems = getListAdapter().getCount() > 0;
		if (haveItems) {
			Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());
			Intent[] specifics = new Intent[1];
			specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
			MenuItem[] items = new MenuItem[1];
			Intent intent = new Intent(null, uri);
			intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
			menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, Menu.NONE, Menu.NONE, null, specifics, intent, Menu.NONE, items);
			if (items[0] != null) {
				items[0].setShortcut('1', 'e');
			}
		} else {
			menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			return;
		}
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_context_menu, menu);
		menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));
		Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(), Integer.toString((int) info.id)));
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, RuleList.class), null, intent, 0, null);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}
		Uri ruleUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

		switch (item.getItemId()) {
		case R.id.context_open:
			startActivity(new Intent(Intent.ACTION_EDIT, ruleUri));
			return true;

		case R.id.context_delete:
			getContentResolver().delete(ruleUri, null, null);
			RuleManager.setNeedReload(true);
			return true;
			
		case R.id.context_apply:
			Cursor cursor = getContentResolver().query(ruleUri, PROJECTION, null, null, null);
			cursor.moveToFirst();
			String pattern = cursor.getString(COLUMN_INDEX_PATTERN);
			RuleManager.applyRule(pattern);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
		}
	}
}