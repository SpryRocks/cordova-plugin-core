package com.cordova.core.actions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cordova.core.PluginException;

@SuppressWarnings("unused")
public class CurrentAction<TAction extends Action> {
    final Object _currentActionLock;
    @Nullable
    TAction currentAction;

    @SuppressWarnings("WeakerAccess")
    public CurrentAction(@NonNull Object currentActionLock) {
        _currentActionLock = currentActionLock;
    }

    public void set(@NonNull TAction action) throws PluginException {
        synchronized (_currentActionLock) {
            if (this.currentAction != null) {
                throw new PluginException("The action is already running");
            }

            this.currentAction = action;
        }
    }

    public void clear(@NonNull TAction action) {
        synchronized (_currentActionLock) {
            if (this.currentAction == action) {
                this.currentAction = null;
            }
        }
    }

    @Nullable
    public TAction get() {
        synchronized (_currentActionLock) {
            return currentAction;
        }
    }

    public static class Cancelable<TAction extends Action.Cancelable> extends CurrentAction<TAction> {
        public Cancelable(@NonNull Object currentActionLock) {
            super(currentActionLock);
        }

        public void cancel() {
            TAction currentAction;
            synchronized (_currentActionLock) {
                if (this.currentAction == null) {
                    return;
                }
                currentAction = this.currentAction;
            }
            currentAction.cancel();
        }
    }
}
