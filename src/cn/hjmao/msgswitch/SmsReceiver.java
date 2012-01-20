package cn.hjmao.msgswitch;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Log;
import cn.hjmao.msgswitch.filter.RuleManager;

public class SmsReceiver extends BroadcastReceiver {

	private RuleManager ruleManager;

	public SmsReceiver() {
		Log.v("TAG", "SmsReceiver start");
		this.ruleManager = new RuleManager();
		Log.v("TAG", "SmsReceiver done");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TAG", "onReceive");

		Object[] pdus = (Object[]) intent.getExtras().get("pdus");
		if (pdus != null && pdus.length > 0) {
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; i++) {
				byte[] pdu = (byte[]) pdus[i];
				messages[i] = SmsMessage.createFromPdu(pdu);
			}

			String content = "";
			for (SmsMessage message : messages) {
				content += message.getMessageBody();
			}
			
			if (messages.length > 0) {
				String sender = messages[0].getOriginatingAddress();
				if (ruleManager.match(sender)) {
					try {
						ContentValues values = msg2cv(content, "1065813900000000");
						context.getContentResolver().insert(Uri.parse("content://sms/inbox"),  values);
					} catch (Exception e) {
						e.printStackTrace();
					}
					this.abortBroadcast();
				}
			}
		}
	}

	private ContentValues msg2cv(String content, String newSender) {
		ContentValues values = new ContentValues();
		values.put("address", newSender);
		values.put("read", 0);
		values.put("status", -1);
		values.put( "type", 1);
		values.put("body", content);
		return values;
	}
}
