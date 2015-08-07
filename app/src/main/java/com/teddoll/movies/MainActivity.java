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

package com.teddoll.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.teddoll.movies.data.Favorites;
import com.teddoll.movies.data.Movie;
import com.teddoll.movies.data.MovieProvider;
import com.teddoll.movies.network.HttpClientProvider;
import com.teddoll.movies.reciever.MovieSync;

import java.util.List;


/**
 * Main Activity of the application. Holds a single fragment for viewing a grid of movies.
 */
public class MainActivity extends AppCompatActivity implements MovieListFragment.MovieListFragmentListener {

    private MovieProvider movieProvider;

    public enum Sort {
        POP, RATE, FAVORITE
    }

    private Sort sort;
    private MovieListFragment fragment;
    private ProgressBar progress;
    private Favorites favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        favorites = Favorites.getInstance(this);
        this.progress = (ProgressBar) findViewById(R.id.progress);
        this.movieProvider = MovieProvider.getInstance(HttpClientProvider.getInstance(this).getHttpClient());
        if (savedInstanceState == null) {
            sort = Sort.POP;
        } else {
            sort = (Sort) savedInstanceState.getSerializable("sort");
        }
        this.fragment = (MovieListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);



    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        List<Movie> movies = getMovies();

        if (movies == null || movies.size() == 0) {
            updateMovies();
        } else {
            fragment.updateMovies(movies);
        }
    }

    private void updateMovies() {
        progress.setVisibility(View.VISIBLE);
        MovieSync.SyncData(HttpClientProvider.getInstance(this).getHttpClient(), movieProvider, new MovieSync.OnFetchCompleteListener() {
            @Override
            public void onComplete() {
                final List<Movie> movies = getMovies();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.updateMovies(movies);
                        progress.setVisibility(View.GONE);
                    }
                });

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("sort", sort);
    }

    private List<Movie> getMovies() {
        List<Movie> movies;
        switch (sort) {
            case POP:
            default:
                movies = movieProvider.getPopularMovies();
                break;
            case RATE:
                movies = movieProvider.getRatingMovies();
                break;
            case FAVORITE:
                movies = favorites.getFavorites();
        }
        return movies;
    }

    private void setMovies() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem sort = menu.findItem(R.id.menu_list_select);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(sort);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_array, R.layout.item_action_bar_spinner);
        adapter.setDropDownViewResource(R.layout.item_action_bar_spinner_item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sort(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setAdapter(adapter);
        switch (this.sort) {
            case POP:
                spinner.setSelection(0);
                break;
            case RATE:
                spinner.setSelection(1);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_about) {
            Intent about = new Intent(this, AboutActivity.class);
            startActivity(about);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sort(int position) {

        switch (position) {
            case 0:
                sort = Sort.POP;
                break;
            case 1:
                sort = Sort.RATE;
                break;
            case 2:
                sort = Sort.FAVORITE;
                break;
        }
        fragment.updateMovies(getMovies());
    }

    @Override
    public void onMovieSelected(Movie movie) {
        Intent info = new Intent(this, InfoActivity.class);
        info.putExtra("movie", movie);
        startActivity(info);
    }

    @Override
    public void refreshMovies() {
        updateMovies();
    }

    @Override
    public Sort getSort() {
        return sort;
    }
}
