package com.poalim.exam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.realm.Realm;

/**
 * Created by Shahar on 27/08/2019.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    private Context mContext;
    private List<Movie> moviesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, release_date;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            release_date = (TextView) view.findViewById(R.id.release_date);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public MoviesAdapter(Context mContext, List<Movie> albumList) {
        this.mContext = mContext;
        this.moviesList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Movie movie = moviesList.get(position);

        holder.title.setText(movie.title);
        holder.release_date.setText(movie.release_date);

        // loading movie poster using Glide library
        Glide.with(mContext).load("https://image.tmdb.org/t/p/original" + movie.poster_path).into(holder.thumbnail);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, movie);
            }
        });

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMovieInfoActivity(movie);
            }
        });
    }

    /**
     * Go to movie information activity
     * @param movie
     */
    private void goToMovieInfoActivity(Movie movie) {
        Intent movieActivity = new Intent(mContext, MovieActivity.class);
        movieActivity.putExtra("title", movie.title);
        movieActivity.putExtra("release_date", movie.release_date);
        movieActivity.putExtra("overview", movie.overview);
        movieActivity.putExtra("poster_path", movie.poster_path);
        movieActivity.putExtra("vote_average", movie.vote_average);
        mContext.startActivity(movieActivity);
        ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, Movie movie) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_movie, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(movie));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        Movie selected_movie;

        public MyMenuItemClickListener(Movie movie) {
            selected_movie = movie;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.like:
                    addMovieToSavedMovies(selected_movie);
                    return true;
                case R.id.movie_info:
                    goToMovieInfoActivity(selected_movie);
                    return true;
                default:
            }
            return false;
        }
    }

    private void addMovieToSavedMovies(Movie movie) {
        try {
            Realm realm=Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.insertOrUpdate(movie);
            realm.commitTransaction();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}