package com.cordova.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.cordova.core.actions.BaseAction;
import com.cordova.core.actions.Factory;
import com.cordova.core.actions.Mappers;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ExecutorService;

import static com.cordova.core.actions.LogUtils.debug;

public abstract class CordovaPlugin extends org.apache.cordova.CordovaPlugin implements BaseAction.Delegate, BaseAction.Callback {
    private final Object _actionsLockObject = new Object();
    @NonNull
    private final Factory factory;
    @NonNull
    private Mappers.IErrorMapper errorMapper = new Mappers.DefaultErrorMapper();

    protected CordovaPlugin() {
        this.factory = new Factory(this, this);
    }

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        registerActions(factory);
    }

    protected abstract void registerActions(@NonNull Registration factory);

    @SuppressWarnings("RedundantThrows")
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        debug("plugin action: " + action + ", args: " + args.toString());

        try {
            BaseAction baseAction = factory.createAction(action, args, callbackContext);
            if (baseAction == null)
                return false;

            setCurrentActionAndRunSafely(baseAction);
        } catch (PluginException e) {
            callbackContext.sendPluginResult(getErrorMapper().map(e));
        }

        return true;
    }

    @SuppressWarnings("RedundantThrows")
    private void setCurrentActionAndRunSafely(@NonNull BaseAction action) throws PluginException {
        synchronized (_actionsLockObject) {
            beforeActionRun(action);
        }

        action.run();
    }

    @Override
    public void finishActionSafely(@NonNull BaseAction action) {
        synchronized (_actionsLockObject) {
            actionFinished(action);
        }
    }

    protected abstract void beforeActionRun(@NonNull BaseAction action) throws PluginException;
    protected abstract void actionFinished(@NonNull BaseAction action);

    protected void setErrorMapper(@NonNull Mappers.IErrorMapper errorMapper) {
        this.errorMapper = errorMapper;
    }

    @NonNull
    @Override
    public Mappers.IErrorMapper getErrorMapper() {
        return errorMapper;
    }

    //region Action Delegate
    @NonNull
    @Override
    public Context getContext() {
        return cordova.getContext();
    }

    @NonNull
    @Override
    public ExecutorService getThreadPool() {
        return cordova.getThreadPool();
    }

    @NonNull
    @Override
    public Activity getActivity() {
        return cordova.getActivity();
    }

    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        cordova.startActivityForResult(this, intent, requestCode);
    }

    public void finishActivity(int requestCode) {
        cordova.getActivity().finishActivity(requestCode);
    }
    //endregion
}
