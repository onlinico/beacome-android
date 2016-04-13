package co.onlini.beacome.ui.fragment;


import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import co.onlini.beacome.DataService;
import co.onlini.beacome.R;
import co.onlini.beacome.dal.SessionManager;
import co.onlini.beacome.databinding.FragmentBeaconDetailsBinding;
import co.onlini.beacome.event.SetCardLinksToBeaconResult;
import co.onlini.beacome.model.CardByUserAndBeaconItem;
import co.onlini.beacome.model.CardByUserItem;
import co.onlini.beacome.model.CardLink;
import co.onlini.beacome.model.Session;
import co.onlini.beacome.ui.OnItemActionClickListener;
import co.onlini.beacome.ui.activity.MainActivity;
import co.onlini.beacome.ui.activity.SelectCardsActivity;
import co.onlini.beacome.ui.adapter.CardsRecyclerViewAdapter;
import co.onlini.beacome.ui.loader.CardsByBeaconAsyncLoader;

public class BeaconDetailsFragment extends NotifyActivePageFragment {

    public static final String ARGUMENT_BEACON_UUID = "arg_beacon_uuid";
    public static final String ARGUMENT_TITLE = "arg_title";
    public static final int REQUEST_CARDS = 0x16;
    private static final int LOADER_CARDS = 0x2;
    private FragmentBeaconDetailsBinding mBinding;
    private CardsRecyclerViewAdapter mCardListAdapter;
    private List<CardByUserAndBeaconItem> mCards;
    private LoaderManager.LoaderCallbacks mLoaderCallbacks = new LoaderManager.LoaderCallbacks() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            String uuid = args.getString(ARGUMENT_BEACON_UUID);
            switch (id) {
                case LOADER_CARDS:
                    return new CardsByBeaconAsyncLoader(getContext(), uuid);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {
            switch (loader.getId()) {
                case LOADER_CARDS:
                    mCards = (List<CardByUserAndBeaconItem>) data;
                    mCardListAdapter.setData(mCards);
                    mCardListAdapter.notifyDataSetChanged();
                    getLoaderManager().destroyLoader(LOADER_CARDS);
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };
    private View.OnClickListener mOnLinkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<String> mCardsUuids = new ArrayList<>();
            for (CardByUserAndBeaconItem card : mCards) {
                mCardsUuids.add(card.getCardUuid());
            }
            Intent intent = SelectCardsActivity.getIntent(getContext(), mCardsUuids);
            startActivityForResult(intent, REQUEST_CARDS);
        }
    };
    private OnItemActionClickListener<CardByUserAndBeaconItem> mOnItemActionClickListener =
            new OnItemActionClickListener<CardByUserAndBeaconItem>() {
                @Override
                public void onItemActionClick(final CardByUserAndBeaconItem cardItem, View view) {
                    PopupMenu popup = new PopupMenu(view.getContext(), view);
                    popup.inflate(R.menu.popup_card_beacon);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_item_card_beacon_change_state:
                                    cardItem.setIsActive(!cardItem.isActive());
                                    mCardListAdapter.notifyDataSetChanged();
                                    return true;
                                case R.id.menu_item_card_beacon_unlink:
                                    mCards.remove(cardItem);
                                    mCardListAdapter.setData(mCards);
                                    mCardListAdapter.notifyDataSetChanged();
                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            };
    private String mBeaconUuid;
    public BeaconDetailsFragment() {
        // Required empty public constructor
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SetCardLinksToBeaconResult result) {
        hideProgress();
        if (!result.isSuccess()) {
            Toast.makeText(getContext(), R.string.toast_save_beacon_links_error, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.toast_save_beacon_links_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = BeaconDetailsFragment.this.getArguments();
        if (arg == null || !arg.containsKey(ARGUMENT_BEACON_UUID)) {
            throw new IllegalArgumentException("There is no expected beacon uuid argument");
        }
        mBeaconUuid = arg.getString(ARGUMENT_BEACON_UUID);
        getLoaderManager().initLoader(LOADER_CARDS, arg, mLoaderCallbacks).forceLoad();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_beacon_details, container, false);
        mBinding.btnLink.setOnClickListener(mOnLinkClickListener);
        mBinding.rvCards.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mCardListAdapter = new CardsRecyclerViewAdapter(getContext());
        mCardListAdapter.setOnItemActionClickListener(mOnItemActionClickListener);
        mBinding.rvCards.setAdapter(mCardListAdapter);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.etBeaconUuid.setText(mBeaconUuid);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        setTopFragment(MainActivity.FRAGMENT_TRANSLATOR, 0);
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            Bundle arg = BeaconDetailsFragment.this.getArguments();
            String title = arg.getString(ARGUMENT_TITLE);
            actionBar.setTitle(title);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        hideProgress();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CARDS && resultCode == Activity.RESULT_OK) {
            List<CardByUserItem> selectedCardsUuids = data.getParcelableArrayListExtra(SelectCardsActivity.EXTRA_RESULT);
            updateLinkedCardsList(selectedCardsUuids);
        }
    }

    private void updateLinkedCardsList(List<CardByUserItem> selectedCardsUuids) {
        ArrayList<CardByUserAndBeaconItem> newList = new ArrayList<>(mCards.size());
        if (selectedCardsUuids != null) {
            for (CardByUserItem card : selectedCardsUuids) {
                boolean isActive = true;
                for (CardByUserAndBeaconItem cardByUserAndBeaconItem : mCards) {
                    if (cardByUserAndBeaconItem.getCardUuid().equals(card.getCardUuid())) {
                        isActive = cardByUserAndBeaconItem.isActive();
                        break;
                    }
                }
                newList.add(new CardByUserAndBeaconItem(card.getCardUuid(), card.getTitle(), card.getDescription(),
                        card.getVersion(), card.isCurrentUserOwner(), card.getBeaconsCount(), card.getImage(), isActive));
            }

            mCards = newList;
            mCardListAdapter.setData(mCards);
            mCardListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_action_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveLinks();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveLinks() {
        showProgress(getString(R.string.dialog_progress_saving));
        ArrayList<CardLink> links = new ArrayList<>(mCards.size());
        for (CardByUserAndBeaconItem item : mCards) {
            links.add(new CardLink(item.getCardUuid(), item.isActive()));
        }
        Session session = SessionManager.getSession(getContext());
        DataService.setCardLinksToBeacon(getContext(), session, mBeaconUuid, links);
    }

}
