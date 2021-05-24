package com.cordova.core.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cordova.core.PluginException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;

public abstract class BaseAction<TDelegate extends BaseAction.Delegate> implements Action {
    Callback callback;
    TDelegate delegate;
    CallbackContext callbackContext;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @NonNull
    private final JSONArray args;

    private final Object _lock = new Object();

    private volatile State state = State.NONE;

    protected BaseAction(@NonNull JSONArray args) {
        this.args = args;
    }

    public final void run() {
        synchronized (_lock) {
            if (!state.equals(State.NONE)) {
                return;
            }

            state = State.RUNNING;
        }

        executeActionSafe(new ExecuteAction() {
            @Override
            public void execute() throws PluginException {
                onExecute();
            }
        });
    }

    private void executeActionSafe(@NonNull ExecuteAction action) {
        try {
            action.execute();
        } catch (PluginException e) {
            resultError(e);
        } catch (Exception e) {
            resultError(new PluginException(e.getMessage(), e));
        }
    }

    protected abstract void onExecute() throws PluginException;

    @SuppressWarnings({"unused"})
    protected void resultSuccess() {
        result(new PluginResult(PluginResult.Status.OK), true);
    }

    @SuppressWarnings({"unused"})
    protected void resultSuccess(int message) {
        result(new PluginResult(PluginResult.Status.OK, message), true);
    }

    @SuppressWarnings({"unused"})
    protected void resultSuccess(@NonNull String message) {
        result(new PluginResult(PluginResult.Status.OK, message), true);
    }

    @SuppressWarnings({"unused"})
    protected void resultSuccess(@NonNull JSONObject jsonObject) {
        result(new PluginResult(PluginResult.Status.OK, jsonObject), true);
    }

    @SuppressWarnings({"unused"})
    protected void result(@NonNull JSONObject jsonObject) {
        result(new PluginResult(PluginResult.Status.OK, jsonObject), false);
    }

    @SuppressWarnings({"unused"})
    protected void resultError(@NonNull JSONObject jsonObject) {
        result(new PluginResult(PluginResult.Status.ERROR, jsonObject), true);
    }

    @SuppressWarnings({"unused"})
    protected void resultError(@NonNull String message) {
        result(new PluginResult(PluginResult.Status.ERROR, message), true);
    }

    protected void resultError(PluginException e) {
        result(getDelegate().getErrorMapper().map(e), true);
    }

    @SuppressWarnings("WeakerAccess")
    protected void result(@NonNull PluginResult pluginResult, boolean finish) {
        synchronized (_lock) {
            if (!isRunning())
                return;

            if (!finish) {
                pluginResult.setKeepCallback(true);
            }

            callbackContext.sendPluginResult(pluginResult);

            if (finish) {
                finish();
            }
        }
    }

    private void finish() {
        cancelTimeout();

        callback.finishActionSafely(this);

        state = State.FINISHED;
    }

    private boolean isRunning() {
        return state.equals(State.RUNNING);
    }

    @NonNull
    protected Context getContext() {
        return delegate.getContext();
    }

    @NonNull
    public TDelegate getDelegate() {
        return delegate;
    }

    @SuppressWarnings({"unused"})
    protected void executeAsync(final ExecuteAction action) {
        delegate.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                executeActionSafe(action);
            }
        });
    }

    public interface Callback {
        void finishActionSafely(@NonNull BaseAction action);
    }

    public interface Delegate {
        @NonNull
        Context getContext();

        @SuppressWarnings("unused")
        @NonNull
        ExecutorService getThreadPool();

        @SuppressWarnings("unused")
        @NonNull
        Activity getActivity();

        @NonNull
        Mappers.IErrorMapper getErrorMapper();

        void startActivityForResult(@NonNull Intent intent, int requestCode);

        void finishActivity(int requestCode);
    }

    private static final Object timeoutTimer_lock = new Object();
    @Nullable
    private CountDownTimer timeoutTimer;

    @SuppressWarnings("unused")
    public void setTimeout(int interval, final Runnable runnable) {
        CountDownTimer timer = new CountDownTimer(interval, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                synchronized (timeoutTimer_lock) {
                    if (timeoutTimer == this) {
                        timeoutTimer = null;
                        runnable.run();
                    }
                }
            }
        };

        synchronized (timeoutTimer_lock) {
            this.timeoutTimer = timer;
            timer.start();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void cancelTimeout() {
        synchronized (timeoutTimer_lock) {
            if (timeoutTimer != null) {
                timeoutTimer.cancel();
                timeoutTimer = null;
            }
        }
    }

    private enum State {
        NONE,
        RUNNING,
        FINISHED
    }

    public interface ExecuteAction {
        void execute() throws PluginException;
    }
}
