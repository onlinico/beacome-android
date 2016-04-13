package co.onlini.beacome.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import co.onlini.beacome.R;
import co.onlini.beacome.ui.fragment.BeaconDetailsFragment;

public class BeaconDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BEACON_UUID = "extra_history_card";
    private static final String TOP_FRAGMENT = "tag_top_fragment";

    public static Intent getIntent(Context context, String beaconUuid) {
        Intent intent = new Intent(context, BeaconDetailsActivity.class);
        intent.putExtra(EXTRA_BEACON_UUID, beaconUuid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_details);
        initToolBar();
        initFragment();
    }

    private void initFragment() {
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(BeaconDetailsActivity.EXTRA_BEACON_UUID)) {
            throw new IllegalArgumentException("No expected extras, beaconUuid");
        }
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(TOP_FRAGMENT) == null) {
            String uuid = extras.getString(EXTRA_BEACON_UUID);
            String title = getString(R.string.title_activity_beacon_edit);
            Bundle fragmentArg = new Bundle();
            fragmentArg.putString(BeaconDetailsFragment.ARGUMENT_BEACON_UUID, uuid);
            fragmentArg.putString(BeaconDetailsFragment.ARGUMENT_TITLE, title);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = new BeaconDetailsFragment();
            fragment.setArguments(fragmentArg);
            transaction.add(R.id.lay_fragment_container, fragment, TOP_FRAGMENT);
            transaction.commit();
        }
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
