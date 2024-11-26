package com.example.upnext;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.upnext.Models.NewsApiResponse;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RequestManager {
    Context context;
    Retrofit retrofit;

    public RequestManager(Context context) {
        this.context = context;
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .build();
                    return chain.proceed(request);
                })
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        // Create Retrofit instance without logging
        retrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/v2/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // Gson for JSON parsing
                .build();
    }

    public void getNewsHeadlines(onFetchDataListener<NewsApiResponse> listener, String category, String query) {
        CallNewsApi callNewsApi = retrofit.create(CallNewsApi.class);

        String apiKey = context.getString(R.string.api_key);
        Log.d("RequestManager", "API Key: " + apiKey);

        Call<NewsApiResponse> call = callNewsApi.callHeadlines("us", category, query, apiKey);

        Log.d("RequestManager", "Constructed URL: " + call.request().url());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<NewsApiResponse> call, @NonNull Response<NewsApiResponse> response) {
                Log.d("RequestManager", "Response Code: " + response.code());
                Log.d("RequestManager", "Response Message: " + response.message());

                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("RequestManager", "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("RequestManager", "Error reading error body", e);
                    }
                    listener.onError("Error: Response not successful. Code: " + response.code());
                    return;
                }

                if (response.body() == null) {
                    Log.e("RequestManager", "Response body is null");
                    listener.onError("Response body is null");
                    return;
                }

                listener.onFetchData(response.body().getArticles(), response.message());
            }

            @Override
            public void onFailure(Call<NewsApiResponse> call, Throwable throwable) {
                Log.e("RequestManager", "Request failed: " + throwable.getMessage());
                listener.onError("Request failed: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });
    }

    public interface CallNewsApi {
        @GET("top-headlines")
        Call<NewsApiResponse> callHeadlines(
                @Query("country") String country,
                @Query("category") String category,
                @Query("q") String query,
                @Query("apiKey") String api_key
        );
    }
}
