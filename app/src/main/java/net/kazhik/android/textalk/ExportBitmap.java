package net.kazhik.android.textalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ExportBitmap extends AsyncTask<Intent, Void, Boolean> {
    private Context m_context;
    private Handler.Callback m_handler;
    private Bitmap m_bitmap;
    private String filename;
    private String sender;

    public ExportBitmap(Context context, Handler.Callback handler,
            Bitmap bitmap, String sender) {
        m_context = context;
        m_handler = handler;
        m_bitmap = bitmap;
        this.sender = sender;
    }

    @Override
    protected Boolean doInBackground(Intent... arg0) {
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + m_context.getResources().getString(R.string.app_name);
        File APP_FILE_PATH = new File(filePath);
        FileOutputStream out = null;
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

            this.filename = filePath + File.separator + "HW_" + timeStamp + ".png";
            out = new FileOutputStream(new File(this.filename));
            m_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            return true;
        } catch (Exception e) {
            Log.e("ExportBitmap", e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e("ExportBitmap", e.getMessage(), e);
                }
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);
        if (bool) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("filename", this.filename);
            bundle.putString("sender", this.sender);
            msg.setData(bundle);
            m_handler.handleMessage(msg);
        }
    }
}
 
