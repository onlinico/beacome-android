package co.onlini.beacome.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Beacon;
import co.onlini.beacome.ui.OnItemClickListener;
import co.onlini.beacome.ui.activity.BeaconDetailsActivity;
import co.onlini.beacome.ui.activity.MainActivity;
import co.onlini.beacome.ui.adapter.BeaconsRecyclerViewAdapter;
import co.onlini.beacome.ui.loader.BeaconsAsyncLoader;
import co.onlini.beacome.util.SortVariants;

public class BeaconsListFragment extends SearchAndSortToolbarFragment {

    private static final int LOADER_BEACONS = 0x1;
    private BeaconsRecyclerViewAdapter mAdapter;
    private LoaderManager.LoaderCallbacks mLoaderCallbacks = new LoaderManager.LoaderCallbacks() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return new BeaconsAsyncLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {
            if (loader.getId() == LOADER_BEACONS) {
                Beacon[] array = (Beacon[]) data;
                mAdapter.setData(new ArrayList<>(Arrays.asList(array)));
                mAdapter.updateList();
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };
    private OnItemClickListener<String> mItemClickListener = new OnItemClickListener<String>() {
        @Override
        public void onItemClick(String itemId, View view) {
            startBeaconDetailsActivity(itemId);
        }
    };

    public BeaconsListFragment() {
        // Required empty public constructor
    }

    public static BeaconsListFragment newInstance() {
        return new BeaconsListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_BEACONS, null, mLoaderCallbacks);
    }

    @Override
    public void onStart() {
        setTopFragment(MainActivity.FRAGMENT_BEACONS, 0);
        getLoaderManager().getLoader(LOADER_BEACONS).forceLoad();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_beacons));
        }
        super.onStart();
    }

    private void startBeaconDetailsActivity(String itemId) {
        startActivity(BeaconDetailsActivity.getIntent(getContext(), itemId));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beacons, container, false);
        RecyclerView rvBeacons = (RecyclerView) view.findViewById(R.id.rv_beacons);
        rvBeacons.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new BeaconsRecyclerViewAdapter();
        mAdapter.setItemClickListener(mItemClickListener);
        rvBeacons.setAdapter(mAdapter);
        return view;
    }

    public void onSearchQuery(String filter) {
        mAdapter.setFilter(filter);
        mAdapter.updateList();
    }

    private void showSortPopup(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.inflate(R.menu.sort_beacon);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int sortVariant;
                switch (item.getItemId()) {
                    case R.id.sort_by_name_desc:
                        sortVariant = SortVariants.BY_NAME_DESC;
                        break;
                    case R.id.sort_by_links_count:
                        sortVariant = SortVariants.BY_CARDS_COUNT;
                        break;
                    default:
                        sortVariant = SortVariants.BY_NAME;
                }
                if (mAdapter != null) {
                    mAdapter.setSortVariant(sortVariant);
                    mAdapter.updateList();
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortPopup(getActivity().findViewById(R.id.action_sort));
            return true;
        }
        return false;
    }
}
