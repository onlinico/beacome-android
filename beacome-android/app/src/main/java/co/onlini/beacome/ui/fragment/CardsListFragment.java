package co.onlini.beacome.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.event.DeleteCardException;
import co.onlini.beacome.event.PullUserCardsResult;
import co.onlini.beacome.model.CardByUserItem;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.ui.OnItemClickListener;
import co.onlini.beacome.ui.activity.EditCardActivity;
import co.onlini.beacome.ui.activity.MainActivity;
import co.onlini.beacome.ui.adapter.CardsSortedRecyclerViewAdapter;
import co.onlini.beacome.ui.loader.UserCardsAsyncLoader;
import co.onlini.beacome.ui.view.ShareDialog;
import co.onlini.beacome.util.SortVariants;

public class CardsListFragment extends SearchAndSortToolbarFragment {

    private static final int LOADER_CARDS = 0x1;

    private CardsSortedRecyclerViewAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private OnItemClickListener<String> mOnItemClickListener = new OnItemClickListener<String>() {
        @Override
        public void onItemClick(String cardUuid, View view) {
            startEditCardActivity(cardUuid);
        }
    };
    private PopupMenu.OnMenuItemClickListener mOnSearchMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int sortVariant;
            switch (item.getItemId()) {
                case R.id.sort_by_name_desc:
                    sortVariant = SortVariants.BY_NAME_DESC;
                    break;
                case R.id.sort_by_beacons_count:
                    sortVariant = SortVariants.BY_BEACONS_COUNT;
                    break;
                default:
                    sortVariant = SortVariants.BY_NAME;
            }
            SessionManager.setPageSortOrder(getContext(), SessionManager.Pages.MY_CARDS, sortVariant);
            mAdapter.setSortBy(sortVariant);
            mAdapter.updateList();
            return true;
        }
    };
    private LoaderManager.LoaderCallbacks mLoaderCallback = new LoaderManager.LoaderCallbacks<List<CardByUserItem>>() {
        @Override
        public Loader<List<CardByUserItem>> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case LOADER_CARDS:
                    return new UserCardsAsyncLoader(getContext());
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<CardByUserItem>> loader, List<CardByUserItem> data) {
            switch (loader.getId()) {
                case LOADER_CARDS:
                    mAdapter.setData(data);
                    mAdapter.updateList();
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<List<CardByUserItem>> loader) {
        }

    };

    private OnItemActionClickListener<CardByUserItem> mOnItemActionClickListener =
            new OnItemActionClickListener<CardByUserItem>() {
                @Override
                public void onItemActionClick(final CardByUserItem card, View v) {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.inflate(R.menu.popup_card_item_owner);
                    Menu menu = popup.getMenu();
                    if (!card.isCurrentUserOwner()) {
                        menu.findItem(R.id.menu_item_card_share).setEnabled(false);
                        menu.findItem(R.id.menu_item_card_delete).setEnabled(false);
                    }
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_item_card_delete:
                                    showDeleteCardDialog(card);
                                    break;
                                case R.id.menu_item_card_share:
                                    showShareDialog(card.getCardUuid());
                                    break;
                                default:
                            }
                            return true;
                        }
                    });
                    popup.show();
                }
            };
    private TabLayout mTabLayout;

    public CardsListFragment() {
        // Required empty public constructor
    }

    public static CardsListFragment newInstance() {
        return new CardsListFragment();
    }

    private void showDeleteCardDialog(final CardByUserItem card) {
        new AlertDialog.Builder(getContext(), R.style.AlertDialog)
                .setMessage(getContext().getString(R.string.dialog_delete_card_message))
                .setPositiveButton(getContext().getString(R.string.btn_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCard(card);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void deleteCard(CardByUserItem card) {
        showProgress("Deleting");
        Session session = SessionManager.getSession(getContext());
        DataService.deleteCard(getContext(), session, card.getCardUuid());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CardsSortedRecyclerViewAdapter(getContext(),
                mOnItemClickListener, mOnItemActionClickListener);
        int sortBy = SessionManager.getPageSortOrder(getContext(), SessionManager.Pages.MY_CARDS);
        mAdapter.setSortBy(sortBy);
        getLoaderManager().initLoader(LOADER_CARDS, null, mLoaderCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_cards, container, false);
        mSwipeRefresh = ((SwipeRefreshLayout) inflatedView.findViewById(R.id.swipe));
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataService.pullUserCards(getContext(), SessionManager.getSession(getContext()));
            }
        });
        mSwipeRefresh.setColorSchemeColors(ActivityCompat.getColor(getContext(), R.color.accent));
        initTabs(inflatedView);
        inflatedView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(EditCardActivity.getEditCardIntent(getContext(), null));
            }
        });
        RecyclerView recyclerView = (RecyclerView) inflatedView.findViewById(R.id.rv_cards);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefresh.setEnabled(topRowVerticalPosition >= 0);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        return inflatedView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PullUserCardsResult result) {
        hideProgress();
        mSwipeRefresh.setRefreshing(false);
        getLoaderManager().getLoader(LOADER_CARDS).forceLoad();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeleteCardException result) {
        hideProgress();
        Toast.makeText(getContext(), R.string.toast_delete_card_error, Toast.LENGTH_SHORT).show();
    }

    private void initTabs(View inflatedView) {
        mTabLayout = (TabLayout) inflatedView.findViewById(R.id.tab_lay);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_carts_all_title));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_cards_owner_title));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.tab_cards_translator_title));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mAdapter.setPage(mTabLayout.getSelectedTabPosition());
                mAdapter.updateList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        setTopFragment(MainActivity.FRAGMENT_CARDS, 0);
        getLoaderManager().getLoader(LOADER_CARDS).forceLoad();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_cards));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mSwipeRefresh.setRefreshing(false);
        hideProgress();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.action_sort));
            popup.inflate(R.menu.sort_user_cards);
            popup.setOnMenuItemClickListener(mOnSearchMenuItemClickListener);
            popup.show();
            return true;
        }
        return false;
    }

    public void onSearchQuery(String filter) {
        mAdapter.setFilter(filter);
        mAdapter.updateList();
    }

    private void startEditCardActivity(String cardUuid) {
        startActivity(EditCardActivity.getEditCardIntent(getContext(), cardUuid));
    }

    private void showShareDialog(final String uuid) {
        ShareDialog.showShareDialog(getActivity(), new ShareDialog.ShareDialogConfirmClickListener() {
            @Override
            public void onConfirmClickListener(String email, boolean isOwner) {
                shareTo(uuid, email, isOwner);
            }
        });
    }

    private void shareTo(String uuid, String email, boolean isOwner) {
        showProgress(getContext().getString(R.string.dialog_progress_sending_share));
        Session session = SessionManager.getSession(getContext());
        DataService.addInvite(getContext(), session, uuid, email, isOwner);
    }

}
