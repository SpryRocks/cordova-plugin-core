package com.cordova.core.actions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cordova.core.PluginException;
import com.cordova.core.Registration;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Factory implements Registration {
    private final Map<String, Class<? extends BaseAction>> actions = new HashMap<>();
    private final BaseAction.Callback baseActionCallback;
    private final BaseAction.Delegate baseActionDelegate;

    public Factory(BaseAction.Callback baseActionCallback, BaseAction.Delegate baseActionDelegate) {
        this.baseActionCallback = baseActionCallback;
        this.baseActionDelegate = baseActionDelegate;
    }



    @Override
    public void registerAction(@NonNull String action, @NonNull Class<? extends BaseAction> actionType) {
        actions.put(action, actionType);
    }

    @SuppressWarnings("RedundantThrows")
    @Nullable
    public BaseAction createAction(String action, JSONArray args, CallbackContext callbackContext) throws PluginException {
        Class<? extends BaseAction> aClass = actions.get(action);
        if (aClass == null)
            return null;

        try {
            Constructor<? extends BaseAction> constructor = aClass.getConstructor(JSONArray.class);
            BaseAction baseAction = constructor.newInstance(args);
            baseAction.callback = baseActionCallback;
            baseAction.delegate = baseActionDelegate;
            baseAction.callbackContext = callbackContext;
            return baseAction;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();

            if (e instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) e;
                Throwable targetException = ite.getTargetException();
                if (targetException instanceof PluginException) {
                    throw (PluginException) targetException;
                }
            }

            throw new PluginException(e.getMessage(), e);
        }
    }
}
