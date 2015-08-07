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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * A model object representing a Movie from themoviedb.org
 */
public class Movie implements Parcelable {

    public final boolean adult;

    @SerializedName("backdrop_path")
    public final String backdropPath;
    @SerializedName("genre_ids")
    public final int[] genreIds;

    public final int id;

    @SerializedName("original_language")
    public final String language;

    @SerializedName("original_title")
    public final String title;

    public final String overview;

    @SerializedName("release_date")
    public final String releaseDate;

    @SerializedName("poster_path")
    public final String posterPath;

    public final float popularity;

    public final boolean video;

    @SerializedName("vote_average")
    public final float voteAverage;

    @SerializedName("vote_count")
    public final int voteCount;


    public Movie(boolean adult, String backdropPath, int[] genreIds, int id, String language, String title, String overview, String releaseDate, String posterPath, float popularity, boolean video, int voteAverage, int voteCount) {
        this.adult = adult;
        this.backdropPath = backdropPath;
        this.genreIds = genreIds;
        this.id = id;
        this.language = language;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.popularity = popularity;
        this.video = video;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
    }

    public Movie(Parcel in) {
        this.adult = Boolean.parseBoolean(in.readString());
        this.backdropPath = in.readString();
        int genreIdSize = in.readInt();
        this.genreIds = new int[genreIdSize];
        in.readIntArray(this.genreIds);
        this.id = in.readInt();
        this.language = in.readString();
        this.title = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.posterPath = in.readString();
        this.popularity = in.readFloat();
        this.video = Boolean.parseBoolean(in.readString());
        this.voteAverage = in.readFloat();
        this.voteCount = in.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Movie)) {
            return false;
        }
        Movie other = (Movie) o;
        return this.id == other.id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(this.adult));
        dest.writeString(this.backdropPath);
        dest.writeInt(this.genreIds.length);
        dest.writeIntArray(this.genreIds);
        dest.writeInt(this.id);
        dest.writeString(this.language);
        dest.writeString(this.title);
        dest.writeString(this.overview);
        dest.writeString(this.releaseDate);
        dest.writeString(this.posterPath);
        dest.writeFloat(this.popularity);
        dest.writeString(String.valueOf(this.video));
        dest.writeFloat(this.voteAverage);
        dest.writeInt(this.voteCount);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };


}
