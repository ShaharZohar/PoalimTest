package com.poalim.exam;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView m_recyclerView;
    private MoviesAdapter m_adapter;
    private List<Movie> m_moviesList;
    private boolean isLoading = false;
    private int m_page = 1;        // used for paging while scrolling down
    private int m_current_tab = 0; // 0 - all latest movies
                                   // 1 - saved movies

    private APIInterface m_apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initiate Realm database
        Realm.init(this);

        // Initiate collapsing toolbar
        initCollapsingToolbar();

        // Create api client for https requests
        m_apiInterface = APIClient.getClient().create(APIInterface.class);

        m_recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        m_moviesList = new ArrayList<>();
        m_adapter = new MoviesAdapter(this, m_moviesList);

        // Initiate recyclerView
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        m_recyclerView.setLayoutManager(mLayoutManager);
        m_recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        m_recyclerView.setItemAnimator(new DefaultItemAnimator());
        m_recyclerView.setAdapter(m_adapter);

        // Initiate scroller listener, used for paging
        initScrollListener();

        try {
            Glide.with(this).load(R.drawable.cover_img).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get latest movies - first page
        getLatestMovies(m_page);

    }

    private void initScrollListener() {
        m_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(m_current_tab == 0) // load more movies only if current tab is - "all latest movies" tab
                {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                    if (!isLoading) {
                        if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == m_moviesList.size() - 1) {
                            //bottom of list!
                            getLatestMovies(m_page);
                        }
                    }
                }

            }
        });
    }

    private void setGridAdapter() {
        m_moviesList = new ArrayList<>();
        m_adapter = new MoviesAdapter(this, m_moviesList);
        m_recyclerView.setAdapter(m_adapter);
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.backdrop_title));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    public void getLatestMovies(int page) {

        isLoading = true;

        /**
         GET Latest Movies List
         **/

        // Calculate date range for discover api, in order to find the latest movies from the past year.

        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("yyy-MM-dd");

        String lte = date.format(currentLocalTime);

        cal.add(Calendar.YEAR, -1); // to get previous year add -1
        Date pastYearTime = cal.getTime();
        String gte = date.format(pastYearTime);

        Log.d("test ", "from " + gte + " to " + lte);

        Call<MultipleResource> call = m_apiInterface.getMovies(gte, lte, page);
        call.enqueue(new Callback<MultipleResource>() {
            @Override
            public void onResponse(Call<MultipleResource> call, Response<MultipleResource> response) {


                Log.d("TAG",response.code()+"");

                String displayResponse = "";

                MultipleResource resource = response.body();
                Integer text = resource.page;
                Integer total = resource.total;
                Integer totalPages = resource.totalPages;
                List<Movie>moviesList = resource.results;

                displayResponse += text + " Page\n" + total + " Total\n" + totalPages + " Total Pages\n";

                for (Movie movie : moviesList) {
                    displayResponse += movie.id + " " + movie.title + " " + movie.poster_path + " " + movie.release_date + " " + movie.overview + " " + movie.vote_average + "\n";

                    Movie a = new Movie(movie.id, movie.title, movie.release_date, movie.poster_path, movie.overview, movie.vote_average);
                    MainActivity.this.m_moviesList.add(a);
                }

                Log.d("test ", displayResponse);

                m_adapter.notifyDataSetChanged();

                isLoading = false;

                if((m_page+1) < total)
                    m_page++;

            }

            @Override
            public void onFailure(Call<MultipleResource> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Get saved movies from realm local database
     */
    public void getSavedMovies() {
        try {
            Realm realm=Realm.getDefaultInstance();
            RealmResults<Movie> realmModels=realm.where(Movie.class).findAll();

            String displayResponse = "";

            for ( Movie movie : realmModels ) {
                displayResponse += movie.id + " " + movie.title + " " + movie.poster_path + " " + movie.release_date + " " + movie.overview + " " + movie.vote_average + "\n";

                Movie a = new Movie(movie.id, movie.title, movie.release_date, movie.poster_path, movie.overview, movie.vote_average);
                m_moviesList.add(a);
            }

            Log.d("test ", displayResponse);

            m_adapter.notifyDataSetChanged();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle toolbar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // All latest movies from TMDb
        if (id == R.id.all_movies) {
            m_current_tab = 0;
            setGridAdapter();
            m_page = 1;
            getLatestMovies(m_page);

        // Al saved movies from realm local database
        } else if (id == R.id.saved_movies) {
            m_current_tab = 1;
            setGridAdapter();
            getSavedMovies();
        }
            
        return super.onOptionsItemSelected(item);
    }
}