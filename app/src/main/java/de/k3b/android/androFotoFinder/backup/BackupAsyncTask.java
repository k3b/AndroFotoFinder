package de.k3b.android.androFotoFinder.backup;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import de.k3b.android.androFotoFinder.Global;
import de.k3b.android.androFotoFinder.R;
import de.k3b.io.IProgessListener;
import de.k3b.zip.IZipConfig;
import de.k3b.zip.LibZipGlobal;
import de.k3b.zip.ZipConfigDto;
import de.k3b.zip.ZipStorage;

class ProgressData {
    final int itemcount;
    final int size;
    final String message;

    ProgressData(int itemcount, int size, String message) {
        this.itemcount = itemcount;
        this.size = size;
        this.message = message;
    }
}

public class BackupAsyncTask extends AsyncTask<Object, ProgressData, IZipConfig> implements IProgessListener {

    private final Backup2ZipService service;
    private Activity activity;
    private ProgressBar mProgressBar = null;
    private TextView status;
    private AtomicBoolean isActive = new AtomicBoolean(true);

    // last known number of items to be processed
    private int lastSize = 0;

    public BackupAsyncTask(Context context, ZipConfigDto mZipConfigData, ZipStorage zipStorage) {
        this.service = new Backup2ZipService(context.getApplicationContext(),
                mZipConfigData, zipStorage, null);
    }

    public void setContext(Activity activity, ProgressBar progressBar, TextView status) {
        this.activity = activity;
        mProgressBar = progressBar;
        this.status = status;
        service.setProgessListener((progressBar != null) ? this : null);
    }

    @Override
    protected IZipConfig doInBackground(Object... voids) {
        try {
            return this.service.execute();
        } finally {
            this.isActive.set(false);
        }
    }

    /** called on success */
    @Override
    protected void onPostExecute(IZipConfig iZipConfig) {
        super.onPostExecute(iZipConfig);
        if (activity != null) {
            activity.setResult(Activity.RESULT_OK);
            activity.finish();

            Toast.makeText(activity, iZipConfig.toString(), Toast.LENGTH_LONG).show();

            if (LibZipGlobal.debugEnabled || Global.debugEnabled) {
                Log.d(LibZipGlobal.LOG_TAG, activity.getClass().getSimpleName() + ": " +
                                activity.getText(R.string.backup_title) + " " +
                                iZipConfig.toString());
            }

            setContext(null, null, null);
        }
    }

    /** called on error */
    @Override
    protected void onCancelled() {
        if (activity != null) {
            Toast.makeText(activity, activity.getText(android.R.string.cancel), Toast.LENGTH_LONG).show();

            if (LibZipGlobal.debugEnabled || Global.debugEnabled) {
                Log.d(LibZipGlobal.LOG_TAG, activity.getClass().getSimpleName() + ": " + activity.getText(android.R.string.cancel));
            }

            setContext(null, null, null);
        }
    }

    public static boolean isActive(BackupAsyncTask backupAsyncTask) {
        return (backupAsyncTask != null) && (backupAsyncTask.isActive.get());
    }

    /**
     * de.k3b.io.IProgessListener:
     *
     * called every time when command makes some little progress in non gui thread.
     * return true to continue
     */
    @Override
    public boolean onProgress(int itemcount, int size, String message) {
        publishProgress(new ProgressData(itemcount, size, message));
        return !this.isCancelled();
    }

    /** called from {@link AsyncTask} in gui task */
    @Override
    protected void onProgressUpdate(ProgressData... values) {
        if (mProgressBar != null) {
            int size = values[0].size;
            if ((size != 0) && (size > lastSize)) {
                mProgressBar.setMax(size);
                lastSize = size;
            }
            mProgressBar.setProgress(values[0].itemcount);
        }
        if (this.status != null) {
            this.status.setText(values[0].message);
        }
    }
}
