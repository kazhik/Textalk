package net.kazhik.android.textalk;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class ExportBitmap extends AsyncTask<Intent, Void, Boolean> {
	private Context m_context;
	private Handler.Callback m_handler;
	private Bitmap m_bitmap;

	public ExportBitmap(Context context, Handler.Callback handler,
			Bitmap bitmap) {
		m_context = context;
		m_bitmap = bitmap;
		m_handler = handler;
	}

	@Override
	protected Boolean doInBackground(Intent... arg0) {
		String filePath = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + m_context.getResources().getString(R.string.app_name);
		File APP_FILE_PATH = new File(filePath);
		try {
			if (!APP_FILE_PATH.exists()) {
				boolean result = APP_FILE_PATH.mkdirs();
				if (!result) {
					Log.d("ExportBitmap", "Failed to make directory");
					return false;
				}
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",
					Locale.getDefault());
			String timeStamp = sdf.format(new Date());
			
			FileOutputStream out = new FileOutputStream(new File(filePath
					+ File.separator + "HW_" + timeStamp + ".png"));
			m_bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			Log.d("ExportBitmap", e.getMessage());
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean bool) {
		super.onPostExecute(bool);
		if (bool) {
			m_handler.handleMessage(null);
		}
	}
}
 
