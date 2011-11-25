package net.kazhik.android.textalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Config extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);
	}
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
		
		if (preference.getKey().equals("pref_expressions_clear")) {
			setResult(Constants.RESULT_CODE_CLEAR);
			finish();
			
		}
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		
	}
}
