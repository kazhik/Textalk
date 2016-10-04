package net.kazhik.android.textalk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {
	public static AboutDialog newInstance() {

		return new AboutDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
		PackageInfo pkgInfo;
		try {
			pkgInfo = activity.getPackageManager().getPackageInfo(
					activity.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		Resources res = getResources();
		String aboutText = res.getString(R.string.app_name) +
				"\n\n" +
				"Version: " + pkgInfo.versionName +
				"\n" +
				"Website: github.com/kazhik/Textalk";
		final SpannableString sstr = new SpannableString(aboutText);
		Linkify.addLinks(sstr, Linkify.ALL);

		return new AlertDialog.Builder(getActivity())
				.setPositiveButton(android.R.string.ok, null)
				.setMessage(sstr)
				.create();
	}
	@Override
	public void onStart() {
		super.onStart();
		((TextView) getDialog().findViewById(android.R.id.message))
				.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
