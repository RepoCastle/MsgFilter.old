package cn.hjmao.msgswitch;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MsgSwitch extends Activity {
	private SmsReceiver recevier;
	private boolean isregiset = false;
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		recevier = new SmsReceiver();
	}

	public void regiset(View v) {
		IntentFilter filter = new IntentFilter(ACTION);
		filter.setPriority(1000);
		registerReceiver(recevier, filter);
		isregiset = true;
		Toast.makeText(this, "Start filter mode", 0).show();
	}

	public void unregiset(View v) {
		if (recevier != null && isregiset) {
			unregisterReceiver(recevier);
			isregiset = false;
			Toast.makeText(this, "Stop filter mode", 0).show();
		} else {
			Toast.makeText(this, "Not start filter mode yet", 0).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}