package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;


/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */

public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    int currentPosition;
    private Bundle mReenterPositions;
    static final String START_POSITION = "extra_start_position";
    static final String CURRENT_POSITION = "extra_current_position";
    int startPosition;
    Cursor theCursor;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        mReenterPositions = data.getExtras();
        currentPosition = mReenterPositions.getInt(CURRENT_POSITION);
        int startPosition = mReenterPositions.getInt(START_POSITION);
        if (startPosition != currentPosition) {
           postponeEnterTransition();
            mRecyclerView.scrollToPosition(currentPosition);

        }

        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                mRecyclerView.requestLayout();
                startPostponedEnterTransition();
                return true;
            }
        });

    }

    private final SharedElementCallback mCallbackExit = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mReenterPositions != null) {
                startPosition = mReenterPositions.getInt(START_POSITION);
                currentPosition = mReenterPositions.getInt(CURRENT_POSITION);
                if (startPosition != currentPosition) {
                    theCursor.moveToPosition(currentPosition);
                    String transitionNewName = toString().valueOf(theCursor.getLong(ArticleLoader.Query._ID)) + currentPosition;
                    View newSharedElement = findViewById(R.id.thumbnail);


                    if (newSharedElement != null) {
                        names.clear();
                        names.add(transitionNewName);
                        sharedElements.clear();
                        sharedElements.put(transitionNewName, newSharedElement);
                    }
                    mReenterPositions = null;
                } else {

                    View navBar = findViewById(android.R.id.navigationBarBackground);
                    View statusBar = findViewById(android.R.id.statusBarBackground);
                    if (navBar != null) {
                        names.add(navBar.getTransitionName());
                        sharedElements.put(navBar.getTransitionName(), navBar);
                    }
                    if (statusBar != null) {
                        names.add(statusBar.getTransitionName());
                        sharedElements.put(statusBar.getTransitionName(), statusBar);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setExitSharedElementCallback(mCallbackExit);
        if (Build.VERSION.SDK_INT >= 21) {
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.shared_element_photo);
            getWindow().setSharedElementEnterTransition(transition);

        }


        setContentView(R.layout.activity_article_list);



        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }


    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ArticleList Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.xyzreader.ui/http/host/path")
        );


    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }


    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };


    private void updateRefreshingUI() {
        //  mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        theCursor = cursor;
        Adapter adapter = new Adapter(cursor, this);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager glm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(glm);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }


    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
        private Context context;
        View view;

        public Adapter(Cursor cursor, Context ctx) {
            mCursor = cursor;
            context = ctx;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    currentPosition = vh.getAdapterPosition();
                    mRecyclerView.getLayoutManager().findViewByPosition(currentPosition).setTag("article_photo" + currentPosition);
                    Intent intentToDetailActivity = new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())));
                    if (Build.VERSION.SDK_INT >= 21) {
                        intentToDetailActivity.putExtra(START_POSITION, vh.getAdapterPosition());
                        view.findViewById(R.id.thumbnail).setTransitionName(toString().valueOf(vh.getItemId()) + vh.getAdapterPosition());


                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, view.findViewById(R.id.thumbnail),
                                view.findViewById(R.id.thumbnail).getTransitionName());
                        startActivity(intentToDetailActivity, options.toBundle());
                    } else

                    {
                        startActivity(intentToDetailActivity);
                    }
                }
            });

            return vh;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            currentPosition = position;

            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            String subTitle = DateUtils.getRelativeTimeSpanString(
                    mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString()
                    + " by "
                    + mCursor.getString(ArticleLoader.Query.AUTHOR);

            holder.subtitleView.setText(subTitle);

            Picasso.with(context).load(mCursor.getString(ArticleLoader.Query.THUMB_URL)).into(holder.thumbnailView);
            holder.thumbnailView.setTag(holder.getItemId() + position);

            // holder.thumbnailView.setTag("article_photo" +position);
            // Log.d("WWWW",holder.thumbnailView.toString());
        /*    holder.thumbnailView.setImageUrl(
                   mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
            */
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);

        }

    }
}



