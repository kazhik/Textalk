package net.kazhik.android.textalk.chat;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable {
	public static final int SENT = 0x01;
	public static final int RECEIVED = 0x02;
	public static final int SYSTEM = 0x03;

	private int type;
	private String sender;
	private String message;
	private long sendTime;

	public ChatMessage(int type, String sender, String msg) {
		this.type = type;
		this.sender = sender;
		this.message = msg;
		this.sendTime = System.currentTimeMillis();
	}
    private ChatMessage(Parcel in) {
		this.type = in.readInt();
		String[] strArray = new String[2];
		in.readStringArray(strArray);
		this.sender = strArray[0];
		this.message = strArray[1];
		this.sendTime = in.readLong();
    }
	boolean isSent() {
		return (this.type == SENT);
	}

	String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;

	}

	String getSender() {
		return this.sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	long getSendTime() {
		return this.sendTime;
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.type);
		String[] strArray = {this.sender, this.message};
		dest.writeStringArray(strArray);
		
	}
	
    public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };	
}
