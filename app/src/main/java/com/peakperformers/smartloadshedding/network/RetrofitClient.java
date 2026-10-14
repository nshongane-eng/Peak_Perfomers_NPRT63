package com.peakperformers.smartloadshedding.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Default API base URL. Can be updated when deploying to Render / Railway / cloud.
    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:3000/";
    private static final String PREFS_NAME = "network_settings";
    private static final String KEY_BASE_URL = "custom_base_url";

    private static Retrofit retrofit;
    private static String activeBaseUrl = DEFAULT_BASE_URL;

    public static synchronized ApiService getApiService() {
        return getApiService(null);
    }

    public static synchronized ApiService getApiService(Context context) {
        String baseUrl = getBaseUrl(context);

        if (retrofit == null || !baseUrl.equals(activeBaseUrl)) {
            activeBaseUrl = baseUrl;

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Resilient timeouts prevent UI hangs when deployed without an active backend
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(activeBaseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static String getBaseUrl(Context context) {
        if (context != null) {
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_BASE_URL, activeBaseUrl);
        }
        return activeBaseUrl;
    }

    public static synchronized void setBaseUrl(Context context, String newUrl) {
        if (newUrl == null || newUrl.trim().isEmpty()) {
            newUrl = DEFAULT_BASE_URL;
        }
        newUrl = newUrl.trim();
        if (!newUrl.endsWith("/")) {
            newUrl += "/";
        }

        if (context != null) {
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_BASE_URL, newUrl).apply();
        }

        activeBaseUrl = newUrl;
        retrofit = null; // Forces rebuild with new base URL
    }
}