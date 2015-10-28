package org.dayup.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.dayup.tasks.AsyncTask.Status;

import android.os.Handler;

public class BackgroundTaskManager {

    public static interface BackgroundTaskStatusListener {
        void onBackgroundException(Throwable e);

        void onLoadBegin();

        void onLoadEnd();
    }

    protected List<BackgroundTaskStatusListener> listeners = new ArrayList<BackgroundTaskStatusListener>();

    protected Handler handler = new Handler();

    protected HashMap<BackgroundTask<?>, Long> runningTasks = new HashMap<BackgroundTask<?>, Long>();
    protected AtomicInteger count = new AtomicInteger(0);

    protected ExecutorService singleThreadService = Executors.newSingleThreadExecutor();

    protected long mTimeout = 30000;

    private String TAG = getClass().getSimpleName();

    private Runnable checkJob = new Runnable() {

        @Override
        public void run() {
            if (!runningTasks.isEmpty()) {
                while (true) {
                    boolean flag = false;
                    for (Map.Entry<BackgroundTask<?>, Long> entry : runningTasks.entrySet()) {
                        if (System.currentTimeMillis() - entry.getValue() > mTimeout) {
                            if (entry.getKey().cancel(true)) {
                                runningTasks.remove(entry.getKey());
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (!flag)
                        break;
                }
            }
            // Log.d(TAG, "RunningTasks " + runningTasks.size() + ", " +
            // runningTasks);
            handler.postDelayed(checkJob, 1000);
        }
    };

    public BackgroundTaskManager(long timeout) {
        this.mTimeout = timeout;
        handler.postDelayed(checkJob, 1000);
    }

    public synchronized void addBackgroundTaskStatusListener(BackgroundTaskStatusListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeBackgroundTaskStatusListener(
            BackgroundTaskStatusListener listener) {
        listeners.remove(listener);
    }

    protected void notifyLoadBegin() {
        for (BackgroundTaskStatusListener listener : listeners) {
            listener.onLoadBegin();
        }
    }

    protected void notifyLoadEnd() {
        for (BackgroundTaskStatusListener listener : listeners) {
            listener.onLoadEnd();
        }
    }

    protected void notifyBackgroundException(Throwable e) {
        for (BackgroundTaskStatusListener listener : listeners) {
            listener.onBackgroundException(e);
        }
    }

    public <T> Future<T> submit(Task<T> task) {
        return submit(task, true);
    }

    public <T> Future<T> submit(Task<T> task, boolean seperate) {
        BackgroundTask<T> t = getBackgroundTask(task);
        BackgroundFuture<T> f = new BackgroundFuture<T>(t);
        if (seperate) {
            t.execute();
        } else {
            t.execute(singleThreadService);
        }

        return f;
    }

    public <T> Future<T> schedule(Task<T> task, long firstDelay, long internal) {
        ScheduleRunnable<T> job = new ScheduleRunnable<T>();
        job.firstDelay = firstDelay;
        job.internal = internal;
        job.task = task;
        return job.go();
    }

    private class ScheduleRunnable<T> implements Runnable {

        long firstDelay;
        long internal;
        Task<T> task;

        private ScheduleBackgroundFuture<T> future;

        @Override
        public void run() {
            BackgroundTask<T> t = getBackgroundTask(task);
            t.job = this;
            future.tasks.add(t);
            t.execute();
        }

        public Future<T> go() {
            future = new ScheduleBackgroundFuture<T>(this);
            handler.postDelayed(this, firstDelay);
            return future;
        }

        @Override
        public String toString() {
            return super.toString() + " " + task;
        }
    }

    private class ScheduleBackgroundFuture<T> implements Future<T> {

        ArrayList<BackgroundTask<T>> tasks = new ArrayList<BackgroundTask<T>>();
        ScheduleRunnable<T> job;

        public ScheduleBackgroundFuture(ScheduleRunnable<T> job) {
            super();
            this.job = job;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // Log.d(TAG, "Cancel schedule tasks " + tasks + " and job " + job);
            if (job == null) {
                // already cancelled
                return false;
            }
            job.internal = -1; // prevent posting to the handler again.
            handler.removeCallbacks(job);
            job = null;
            if (tasks.isEmpty()) {
                return true;
            }
            boolean flag = false;
            for (BackgroundTask<T> task : tasks) {
                if (task.cancel(mayInterruptIfRunning)) {
                    flag = true;
                }
            }
            return flag;
        }

        @Override
        public boolean isCancelled() {
            return job == null;
        }

        @Override
        public boolean isDone() {
            return job == null;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {

            throw new UnsupportedOperationException();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "Schedule Future (" + job + ")";
        }
    }

    private class BackgroundFuture<T> implements Future<T> {

        BackgroundTask<T> task;

        public BackgroundFuture() {
        }

        public BackgroundFuture(BackgroundTask<T> task) {
            this.task = task;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (task != null) {
                return task.cancel(mayInterruptIfRunning);
            }
            return false;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (task != null) {
                return task.get();
            }
            return null;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            if (task != null) {
                return task.get(timeout, unit);
            }
            return null;
        }

        @Override
        public boolean isCancelled() {
            if (task != null) {
                return task.isCancelled();
            }
            return false;
        }

        @Override
        public boolean isDone() {
            if (task != null) {
                return task.getStatus() == Status.FINISHED;
            }
            return false;
        }

    }

    private class BackgroundTask<T> extends AsyncTask<Void, Throwable, T> {

        Task<T> task;
        ScheduleRunnable<T> job = null;

        public BackgroundTask(Task<T> task) {
            this.task = task;
        }

        @Override
        protected void onPreExecute() {
            runningTasks.put(this, System.currentTimeMillis());
            task.preExecute();
            // Log.d(TAG, "onPreExecute " + this);
        }

        @Override
        protected void onPostExecute(T result) {

            runningTasks.remove(this);
            task.postExecute(result);
            if (job != null && job.internal > 0) {
                handler.postDelayed(job, job.internal);
            }
            // Log.d(TAG, "onPostExecute " + this);
        };

        @Override
        protected T doInBackground(Void... values) {
            if (!task.isQuiet()) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        if (count.incrementAndGet() == 1) {
                            notifyLoadBegin();
                        }

                    }

                });
            }

            boolean exception = false;
            try {
                return task.doInbackground();
            } catch (Throwable e) {
                exception = true;
                publishProgress(e);
                return null;
            } finally {
                if (!task.isQuiet()) {
                    final boolean mException = exception;
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (count.decrementAndGet() == 0 && !mException) {
                                notifyLoadEnd();
                            }

                        }
                    });
                }
            }
        }

        @Override
        protected void onProgressUpdate(Throwable... values) {
            if (values.length > 0) {
                notifyBackgroundException(values[0]);
            }
        }

        @Override
        protected void onCancelled() {
            task.onCancel(true);
            runningTasks.remove(this);
        }

        @Override
        public String toString() {
            return super.toString() + " - (" + task + "count " + count + ", running tasks size - "
                    + runningTasks.size() + ")";

        }
    }

    private class TwoPhaseBackgroundTask<T> extends BackgroundTask<T> {

        TwoPhaseTask<T> tTask;

        public TwoPhaseBackgroundTask(TwoPhaseTask<T> task) {
            super(task);
            tTask = task;
        }

        @Override
        protected T doInBackground(Void... args) {
            try {
                boolean r = tTask.doFirstPhaseInBackground();
                if (!r)
                    return null;
            } catch (Exception e) {
                publishProgress(e);
                return null;
            }
            // Log.d(TAG, this + " Trying to run the second phase $$$$$$$$");
            if (!task.isQuiet()) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        if (count.incrementAndGet() == 1) {
                            notifyLoadBegin();
                        }

                    }

                });
            }

            boolean exception = false;
            try {
                final T result = task.doInbackground();
                // post the reslt whenever this task is cancelled.
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        tTask.postExecute(result);
                    }

                });
                return result;
            } catch (Throwable e) {
                exception = true;
                publishProgress(e);
                return null;
            } finally {
                if (!task.isQuiet()) {
                    final boolean mException = exception;
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (count.decrementAndGet() == 0 && !mException) {
                                notifyLoadEnd();
                            }
                        }
                    });
                }
            }
        }

        @Override
        protected void onPostExecute(T result) {

            runningTasks.remove(this);
            if (job != null && job.internal > 0) {
                handler.postDelayed(job, job.internal);
            }
            // Log.d(TAG, "onPostExecute " + this);
        };
    }

    private <T> BackgroundTask<T> getBackgroundTask(Task<T> task) {
        if (task instanceof TwoPhaseTask) {
            return new TwoPhaseBackgroundTask<T>((TwoPhaseTask<T>) task);
        }
        return new BackgroundTask<T>(task);
    }

    public void shutdown() {
        handler.removeCallbacks(checkJob);
        singleThreadService.shutdownNow();
    }
}
