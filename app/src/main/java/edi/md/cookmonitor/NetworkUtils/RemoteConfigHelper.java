package edi.md.cookmonitor.NetworkUtils;
import android.content.Context;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Igor on 12.05.2020
 */

public class RemoteConfigHelper {
    public static String KEY_COMPANY_NAME = "star_kebab";

    public interface OnCompaniesCheckListener{
        void onCompaniesCheckListener();
    }

    public static Builder with(Context context){
        return new Builder(context);
    }

    private OnCompaniesCheckListener onCompaniesCheckListener;
    private Context context;

    public RemoteConfigHelper(Context context, OnCompaniesCheckListener onCompaniesCheckListener) {
        this.onCompaniesCheckListener = onCompaniesCheckListener;
        this.context = context;
    }

    public void check(){
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        Map<String,Object> defaultValue = new HashMap<>();
        defaultValue.put(RemoteConfigHelper.KEY_COMPANY_NAME,true);

        remoteConfig.setDefaultsAsync(defaultValue);

        onCompaniesCheckListener.onCompaniesCheckListener();
    }

    public static class Builder{

        private Context context;
        private OnCompaniesCheckListener onCompaniesCheckListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onCheckList (OnCompaniesCheckListener onCompaniesCheckListener){
            this.onCompaniesCheckListener = onCompaniesCheckListener;
            return this;
        }

        public RemoteConfigHelper build(){
            return new RemoteConfigHelper(context,onCompaniesCheckListener);
        }

        public RemoteConfigHelper check(){
            RemoteConfigHelper helper = build();
            helper.check();

            return helper;
        }
    }
}
