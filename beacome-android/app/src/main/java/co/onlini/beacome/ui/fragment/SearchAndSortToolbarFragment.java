package co.onlini.beacome.ui.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import co.onlini.beacome.R;

public abstract class SearchAndSortToolbarFragment extends NotifyActivePageFragment {

    private android.support.v7.widget.SearchView mSearchView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_search_sort, menu);
        SearchManager manager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (android.support.v7.widget.SearchView) searchMenuItem.getActionView();
        MenuItemCompat.OnActionExpandListener mListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchView.setQuery("", false);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchView, 0);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchView.setQuery("", false);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, mListener);
        mSearchView.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));
        android.support.v7.widget.SearchView.OnQueryTextListener onQueryTextListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                onSearchQuery(query);
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(onQueryTextListener);
        mSearchView.setIconifiedByDefault(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            item.getActionView().requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
        }
        return false;
    }

    public abstract void onSearchQuery(String searchString);
}
