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
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.teddoll.movies.data.Movie;
import com.teddoll.movies.data.MovieProvider;
import com.teddoll.movies.data.Video;
import com.teddoll.movies.network.HttpClientProvider;
import com.teddoll.movies.reciever.MovieSync;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment for viewing a Movies details.
 */
public class InfoActivityFragment extends Fragment {

    private Movie movie;

    public InfoActivityFragment() {
    }

    public static InfoActivityFragment newInstance(Movie movie) {
        InfoActivityFragment fragment = new InfoActivityFragment();
        Bundle b = new Bundle();
        b.putParcelable("movie", movie);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movie = getArguments().getParcelable("movie");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_info, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView overview = (TextView) view.findViewById(R.id.overview);
        TextView rate = (TextView) view.findViewById(R.id.rating);
        TextView genre = (TextView) view.findViewById(R.id.genres);
        TextView dateText = (TextView) view.findViewById(R.id.date);
        ImageView poster = (ImageView) view.findViewById(R.id.poster);

        title.setText(movie.title);
        overview.setText(movie.overview);
        rate.setText(getString(R.string.rating_format, movie.voteAverage));
        if (movie.releaseDate != null) {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outFormat = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
            String dateOut;
            try {
                Date date = inFormat.parse(movie.releaseDate);
                dateOut = outFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                dateOut = movie.releaseDate;
            }
            dateText.setText(dateOut);
        }

        if (movie.genreIds != null && movie.genreIds.length > 0) {
            int[] gIds = movie.genreIds;
            StringBuilder genreOut = new StringBuilder();
            Map<Integer, String> genreMap = MovieProvider.getInstance(
                    HttpClientProvider.getInstance(getActivity()).getHttpClient()).getGenres();
            for (int i = 0; i < gIds.length - 1; i++) {
                String genreString = getGenre(genreMap, gIds[i]);
                if (!genreString.isEmpty()) {
                    genreOut.append(genreString).append(", ");
                }
            }
            if (gIds.length > 0) {
                genreOut.append(getGenre(genreMap, gIds[gIds.length - 1]));
            }
            genre.setText(genreOut.toString());
        }
        String url = "http://image.tmdb.org/t/p/w500/" + movie.posterPath;

        Picasso.with(getActivity()).load(url).into(poster);



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        MovieSync.getVideos(HttpClientProvider.getInstance(getActivity()).getHttpClient(), movie, new MovieSync.OnGetVideosListener() {
            @Override
            public void onVideos(final List<Video> videos) {
                Activity context = getActivity();
                View view = getView();
                if (view == null || context == null) return;
                final LinearLayout videosList = (LinearLayout) view.findViewById(R.id.videos);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                        int height = getResources().getDimensionPixelSize(R.dimen.trailer_item_height);
                        int lineHeight = getResources().getDimensionPixelSize(R.dimen.linear_line_break);
                        for (int i = 0; i < videos.size() - 1; i++) {
                            addVideoView(videos.get(i), videosList, inflater, height);
                            addSeparator(videosList, lineHeight);
                        }
                        if (videos.size() > 0) {
                            addVideoView(videos.get(videos.size() - 1), videosList, inflater, height);
                        }

                    }
                });

            }
        });


    }

    private void addSeparator(LinearLayout parent, int height) {
        View line = new View(getActivity());
        line.setBackgroundColor(Color.parseColor("#88444444"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        line.setLayoutParams(params);
        parent.addView(line);
    }

    private void addVideoView(final Video video, LinearLayout parent, LayoutInflater inflater, int height) {
        View itemView = inflater.inflate(R.layout.item_video, parent, false);
        TextView title = (TextView) itemView.findViewById(R.id.video_title);
        title.setText(video.name);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        itemView.setLayoutParams(params);
        parent.addView(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://www.youtube.com/watch?v=" + video.key));
                startActivity(i);
            }
        });
    }

    private String getGenre(Map<Integer, String> genreMap, int id) {
        String genre = genreMap.get(id);
        return genre != null ? genre : "";
    }



}
