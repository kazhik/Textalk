package net.kazhik.android.textalk.chat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.kazhik.android.textalk.R;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter {

	private static class ViewHolder {
		public TextView message;
		public TextView info;
	}

	private Context context;
	private List<ChatMessage> messages
		= Collections.synchronizedList(new ArrayList<ChatMessage>());

	public ChatAdapter(Context context) {
		super();
		this.context = context;
	}

	public void setMessageList(List<ChatMessage> msgList) {
		this.messages.clear();
		this.messages.addAll(msgList);
	}
	public void addMessage(ChatMessage msg) {
		this.messages.add(msg);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return this.messages.size();
	}

	@Override
	public Object getItem(int position) {
		return this.messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(this.context).inflate(
					R.layout.chat_row, parent, false);
			holder.message = (TextView) convertView
					.findViewById(R.id.text_message);
			holder.info = (TextView) convertView.findViewById(R.id.text_info);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ChatMessage message = (ChatMessage) this.getItem(position);
		
		holder.message.setText(message.getMessage());
		
		String sender = message.getSender();
		
		DateFormat df = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault());
		String timeText = df.format(Calendar.getInstance().getTime());
		
		String infoTxt = "(" + sender + "/" + timeText + ")";
		
		holder.info.setText(infoTxt);
		
		int gravity = message.isSent() ? Gravity.RIGHT : Gravity.LEFT;
		
		LayoutParams lp;

		lp = (LayoutParams) holder.message.getLayoutParams();
		lp.gravity = gravity;
		holder.message.setLayoutParams(lp);

		lp = (LayoutParams) holder.info.getLayoutParams();
		lp.gravity = gravity;
		holder.info.setLayoutParams(lp);

		return convertView;
	}

}
