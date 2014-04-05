package net.kazhik.android.textalk.chat;

public interface ConnectionListener {
	void onNewHost(String addr, String name);
	void onHostDead(String addr);

}
