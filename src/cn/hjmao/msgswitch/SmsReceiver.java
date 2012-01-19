package cn.hjmao.msgswitch;

import cn.hjmao.msgswitch.filter.RuleManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	private RuleManager ruleManager;
	public SmsReceiver() {
		Log.v("TAG", "SmsReceiver start");
		ruleManager = new RuleManager();
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

			for (SmsMessage message : messages) {
				String content = message.getMessageBody();
				String sender = message.getOriginatingAddress();
				
				// Check with the rules
				if (ruleManager.match(sender)) {
					Log.v("TAG", "Match rule!!!");
					this.abortBroadcast();
				} else {
					Log.v("TAG", "Not match rule!!!");
				}
				Log.v("TAG", sender + ": " + content);
			}
		}
	}
}
