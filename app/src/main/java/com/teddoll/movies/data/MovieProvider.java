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

package com.teddoll.movies.data;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.teddoll.movies.reciever.MovieSync;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache for Movie list backed by a flat file.
 */
public class MovieProvider {

    private static MovieProvider instance;

    private ArrayList<Movie> pop;
    private ArrayList<Movie> rate;
    private HashMap<Integer, String> genres;

    private final OkHttpClient httpClient;
    private final Object lock;

    public static MovieProvider getInstance(OkHttpClient httpClient) {
        if (instance == null) {
            instance = new MovieProvider(httpClient);
        }
        return instance;
    }

    private MovieProvider(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.lock = new Object();
        load();
        if (pop == null) {
            pop = new ArrayList<>(0);
        }
        if (rate == null) {
            rate = new ArrayList<>(0);
        }
        if (genres == null) {
            genres = new HashMap<>();
        }
    }

    public List<Movie> getPopularMovies() {
        synchronized (lock) {
            return Collections.unmodifiableList(pop);
        }
    }

    public List<Movie> getRatingMovies() {
        synchronized (lock) {
            return Collections.unmodifiableList(rate);
        }
    }

    public Map<Integer, String> getGenres() {
        synchronized (lock) {
            return Collections.unmodifiableMap(genres);
        }
    }

    public void updateData(String rawPopJSON, String rawRateJSON, String rawGenre) {
        synchronized (lock) {
            loadFromJson(rawPopJSON, rawRateJSON, rawGenre);
        }

    }


    private void load() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    String[] data = MovieSync.loadCachedData(httpClient);
                    loadFromJson(data[0], data[1], data[2]);
                }
            }
        }).start();

    }

    private void loadFromJson(String pop, String rate, String genre) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Movie>>() {
        }.getType();
        if (pop != null)
            this.pop = gson.fromJson(pop, type);
        if (rate != null)
            this.rate = gson.fromJson(rate, type);

        loadGenres(genre);
    }

    private void loadGenres(String genresJson) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        ArrayList<Genre> genreList = gson.fromJson(genresJson, type);
        genres = new HashMap<>();
        if (genreList != null) {
            for (int i = 0; i < genreList.size(); i++) {
                Genre g = genreList.get(i);
                genres.put(g.id, g.name);
            }
        }
    }


}
