package cn.hjmao.msgswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	public SmsReceiver() {
		Log.v("TAG", "SmsRecevier create");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TAG", "SmsRecevier onReceive");
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
				Log.v("TAG", sender + ": " + content);
			}
		}
	}
}
