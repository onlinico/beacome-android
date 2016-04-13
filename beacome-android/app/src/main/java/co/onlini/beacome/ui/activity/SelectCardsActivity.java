package co.onlini.beacome.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.onlini.beacome.R;
import co.onlini.beacome.model.CardByUserItem;
import co.onlini.beacome.ui.SelectionWrapper;
import co.onlini.beacome.ui.adapter.CardsSelectRecyclerViewAdapter;
import co.onlini.beacome.ui.loader.UserCardsAsyncLoader;

public class SelectCardsActivity extends AppCompatActivity {

    public static final String EXTRA_LINKED_CARDS_UUIDS = "co.onlini.beaconproject.extra_args_cards";
    public static final String EXTRA_RESULT = "co.onlini.beaconproject.extra_result_cards";
    private static final int LOADER_CARDS = 0x1;
    private static final String SAVED_STATE_SELECTED_CARDS_UUIDS = "saved_state_cards";
    private CardsSelectRecyclerViewAdapter mAdapter;
    private Set<SelectionWrapper<CardByUserItem>> mCards;
    private ArrayList<String> mLinkedCards;
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

    private LoaderManager.LoaderCallbacks<List<CardByUserItem>> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<CardByUserItem>>() {
                @Override
                public Loader<List<CardByUserItem>> onCreateLoader(int id, Bundle args) {
                    return new UserCardsAsyncLoader(SelectCardsActivity.this);
                }

                @Override
                public void onLoadFinished(Loader<List<CardByUserItem>> loader, List<CardByUserItem> data) {
                    for (CardByUserItem card : data) {
                        boolean isSelected = mLinkedCards.contains(card.getCardUuid());
                        SelectionWrapper<CardByUserItem> wrapper =
                                new SelectionWrapper<>(card, isSelected);
                        mCards.add(wrapper);
                    }
                    updateBeaconsList();
                }

                @Override
                public void onLoaderReset(Loader<List<CardByUserItem>> loader) {

                }
            };

    public static Intent getIntent(Context context, ArrayList<String> linkedCards) {
        Intent intent = new Intent(context, SelectCardsActivity.class);
        intent.putExtra(EXTRA_LINKED_CARDS_UUIDS, linkedCards);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_cards);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }
        mCards = new HashSet<>();
        if (savedInstanceState != null) {
            mLinkedCards = savedInstanceState.getStringArrayList(SAVED_STATE_SELECTED_CARDS_UUIDS);
        } else {
            //Get array of linked to card beacons
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(EXTRA_LINKED_CARDS_UUIDS)) {
                mLinkedCards = extras.getStringArrayList(EXTRA_LINKED_CARDS_UUIDS);
            }
        }
        if (mLinkedCards == null) {
            mLinkedCards = new ArrayList<>();
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new CardsSelectRecyclerViewAdapter(this);
        recyclerView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(LOADER_CARDS, null, mLoaderCallbacks).forceLoad();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLinkedCards.clear();
        for (SelectionWrapper<CardByUserItem> selectableCard : mCards) {
            if (selectableCard.isSelected()) {
                mLinkedCards.add(selectableCard.getItem().getCardUuid());
            }
        }
        outState.putStringArrayList(SAVED_STATE_SELECTED_CARDS_UUIDS, mLinkedCards);
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
        mAdapter.setData(new ArrayList<>(mCards));
        mAdapter.notifyDataSetChanged();
    }

    private void finishWithResult() {
        mLinkedCards.clear();
        ArrayList<CardByUserItem> selectedCards = new ArrayList<>();
        for (SelectionWrapper<CardByUserItem> selectableCard : mCards) {
            if (selectableCard.isSelected()) {
                selectedCards.add(selectableCard.getItem());
            }
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT, selectedCards);
        setResult(RESULT_OK, data);
        finish();
    }

}
