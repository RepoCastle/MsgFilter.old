package cn.hjmao.msgfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import cn.hjmao.msgfilter.utils.Notify;
import cn.hjmao.msgfilter.utils.RuleManager;
import cn.hjmao.msgfilter.utils.SMSModifier;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TAG", "onReceive");
		RuleManager.setContentResolver(context.getContentResolver());

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
				String newSender = RuleManager.match(sender);
				if (null != newSender) {
					content = SMSModifier.smsBodyPrefix(sender) + content;
					SMSModifier.smsInsert(context.getContentResolver(), newSender, content);
					this.abortBroadcast();
					Notify.statusBar(context, content);
				}
			}
		}
	}
}
