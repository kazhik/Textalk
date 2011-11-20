package net.kazhik.android.koekaki;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class KoeKakiConfig extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);
	}
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Constants.MENU_STOPWATCH, Menu.NONE,
        		R.string.menu_stopwatch);
        menu.add(Menu.NONE, Constants.MENU_MAP, Menu.NONE,
        		R.string.menu_map).setIcon(android.R.drawable.ic_menu_mapmode);
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
        switch (item.getItemId()) {
        case Constants.MENU_STOPWATCH:
        	intent = new Intent(this, TokyoRunners.class);
        	intent.setAction(Intent.ACTION_VIEW);
        	startActivity(intent);
        case Constants.MENU_MAP:
        	intent = new Intent(this, MapMode.class);
        	intent.setAction(Intent.ACTION_VIEW);
        	startActivity(intent);
        	
        	break;
        default:
            break;
        }
        return false;
    }       
*/
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
		
		if (preference.getKey().equals("pref_expressions_clear")) {
			setResult(200);
			finish();
			
		}
		return true;
	}

//	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		// if "use_gps" is false, disable "gps_frequency"
		
	}
}
