/*
 * Copyright 2015 Theodore Doll
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.teddoll.movies.reciever;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teddoll.movies.config.Config;
import com.teddoll.movies.data.Movie;
import com.teddoll.movies.data.MovieProvider;
import com.teddoll.movies.data.Video;
import com.teddoll.movies.network.HttpClientProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MovieSync extends BroadcastReceiver {

    private static final String POP_URL = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + Config.API_KEY;
    private static final String RATE_URL = "http://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&api_key=" + Config.API_KEY;
    private static final String GENRE_URL = "http://api.themoviedb.org/3/genre/movie/list?api_key=" + Config.API_KEY;
    private static final String VIDEO_URL = "http://api.themoviedb.org/3/movie/%s/videos?api_key=" + Config.API_KEY;

    private static final long INTERVAL = AlarmManager.INTERVAL_DAY;

    public interface OnFetchCompleteListener {
        void onComplete();
    }


    private static int requestCount;
    private static String pop;
    private static String rate;
    private static String genres;
    private static boolean inProcess;

    public static synchronized void SyncData(final OkHttpClient client,
                                             final MovieProvider movieCache,
                                             final OnFetchCompleteListener listener) {
        if (inProcess) return;
        inProcess = true;

        Request popRequest = new Request.Builder()
                .url(POP_URL)
                .addHeader("Accept", "application/json")
                .build();
        Request rateRequest = new Request.Builder()
                .url(RATE_URL)
                .addHeader("Accept", "application/json")
                .build();
        Request genresRequest = new Request.Builder()
                .url(GENRE_URL)
                .addHeader("Accept", "application/json")
                .build();
        requestCount = 3;
        pop = null;
        rate = null;
        genres = null;
        client.newCall(popRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                onComplete(movieCache, listener);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray array = json.optJSONArray("results");
                        pop = array != null ? array.toString() : null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                onComplete(movieCache, listener);
            }
        });

        client.newCall(rateRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                onComplete(movieCache, listener);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray array = json.optJSONArray("results");
                        rate = array != null ? array.toString() : null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                onComplete(movieCache, listener);
            }
        });
        client.newCall(genresRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                onComplete(movieCache, listener);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray array = json.optJSONArray("genres");
                        genres = array != null ? array.toString() : null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                onComplete(movieCache, listener);
            }
        });


    }

    public interface OnGetVideosListener {
        void onVideos(List<Video> videos);
    }

    public static void getVideos(final OkHttpClient client, Movie movie, final OnGetVideosListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("OnGetVideosListener cannot be null");
        Request request = new Request.Builder()
                .url(String.format(Locale.US, VIDEO_URL, movie.id))
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                listener.onVideos(new ArrayList<Video>(0));
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray array = json.optJSONArray("results");
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<Video>>() {
                        }.getType();
                        List<Video> vids = gson.fromJson(array.toString(), type);
                        listener.onVideos(vids);
                    } catch (JSONException e) {
                        listener.onVideos(new ArrayList<Video>(0));
                    }
                } else {
                    listener.onVideos(new ArrayList<Video>(0));
                }

            }
        });
    }

    private static synchronized void onComplete(MovieProvider cache, OnFetchCompleteListener listener) {
        if (--requestCount <= 0) {
            cache.updateData(pop, rate, genres);
            inProcess = false;
            if (listener != null) {
                listener.onComplete();
            }
        }
    }


    public static String[] loadCachedData(final OkHttpClient client) {
        String pop = "[]";
        String rate = "[]";
        String genre = "[]";
        Request popRequest = new Request.Builder()
                .cacheControl(new CacheControl.Builder()
                        .onlyIfCached().build())
                .url(POP_URL)
                .addHeader("Accept", "application/json")
                .build();
        try {
            Response forcePopCacheResponse = client.newCall(popRequest).execute();
            if (forcePopCacheResponse.code() != 504) {
                JSONObject json = new JSONObject(forcePopCacheResponse.body().string());
                JSONArray array = json.optJSONArray("results");
                pop = array != null ? array.toString() : null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        Request rateRequest = new Request.Builder()
                .cacheControl(new CacheControl.Builder()
                        .onlyIfCached().build())
                .url(RATE_URL)
                .addHeader("Accept", "application/json")
                .build();
        try {
            Response forceRateCacheResponse = client.newCall(rateRequest).execute();
            if (forceRateCacheResponse.code() != 504) {
                JSONObject json = new JSONObject(forceRateCacheResponse.body().string());
                JSONArray array = json.optJSONArray("results");
                rate = array != null ? array.toString() : null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        Request genreRequest = new Request.Builder()
                .cacheControl(new CacheControl.Builder()
                        .onlyIfCached().build())
                .url(GENRE_URL)
                .addHeader("Accept", "application/json")
                .build();
        try {
            Response forceGenreCacheResponse = client.newCall(genreRequest).execute();
            if (forceGenreCacheResponse.code() != 504) {
                JSONObject json = new JSONObject(forceGenreCacheResponse.body().string());
                JSONArray array = json.optJSONArray("genres");
                genre = array != null ? array.toString() : null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return new String[]{pop, rate, genre};
    }

    public static void startTimer(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, com.teddoll.movies.reciever.MovieSync.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis() + INTERVAL, INTERVAL, pi);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        OkHttpClient client = HttpClientProvider.getInstance(context).getHttpClient();
        SyncData(client, MovieProvider.getInstance(client), null);
    }
}
