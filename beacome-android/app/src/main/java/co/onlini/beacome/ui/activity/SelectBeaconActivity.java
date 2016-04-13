package co.onlini.beacome.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.ui.SelectionWrapper;
import co.onlini.beacome.ui.adapter.BeaconsSelectRecyclerViewAdapter;
import co.onlini.beacome.ui.loader.BeaconsAsyncLoader;

public class SelectBeaconActivity extends AppCompatActivity {

    public static final String EXTRA_BEACON_LINKS = "extra_beacon_links";
    private static final int LOADER_BEACONS = 0x1;
    private static final int REQUEST_ADD_BEACONS = 0x1;
    private static final String SAVED_STATE_BEACON_LINKS = "saved_state_selected_beacon_links";

    private BeaconsSelectRecyclerViewAdapter mAdapter;

    private Set<SelectionWrapper<Beacon>> mBeacons;
    private LoaderManager.LoaderCallbacks<Beacon[]> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Beacon[]>() {
                @Override
                public Loader<Beacon[]> onCreateLoader(int id, Bundle args) {
                    return new BeaconsAsyncLoader(SelectBeaconActivity.this);
                }

                @Override
                public void onLoadFinished(Loader<Beacon[]> loader, Beacon[] data) {
                    for (Beacon beacon : data) {
                        SelectionWrapper<Beacon> wrapper =
                                new SelectionWrapper<>(beacon);
                        mBeacons.add(wrapper);
                    }
                    updateBeaconsList();
                }

                @Override
                public void onLoaderReset(Loader<Beacon[]> loader) {

                }
            };
    private MenuItem.OnMenuItemClickListener mOnMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_link:
                    finishWithResult();
                    break;
                default:
                    onBackPressed();
            }
            return true;
        }
    };

    public static Intent getIntent(Context context, Beacon[] beaconLinks) {
        Intent intent = new Intent(context, SelectBeaconActivity.class);
        intent.putExtra(EXTRA_BEACON_LINKS, beaconLinks);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_beacon);
        //noinspection ConstantConditions
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fab:
                        addBeacon();
                        break;
                }
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_cards);
        //noinspection ConstantConditions
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBeacons = new HashSet<>();
        if (savedInstanceState != null) {
            ArrayList<SelectionWrapper<Beacon>> arrayList
                    = savedInstanceState.getParcelableArrayList(SAVED_STATE_BEACON_LINKS);
            if (arrayList != null) {
                mBeacons.addAll(arrayList);
            }
        } else {
            //Get array of linked to card beacons
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(EXTRA_BEACON_LINKS)) {
                Parcelable[] parcelables = extras.getParcelableArray(EXTRA_BEACON_LINKS);
                if (parcelables != null) {
                    for (Parcelable parcelable : parcelables) {
                        Beacon beaconLink = (Beacon) parcelable;
                        mBeacons.add(new SelectionWrapper<Beacon>(beaconLink, true));
                    }
                }
            }
        }
        mAdapter = new BeaconsSelectRecyclerViewAdapter();
        mAdapter.setData(mBeacons);
        recyclerView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(LOADER_BEACONS, null, mLoaderCallbacks).forceLoad();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<SelectionWrapper<Beacon>> arrayList = new ArrayList<>(mBeacons);
        outState.putParcelableArrayList(SAVED_STATE_BEACON_LINKS, arrayList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_action_link, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_link).setOnMenuItemClickListener(mOnMenuItemClickListener);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void updateBeaconsList() {
        mAdapter.setData(mBeacons);
        mAdapter.notifyDataSetChanged();
    }

    private void finishWithResult() {
        Intent data = new Intent();
        ArrayList<Beacon> arrayList = new ArrayList<>();
        for (SelectionWrapper<Beacon> wrapper : mBeacons) {
            if (wrapper.isSelected()) {
                arrayList.add(wrapper.getItem());
            }
        }
        data.putExtra(EXTRA_BEACON_LINKS, arrayList);
        setResult(RESULT_OK, data);
        finish();
    }

    private void addBeacon() {
        startActivityForResult(AddBeaconActivity.getIntent(this), REQUEST_ADD_BEACONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_BEACONS) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Beacon beaconLink = data.getParcelableExtra(AddBeaconActivity.EXTRA_BEACON_UUID);
                if (beaconLink != null) {
                    mBeacons.add(new SelectionWrapper<>(beaconLink, true));
                    updateBeaconsList();
                }
            }
        }
    }
}
