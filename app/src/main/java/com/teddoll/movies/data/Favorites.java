package com.teddoll.movies.data;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Favorites {

    private static Favorites instance;
    private static final String FILE = "faves.json";
    private Context appContext;
    private List<Movie> favorites;
    private final Object lock;

    public static synchronized Favorites getInstance(Context context) {
        if (instance == null) {
            instance = new Favorites(context);
        }
        return instance;
    }

    private Favorites(Context context) {
        appContext = context.getApplicationContext();
        lock = new Object();
        load();
        if(favorites == null) favorites = new ArrayList<>(0);
    }

    public void addFavorite(Movie movie) {
        synchronized (lock) {
            if (!favorites.contains(movie)) {
                favorites.add(movie);
            }
        }
    }

    public void removeFavorite(Movie movie) {
        synchronized (lock) {
            favorites.remove(movie);
        }
    }

    public boolean hasFavorite(Movie movie) {
        synchronized (lock) {
            return favorites.contains(movie);
        }
    }

    public List<Movie> getFavorites() {
        synchronized (lock) {
            return Collections.unmodifiableList(favorites);
        }
    }

    public void save() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                File file = new File(appContext.getFilesDir(), FILE);
                String data = gson.toJson(favorites);
                try {
                    FileUtils.write(file, data);
                } catch (IOException e) {
                    //TODO clean up.
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void load() {
        File file = new File(appContext.getFilesDir(), FILE);
        String rawData;
        try {

            if (!file.exists()) {
                rawData = "[]";
                FileUtils.write(file, rawData);
            } else {
                rawData = FileUtils.readFileToString(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            rawData = "[]";
        }
        popData(rawData);

    }

    private void popData(String data) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Movie>>() {
        }.getType();
        favorites = gson.fromJson(data, type);
    }


}
