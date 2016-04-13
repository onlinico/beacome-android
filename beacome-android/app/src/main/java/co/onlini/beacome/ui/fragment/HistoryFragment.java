package co.onlini.beacome.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.bluetooth.ScannerService;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.event.PullHistoryResult;
import co.onlini.beacome.event.RetrofitIoExceptionResult;
import co.onlini.beacome.event.ScannerServiceState;
import co.onlini.beacome.event.SetHistoryFavoriteResult;
import co.onlini.beacome.model.BeaconInfo;
import co.onlini.beacome.model.HistoryCardBase;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.ui.OnItemClickListener;
import co.onlini.beacome.ui.ServiceProvider;
import co.onlini.beacome.ui.activity.MainActivity;
import co.onlini.beacome.ui.activity.ViewCardActivity;
import co.onlini.beacome.ui.adapter.HistoryRecyclerViewAdapter;
import co.onlini.beacome.ui.loader.HistoryAsyncLoader;
import co.onlini.beacome.util.SortVariants;

public class HistoryFragment extends SearchAndSortToolbarFragment implements
        ScannerService.ScanningEventListener {

    public static final String ARGUMENT_PAGE = "arg_page";

    private final static int LOADER_HISTORY_ALL = 0x1;
    private static int mTabIndex;

    private HistoryRecyclerViewAdapter mHistoryRVAdapter;
    private ScannerService mScannerService;
    private SwipeRefreshLayout mSwipeRefresh;
    private TabLayout mTabLayout;
    private int mPageScanningSortOrder;
    private int mPageHistorySortOrder;
    private int mPageFavoriteSortOrder;

    private LoaderManager.LoaderCallbacks mCallback = new LoaderManager.LoaderCallbacks() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            switch (id) {
                case LOADER_HISTORY_ALL:
                    return new HistoryAsyncLoader(getContext());
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {
            switch (loader.getId()) {
                case LOADER_HISTORY_ALL:
                    mSwipeRefresh.setRefreshing(false);
                    mHistoryRVAdapter.setData((List) data);
                    mHistoryRVAdapter.setHistoryPage(mTabIndex);
                    Collection<String> nearCardUuids = getNearBeaconsCardsUuids();
                    switch (mTabIndex) {
                        case 0:
                            mHistoryRVAdapter.setSortBy(mPageScanningSortOrder);
                            break;
                        case 1:
                            mHistoryRVAdapter.setSortBy(mPageHistorySortOrder);
                            break;
                        case 2:
                            mHistoryRVAdapter.setSortBy(mPageFavoriteSortOrder);
                            break;
                    }
                    mHistoryRVAdapter.setNearBeaconsCardsUuids(nearCardUuids);
                    mHistoryRVAdapter.prepareData();
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {
            //do nothing
        }
    };

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ScannerServiceState result) {
        hideProgress();
        getLoaderManager().restartLoader(LOADER_HISTORY_ALL, null, mCallback);
        if (mHistoryRVAdapter != null) {
            mHistoryRVAdapter.setScannerState(result.isRunning());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PullHistoryResult result) {
        hideProgress();
        getLoaderManager().restartLoader(LOADER_HISTORY_ALL, null, mCallback);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RetrofitIoExceptionResult result) {
        hideProgress();
        mSwipeRefresh.setRefreshing(false);
        Toast.makeText(getContext(), R.string.toast_no_internet_connection, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetHistoryFavoriteResult result) {
        hideProgress();
        if (mHistoryRVAdapter != null) {
            HistoryCardBase card = mHistoryRVAdapter.getCardByUuid(result.getCardUuid());
            if (card != null) {
                card.setIsFavorite(result.isFavorite());
                mHistoryRVAdapter.prepareData();
            }
            if (!result.isSuccess()) {
                Toast.makeText(getContext(), R.string.toast_favorite_unable, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_HISTORY_ALL, null, mCallback);
        mPageScanningSortOrder = SessionManager.getPageSortOrder(getContext(), SessionManager.Pages.SCANNING);
        mPageHistorySortOrder = SessionManager.getPageSortOrder(getContext(), SessionManager.Pages.HISTORY);
        mPageFavoriteSortOrder = SessionManager.getPageSortOrder(getContext(), SessionManager.Pages.FAVORITE);
        Bundle args = getArguments();
        if (args != null) {
            mTabIndex = args.getInt(ARGUMENT_PAGE, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_history, container, false);
        initTabs(inflatedView);
        initItemList(inflatedView);
        mSwipeRefresh = ((SwipeRefreshLayout) inflatedView.findViewById(R.id.swipe));
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataService.pullHistory(getContext(), SessionManager.getSession(getContext()));
            }
        });
        mSwipeRefresh.setColorSchemeColors(ActivityCompat.getColor(getContext(), R.color.accent));
        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        setTopFragment(MainActivity.FRAGMENT_HISTORY, mTabIndex);
        getLoaderManager().restartLoader(LOADER_HISTORY_ALL, null, mCallback);
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
        }
    }

    @Override
    public void onResume() {
        if (mScannerService != null) {
            mHistoryRVAdapter.setScannerState(mScannerService.isRunning());
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        mSwipeRefresh.setRefreshing(false);
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ServiceProvider) getActivity()).getScanningService(new ServiceProvider.ServiceDst<ScannerService>() {
            @Override
            public void onServiceReady(ScannerService service) {
                mScannerService = service;
                if (mHistoryRVAdapter != null) {
                    mHistoryRVAdapter.setScannerState(mScannerService.isRunning());
                }
                mScannerService.registerObserver(HistoryFragment.this);
                updateNearDevicesList();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mScannerService != null) {
            mScannerService.unregisterObserver(this);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTab(mTabIndex);
    }

    private void initItemList(View inflatedView) {
        mHistoryRVAdapter = new HistoryRecyclerViewAdapter(getContext(), mTabIndex, SortVariants.BY_NAME);
        mHistoryRVAdapter.setTvEmptyListHolder((TextView) inflatedView.findViewById(R.id.empty_list_holder));
        RecyclerView recyclerView = ((RecyclerView) inflatedView.findViewById(R.id.rv_cards));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mHistoryRVAdapter);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefresh.setEnabled(topRowVerticalPosition >= 0);
            }
        });
        switch (mTabIndex) {
            case 0:
                mHistoryRVAdapter.setSortBy(mPageScanningSortOrder);
                break;
            case 1:
                mHistoryRVAdapter.setSortBy(mPageHistorySortOrder);
                break;
            case 2:
                mHistoryRVAdapter.setSortBy(mPageFavoriteSortOrder);
                break;
        }
        mHistoryRVAdapter.setOnItemClickListener(new OnItemClickListener<HistoryCardBase>() {
            @Override
            public void onItemClick(HistoryCardBase card, View view) {
                showViewCardActivity(card.getUuid());
            }
        });
        mHistoryRVAdapter.setOnItemMenuClickListener(new OnItemActionClickListener<HistoryCardBase>() {
            @Override
            public void onItemActionClick(HistoryCardBase item, View view) {
                changeFavorite(item);
            }
        });
    }

    private void initTabs(View inflatedView) {
        mTabLayout = (TabLayout) inflatedView.findViewById(R.id.tab_lay);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_beacon_near_title));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_beacon_history_title));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_beacon_favorite_title));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTabIndex = tab.getPosition();
                setTopFragment(MainActivity.FRAGMENT_HISTORY, mTabIndex);
                getLoaderManager().restartLoader(LOADER_HISTORY_ALL, null, mCallback);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void showSortPopup(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.inflate(R.menu.sort_history);
        if (mTabIndex == 0) {
            popup.getMenu().removeItem(R.id.sort_by_date);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int sortVariant;
                switch (item.getItemId()) {
                    case R.id.sort_by_date:
                        sortVariant = SortVariants.BY_DATE;
                        break;
                    case R.id.sort_by_name_desc:
                        sortVariant = SortVariants.BY_NAME_DESC;
                        break;
                    case R.id.sort_by_name:
                    default:
                        sortVariant = SortVariants.BY_NAME;
                }
                int page = SessionManager.Pages.HISTORY;
                switch (mTabIndex) {
                    case 0:
                        mPageScanningSortOrder = sortVariant;
                        page = SessionManager.Pages.SCANNING;
                        break;
                    case 1:
                        mPageHistorySortOrder = sortVariant;
                        page = SessionManager.Pages.HISTORY;
                        break;
                    case 2:
                        mPageFavoriteSortOrder = sortVariant;
                        page = SessionManager.Pages.FAVORITE;
                        break;
                }
                SessionManager.setPageSortOrder(getContext(), page, sortVariant);
                mHistoryRVAdapter.setSortBy(sortVariant);
                mHistoryRVAdapter.prepareData();
                return true;
            }
        });
        popup.show();
    }

    public void setTab(int index) {
        mTabIndex = index;
        if (mTabLayout != null && this.isVisible() && getContext() != null) {
            TabLayout.Tab tab = mTabLayout.getTabAt(index);
            if (tab != null) {
                tab.select();
            }
        }
    }

    @Override
    public void onBeaconFound(BeaconInfo beaconInfo) {
        //do nothing
    }

    @Override
    public void onBeaconLost(BeaconInfo beaconInfo) {
        //do nothing
    }

    @Override
    public void onNearBeaconsCardsListChanged() {
        updateNearDevicesList();
    }

    public void updateNearDevicesList() {
        if (mHistoryRVAdapter != null) {
            Collection<String> nearCardUuids = getNearBeaconsCardsUuids();
            mHistoryRVAdapter.setNearBeaconsCardsUuids(nearCardUuids);
            if (mTabIndex == 0) {
                getLoaderManager().restartLoader(LOADER_HISTORY_ALL, null, mCallback);
            }
        }
    }

    private Collection<String> getNearBeaconsCardsUuids() {
        Collection<String> cardsUuid;
        if (mScannerService != null) {
            cardsUuid = mScannerService.getNearBeaconsCardsUuids();
        } else {
            cardsUuid = new ArrayList<>();
        }
        return cardsUuid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortPopup(getActivity().findViewById(R.id.action_sort));
            return true;
        }
        return false;
    }

    private void showViewCardActivity(String cardUuid) {
        startActivity(ViewCardActivity.getViewIntent(getContext(), cardUuid));
    }

    public void onSearchQuery(String filter) {
        if (mHistoryRVAdapter != null) {
            mHistoryRVAdapter.setFilter(filter);
            mHistoryRVAdapter.prepareData();
        }
    }

    private void changeFavorite(final HistoryCardBase item) {
        final boolean isFavorite = !item.isFavorite();
        final Session session = SessionManager.getSession(getContext());
        item.setIsFavorite(isFavorite);
        //mHistoryRVAdapter.prepareData();
        showProgress("Sending request");
        DataService.setHistoryFavorite(getContext(), session, item.getUuid(), isFavorite);
    }
}
