package com.cordova.core;

import androidx.annotation.NonNull;

import com.cordova.core.actions.BaseAction;


public interface Registration {
    void registerAction(@NonNull String action, @NonNull Class<? extends BaseAction> actionType);
}
