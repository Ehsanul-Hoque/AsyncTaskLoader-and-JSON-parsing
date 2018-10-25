package com.ehsanfahad.jsonparsingtest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>
{

    private static final int LOADER_ID = 521;   // A random id.
    private static String newsUrl = "https://newsapi.org/v2/everything?q=bitcoin&from=2018-09-25&sortBy=publishedAt&apiKey=c415b8d543824f7dab24080a22c502b4";

    private TextView textView_news;
    private ProgressBar progressBar_news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views by id
        textView_news = findViewById(R.id.textView_news);
        progressBar_news = findViewById(R.id.progressBar_news);

        // Destroy any previous loader with the same id
        getSupportLoaderManager().destroyLoader(LOADER_ID);

        // Initiate new AsyncTaskLoader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args)
    {
        // Show the progress indicator
        progressBar_news.setVisibility(View.VISIBLE);

        // Return our extended AsyncTaskLoader class
        return new AsyncGetNews(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data)
    {
        // Load finished.
        if (data == null) {
            Toast.makeText(this, "Error loading from server", Toast.LENGTH_SHORT).show();
            Log.e("LOG_MESSAGES", "onLoadFinished: Error loading from server. data is null");

        } else {
            parseJsonFromString(data);
        }

        // Hide the progress indicator
        progressBar_news.setVisibility(View.GONE);

        // Destroy loader
        getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader)
    {
        // Nothing here, for now
    }


    private void parseJsonFromString(String data)
    {
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray_articles = jsonObject.getJSONArray("articles");

            for (int i = 0, len = jsonArray_articles.length(); i < len; i++) {
                JSONObject jsonObject_singleArticle = jsonArray_articles.getJSONObject(i);
                String article = "";

                article += jsonObject_singleArticle.getString("title");
                article += "\n[news by " + jsonObject_singleArticle.getString("author") + "]";
                article += "\n\n" + jsonObject_singleArticle.getString("content");
                article += "\n\n------------------------------------------------------------------\n\n\n\n";

                textView_news.append(article);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static class AsyncGetNews extends AsyncTaskLoader< String >
    {
        AsyncGetNews(@NonNull Context context) {
            super(context);
        }

        @Nullable
        @Override
        public String loadInBackground() {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            // TODO : Uncomment if you want to make a post request with parameters
            /*RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_id", "1")
                    .addFormDataPart("quiz_id", "2")
                    .addFormDataPart("thumbs", "3")
                    .build();*/

            Request request = new Request.Builder()
                    .url(newsUrl)
                    //.post(requestBody)    // TODO : Uncomment if you want to make a post request
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }

            } catch (Exception e) {
                Log.e("LOG_MESSAGES", "loadInBackground: Exception!", e);
            }

            return null;
        }
    }

}
