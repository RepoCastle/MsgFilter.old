package cn.hjmao.msgfilter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	private Context context;
	
	private RuleManager ruleManager;
	private static Uri SMSINBOX_URI = Uri.parse("content://sms/inbox");
	public SmsReceiver() {
		Log.v("TAG", "SmsReceiver start");
		this.ruleManager = new RuleManager();
		Log.v("TAG", "SmsReceiver done");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TAG", "onReceive");

		this.context = context;
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
				ruleManager.setContentResolver(context.getContentResolver());
				String newSender = ruleManager.match(sender);
				if (null != newSender) {
					try {
						ContentValues values = msg2cv(content, newSender);
						context.getContentResolver().insert(SMSINBOX_URI, values);
					} catch (Exception e) {
						e.printStackTrace();
					}
					this.abortBroadcast();
					notification(content);
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
	
	private void notification(String content) {
		NotificationManager notifMng = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notif = new Notification();
		notif.icon = android.R.drawable.stat_notify_chat;
		notif.tickerText = "New msgfilter notification";
		notif.defaults = Notification.DEFAULT_SOUND;
		notif.flags |= Notification.FLAG_AUTO_CANCEL;
		notif.flags |= Notification.DEFAULT_SOUND;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setType("vnd.android-dir/mms-sms");
		PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		notif.setLatestEventInfo(context, "MsgFilter", "You have unread mail notifcation!", pIntent);
		notifMng.notify(0, notif);
	}
}
