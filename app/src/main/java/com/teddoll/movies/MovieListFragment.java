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

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teddoll.movies.data.Movie;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment for diplaying movies in a grid.
 */
public class MovieListFragment extends Fragment {

    public interface MovieListFragmentListener {
        void onMovieSelected(Movie movie);

        void refreshMovies();
    }

    private MovieListFragmentListener listener;
    private RecyclerView grid;
    private int colunms;
    private View error;

    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.colunms = 3;
        } else {
            this.colunms = 2;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.listener = (MovieListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(activity.getClass().getSimpleName() + " Must implement MovieListFragmentListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        error = view.findViewById(R.id.error_container);
        error.setVisibility(View.GONE);
        view.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.refreshMovies();
            }
        });
        grid = (RecyclerView) view.findViewById(R.id.recyclerview);
        grid.setHasFixedSize(true);
        grid.setLayoutManager(new GridLayoutManager(getActivity(), this.colunms));
        grid.setAdapter(new MyAdapter(null, getActivity(), listener));
        return view;
    }

    public void updateMovies(List<Movie> movies) {
        if (movies == null || movies.size() == 0) {
            error.setVisibility(View.VISIBLE);

        } else {
            error.setVisibility(View.GONE);
        }
        grid.setAdapter(new MyAdapter(movies, getActivity(), listener));
    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<Movie> movies;

        private final Context context;

        private final MovieListFragmentListener listener;


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // each data item is just a string in this case

            public interface OnViewHolderClick {
                void onClick(int position);
            }

            public final TextView title;
            public final ImageView poster;
            private final OnViewHolderClick listener;
            public int position;

            public ViewHolder(View v, OnViewHolderClick listener) {
                super(v);
                title = (TextView) v.findViewById(R.id.title);
                poster = (ImageView) v.findViewById(R.id.poster);
                this.listener = listener;
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                listener.onClick(position);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(List<Movie> movies, Context c, MovieListFragmentListener listener) {
            this.movies = movies;
            this.context = c;
            this.listener = listener;
            if (this.movies == null)
                this.movies = new ArrayList<>(0);

        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_movie_poster, parent, false);

            return new ViewHolder(v, new ViewHolder.OnViewHolderClick() {
                @Override
                public void onClick(int position) {
                    listener.onMovieSelected(movies.get(position));
                }
            });

        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Movie m = movies.get(position);
//            holder.name.setText(m.name);
            holder.position = position;
            String url = "http://image.tmdb.org/t/p/w500/" + m.posterPath;
            Log.d("IMAGE", url);
            Picasso.with(context).load(url).into(holder.poster);

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return movies.size();
        }
    }
}
