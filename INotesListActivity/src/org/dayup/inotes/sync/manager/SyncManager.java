package org.dayup.inotes.sync.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.io.FileUtils;
import org.dayup.common.Log;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.constants.Constants.AppFiles;
import org.dayup.tasks.BackgroundTaskManager;
import org.dayup.tasks.TwoPhaseTask;

import android.os.AsyncTask;
import android.os.Handler;

/**
 * @author Nicky
 * 
 */
public abstract class SyncManager {

    private final String TAG = SyncManager.class.getSimpleName();

    private volatile boolean isSynchronizing = false;
    // private volatile static boolean isCanceled = false;
    protected INotesApplication application = null;
    protected BackgroundTaskManager backgroundTaskManager = null;

    protected Handler handler = new Handler();
    private Future<Boolean> syncTaskFuture = null;

    protected SyncManager() {

    }

    public boolean isSynchronizing() {
        return isSynchronizing;
    }

    public void setSynchronizing(boolean isSynchronizing) {
        this.isSynchronizing = isSynchronizing;
    }

    public void sync(int syncMode) {
        if (syncTaskFuture != null) {
            syncTaskFuture.cancel(false);
        }
        try {
            syncTaskFuture = backgroundTaskManager.submit(
                    new SyncINotesTask(application.getCurrentAccountId(), syncMode), false);
        } catch (RejectedExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
            notifySynchronized(true, syncMode);
        }
    }

    public void instance(INotesApplication application) {
        this.application = application;
        backgroundTaskManager = application.getBackgroundTaskManager();
    }

    abstract Boolean doSyncINotes(long accountId, int syncMode);

    public interface SyncingRefreshUIListener {
        void refreshUI();
    }

    /**
     * 同步完成更新UI Listener
     */
    private List<RefreshSyncedListener> refreshSyncedListener = new ArrayList<SyncManager.RefreshSyncedListener>();

    /**
     * 同步过程中更新UI Listener
     */
    private List<SyncingRefreshUIListener> refreshSyncinglistener = new ArrayList<SyncManager.SyncingRefreshUIListener>();

    public interface RefreshSyncedListener {
        void onSynchronized(boolean result, int syncMode);
    }

    public void notifySynchronized(boolean result, int syncMode) {
        for (RefreshSyncedListener l : refreshSyncedListener) {
            l.onSynchronized(result, syncMode);
        }
        new CleanTmpDir().execute();
    }

    public void refreshSyncingViews() {
        for (SyncingRefreshUIListener l : refreshSyncinglistener) {
            l.refreshUI();
        }
    }

    class CleanTmpDir extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                FileUtils.cleanDirectory(AppFiles.INOTES_TMP_FILE_DIR);
                return true;
            } catch (Exception e) {
                Log.e("GNotes Clean tmp dir", e.toString(), e);
                return false;
            }
        }

    }

    public void setRefreshSyncedListener(RefreshSyncedListener listener) {
        if (listener != null) {
            this.refreshSyncedListener.add(listener);
        }

    }

    public void setRefreshSyncingListener(SyncingRefreshUIListener listener) {
        if (listener != null) {
            refreshSyncinglistener.add(listener);
        }

    }

    protected class SyncINotesTask implements TwoPhaseTask<Boolean> {
        private int syncMode;
        private long accountId;

        public SyncINotesTask(long accountId, int syncMode) {
            this.accountId = accountId;
            this.syncMode = syncMode;
        }

        @Override
        public void preExecute() {
            Log.d(TAG, "preExecute");
        }

        @Override
        public Boolean doInbackground() {
            try {
                isSynchronizing = true;
                return doSyncINotes(accountId, syncMode);
            } finally {
                isSynchronizing = false;
            }
        }

        @Override
        public void postExecute(Boolean result) {
            Log.d(TAG, "postExecute");
            if (result == null) {
                result = false;
            }
            notifySynchronized(result, syncMode);
        }

        @Override
        public boolean isQuiet() {
            return false;
        }

        @Override
        public boolean doFirstPhaseInBackground() {
            return true;
        }

        @Override
        public void onCancel(Boolean canceled) {
            Log.d(TAG, "onCancel");
        }

    }

}
