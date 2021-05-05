package com.cordova.core.actions;

import androidx.annotation.NonNull;

import com.cordova.core.PluginException;
import com.google.gson.Gson;

import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("WeakerAccess")
public class Mappers {
    @NonNull
    public static JSONObject mapObjectToJson(@NonNull Object src) throws PluginException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(src);
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new PluginException("Cannot convert object to json", e);
        }
    }

    public interface IErrorMapper {
        @NonNull
        PluginResult map(@NonNull PluginException error);
    }

    public static class DefaultErrorMapper implements IErrorMapper {
        @NonNull
        @Override
        public PluginResult map(@NonNull PluginException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
        }
    }
}
