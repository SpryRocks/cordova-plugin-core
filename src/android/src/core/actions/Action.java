package com.cordova.core.actions;

public interface Action {
    interface Cancelable extends Action {
        void cancel();
    }
}
