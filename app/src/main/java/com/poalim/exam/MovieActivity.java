package com.poalim.exam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MovieActivity extends AppCompatActivity {

    String m_title = "";
    String m_overview = "";
    String m_release_date = "";
    String m_poster_path = "";
    float  m_vote_average;

    ImageView ivPoster;
    TextView tvTitle, tvOverview, tvVoteAverage, tvReleaseDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        // Initiate view
        ivPoster = (ImageView) findViewById(R.id.ivPoster);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        tvVoteAverage = (TextView) findViewById(R.id.tvVoteAverage);
        tvReleaseDate = (TextView) findViewById(R.id.tvReleaseDate);

        Intent intent = getIntent();

        if(intent != null)
        {
            m_title = intent.getStringExtra("title");
            m_overview = intent.getStringExtra("overview");
            m_release_date = intent.getStringExtra("release_date");
            m_poster_path = intent.getStringExtra("poster_path");
            m_vote_average = intent.getFloatExtra("vote_average", 0);

            // Cut the year from the release date
            String year = m_release_date.split("-")[0];

            // Loading movie poster using Glide library
            Glide.with(this).load("https://image.tmdb.org/t/p/original" + m_poster_path).into(ivPoster);

            tvTitle.setText(m_title);
            tvOverview.setText(m_overview);
            tvVoteAverage.setText(m_vote_average + "");
            tvReleaseDate.setText(year); //show only the year
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Add activity transitions
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
